package agentes;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Hashtable;
import java.util.Iterator;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;


import funciones.FuncionUtilidad;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.OneShotBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.Property;
//import jade.domain.FIPAAgentManagement.Property;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
//import procesos.Proceso;

public class Proveedor extends Agent{
	
	// Parámetros de la función de utilidad
	// Recibe requerimientos
	// Evalúa función de utilidad
	// Envía respuesta al bróker

	/**
	 * 
	 */
	private static final long serialVersionUID = -2231756242227649792L;
	// Se usa un catálogo para ordenar el servicio, sus atributos
	private Hashtable<String, Double> catalogue;
	private FuncionUtilidad funcion;
	String[][] serv;
	int nServ; // n servicios en el requerimiento
	int nServProv; // n servicios proveidos por el proveedor
	String[] aName = new String[9];
	private AID broker;
	String[] nombreAgente;
	double[][] rLocales;
	double[][] atrib;
	double[] precio;
	Agent myAgent = this;
	
	
	protected void setup(){
		
		System.out.println("Hola! El proveedor "+getAID().getName()+" está listo.");
		nombreAgente = this.getName().split("@");
		
		
		
		//funcion = new FuncionUtilidad(1);
		double param[] = obtenerFuncionUtilidad(new FuncionUtilidad(1)); 
		
		aName[0]="latencia";
		aName[1]="documentacion";
		aName[2]="mejPracticas";
		aName[3]="tiempo";
		aName[4]="disponibilidad";
		aName[5]="rendimiento";
		aName[6]="tasaExito";
		aName[7]="confiabilidad";
		aName[8]="conformidad";
		
		// Leer archivo json del proveedor
		
		/*
		public void obtenerServicios(){
		
		}
		*/
		int cont = 0;
		JSONParser parser = new JSONParser();
		try {
			Object obj = parser.parse(new FileReader("C:\\Users\\Camilo\\Desktop\\Eclipse\\JSON\\Prov\\"+nombreAgente[0]+".json"));
			JSONObject jsonObject = (JSONObject) obj;
			long n = (long) jsonObject.get("nServ");
			nServProv = (int) n;
			serv = new String[nServProv][11];
			
			while(cont < nServProv-1){
				int cont2 = 0;
				JSONArray servs = (JSONArray) jsonObject.get("servi"+cont);
				@SuppressWarnings("unchecked")
				Iterator<String> iterator = servs.iterator();
				while (iterator.hasNext()) {
					serv[cont][cont2] = (iterator.next());
					cont2++;
				}
				cont++;				
			}
		} catch (FileNotFoundException e) {
			//manejo de error
		} catch (IOException e) {
			//manejo de error
		} catch (ParseException e) {
			//manejo de error
		}
		//Convierto atributos de serv a double
		atrib = new double[nServProv][9];
		for(int i = 0; i<nServProv-1 ; i++){
			for(int j=0;j<9;j++){
				atrib[i][j]= Double.parseDouble(serv[i][j+1]);
			}
		}
		
		// Evalúo cada servicio con los parámetros de la función de utilidad del proveedor
		precio = new double[nServProv];
		for(int i = 0; i<nServProv;i++){
			precio[i]+=param[0]*atrib[i][0]+param[1]*atrib[i][1]+param[2]*atrib[i][2]+param[3]*atrib[i][3]+param[4]*atrib[i][4]+param[5]*atrib[i][5]+param[6]*atrib[i][6]+param[7]*atrib[i][7]+param[8]*atrib[i][8];
		}
				
		// Registrar el agente en un Directory Facilitator
		DFAgentDescription dfd = new DFAgentDescription();
		dfd.setName(myAgent.getAID());
		ServiceDescription sd = new ServiceDescription();
		sd.setType("proveedor");
		
		dfd.addServices(sd);
		sd.setName( getLocalName() );
		dfd.addServices(sd);
		// Registro los servicios del agente FALTA PROBAR
		for(int i = 0;i<nServProv-1;i++){
			ServiceDescription sd1 = new ServiceDescription();
			sd1.setName(serv[i][10]);
			sd1.setType(serv[i][10]);
			for(int j = 0;j < 9;j++){
				sd1.addProperties(new Property(aName[j],atrib[i][j]));
			}
			dfd.addServices(sd1);
		}
		try {
			DFService.register(myAgent, dfd);
		}
		catch (FIPAException fe) {
			fe.printStackTrace();
		}
		
		/*for(int i=0; i<nServ; i++){
			updateCatalogue(serv[i][10], new Double(precio[i]));
		}*/
		addBehaviour(new RecibirRequerimiento());
		
		addBehaviour(new ConfirmaOrden());
	}// Cierra setup()
	
	protected void takeDown(){
		
		try {
			DFService.deregister(this);
		}
		catch (FIPAException fe) {
			fe.printStackTrace();
		}
		
		System.out.println("Proveedor "+getAID().getName()+" terminado.");
		
		
		
	}// Cierra el takeDown
	public double[] obtenerFuncionUtilidad(FuncionUtilidad f){
		double param[] = f.getParametros();
		while(param[8]<0){
			funcion = new FuncionUtilidad(1);
			double param2[] = funcion.getParametros();
			for(int j=0;j<param.length;j++){
				param[j]=param2[j];
			}
		}
		return param;
	}
	/*
	public void updateCatalogue(final String nombre, double precio) {
		addBehaviour(new OneShotBehaviour() {
			
			private static final long serialVersionUID = 4641510754095104521L;

			public void action() {
				catalogue.put(nombre, precio);
				System.out.println(nombre+" insertado en el catálogo. Precio= "+precio);
			}
		} );
	}
	*/
	private class RecibirRequerimiento extends CyclicBehaviour {
		
		/**
		 * 
		 */
		private static final long serialVersionUID = -1649975511321717018L;

		public void action() {
			
			MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.CFP);
			ACLMessage msg = myAgent.receive(mt);
			if (msg != null) {
				String requerimiento = msg.getContent();
				
				ACLMessage reply = msg.createReply();
				broker = msg.getSender();
				int cont = 0;
				JSONParser parser = new JSONParser();
				try {
					Object obj = parser.parse(new FileReader("C:\\Users\\Camilo\\Desktop\\Eclipse\\JSON\\requerimientos\\"+requerimiento+".json"));
					JSONObject jsonObject = (JSONObject) obj;
					long n = (long) jsonObject.get("NNodos");
					nServ = (int) n;
					
					String[] servs = new String[nServ];
					
					
					int cont2 = 0;
					JSONArray servs2 = (JSONArray) jsonObject.get("Actividad");
					Iterator<String> iterator = servs2.iterator();
					while (iterator.hasNext()) {
						servs[cont2] = iterator.next();
						cont2++;
					}				
					
					JSONArray rL = (JSONArray) jsonObject.get("Locales");
					rLocales = new double[nServ][9];
					Iterator iterator2 = rL.iterator();
					int cont3 = 0;
					int contR = 0;
					while(iterator2.hasNext()){
						double aux = (double) iterator2.next();
						rLocales[cont3][contR] = aux;
						contR++;
						
						if((contR)%9==0){
						cont3++;
						contR=0;}	
					}
					
				} catch (FileNotFoundException e) {
					//manejo de error
				} catch (IOException e) {
					//manejo de error
				} catch (ParseException e) {
					//manejo de error
				}
				/*Convierto atributos de serv a double
				double atrib2[][]=new double[nServ][9];
				for(int i = 0; i<nServ-1 ; i++){
					for(int j=0;j<9;j++){
						atrib2[i][j]= Double.parseDouble(serv[i][j+1]);
					}
				}
				*/
				int cumple[] = cumpleRestr(rLocales); // Arreglo que indica si cumple o no
				
				int mejorOferta[] = elegirMejorOferta(cumple); // Evalúo los servicios que valoro más (es más probable que el consumidor igual los valores)
				
				System.out.println(myAgent.getName()+" generó la oferta.");
				// Genero la oferta y la envío en JSON
				
				JSONObject obj = new JSONObject(); // se debe agregar name como nombre y act como número de nodos				
				int contServ = 0;
				for(int i = 0;i < nServProv;i++){
					JSONArray listAtrib = new JSONArray();
					if(mejorOferta[i]==1){
						for(int j = 0;j < 9;j++){
							listAtrib.add(atrib[i][j]);
							
						}
						listAtrib.add(serv[i][10]); // nombre servicio asginado al proveedor
						listAtrib.add(serv[i][0]); // agrego id del servicio
						listAtrib.add(precio[i]); // agrego precio del producto
						obj.put("serv", listAtrib);
					}
				}
				
				try {
					FileWriter file = new FileWriter("C:\\Users\\Camilo\\Desktop\\Eclipse\\JSON\\ofertas\\oferta_"+nombreAgente[0]+"_"+requerimiento+".json");
					file.write(obj.toJSONString());
					file.flush();
					file.close();
				} catch (IOException e) {
					//manejar error
				}
				
				reply = new ACLMessage(ACLMessage.PROPOSE);
				reply.addReceiver(broker);
				reply.setContent("oferta_"+nombreAgente[0]+"_"+requerimiento);
				reply.setConversationId("service-trade");
				reply.setReplyWith("cfp"+System.currentTimeMillis()); 
				myAgent.send(reply);
				System.out.println("El "+ myAgent.getName() +" envió su oferta.");
			}
		}// Cierra action
		
	}
	private class ConfirmaOrden extends CyclicBehaviour {

		/**
		 * 
		 */
		private static final long serialVersionUID = -1617198703783939574L;

		@Override
		public void action() {
			MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.ACCEPT_PROPOSAL);
			ACLMessage msg = myAgent.receive(mt);
			AID aux = broker;
			if (msg != null) {
				ACLMessage reply = msg.createReply();
				String confirm = msg.getContent();
				if(confirm.equals("Acepto")){ 
					reply.setPerformative(ACLMessage.INFORM);
					reply.setContent("ok");
					System.out.println("Orden aceptada recibida.");
				}else {
					reply.setPerformative(ACLMessage.FAILURE);
					reply.setContent("No quiero vender");
					System.out.println("Al cabo que ni quería.");
				}
				reply.addReceiver(aux);
				myAgent.send(reply);
			} else {
				block();
			}
		} // Cierra Action
	} // Cierra ConfirmaOrden 
	
	public int[] cumpleRestr(double[][] restricciones){
		int cumple[] = new int[nServProv];
		
		for(int i = 0;i < restricciones.length;i++){
			String nombre = "serv"+i;
			for(int j = 0;j < nServProv-1;j++){
				if(serv[j][10].equals(nombre)){
				if(atrib[j][0]<restricciones[i][0]){
				if(atrib[j][1]>restricciones[i][1]){
				if(atrib[j][2]>restricciones[i][2]){
				if(atrib[j][3]<restricciones[i][3]){
				if(atrib[j][4]>restricciones[i][4]){
				if(atrib[j][5]>restricciones[i][5]){
				if(atrib[j][6]>restricciones[i][6]){
				if(atrib[j][7]>restricciones[i][7]){
				if(atrib[j][8]>restricciones[i][8]){
					cumple[j]=1;
				}
				}
				}
				}
				}
				}
				}
				}
				}
				}
			}
		}
		
		return cumple;
	}
	
	public int[] elegirMejorOferta(int[] cumple){
		int[] mejores = new int[nServProv];
		double p = 0;
		int m = 0;
		for(int i = 0;i < nServProv;i++){
			if(cumple[i]==1){
				if(precio[i]>p){
					p = precio[i]; // Voy guardando el precio
					m = i; // Guardo el lugar que ocupa
				}
			}
		}
		
		mejores[m]=1;
		
		
		return mejores;
	}
	
	
}// Cierra clase


