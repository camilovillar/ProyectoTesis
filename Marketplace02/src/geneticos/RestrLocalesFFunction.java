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
	private double[] param;
	private double[] restricciones;
	private double[] util;
	
	
	public RestrLocalesFFunction(int i_nroServ, String[][] atrib, double[] param, double[] util){ // Número de actividades en un proceso por el número de atributos de calidad
		nServ = i_nroServ;
		//nroGenes = i_nroServ*9 ; 
		this.setAtrib(atrib);
		this.param=param;
		this.util = util;
		
	}

	
	
	@Override
	protected double evaluate(IChromosome cromosoma) {
		
		Gene[] gene = cromosoma.getGenes();
		double[] alelos = new double[gene.length];
		double[] opera = new double[gene.length];
		double resultado = 1.0;
		
		for(int i=0; i<gene.length;i++){
			alelos[i]=(double) gene[i].getAllele();
			System.out.println("Se rescata el valor del alelo "+ i +" que es " + alelos[i]);
		}
		
		int cont = 0;
		for(int i = 0;i<opera.length;i++){
			String act = "serv"+cont;
			opera[i]= 0;
			opera[i] = (getNoRestringidos(i, act,alelos[i]));
			opera[i] /= (getNServ(act));
			opera[i] *= (getUtilidad(cont, alelos));
			opera[i] /= (getUMax(act));
			if((i+1)%9 == 0){
				cont++;
			}
		}
		
		for(int i = 0;i<opera.length;i++){
			resultado = 1;
			resultado*=opera[i];
		}
		
		System.out.println("El ajuste de este cromosoma es de: "+resultado);
		return resultado;
		
		
		
	} // Cierra método evaluate 
	public double evaluate(IChromosome cromosoma, int[] tipoNodo, double[] restr, int[] iter, double[] prob){
		double resultado = 1.0;
		resultado = evaluate(cromosoma);
		
		// Agregar penalidad
		double penalty = calcPenalty(cromosoma, tipoNodo, iter, prob, restr);
		resultado -=penalty;
		System.out.println("La función de ajuste para este cromosoma es de " + resultado);
		return resultado;
		
		
	}
	
	public double getUtilidad(int serv, double[] alelos){
		double utilidad = 0;
		for(int i = 0;i< param.length;i++){
			utilidad += param[i]*alelos[serv*9+i];
			
		}
		System.out.println("La utilidad alelos es :"+utilidad );
		return utilidad;
	}
	public double getUtilidad(int serv){
		double utilidad = 0;
		utilidad = util[serv];
		//System.out.println("La utilidad es :"+utilidad );
		return utilidad;
	}
	
	public double getUMax(String serv){
		List utilidades = new ArrayList();
		for(int i = 0;i<util.length;i++){
			utilidades.add(getUtilidad(i));
		}
		double umax = -9999.0;
		Iterator iUtil = utilidades.iterator();
		while(iUtil.hasNext()){
			double num = (double) iUtil.next();
			if(num> umax){
				umax=num;
			}
		}
		System.out.println("La utilidad máxima para el servicio "+serv +" es " +umax);
		return umax;
	}
	
	public int getNoRestringidos(int pos, String serv, double alelo){ // nro de servicios que cumplen con la restricción.
		int noRestr = 0;
		if(pos == 0 || pos == 3){ // latencia y tiempo de ejec
		for(int i =0;i<atrib.length;i++){				
			if(atrib[i][0].equals(serv) && Double.parseDouble(atrib[i][pos+1])<alelo){
				System.out.println("Se revisa si el "+ atrib[i][0]+" es igual a " +serv+ " y cumple con la restriccion "+ alelo + " para el atributo "+ pos );
				noRestr++;	
			}
		}
		}else{
			for(int i =0;i<atrib.length;i++){
				if(atrib[i][9].equals(serv) && Double.parseDouble(atrib[i][pos])>alelo){
					noRestr++;	
				}
			}			
		}
		System.out.println("Los servicios no restringidos son :"+noRestr );
		if(noRestr == 0) noRestr =1;
		return noRestr;
	}
	
	public int getNServ(String serv){
		int n = 0;
		
		if(atrib[0].equals(serv)){
			n++;
		}
		System.out.println("El número de servicios es :"+n );
		if(n == 0) n =1;
		return n;
	}
	
	public void setAtrib(String[][] atributos){
		atrib = new String[atributos.length][atributos[0].length];
		for(int i = 0;i < atributos.length;i++){
			for(int j = 0;j<atributos[0].length;j++){
				atrib[i][j]=atributos[i][j];
			}
		}
	}
	
	
	public void setRestricciones(double[] restr){
		restricciones = restr;
	}
	public void setParam(double[] parametrosFU){
		param = new double[parametrosFU.length];
		for(int i = 0;i < parametrosFU.length;i++){
			param[i] = parametrosFU[i];
		}
	}
	
	public double calcPenalty(IChromosome cromosoma, int[] tipoNodo, int[] iter, double[] prob, double[] restr){
		double divndo = 0.0;
		double divsor = 0.0;
		Gene[] genes = cromosoma.getGenes();
		int largo = genes.length;
		double[] valores = new double[largo];
		for(int j = 0;j < largo;j++){
			valores[j] = (double) genes[j].getAllele();
		}
		int neg = chequeaNeg(valores);
		if(neg == 0) neg =1;
		divndo += neg;
		divsor += neg;
		divsor += 9; // restricciones globales que se deben cumplir
		int g = chequeaGlobal(valores, tipoNodo, iter, restr, prob);
		divndo += g;
				
		return (divndo/divsor);
	}
	
	public int chequeaNeg(double[] datos){ //chequeo negatividad, por construcción no debiesen haber datos negativos.
		int cont = 0;
		for(int i = 0;i < datos.length; i++){
			if(datos[i]<0){
				cont++;
			}
		}
		if(cont == 0) cont =1;
		return cont;
	}
	
	public int chequeaGlobal(double[] datos, int[] tipoNodo, int[] iter, double[] restr, double[] prob){ 
		int g = 0;
		double[] restricGlobal = new double[9];
		restricGlobal[1]=1.0;
		restricGlobal[2]=1.0;
		restricGlobal[4]=1.0;
		restricGlobal[6]=1.0;
		restricGlobal[7]=1.0;
		restricGlobal[8]=1.0;
		double[][] data = new double[datos.length/9][9];
		int cont = 0;
		for(int i = 0;i < datos.length;i++){
			for(int j = 0;j < 9;j++){
				data[i][j] = datos[cont];
				cont++;
			}
		}
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
				restricGlobal[2] *= data[i][2];
				restricGlobal[3] += data[i][3];
				restricGlobal[4] *= data[i][4];
				restricGlobal[5] += data[i][5];
				restricGlobal[6] *= data[i][6];
				restricGlobal[7] *= data[i][7];
				restricGlobal[8] *= data[i][8];
				tipoAnt = 1;
				break;
			case 2: // sec con iter
				restricGlobal[0] += iter[i]*data[i][0];
				restricGlobal[1] *= Math.pow(data[i][1],iter[i]);
				restricGlobal[2] *= Math.pow(data[i][2],iter[i]);
				restricGlobal[3] += iter[i]*data[i][3];
				restricGlobal[4] *= Math.pow(data[i][4],iter[i]);
				restricGlobal[5] += iter[i]*data[i][5];
				restricGlobal[6] *= Math.pow(data[i][6],iter[i]);
				restricGlobal[7] *= Math.pow(data[i][7],iter[i]);
				restricGlobal[8] *= Math.pow(data[i][8],iter[i]);
				tipoAnt = 2;
				break;
			case 3: // paralelo
				if(tipoAnt == 3){ // seteo lo valores máx y mín
					if(data[i][5] < minRend){ minRend=data[i][5];}
					if(data[i][3] > maxTpo){ maxTpo=data[i][3];}
					if(data[i][0] > maxLat){ maxLat=data[i][0];}
				}else{
					minRend=data[i][5];
					maxTpo=data[i][3];
					maxLat=data[i][0];
				}
				if(tipoNodo[i+1] == 3){ // si el siguiente es del mismo tipo, entonces no hago nada en los valores (0,3 y 5), porque se deberá compara
					//restricGlobal[0] += data[i][0];
					restricGlobal[1] *= data[i][1];
					restricGlobal[2] *= data[i][2];
					//restricGlobal[3] += data[i][3];
					restricGlobal[4] *= data[i][4];
					//restricGlobal[5] += data[i][5];
					restricGlobal[6] *= data[i][6];
					restricGlobal[7] *= data[i][7];
					restricGlobal[8] *= data[i][8];
				}else{ // sino
					restricGlobal[0] += maxLat;
					restricGlobal[1] *= data[i][1];
					restricGlobal[2] *= data[i][2];
					restricGlobal[3] += maxTpo;
					restricGlobal[4] *= data[i][4];
					restricGlobal[5] += minRend;
					restricGlobal[6] *= data[i][6];
					restricGlobal[7] *= data[i][7];
					restricGlobal[8] *= data[i][8];
				}
				
				tipoAnt = 3;
				break;
			case 4: // branch
				if(tipoAnt != 4){ 
					inicio4 = i;
				}
				if(tipoNodo[i+1] != 4){
					double r1 = 0.0;
					double r2 = 0.0;
					double r4 = 0.0;
					double r6 = 0.0;
					double r7 = 0.0;
					double r8 = 0.0;
					for(int j = 0;j < (i - inicio4 + 1);j++){
						restricGlobal[0] += prob[inicio4+j]*data[inicio4+j][0];
						r1 *= prob[inicio4+j]*data[inicio4+j][1];
						r2 *= prob[inicio4+j]*data[inicio4+j][2];
						restricGlobal[3] += prob[inicio4+j]*data[inicio4+j][3];
						r4 *= prob[inicio4+j]*data[inicio4+j][4];
						restricGlobal[5] += prob[inicio4+j]*data[inicio4+j][5];
						r6 *= prob[inicio4+j]*data[inicio4+j][6];
						r7 *= prob[inicio4+j]*data[inicio4+j][7];
						r8 *= prob[inicio4+j]*data[inicio4+j][8];
					}
					restricGlobal[1] *= r1;
					restricGlobal[2] *= r2;
					restricGlobal[4] *= r4;
					restricGlobal[6] *= r6;
					restricGlobal[7] *= r7;
					restricGlobal[8] *= r8;
				}
				tipoAnt = 4;
				break;
			}
			if(restricGlobal[0]>restr[0]){g++;}
			if(restricGlobal[1]<restr[1]){g++;}
			if(restricGlobal[2]<restr[2]){g++;}
			if(restricGlobal[3]>restr[3]){g++;}
			if(restricGlobal[4]<restr[4]){g++;}
			if(restricGlobal[5]>restr[5]){g++;}
			if(restricGlobal[6]<restr[6]){g++;}
			if(restricGlobal[7]<restr[7]){g++;}
			if(restricGlobal[8]<restr[8]){g++;}
				
		}
		
		if(g == 0) g =1;
		return g;
	}

} // Cierra clase
