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
		
		System.out.println("Hola! El bróker "+getAID().getName()+" está listo.");
		
		tiempo_i = System.currentTimeMillis();
		// Registro al broker en el DF
		DFAgentDescription dfd = new DFAgentDescription();
		dfd.setName( getAID() );
		ServiceDescription sd  = new ServiceDescription();
		sd.setType( "broker" );
		sd.setName( getLocalName() );
		dfd.addServices(sd);
		try {
			DFService.register(this, dfd );
			}
		catch (FIPAException fe) { fe.printStackTrace(); }
		
		addBehaviour(new TickerBehaviour(this, 300){
			
			public void onTick() {
				
				MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.INFORM);
				ACLMessage msg = myAgent.receive(mt);
				
				if (msg != null) {
					name = msg.getContent();
					consumidor = msg.getSender();
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
						Iterator<String> iterator2 = paramObject.iterator();
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
						int cont2 = 0;
						parametros = new double[9];
						while(iterator2.hasNext()){
							parametros[cont2]=Double.parseDouble(iterator2.next());
							cont2++;
						}*/
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
					
					
					// Busco proveedores
					DFAgentDescription template = new DFAgentDescription();
					ServiceDescription sd = new ServiceDescription();
					sd.setType("proveedor");
					template.addServices(sd);
					DFAgentDescription[] result;
					try {
						result = DFService.search(myAgent, template); 
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
					
					// Busco los servicios disponibles
					DFAgentDescription template2 = new DFAgentDescription();
					ArrayList prop = new ArrayList();
					ArrayList<ArrayList> propServ = new ArrayList<ArrayList>();
					for(int i = 1;i<=nServ;i++){
						ServiceDescription sd1 = new ServiceDescription();
						sd1.setType("serv"+i);
						template2.addServices(sd1);
						Iterator<ServiceDescription> iterServ = template2.getAllServices();
						while(iterServ.hasNext()){
							ServiceDescription aux = iterServ.next();
							Iterator<Property> iterProp =  aux.getAllProperties();
							prop.add(aux.getName());
							while(iterProp.hasNext()){
								prop.add(iterProp.next());
							}
							propServ.add(propServ);
						}
					}
					System.out.println(propServ.size());
					String[][] arregloServ = new String[propServ.size()][9];
					
					// Debe terminar construyendo un archivo con las restricciones locales y los nodos del proceso.
					// Numero de nodos, tipos de nodo, restricciones locales, actividades (servicios)
					
					rLocales = new double[nServ*9];
					for(int i = 0;i < nServ;i++){
						rLocales[i*9] = 10000.0;
						rLocales[(i+1)*9-6] = 10000.0;
					}
					/*RestrLocales r = new RestrLocales();
					try {
						IChromosome resultado = r.restrOptimas(arregloServ, niveles);
						Gene[] genes = new Gene[nServ*9];
						genes = resultado.getGenes();
						for(int i = 0;i < (nServ*9);i++){
							rLocales[i] = (double) genes[i].getAllele();
					    }
						
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					*/
					JSONArray rL = new JSONArray();
					for(int i = 0;i<rLocales.length;i++){
						rL.add(rLocales[i]);
					}
					System.out.println("Se configuraron las restricciones locales.");
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
					
					
					addBehaviour(new RealizaRequerimiento());
					
				}// Cierra if
				
			}// Cierra action
			
			
		});

	}// Cierra el setup
	
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
		
		/**
		 * 
		 */
		private static final long serialVersionUID = 4141739597869771816L;
		//private int contador = 0;
		private MessageTemplate mt; 
		private int paso = 0;
		
		ArrayList ofer = new ArrayList();
		AID[] bestProveedores = new AID[nServ];
		@Override
		public void action() {
			
			 // Arreglo de cada oferta
			//ArrayList<AID> ofertantes = new ArrayList(); // Arreglo para guardar proveedores que enviaron oferta
			ofertantes = new AID[proveedores.length];
			ArrayList mejProv1 = new ArrayList();
						
			int[] posBajos = new int[nServ];
			//ArrayList ofertas = new ArrayList();// Arreglo de ofertas
			ofertas = new String[proveedores.length][2];
			double[] preciosBajos = new double[nServ];
			for(int i = 0;i<nServ;i++){
				preciosBajos[i] = 10.0;
			}
			
			switch (paso) {
			case 0:
				enviarCFP();
				
				paso = 1;
			case 1:
				int cont =0; 
				
				while(cont<proveedores.length){
					ACLMessage reply = myAgent.receive(mt);
					recibirOferta(reply, cont);
					cont++;
				}
				
				
				
				if (cont >= proveedores.length) {
					for(int j = 0;j < ofertas.length;j++){
						//ArrayList k = (ArrayList) ofertas.get(j);
						String s = ofertas[j][0];
						String[] n = s.split("v");
						// n[1] es el número de servicio
						int p = Integer.parseInt(n[1]);
						double aux = Double.parseDouble(ofertas[j][1]);
						System.out.println("El precio de la oferta "+ j + " para la actividad "+ n[1] +" es "+ aux);
						if(preciosBajos[p] > aux){
							posBajos[p] =j;
							preciosBajos[p]= aux;
							continue;
						}
					}
					if(posBajos[nServ-1] != 0){
						for (int i = 0; i < nServ; ++i) {
							bestProveedores[i] = ofertantes[posBajos[i]];
							System.out.println("El mejor proveedor para la actividad "+i+" es "+posBajos[i]);
							System.out.println(bestProveedores[i].getName());
						}
						paso = 2;
					}
				}// Cierra if 
				
			case 2: // Acepto propuestas de mejores ofertas
				
				enviarAccept();
				
			case 3: // Recibo confirmación/respuesta
				ACLMessage reply2 = myAgent.receive(mt);
				if (reply2 != null) {
					int cont2 = 0;
					if (reply2.getPerformative() == ACLMessage.INFORM) {
						cont2++;
					}
					if (cont2 >= bestProveedores.length) {
						paso = 4; 
					}
				}
			case 4: // Informo al consumidor
				ACLMessage confirm = new ACLMessage(ACLMessage.INFORM);
				confirm.addReceiver(consumidor);
				System.out.println("Las ofertas aceptadas fueron:");
				for(int i = 0;i < bestProveedores.length;i++){
					System.out.println(bestProveedores[i]);
				}
				myAgent.send(confirm);
				myAgent.doDelete();
				
			}// Cierra switch
		}// Cierra behaviour
		

		@Override
		public boolean done() {
			
			return false;
		}
		
		public int enviarCFP(){
			ACLMessage cfp = new ACLMessage(ACLMessage.CFP);
			for (int i = 0; i < proveedores.length; ++i) {
				cfp.addReceiver(proveedores[i]);
			}
			cfp.setContent("requerimiento"+name);
			cfp.setConversationId("service-trade");
			cfp.setReplyWith("cfp"+System.currentTimeMillis()); 
			send(cfp);
			
			tiempo_f = System.currentTimeMillis();
			System.out.println("El requerimiento se ha enviado luego de "+ ( tiempo_f - tiempo_i ) +" milisegundos");
			
			return paso = 1;
		}
		public void recibirOferta(ACLMessage reply, int cont){
				if (reply != null) {
					String oferta = reply.getContent();
					//ofertantes.add(reply.getSender());
					ofertantes[cont] = reply.getSender();
					System.out.println("Se recibe la oferta de "+ofertantes[cont].getName());
				
					// Hay respuestas positivas
					if (reply.getPerformative() == ACLMessage.PROPOSE) {
						JSONParser parser = new JSONParser();
						try {
							Object objOferta = parser.parse(new FileReader("C:\\Users\\Camilo\\Desktop\\Eclipse\\JSON\\ofertas\\"+oferta+".json"));
							JSONObject jsonOferta = (JSONObject) objOferta;
							int cont2 = 0;
							while(cont2<1){ // Una oferta no puede contener más de nServ servicios
							JSONArray ofert = (JSONArray) jsonOferta.get("serv");
							// El orden es: atributos (0 a 8), nombre (9), id (10), precio (11)
							Iterator<String> iterator = ofert.iterator();
							while (iterator.hasNext()) {
								ofer.add(iterator.next()); // Arreglo de cada oferta
							}
							System.out.println("Por el servicio "+ofer.get(9)+" se ofrece "+ofer.get(11));
							//ofertas.add(contador, ofer); // Arreglo de ofertas
							ofertas[cont][0]=(String) ofer.get(9);
							System.out.println("Se guarda la oferta "+cont+ " por la act "+ofertas[cont][0]);
							ofertas[cont][1]= Double.toString( (double) ofer.get(11));
							System.out.println(cont);
							cont2++;
						}
						System.out.println("Hola4" +ofertas[cont][0]);
						
					} catch (FileNotFoundException e) {
					} catch (IOException e) {
						//manejo de error
					} catch (ParseException e) {
						//manejo de error
					}
				
					System.out.println("Oferta por "+ofertas[cont][0]+" tiene precio "+ofertas[cont][1]);
					cont++;
					} // Contenido mensaje
				}
			
		
		}
		public int enviarAccept(){
			ACLMessage order = new ACLMessage(ACLMessage.ACCEPT_PROPOSAL);
			order.setContent("Acepto");
			
			for (int i = 0; i < nServ; ++i) {
				//AID aux = (AID) mejProv1.get(i);
				order.addReceiver(bestProveedores[i]);
			}
			myAgent.send(order);
			System.out.println("Broker acepta ofertas.");
			return paso = 3;
		}
	
	}
}
