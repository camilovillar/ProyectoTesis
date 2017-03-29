package funciones;

public class Agregacion {
	
	// Se diferencia por porcentajes (p), tiempo (t), rendimiento (r) y precio (pr)
	// Además según el tipo de actividad (sec, it, sw, par) 
	
	public double sumaPonderada(double param[], double values[]){ // Recibe un arreglo n x 2 Valores y ponderadores
		
		double suma = 0;
		for(int i =0; i<param.length;i++){
			suma+=param[i]*values[i];
		}
		return suma;		
	}
	
	public double maximo(double param[]){
		
		double maximo = -99999;
		for(int i =0; i<param.length;i++){
			if(param[i]>maximo){
				maximo=param[i];
			}
		}
		return maximo;
	}
	
	public double minimo(double param[]){
		
		double minimo = 99999;
		for(int i = 0; i<param.length; i++){
			if(param[i]<minimo){
				minimo = param[i];
			}
		}
		return minimo;
	}
	public double sumaNormal(double param[]){
		
		double suma = 0;
		for(int i =0;i<param.length;i++){
			suma+=param[i];
		}
		return suma;
		
	}
	public double pitatoria(double param[]){
		
		double multiplica = 0;
		for(int i =0;i<param.length;i++){
			multiplica*=param[i];
		}
		return multiplica;
		
	}
	public double potencia(double param[]){ // base, n
		
		double potencia = Math.pow(param[0], param[1]);
		return potencia;
		
	}
	
}
