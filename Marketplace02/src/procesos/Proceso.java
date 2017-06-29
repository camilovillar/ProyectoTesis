package procesos;

import funciones.Agregacion;

import java.io.FileWriter;
import java.io.IOException;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

public class Proceso extends Agregacion{
	
	private String[][] restricciones = new String[10][2];
	private int act;
	private String[] texto;
	private int[] nNodos;
	private int[] tipoNodo;
	private String[] activ;
	private int[] iter;
	private double[] probab;
	private double presupuesto;
	
	private String nombre;
	JSONObject obj = new JSONObject();
	JSONArray nodos = new JSONArray();
	JSONArray tipoN = new JSONArray();
	JSONArray activi = new JSONArray();
	JSONArray itera = new JSONArray();
	JSONArray probabi = new JSONArray();
	JSONObject restric = new JSONObject();
	
		
	public Proceso(int n, double r) { // Se le entrega el número de actividades/servicios del proceso
		long time_start;
		long time_end;
		int cont = 0;
		nombre = "Proceso";
		texto = new String[n+1];
		nNodos = new int[n];
		tipoNodo = new int[n];
		activ = new String[n];
		iter = new int[n];
		probab = new double[n];
		act = n;
		time_start = System.currentTimeMillis();
		obj.put(nombre, time_start);
		
		
		while(n>=1){
			
			int max = n;  // número máximo de nodos en un switch o paralelo
			int tipo;
			
			if(n<act && (tipoNodo[cont-1]==0 || tipoNodo[cont-1] == 1) && max>2){ //Siempre se comienza con una actividad/servicio texto[cont-1].indexOf("oo")>0
				tipo = (int) (Math.random() * 2)+1 ;
			} else {
				tipo = 0 ;
			}
			
			if(tipo==0){
			// secuencia, puede ser con loop
				
				double prob = Math.random();
				texto[cont]= "Nodo: "+ cont + "; ";
				nNodos[cont] = cont;
				
				
				if( prob>0.25 ){ // si no tiene loop
					texto[cont] = texto[cont] + "Tipo: NoLoop;";
					tipoNodo[cont] = 1;
					iter[cont] = 0;
					probab[cont] = 0;
					activ[cont] = "serv"+cont;
				} else { // si tiene loop
					int numero = (int) (Math.random() * 4) + 2;//iteraciones deben ser mayores o iguales a 2 y menores o iguales a 5
					texto[cont] = texto[cont] + "Tipo: Loop; ";
					texto[cont] = texto[cont] + "Loops: " + numero + ";";
					tipoNodo[cont] = 2;
					iter[cont] = numero;
					probab[cont] = 0;
					activ[cont] = "serv"+cont;
				}
				System.out.println(texto[cont]);
				nodos.add(nNodos[cont]);
				tipoN.add(tipoNodo[cont]);
				activi.add(activ[cont]);
				itera.add(iter[cont]);
				probabi.add(probab[cont]);
				
				
				n--;
				cont++;
			}
				
			if(tipo==1){ // paralelo, puede tener entre 2 y 5 ramas
				
				int numero = 0;
				
				
				if(max>=5){
					numero = (int) (Math.random() * 4 ) + 2;
					for(int i=0; i < numero ;i++){ // Relleno los nodos
						texto[cont]= "Nodo: " + cont + "; ";
						nNodos[cont] = cont;
						texto[cont] = texto[cont] + "Tipo: Paralelo; ";
						tipoNodo[cont] = 3;
						iter[cont] = 0;
						probab[cont] = 0;
						activ[cont] = "serv"+cont;
						System.out.println(texto[cont]);
						nodos.add(nNodos[cont]);
						tipoN.add(tipoNodo[cont]);
						activi.add(activ[cont]);
						itera.add(iter[cont]);
						probabi.add(probab[cont]);
						n--;
						cont++;
					}
				} else {
					numero = (int) (Math.random() * max-1) + 2;
					for(int i=0;i < numero ;i++){ // Relleno los nodos
						texto[cont]= "Nodo: " + cont + "; ";
						nNodos[cont] = cont;
						texto[cont] = texto[cont] + "Tipo: Paralelo; ";
						tipoNodo[cont] = 3;
						iter[cont] = 0;
						probab[cont] = 0;
						activ[cont] = "serv"+cont;
						System.out.println(texto[cont]);
						nodos.add(nNodos[cont]);
						tipoN.add(tipoNodo[cont]);
						activi.add(activ[cont]);
						itera.add(iter[cont]);
						probabi.add(probab[cont]);
						n--;
						cont++;
					}
				}
			}
				
			if(tipo==2){ // switch, puede tener entre 2 y 5 ramas 
				
				if(max>=5){
					int numero = (int) (Math.random() * 4 ) + 2; // número de ramas
					double prob=0;
					for(int i=0; i < numero ;i++){ // Relleno los nodos
						texto[cont]= "Nodo: "+ cont + "; ";
						nNodos[cont] = cont;
						texto[cont] = texto[cont] + "Tipo: Switch; ";
						tipoNodo[cont] = 4;
						iter[cont] = 0;
						
						//Definir la probabilidad de la rama
						
						if(i==numero-1){
							double y = 0;
							y = 1-prob;
							texto[cont] = texto[cont] + "Prob: " + y + "; ";
							probab[cont] = y;
							activ[cont] = "serv"+cont;
							System.out.println(texto[cont]);
							nodos.add(nNodos[cont]);
							tipoN.add(tipoNodo[cont]);
							activi.add(activ[cont]);
							itera.add(iter[cont]);
							probabi.add(probab[cont]);
							prob+=y;
						}else{
							double media = (double) 1/(numero+1);
							double sd = (double) 1/(2*numero);
							double x = Math.random();
							double y = x*sd + media;
							texto[cont] = texto[cont] + "Prob: " + y + "; ";
							probab[cont] = y;
							activ[cont] = "serv"+cont;
							prob+=y;
							System.out.println(texto[cont]);
							nodos.add(nNodos[cont]);
							tipoN.add(tipoNodo[cont]);
							activi.add(activ[cont]);
							itera.add(iter[cont]);
							probabi.add(probab[cont]);
						}
						
						n--;
						cont++;
					} // Cierra for
				} else {
					int numero = (int) (Math.random() * (max-1)) + 2;
					double prob=0;
					for(int i=0;i < numero;i++){ // Relleno los nodos
						texto[cont]= "Nodo: "+ cont + "; ";
						nNodos[cont] = cont;
						texto[cont] = texto[cont] + "Tipo: Switch; ";
						tipoNodo[cont] = 4;
						iter[cont] = 0;
						nodos.add(nNodos[cont]);
						
						if(i==numero-1){
							double y = 0;
							y = 1-prob;
							texto[cont] = texto[cont] + "Prob: " + y + "; ";
							probab[cont] = y;
							activ[cont] = "serv"+cont;
							System.out.println(texto[cont]);
							nodos.add(nNodos[cont]);
							tipoN.add(tipoNodo[cont]);
							activi.add(activ[cont]);
							itera.add(iter[cont]);
							probabi.add(probab[cont]);
							prob+=y;
						}else{
							double media = (double) 1/(numero+1);
							double sd = (double) 1/(2*numero);
							double x = Math.random();
							double y = x*sd + media;
							texto[cont] = texto[cont] + "Prob: " + y + "; ";
							probab[cont] = y;
							activ[cont] = "serv"+cont;
							System.out.println(texto[cont]);
							nodos.add(nNodos[cont]);
							tipoN.add(tipoNodo[cont]);
							activi.add(activ[cont]);
							itera.add(iter[cont]);
							probabi.add(probab[cont]);
							prob+=y;
						}
						
						n--;
						cont++;
					}// Cierra for
					
				}
			}
		} // Cierra while
		// Agrego restricciones globales al archivo
		obj.put("Nodos", nodos);
		obj.put("Tipo", tipoN);
		obj.put("Iteraciones", itera);
		obj.put("Probabilidades", probabi);
		obj.put("Actividad", activi);
		obj.put("NNodos", act);
		
		setRestriccion(r); // Se deberían setear otras condiciones
		double[] globales;
		globales = restricGlobal(this, r);
		
		for(int i=0;i<restricciones.length;i++){
			restric.put(restricciones[i][0], globales[i]);
		}
		obj.put("Restricciones",restric);
		
		try {

			FileWriter file = new FileWriter("C:\\Users\\Camilo\\Desktop\\Eclipse\\JSON\\procesos\\proceso"+time_start+".json");
			file.write(obj.toJSONString());
			file.flush();
			file.close();
			nombre = "proceso"+time_start;
		} catch (IOException e) {
			//manejar error
		}
		time_end = System.currentTimeMillis();
		System.out.println("Tiempo de generación del proceso: "+ ( time_end - time_start ) +" milisegundos.");
		
		
		
		
	}// Cierra constructor
	
	public void setRestriccion(double n){
		if(n==0.3){
			restricciones[0][0]="tiempo";
			restricciones[0][1]="0.3";
			restricciones[1][0]="dispo";
			restricciones[1][1]="0.3";
			restricciones[2][0]="through";
			restricciones[2][1]="0.3";
			restricciones[3][0]="exito";
			restricciones[3][1]="0.3";
			restricciones[4][0]="confiab";
			restricciones[4][1]="0.3";
			restricciones[5][0]="confor";
			restricciones[5][1]="0.3";
			restricciones[6][0]="mejorespr";
			restricciones[6][1]="0.3";
			restricciones[7][0]="latencia";
			restricciones[7][1]="0.3";
			restricciones[8][0]="documentacion";
			restricciones[8][1]="0.3";
			restricciones[9][0]="presupuesto";
		}
		
		if(n==0.5){
			
			restricciones[0][0]="tiempo";
			restricciones[0][1]="0.5";
			restricciones[1][0]="dispo";
			restricciones[1][1]="0.50";
			restricciones[2][0]="through";
			restricciones[2][1]="0.5";
			restricciones[3][0]="exito";
			restricciones[3][1]="0.5";
			restricciones[4][0]="confiab";
			restricciones[4][1]="0.5";
			restricciones[5][0]="confor";
			restricciones[5][1]="0.5";
			restricciones[6][0]="mejorespr";
			restricciones[6][1]="0.5";
			restricciones[7][0]="latencia";
			restricciones[7][1]="0.5";
			restricciones[8][0]="documentacion";
			restricciones[8][1]="0.5";
			restricciones[9][0]="presupuesto";
		}
		
		if(n==0.7){
			restricciones[0][0]="tiempo";
			restricciones[0][1]="0.7";
			restricciones[1][0]="dispo";
			restricciones[1][1]="0.7";
			restricciones[2][0]="through";
			restricciones[2][1]="0.7";
			restricciones[3][0]="exito";
			restricciones[3][1]="0.7";
			restricciones[4][0]="confiab";
			restricciones[4][1]="0.7";
			restricciones[5][0]="confor";
			restricciones[5][1]="0.7";
			restricciones[6][0]="mejorespr";
			restricciones[6][1]="0.7";
			restricciones[7][0]="latencia";
			restricciones[7][1]="0.7";
			restricciones[8][0]="documentacion";
			restricciones[8][1]="0.7";
			restricciones[9][0]="presupuesto";
		}
	}
	
	public String[][] getRestriccion(){
		return restricciones;
	}
	
	public int getN(){ // Obtener el número de actividades del proceso
		return act;
	}
	public String[] getDesc(){
		return texto;
	}
	public int getSec(Proceso p){
		String[] desc = p.getDesc();
		int cont=0;
		for(int i=0; i< desc.length ;i++){
			if(desc[i].indexOf("NoLoop")>0){
				cont++;
			}
		}
		return cont;
	}
	public double getPresupuesto(double r){
		if(r== 0.3){
			presupuesto = (2.4)*act;
		}
		
		if(r== 0.5){
			presupuesto = (2.1)*act;
		}
		
		if(r== 0.7){
			presupuesto = (1.9)*act;
		}
		return presupuesto;
	}
	public String getName(){
		return nombre;
	}
	public int[] getNNodo(){ // Devuelve un arreglo desde 0 a n. Puede ser útil
		return nNodos;
	}
	public int[] getTipoNodo(){ // Devuelve un arreglo con el tipo de nodos
		return tipoNodo;
	}
	public String[] getActiv(){ // Devuelve un arreglo con las actividades
		return activ;
	}
	public double[] getProbab(){ // Devuelve un arreglo con la probabilidad de pasar por ese nodo, cambia sólo si es de tipo 4 (switch)
		return probab;
	}
	public int[] getIter(){ // Devuelve el numero de iteraciones de los nodos, cmabia sólo si es de secuencia
		return iter;
	}
	public double[] restricGlobal(Proceso p, double r){ 
		
		double[] restricGlobal = new double[10];
		restricGlobal[1] = 1;
		restricGlobal[3] = 1;
		restricGlobal[4] = 1;
		restricGlobal[5] = 1;
		restricGlobal[6] = 1;
		restricGlobal[8] = 1;
		restricGlobal[9] = p.getPresupuesto(r);
		int[] tipoNodo = p.getTipoNodo();
		int[] iter = p.getIter();
		String[][] restric = p.getRestriccion();
		int cont = 0; 
		while(cont<tipoNodo.length){ // Caso en que se fija un valor para todos los nodos (una especie de restriccion global basada en un valor ideal para todas las actividades)
		//for( int i = 0 ; i < tipoNodo.length ; i++ ){
			
			switch(tipoNodo[cont]){
			
			case 1:
				restricGlobal[0]+=Double.parseDouble(restric[0][1]);
				restricGlobal[1]*=Double.parseDouble(restric[1][1]);
				restricGlobal[2]+=Double.parseDouble(restric[2][1]);
				restricGlobal[3]*=Double.parseDouble(restric[3][1]);
				restricGlobal[4]*=Double.parseDouble(restric[4][1]);
				restricGlobal[5]*=Double.parseDouble(restric[5][1]);
				restricGlobal[6]*=Double.parseDouble(restric[6][1]);
				restricGlobal[7]+=Double.parseDouble(restric[7][1]);
				restricGlobal[8]*=Double.parseDouble(restric[8][1]);
				cont++;
				break;
			case 2:
				restricGlobal[0]+= (iter[cont]*Double.parseDouble(restric[0][1]));
				restricGlobal[1]*=(Math.pow(Double.parseDouble(restric[1][1]), iter[cont]));
				restricGlobal[2]+=(iter[cont]*Double.parseDouble(restric[2][1]));
				restricGlobal[3]*=(Math.pow(Double.parseDouble(restric[3][1]), iter[cont]));
				restricGlobal[4]*=(Math.pow(Double.parseDouble(restric[4][1]), iter[cont]));
				restricGlobal[5]*=(Math.pow(Double.parseDouble(restric[5][1]), iter[cont]));
				restricGlobal[6]*=(Math.pow(Double.parseDouble(restric[6][1]), iter[cont]));
				restricGlobal[7]+=(iter[cont]*Double.parseDouble(restric[7][1]));
				restricGlobal[8]*=(Math.pow(Double.parseDouble(restric[8][1]), iter[cont]));
				cont++;
				break;
			case 3:
				int cont2 = 0;
				boolean check = true;
				while(check){
					cont2++;
					cont++;
					int suma = cont+cont2;
					if(suma>tipoNodo.length){
						check = false;
						cont++;
					}else{
						if(suma>=act){
							check = false;	
							cont++;
						}else{
							 if(tipoNodo[suma]!=3){
								 check = false;
								 cont++;
							 }
						}
					}
				}// cierra el while
				
				restricGlobal[0]+= Double.parseDouble(restric[0][1]);// Maximo
				restricGlobal[1]*=(Math.pow(Double.parseDouble(restric[1][1]), cont2+1));	
				restricGlobal[2]+=(Double.parseDouble(restric[2][1])); // minimo
				restricGlobal[3]*=(Math.pow(Double.parseDouble(restric[3][1]), cont2+1));
				restricGlobal[4]*=(Math.pow(Double.parseDouble(restric[4][1]), cont2+1));
				restricGlobal[5]*=(Math.pow(Double.parseDouble(restric[5][1]), cont2+1));
				restricGlobal[6]*=(Math.pow(Double.parseDouble(restric[6][1]), cont2+1));
				restricGlobal[7]+=(Double.parseDouble(restric[7][1])); // maximo
				restricGlobal[8]*=(Math.pow(Double.parseDouble(restric[8][1]), cont2+1));
				
				break;
			case 4:
				if(cont+1>=act){
					restricGlobal[0]+=Double.parseDouble(restric[0][1]);
					restricGlobal[1]*=Double.parseDouble(restric[1][1]);
					restricGlobal[2]+=Double.parseDouble(restric[2][1]);
					restricGlobal[3]*=Double.parseDouble(restric[3][1]);
					restricGlobal[4]*=Double.parseDouble(restric[4][1]);
					restricGlobal[5]*=Double.parseDouble(restric[5][1]);
					restricGlobal[6]*=Double.parseDouble(restric[6][1]);
					restricGlobal[7]+=Double.parseDouble(restric[7][1]);
					restricGlobal[8]*=Double.parseDouble(restric[8][1]);
					cont++;
					break;
					
				}else if(tipoNodo[cont+1] != 4){
				
				restricGlobal[0]+=Double.parseDouble(restric[0][1]);
				restricGlobal[1]*=Double.parseDouble(restric[1][1]);
				restricGlobal[2]+=Double.parseDouble(restric[2][1]);
				restricGlobal[3]*=Double.parseDouble(restric[3][1]);
				restricGlobal[4]*=Double.parseDouble(restric[4][1]);
				restricGlobal[5]*=Double.parseDouble(restric[5][1]);
				restricGlobal[6]*=Double.parseDouble(restric[6][1]);
				restricGlobal[7]+=Double.parseDouble(restric[7][1]);
				restricGlobal[8]*=Double.parseDouble(restric[8][1]);
				}
				cont++;
				break;
			}
			
			
		//}
		}
		System.out.println("Las restricciones globales son: ");
		for(int i  = 0;i < restricGlobal.length;i++){
			System.out.println(restricGlobal[i]);
		}
		
		return restricGlobal;
		
	}
	
}
