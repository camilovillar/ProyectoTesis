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
import parametros.Parametros;

public class Test {

	public static void main(String[] args) throws StaleProxyException, FileNotFoundException {
		//long ti = System.currentTimeMillis();
		int i = 10; // setea el número de actividades del proceso
		int j = 100; // setear el número de proveedores 
		int l = 1000; // setea número de datos de servicios a utilizar
		int k = 1; //setea el nivel de las restricciones
		
		int niveles = 50; //niveles de servicio en descomposición 5-75 (10)
		int poblacionLocal = 20; // población inicial 10-250 (10)
		int mutacionLocal = 5; // mutación 1-10 (1)
		double crossoverLocal = 0.8; // crossover 0.1-0.9
		int evolucionesLocal = 50;//evoluciones 10-100(10)
		int geneticosLocal = 2; // 0 = lineal, 1 = geneticos, 2 = nada
		
		int poblacionGlobal = 20;
		int mutacionGlobal = 5;
		double crossoverGlobal = 0.8;
		int evolucionesGlobal = 50;
		int geneticosGlobal = 0; // 0 = lineal, 1 = geneticos
		
		Parametros p = new Parametros(niveles,poblacionLocal,mutacionLocal,crossoverLocal,evolucionesLocal, geneticosLocal,poblacionGlobal, mutacionGlobal, crossoverGlobal, evolucionesGlobal, geneticosGlobal); 
		
		//for(i=10;i<11;i+=5){
			//int cont =0;
			//while(cont < 10){
			/*	try {
	    	*		Thread.sleep((int) (60000));
	    	*	} catch (Exception e) {
	    	*		e.printStackTrace();
	    	*	}
			*/	
			try{
				FileOutputStream file = new FileOutputStream("C:\\Users\\Camilo\\Desktop\\Eclipse\\resultados\\lineal\\output"+i+"Act"+j+"Prov"+l+"Serv"+System.currentTimeMillis()+".txt");
				PrintStream out = new PrintStream(file);
				System.setOut(out);
				
				// Actividades, proveedores, debo redistribuir los servicios (true si cambio número de actividades o proveedores),servicios a utilizar, indica grado de restricciones para descomposición de restricciones (0.3, 0.5 o 0.7)
				Marketplace market = new Marketplace(i,j,true, l, k, p );
				
				
				System.out.println(niveles+" niveles de servicio en descomposición");
				System.out.println(poblacionLocal+" población inicial Local");
				System.out.println(mutacionLocal+" mutación Local");
				System.out.println(crossoverLocal+" crossover Local");
				System.out.println(evolucionesLocal+" evoluciones Local");
				
				System.out.println(poblacionLocal+" población inicial Global");
				System.out.println(mutacionLocal+" mutación Global");
				System.out.println(crossoverLocal+" crossover Global");
				System.out.println(evolucionesLocal+" evoluciones Global");
				//System.out.println("Comienza la negociación.");
				//boolean fin = true;
				
				try {
	    			Thread.sleep((int) (60000));
	    		} catch (Exception e) {
	    			e.printStackTrace();
	    		}
				
				market.detenerMarket();
				Thread.currentThread().interrupt();
			
			//long tf = System.currentTimeMillis();
			//System.out.println("Termina la negociación a los "+(tf-ti)+" milisegundos.");
				out.close();
				}catch(Exception e){
					e.printStackTrace();
				}
				//cont++;
			//}
		//}// Cierra for
	} 
}
