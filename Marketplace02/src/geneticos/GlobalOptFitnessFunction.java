package geneticos;

import java.util.ArrayList;
import java.util.Vector;

import org.jgap.FitnessFunction;
import org.jgap.Gene;
import org.jgap.IChromosome;
import org.jgap.InvalidConfigurationException;
import org.jgap.impl.CompositeGene;

public class GlobalOptFitnessFunction extends FitnessFunction{

	private static final long serialVersionUID = -4036877567717740990L;
	private double[] param;
	private int[] tipoNodo;
	private double[] restr;
	private int[] iter; 
	private double[] prob;
	private int serv;
	private String[] data;
	private String[][] ofertas;
	private int[] bundling;

	public GlobalOptFitnessFunction(double[] param, int[] tipoNodo, double[] restr, int[] iter, double[] prob, String[][] ofertas, int[] bundling){
		this.iter = iter;
		this.param = param;
		this.prob = prob;
		this.restr = restr;
		this.tipoNodo =tipoNodo;
		this.serv = tipoNodo.length;
		this.ofertas = ofertas;
		this.bundling = bundling;
	}
	
	@Override
	protected double evaluate(IChromosome cromosoma) {
		double fitness = 0.0;
		
		String[][] data = new String[serv][11];
		try {
			data = this.obtenerDatos(cromosoma);
		} catch (InvalidConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		for(int i = 0;i < data.length;i++){
			for(int j = 0;j < param.length;j++){
				fitness += param[j]*Double.parseDouble(data[i][j]);				
			}
		}
		double[] restricGlobal = this.calcAgregado(data, tipoNodo);
		
		double penalty = this.calPenalty(data, restricGlobal, bundling);
		fitness-=penalty;
		//System.out.println("La función de ajuste es "+fitness);
		return Math.max(fitness,0.0);
	}
	
	protected String[][] obtenerDatos(IChromosome crom) throws InvalidConfigurationException{
		String[][] valores = new String[serv][11];
		data= new String[serv];
		Gene[] genes = new Gene[serv];
		genes = crom.getGenes();
		//CompositeGene compGene = new CompositeGene();
		for(int i = 0;i < serv;i++){
			CompositeGene compGene = (CompositeGene) genes[i];
			data[i]=(String) compGene.getApplicationData();
			Vector a = (Vector) compGene.getAllele();	//atrib, precio
			for(int j =0;j < a.size();j++){
				double aux = (double) a.get(j);
				valores[i][j]= String.valueOf(aux);
			}
		}
		return valores;
	}
	
	protected double[] calcAgregado(String[][] valores, int[] tipoNodo){
		double[] agregado = new double[9*serv];
		agregado[1]=1.0;
		agregado[3]=1.0;
		agregado[4]=1.0;
		agregado[5]=1.0;
		agregado[6]=1.0;
		//agregado[7]=1.0;
		agregado[8]=1.0;
		int tipoAnt = 0; // Para guardar el tipo de nodo de la corrida anterior.
		int inicio4 = 0;
		double minRend = 0.0; // Para guardar el mínimo de rendimiento de un conjunto de nodos
		double maxTpo = 0.0; // Para guardar el máximo de tiempo de respuesta de un conjunto de nodos
		double maxLat = 0.0; // Para guardar el máximo de latencia de un conjunto de nodos
		
		for(int i = 0;i < valores.length;i++){
		switch(tipoNodo[i]){
		case 1: // secuencia
			agregado[0] += Double.parseDouble(valores[i][0]);
			agregado[1] *= Double.parseDouble(valores[i][1]);
			agregado[2] += Double.parseDouble(valores[i][2]);
			agregado[3] *= Double.parseDouble(valores[i][3]);
			agregado[4] *= Double.parseDouble(valores[i][4]);
			agregado[5] *= Double.parseDouble(valores[i][5]);
			agregado[6] *= Double.parseDouble(valores[i][6]);
			agregado[7] += Double.parseDouble(valores[i][7]);
			agregado[8] *= Double.parseDouble(valores[i][8]);
			tipoAnt = 1;
			break;
		case 2: // sec con iter
			agregado[0] += iter[i]*Double.parseDouble(valores[i][0]);
			agregado[1] *= Math.pow(Double.parseDouble(valores[i][1]),iter[i]);
			agregado[2] += iter[i]*Double.parseDouble(valores[i][2]);
			agregado[3] *= Math.pow(Double.parseDouble(valores[i][3]),iter[i]);
			agregado[4] *= Math.pow(Double.parseDouble(valores[i][4]),iter[i]);
			agregado[5] *= Math.pow(Double.parseDouble(valores[i][5]),iter[i]);
			agregado[6] *= Math.pow(Double.parseDouble(valores[i][6]),iter[i]);
			agregado[7] += iter[i]*Double.parseDouble(valores[i][7]);
			agregado[8] *= Math.pow(Double.parseDouble(valores[i][8]),iter[i]);
			tipoAnt = 2;
			break;
		case 3: // paralelo
			if(tipoAnt == 3){ // seteo lo valores máx y mín
				if(Double.parseDouble(valores[i][0]) > maxTpo){ maxTpo=Double.parseDouble(valores[i][0]);}
				if(Double.parseDouble(valores[i][2]) < minRend){ minRend=Double.parseDouble(valores[i][2]);}
				if(Double.parseDouble(valores[i][7]) > maxLat){ maxLat=Double.parseDouble(valores[i][7]);}
			}else{
				maxTpo=Double.parseDouble(valores[i][0]);
				minRend=Double.parseDouble(valores[i][2]);
				maxLat=Double.parseDouble(valores[i][7]);
			}
			if((i+1)<serv){
			if(tipoNodo[i+1] == 3){ // si el siguiente es del mismo tipo, entonces no hago nada en los valores (0,3 y 5), porque se deberá compara
				//agregado[0] += data[i][0];
				agregado[1] *= Double.parseDouble(valores[i][1]);
				//agregado[2] += Double.parseDouble(valores[i][2]);
				agregado[3] *= Double.parseDouble(valores[i][3]);
				agregado[4] *= Double.parseDouble(valores[i][4]);
				agregado[5] *= Double.parseDouble(valores[i][5]);
				agregado[6] *= Double.parseDouble(valores[i][6]);
				//agregado[7] += Double.parseDouble(valores[i][7]);
				agregado[8] *= Double.parseDouble(valores[i][8]);
			}else{ // sino
				agregado[0] += maxTpo;
				agregado[1] *= Double.parseDouble(valores[i][1]);
				agregado[2] += minRend;
				agregado[3] *= Double.parseDouble(valores[i][3]);
				agregado[4] *= Double.parseDouble(valores[i][4]);
				agregado[5] *= Double.parseDouble(valores[i][5]);
				agregado[6] *= Double.parseDouble(valores[i][6]);
				agregado[7] += maxLat;
				agregado[8] *= Double.parseDouble(valores[i][8]);
			}
			}else{
				agregado[0] += maxTpo;
				agregado[1] *= Double.parseDouble(valores[i][1]);
				agregado[2] += minRend;
				agregado[3] *= Double.parseDouble(valores[i][3]);
				agregado[4] *= Double.parseDouble(valores[i][4]);
				agregado[5] *= Double.parseDouble(valores[i][5]);
				agregado[6] *= Double.parseDouble(valores[i][6]);
				agregado[7] += maxLat;
				agregado[8] *= Double.parseDouble(valores[i][8]);
			}
			tipoAnt = 3;
			break;
		case 4: // branch
			if(tipoAnt != 4){ 
				inicio4 = i;
			}
			if((i+1)<serv){
			if(tipoNodo[i+1] != 4){
				double r1 = 0.0;
				double r3 = 0.0;
				double r4 = 0.0;
				double r5 = 0.0;
				double r6 = 0.0;
				double r8 = 0.0;
				for(int j = 0;j < (i - inicio4 + 1);j++){
					agregado[0] += prob[inicio4+j]*Double.parseDouble(valores[inicio4+j][0]);
					r1 *= prob[inicio4+j]*Double.parseDouble(valores[inicio4+j][1]);
					agregado[2] += prob[inicio4+j]*Double.parseDouble(valores[inicio4+j][2]);
					r3 *= prob[inicio4+j]*Double.parseDouble(valores[inicio4+j][3]);
					r4 *= prob[inicio4+j]*Double.parseDouble(valores[inicio4+j][4]);
					r5 *= prob[inicio4+j]*Double.parseDouble(valores[inicio4+j][5]);
					r6 *= prob[inicio4+j]*Double.parseDouble(valores[inicio4+j][6]);
					agregado[7] += prob[inicio4+j]*Double.parseDouble(valores[inicio4+j][7]);
					r8 *= prob[inicio4+j]*Double.parseDouble(valores[inicio4+j][8]);
				}
				agregado[1] *= r1;
				agregado[3] *= r3;
				agregado[4] *= r4;
				agregado[5] *= r5;
				agregado[6] *= r6;
				agregado[8] *= r8;
			}
			}
			tipoAnt = 4;
			break;
		}
		}
		return agregado;
	}
	
	protected double chequeaRestricciones(double[] restricGlobal){
		int g = 0;
		
		if(restricGlobal[0]<restr[0]){g++;}
		if(restricGlobal[1]<restr[1]){g++;}
		if(restricGlobal[2]<restr[2]){g++;}
		if(restricGlobal[3]<restr[3]){g++;}
		if(restricGlobal[4]<restr[4]){g++;}
		if(restricGlobal[5]<restr[5]){g++;}
		if(restricGlobal[6]<restr[6]){g++;}
		if(restricGlobal[7]<restr[7]){g++;}
		if(restricGlobal[8]<restr[8]){g++;}
		
		g/=9;
		
		return g;
	}
	
	protected double chequeaNeg(double[] restricGlobal){
		int g=0;
		
		if(restricGlobal[0]<0){g++;}
		if(restricGlobal[1]<0){g++;}
		if(restricGlobal[2]<0){g++;}
		if(restricGlobal[3]<0){g++;}
		if(restricGlobal[4]<0){g++;}
		if(restricGlobal[5]<0){g++;}
		if(restricGlobal[6]<0){g++;}
		if(restricGlobal[7]<0){g++;}
		if(restricGlobal[8]<0){g++;}
		
		g /= 9;
		
		return g;
	}
	
	protected double chequeaBundling(String[][] valores, int[] bundling){
		
		//Separo los datos anexos del cromosoma, aux0 corresponde al número (id) de la oferta 
		String[] aux;
		int[] aux0 = new int[data.length];
		for(int i = 0;i < aux0.length;i++){
			aux = data[i].split("_");
			//System.out.println(aux[0]+" "+aux[1]+" "+aux[2]+" "+aux[3]);
			aux0[i] = (int) Integer.parseInt(aux[3]);  
		}
		
		int total = 0;
		//Cuento los servicios por oferta
		int[] sumaBundling = new int[bundling.length];
		for(int i = 0;i < sumaBundling.length;i++){
			for(int j = 0;j < aux0.length;j++){
				if(aux0[j]== i){
					sumaBundling[i] +=1;
					//System.out.println("El valor de sumabundling "+i+ " es "+sumaBundling[i]);
				}
			}
			if(bundling[i] != 1){ //cuento los bundlings que tienen más de un servicio
				total += 1;
			}
		}
		
		//Si todas las ofertas son con un servicio...
		if(total == 0){
			return 0.0;
		}
		int no = 0;
		//Chequeo que estén completos los bundlings
		for(int i = 0;i < bundling.length;i++){
			if(sumaBundling[i] != 0){ //Si tengo algun servicio de la oferta i
				if(sumaBundling[i] != bundling[i]){
					no+=1;
				}
			}
		}
		return (no/total);
		
	}
	
	public int getMax(int[] aux){
		int maximo= -999;
		for(int i = 0;i<aux.length;i++){
			int aux2 = aux[i];
			if(aux2 > maximo){
				maximo = aux2;
			}
		}
		return maximo;	
	}
	
	protected double calPenalty(String[][] valores, double[] restricGlobal, int[] bundling){
		double penalty = 0.0;
		
		double a = this.chequeaNeg(restricGlobal);

		double b = this.chequeaRestricciones(restricGlobal);
		
		double c = this.chequeaBundling(valores, bundling);
		
		penalty = a + b + c;
		
		return penalty;
		
	}
	
	/*protected double calcPenalty(IChromosome crom){
		double penalty = 0;
		for(int i = 0;i < largo;i++){
			if(i == 0 || i == 3){
				if(valores[i]<restricciones[i]){
					cont++;
					
				}
			}else{
				if(valores[i]>restricciones[i]){
					cont++;				
					
				}
			}
			return penalty;
		}
		penalty = cont/largo;
	}*/
	
	public void setParam(double[] parametrosFU){
		param = new double[parametrosFU.length];
		for(int i = 0;i < parametrosFU.length;i++){
			param[i] = parametrosFU[i];
		}
	}
	
	public void setTipoNodo(int[] tipoNodo){
		this.tipoNodo = new int[tipoNodo.length];
		for(int i = 0;i < tipoNodo.length;i++){
			this.tipoNodo[i] = tipoNodo[i];
		}
	}
	
	public void setRestricciones(double[] restr){
		this.restr = new double[restr.length];
		for(int i = 0;i < restr.length;i++){
			this.restr[i] = restr[i];
		}
	}
	
	public void setIter(int[] iter){
		this.iter = new int[iter.length];
		for(int i = 0;i < iter.length;i++){
			this.iter[i] = iter[i];
		}
	}
	
	public void setProb(double[] prob){
		this.prob = new double[prob.length];
		for(int i = 0;i < prob.length;i++){
			this.prob[i] = prob[i];
		}
	}

}
