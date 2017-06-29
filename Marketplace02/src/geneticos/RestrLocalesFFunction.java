package geneticos;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.jgap.FitnessFunction;
import org.jgap.Gene;
import org.jgap.IChromosome;

import funciones.FuncionUtilidad;

public class RestrLocalesFFunction extends FitnessFunction{

	private static final long serialVersionUID = -5484443467776976033L;
	
	private int nroGenes;
	int nServ;
	private String[][] atrib;
	private String[] tipo;
	private double[] param;
	//private double[] restricciones;
	private double[] util;
	private int[] nServN;
	private int[] tipoNodo;
	private int[] iter; 
	private double[] prob; 
	private double[] restr; // restricciones globales
	
	
	public RestrLocalesFFunction(int i_nroServ, String[][] atrib, double[] param, double[] util, int[] tipoNodo, int[] iter, double[] prob, double[] restr){ // Número de actividades en un proceso por el número de atributos de calidad
		nServ = i_nroServ;
		//nroGenes = i_nroServ*9 ; 
		this.setAtrib(atrib);
		this.param=param;
		this.util = util;
		this.tipoNodo = tipoNodo;
		this.iter = iter;
		this.prob = prob;
		this.restr = restr;
		
	}

	
	
	@Override
	protected double evaluate(IChromosome cromosoma) {
		//System.out.println("1. Entra a evaluar el cromosoma");
		Gene[] gene = cromosoma.getGenes();
		double[] alelos = new double[gene.length];
		int largo = (alelos.length/9);
		double[][] opera = new double[largo][9];
		
		for(int i = 0;i < gene.length;i++){
			alelos[i] =(double) gene[i].getAllele();
		}
		
		for(int i = 0;i < opera.length;i++){
			for(int j = 0;j < opera[0].length;j++){
				//System.out.println(i*9+j);
				opera[i][j] = alelos[i*9+j];
			}
		}
		this.saveNServ();
		
		double[][] probabilidad = getProbabilidad(opera);
		double[][] puntaje = getPuntaje(opera, probabilidad);
		double[] utilidad = getUtilidad3(puntaje);
		
		double ajuste = 0;
		for(int i = 0;i < utilidad.length;i++){
			ajuste += utilidad[i]*param[i];
		}
		
		//System.out.println("Se calcula el ajuste "+ajuste);
		
		double penalty = calcPenalty(opera);
		
		ajuste -= penalty;
		//System.out.println("El ajuste de este cromosoma es de: "+ajuste);
		return ajuste;
		
	} // Cierra método evaluate 

	public double getUtilidad(int serv){
		double utilidad = 0;
		utilidad = util[serv];
		//System.out.println("La utilidad es :"+utilidad );
		return utilidad;
	}
	
	public double getUtilidad2(int a, String serv, double[] alelo){
		List utilidades = new ArrayList();
		double umax = -9999.0;
		for(int i = 0;i<util.length;i++){
			if(tipo[i].equals(serv)){
				if(Double.parseDouble(atrib[i][0])>alelo[a*9+0]){
				if(Double.parseDouble(atrib[i][1])>alelo[a*9+1]){
				if(Double.parseDouble(atrib[i][2])>alelo[a*9+2]){
				if(Double.parseDouble(atrib[i][3])>alelo[a*9+3]){
				if(Double.parseDouble(atrib[i][4])>alelo[a*9+4]){
				if(Double.parseDouble(atrib[i][5])>alelo[a*9+5]){
				if(Double.parseDouble(atrib[i][6])>alelo[a*9+6]){
				if(Double.parseDouble(atrib[i][7])>alelo[a*9+7]){
				if(Double.parseDouble(atrib[i][8])>alelo[a*9+8]){
					utilidades.add(getUtilidad(i));			// si pasa todas las restricciones
				}
				}
				}
				}
				}
				}
				}
				}
				}
				
				Iterator iter = utilidades.iterator();
				while(iter.hasNext()){
					double num = (double) iter.next();
					if(num> umax){
						umax=num;
					}
				}
				
			}
		}
		
		
		return umax;
	}
	
	public double getUMax(String serv){
		List utilidades = new ArrayList();
		
		for(int i = 0;i<util.length;i++){
			if(tipo[i].equals(serv)){
				utilidades.add(getUtilidad(i));
			}
		}
		double umax = -9999.0;
		Iterator iUtil = utilidades.iterator();
		while(iUtil.hasNext()){
			double num = (double) iUtil.next();
			if(num> umax){
				umax=num;
			}
		}
		//System.out.println("La utilidad máxima para el servicio "+serv +" es " +umax);
		return umax;
	}
	
	
	public int getNoRestringidos(int pos, String serv, double alelo){ // nro de servicios que cumplen con la restricción.
		int noRestr = 0;
		for(int i =0;i<atrib.length;i++){ //reviso todos los servicios en atrib		
			if(tipo[i].equals(serv) && Double.parseDouble(atrib[i][pos])<alelo){
				//System.out.println("Se revisa si el "+ tipo[i]+" es igual a " +serv+ " y "+atrib[i][pos]+" cumple con la restriccion "+ alelo + " para el atributo "+ pos );
				noRestr++;
			}
		}
		//if(noRestr == 0) noRestr =1;
		return noRestr;
	}
	
	public double[][] getProbabilidad(double[][] opera){
		double[][] prob = new double[opera.length][opera[0].length];
		for(int i = 0;i < opera.length;i++){
			String s = "serv"+i;
			for(int j = 0;j < opera[0].length;j++){
				prob[i][j] = getNoRestringidos(j, s, opera[i][j]);
				prob[i][j] /= getNServ(s);
			}
		}
		return prob;
	}
	
	public double[][] getPuntaje(double[][] opera, double[][] probabilidad){
		double[][] puntaje = new double[opera.length][opera[0].length];
		for(int i = 0;i < puntaje.length;i++){
			for(int j = 0;j < puntaje[0].length;j++){
				puntaje[i][j] = probabilidad[i][j]*opera[i][j];
			}
		}
		return puntaje;
	}
	
	public double[] getUtilidad3(double[][] puntaje){
		double[] restricGlobal = new double[puntaje[0].length];
		restricGlobal[1]=1.0;
		restricGlobal[5]=1.0;
		restricGlobal[3]=1.0;
		restricGlobal[4]=1.0;
		restricGlobal[6]=1.0;
		restricGlobal[8]=1.0;
		/*int largo = (datos.length/9);
		double[][] data = new double[largo][9];
		int cont = 0;
		for(int i = 0;i < largo;i++){
			for(int j = 0;j < 9;j++){
				data[i][j] = datos[cont];
				cont++;
			}
		}*/
		int tipoAnt = 0; // Para guardar el tipo de nodo de la corrida anterior.
		int inicio4 = 0;
		double minRend = 0.0; // Para guardar el mínimo de rendimiento de un conjunto de nodos
		double maxTpo = 0.0; // Para guardar el máximo de tiempo de respuesta de un conjunto de nodos
		double maxLat = 0.0; // Para guardar el máximo de latencia de un conjunto de nodos
		
		for(int i = 0;i < tipoNodo.length;i++){
			
			switch(tipoNodo[i]){
			case 1: // secuencia
				restricGlobal[0] += puntaje[i][0];
				restricGlobal[1] *= puntaje[i][1];
				restricGlobal[2] += puntaje[i][2];
				restricGlobal[3] *= puntaje[i][3];
				restricGlobal[4] *= puntaje[i][4];
				restricGlobal[5] *= puntaje[i][5];
				restricGlobal[6] *= puntaje[i][6];
				restricGlobal[7] += puntaje[i][7];
				restricGlobal[8] *= puntaje[i][8];
				tipoAnt = 1;
				break;
			case 2: // sec con iter
				restricGlobal[0] += iter[i]*puntaje[i][0];
				restricGlobal[1] *= Math.pow(puntaje[i][1],iter[i]);
				restricGlobal[2] += iter[i]*puntaje[i][2];
				restricGlobal[3] *= Math.pow(puntaje[i][3],iter[i]); 
				restricGlobal[4] *= Math.pow(puntaje[i][4],iter[i]);
				restricGlobal[5] *= Math.pow(puntaje[i][5],iter[i]);
				restricGlobal[6] *= Math.pow(puntaje[i][6],iter[i]);
				restricGlobal[7] += iter[i]*puntaje[i][7];
				restricGlobal[8] *= Math.pow(puntaje[i][8],iter[i]);
				tipoAnt = 2;
				break;
			case 3: // paralelo
				if(tipoAnt == 3){ // seteo lo valores máx y mín
					if(puntaje[i][0] > maxTpo){ maxTpo=puntaje[i][0];}
					if(puntaje[i][2] < minRend){ minRend=puntaje[i][2];}
					if(puntaje[i][7] > maxLat){ maxLat=puntaje[i][7];}
				}else{
					maxLat=puntaje[i][0];
					minRend=puntaje[i][2];
					maxTpo=puntaje[i][7];
				}
				if(i+1<nServ){
				if(tipoNodo[i+1] == 3){ // si el siguiente es del mismo tipo, entonces no hago nada en los valores (0,3 y 5), porque se deberá compara
					//restricGlobal[0] += data[i][0];
					restricGlobal[1] *= puntaje[i][1];
					//restricGlobal[2] += data[i][2];
					restricGlobal[3] *= puntaje[i][3];
					restricGlobal[4] *= puntaje[i][4];
					restricGlobal[5] *= puntaje[i][5];
					restricGlobal[6] *= puntaje[i][6];
					//restricGlobal[7] += data[i][7];					
					restricGlobal[8] *= puntaje[i][8];
				}else{ // sino
					restricGlobal[0] += maxTpo;
					restricGlobal[1] *= puntaje[i][1];
					restricGlobal[2] += minRend;
					restricGlobal[3] *= puntaje[i][3];
					restricGlobal[4] *= puntaje[i][4];
					restricGlobal[5] *= puntaje[i][5];
					restricGlobal[6] *= puntaje[i][6];
					restricGlobal[7] += maxLat;
					restricGlobal[8] *= puntaje[i][8];
				}
				}else{
					restricGlobal[0] += maxTpo;
					restricGlobal[1] *= puntaje[i][1];
					restricGlobal[2] += minRend;
					restricGlobal[3] *= puntaje[i][3];
					restricGlobal[4] *= puntaje[i][4];
					restricGlobal[5] *= puntaje[i][5];
					restricGlobal[6] *= puntaje[i][6];
					restricGlobal[7] += maxLat;
					restricGlobal[8] *= puntaje[i][8];
				}
				tipoAnt = 3;
				break;
			case 4: // branch
				if(tipoAnt != 4){// el primero de tipo 4 marca el inicio de los nodos en branch
					inicio4 = i;
				}
				if((i+1)<5){
				if(tipoNodo[i+1] != 4){// si es el último de tipo 4 en la serie...
					double r1 = 0.0;
					double r3 = 0.0;
					double r4 = 0.0;
					double r5 = 0.0;
					double r6 = 0.0;
					double r8 = 0.0;
					for(int j = 0;j < (i - inicio4 + 1);j++){
						restricGlobal[0] += prob[inicio4+j]*puntaje[inicio4+j][0];
						r1 *= prob[inicio4+j]*puntaje[inicio4+j][1];
						restricGlobal[2] += prob[inicio4+j]*puntaje[inicio4+j][2];
						r3 *= prob[inicio4+j]*puntaje[inicio4+j][3];
						r4 *= prob[inicio4+j]*puntaje[inicio4+j][4];
						r5 *= prob[inicio4+j]*puntaje[inicio4+j][5];
						r6 *= prob[inicio4+j]*puntaje[inicio4+j][6];
						restricGlobal[7] += prob[inicio4+j]*puntaje[inicio4+j][7];
						r8 *= prob[inicio4+j]*puntaje[inicio4+j][8];
					}
					restricGlobal[1] *= r1;
					restricGlobal[3] *= r3;
					restricGlobal[4] *= r4;
					restricGlobal[5] *= r5;
					restricGlobal[6] *= r6;
					restricGlobal[8] *= r8;
				}
				}else{//Si no hay i+1 entonces hago lo mismo
					double r1 = 0.0;
					double r3 = 0.0;
					double r4 = 0.0;
					double r5 = 0.0;
					double r6 = 0.0;
					double r8 = 0.0;
					for(int j = 0;j < (i - inicio4 + 1);j++){
						restricGlobal[0] += prob[inicio4+j]*puntaje[inicio4+j][0];
						r1 *= prob[inicio4+j]*puntaje[inicio4+j][1];
						restricGlobal[2] += prob[inicio4+j]*puntaje[inicio4+j][2];
						r3 *= prob[inicio4+j]*puntaje[inicio4+j][3];
						r4 *= prob[inicio4+j]*puntaje[inicio4+j][4];
						r5 *= prob[inicio4+j]*puntaje[inicio4+j][5];
						r6 *= prob[inicio4+j]*puntaje[inicio4+j][6];
						restricGlobal[7] += prob[inicio4+j]*puntaje[inicio4+j][7];
						r8 *= prob[inicio4+j]*puntaje[inicio4+j][8];
					}
					restricGlobal[1] *= r1;
					restricGlobal[3] *= r3;
					restricGlobal[4] *= r4;
					restricGlobal[5] *= r5;
					restricGlobal[6] *= r6;
					restricGlobal[8] *= r8;
				}
				tipoAnt = 4;
				break;
			}	
		}
		return restricGlobal;
	}
	
	public int getNoRestringidos(int serv, double[] alelo){ // serv indica el número del servicio que se está revisando
		int n = 0;
		for(int i  = 0;i < atrib.length;i++){
			if(tipo[i].equals(serv)){
				if(Double.parseDouble(atrib[i][0])<alelo[serv*9+0]){
				if(Double.parseDouble(atrib[i][1])<alelo[serv*9+1]){
				if(Double.parseDouble(atrib[i][2])<alelo[serv*9+2]){
				if(Double.parseDouble(atrib[i][3])<alelo[serv*9+3]){
				if(Double.parseDouble(atrib[i][4])<alelo[serv*9+4]){
				if(Double.parseDouble(atrib[i][5])<alelo[serv*9+5]){
				if(Double.parseDouble(atrib[i][6])<alelo[serv*9+6]){
				if(Double.parseDouble(atrib[i][7])<alelo[serv*9+7]){
				if(Double.parseDouble(atrib[i][8])<alelo[serv*9+8]){
					n++;			// si pasa todas las restricciones, entonces se suma 1 a los serv no restringidos
				}
				}
				}
				}
				}
				}
				}
				}
				}
			}
		}
		return n;
	}
	public void saveNServ(){
		nServN = new int[nServ];
		for(int j = 0;j < nServN.length;j++){
			String serv = "serv"+j;
			int n = 0;
			for(int i = 0;i<tipo.length;i++){
				if(tipo[i].equals(serv)){
					n++;
				}
			}
			nServN[j] = n;
		}
	}
	public int getNServ(String serv){
		String[] s = serv.split("v");
		int n = Integer.parseInt(s[1]);
		
		//System.out.println("El número de servicios es :"+nServN[n]);
		if(nServN[n] == 0) nServN[n] =1;
		return nServN[n];
	}
	
	public void setAtrib(String[][] atributos){
		atrib = new String[atributos.length][atributos[0].length-1];
		tipo = new String[atributos.length];
		
		for(int i = 0;i < atrib.length;i++){
			tipo[i]=atributos[i][0];
			for(int j = 1;j<atributos[0].length;j++){
				atrib[i][j-1]=atributos[i][j];
			}
		}
	}
	
	
	/*public void setRestricciones(double[] restr){
		restricciones = restr;
	}*/
	public void setParam(double[] parametrosFU){
		param = parametrosFU;
	}
	
	public double calcPenalty(double[][] opera){
		double resultado = 0.0;
		//double wNeg = 1.0;
		double wGlob = 3.0;

		//double neg = chequeaNeg(opera);
		//resultado+=(wNeg*neg);
		 // restricciones globales que se deben cumplir
		double g = chequeaGlobal(opera, tipoNodo, iter, restr, prob);
		resultado+=(wGlob*g);
			
		return resultado;
	}
	
	public double chequeaNeg(double[][] datos){ //chequeo negatividad, por construcción no debiesen haber datos negativos.
		int cont = 0;
		for(int i = 0;i < datos.length; i++){
			for(int j = 0;j < datos[0].length;j++){
				if(datos[i][j] < 0){
					cont++;
				}
			}
		}
		int divisor = (datos.length*datos[0].length);
		//System.out.println("los número negativos son: " +cont);
		cont /= divisor;

		return cont;
	}
	
	public double chequeaGlobal(double[][] data, int[] tipoNodo, int[] iter, double[] restr, double[] prob){ 
		int g = 0;
		double[] restricGlobal = new double[9];
		restricGlobal[1]=1.0;
		restricGlobal[3]=1.0;
		restricGlobal[4]=1.0;
		restricGlobal[5]=1.0;
		restricGlobal[6]=1.0;
		restricGlobal[8]=1.0;
		int tipoAnt = 0; // Para guardar el tipo de nodo de la corrida anterior.
		int inicio4 = 0;
		double minRend = 0.0; // Para guardar el mínimo de rendimiento de un conjunto de nodos
		double maxTpo = 0.0; // Para guardar el máximo de tiempo de respuesta de un conjunto de nodos
		double maxLat = 0.0; // Para guardar el máximo de latencia de un conjunto de nodos
		
		for(int i = 0;i < tipoNodo.length;i++){
			
			switch(tipoNodo[i]){
			case 1: // secuencia
				restricGlobal[0] += data[i][0];
				restricGlobal[1] *= data[i][1];
				restricGlobal[2] += data[i][2];
				restricGlobal[3] *= data[i][3];
				restricGlobal[4] *= data[i][4];
				restricGlobal[5] *= data[i][5];
				restricGlobal[6] *= data[i][6];
				restricGlobal[7] += data[i][7];
				restricGlobal[8] *= data[i][8];
				tipoAnt = 1;
				break;
			case 2: // sec con iter
				restricGlobal[0] += iter[i]*data[i][0];
				restricGlobal[1] *= Math.pow(data[i][1],iter[i]);
				restricGlobal[2] += iter[i]*data[i][2];
				restricGlobal[3] *= Math.pow(data[i][3],iter[i]); 
				restricGlobal[4] *= Math.pow(data[i][4],iter[i]);
				restricGlobal[5] *= Math.pow(data[i][5],iter[i]);
				restricGlobal[6] *= Math.pow(data[i][6],iter[i]);
				restricGlobal[7] += iter[i]*data[i][7];
				restricGlobal[8] *= Math.pow(data[i][8],iter[i]);
				tipoAnt = 2;
				break;
			case 3: // paralelo
				if(tipoAnt == 3){ // seteo lo valores máx y mín
					if(data[i][0] > maxTpo){ maxTpo=data[i][0];}
					if(data[i][2] < minRend){ minRend=data[i][2];}
					if(data[i][7] > maxLat){ maxLat=data[i][7];}
				}else{
					maxLat=data[i][7];
					minRend=data[i][2];
					maxTpo=data[i][0];
					
				}
				if(i+1<nServ){
				if(tipoNodo[i+1] == 3){ // si el siguiente es del mismo tipo, entonces no hago nada en los valores (0,3 y 5), porque se deberá compara
					//restricGlobal[0] += data[i][0];
					restricGlobal[1] *= data[i][1];
					//restricGlobal[2] += data[i][2];
					restricGlobal[3] *= data[i][3];
					restricGlobal[4] *= data[i][4];
					restricGlobal[5] *= data[i][5];
					restricGlobal[6] *= data[i][6];
					//restricGlobal[7] += data[i][7];					
					restricGlobal[8] *= data[i][8];
				}else{ // sino
					restricGlobal[0] += maxTpo;
					restricGlobal[1] *= data[i][1];
					restricGlobal[2] += minRend;
					restricGlobal[3] *= data[i][3];
					restricGlobal[4] *= data[i][4];
					restricGlobal[5] *= data[i][5];
					restricGlobal[6] *= data[i][6];
					restricGlobal[7] += maxLat;
					restricGlobal[8] *= data[i][8];
				}
				}else{
					restricGlobal[0] += maxTpo;
					restricGlobal[1] *= data[i][1];
					restricGlobal[2] += minRend;
					restricGlobal[3] *= data[i][3];
					restricGlobal[4] *= data[i][4];
					restricGlobal[5] *= data[i][5];
					restricGlobal[6] *= data[i][6];
					restricGlobal[7] += maxLat;
					restricGlobal[8] *= data[i][8];
				}
				
				tipoAnt = 3;
				break;
			case 4: // branch
				if(tipoAnt != 4){// el primero de tipo 4 marca el inicio de los nodos en branch
					inicio4 = i;
				}
				if((i+1)<5){
				if(tipoNodo[i+1] != 4){// si es el último de tipo 4 en la serie...
					double r1 = 0.0;
					double r3 = 0.0;
					double r4 = 0.0;
					double r5 = 0.0;
					double r6 = 0.0;
					double r8 = 0.0;
					for(int j = 0;j < (i - inicio4 + 1);j++){
						restricGlobal[0] += prob[inicio4+j]*data[inicio4+j][0];
						r1 *= prob[inicio4+j]*data[inicio4+j][1];
						restricGlobal[2] += prob[inicio4+j]*data[inicio4+j][2];
						r3 *= prob[inicio4+j]*data[inicio4+j][3];
						r4 *= prob[inicio4+j]*data[inicio4+j][4];
						r5 *= prob[inicio4+j]*data[inicio4+j][5];
						r6 *= prob[inicio4+j]*data[inicio4+j][6];
						restricGlobal[7] += prob[inicio4+j]*data[inicio4+j][7];
						r8 *= prob[inicio4+j]*data[inicio4+j][8];
					}
					restricGlobal[1] *= r1;
					restricGlobal[3] *= r3;
					restricGlobal[4] *= r4;
					restricGlobal[5] *= r5;
					restricGlobal[6] *= r6;
					restricGlobal[8] *= r8;
				}
				}else{//Si no hay i+1 entonces hago lo mismo
					double r1 = 0.0;
					double r3 = 0.0;
					double r4 = 0.0;
					double r5 = 0.0;
					double r6 = 0.0;
					double r8 = 0.0;
					for(int j = 0;j < (i - inicio4 + 1);j++){
						restricGlobal[0] += prob[inicio4+j]*data[inicio4+j][0];
						r1 *= prob[inicio4+j]*data[inicio4+j][1];
						restricGlobal[2] += prob[inicio4+j]*data[inicio4+j][2];
						r3 *= prob[inicio4+j]*data[inicio4+j][3];
						r4 *= prob[inicio4+j]*data[inicio4+j][4];
						r5 *= prob[inicio4+j]*data[inicio4+j][5];
						r6 *= prob[inicio4+j]*data[inicio4+j][6];
						restricGlobal[7] += prob[inicio4+j]*data[inicio4+j][7];
						r8 *= prob[inicio4+j]*data[inicio4+j][8];
					}
					restricGlobal[1] *= r1;
					restricGlobal[3] *= r3;
					restricGlobal[4] *= r4;
					restricGlobal[5] *= r5;
					restricGlobal[6] *= r6;
					restricGlobal[8] *= r8;
				}
				tipoAnt = 4;
				break;
			}
			
				
		}
		if(restricGlobal[0]<restr[0]){g++;}
		if(restricGlobal[1]<restr[1]){g++;}
		if(restricGlobal[2]<restr[2]){g++;}
		if(restricGlobal[3]<restr[3]){g++;}
		if(restricGlobal[4]<restr[4]){g++;}
		if(restricGlobal[5]<restr[5]){g++;}
		if(restricGlobal[6]<restr[6]){g++;}
		if(restricGlobal[7]<restr[7]){g++;}
		if(restricGlobal[8]<restr[8]){g++;}
		
		g /= 9;
		return g;
	}

} // Cierra clase
