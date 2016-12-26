package test;

import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;
import org.graphstream.graph.implementations.SingleGraph;

import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.wrapper.AgentController;
import jade.wrapper.ContainerController;
import jade.wrapper.StaleProxyException;

public abstract class Test {

	public static void main(String[] args) throws StaleProxyException, InterruptedException {
		
				
		//Creo un nodo para transar	
		Graph graph = new SingleGraph("Graph 1");
		graph.addNode("A");
		
		// Agrego atributos
		Node a =graph.getNode("A");
		a.setAttribute("name", "service1");
		a.setAttribute("dap", 900);
		
		// Creo un contenedor de agentes
		Profile p = new ProfileImpl(true);
		ContainerController cc = jade.core.Runtime.instance().createMainContainer(p);
		
		// Creo un agente Nombre, Clase y Argumentos
		AgentController pc = cc.createNewAgent("Agent2", "agents.ProviderAgent", null);
		pc.start();
		
		// Thread.sleep (600);
				
		AgentController pc2 = cc.createNewAgent("Agent3", "agents.ProviderAgent", null);
		pc2.start();
		
		AgentController pc3 = cc.createNewAgent("Agent4", "agents.ProviderAgent", null);
		pc3.start();
		
		AgentController ac = cc.createNewAgent("Agent1", "agents.ConsumerAgent", args);
		ac.start();
		
		/*
		if(getCatalogue.isEmpty()){
			ac.activate();
		}
			*/	
	
		
		
	}

}
