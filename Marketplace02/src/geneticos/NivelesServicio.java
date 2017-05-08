package geneticos;

public class NivelesServicio {
	
	double[][] atributos;
	String[] act;
	int nServ;
	
	public NivelesServicio(String[][] atrib, int nServ){
		/*System.out.println("Atrib tiene las siguientes dimensiones: "+atrib.length + " y "+atrib[0].length);
		for(int y = 0;y < atrib.length;y++){
			for(int z = 0;z < atrib[0].length;z++){
				System.out.println("Atrib "+atrib[y][z]);
			}
		}*/
		/*System.out.println("Se recibe atrib: " + atrib[0][0]);
		System.out.println("Se recibe atrib: " + atrib[1][0]);
		System.out.println("Se recibe atrib: " + atrib[0][1]);*/
		act = new String[atrib.length];
		atributos = new double[atrib.length][atrib[0].length-1];
		for(int i = 0;i < atrib.length;i++){
			for(int j = 1;j < atrib[0].length;j++){
				float aux = Float.parseFloat(atrib[i][j]);
				atributos[i][j-1] = (double) aux;
			}
			act[i] = atrib[i][0];
		}
		
		this.nServ = nServ;
	} // Cierra constructor
	
	public double calcMinimo(String serv, int pos){ // Minimo para un servicio de un atributo en la posición pos del arreglo
		double minimo=999.0;
		for(int i = 0;i<atributos.length;i++){
			if(act[i].equals(serv) && atributos[i][pos] < minimo){
				minimo=atributos[i][pos];
			}
		}
		return minimo;
	}
	
	public double calcMaximo(String serv, int pos){
		double maximo= -999.0;
		for(int i = 0;i<atributos.length;i++){
			if(act[i].equals(serv) && atributos[i][pos] > maximo){
				maximo=atributos[i][pos];
			}
		}
		return maximo;	
	}
	
	public double[][][] getNiveles(int niveles){
		
		double[][][] arreglo = new double[nServ][atributos[0].length][niveles]; // numero de servicios x atributos x niveles
		
		double[][] maximos = new double[nServ][atributos[0].length];
		double[][] minimos = new double[nServ][atributos[0].length];
		double[][] delta = new double[nServ][atributos[0].length];
		
		for(int i = 0;i < nServ;i++){
			String act1 = "serv"+(i);
			for(int j = 0;j<atributos[0].length;j++){
				maximos[i][j] = calcMaximo(act1, j);
				//System.out.println("Se setea el máximo del "+act1+" atributo "+j+" en "+maximos[i][j]);
				minimos[i][j] = calcMinimo(act1, j);
				//System.out.println("Se setea el mínimo del "+act1+" atributo "+j+" en "+minimos[i][j]);
				delta[i][j] = (maximos[i][j]-minimos[i][j])/niveles;
				
				
				for(int k = 0;k < niveles;k++){
					if(k < 1){
						arreglo[i][j][k]=minimos[i][j];
						//System.out.println("El valor del arreglo es para "+i+", "+j+", "+0+" es: "+arreglo[i][j][0]);
					}else{
						arreglo[i][j][k] = arreglo[i][j][k-1] + delta[i][j];
						//System.out.println("El valor del arreglo es para "+i+", "+j+", "+k+" es: "+arreglo[i][j][k]);
					}
				}
			}
		}
		return arreglo;
	}
	
	public double[][] asignarAleatorio(int niveles){
		double[][][] arreglo = getNiveles(niveles); 
		double[][] num = new double[arreglo.length][arreglo[0].length]; // numero de servicios x atributos
		for(int i =0;i < arreglo.length;i++){
			for(int j =0;j < arreglo[0].length;j++){
				int aleatorio = (int) Math.floor(Math.random() * niveles);
				num[i][j] = arreglo[i][j][aleatorio];
				//System.out.println("Para el número "+i+", "+j+" se asigna el valor "+ arreglo[i][j][aleatorio]);
			}
		}
		return num;
	}
	
	

}
