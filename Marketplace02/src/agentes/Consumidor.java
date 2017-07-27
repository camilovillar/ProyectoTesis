package agentes;

import jade.core.behaviours.*;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import funciones.FuncionUtilidad;
import procesos.Proceso;

import jade.core.AID;
import jade.core.Agent;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;

public class Consumidor extends Agent{
	
	// Envía proceso a Bróker
	// Parámetros de la función de utilidad y los envía
	//  Se debe generar un proceso, incluye restricciones globales
	//  
	// Recibe respuesta del bróker
	// Responde al bróker con si o no
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -4248736635939866841L;
	private String[] servicios;
	private AID broker;
	//private long tiempo_i;
	//private long tiempo_f;
	
	private Proceso proceso;
	private FuncionUtilidad funcion;
	private String nproceso;
	private long tiempo_i;
	private long tiempo_f;
	private double presupuesto;
	
	protected void setup(){
		tiempo_i = System.currentTimeMillis();
		System.out.println("Hola! Consumidor "+getAID().getName()+" está listo.");
		
		//recupero los argumentos entregados, indica el número de actividades en el proceso
		Object[] args = getArguments();
		int n=0;
		int r = 1;
		
		if (args != null && args.length > 0) {
			n = (int) args[0];
			r = (int) args[1];
		}
		
		
		
		funcion = new FuncionUtilidad(1);
		proceso = new Proceso(n, r); 
		nproceso = proceso.getName();
		double param[] = funcion.getParametros(); 
		
		presupuesto = proceso.getPresupuesto(r);

		//Agregar parametros de funcion de utilidad a proceso. http://stackoverflow.com/questions/23724221/java-append-object-to-json

		
		
		/* public void agregarParam(double[] param){
			JSONArray paramet = new JSONArray();
		for(int i = 0;i < param.length;i++){
			paramet.add(param[i]);
		}
		
		// Agregar parámetros de función de utilidad
		JSONParser parser = new JSONParser();
		try {
			Object obj = parser.parse(new FileReader("C:\\Users\\Camilo\\Desktop\\Eclipse\\JSON\\procesos\\"+proceso.getName()+".json"));
			JSONObject jsonObject = (JSONObject) obj;
			jsonObject.put("Parametros", paramet);
			FileWriter file = new FileWriter("C:\\Users\\Camilo\\Desktop\\Eclipse\\JSON\\procesos\\"+proceso.getName()+".json");
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
		
		
		 */
		
		
		
		JSONArray paramet = new JSONArray();
		for(int i = 0;i < param.length;i++){
			paramet.add(param[i]);
		}
		
		// Agregar parámetros de función de utilidad
		JSONParser parser = new JSONParser();
		try {
			Object obj = parser.parse(new FileReader("C:\\Users\\Camilo\\Desktop\\Eclipse\\JSON\\procesos\\"+proceso.getName()+".json"));
			JSONObject jsonObject = (JSONObject) obj;
			jsonObject.put("Parametros", paramet);
			FileWriter file = new FileWriter("C:\\Users\\Camilo\\Desktop\\Eclipse\\JSON\\procesos\\"+proceso.getName()+".json");
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
		
		
		servicios = proceso.getActiv();
		if (servicios != null && servicios.length > 0) {
			
			System.out.println("El consumidor ha generado el proceso " + proceso.getName());
			// Busco al broker
			DFAgentDescription dfd = new DFAgentDescription();
            ServiceDescription sd  = new ServiceDescription();
            sd.setType( "broker" );
            dfd.addServices(sd);
            
            DFAgentDescription[] result;
            
          //Sleep para esperar la inicialización del broker con una parte random
    		try {
    			Thread.sleep((int) (300 * (2*Math.random())));
    		} catch (Exception e) {
    			e.printStackTrace();
    		}
    		
    		//www.redeszone.net/2012/09/03/curso-java-volumen-vi-todo-sobre-semaforos-en-java/#sthash.f6ftBjtO.dpuf
    		
            
            
			try {
				result = DFService.search(this, dfd);
				System.out.println("Se encontró "+result.length +" broker. Su nombre es "+result[0].getName() );
				broker = result[0].getName();
			} catch (FIPAException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			tiempo_f = System.currentTimeMillis();
            System.out.println("Broker encontrado: "+broker+". a los (milisegundos) "+ ( tiempo_f - tiempo_i ));
            
			addBehaviour(new enviarRequerimiento());
			addBehaviour(new recibirPropuesta());
			
			
		} // Cierra el if
		
	} // Cierra el setup()
	
	protected void takeDown() {
		
		System.out.println("Consumidor "+getAID().getName()+" terminado.");
		
	}
	
	private class enviarRequerimiento extends OneShotBehaviour {

		/**
		 * 
		 */
		private static final long serialVersionUID = -4155909910790581855L;
		@Override
		public void action() {
			// TODO Auto-generated method stub
			
			ACLMessage inf = new ACLMessage(ACLMessage.INFORM);
			inf.addReceiver(broker);
			inf.setContent(nproceso);
			inf.setConversationId("service-trade");
			inf.setReplyWith("inf"+System.currentTimeMillis());
			myAgent.send(inf);
			MessageTemplate.and(MessageTemplate.MatchConversationId("service-trade"), MessageTemplate.MatchInReplyTo(inf.getReplyWith()));			
			//System.out.println("El consumidor envía el proceso al bróker.");
		}
		
	} // Cierra Behaviour
	
	private class recibirPropuesta extends Behaviour {

		private static final long serialVersionUID = -6144388368618375199L;
		
		
		@Override
		public void action() {
			// TODO Auto-generated method stub
			MessageTemplate mt = new MessageTemplate(null);
			ACLMessage reply = myAgent.receive(mt);
			if (reply != null) {
				
				if (reply.getPerformative() == ACLMessage.INFORM) {// Broker informa los resultados de la negociación
					double precio1 = 0;
					String info = reply.getContent();
					JSONParser parser = new JSONParser();
					try {
						FileReader f = new FileReader("C:\\Users\\Camilo\\Desktop\\Eclipse\\JSON\\resultados\\"+info+".json");
						Object resul = parser.parse(f);
						JSONObject jsonResul = (JSONObject) resul;
						
						JSONArray proveedores = (JSONArray) jsonResul.get("Proveedores");
						long precio = (long) jsonResul.get("Precio");
						precio1 = (double) precio;
						//obtener la info
					
					} catch (FileNotFoundException e) {
						e.printStackTrace();
					} catch (IOException e) {
						e.printStackTrace();
						//manejo de error
					} catch (ParseException e) {
						e.printStackTrace();
						//manejo de error
					}
					
					//obtener resultados de la negociación
					// Comparar con funcion de utilidad 
					
					
					System.out.println("Precio =  "+precio1); //
					if(precio1<presupuesto){
						System.out.println(nproceso + " fue negociado exitosamente.");
						myAgent.doDelete();
					}else{
						System.out.println("Negociación fallida: proceso no instanciado");
						doDelete();
					}
				}
				else {
					System.out.println("Intento fallido: proceso no instanciado");
					doDelete();
				}
			}
			else {
				block();
			}
				
				
			}// Cierra el action
			
		

		@Override
		public boolean done() {
			// TODO Auto-generated method stub
			return true;
		}
		
	}// Cierra recibirRespuesta
	
		
}// Cierra la clase
