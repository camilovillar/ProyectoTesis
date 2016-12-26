package agents;

import jade.core.Agent;
import jade.core.AID;
import jade.core.behaviours.*;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;

/* Como se realizará una iteración simple, el Consumer agent cumple el rol que 
 * debería cumplir el broker, ya que él mismo envía sus requerimientos a 
 * los proveedores 
 */


public class ConsumerAgent extends Agent{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String targetService;
	// Lista de proveedores
	private AID[] providerAgents;

	// Inicialización de los agentes
	protected void setup() {

		System.out.println("Hello! Consumer-agent "+getAID().getName()+" is ready.");

		// Hay que darle como argumento el servicio
		Object[] args = getArguments();
		if (args != null && args.length > 0) {
			targetService = (String) args[0];
			System.out.println("Object is " + targetService);

			// Ticker Beahevior que permitiría revisar cada 60 segundos el servicio buscado
			// Es necesario revisar más de una vez ?
			
			addBehaviour(new TickerBehaviour(this, 60000) {
				/**
				 * 
				 */
				private static final long serialVersionUID = 1L;

				protected void onTick() {
					System.out.println("Trying to buy " + targetService);
					
					// Actualizar la lista de proveedores Sera necesario mas de una vez
					
					DFAgentDescription template = new DFAgentDescription();
					ServiceDescription sd = new ServiceDescription();
					sd.setType("service-trade");
					template.addServices(sd);
					try {
						DFAgentDescription[] result = DFService.search(myAgent, template); 
						System.out.println("Found the following provider agents:");
						providerAgents = new AID[result.length];
						for (int i = 0; i < result.length; ++i) {
							providerAgents[i] = result[i].getName();
							System.out.println(providerAgents[i].getName());
						}
					}
					catch (FIPAException fe) {
						fe.printStackTrace();
					}

					// Realizar el reuest
					myAgent.addBehaviour(new RequestPerformer());
				}
			} );
		}
		else {
			// Para terminar o eleiminar el agente
			System.out.println("No target service name specified");
			doDelete();
		}
	}

	protected void takeDown() {
		
		System.out.println("Consumer-agent "+getAID().getName()+" terminating.");
	}

	/**
	   Inner class RequestPerformer.
	   This is the behaviour used by Service-buyer agents to request provider 
	   agents the target book.
	 */
	private class RequestPerformer extends Behaviour {
		
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		//Cambiar en ProviderAgent
		private AID bestProvider; 
		private int bestPrice;  
		private int repliesCnt = 0; 
		private MessageTemplate mt; 
		private int step = 0;

		public void action() {
			switch (step) {
			case 0:
				// Enviar Call For Proposal a los proveedores de la lista
				ACLMessage cfp = new ACLMessage(ACLMessage.CFP);
				for (int i = 0; i < providerAgents.length; ++i) {
					cfp.addReceiver(providerAgents[i]);
				} 
				cfp.setContent(targetService);
				cfp.setConversationId("service-trade");
				cfp.setReplyWith("cfp"+System.currentTimeMillis()); 
				myAgent.send(cfp);
				// Crear un template para responder
				mt = MessageTemplate.and(MessageTemplate.MatchConversationId("service-trade"),
						MessageTemplate.MatchInReplyTo(cfp.getReplyWith()));
				step = 1;
				break;
			case 1:
				// Recibo respuestas
				// Analizo las proposiciones
				
				ACLMessage reply = myAgent.receive(mt);
				if (reply != null) {
					
					if (reply.getPerformative() == ACLMessage.PROPOSE) {
						// Oferta
						
						// Esta oferta debería entregar los atributos de calidad, no sólo el precio
						/* Aquí deberían correr o usar los algoritmos de optimización
						* para elegir los mejores servicios y los mejores proveedores.
						*/
						
						int price = Integer.parseInt(reply.getContent());
						if (bestProvider == null || price < bestPrice) {
							// Se toma la mejor
							bestPrice = price;
							bestProvider = reply.getSender();
						}
					}
					repliesCnt++;
					if (repliesCnt >= providerAgents.length) {
						// se analizaron todas las respuestas
						step = 2; 
					}
				}
				else {
					block();
				}
				break;
			case 2:
				// se responde al mejor proveedor
				
				ACLMessage order = new ACLMessage(ACLMessage.ACCEPT_PROPOSAL);
				order.addReceiver(bestProvider);
				order.setContent(targetService);
				order.setConversationId("service-trade");
				order.setReplyWith("order"+System.currentTimeMillis());
				myAgent.send(order);
				// se prepara el template
				mt = MessageTemplate.and(MessageTemplate.MatchConversationId("service-trade"),
						MessageTemplate.MatchInReplyTo(order.getReplyWith()));
				step = 3;
				break;
			case 3:      
				// recibe la respuesta
				reply = myAgent.receive(mt);
				if (reply != null) {
					// orden de compra recibida
					if (reply.getPerformative() == ACLMessage.INFORM) {
						// éxito, se puede terminar
						
						System.out.println(targetService+" successfully purchased from agent "+reply.getSender().getName());
						System.out.println("Price = "+bestPrice);
						myAgent.doDelete();
					}
					else {
						System.out.println("Attempt failed: requested book already sold.");
					}

					step = 4;
				}
				else {
					block();
				}
				break;
			}        
		}

		public boolean done() {
			//si no hay mejor proveedor entonces no está el servicio disponible
			if (step == 2 && bestProvider == null) {
				System.out.println("Attempt failed: "+targetService+" not available for sale");
			}
			return ((step == 2 && bestProvider == null) || step == 4);
		}
	}  

}
