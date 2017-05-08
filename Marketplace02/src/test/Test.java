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
		//for(int i=5;i<21;i++){
		FileOutputStream file = new FileOutputStream("C:\\Users\\Camilo\\Desktop\\Eclipse\\resultados\\output"+35+"Act"+100+"Prov"+System.currentTimeMillis()+".txt");
		PrintStream out = new PrintStream(file);
		System.setOut(out);
		
			Marketplace m = new Marketplace(,100,false); // Actividades, proveedores, debo redistribuir los servicios (true si cambio número de proveedores)
			System.out.println("Comienza la negociación.");
			boolean fin = true;
			while(fin){
				fin = m.getFin();
			}
			
			m.detenerMarket();
			
			long tf = System.currentTimeMillis();
			System.out.println("Termina la negociación a los "+(tf-ti)+" milisegundos.");
			
			out.close();

		//}
	} 
}
