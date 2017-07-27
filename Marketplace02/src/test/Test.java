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
		int i = 20; // setea el número de actividades del proceso
		int j = 100; // setear el número de proveedores 
		int l = 1000; // setea número de datos de servicios a utilizar
		int k = 1; //setea el nivel de las restricciones
		for(int cont=0;cont<1;cont++){
			try{
				FileOutputStream file = new FileOutputStream("C:\\Users\\Camilo\\Desktop\\Eclipse\\resultados\\lineal\\output"+i+"Act"+j+"Prov"+l+"Serv"+System.currentTimeMillis()+".txt");
				PrintStream out = new PrintStream(file);
				System.setOut(out);
				
				// Actividades, proveedores, debo redistribuir los servicios (true si cambio número de actividades o proveedores),servicios a utilizar, indica grado de restricciones para descomposición de restricciones (0.3, 0.5 o 0.7)
				Marketplace m = new Marketplace(i,j,false, l, k);
				
				System.out.println("Comienza la negociación.");
				//boolean fin = true;
				
				try {
	    			Thread.sleep((int) (30000 * (1 + 2*Math.random())));
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
