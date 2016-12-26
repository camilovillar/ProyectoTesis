package test;

import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.wrapper.AgentController;
import jade.wrapper.ContainerController;
import jade.wrapper.StaleProxyException;

public abstract class Test {

	public static void main(String[] args) throws StaleProxyException {
		
		// Creo un contenedor de agentes
		Profile p = new ProfileImpl(true);
		ContainerController cc = jade.core.Runtime.instance().createMainContainer(p);
		
		// Creo un agente Nombre, Clase y Argumentos
		AgentController pc = cc.createNewAgent("Agent2", "agents.ProviderAgent", null);
		pc.start();
		
		AgentController ac = cc.createNewAgent("Agent1", "agents.ConsumerAgent", null);
		ac.start();
		
		
		
	}

}
