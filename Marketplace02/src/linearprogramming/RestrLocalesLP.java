package linearprogramming;
import gurobi.*;
import nivelescalidad.NivelesServicio;
import parametros.Parametros;


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
	private Parametros parametros;
	
	
	public RestrLocalesLP(int niveles, int n, String[][] arregloServ, double[] param, int[] tipoNodo, int[] iter, double[] prob, double[] restr){
		this.serv = n;
		this.arregloServ = arregloServ;
		this.param = param;
		this.tipoNodo = tipoNodo;
		this.iter = iter;
		this.prob = prob;
		this.restr = restr;
	/*	for(int i = 0;i < restr.length;i++){
	*		System.out.println(this.restr[i]);
	*	}
	*/
		this.niveles = niveles;
		this.setTipo(arregloServ);
	}
	
	public double[][][] calcularLogPuntaje(double[][][] matriz){
		
		//double[][][] puntaje = new double[serv][arregloServ[0].length][niveles]; // matriz serv*atributos*niveles
		
		int[][][] puntaje = calcularFuncionH(matriz, arregloServ);
		double[][][] puntaje2 = new double[puntaje.length][puntaje[0].length][puntaje[0][0].length];
		int[] nServ = contarServicios(tipo);
		
		for(int i = 0;i<puntaje.length;i++){
			for(int j = 0;j < puntaje[0].length;j++){
				for(int k = 0;k < puntaje[0][0].length;k++){
					//System.out.println("El puntaje "+i+","+j+","+k+" es "+ puntaje[i][j][k]);
					//System.out.println("El número de servicios de tipo "+i+ " es "+nServ[i]);
					puntaje2[i][j][k] = puntaje[i][j][k];
					puntaje2[i][j][k] /= nServ[i];
					//System.out.println("El puntaje2 "+i+","+j+","+k+" es "+ puntaje2[i][j][k]);
				}
			}
		}
		double[][][] logPuntaje = calcularLog(puntaje2);
		for(int i = 0;i<logPuntaje.length;i++){
			for(int j = 0;j < logPuntaje[0].length;j++){
				for(int k = 0;k < logPuntaje[0][0].length;k++){	
					//System.out.println("El logaritmo del puntaje "+i+","+j+","+k+" es "+logPuntaje[i][j][k]);
				}
			}
		}
		
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
	
	public int[][][] calcularFuncionH(double[][][] matriz, String[][] servicios){
		//Cuento el número de servicios que cumple con las restricción en ese nivel de servicio
		//Entrega una matriz int[][][]
		
		int[][][] resultado = new int[serv][matriz[0].length][niveles];
		
		
		for(int i = 0;i < resultado.length;i++){
			String serv = "serv"+i;
			for(int j = 0; j< servicios.length;j++){
				//System.out.println("Si el tipo "+tipo[j]+" de servicio es "+serv);
				if(tipo[j].equals(serv)){
				for(int k = 0;k < (servicios[0].length-1);k++){
					//System.out.println(i+","+j+","+k);
					for(int l = 0;l < resultado[0][0].length;l++){
						//System.out.println("Si el tipo "+tipo[j]+" de servicio es "+serv+" y "+servicios[j][k+1]+" es mayor que "+matriz[i][k][l]);
						if(Double.parseDouble(servicios[j][k+1])>matriz[i][k][l]){
							//System.out.println("Entro a sumar en resultado "+i+","+k+","+l);
							resultado[i][k][l]++;
						}
					}
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
		//	System.out.println("Se setea tipo "+i+" como tipo "+tipo[i]);
		}
		
	}
	
	public double[][] optimizar(){
		long ti = System.currentTimeMillis();
		NivelesServicio nivel = new NivelesServicio( arregloServ , serv);
		double[][][] matriz = nivel.getNiveles(niveles);
		/*
		 * for(int i = 0; i < matriz.length;i++){
		*	for(int j = 0;j < matriz[0].length;j++){
		*		for(int k = 0;k < matriz[0][0].length;k++){
		*			System.out.println("matriz "+i+","+j+","+ k+" es "+matriz[i][j][k]);
		*		}
		*	}
		}
		*/
		
		
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
			
			System.out.println("Crea las variables");
			
			//Objetivo es maximizar
			model.set(GRB.IntAttr.ModelSense, GRB.MAXIMIZE);
			
			model.set("NumericFocus", "0");
			
			GRBLinExpr obj = new GRBLinExpr();
			
			System.out.println("Seteo la función objetivo");
			
			//Restricciones de calidad
			GRBLinExpr[] r = new GRBLinExpr[restr.length];
			
			System.out.println("Crea el espacio para las restricciones");
			
			//Lado derecho se setea directo
			/*for(int i = 0;i < restr.length;i++){
			*	System.out.println("Entra a crear lado derecho con la restrcción " +restr[i]);
			*	r[i].addConstant(restr[i]);
			}*/
			//Lado izquierdo
			
			//Vector de expresiones lineales para el lado izquierdo de las restricciones
			GRBLinExpr[] lhs = new GRBLinExpr[restr.length];
			
			
			GRBLinExpr[][][] lhs2= new GRBLinExpr[matriz.length][matriz[0].length][matriz[0][0].length];
			GRBLinExpr[][] lhs3 = new GRBLinExpr[matriz.length][matriz[0].length];
			for(int i = 0;i < lhs2.length;i++){
				for(int j = 0;j < lhs2[0].length;j++){
					lhs3[i][j]=new GRBLinExpr();
					lhs[j]=new GRBLinExpr();
				}
			}
			
			System.out.println("Creo el espacio para el lado izquierdo");
			System.out.println("Agrego expresiones a la función objetivo");
			for(int i = 0;i < lhs2.length;i++){
				for(int j = 0;j < lhs2[0].length;j++){
					for(int k = 0;k < lhs2[0][0].length;k++){
						
						GRBLinExpr add = new GRBLinExpr();
						//Agrego el logaritmo del puntaje x X a la función objetivo
						//System.out.println("Comienzo con el atributo "+logPuntaje[i][j][k]);
						add.addTerm(logPuntaje[i][j][k], x[i][j][k]);
						obj.multAdd(1,add);
						
						//System.out.println("Para el lado izquierdo comienzo con el atributo "+logPuntaje[i][j][k]+" y "+x[i][j][k]);
						
						lhs3[i][j].addTerm(1.0d,x[i][j][k]);
						
						//lhs3[i][j].multAdd(1, lhs2[i][j][k]);
						
						lhs[j].addTerm(1, x[i][j][k]);
						
						
					}
					
					model.addConstr(lhs3[i][j],GRB.EQUAL,1,"r"+i+"-"+j);
				}
			}
			
			//model.addConstr(lhs[0], GRB.GREATER_EQUAL, restr[0], "ra"+0);
			model.addConstr(lhs[1], GRB.GREATER_EQUAL, Math.log(restr[1]), "ra"+1);
			model.addConstr(lhs[2], GRB.GREATER_EQUAL, Math.log(restr[2]), "ra"+2);
			model.addConstr(lhs[3], GRB.GREATER_EQUAL, Math.log(restr[3]), "ra"+3);
			model.addConstr(lhs[4], GRB.GREATER_EQUAL, Math.log(restr[4]), "ra"+4);
			model.addConstr(lhs[5], GRB.GREATER_EQUAL, Math.log(restr[5]), "ra"+5);
			model.addConstr(lhs[6], GRB.GREATER_EQUAL, Math.log(restr[6]), "ra"+6);
			//Bmodel.addConstr(lhs[7], GRB.GREATER_EQUAL, Math.log10(restr[7]), "ra"+7);
			
			
			model.update();
			/*
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
				model.addConstr(lhs[i], GRB.LESS_EQUAL, restr[i], "r"+i);
			}
			*/
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
								System.out.println("x "+i+","+j+","+k+" es "+x[i][j][k].get(GRB.DoubleAttr.X));
								resultado[i][j][k] = x[i][j][k].get(GRB.DoubleAttr.X);
							}
						}
					}
				}
			}
			
		} catch (GRBException e) {
			System.out.println("Error code: " + e.getErrorCode() + ". " +
			e.getMessage());
		}
		
		double[][] restricciones = new double[resultado.length][resultado[0].length];
		for(int i = 0;i < resultado.length;i++) {
			for(int j = 0;j < resultado[0].length;j++) {
				for(int k = 0;k < resultado[0][0].length;k++){
					resultado[i][j][k] *= matriz[i][j][k];
					if(resultado[i][j][k]!=0){
						restricciones[i][j]=resultado[i][j][k];
					}
				}
			}
		}
		long tf = System.currentTimeMillis();
		System.out.println("La optimización demoró "+(tf-ti));
		
		return restricciones;
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
	*
	*/
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
}


