package agentes;

import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.TickerBehaviour;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Vector;

import org.jgap.IChromosome;
import org.jgap.impl.CompositeGene;
import org.jgap.Gene;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import geneticos.GlobalOpt;
import geneticos.RestrLocales;
import jade.core.AID;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.Property;
import jade.domain.FIPAAgentManagement.SearchConstraints;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import linearprogramming.RestrLocalesLP;

public class Broker extends Agent{
	
	// Recibe proceso del consumidor y función de utilidad
	// Parsea y establece criterios locales de optimización
	// Envía información a los proveedores
	// Recibe respuestas de los proveedores
	// Encuentra los mejores servicios
	// Cierra acuerdos con los proveedores
	// Envia respuestas a consumidor
	
	private static final long serialVersionUID = -7419979154495603904L;
	
	private AID[] proveedores;
	private AID consumidor;
	private long tiempo_i;
	private long tiempo_f;
	private String[] servicios; 
	private String name = "";
	private double[] rLocales;
	private double[] parametros;
	private double[] rGlobales;
	private int[] tipos;
	private int[] iter;
	private double[] probab;
	private int nServ;
	//private int niveles = 50;
	private AID[] ofertantes;
	
	
	protected void setup(){
		tiempo_i = System.currentTimeMillis();
		System.out.println("Hola! El bróker "+getAID().getName()+" está listo.");
		// Registro al broker en el DF
		this.registrarEnDF();
		
		
		addBehaviour(new TickerBehaviour(this, 300){
			
			//UID 
			private static final long serialVersionUID = 1L;

			public void onTick() {
				Object[] args = getArguments();
				int n=0;
				if (args != null && args.length > 0) {
					n = (int) args[0];
				}
				
				MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.INFORM);
				ACLMessage msg = myAgent.receive(mt);
				
				if (msg != null) {
					name = msg.getContent();
					consumidor = msg.getSender();
					
					// Aquí obtengo proceso
					
					obtenerProceso(name);
					tiempo_f = System.currentTimeMillis();
					System.out.println("El broker obtuvo el proceso a los (milisegundos) "+ ( tiempo_f - tiempo_i ));
					// Busco proveedores
					// Busco los servicios disponibles
									
					String[][] arregloServ = buscarProveedoresServiciosDisponibles(n);
					System.out.println("Se encontraron "+proveedores.length);
					System.out.println("Se encontraron "+arregloServ.length);
					
					tiempo_f = System.currentTimeMillis();
					System.out.println("El broker buscó servicios disponibles a los "+ ( tiempo_f - tiempo_i ) +" milisegundos.");
					
					
					//Determino restricciones locales
					rLocales = generarRestricciones(50, arregloServ); // niveles y arreglo de serv
					for(int i =0;i<rLocales.length;i++){
						System.out.println("La restricción "+i +" quedó con el valor "+ rLocales[i]);
					}
					tiempo_f = System.currentTimeMillis();
					System.out.println("El broker generó las restricciones a los (milisegundos) "+ ( tiempo_f - tiempo_i ));
					
					// Numero de nodos, tipos de nodo, restricciones locales, actividades (servicios)
					agregarRestrRequerimiento(rLocales, name);
					tiempo_f = System.currentTimeMillis();
					System.out.println("El broker agregó las restricciones al requerimiento a los (milisegundos) "+ ( tiempo_f - tiempo_i ));
					
					addBehaviour(new RealizaRequerimiento());
					
				}// Cierra if
				
			}// Cierra onTick()
			
			
			
		});
		

	}// Cierra el setup
	public void registrarEnDF(){
		DFAgentDescription dfd = new DFAgentDescription();
		dfd.setName( getAID() );
		ServiceDescription sd  = new ServiceDescription();
		sd.setType( "broker" );
		sd.setName( getLocalName() );
		dfd.addServices(sd);
		try {
			DFService.register(this, dfd );
			}
		catch (FIPAException fe) { 
			fe.printStackTrace(); 
		}
	}
	public void obtenerProceso(String name){
		JSONParser parser = new JSONParser();
		try {
			Object obj = parser.parse(new FileReader("C:\\Users\\Camilo\\Desktop\\Eclipse\\JSON\\procesos\\"+name+".json"));
			JSONObject jsonObject = (JSONObject) obj;
			
			long nnodos = (long) jsonObject.get("NNodos");
			nServ = (int) (long) nnodos;
			JSONObject restrObject = (JSONObject) jsonObject.get("Restricciones");
			JSONArray paramObject = (JSONArray) jsonObject.get("Parametros");
			JSONArray probabObject = (JSONArray) jsonObject.get("Probabilidades");
			JSONArray iterObject = (JSONArray) jsonObject.get("Iteraciones");
			JSONArray tipoObject = (JSONArray) jsonObject.get("Tipo");
			JSONArray actividad = (JSONArray) jsonObject.get("Actividad");
		
			Iterator<Double> iterator2 = paramObject.iterator();
			Iterator<Double> iterator3 = probabObject.iterator();
			Iterator iterator4 = iterObject.iterator();
			Iterator iterator5 = tipoObject.iterator();
			Iterator<String> iterator = actividad.iterator();
			
			rGlobales = new double[10];
			rGlobales[0] = (double) restrObject.get("tiempo");
			rGlobales[1] = (double) restrObject.get("dispo");
			rGlobales[2] = (double) restrObject.get("through");
			rGlobales[3] = (double) restrObject.get("exito");
			rGlobales[4] = (double) restrObject.get("confiab");
			rGlobales[5] = (double) restrObject.get("confor");
			rGlobales[6] = (double) restrObject.get("mejorespr");
			rGlobales[7] = (double) restrObject.get("latencia");
			rGlobales[8] = (double) restrObject.get("documentacion");
			rGlobales[9] = (double) restrObject.get("presupuesto");
			
			int cont2 = 0;
			parametros = new double[9];
			while(iterator2.hasNext()){
				parametros[cont2]= (double) iterator2.next();
				cont2++;
			}
			int cont3 = 0;
			probab = new double[nServ];
			while(iterator3.hasNext()){
				probab[cont3]=(double) iterator3.next();
				cont3++;
			}
			int cont4 = 0;
			iter = new int[nServ];
			while(iterator4.hasNext()){
				long aux = (long) iterator4.next();
				iter[cont4] = (int) aux;
				cont4++;
			}
			int cont5 = 0;
			tipos = new int[nServ];
			while(iterator5.hasNext()){
				long aux = (long) iterator5.next();
				tipos[cont5] = (int) aux;
				cont5++;
			}
			servicios = new String[nServ];
			int cont =0;
			while (iterator.hasNext()) {
				servicios[cont]=iterator.next();
				cont++;
			}
		} catch (FileNotFoundException e) {
			//manejo de error
		} catch (IOException e) {
			//manejo de error
		} catch (ParseException e) {
			//manejo de error
		}
	}
	public void buscarProveedores(){
		DFAgentDescription template = new DFAgentDescription();
		ServiceDescription sd = new ServiceDescription();
		SearchConstraints getAll = new SearchConstraints();
		getAll.setMaxResults(new Long(1000));
		sd.setType("proveedor");
		template.addServices(sd);
		DFAgentDescription[] result;
		try {
			result = DFService.search(this, template, getAll); 
			System.out.println("Se encontraron los siguientes proveedores:");
			proveedores = new AID[result.length];
			for (int i = 0; i < result.length; ++i) {
				proveedores[i] = result[i].getName();
				System.out.println(proveedores[i].getName());
			}
		}
		catch (FIPAException fe) {
			fe.printStackTrace();
		}
	}
	
	public ArrayList<AID> buscarProveedores2(int n){
		ArrayList<AID> proveedores = new ArrayList<AID>();
		System.out.println("Se encontraron los siguientes proveedores:");
		for(int i = 0;i < n;i++){
			String s = "Proveedor"+i;
			DFAgentDescription template = new DFAgentDescription();
			ServiceDescription sd = new ServiceDescription();
			SearchConstraints getAll = new SearchConstraints();
			getAll.setMaxResults(new Long(-1));
			sd.setName(s);
			sd.setType("proveedor");
			template.addServices(sd);
			
			DFAgentDescription[] result;
			try {
				result = DFService.search(this, template, getAll); 
				for (int j = 0; j < result.length; ++j) {
					proveedores.add(result[j].getName());
					int t = proveedores.size();
					System.out.println(proveedores.get(t-1));
				}
			}catch(Exception e){
				e.printStackTrace();
			}
		}	
		return proveedores;
	}
	
	public String[][] buscarProveedoresServiciosDisponibles(int n){
		proveedores = new AID[n];
		ArrayList<ArrayList> prop = new ArrayList<ArrayList>();
		for(int i = 0;i< n;i++){
			String s = "Proveedor"+i;
			DFAgentDescription template2 = new DFAgentDescription();
			//ArrayList<ArrayList> prop = new ArrayList<ArrayList>();
			ServiceDescription sd1 = new ServiceDescription();
			sd1.setName(s);
			sd1.setType("proveedor");
			template2.addServices(sd1);
			try{
				 DFAgentDescription[] result = DFService.search(this, template2);
				 //System.out.println("Encontré "+result.length+ " proveedores.");
				 for(int j = 0;j < result.length;j++){
					 DFAgentDescription aux = result[j];
					 proveedores[i]= aux.getName();
		        	 //System.out.println("Encuentro al "+ proveedores[i].getLocalName());
					 Iterator<ServiceDescription> iterServices = aux.getAllServices();
		        	 while(iterServices.hasNext()){
		        		 ServiceDescription aux2 = iterServices.next();
		        		 //System.out.println("El servicio rescatado es " + aux2.getName());
		        		 Iterator<Property> iterProp = aux2.getAllProperties();
		        		 ArrayList propServ = new ArrayList();
		        		 propServ.add(aux2.getName());
		        		 while(iterProp.hasNext()){
		        			 Property aux3 = iterProp.next();
		        			 propServ.add(aux3.getValue());
		        			 //System.out.println("La propiedad rescatada es " +aux3.getName()+" con el valor de "+aux3.getValue());
		        		 }
		        		 if(propServ.size()>9){
		        			 //System.out.println("Agrego el servicio " + propServ.get(0));
		        			 prop.add(propServ);
		        		 }
		        	 }
		         } 	 
			}catch(Exception e){
				e.printStackTrace();
			}
		}
		String[][] s = new String[prop.size()][10];
		for(int i = 0;i < prop.size();i++){
			ArrayList aux1 = prop.get(i);
			//System.out.println("El arreglo de propiedades tiene un largo de "+aux1.size()+".");
			for(int j = 0;j < aux1.size();j++){
				//System.out.println("La propiedad a rescatar tiene el valor "+ aux1.get(j) +"." );
				Object aux = aux1.get(j);
				s[i][j] = aux.toString();	
			}
		}
		return s;
	}
	
	public String[][] buscarServiciosDisponibles(){
		//System.out.println("Entro a buscar los servicios");
		DFAgentDescription template2 = new DFAgentDescription();
		//System.out.println("Creo un AgentDescription");
		ArrayList<ArrayList> prop = new ArrayList<ArrayList>();
		//System.out.println("Creo un ArrayList");
		//System.out.println("Creo un ArrayList de ArrayLists");
		//for(int i = 1;i<=nServ;i++){
			ServiceDescription sd1 = new ServiceDescription();
			//System.out.println("Creo un ServiceDescription");
			sd1.setType("proveedor");
			//System.out.println("Seteo el tipo del SD a " + sd1.getType());
			template2.addServices(sd1);
			//System.out.println("Agrego SD a AD");
			try{
				 DFAgentDescription[] result = DFService.search(this, template2);
		         if (result.length>0){
		             System.out.println("Se encontraron "+result.length+" agentes");
		         }
		         for(int j = 0;j < result.length;j++){
		        	 Iterator<ServiceDescription> iterServices = result[j].getAllServices();
		        	 while(iterServices.hasNext()){
		        		 ServiceDescription aux = iterServices.next();
		        		 //System.out.println("El servicio rescatado es " + aux.getName());
		        		 Iterator<Property> iterProp = aux.getAllProperties();
		        		 ArrayList propServ = new ArrayList();
		        		 propServ.add(aux.getName());
		        		 while(iterProp.hasNext()){
		        			 Property aux2 = iterProp.next();
		        			 propServ.add(aux2.getValue());
		        			// System.out.println("La propiedad rescatada es " +aux2.getName()+" con el valor de "+aux2.getValue());
		        		 }
		        		 if(propServ.size()>9){
		        			 //System.out.println("Agrego el servicio " + propServ.get(0));
		        			 prop.add(propServ);
		        		 }
		        	 }
		         }
		            
			}catch(FIPAException fe){
				fe.printStackTrace();
			}
		//}
		System.out.println("Se encontraron "+prop.size()+" servicios.");
		
		String[][] s = new String[prop.size()][10];
		for(int i = 0;i < prop.size();i++){
			ArrayList aux1 = prop.get(i);
			//System.out.println("El arreglo de propiedades tiene un largo de "+aux1.size()+".");
			for(int j = 0;j < aux1.size();j++){
				//System.out.println("La propiedad a rescatar tiene el valor "+ aux1.get(j) +"." );
				Object aux = aux1.get(j);
				s[i][j] = aux.toString();	
			}
		}
		return s;
	}
	public double[] generarRestricciones(int niveles, String[][] arregloServ){
		double[] r = new double[nServ*9];
		
		/*
		RestrLocales re = new RestrLocales(nServ, arregloServ, parametros, tipos, iter, probab, rGlobales);
		try {
			IChromosome resultado = re.restrOptimas(niveles);// nro de niveles de calidad para un atributo
			Gene[] genes = new Gene[nServ*9];
			genes = resultado.getGenes();
			//System.out.println("Se recibe el resultado, el primer gen es "+genes[0].getAllele());
			for(int i = 0;i < genes.length;i++){
				//System.out.println("Se recibe el resultado, el primer gen es "+genes[0].getAllele());
				r[i] = (double) genes[i].getApplicationData();	
		    }
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}*/
		
		
		
		RestrLocalesLP rL = new RestrLocalesLP(50, nServ, arregloServ, parametros, tipos, iter, probab, rGlobales);
		
		double[][][] r1 = rL.optimizar();

		for(int i = 0;i < r1.length;i++){
			for(int j = 0;j < r1[0].length;j++){
				for(int k = 0;k < r1[0][0].length;k++){
					System.out.println(r1[i][j][k]);
				}
			}
		}
		
		return r;
	}
	public void agregarRestrRequerimiento(double [] rLocales, String name){
		JSONArray rL = new JSONArray();
		for(int i = 0;i<rLocales.length;i++){
			rL.add(rLocales[i]);
		}
		JSONParser parser2 = new JSONParser();
		try {
			Object obj = parser2.parse(new FileReader("C:\\Users\\Camilo\\Desktop\\Eclipse\\JSON\\procesos\\"+name+".json"));
			JSONObject jsonObject = (JSONObject) obj;
			jsonObject.put("Locales", rL);
			
			FileWriter file = new FileWriter("C:\\Users\\Camilo\\Desktop\\Eclipse\\JSON\\requerimientos\\requerimiento"+name+".json");
			file.write(jsonObject.toJSONString());
			file.flush();
			file.close();
			
		} catch (FileNotFoundException e) {
			//manejo de error
		} catch (IOException e) {
			//manejo de error
		} catch (ParseException e) {
			//manejo de error
		}
	}
	
	protected void takeDown() {
		try {
			DFService.deregister(this);
		}
		catch (FIPAException fe) {
			fe.printStackTrace();
		}
		
		System.out.println("Broker "+getAID().getName()+" terminado.");
		tiempo_f = System.currentTimeMillis();
		System.out.println("El broker ha trabajado "+ ( tiempo_f - tiempo_i ) +" milisegundos.");
		
	}
	
	private class RealizaRequerimiento extends Behaviour {
		
		private static final long serialVersionUID = 4141739597869771816L;
		private MessageTemplate mt; 
		//private int paso = 0;
		public double[] preciosBajos = new double[nServ];
		//int[] posBajos = new int[nServ];
		
		public AID[] bestProveedores = new AID[nServ];
		
		public int[] bundling;
		@Override
		public void action() {
			ofertantes = new AID[proveedores.length]; // Arreglo para guardar proveedores que enviaron oferta	
			
			// Arreglo de ofertas
			//ofertas = new String[proveedores.length][2];
			ArrayList<ArrayList> ofertas0 = new ArrayList<ArrayList>();
			
			for(int i = 0;i<nServ;i++){
				preciosBajos[i] = 10.0;
			}
			
				boolean enviado = false;
				
				enviado = enviarCFP(name);
				tiempo_f = System.currentTimeMillis();
				System.out.println("El broker envió el requerimiento a los "+ ( tiempo_f - tiempo_i ) +" milisegundos.");
				
				//paso = 1;


				String[] ofertasRecibidas = recibirOfertas();
				tiempo_f = System.currentTimeMillis();
				ofertas0 = parsearOfertas(ofertasRecibidas);
				System.out.println("El broker recibió y parseó la oferta a los "+ ( tiempo_f - tiempo_i ) +" milisegundos.");
				
				/*for(int i = 0;i < ofertas.length;i++){
					System.out.println("Oferta por "+ofertas[i][0]+" tiene precio "+ofertas[i][1]);	
				}
				*/
				//if (cont >= proveedores.length) {
				System.out.println("Se obtuvieron "+ofertas0.size()+" ofertas");
				//for(int i = 0;i < ofertas0.size();i++){
				//	System.out.println("La oferta "+i+ " es "+ofertas0.get(i));
				//}
				
				elegirMejoresOfertas(ofertas0, servicios);
				//} Cierra if 
				tiempo_f = System.currentTimeMillis();
				System.out.println("El broker eligió las mejores ofertas a los "+ ( tiempo_f - tiempo_i ) +" milisegundos.");
				// Acepto propuestas de mejores ofertas
				enviarAccept(bestProveedores);
				tiempo_f = System.currentTimeMillis();
				System.out.println("El broker notificó a los elegidos a los "+ ( tiempo_f - tiempo_i ) +" milisegundos.");
				
				// Informo al consumidor
				informoResultados(consumidor);
				tiempo_f = System.currentTimeMillis();
				System.out.println("El broker informó resultados a los "+ ( tiempo_f - tiempo_i ) +" milisegundos.");
				
				AID[] noProvee = obtenerNoProveedores();
				enviarRefuse(noProvee);
				
				
				// Recibo confirmación/respuesta
				reciboAprob();
				tiempo_f = System.currentTimeMillis();
				System.out.println("El broker recibió aprobación de los proveedores a los "+ ( tiempo_f - tiempo_i ) +" milisegundos.");				
				takeDown();
				
				
		}// Cierra behaviour
		

		@Override
		public boolean done() {
			
			return false;
		}
		
		public void enviarRefuse(AID[] noProvee){
			ACLMessage refuse = new ACLMessage(ACLMessage.REFUSE);
			for(AID prov: noProvee){
				refuse.addReceiver(prov);				
			}
			refuse.setContent("refuse");
			refuse.setConversationId("service-trade");
			send(refuse);
		}
		public AID[] obtenerNoProveedores(){
			
			AID[] noProvee = new AID[(proveedores.length-bestProveedores.length)];
			for(int i = 0;i < noProvee.length;i++){
				for(AID prov: proveedores){
					for(AID prov2: bestProveedores){
						if(!(prov.equals(prov2))){
							noProvee[i]=prov;
						}
					}
				}
			}
			return noProvee;
		}
		
		public boolean enviarCFP(String name){
			ACLMessage cfp = new ACLMessage(ACLMessage.CFP);
			for (int i = 0; i < proveedores.length; ++i) {
				cfp.addReceiver(proveedores[i]);
			}
			cfp.setContent("requerimiento"+name);
			cfp.setConversationId("service-trade");
			cfp.setReplyWith("cfp"+System.currentTimeMillis()); 
			send(cfp);
			
			return true;
		}
		public String[] recibirOfertas(){
			int cont =0; 
			String[] ofertasRecibidas= new String[ofertantes.length];
			while(cont<proveedores.length){
				ACLMessage reply = myAgent.receive(mt);
				if (reply != null) {
					tiempo_f = System.currentTimeMillis();
					//System.out.println("Se recibe la oferta "+cont+" a los " +(tiempo_f-tiempo_i)+" milisegundos.");
					
					if(cont == 0 || !(ofertantes[cont-1].equals(ofertantes[cont]))){
						String oferta = reply.getContent();
						//System.out.println("El contenido del mensaje es "+oferta);
					//ofertantes.add(reply.getSender());
						ofertantes[cont] = reply.getSender();
						//System.out.println("Se recibe la oferta de "+ofertantes[cont].getName());
				
						// Son propuestas
						if (reply.getPerformative() == ACLMessage.PROPOSE) {
							ofertasRecibidas[cont] = oferta;
							//parsearOferta(oferta, cont, ofertas);
							//System.out.println("Oferta por "+ofertas[cont][0]+" tiene precio "+ofertas[cont][1]);
							cont++;
						} // Contenido mensaje
						else if(reply.getPerformative() == ACLMessage.REFUSE){
							
							ofertasRecibidas[cont]=oferta;
							cont++;
						}
					}
				}
			}
			System.out.println("Se recibieron "+ofertasRecibidas.length+" ofertas.");
			return ofertasRecibidas;
		}
		public ArrayList<ArrayList> parsearOfertas(String[] ofertasRecibidas){
			//System.out.println("Se ingresa a parsear ofertas." );
			ArrayList<ArrayList> ofertas0=new ArrayList<ArrayList>();
			//System.out.println("Se crea un arraylist." );
			
			bundling = new int[ofertasRecibidas.length];
			for(int i = 0;i < ofertasRecibidas.length;i++){
				
				if(ofertasRecibidas[i].equals("NO PARTICIPO")){
					System.out.println("Oferta vacía");
				}else{
				JSONParser parser = new JSONParser();
				try {			
					//System.out.println("Entra a leer el archivo "+ ofertasRecibidas[i]+".");
					FileReader f = new FileReader("C:\\Users\\Camilo\\Desktop\\Eclipse\\JSON\\ofertas\\"+ofertasRecibidas[i]+".json");
					//System.out.println("Creo un filereader.");
					Object objOferta = parser.parse(f);
					JSONObject jsonOferta = (JSONObject) objOferta;
					//System.out.println("Abre el archivo "+  ofertasRecibidas[i]);
					//int cont2 = 0;
					
					ArrayList ofer;
					long n = (long) jsonOferta.get("n");
					int n0 = (int) n; // rescato el número de servicios de la oferta
					bundling[i]=n0;
					
					//System.out.println("La oferta "+ofertasRecibidas[i]+" tiene " + bundling[i]+ " servicios" );
					System.out.println("La oferta " + ofertasRecibidas[i] + " tiene "+ n0 + " servicios");
					JSONArray ofert = (JSONArray) jsonOferta.get("serv"); // rescato los nombres de los servicios en la oferta
					for(int j = 0;j < n0;j++){// por cada elemento en el archivo de la oferta
						String nombre = (String) ofert.get(j);// rescato el nombre del servicio
						//System.out.println("El servicio "+ j+" tiene nombre "+ nombre);
						JSONArray atrib = (JSONArray) jsonOferta.get(nombre);
						ofer = new ArrayList();
						for(int k = 0;k < 13;k++){
							System.out.println("Se rescata el atributo "+k + " del servicio "+nombre+" cuyo valor es "+atrib.get(k));
							ofer.add(atrib.get(k));
						// El orden es: atributos (0 a 8), nombreAgente (9), nombre (10), id (11), precio (12), indicador de la oferta como (13)
						}
						System.out.println("La oferta "+ofertasRecibidas[i]+" tiene como id "+i);
						ofer.add(i);
						//agrego el indicador de la oferta como (13)
						ofertas0.add(ofer);
					}
				
				} catch (FileNotFoundException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
					//manejo de error
				} catch (ParseException e) {
					e.printStackTrace();
					//manejo de error
				}
				}
			}
			return ofertas0;
		}
	
		public void elegirMejoresOfertas(ArrayList ofertas, String[] nombres){
			
			GlobalOpt opt = new GlobalOpt(parametros, tipos, rGlobales, iter, probab, ofertas, bundling, nombres);
			
			String[] nombresProveedores = opt.obtenerNombre(ofertas);
			
			ArrayList ofertasOrdenadas = opt.obtenerOfertasOrdenadas();
			
			/*for(int i = 0;i < nombresProveedores.length;i++){
			*	System.out.println("El nombre del proveedor del serv "+i+" es "+nombresProveedores[i]);
			}*/
			String[] id = new String[nServ];
			int[] idOferta = new int[nServ];
			try {
				IChromosome resultado = opt.servOptimos();
				String[][] ofertas2 = opt.setOfertas(ofertas);
				
				String[][] valores = new String[nServ][14];
				
 				Gene[] genes = new Gene[nServ];
				genes = resultado.getGenes();
				for(int i = 0;i < genes.length;i++){
					int aux = (int) genes[i].getAllele();
					ArrayList aux0 = (ArrayList) ofertasOrdenadas.get(i);
					//System.out.println(aux0);
					int posicion = (int) aux0.get(aux);
					for(int j =0;j < valores[0].length;j++){
						valores[i][j]= ofertas2[posicion][j];
						//System.out.println(valores[i][j]);
						
					}
					// El orden es: atributos (0 a 8), nombreAgente (9), nombre (10), id (11), precio (12), indicador de la oferta como (13)
					idOferta[i]= Integer.parseInt(ofertas2[posicion][13]);
					preciosBajos[i]= Double.parseDouble(ofertas2[posicion][12]); // rescato el precio del servicio
					id[i]=ofertas2[posicion][9]+"_"+ofertas2[posicion][10]+"_"+ofertas2[posicion][11]+"_"+ofertas2[posicion][13];
				}
				
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			for(int i = 0;i < id.length;i++){
				String[] id1 = id[i].split("_");
				System.out.println("Se eligió la oferta del "+id1[0]+" para el "+id1[1]+" cuyo id es "+id1[2]);
				for(int j = 0;j < ofertantes.length;j++){
					String[] nombreAgente = ofertantes[j].getName().split("@");
					boolean b = nombreAgente[0].equals(id1[0]);
					if(b){bestProveedores[i]= ofertantes[j];}
				}
				System.out.println("El proveedor es "+ id1[0]+" se setea el mejor ofertante como " + bestProveedores[i].getName());
			}
			
		}
		public boolean enviarAccept(AID[] bestProveedores){
			ACLMessage order = new ACLMessage(ACLMessage.ACCEPT_PROPOSAL);
			order.setContent("Acepto");
			
			for (int i = 0; i < nServ; ++i) {
				//AID aux = (AID) mejProv1.get(i);
				order.addReceiver(bestProveedores[i]);
			}
			myAgent.send(order);
			System.out.println("Broker acepta ofertas.");
			return true;
		}
		public void reciboAprob(){
			ACLMessage reply2 = myAgent.receive(mt);
			//AID aux = reply2.getSender();
			int cont2 = 0;
			while(cont2<bestProveedores.length){
				if (reply2 != null) {
					if (reply2.getPerformative() == ACLMessage.INFORM) {
						cont2++;
						System.out.println("Recibo una aprobación");
					}	
				}	 
			}
			tiempo_f=System.currentTimeMillis();
			System.out.println("Recibo aprobación de todos a los "+(tiempo_f-tiempo_i)+" milisegundos.");//paso = 4;
			
		}
		public void informoResultados(AID consumidor){
			ACLMessage confirm = new ACLMessage(ACLMessage.INFORM);
			confirm.addReceiver(consumidor);
			System.out.println("Las ofertas aceptadas fueron de:");
			for(int i = 0;i < bestProveedores.length;i++){
				System.out.println(bestProveedores[i].getName());
			}
			double suma =0.0;
			for(int i = 0;i< preciosBajos.length;i++){
				suma+=preciosBajos[i];
			}
			System.out.println("Precio: "+suma);
			
			JSONObject resultados = new JSONObject();
			JSONArray jsonProv = new JSONArray();
			for(int i = 0;i< bestProveedores.length;i++){
				jsonProv.add(bestProveedores[i]);
			}
			try {
				
				resultados.put("Proveedores",jsonProv);
				resultados.put("Precio",suma);
				
				FileWriter file = new FileWriter("C:\\Users\\Camilo\\Desktop\\Eclipse\\JSON\\resultados\\resultado"+name+".json");
				file.write(resultados.toJSONString());
				file.flush();
				file.close();
				
				//System.out.println("Se guardó en archivo el requerimiento.");
				
			} catch (FileNotFoundException e) {
				//manejo de error
				e.printStackTrace();
			} catch (IOException e) {
				//manejo de error
				e.printStackTrace();
			} 
			
			
			confirm.setContent("resultado"+name);
			myAgent.send(confirm);
			//myAgent.doDelete();
		}
	
	}
}
