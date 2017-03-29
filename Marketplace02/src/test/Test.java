package test;
//import funciones.FuncionUtilidad;
import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.wrapper.AgentController;
import jade.wrapper.ContainerController;
import jade.wrapper.StaleProxyException;
//import procesos.Proceso;
import servicios.Servicios;

public class Test {

	public static void main(String[] args) throws StaleProxyException {
		/*Servicios s = new Servicios();
		s.creaArchivoServicios();
		String[][] atrib = s.getAtributos(s.getServicios(2507, 10, 4), 2507); // Servicios totales, proveedores, actividades proceso
		s.creaArchivosProveedores(atrib, 10); // proveedores
		*/
		
		Profile p = new ProfileImpl(true);
		ContainerController cc = jade.core.Runtime.instance().createMainContainer(p);
		
		AgentController ac0 = cc.createNewAgent("Agente1", "agentes.Broker", null);
		ac0.start();
		
		AgentController ac1 = cc.createNewAgent("Agente2", "agentes.Consumidor", null);
		ac1.start();
		
		AgentController ac2 = cc.createNewAgent("Proveedor0", "agentes.Proveedor", null);
		AgentController ac3 = cc.createNewAgent("Proveedor1", "agentes.Proveedor", null);
		AgentController ac4 = cc.createNewAgent("Proveedor2", "agentes.Proveedor", null);
		AgentController ac5 = cc.createNewAgent("Proveedor3", "agentes.Proveedor", null);
		AgentController ac6 = cc.createNewAgent("Proveedor4", "agentes.Proveedor", null);
		AgentController ac7 = cc.createNewAgent("Proveedor5", "agentes.Proveedor", null);
		AgentController ac8 = cc.createNewAgent("Proveedor6", "agentes.Proveedor", null);
		AgentController ac9 = cc.createNewAgent("Proveedor7", "agentes.Proveedor", null);
		AgentController ac10 = cc.createNewAgent("Proveedor8", "agentes.Proveedor", null);
		AgentController ac11 = cc.createNewAgent("Proveedor9", "agentes.Proveedor", null);
		ac2.start();
		ac3.start();
		ac4.start();
		ac5.start();
		ac6.start();
		ac7.start();
		ac8.start();
		ac9.start();
		ac10.start();
		ac11.start();
		

	}

}
