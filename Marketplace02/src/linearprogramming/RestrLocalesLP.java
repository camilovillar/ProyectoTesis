package linearprogramming;
import geneticos.NivelesServicio;
import gurobi.*;


public class RestrLocalesLP {
	private int serv;
	private String[][] arregloServ;
	private double[] param;
	private int[] tipoNodo;
	private int[] iter; 
	private double[] prob; 
	private double[] restr;
	private int niveles;
	private String[] tipo;
	
	
	public RestrLocalesLP(int niveles, int n, String[][] arregloServ, double[] param, int[] tipoNodo, int[] iter, double[] prob, double[] restr){
		this.serv = n;
		this.arregloServ = arregloServ;
		this.param = param;
		this.tipoNodo = tipoNodo;
		this.iter = iter;
		this.prob = prob;
		this.restr = restr;
		for(int i = 0;i < restr.length;i++){
			System.out.println(this.restr[i]);
		}
		this.niveles = niveles;
		this.setTipo(arregloServ);
	}
	
	public double[][][] calcularLogPuntaje(double[][][] matriz){
		
		//double[][][] puntaje = new double[serv][arregloServ[0].length][niveles]; // matriz serv*atributos*niveles
		
		double[][][] puntaje = calcularFuncionH(matriz, arregloServ);
		int[] nServ = contarServicios(tipo);
		for(int i = 0;i<puntaje.length;i++){
			for(int j = 0;j < puntaje[0].length;j++){
				for(int k = 0;k < puntaje[0][0].length;k++){
					puntaje[i][j][k]/=nServ[i];
				}
			}
		}
		double[][][] logPuntaje = calcularLog(puntaje);
		
		return logPuntaje;
	}
	
	public double[][][] calcularLog(double[][][] puntaje){
		double[][][] logPuntaje = new double[puntaje.length][puntaje[0].length][puntaje[0][0].length];
		for(int i = 0;i < puntaje.length;i++){
			for(int j = 0;j < puntaje[0].length;j++){
				for(int k = 0;k < puntaje[0][0].length;k++){
					logPuntaje[i][j][k] = Math.log(puntaje[i][j][k]);					
				}
			}
		}
		return logPuntaje;		
	}
	
	public double[][][] calcularFuncionH(double[][][] matriz, String[][] servicios){
		//Cuento el número de servicios que cumple con las restricción en ese nivel de servicio
		//Entrega una matriz double[][][]
		
		double[][][] resultado = new double[serv][matriz[0].length][niveles];
		
		for(int i = 0;i < resultado.length;i++){
			String serv = "serv"+i;
			for(int j = 0; j< resultado[0].length;j++){
				for(int k = 0;k < resultado[0][0].length;k++){
					if(tipo[i].equals(serv) && Double.parseDouble(servicios[i][j+1])<matriz[i][j][k]){
						resultado[i][j][k]+=1;
					}
				}
			}
		}
		
		return resultado;
	}
	
	public int[] contarServicios(String[] tipo){
		// cuento el total de servicios por cada tipo
		// Entrega un vector del tamaño de los servicios distintos
		
		int[] nServN = new int[serv];
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
		return nServN;
	}
	
	public void setTipo(String[][] servicios){
		tipo = new String[servicios.length];
		for(int i = 0;i < servicios.length;i++){
			tipo[i]=servicios[i][0];
		}
	}
	
	public double calcularFATiempo(double[][] data, int a){//a puede ser 0 (cero, para el tiempode ejecución o 7 (siete, para la latencia)
		/*double[][] data = new double[datos.length][datos[0].length];
		for(int i = 0;i < datos.length;i++){
			for(int j = 0;j < datos[0].length;j++){
				for(int k = 0;k < datos[0][0].length;k++){
					data[i][j]+=datos[i][j][k];
				}
			}
		}		
		*/
		double agregado0=0.0;
		int tipoAnt = 0; // Para guardar el tipo de nodo de la corrida anterior.
		int inicio4 = 0;
		double maxTpo = 0.0; // Para guardar el máximo de tiempo de respuesta de un conjunto de nodos
		
		for(int i = 0;i < tipoNodo.length;i++){
			
			switch(tipoNodo[i]){
			case 1: // secuencia
				agregado0 += data[i][a];
				tipoAnt = 1;
				break;
			case 2: // sec con iter
				agregado0 += iter[i]*data[i][a];
				tipoAnt = 2;
				break;
			case 3: // paralelo
				if(tipoAnt == 3){ // seteo lo valores máx y mín
					if(data[i][a] > maxTpo){ maxTpo=data[i][a];}
				}else{
					maxTpo=data[i][a];
					
				}
				if(i+1<serv){
				if(tipoNodo[i+1] != 3){ // si el siguiente es del mismo tipo, entonces no hago nada en los valores (0,3 y 5), porque se deberá compara
					agregado0 += maxTpo;
				}
				}else{
					agregado0 += maxTpo;
				}
				
				tipoAnt = 3;
				break;
			case 4: // branch
				if(tipoAnt != 4){// el primero de tipo 4 marca el inicio de los nodos en branch
					inicio4 = i;
				}
				if((i+1)<5){
				if(tipoNodo[i+1] != 4){// si es el último de tipo 4 en la serie...
					for(int j = 0;j < (i - inicio4 + 1);j++){
						agregado0 += prob[inicio4+j]*data[inicio4+j][a];	
					}
				}
				}else{//Si no hay i+1 entonces hago lo mismo
					for(int j = 0;j < (i - inicio4 + 1);j++){
						agregado0 += prob[inicio4+j]*data[inicio4+j][a];
					}
				}
				tipoAnt = 4;
				break;
			}	
		}
		return agregado0;
	}
	
	public double calcularFAPorcentaje(double[][] data, int j){
		
		double agregado1=1.0;
		int tipoAnt = 0; // Para guardar el tipo de nodo de la corrida anterior.
		int inicio4 = 0;
		double minRend = 0.0; // Para guardar el mínimo de rendimiento de un conjunto de nodos
		double maxTpo = 0.0; // Para guardar el máximo de tiempo de respuesta de un conjunto de nodos
		double maxLat = 0.0; // Para guardar el máximo de latencia de un conjunto de nodos
		
		for(int i = 0;i < tipoNodo.length;i++){
			
			switch(tipoNodo[i]){
			case 1: // secuencia
				agregado1 *= data[i][j];
				tipoAnt = 1;
				break;
			case 2: // sec con iter
				agregado1 *= Math.pow(data[i][j],iter[i]);
				tipoAnt = 2;
				break;
			case 3: // paralelo

				if(i+1<serv){
				if(tipoNodo[i+1] == 3){ // si el siguiente es del mismo tipo, entonces no hago nada en los valores (0,3 y 5), porque se deberá compara
					//agregado0 += data[i][0];
					agregado1 *= data[i][j];
				}else{ // sino

					agregado1 *= data[i][j];
				}
				}else{

					agregado1 *= data[i][j];

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

					for(int k = 0;k < (i - inicio4 + 1);k++){

						r1 *= prob[inicio4+k]*data[inicio4+k][j];
					}
					agregado1 *= r1;

				}
				}else{//Si no hay i+1 entonces hago lo mismo
					double r1 = 0.0;

					for(int k = 0;k < (i - inicio4 + 1);k++){

						r1 *= prob[inicio4+k]*data[inicio4+k][1];

					}
					agregado1 *= r1;

				}
				tipoAnt = 4;
				break;
			}	
		}
		return agregado1;
	}
	
	public double calcularFARendimiento(double[][] data){
		double agregado2=0.0;
		int tipoAnt = 0; // Para guardar el tipo de nodo de la corrida anterior.
		int inicio4 = 0;
		double minRend = 0.0; // Para guardar el mínimo de rendimiento de un conjunto de nodos
		double maxTpo = 0.0; // Para guardar el máximo de tiempo de respuesta de un conjunto de nodos
		double maxLat = 0.0; // Para guardar el máximo de latencia de un conjunto de nodos
		
		for(int i = 0;i < tipoNodo.length;i++){
			
			switch(tipoNodo[i]){
			case 1: // secuencia
				agregado2 += data[i][2];
				tipoAnt = 1;
				break;
			case 2: // sec con iter

				agregado2 += iter[i]*data[i][2];
				tipoAnt = 2;
				break;
			case 3: // paralelo
				if(tipoAnt == 3){ // seteo lo valores máx y mín
					if(data[i][0] > maxTpo){ maxTpo=data[i][0];}
					if(data[i][2] < minRend){ minRend=data[i][2];}
					if(data[i][7] > maxLat){ maxLat=data[i][7];}
				}else{
					maxLat=data[i][0];
					minRend=data[i][2];
					maxTpo=data[i][7];
					
				}
				if(i+1<serv){
				if(tipoNodo[i+1] != 3){ // si el siguiente es del mismo tipo, entonces no hago nada en los valores (0,3 y 5), porque se deberá compara

					agregado2 += minRend;
				}
				}else{
					agregado2 += minRend;
				}
				
				tipoAnt = 3;
				break;
			case 4: // branch
				if(tipoAnt != 4){// el primero de tipo 4 marca el inicio de los nodos en branch
					inicio4 = i;
				}
				if((i+1)<5){
				if(tipoNodo[i+1] != 4){// si es el último de tipo 4 en la serie...

					for(int j = 0;j < (i - inicio4 + 1);j++){
						agregado2 += prob[inicio4+j]*data[inicio4+j][2];
					}
				}
				}else{//Si no hay i+1 entonces hago lo mismo
					for(int j = 0;j < (i - inicio4 + 1);j++){
						agregado2 += prob[inicio4+j]*data[inicio4+j][2];
					}
				}
				tipoAnt = 4;
				break;
			}		
		}
		return agregado2;
	}
	
	public double[][][] optimizar(){
		
		NivelesServicio nivel = new NivelesServicio( arregloServ , serv);
		double[][][] matriz = nivel.getNiveles(niveles);
		double[][][] logPuntaje = calcularLogPuntaje(matriz);
		double[][][] resultado=new double[matriz.length][matriz[0].length][matriz[0][0].length];
		
			
		try{
			GRBEnv env = new GRBEnv("RestrLocalesLP.log");
			GRBModel model = new GRBModel(env);
			
			//Variables
			GRBVar[][][] x = new GRBVar[matriz.length][matriz[0].length][matriz[0][0].length];
			for(int i = 0;i < matriz.length;i++){
				for(int j = 0;j < matriz[0].length;j++){
					for(int k = 0;k < matriz[0][0].length;k++){
						x[i][j][k] = model.addVar(0, 1, 0, GRB.BINARY, "x"+i+j+k);
					}
				}
			}
			model.update();
			
			//Objetivo es maximizar
			model.set(GRB.IntAttr.ModelSense, GRB.MAXIMIZE);
			GRBLinExpr obj = new GRBLinExpr();
			
			
			//Restricciones
			GRBLinExpr[] r = new GRBLinExpr[restr.length];
			
			//Lado derecho
			for(int i = 0;i < restr.length;i++){
				System.out.println(restr[i]);
				r[i].addConstant(restr[i]);
				System.out.println(restr[i]);
			}
			//Lado izquierdo
			
			GRBLinExpr[] lhs = new GRBLinExpr[restr.length];
			
			GRBLinExpr[][][] lhs2= new GRBLinExpr[matriz.length][matriz[0].length][matriz[0][0].length];
			GRBLinExpr[][] lhs3 = new GRBLinExpr[matriz.length][matriz[0].length];
			for(int i = 0;i < lhs2.length;i++){
				for(int j = 0;j < lhs2[0].length;j++){
					for(int k = 0;k < lhs2[0][0].length;k++){
						lhs2[i][j][k].addTerm(matriz[i][j][k], x[i][j][k]);
						lhs3[i][j].multAdd(1, lhs2[i][j][k]); 
						obj.addTerm(logPuntaje[i][j][k], x[i][j][k]);
					}
				}
			}
			model.update();
			double[][] datos = new double[matriz.length][matriz[0].length];
			for(int i = 0;i < datos.length;i++){
				for(int j = 0;j < datos[0].length;j++){
					datos[i][j]=lhs3[i][j].getValue();					
				}
			}
			
			lhs[0].addConstant(calcularFATiempo(datos, 0));
			lhs[1].addConstant(calcularFAPorcentaje(datos, 1));
			lhs[2].addConstant(calcularFARendimiento(datos));
			lhs[3].addConstant(calcularFAPorcentaje(datos, 3));
			lhs[4].addConstant(calcularFAPorcentaje(datos, 4));
			lhs[5].addConstant(calcularFAPorcentaje(datos, 5));
			lhs[6].addConstant(calcularFAPorcentaje(datos, 6));
			lhs[7].addConstant(calcularFATiempo(datos, 7));
			lhs[8].addConstant(calcularFAPorcentaje(datos, 8));
			
			for(int i = 0;i < r.length;i++){
				model.addConstr(lhs[i], GRB.LESS_EQUAL, r[i], "r"+i);
			}
			model.update();
			
			model.setObjective(obj);
			
			model.optimize();
			
			GRBVar[][][] vars;
			//double[][][] resultado=new double[matriz.length][matriz[0].length][matriz[0][0].length];
			if (model.get(GRB.IntAttr.Status) == GRB.Status.OPTIMAL) {
				for(int i = 0;i < x.length;i++) {
					for(int j = 0;j < x[0].length;j++) {
						for(int k = 0;k < x[0][0].length;k++){
							if (x[i][j][k].get(GRB.DoubleAttr.X) > 0.0001) {
								System.out.println(x[i][j][k].get(GRB.DoubleAttr.X));
								resultado[i][j][k] = x[i][j][k].get(GRB.DoubleAttr.X);
							}
						}
					}
				}
			}
			
			
			/*double[] vars2 = new double[vars.length];
			for(int i = 0;i < vars2.length;i++){
				vars2[i]= vars[i].get
			}*/
			//printSolution(model, x);
			// Optimizar
			//int status = solveAndPrint(model, x, arregloServ.length);
			/*if (status != GRB.Status.OPTIMAL ) {
				return;
			}*/
			
		} catch (GRBException e) {
			System.out.println("Error code: " + e.getErrorCode() + ". " +
			e.getMessage());
		}
		
		for(int i = 0;i < resultado.length;i++) {
			for(int j = 0;j < resultado[0].length;j++) {
				for(int k = 0;k < resultado[0][0].length;k++){
					resultado[i][j][k] *= matriz[i][j][k];
				}
			}
		}
		
		return resultado;
	}
	
	
	/*
	public void calcularUtilidad(String[][] servicios, double[] param){
		//Calculo la utilidad de los servicios disponibles
		//Entrega un vector del largo de los servicios disponibles
		
		
	}
	
	public void calcularMaximo(){
		//Calculo el máximo de las utilidades para los tipo de servicio servicio
		//genera un arreglo de tipo de servicio*niveles
		
		
	}
	
	public void calcularUtilidadNivel(){ 
		//Corresponde a la función u, que es la utilidad máxima que se obtiene considerando los servicios de la función H
		//entrega una matriz serv*atributos*niveles
		
		
	}
	*/
}
