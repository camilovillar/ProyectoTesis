package agentes;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import java.util.ArrayList;
import java.util.Iterator;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;


import funciones.FuncionUtilidad;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.Property;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

public class Proveedor extends Agent{
	
	private static final long serialVersionUID = -2231756242227649792L;
	// Se usa un catálogo para ordenar el servicio, sus atributos
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
	private double descuento;
	private double margen;
	
	protected void setup(){
		tiempo_i = System.currentTimeMillis();
		//System.out.println("Hola! El proveedor "+getAID().getName()+" está listo.");
		nombreAgente = this.getName().split("@");
		
		
		
		funcion = new FuncionUtilidad(2);
		double param[] = funcion.getParametros();
		//System.out.println("El primer param para el agente "+nombreAgente[0]+" es: "+param[8]);
		
		nroBundling = funcion.getNroBundling();
		//System.out.println(nombreAgente[0]+" "+nroBundling);
		probBundling = funcion.getProbBundling();
		//System.out.println(probBundling);
		
		descuento = funcion.getDescuento();
		
		margen = funcion.getMargen();
		
		aName[0]="tiempo";
		aName[1]="disponibilidad";
		aName[2]="rendimiento";
		aName[3]="tasaExito";
		aName[4]="confiabilidad";
		aName[5]="conformidad";
		aName[6]="mejPracticas";
		aName[7]="latencia";
		aName[8]="documentacion";
		
		// Leer archivo json del proveedor
		nServProv=obtenerNumeroServicios(nombreAgente[0]);
		
		if(nServProv == 0){
			myAgent.doDelete();
		}
		//System.out.println("El número de servicios asignados al proveedor es de: "+nServProv);
		serv = obtenerServicios(nombreAgente[0]);
		
		atrib = new double[nServProv][9];
		for(int i = 0; i<serv.length ; i++){
			for(int j=1;j<(serv[0].length-1);j++){
				//System.out.println("Se va a convertir del serv(id) "+serv[i][0]+" el atributo "+j+" con valor "+serv[i][j]);
				atrib[i][j-1]= Double.parseDouble(serv[i][j]);
			}
		}
		tiempo_f = System.currentTimeMillis();
		//System.out.println("El "+myAgent.getName()+" leyó sus servicios a los "+ ( tiempo_f - tiempo_i ) +" milisegundos.");
		
		// Evalúo cada servicio con los parámetros de la función de utilidad del proveedor
		precio = new double[nServProv];
		for(int i = 0; i<nServProv;i++){
			precio[i]=param[0]*(atrib[i][0])+param[1]*atrib[i][1]+param[2]*atrib[i][2]+param[3]*(atrib[i][3])+param[4]*atrib[i][4]+param[5]*atrib[i][5]+param[6]*atrib[i][6]+param[7]*atrib[i][7]+param[8]*atrib[i][8];
		}
		
				
		tiempo_f = System.currentTimeMillis();
		//System.out.println("El "+myAgent.getName()+" valoró sus servicios a los "+ ( tiempo_f - tiempo_i ) +" milisegundos.");
		// Registrar el agente en un Directory Facilitator
		
		registrarEnDF();
		
		tiempo_f = System.currentTimeMillis();
		//System.out.println("El "+myAgent.getName()+" registró sus servicios a los "+ ( tiempo_f - tiempo_i ) +" milisegundos.");
		
		addBehaviour(new RecibirRequerimiento());
		
		addBehaviour(new ConfirmaOrden());
		
		addBehaviour(new OrdenRechazada());
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
		
		
		// Registro los servicios del agente
		for(int i = 0;i<nServProv;i++){
			ServiceDescription sd1 = new ServiceDescription();
			sd1.setName(serv[i][10]);
			sd1.setType(serv[i][10]);
			for(int j = 0;j < 9;j++){
				sd1.addProperties(new Property(aName[j],atrib[i][j]));
			}
			dfd.addServices(sd1);
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
		
		/*try {
			DFService.deregister(this);
		}
		catch (FIPAException fe) {
			fe.printStackTrace();
		}*/
		tiempo_f = System.currentTimeMillis();
		System.out.println("El "+myAgent.getName()+" trabajó "+ ( tiempo_f - tiempo_i ) +" milisegundos.");
		myAgent.doDelete();
	}// Cierra el takeDown

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
						
						if(contR%9==0){
							cont3++;
							contR=0;
						}	
					}
					
				} catch (FileNotFoundException e) {
					//manejo de error
				} catch (IOException e) {
					//manejo de error
				} catch (ParseException e) {
					//manejo de error
				}
				
				tiempo_f = System.currentTimeMillis();
				//System.out.println("El "+myAgent.getName()+" parsea el req a los "+ ( tiempo_f - tiempo_i ) +" milisegundos.");
				
				
				int cumple[] = cumpleRestr(rLocales); // Arreglo que indica si cumple o no
				
				int suma0 = 0;
				for(int i = 0;i < cumple.length;i++){
					suma0+=cumple[i];
				}
				
				System.out.println("Los servicios del "+nombreAgente[0]+" que cumplen con las restricciones son "+suma0);
				
				if(suma0 == 0){
					enviarRefuse(broker,reply, requerimiento);
					
					takeDown();
					
				}else{
				
				int mejorOferta[] = elegirMejorOferta(cumple); // Evalúo los servicios que valoro más (es más probable que el consumidor igual los valores)
				
				/*int suma = 0;
				for(int i = 0;i < mejorOferta.length;i++){
					suma+=mejorOferta[i];
				}*/
				//System.out.println("La oferta del "+nombreAgente[0]+" debe tener "+suma+ " elementos.");
				// Genero la oferta y la envío en JSON
				tiempo_f = System.currentTimeMillis();
				
				crearOferta(mejorOferta, requerimiento);
				System.out.println("El "+myAgent.getName()+" generó la oferta a los "+ ( tiempo_f - tiempo_i ) +" milisegundos.");
				enviarOferta(broker, reply, requerimiento);
				}
			}
		}// Cierra action
		
	}
	private class OrdenRechazada extends CyclicBehaviour {
		private static final long serialVersionUID = 1L;

		public void action(){
			MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.REFUSE);
			ACLMessage msg = myAgent.receive(mt);
			if(msg != null){
				String cont = msg.getContent();
				if(cont.equals("refuse")){
					System.out.println("Al cabo que ni quería");
					doDelete();
				}
			}
		}
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
		//System.out.println("El "+nombreAgente[0]+" tiene "+ nServProv +" servicios.");
		for(int i = 0;i < cumple.length;i++){
			for(int j = 0;j < nServ;j++){
			String nombre = "serv"+j;
				if(serv[i][10].equals(nombre)){ // comparo el nombre del servicio
					//System.out.println(nombreAgente[0]+" compara el atributo "+atrib[j][0]+" con la restricción "+restricciones[i][0]);
					for(int k = 0;k < restricciones[0].length;k++){
						if(i == 0 || i==2 || i== 7){
							if(atrib[i][k]<=restricciones[j][k]){
								cumple[i] = 1;
							}else{
								cumple[i] = 0;
							}
						}else{
							if(atrib[i][k]>=restricciones[j][k]){
								cumple[i] = 1;
							}else{
								cumple[i] = 0;
							}
						}
					}
				}
			}
		}	
		return cumple;
	}
	
	public void enviarRefuse(AID broker, ACLMessage reply, String requerimiento){
		reply = new ACLMessage(ACLMessage.REFUSE);
		reply.addReceiver(broker);
		reply.setContent("NO PARTICIPO");
		reply.setConversationId("service-trade");
		reply.setReplyWith("ref"+System.currentTimeMillis()); 
		myAgent.send(reply);
		tiempo_f = System.currentTimeMillis();
		System.out.println("El "+myAgent.getName()+" envió rechazo a los "+ ( tiempo_f - tiempo_i ) +" milisegundos.");
		
	}
	
	public int[] elegirMejorOferta(int[] cumple){ // entrega la posición de los mejores servicios
		int[] mejores = new int[nServProv]; // guardo al final la posición de los mejores servicios
		String[] servDisp = getServDisp();
		int suma=0;
		for(int i = 0;i < cumple.length;i++){
			if(cumple[i] == 1) suma++;
		}
		System.out.println("El "+nombreAgente[0]+" tiene "+servDisp.length+" servicios disponibles" );
		ArrayList p = new ArrayList(); // arreglo para guardar precio de bundling
		ArrayList m = new ArrayList(); // arreglo para guardar posición del serv a ofertar
		//ArrayList<Object> n = new ArrayList<Object>(); // arreglo para guardar los nombres de los servicios
		System.out.println("El nro de bund es "+ nroBundling+ " para el "+nombreAgente[0]);
		System.out.println("La prob de bund es "+ probBundling+ " para el "+nombreAgente[0]+" entonces");
		
		if(probBundling>0.4 && suma > 1){
			//System.out.println("Entra a hacer bundling el "+nombreAgente[0]);
			int[] m1 = new int[servDisp.length];
			double[] p1 = new double[servDisp.length];
			for(int i = 0;i < serv.length;i++){
				for(int j = 0;j < servDisp.length;j++){
					if(cumple[i]==1){
						if(servDisp[j].equals(serv[i][10])){
							if(precio[i]>p1[j]){
								p1[j]=precio[i]+(1-descuento)*margen*precio[i]; // precio más el margen con el descuento respectivo
								m1[j]=i;
							}
						}
					}
				}
			}
			//System.out.println("El "+nombreAgente[0]+" tiene "+servDisp.length+" servicios disponibles y "+nroBundling+" nro de bund");
			if(servDisp.length > nroBundling){// Si los servicios disponibles son más que los que asigno a bundling 
				//System.out.println("El "+nombreAgente[0]+" tiene que entrar a escoger lo mejor entre lo disponible" );
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
				int[] agregado = new int[nroBundling];
				for(int i = 0;i < agregado.length;i++){
					if(i>=1){
						boolean agrego = true;
						for(int j = 0;j < i;j++){
							if((serv[m1[i]][10].equals(serv[agregado[j]][10]))){
								agrego = false;
							}
						}
						if(agrego){
							p.add(p1[i]);
							m.add(m1[i]);
							agregado[i] = m1[i];
						}
					}else{ // i = 0 
						p.add(p1[i]);
						m.add(m1[i]);
						agregado[i] = m1[i];
					}					
				}
				System.out.println("El "+nombreAgente[0]+" agregó "+p.size());
			}else
			if(servDisp.length == nroBundling){ // Si es el mismo número
				//System.out.println("El "+nombreAgente[0]+" tiene que enviar el mejor de cada disponible" );
				for(int i = 0;i < servDisp.length;i++){
					p.add(p1[i]);
					m.add(m1[i]);
				}
				System.out.println("El "+nombreAgente[0]+" agregó "+p.size());
			}else
			if(servDisp.length < nroBundling){// Si el número de bundling es mayor que el número de servicios disponibles
				//System.out.println("El "+nombreAgente[0]+" tiene que enviar el mejor de cada disponible sin completar el n de bund" );
				if(servDisp.length==1){
					p1[0]= p1[0]/(1+(1-descuento)*margen); // deshago el descuento, ya que no ofrece un pack realmente
				}
				for(int i = 0;i < servDisp.length;i++){
					p.add(p1[i]);
					m.add(m1[i]);
				}
				System.out.println("El "+nombreAgente[0]+" agregó "+p.size());
				nroBundling = servDisp.length; // actualizo el número de bundling
			}
			
			for(int i = 0;i < m.size();i++){
				mejores[(int) m.get(i)] = 1;
			}
		}else{ // si la probabilidad de bundling es menor, entonces se elige el servicio con el mayor precio para la oferta
			// o si tiene sólo un servicio que cumpla con las restricciones
			//System.out.println("No entra a hacer bundling el "+nombreAgente[0]);
			double p0 = 0.0;
			int m0 = 0;
			for(int i = 0;i < mejores.length;i++){
				if(cumple[i]==1){
					if(precio[i]>p0){
						p0 = precio[i]+precio[i]*margen; // Voy guardando el precio
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
		list.add(serv[0][10]);// agrego el primer serv a la lista
		//System.out.println("Agrego el "+serv[0][10]);
		for(int i = 0;i < nServProv;i++){
			boolean agregar = true;
			for(int j = 0;j < list.size();j++){
				if((list.get(j).equals(serv[i][10]))){
					agregar = false;
					//System.out.println(nombreAgente[0]+" Agrego el "+serv[i][10]);
				}
			}
			if(agregar){
				list.add(serv[i][10]);
			}
		}
		servDisp = new String[list.size()];
		for(int i = 0;i < servDisp.length;i++){
			servDisp[i] = (String) list.get(i);
			//System.out.println("El servicio "+servDisp[i]+ " fue asignado al "+ nombreAgente[0]);
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
	
	public void enviarOferta(AID broker, ACLMessage reply, String requerimiento){
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


