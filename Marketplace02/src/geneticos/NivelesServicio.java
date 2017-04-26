package geneticos;

public class NivelesServicio {
	
	double[][] atributos;
	String[] act;
	int nServ;
	
	public NivelesServicio(String[][] atrib, int nServ){
		atributos = new double[atrib.length][atrib[0].length-1];
		for(int i = 0;i < atrib.length;i++){
			for(int j = 0;j < atrib[0].length-1;j++){
				atributos[i][j] = Double.parseDouble(atrib[i][j]);
			}
			act[i] = atrib[i][atrib[0].length];
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
			String act1 = "serv"+(i+1);
			
			for(int j = 0;j<atributos[0].length;j++){
				maximos[i][j] = calcMaximo(act1, j);
				minimos[i][j] = calcMinimo(act1, j);
				delta[i][j] = (maximos[i][j]-minimos[i][j])/niveles;
				arreglo[i][j][0]=minimos[i][j];
				for(int k = 1;k<niveles;k++){
					arreglo[i][j][k]+=delta[i][j];
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
			}
		}
		return num;
	}
	
	

}
