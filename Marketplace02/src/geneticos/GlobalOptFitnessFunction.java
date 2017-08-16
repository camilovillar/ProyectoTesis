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
	private ArrayList<ArrayList> ofertasOrdenadas;

	public GlobalOptFitnessFunction(double[] param, int[] tipoNodo, double[] restr, int[] iter, double[] prob, String[][] ofertas, int[] bundling, ArrayList ofertasOrdenadas){
		this.iter = iter;
		this.param = param;
		this.prob = prob;
		this.restr = restr;
		this.tipoNodo =tipoNodo;
		this.serv = tipoNodo.length;
		this.ofertas = ofertas;
		this.bundling = bundling;
		this.ofertasOrdenadas = ofertasOrdenadas;
	}
	
	@Override
	protected double evaluate(IChromosome cromosoma) {
		double fitness = 0.0;
		
		String[][] atributos = new String[serv][11];
		int[] idOferta = new int[serv];
		//double[] 
		//int[] datos2 = new int[serv];
		try {
			atributos = this.obtenerDatos2(cromosoma);
			idOferta = this.obtenerIdOferta(cromosoma);
		} catch (InvalidConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		for(int i = 0;i < atributos.length;i++){
			for(int j = 0;j < param.length;j++){
				fitness += param[j]*Double.parseDouble(atributos[i][j]);				
			}
		}
		//double[] restricGlobal = this.calcAgregado(atributos, tipoNodo);
		System.out.println("La función de ajuste es "+fitness);
		//double penalty = this.calPenalty(atributos, restricGlobal, idOferta, bundling);
		double penalty = this.calPenalty2(atributos, idOferta, bundling);
		
		System.out.println("El castigo es "+penalty);
		
		fitness-=penalty;
		if(fitness <0) fitness = 0.0;
		System.out.println("La función de ajuste es "+fitness);
		return Math.max(fitness,0.0);
		
	}
	
	protected String[][] obtenerDatos(IChromosome crom) throws InvalidConfigurationException{
		String[][] valores = new String[serv][11];
		//data= new String[serv];
		Gene[] genes = new Gene[serv];
		genes = crom.getGenes();
		//CompositeGene compGene = new CompositeGene();
		for(int i = 0;i < serv;i++){
			CompositeGene compGene = (CompositeGene) genes[i];
			//setData(i,(String) compGene.getApplicationData());
			Vector a = (Vector) compGene.getAllele();	//atrib, precio
			for(int j =0;j < (a.size()-1);j++){
				double aux = (double) a.get(j);
				valores[i][j]= String.valueOf(aux);
				System.out.println("El valor de "+i+" "+j+" es "+valores[i][j]);
			}
			System.out.println("El valor de "+i+" "+10+" es "+a.get(10));
		}
		return valores;
	}
	protected String[][] obtenerDatos2(IChromosome crom) throws InvalidConfigurationException{
		// devuelve un areglo con los atributos y el precio
		String[][] valores = new String[serv][11];
		Gene[] genes = crom.getGenes();
		for(int i = 0;i < genes.length; i++){
			int alelo = (int) genes[i].getAllele(); 
			ArrayList aux0 = (ArrayList) ofertasOrdenadas.get(i);
			//System.out.println(aux0);
			int posicion = (int) aux0.get(alelo);
			// El orden es: atributos (0 a 8), nombreAgente (9), nombre (10), id (11), precio (12), indicador de la oferta como (13)
			for(int j = 0;j < valores[0].length-1; j++){
				valores[i][j] = ofertas[posicion][j];
			}
			valores[i][10]= ofertas[posicion][12];
		}
		return valores;
	}
	
	protected int[] obtenerIdOferta(IChromosome crom) throws InvalidConfigurationException{
		int[] idOferta = new int[serv];
		Gene[] genes = new Gene[serv];
		genes = crom.getGenes();
		for(int i = 0;i < genes.length;i++){
			int alelo = (int) genes[i].getAllele();
			ArrayList aux0 = (ArrayList) ofertasOrdenadas.get(i);
			//System.out.println(aux0);
			int posicion = (int) aux0.get(alelo);
			idOferta[i] = (int) Integer.parseInt(ofertas[posicion][13]);
		}
		return idOferta;
	}
	
	protected int[] obtenerIdOferta2(IChromosome crom) throws InvalidConfigurationException{
		int[] ids = new int[serv];
		Gene[] genes = new Gene[serv];
		
		genes = crom.getGenes();
		
		CompositeGene compGene;
		
		for(int i = 0;i < genes.length;i++){
			compGene = (CompositeGene) genes[i];
			Vector a = (Vector) compGene.getAllele();	//atrib, precio, idOferta
			ids[i] = (int) a.get(10);
			//ids[i]= Integer.parseInt(String.valueOf(aux));
			System.out.println("El valor de "+i+" es "+ids[i]);
		}	
		
		return ids;
	}
	
	protected double[] calcAgregado(String[][] valores, int[] tipoNodo){
		double[] agregado = new double[10];
		agregado[1]=1.0;
		agregado[3]=1.0;
		agregado[4]=1.0;
		agregado[5]=1.0;
		agregado[6]=1.0;
		agregado[8]=1.0;
		int tipoAnt = 0; // Para guardar el tipo de nodo de la corrida anterior.
		int inicio4 = 0;
		double minRend = 0.0; // Para guardar el mínimo de rendimiento de un conjunto de nodos
		double maxTpo = 0.0; // Para guardar el máximo de tiempo de respuesta de un conjunto de nodos
		double maxLat = 0.0; // Para guardar el máximo de latencia de un conjunto de nodos
		
		for(int i = 0;i < valores.length;i++){
			agregado[9] += Double.parseDouble(valores[i][10]); // sumo los precios
		
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
				double r1 = 1.0;
				double r3 = 1.0;
				double r4 = 1.0;
				double r5 = 1.0;
				double r6 = 1.0;
				double r8 = 1.0;
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
	
	protected int chequeaRestricciones(double[] restricGlobal){
		int g = 0;
		
		//for(int i = 0;i < restricGlobal.length;i++){
			if(restricGlobal[0]<restr[0]){
				g++;
				System.out.println("No se cumple la "+0+"esima restricción");
				System.out.println("agregado es "+ restricGlobal[0] + " la restricción es "+ restr[0]);
			}
			if(restricGlobal[1]<restr[1]){
				g++;
				System.out.println("No se cumple la "+1+"esima restricción");
				System.out.println("agregado es "+ restricGlobal[1] + " la restricción es "+ restr[1]);
			}
			if(restricGlobal[2]<restr[2]){
				g++;
				System.out.println("No se cumple la "+2+"esima restricción");
				System.out.println("agregado es "+ restricGlobal[2] + " la restricción es "+ restr[2]);
			}
			if(restricGlobal[3]<restr[3]){
				g++;
				System.out.println("No se cumple la "+3+"esima restricción");
				System.out.println("agregado es "+ restricGlobal[3] + " la restricción es "+ restr[3]);
			}
			if(restricGlobal[4]<restr[4]){
				g++;
				System.out.println("No se cumple la "+4+"esima restricción");
				System.out.println("agregado es "+ restricGlobal[4] + " la restricción es "+ restr[4]);
			}
			if(restricGlobal[5]<restr[5]){
				g++;
				System.out.println("No se cumple la "+5+"esima restricción");
				System.out.println("agregado es "+ restricGlobal[5] + " la restricción es "+ restr[5]);
			}
			if(restricGlobal[6]<restr[6]){
				g++;
				System.out.println("No se cumple la "+6+"esima restricción");
				System.out.println("agregado es "+ restricGlobal[6] + " la restricción es "+ restr[6]);
			}
			if(restricGlobal[7]<restr[7]){
				g++;
				System.out.println("No se cumple la "+7+"esima restricción");
				System.out.println("agregado es "+ restricGlobal[7] + " la restricción es "+ restr[7]);
			}
			if(restricGlobal[8]<restr[8]){
				g++;
				System.out.println("No se cumple la "+8+"esima restricción");
				System.out.println("agregado es "+ restricGlobal[8] + " la restricción es "+ restr[8]);
			}
		//}
		
		
		System.out.println("No se cumplen "+ (g) +" restricciones");
		
		
		//System.out.println("El castigo por restricciones no cumplidas es de "+ g);
		
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
	
	protected double chequeaBundling(String[][] valores, int[] bundling, String[] datos){
		
		//Separo los datos anexos del cromosoma, aux0 corresponde al número (id) de la oferta 
		//String[] aux;
		int[] idOferta = new int[datos.length];
		int[] idOferta2 = new int[datos.length];
		//int[] idServicio = new int[datos.length];
		
		//Recupero el número de la oferta recibida a la que corresponde el 
		
		int[] sumaBundling = new int[bundling.length];
		//int[] sumaBundling2 = new int[bundling.length];
		int total = 0;
		for(int i = 0;i < idOferta.length;i++){
			
			idOferta2[i] = Integer.parseInt(valores[i][13]);
			//System.out.println("El serv "+ i +" corresponde a la oferta "+idOferta2[i]);
			sumaBundling[idOferta2[i]]++;
			total++;
			//revisado
		}
		
		int no = 0;
		//Chequeo que estén completos los bundlings
		for(int i = 0;i < bundling.length;i++){
			//System.out.println("sumabundling de " +i+" es "+sumaBundling[i]);
			if(sumaBundling[i] != 0){ //Si tengo algun servicio de la oferta i	
				if(sumaBundling[i] != bundling[i]){
					//System.out.println("bundling es "+bundling[i]);
					no+=1;
				}else{
					System.out.println("sumabundling de " +i+" es "+sumaBundling[i]);
					System.out.println("bundling es "+bundling[i] +" son iguales!");
				}
			}
		}
		System.out.println("No es igual a "+no+ " y total es "+total);
		return (no/total);
		
	}
	
	protected double chequeaBundling2(String[][] valores, int[] bundling, int[] datos){
		
		double resultado;
		int[] sumaBundling2 = new int[bundling.length];
		int total = 0;
		for(int i = 0;i < datos.length;i++){
			//System.out.println("El serv "+ i +" corresponde a la oferta "+datos[i]);
			sumaBundling2[datos[i]]++;
			total++;
			//revisado
		}
		
		int no = 0;
		//Chequeo que estén completos los bundlings
		for(int j = 0;j < bundling.length;j++){
			//System.out.println("sumabundling2 de " +j+" es "+sumaBundling2[j]);
			if(sumaBundling2[j] != 0){ //Si tengo algun servicio de la oferta i	
				if(sumaBundling2[j] != bundling[j]){
					//System.out.println("bundling "+j+" es "+bundling[j]);
					no++;
				}else{
					//System.out.println("sumabundling2 de " +j+" es "+sumaBundling2[j]);
					System.out.println("bundling es "+bundling[j] +" son iguales!");
				}
			}
		}
		System.out.println("No es igual a "+no+ " y total es "+total);
		
		//System.out.println("El castigo por bundling retornado es "+(no/total));
		
		resultado = no;
		resultado /= total;
		
		return resultado;
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
	
	public int chequeaPresupuesto(double[] restricGlobal){
		int c=0;
		if(restricGlobal[9]>restr[9]){c = 1;}
		System.out.println("El presupuesto es "+ restr[9] +" y el precio acumulado es "+restricGlobal[9]);
		return c;
	}
	
	protected double calPenalty(String[][] valores, double[] restricGlobal, String[] datos, int[] bundling){
		double penalty = 0.0;
		double w1 = 1.0;
		//double w2 = 2.0;		
		double w3 = serv*2;
		int a = 0;
		//double b = 0.0;
		double c = 0.0;
		
		a = this.chequeaPresupuesto(restricGlobal);

		//b = this.chequeaRestricciones(restricGlobal);
		
		c = this.chequeaBundling(valores, bundling, datos);
		
		// + w2*b/9
		penalty = w1*a  + w3*c;
		
		return penalty;
		
	}
	protected double calPenalty2(String[][] valores, int[] datos, int[] bundling){
		double penalty = 0.0;
		double w1 = 1.0;
		double w2 = 3.0;		
		double w3 = serv*1.5;
		
		int a = 0;
		int b = 0;
		double c = 0.0;
		
		double[] restricGlobal = this.calcAgregado(valores, tipoNodo);
		//double d = this.chequeaRestricciones(agregado);
		
		a = chequeaPresupuesto(restricGlobal);
		System.out.println("El castigo por presupuesto es "+a);
		System.out.println("El castigo por presupuesto luego de ponderar es "+(w1*a));

		b = chequeaRestricciones(restricGlobal);
		System.out.println("El castigo por violar restricciones es "+b);
		System.out.println("El castigo por violar restricciones luego de ponderar es "+(w2*b/9));
		
		c = chequeaBundling2(valores, bundling, datos);
		//System.out.println("c tiene el valor de "+c);
		//System.out.println("El castigo por bundling es "+c);
		System.out.println("El castigo por bundling luego de ponderar es "+(w3*c));
		
		penalty += w2*b/9;
		penalty += w1*a + w3*c;
		
		return penalty;
		
	}
	
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
