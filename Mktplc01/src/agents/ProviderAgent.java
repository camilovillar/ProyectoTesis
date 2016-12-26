package agents;

import jade.core.Agent;
import jade.core.behaviours.*;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import java.util.*;

public class ProviderAgent extends Agent{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
		// Se usa un catálogo para ordenar el servicio, sus atributos
		private Hashtable catalogue;
		// se maneja a través de una GUI, esto se deberá eliminar
		private ProviderGui myGui;

		// inicializaciones
		protected void setup() {
			
			catalogue = new Hashtable();

			// GUI
			myGui = new ProviderGui(this);
			myGui.showGui();

			// Registrar el servicio
			DFAgentDescription dfd = new DFAgentDescription();
			dfd.setName(getAID());
			ServiceDescription sd = new ServiceDescription();
			sd.setType("service-trade");
			sd.setName("servicio1");
			dfd.addServices(sd);
			try {
				DFService.register(this, dfd);
			}
			catch (FIPAException fe) {
				fe.printStackTrace();
			}

			// behaviot para responder a consumidores
			addBehaviour(new OfferRequestsServer());

			// para enviar ordenes de compra
			addBehaviour(new PurchaseOrdersServer());
		}

	
		protected void takeDown() {
			// Si lo elimino lo borro del registro
			try {
				DFService.deregister(this);
			}
			catch (FIPAException fe) {
				fe.printStackTrace();
			}
			// GUI
			myGui.dispose();
			
			System.out.println("Provider-agent "+getAID().getName()+" terminating.");
		}

		/**
	     This is invoked by the GUI when the user adds a new book for sale
		 */
		public void updateCatalogue(final String name, final int price) {
			addBehaviour(new OneShotBehaviour() {
				/**
				 * 
				 */
				private static final long serialVersionUID = 1L;

				public void action() {
					catalogue.put(name, new Integer(price));
					System.out.println(name+" inserted into catalogue. Price = "+price);
				}
			} );
		}

		/**
		   Inner class OfferRequestsServer.
		   This is the behaviour used by Book-seller agents to serve incoming requests 
		   for offer from buyer agents.
		   If the requested book is in the local catalogue the seller agent replies 
		   with a PROPOSE message specifying the price. Otherwise a REFUSE message is
		   sent back.
		 */
		private class OfferRequestsServer extends CyclicBehaviour {
			/**
			 * 
			 */
			private static final long serialVersionUID = 1L;

			
			// envio de mensajes
			public void action() {
				MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.CFP);
				ACLMessage msg = myAgent.receive(mt);
				if (msg != null) {
					// CFP recibido
					String name = msg.getContent();
					ACLMessage reply = msg.createReply();

					Integer price = (Integer) catalogue.get(name);
					if (price != null) {
						// disponible, enviar precio
						reply.setPerformative(ACLMessage.PROPOSE);
						reply.setContent(String.valueOf(price.intValue()));
					}
					else {
						// no disponible
						reply.setPerformative(ACLMessage.REFUSE);
						reply.setContent("not-available");
					}
					myAgent.send(reply);
				}
				else {
					block();
				}
			}
		}  

		/**
		   Inner class PurchaseOrdersServer.
		   This is the behaviour used by Book-seller agents to serve incoming 
		   offer acceptances (i.e. purchase orders) from buyer agents.
		   The seller agent removes the purchased book from its catalogue 
		   and replies with an INFORM message to notify the buyer that the
		   purchase has been sucesfully completed.
		 */
		private class PurchaseOrdersServer extends CyclicBehaviour {
			/**
			 * 
			 */
			private static final long serialVersionUID = 1L;

			public void action() {
				MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.ACCEPT_PROPOSAL);
				ACLMessage msg = myAgent.receive(mt);
				if (msg != null) {
					// ACCEPT_PROPOSAL recibido
					String title = msg.getContent();
					ACLMessage reply = msg.createReply();

					Integer price = (Integer) catalogue.remove(title);
					if (price != null) {
						reply.setPerformative(ACLMessage.INFORM);
						System.out.println(title+" sold to agent "+msg.getSender().getName());
					}
					else {
						// falla en venta
						reply.setPerformative(ACLMessage.FAILURE);
						reply.setContent("not-available");
					}
					myAgent.send(reply);
				}
				else {
					block();
				}
			}
		}  

}
