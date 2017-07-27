package ofertas;

import java.util.ArrayList;

public class Ofertas {
	
	String[][] ofertas;
	int serv;
	
	public Ofertas(String[][] ofertas, int serv){
		
		this.ofertas = ofertas;
		this.serv = serv;
	}
	
	public String[][] buscarOfertas(String nombre){
		String[][] ofertasServ;
		ArrayList<Integer> ofrecidos = ordenarServicios(ofertas,nombre);
		ofertasServ = new String[ofrecidos.size()][ofertas[0].length];
		
		for(int i = 0;i < ofrecidos.size();i++){
			for(int j= 0;j < ofertas[0].length;j++){
				ofertasServ[i][j]=ofertas[ofrecidos.get(i)][j];
			}
		}
		return ofertasServ;
	}
	
	// Ordeno las ofertas según la actividad a la que corresponden, luego las ordeno en un array según la actividad
	// Y esos array los guardo en el array final que se entregará
	public ArrayList<ArrayList> buscarOfertas(String[] nombres){
		ArrayList<ArrayList> ofrecidos = new ArrayList<ArrayList>();
		for(int i = 0;i < nombres.length;i++){
			ArrayList<Integer> orden = ordenarServicios(ofertas,nombres[i]);
			ArrayList ofertasActI = new ArrayList(); 
			for(int j = 0;j < orden.size();j++){
				ofertasActI.add(orden.get(j));
			}
			ofrecidos.add(ofertasActI);
		}
		return ofrecidos; 
	}
		
	public ArrayList<Integer> ordenarServicios(String[][] ofertas, String serv){ // Genera un arraylist con las posiciones en ofertas para el servicio serv baasado en las ofertas obtenidas.
		ArrayList<Integer> ofrecidos = new ArrayList<Integer>();
		for(int i = 0;i < ofertas.length;i++){
			if(serv.equals(ofertas[i][10])){
				ofrecidos.add(i);
			}
		}
		return ofrecidos;
	}
	
	public int[] asignarAleatorio(ArrayList ofertasOrdenadas){
		int[] asignar = new int[serv];
		for(int i = 0;i < serv; i++){
			ArrayList ofertas = (ArrayList) ofertasOrdenadas.get(i);
			int max = ofertas.size();
			//System.out.println("Se encontraron "+max+ " ofertas.");
			if(max>0){
				double aleatorio = Math.random()*max;
				int lugar = (int) ofertas.get((int) aleatorio);
				asignar[i] = lugar;
			}else{
				System.out.println("No se encontraron ofertas.");
			}
		}
		
		return asignar;
	}

}
