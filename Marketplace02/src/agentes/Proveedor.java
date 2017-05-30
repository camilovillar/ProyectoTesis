package agentes;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
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
	private long tiempo_i;
	private long tiempo_f;
	private double probBundling;
	private int nroBundling;
	
	
	protected void setup(){
		tiempo_i = System.currentTimeMillis();
		System.out.println("Hola! El proveedor "+getAID().getName()+" está listo.");
		nombreAgente = this.getName().split("@");
		
		
		
		funcion = new FuncionUtilidad(2);
		double param[] = funcion.getParametros();
		//System.out.println("El primer param para el agente "+nombreAgente[0]+" es: "+param[8]);
		
		nroBundling = funcion.getNroBundling();
		//System.out.println(nombreAgente[0]+" "+nroBundling);
		probBundling = funcion.getProbBundling();
		//System.out.println(probBundling);
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
		nServProv=obtenerNumeroServicios(nombreAgente[0]);
		//System.out.println("El número de servicios asignados al proveedor es de: "+nServProv);
		serv = obtenerServicios(nombreAgente[0]);
		//System.out.println("El número de servicios guardados del proveedor es de: "+serv.length);
		/*
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
					System.out.println("Se rescató el atributo "+serv[cont][cont2]);
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
		*/
		//Convierto atributos de serv a double
		/*for(int i = 0;i < serv[0].length;i++){
			System.out.println("Serv "+i+" tiene el valores "+serv[0][i]);
		}*/
		
		atrib = new double[nServProv][9];
		for(int i = 0; i<serv.length ; i++){
			for(int j=1;j<(serv[0].length-1);j++){
				//System.out.println("Se va a convertir del serv(id) "+serv[i][0]+" el atributo "+j+" con valor "+serv[i][j]);
				atrib[i][j-1]= Double.parseDouble(serv[i][j]);
			}
		}
		tiempo_f = System.currentTimeMillis();
		System.out.println("El "+myAgent.getName()+" leyó sus servicios a los "+ ( tiempo_f - tiempo_i ) +" milisegundos.");
		
		// Evalúo cada servicio con los parámetros de la función de utilidad del proveedor
		precio = new double[nServProv];
		for(int i = 0; i<nServProv;i++){
			precio[i]=(1-param[0])*atrib[i][0]+param[1]*atrib[i][1]+param[2]*atrib[i][2]+(1-param[3])*atrib[i][3]+param[4]*atrib[i][4]+param[5]*atrib[i][5]+param[6]*atrib[i][6]+param[7]*atrib[i][7]+param[8]*atrib[i][8];
		}
		if(nServProv == 0){
			myAgent.doDelete();
		}
				
		tiempo_f = System.currentTimeMillis();
		System.out.println("El "+myAgent.getName()+" valoró sus servicios a los "+ ( tiempo_f - tiempo_i ) +" milisegundos.");
		// Registrar el agente en un Directory Facilitator
		
		registrarEnDF();
		
		tiempo_f = System.currentTimeMillis();
		System.out.println("El "+myAgent.getName()+" registró sus servicios a los "+ ( tiempo_f - tiempo_i ) +" milisegundos.");
		
		addBehaviour(new RecibirRequerimiento());
		
		addBehaviour(new ConfirmaOrden());
	}// Cierra setup()
	
	public int obtenerNumeroServicios(String nombreAgente){
		int nServProv = 0;
		JSONParser parser = new JSONParser();
		try {
			Object obj = parser.parse(new FileReader("C:\\Users\\Camilo\\Desktop\\Eclipse\\JSON\\Prov\\"+nombreAgente+".json"));
			JSONObject jsonObject = (JSONObject) obj;
			long n = (long) jsonObject.get("nServ");
			nServProv = (int) n;
			
		} catch (FileNotFoundException e) {
			//manejo de error
		} catch (IOException e) {
			//manejo de error
		} catch (ParseException e) {
			//manejo de error
		}
		return nServProv;
	}
	
	public String[][] obtenerServicios(String nombreAgente){
		
		int cont = 0;
		
		String[][] servc = new String[nServProv][11];
		JSONParser parser = new JSONParser();
		try {
			Object obj = parser.parse(new FileReader("C:\\Users\\Camilo\\Desktop\\Eclipse\\JSON\\Prov\\"+nombreAgente+".json"));
			JSONObject jsonObject = (JSONObject) obj;
			while(cont < nServProv){
				int cont2 = 0;
				JSONArray servs = (JSONArray) jsonObject.get("servi"+cont);
				@SuppressWarnings("unchecked")
				Iterator<String> iterator = servs.iterator();
				while (iterator.hasNext()) {
					servc[cont][cont2] = (iterator.next());
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
		return servc;
	}	
	
	public void registrarEnDF(){
		DFAgentDescription dfd = new DFAgentDescription();
		dfd.setName(myAgent.getAID());
		ServiceDescription sd = new ServiceDescription();
		sd.setType("proveedor");
		
		dfd.addServices(sd);
		sd.setName( getLocalName() );
		dfd.addServices(sd);
		
		
		// Registro los servicios del agente FALTA PROBAR
		int cont = 0;
		for(int i = 0;i<nServProv;i++){
			ServiceDescription sd1 = new ServiceDescription();
			sd1.setName(serv[i][10]);
			sd1.setType(serv[i][10]);
			for(int j = 0;j < 9;j++){
				sd1.addProperties(new Property(aName[j],atrib[i][j]));
			}
			dfd.addServices(sd1);
			cont++;
			//System.out.println("Se registró el servicio "+serv[i][10]+" del prov "+ nombreAgente[0] );
		}
		//System.out.println("Se registraron "+cont+" servicios del agente "+nombreAgente[0]);
		try {
			DFService.register(myAgent, dfd);
		}
		catch (FIPAException fe) {
			fe.printStackTrace();
		}
	}
	
	protected void takeDown(){
		
		try {
			DFService.deregister(this);
		}
		catch (FIPAException fe) {
			fe.printStackTrace();
		}
		tiempo_f = System.currentTimeMillis();
		System.out.println("El "+myAgent.getName()+" trabajó "+ ( tiempo_f - tiempo_i ) +" milisegundos.");
		
	}// Cierra el takeDown
	
	/*public double[] obtenerFuncionUtilidad(FuncionUtilidad f){
		double param[] = f.getParametros();
		return param;
	}*/

	private class RecibirRequerimiento extends CyclicBehaviour {
		
		private static final long serialVersionUID = -1649975511321717018L;

		public void action() {
			
			MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.CFP);
			ACLMessage msg = myAgent.receive(mt);
			if (msg != null) {
				String requerimiento = msg.getContent();
				tiempo_f = System.currentTimeMillis();
				System.out.println("El "+myAgent.getName()+" recibe el req a los "+ ( tiempo_f - tiempo_i ) +" milisegundos.");
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
				tiempo_f = System.currentTimeMillis();
				//System.out.println("El "+myAgent.getName()+" parsea el req a los "+ ( tiempo_f - tiempo_i ) +" milisegundos.");
				int cumple[] = cumpleRestr(rLocales); // Arreglo que indica si cumple o no
				
				int suma0 = 0;
				for(int i = 0;i < cumple.length;i++){
					suma0+=cumple[i];
				}
				System.out.println("Los servicios del "+nombreAgente[0]+" que cumplen con las restricciones son "+suma0);
				/*if (cumple == null){
					ACLMessage notify = new ACLMessage(ACLMessage.REFUSE); 
					notify.addReceiver(broker);
					send(notify);
				}*/
				int mejorOferta[] = elegirMejorOferta(cumple); // Evalúo los servicios que valoro más (es más probable que el consumidor igual los valores)
				
				int suma = 0;
				for(int i = 0;i < mejorOferta.length;i++){
					suma+=mejorOferta[i];
				}
				System.out.println("La oferta del "+nombreAgente[0]+" debe tener "+suma+ " elementos.");
				// Genero la oferta y la envío en JSON
				tiempo_f = System.currentTimeMillis();
				
				crearOferta(mejorOferta, requerimiento);
				System.out.println("El "+myAgent.getName()+" generó la oferta a los "+ ( tiempo_f - tiempo_i ) +" milisegundos.");
				enviarOferta(broker, reply, requerimiento);
				
			}
		}// Cierra action
		
	}
	private class ConfirmaOrden extends CyclicBehaviour {
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
					//System.out.println("Orden aceptada recibida.");
					doDelete();
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
			for(int j = 0;j < nServProv;j++){
				if(serv[j][10].equals(nombre)){ // comparo el nombre del servicio
					System.out.println(nombreAgente[0]+" compara el atributo "+atrib[j][0]+" con la restricción "+restricciones[i][0]);
				if(atrib[j][0]<restricciones[i][0]){
					System.out.println(nombreAgente[0]+" compara el atributo "+atrib[j][1]+" con la restricción "+restricciones[i][1]);
				if(atrib[j][1]>restricciones[i][1]){
					System.out.println(nombreAgente[0]+" compara el atributo "+atrib[j][2]+" con la restricción "+restricciones[i][2]);
				if(atrib[j][2]>restricciones[i][2]){
					System.out.println(nombreAgente[0]+" compara el atributo "+atrib[j][3]+" con la restricción "+restricciones[i][3]);
				if(atrib[j][3]<restricciones[i][3]){
					System.out.println(nombreAgente[0]+" compara el atributo "+atrib[j][4]+" con la restricción "+restricciones[i][4]);
				if(atrib[j][4]>restricciones[i][4]){
					System.out.println(nombreAgente[0]+" compara el atributo "+atrib[j][5]+" con la restricción "+restricciones[i][5]);
				if(atrib[j][5]>restricciones[i][5]){
					System.out.println(nombreAgente[0]+" compara el atributo "+atrib[j][6]+" con la restricción "+restricciones[i][6]);
				if(atrib[j][6]>restricciones[i][6]){
					System.out.println(nombreAgente[0]+" compara el atributo "+atrib[j][7]+" con la restricción "+restricciones[i][7]);
				if(atrib[j][7]>restricciones[i][7]){
					System.out.println(nombreAgente[0]+" compara el atributo "+atrib[j][8]+" con la restricción "+restricciones[i][8]);
				if(atrib[j][8]>restricciones[i][8]){
					System.out.println(nombreAgente[0]+" compara el atributo "+atrib[j][0]+" con la restricción "+restricciones[i][0]);
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
	
	public int[] elegirMejorOferta(int[] cumple){ // entrega la posición de los mejores servicios
		int[] mejores = new int[nServProv]; // guardo al final la posición de los mejores servicios
		String[] servDisp = getServDisp();
		System.out.println("El "+nombreAgente[0]+" tiene "+servDisp.length+" servicios disponibles" );
		ArrayList p = new ArrayList(); // arreglo para guardar precio de bundling
		ArrayList m = new ArrayList(); // arreglo para guardar posición del serv a ofertar
		ArrayList n = new ArrayList(); // arreglo para guardar los nombres de los servicios
		System.out.println("La prob de bund es "+ probBundling+ " para el "+nombreAgente[0]+" entonces");
		if(probBundling>0.4){
			System.out.println("Entra a hacer bundling el "+nombreAgente[0]);
			int[] m1 = new int[servDisp.length];
			double[] p1 = new double[servDisp.length];
			for(int i = 0;i < serv.length;i++){
				for(int j = 0;j < servDisp.length;j++){
					if(servDisp[j].equals(serv[i][10])){
						if(precio[i]>p1[j]){
							p1[j]=precio[i];
							m1[j]=i;
							
						}
					}
				}
			}
			System.out.println("El "+nombreAgente[0]+" tiene "+servDisp.length+" servicios disponibles y "+nroBundling+" nro de bund");
			if(servDisp.length > nroBundling){// Si los servicios disponibles son más que los que asigno a bundling 
				System.out.println("El "+nombreAgente[0]+" tiene que entrar a escoger lo mejor entre lo disponible" );
				for(int i=0;i<(p1.length-1);i++){
		            for(int j=i+1;j<p1.length;j++){
		                if(p1[i]>p1[j]){
		                    double aux1=p1[i];
		                    p1[i]=p1[j];
		                    p1[j]=aux1;
		                    int aux2 = m1[i];
		                    m1[i]=m1[j];
		                    m1[i]=aux2;
		                }
		            }
		        }
				for(int i = 0;i < nroBundling;i++){
					p.add(p1[i]);
					m.add(m1[i]);
				}
				System.out.println("El"+nombreAgente[0]+" agregó "+p.size());
			}else
			if(servDisp.length == nroBundling){ // Si es el mismo número
				System.out.println("El "+nombreAgente[0]+" tiene que enviar el mejor de cada disponible" );
				for(int i = 0;i < servDisp.length;i++){
					p.add(p1[i]);
					m.add(m1[i]);
				}
				System.out.println("El"+nombreAgente[0]+" agregó "+p.size());
			}else
			if(servDisp.length < nroBundling){// Si el número de bundling es mayor que el número de servicios disponibles
				System.out.println("El "+nombreAgente[0]+" tiene que enviar el mejor de cada disponible sin completar el n de bund" );
				for(int i = 0;i < servDisp.length;i++){
					p.add(p1[i]);
					m.add(m1[i]);
				}
				System.out.println("El"+nombreAgente[0]+" agregó "+p.size());
				nroBundling = servDisp.length; // actualizo el número de bundling
			}
			
			for(int i = 0;i < m.size();i++){
				mejores[(int) m.get(i)] = 1;
			}
		}else{ // si la probabilidad de bundling es menor, entonces se elige el servicio con el mayor precio para la oferta
			System.out.println("No entra a hacer bundling el "+nombreAgente[0]);
			double p0 = 0.0;
			int m0 = 0;
			for(int i = 0;i < mejores.length;i++){
				if(cumple[i]==1){
					if(precio[i]>p0){
						p0 = precio[i]; // Voy guardando el precio
						m0 = i; // Guardo el lugar que ocupa
					}
				}
			}
			mejores[m0]=1;
		}//Cierra if else
		
		return mejores;
	}
	
	public String[] getServDisp(){
		String[] servDisp;
		ArrayList<String> list = new ArrayList<String>();
		// REVISAR ESTO!!!!!
		list.add(serv[0][10]);// agrego el primer serv a la lista
		System.out.println("Agrego el "+serv[0][10]);
		for(int i = 0;i < nServProv;i++){
			int k = 0;
			for(int j = 0;j < list.size();j++){
				if((list.get(j).equals(serv[i][10]))){
					System.out.println(nombreAgente[0]+" No agrego el "+serv[i][10]);
				}else{
					list.add(serv[i][10]);
					System.out.println(nombreAgente[0]+" Agrego el "+serv[i][10]);
					break;
				}
			}
		}
		servDisp = new String[list.size()];
		for(int i = 0;i < servDisp.length;i++){
			servDisp[i] = (String) list.get(i);
			System.out.println("El servicio "+servDisp[i]+ " fue asignado al "+ nombreAgente[0]);
		}
		return servDisp;
	}
	
	public void crearOferta(int[] mejorOferta, String requerimiento){
		JSONObject obj = new JSONObject(); // se debe agregar name como nombre y act como número de nodos				
		int contServ = 0;
		JSONArray listServ = new JSONArray();
		for(int i = 0;i < mejorOferta.length;i++){
			JSONArray listAtrib = new JSONArray();
			if(mejorOferta[i]==1){
				for(int j = 0;j < 9;j++){
					listAtrib.add(atrib[i][j]);
				}
				listAtrib.add(nombreAgente[0]); //nombre agente proveedor
				listAtrib.add(serv[i][10]); // nombre servicio asginado al proveedor
				listAtrib.add(serv[i][0]); // agrego id del servicio
				listAtrib.add(precio[i]); // agrego precio del producto
				obj.put(serv[i][10], listAtrib);
				listServ.add(serv[i][10]);
				contServ++;
			}
		}
		obj.put("serv", listServ);
		obj.put("n", contServ);
		System.out.println("La oferta del "+nombreAgente[0]+" tiene "+contServ+ " elementos.");
		try {
			FileWriter file = new FileWriter("C:\\Users\\Camilo\\Desktop\\Eclipse\\JSON\\ofertas\\oferta_"+nombreAgente[0]+"_"+requerimiento+".json");
			file.write(obj.toJSONString());
			file.flush();
			file.close();
		} catch (IOException e) {
			//manejar error
		}
	}
	
	public void enviarOferta(AID boker, ACLMessage reply, String requerimiento){
		reply = new ACLMessage(ACLMessage.PROPOSE);
		reply.addReceiver(broker);
		reply.setContent("oferta_"+nombreAgente[0]+"_"+requerimiento);
		reply.setConversationId("service-trade");
		reply.setReplyWith("cfp"+System.currentTimeMillis()); 
		myAgent.send(reply);
		tiempo_f = System.currentTimeMillis();
		System.out.println("El "+myAgent.getName()+" envió su oferta a los "+ ( tiempo_f - tiempo_i ) +" milisegundos.");
	}
	
	
}// Cierra clase


