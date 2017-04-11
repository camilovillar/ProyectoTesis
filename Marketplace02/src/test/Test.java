package test;
import java.io.FileNotFoundException;
//import java.io.FileOutputStream;
//import java.io.PrintStream;
import java.io.FileOutputStream;
import java.io.PrintStream;

//import funciones.FuncionUtilidad;
//import jade.core.Profile;
//import jade.core.ProfileImpl;
//import jade.wrapper.AgentController;
//import jade.wrapper.ContainerController;
import jade.wrapper.StaleProxyException;
import marketplace.Marketplace;
//import procesos.Proceso;
//import servicios.Servicios;

public class Test {

	public static void main(String[] args) throws StaleProxyException, FileNotFoundException {
		long ti = System.currentTimeMillis();
		//for(int i=1;i<21;i++){
			
			Marketplace m = new Marketplace(1,100,true); // Actividades, proveedores, debo redistribuir los servicios (true si cambio número de proveedores)
			System.out.println("Comienza la negociación.");
			boolean fin = true;
			while(fin){
				fin = m.getFin();
			}
			
			m.detenerMarket();
			
			long tf = System.currentTimeMillis();
			System.out.println("Termina la negociación a los "+(tf-ti)+" milisegundos.");
			PrintStream out = new PrintStream(new FileOutputStream("C:\\Users\\Camilo\\Desktop\\Eclipse\\resultados\\output"+1+"Act"+100+"Prov"+System.currentTimeMillis()+".txt"));
			System.setOut(out);

		//}
	} 
}
