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
	
	protected void setup(){
		
		System.out.println("Hola! Consumidor "+getAID().getName()+" está listo.");
		
		funcion = new FuncionUtilidad(1);
		proceso = new Proceso(4);
		nproceso = proceso.getName();
		double param[] = funcion.getParametros(); 
		if(param[8]<0){
			funcion = new FuncionUtilidad(1);
			double param2[] = funcion.getParametros();
			for(int j=0;j<param.length;j++){
				param[j]=param2[j];
			}
		}

		//Agregar parametros de funcion de utilidad a proceso. http://stackoverflow.com/questions/23724221/java-append-object-to-json

		JSONArray paramet = new JSONArray();
		
		for(int i = 0;i < param.length;i++){
			paramet.add(param[i]);
		}
		
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
			
			System.out.println("El consumidor ha generado el proceso.");
			// Busco al broker
			DFAgentDescription dfd = new DFAgentDescription();
            ServiceDescription sd  = new ServiceDescription();
            sd.setType( "broker" );
            dfd.addServices(sd);
            
            DFAgentDescription[] result;
            
			try {
				result = DFService.search(this, dfd);
				System.out.println(result.length + " resultados." );
				if (result.length>0){
	                System.out.println(" " + result[0].getName() );
				}
				broker = result[0].getName();
			} catch (FIPAException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
            System.out.println("Broker encontrado: "+broker);
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
		private MessageTemplate mt;
		
		@Override
		public void action() {
			// TODO Auto-generated method stub
			ACLMessage reply = myAgent.receive(mt);
			if (reply != null) {
				
				if (reply.getPerformative() == ACLMessage.INFORM) {// Broker informa los resultados de la negociación
					
					System.out.println(nproceso + " fue negociado exitosamente.");
					System.out.println("Precio = Agregar variable de precio final total. "); // 
					myAgent.doDelete();
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
			return (mt != null);
		}
		
	}// Cierra recibirRespuesta
	
		
}// Cierra la clase
