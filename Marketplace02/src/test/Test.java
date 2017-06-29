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
		//long ti = System.currentTimeMillis();
		int i = 10; // setear el número de proveedores
		int j = 100; // setea número de datos de servicios a utilizar
		int l = 2000;
		for(int cont=0;cont<2001;cont++){
			try{
				FileOutputStream file = new FileOutputStream("C:\\Users\\Camilo\\Desktop\\Eclipse\\resultados\\output"+i+"Act"+j+"Prov"+System.currentTimeMillis()+".txt");
				PrintStream out = new PrintStream(file);
				System.setOut(out);
		
				Marketplace m = new Marketplace(i,j,true, l); // Actividades, proveedores, debo redistribuir los servicios (true si cambio número de actividades o proveedores)
				System.out.println("Comienza la negociación.");
				boolean fin = true;
				
				try {
	    			Thread.sleep((int) (60000 * (1 + 2*Math.random())));
	    		} catch (Exception e) {
	    			e.printStackTrace();
	    		}
			
				m.detenerMarket();
			
			//long tf = System.currentTimeMillis();
			//System.out.println("Termina la negociación a los "+(tf-ti)+" milisegundos.");
			
				out.close();
				}catch(Exception e){
					e.printStackTrace();
				}
		}// Cierra for
	} 
}
