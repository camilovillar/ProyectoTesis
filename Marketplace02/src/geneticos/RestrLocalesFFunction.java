package geneticos;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.jgap.FitnessFunction;
import org.jgap.Gene;
import org.jgap.IChromosome;

import funciones.FuncionUtilidad;

public class RestrLocalesFFunction extends FitnessFunction{

	/**
	 * 
	 */
	private static final long serialVersionUID = -5484443467776976033L;
	
	private int nroGenes;
	int nServ;
	private String[][] atrib = new String[2507][10];
	private double[] param;
	
	
	public RestrLocalesFFunction(int i_nroServ){ // Número de actividades en un proceso por el número de atributos de calidad
		nServ = i_nroServ;
		nroGenes = i_nroServ*9 ; 
	}

	@Override
	protected double evaluate(IChromosome cromosoma) {
		
		Gene[] gene = cromosoma.getGenes();
		double[] alelos = new double[gene.length];
		double[] opera = new double[gene.length];
		double resultado = 1;
		
		for(int i=0; i<gene.length;i++){
			alelos[i]=(double) gene[i].getAllele();
		}
		
		int cont = 0;
		for(int i = 0;i<opera.length;i++){
			String act = "serv"+cont;
			opera[i] = (getNoRestringidos(i, act,alelos[i]));
			opera[i] /= (getNServ(act));
			opera[i] *= (getUtilidad(cont));
			opera[i] /=(getUMax(act));
			if((i+1)%9 == 0){
				cont++;
			}
		}
		
		for(int i = 0;i<opera.length;i++){
			resultado = 1;
			resultado*=opera[i];
		}
		
		return resultado;
		
		
		
	} // Cierra método evaluate 
	
	
	public double getUtilidad(int serv){
		double utilidad = 0;
		
		for(int i = 0;i<9;i++){
			utilidad += param[i]*Double.parseDouble(atrib[serv][i]);
		}
		
		return utilidad;
	}
	
	public double getUMax(String serv){
		List utilidades = new ArrayList();
		for(int i = 0;i<atrib.length;i++){
			if(atrib[i][9].equals(serv)){
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
		
		return umax;
	}
	
	public int getNoRestringidos(int pos, String serv, double alelo){
		int noRestr = 0;
		if(pos == 0 || pos == 3){ // latencia y tiempo de ejec
		for(int i =0;i<atrib.length;i++){
			if(atrib[i][9].equals(serv) && Double.parseDouble(atrib[i][pos])<alelo){
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
		return noRestr;
	}
	
	public int getNServ(String serv){
		int n = 0;
		
		if(atrib[9].equals(serv)){
			n++;
		}
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
	
	public void setParam(double[] parametrosFU){
		param = new double[parametrosFU.length];
		for(int i = 0;i < parametrosFU.length;i++){
			param[i] = parametrosFU[i];
		}
	}
	/*
	public double calcPenalty(IChromosome cromosoma){
		double penalty = 1;
		Gene[] genes = cromosoma.getGenes();
		int largo = genes.length;
		double[] valores = new double[largo];
		for(int j = 0;j < largo;j++){
			valores[j] = (double) genes[j].getAllele();
		}
		for(int i = 0;i < largo;i++){
			if(valores)
		}
			
		return penalty;
	}
	
	public boolean setConstraint(double[] restricciones){
		
	}
	*/

} // Cierra clase
