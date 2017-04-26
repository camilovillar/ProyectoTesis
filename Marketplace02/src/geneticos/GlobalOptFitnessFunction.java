package geneticos;

import java.util.ArrayList;

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
	

	public GlobalOptFitnessFunction(double[] param, int[] tipoNodo, double[] restr, int[] iter, double[] prob){
		this.setIter(iter);
		this.setParam(param);
		this.setProb(prob);
		this.setRestricciones(restr);
		this.setTipoNodo(tipoNodo);
		this.serv = tipoNodo.length;
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
		
		
		
		
		
		return Math.max(fitness,0.0);
	}
	
	protected String[][] obtenerDatos(IChromosome crom) throws InvalidConfigurationException{
		String[][] valores = new String[serv][11];
		Gene[] genes = new Gene[serv];
		genes = crom.getGenes();
		CompositeGene compGene = new CompositeGene();
		for(int i = 0;i < serv;i++){
			compGene = (CompositeGene) genes[i];
			valores[i] = (String[]) compGene.getAllele();			
		}
		return valores;
	}
	
	protected double[] calcAgregado(String[][] valores, int[] tipoNodo){
		double[] agregado = new double[9*serv];
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
			agregado[2] *= Double.parseDouble(valores[i][2]);
			agregado[3] += Double.parseDouble(valores[i][3]);
			agregado[4] *= Double.parseDouble(valores[i][4]);
			agregado[5] += Double.parseDouble(valores[i][5]);
			agregado[6] *= Double.parseDouble(valores[i][6]);
			agregado[7] *= Double.parseDouble(valores[i][7]);
			agregado[8] *= Double.parseDouble(valores[i][8]);
			tipoAnt = 1;
			break;
		case 2: // sec con iter
			agregado[0] += iter[i]*Double.parseDouble(valores[i][0]);
			agregado[1] *= Math.pow(Double.parseDouble(valores[i][1]),iter[i]);
			agregado[2] *= Math.pow(Double.parseDouble(valores[i][2]),iter[i]);
			agregado[3] += iter[i]*Double.parseDouble(valores[i][3]);
			agregado[4] *= Math.pow(Double.parseDouble(valores[i][4]),iter[i]);
			agregado[5] += iter[i]*Double.parseDouble(valores[i][5]);
			agregado[6] *= Math.pow(Double.parseDouble(valores[i][6]),iter[i]);
			agregado[7] *= Math.pow(Double.parseDouble(valores[i][7]),iter[i]);
			agregado[8] *= Math.pow(Double.parseDouble(valores[i][8]),iter[i]);
			tipoAnt = 2;
			break;
		case 3: // paralelo
			if(tipoAnt == 3){ // seteo lo valores máx y mín
				if(Double.parseDouble(valores[i][5]) < minRend){ minRend=Double.parseDouble(valores[i][5]);}
				if(Double.parseDouble(valores[i][3]) > maxTpo){ maxTpo=Double.parseDouble(valores[i][3]);}
				if(Double.parseDouble(valores[i][0]) > maxLat){ maxLat=Double.parseDouble(valores[i][0]);}
			}else{
				minRend=Double.parseDouble(valores[i][5]);
				maxTpo=Double.parseDouble(valores[i][3]);
				maxLat=Double.parseDouble(valores[i][0]);
			}
			if(tipoNodo[i+1] == 3){ // si el siguiente es del mismo tipo, entonces no hago nada en los valores (0,3 y 5), porque se deberá compara
				//agregado[0] += data[i][0];
				agregado[1] *= Double.parseDouble(valores[i][1]);
				agregado[2] *= Double.parseDouble(valores[i][2]);
				//agregado[3] += Double.parseDouble(valores[i][3]);
				agregado[4] *= Double.parseDouble(valores[i][4]);
				//agregado[5] += Double.parseDouble(valores[i][5]);
				agregado[6] *= Double.parseDouble(valores[i][6]);
				agregado[7] *= Double.parseDouble(valores[i][7]);
				agregado[8] *= Double.parseDouble(valores[i][8]);
			}else{ // sino
				agregado[0] += maxLat;
				agregado[1] *= Double.parseDouble(valores[i][1]);
				agregado[2] *= Double.parseDouble(valores[i][2]);
				agregado[3] += maxTpo;
				agregado[4] *= Double.parseDouble(valores[i][4]);
				agregado[5] += minRend;
				agregado[6] *= Double.parseDouble(valores[i][6]);
				agregado[7] *= Double.parseDouble(valores[i][7]);
				agregado[8] *= Double.parseDouble(valores[i][8]);
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
					agregado[0] += prob[inicio4+j]*Double.parseDouble(valores[inicio4+j][0]);
					r1 *= prob[inicio4+j]*Double.parseDouble(valores[inicio4+j][1]);
					r2 *= prob[inicio4+j]*Double.parseDouble(valores[inicio4+j][2]);
					agregado[3] += prob[inicio4+j]*Double.parseDouble(valores[inicio4+j][3]);
					r4 *= prob[inicio4+j]*Double.parseDouble(valores[inicio4+j][4]);
					agregado[5] += prob[inicio4+j]*Double.parseDouble(valores[inicio4+j][5]);
					r6 *= prob[inicio4+j]*Double.parseDouble(valores[inicio4+j][6]);
					r7 *= prob[inicio4+j]*Double.parseDouble(valores[inicio4+j][7]);
					r8 *= prob[inicio4+j]*Double.parseDouble(valores[inicio4+j][8]);
				}
				agregado[1] *= r1;
				agregado[2] *= r2;
				agregado[4] *= r4;
				agregado[6] *= r6;
				agregado[7] *= r7;
				agregado[8] *= r8;
			}
			tipoAnt = 4;
			break;
		}
		}
		return agregado;
	}
	
	protected int chequeaRestricciones(double[] restricGlobal){
		int g = 0;
		
		if(restricGlobal[0]>restr[0]){g++;}
		if(restricGlobal[1]<restr[1]){g++;}
		if(restricGlobal[2]<restr[2]){g++;}
		if(restricGlobal[3]>restr[3]){g++;}
		if(restricGlobal[4]<restr[4]){g++;}
		if(restricGlobal[5]>restr[5]){g++;}
		if(restricGlobal[6]<restr[6]){g++;}
		if(restricGlobal[7]<restr[7]){g++;}
		if(restricGlobal[8]<restr[8]){g++;}
		
		return g;
	}
	
	protected int chequeaNeg(double[] restricGlobal){
		int g=0;
		
		if(restricGlobal[0]>restr[0]){g++;}
		if(restricGlobal[1]<restr[1]){g++;}
		if(restricGlobal[2]<restr[2]){g++;}
		if(restricGlobal[3]>restr[3]){g++;}
		if(restricGlobal[4]<restr[4]){g++;}
		if(restricGlobal[5]>restr[5]){g++;}
		if(restricGlobal[6]<restr[6]){g++;}
		if(restricGlobal[7]<restr[7]){g++;}
		if(restricGlobal[8]<restr[8]){g++;}
		
		return g;
	}
	
	protected void chequeaBundling(String[][] valores){
		ArrayList bundlings = new ArrayList();
		int n = 0;
		for(int i = 0;i < valores.length;i++){
			if(valores[i][11].indexOf("p") > 0){
				String bund = valores[i][11];
				String[] num = bund.split("_");
				n = Integer.parseInt(num[1]);
			
			}
		}
	}
	
	protected void calPenalty(double[] restricGlobal){
		double divndo = 0.0;
		double divsor = 0.0;
		
		int a = this.chequeaNeg(restricGlobal);
		
		divndo += a;
		divsor += a;
		
		int b = this.chequeaRestricciones(restricGlobal);
		
		divndo += b;
		divsor += 9;
		
		
		
		
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
