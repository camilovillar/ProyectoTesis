package funciones;

public class FuncionUtilidad {
	
	double[] parametros = new double[9];
	double descuento;
	double suma;
	
	public FuncionUtilidad(int tipo){// tipo indica si el agente es consumidor o proveedor.
		// tipo 1 indica consumidor
		if(tipo == 1){
			int numero = 9; // número de atributos de calidad
			double media = (double) 1/(numero+2);
			double sd = (double) 1/(2*numero);
			
			suma = 0;
			for(int i =0; i < numero ; i++){
				if(i==numero-1){
					if(suma>=1){
						double param = parametros[i-1];
						suma-=parametros[i-1];
						parametros[i-1]=parametros[i-1]/2;
						suma+=parametros[i-1];
						parametros[i]=1-suma;
					}
					double y = 0;
					y = 1-suma;
					parametros[i] = y;
					suma+=y;
				}else{
					double x = Math.random();
					double y = x*sd + media;
					parametros[i] = y;
					suma+=y;
				}
			}
			
			descuento = 0;
			
			
		}// tipo 2 indica proveedor
		
		else{
			int numero = 9; // número de atributos de calidad
			double media = (double) (1/(numero+1));
			double sd = (double) (1/(2*numero));
			suma = 0;			
			for(int i =0; i < numero ; i++){
				if(i==numero-1){
					double y = 0;
					y = 1-suma;
					parametros[i] = y;
					suma+=y;
				}else{
					double x = Math.random();
					double y = x*sd + media;
					parametros[i] = y;
					suma+=y;
				}
			}
			descuento = (Math.random()*0.7)+0.1;
			
		}	
		
	} // Cierra constructor
	
	public double[] getParametros(){
		return parametros;
	}
	
	public double getDescuento(){
		return descuento;
	}
	
	public double getSuma(){
		return suma;
	}
	
}
