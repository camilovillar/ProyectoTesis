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

import org.jgap.Chromosome;
import org.jgap.IChromosome;
import org.jgap.Gene;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import geneticos.RestrLocales;
import jade.core.AID;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.Property;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import test.Test;

public class Broker extends Agent{
	
	// Recibe proceso del consumidor y función de utilidad
	// Parsea y establece criterios locales de optimización
	// Envía información a los proveedores
	// Recibe respuestas de los proveedores
	// Encuentra los mejores servicios
	// Cierra acuerdos con los proveedores
	// Envia respuestas a consumidor
	
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -7419979154495603904L;
	/**
	 * 
	 */
	
	private AID[] proveedores;
	private AID consumidor;
	private long tiempo_i;
	private long tiempo_f;
	private String[] servicios; // Listo
	private String name = "";
	private double[] rLocales;
	private double[] parametros;
	private double[] rGlobales;
	private int[] tipos;
	private int[] iter;
	private double[] probab;
	private int nServ;
	private int niveles = 50;
	private String[][] ofertas;
	private AID[] ofertantes;
	
	
	protected void setup(){
		tiempo_i = System.currentTimeMillis();
		System.out.println("Hola! El bróker "+getAID().getName()+" está listo.");
		
		
		// Registro al broker en el DF
		this.registrarEnDF();
		
		addBehaviour(new TickerBehaviour(this, 300){
			
			public void onTick() {
				
				MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.INFORM);
				ACLMessage msg = myAgent.receive(mt);
				
				if (msg != null) {
					name = msg.getContent();
					consumidor = msg.getSender();
					
					// Aquí obtengo proceso
					
					obtenerProceso();
					tiempo_f = System.currentTimeMillis();
					System.out.println("El broker obtuvo el proceso a los "+ ( tiempo_f - tiempo_i ) +" milisegundos.");
					// Busco proveedores
					buscarProveedores();		
					tiempo_f = System.currentTimeMillis();
					System.out.println("El broker encontró "+proveedores.length+" proveedores a los "+ ( tiempo_f - tiempo_i ) +" milisegundos.");
					// Busco los servicios disponibles
					
					
					String[][] arregloServ = buscarServiciosDisponibles();
					
					System.out.println("El broker buscó servicios disponibles a los "+ ( tiempo_f - tiempo_i ) +" milisegundos.");
					// Debe terminar construyendo un archivo con las restricciones locales y los nodos del proceso.
					// Numero de nodos, tipos de nodo, restricciones locales, actividades (servicios)
					
					
					//Determino restricciones locales
					rLocales = generarRestricciones(arregloServ);
					for(int i =0;i<rLocales.length;i++){
						System.out.println("La restricción "+i +" quedó con el valor "+ rLocales[i]);
					}
					tiempo_f = System.currentTimeMillis();
					System.out.println("El broker generó las restricciones a los "+ ( tiempo_f - tiempo_i ) +" milisegundos.");
					
					agregarRestrRequerimiento(rLocales);
					tiempo_f = System.currentTimeMillis();
					System.out.println("El broker agregó las restricciones al requerimiento a los "+ ( tiempo_f - tiempo_i ) +" milisegundos.");
					
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
	public void obtenerProceso(){
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
			
			//Iterator<String> iterator1 = restrObject.iterator();
			Iterator iterator2 = paramObject.iterator();
			Iterator iterator3 = probabObject.iterator();
			Iterator iterator4 = iterObject.iterator();
			Iterator iterator5 = tipoObject.iterator();
			Iterator<String> iterator = actividad.iterator();
			
			/*
			int cont1 = 0;
			rGlobales = new double[9];
			while(iterator1.hasNext()){
				rGlobales[cont1]=Double.parseDouble(iterator1.next());
				cont1++;
			}
			*/int cont2 = 0;
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
		sd.setType("proveedor");
		template.addServices(sd);
		DFAgentDescription[] result;
		try {
			result = DFService.search(this, template); 
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
	public String[][] buscarServiciosDisponibles(){
		//System.out.println("Entro a buscar los servicios");
		DFAgentDescription template2 = new DFAgentDescription();
		//System.out.println("Creo un AgentDescription");
		ArrayList<ArrayList> prop = new ArrayList<ArrayList>();
		//System.out.println("Creo un ArrayList");
		//System.out.println("Creo un ArrayList de ArrayLists");
		//for(int i = 1;i<=nServ;i++){
			ServiceDescription sd1 = new ServiceDescription();
			System.out.println("Creo un ServiceDescription");
			sd1.setType("proveedor");
			System.out.println("Seteo el nombre del SD a " + sd1.getName());
			template2.addServices(sd1);
			System.out.println("Agrego SD a AD");
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
	public double[] generarRestricciones(String[][] arregloServ){
		double[] r = new double[nServ*9];
		/*for(int i = 0;i < nServ;i++){
			r[i*9] = 10000.0;
			r[(i+1)*9-6] = 10000.0;
		}*/
		
		RestrLocales re = new RestrLocales(nServ, arregloServ, parametros);
		//re.setNivelesServicio();
		try {
			IChromosome resultado = re.restrOptimas(niveles);
			Gene[] genes = new Gene[nServ*9];
			genes = resultado.getGenes();
			//System.out.println("Se recibe el resultado, el primer gen es "+genes[0].getAllele());
			for(int i = 0;i < genes.length;i++){
				//System.out.println("Se recibe el resultado, el primer gen es "+genes[0].getAllele());
				r[i] = (double) genes[i].getAllele();
				
		    }
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return r;
	}
	public void agregarRestrRequerimiento(double [] rLocales){
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
			
			System.out.println("Se guardó en archivo el requerimiento.");
			
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
		double[] preciosBajos = new double[nServ];
		int[] posBajos = new int[nServ];
		
		AID[] bestProveedores = new AID[nServ];
		@Override
		public void action() {
			ofertantes = new AID[proveedores.length]; // Arreglo para guardar proveedores que enviaron oferta
						
			
			// Arreglo de ofertas
			ofertas = new String[proveedores.length][2];
			
			for(int i = 0;i<nServ;i++){
				preciosBajos[i] = 10.0;
			}
			
				boolean enviado = false;
				
				enviado = enviarCFP();
				tiempo_f = System.currentTimeMillis();
				System.out.println("El broker envió el requerimiento a los "+ ( tiempo_f - tiempo_i ) +" milisegundos.");
				
				//paso = 1;


				int cont = recibirOferta();
				tiempo_f = System.currentTimeMillis();
				System.out.println("El broker recibió y parseó la oferta a los "+ ( tiempo_f - tiempo_i ) +" milisegundos.");
				
				/*for(int i = 0;i < ofertas.length;i++){
					System.out.println("Oferta por "+ofertas[i][0]+" tiene precio "+ofertas[i][1]);	
				}
				*/
				if (cont >= proveedores.length) {
					elegirMejoresOfertas(ofertas);
				}// Cierra if 
				tiempo_f = System.currentTimeMillis();
				System.out.println("El broker eligió las mejores ofertas a los "+ ( tiempo_f - tiempo_i ) +" milisegundos.");
				// Acepto propuestas de mejores ofertas
				enviarAccept(bestProveedores);
				tiempo_f = System.currentTimeMillis();
				System.out.println("El broker notificó a los elegidos a los "+ ( tiempo_f - tiempo_i ) +" milisegundos.");
 
				// Recibo confirmación/respuesta
				reciboAprob();
				tiempo_f = System.currentTimeMillis();
				System.out.println("El broker recibió aprobación de los proveedores a los "+ ( tiempo_f - tiempo_i ) +" milisegundos.");

				// Informo al consumidor
				informoResultados(consumidor);
				tiempo_f = System.currentTimeMillis();
				System.out.println("El broker informó resultados a los "+ ( tiempo_f - tiempo_i ) +" milisegundos.");
				
		}// Cierra behaviour
		

		@Override
		public boolean done() {
			
			return false;
		}
		
		public boolean enviarCFP(){
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
		public int recibirOferta(){
			int cont =0; 
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
						
						parsearOferta(oferta, cont);
				
						//System.out.println("Oferta por "+ofertas[cont][0]+" tiene precio "+ofertas[cont][1]);
						cont++;
					} // Contenido mensaje
				}
				}
			}
			return cont;
		}
		public void parsearOferta(String oferta, int cont){
			JSONParser parser = new JSONParser();
			try {
				//System.out.println("Entra a leer el archivo "+ oferta+".");
				Object objOferta = parser.parse(new FileReader("C:\\Users\\Camilo\\Desktop\\Eclipse\\JSON\\ofertas\\"+oferta+".json"));
				JSONObject jsonOferta = (JSONObject) objOferta;
				int cont2 = 0;
				ArrayList ofer = new ArrayList();
				while(cont2<1){ // Una oferta no puede contener más de nServ servicios
					//Después será necesario incluir un int en la propuesta que indique el número de items ofertados para cortar el while
					JSONArray ofert = (JSONArray) jsonOferta.get("serv");
					// El orden es: atributos (0 a 8), nombre (9), id (10), precio (11)
					Iterator<String> iterator = ofert.iterator();
					while(iterator.hasNext()) {
						ofer.add(iterator.next()); // Arreglo de cada oferta
					}
					//System.out.println("Por el servicio "+ofer.get(9)+" se ofrece "+ofer.get(11));
					//ofertas.add(contador, ofer); // Arreglo de ofertas
					ofertas[cont][0]=(String) ofer.get(9);
					ofertas[cont][1]= Double.toString( (double) ofer.get(11));
					cont2++;
				}
			
			} catch (FileNotFoundException e) {
			} catch (IOException e) {
				//manejo de error
			} catch (ParseException e) {
				//manejo de error
			}
		}
		public void elegirMejoresOfertas(String[][] ofertas){
			for(int j = 0;j < ofertas.length;j++){
				//ArrayList k = (ArrayList) ofertas.get(j);
				String s = ofertas[j][0];
				String[] n = s.split("v");
				// n[1] es el número de servicio
				int p = Integer.parseInt(n[1]);
				double aux = Double.parseDouble(ofertas[j][1]);
				//System.out.println("El precio de la oferta "+ j + " para la actividad "+ n[1] +" es "+ aux);
				if(preciosBajos[p] > aux){
					posBajos[p] =j;
					preciosBajos[p]= aux;
					continue;
				}
			}
			if(posBajos[nServ-1] >= 0){
				for (int i = 0; i < nServ; ++i) {
					bestProveedores[i] = ofertantes[posBajos[i]];
					//System.out.println("El mejor proveedor para la actividad "+i+" es :");
					//System.out.println(bestProveedores[i].getName());
				}
				//paso = 2;
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
			//return paso = 3;
			return true;
		}
		public void reciboAprob(){
			ACLMessage reply2 = myAgent.receive(mt);
			//AID aux = reply2.getSender();
			if (reply2 != null) {
				
				int cont2 = 0;
				if (reply2.getPerformative() == ACLMessage.INFORM) {
					cont2++;
				}
				if (cont2 >= bestProveedores.length) {
					tiempo_f=System.currentTimeMillis();
					System.out.println("Recibo aprobación de todos a los "+(tiempo_f-tiempo_i)+" milisegundos.");//paso = 4; 
				}
			}
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
			myAgent.send(confirm);
			myAgent.doDelete();
		}
	
	}
}
