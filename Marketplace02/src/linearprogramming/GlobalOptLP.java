package linearprogramming;

import java.util.ArrayList;

import gurobi.*;
import gurobi.GRB;
import gurobi.GRBConstr;
import gurobi.GRBEnv;
import gurobi.GRBException;
import gurobi.GRBLinExpr;
import gurobi.GRBModel;
import gurobi.GRBVar;

public class GlobalOptLP {
	
	private int serv;
	private double[] param;
	private int[] tipoNodo;
	private double[] restr;
	private int[] iter; 
	private double[] prob;
	private String[][] ofertas;	
	private int[] bundling; 

	public GlobalOptLP(double[] param, int[] tipoNodo, double[] restr, int[] iter, double[] prob, ArrayList ofertas,int[] bundling, String[] nombres){
		this.serv = tipoNodo.length;
		this.param = param;
		this.tipoNodo = tipoNodo;
		this.iter = iter;
		this.prob = prob;
		this.restr = restr;
		this.setOfertas(ofertas);
		this.bundling=bundling;
	}
		
	public int[] optimizar(){
		
		int[] mejoresServicios = new int[ofertas.length];
		
		try{
			GRBEnv env = new GRBEnv("GlobalOptLP.log");
			GRBModel model = new GRBModel(env);
			
			
			
			//Creo expresiones lineales que corresponden a un atributo del servicio por una variable de decisión correspondiente
			//Las variables para los atributos del servicio se mantiene en un servicio. El atributo j de i tiene la misma variable que el atributo j+1
			//Además creo un término para la función objetivo que pondera el atributo con la utilidad que entrega.
			
			// Creo una variable por cada una de las ofertas que recibo
			GRBVar[] y = new GRBVar[bundling[bundling.length-1]+1];
			
			for(int i = 0;i < y.length;i++){
				y[i] = model.addVar(0, 1, 0, GRB.BINARY, "y"+i);
			}
			System.out.println("Crea las variables");
			model.update();
			
			GRBLinExpr[][] atrVar = new GRBLinExpr[ofertas.length][param.length];
			GRBLinExpr[][] paramAtrVar = new GRBLinExpr[ofertas.length][param.length];
			for(int i = 0; i < ofertas.length;i++){
				for(int j = 0;j <param.length;j++){
					atrVar[i][j] = new GRBLinExpr();
					paramAtrVar[i][j] = new GRBLinExpr();
				}
			}
			
			//double[][] paramAtVar = new double[ofertas.length][param.length];
			//double[][] atVar = new double[ofertas.length][param.length];
			
			for(int i = 0; i < ofertas.length;i++){
				int idOferta = Integer.parseInt(ofertas[i][13]);
				for(int k = 0;k < y.length;k++){
					if(idOferta == k){
						for(int j = 0;j < param.length;j++){					
							//System.out.println("atributo "+j+" de "+i+" es "+ofertas[i][j]);
							//System.out.println("agrego el parámetro "+i+","+j+ofertas[i][j]);
							double atributo = Double.parseDouble(ofertas[i][j]);
							atrVar[i][j].addTerm(atributo,y[k]);
					
							double paramAtr = param[j]*(Double.parseDouble(ofertas[i][j]));
							paramAtrVar[i][j].addTerm(paramAtr, y[k]);
						}
					}
				}
			}
			
			model.update();
			
			/* Creo variables de decisión por cada una de los servicios dentro de las ofertas, de este modo
			 * decido sobre la oferta elegida, y puedo establecer una restricción que permita garantizar la completitud del
			 * pack o bundling elegido
			 */
		
			GRBVar[] x = new GRBVar[ofertas.length];
		
			for(int i = 0;i < x.length;i++){
					x[i] = model.addVar(0, 1, 0, GRB.BINARY, "x"+i);
			}
			
			System.out.println("Crea las variables");
			
			model.update();

			
			
			
			//Objetivo es maximizar
			model.set(GRB.IntAttr.ModelSense, GRB.MAXIMIZE);
			
			model.set("NumericFocus", "0");
			
			
			// Creo el objeto para la función objetivo
			// Corresponde a la suma de la utilidad que entrega cada servicio elegido
			// Debo sumar paramAtrVar de los servicios
			// Al tener la variable, podrá detereminar si se suma o se anula (cero)
			
			GRBLinExpr obj = new GRBLinExpr();
			
			for(int i  = 0;i < ofertas.length;i++){
				for(int j = 0;j < param.length;j++){
					obj.multAdd(1, paramAtrVar[i][j]);
				}
			}
			
			
			
			model.setObjective(obj);
			model.update();
			
			System.out.println("Seteo la función objetivo");
			
			
			
			
			
			// Para las restricciones, será necesario considerar aquellas de tipo de servicio (un servicio por cada tipo requerido)
			// aquellas de bundling, donde se deben completar los bundling ofrecidos por los proveedores
			// aquellas de funciones de agregación y restricciones globales
			
			int[] rhs = new int[y.length];
			int[][] pertenece = new int [x.length][y.length];
			for(int i = 0;i < x.length;i++){ // nro de ofertas
				for(int j = 0;j < y.length;j++){ // nro de packs
					int idOferta = Integer.parseInt(ofertas[i][13]);
					if(idOferta == j){
						pertenece[i][j] = 1;
						rhs[j]++;
					}
				}
			}
			
			
			GRBLinExpr[] restrBundling = new GRBLinExpr[y.length]; //tamaño es el id de bundling del último elemento del arreglo bundling más uno, entendiendo que parte desde cero...
			for(int i = 0;i < restrBundling.length;i++){
				restrBundling[i]= new GRBLinExpr();
			}
			for(int i = 0;i < y.length;i++){
				//System.out.println("El servicio "+i+" corresponde a la oferta "+ofertas[i][13]);
				for(int j = 0;j < ofertas.length;j++){
					//System.out.println("Comparo "+idOferta+" con "+j);
						//System.out.println("Agrego x"+i+" a la restricción "+j);
					restrBundling[i].addTerm(pertenece[j][i],x[j]);
				}
				restrBundling[i].addTerm((-rhs[i]),y[i]);
			}
			
			
			//restrBundling.length
			for(int i = 0;i < 1;i++){
				//System.out.println("Agrego la restricción de bundling "+i);
				//model.addConstr(restrBundling[i], GRB.LESS_EQUAL, rhs[i], "rB0"+i);
			}
			for(int i = 0;i < restrBundling.length;i++){
				//System.out.println("Agrego la restricción de bundling "+i);
				model.addConstr(restrBundling[i], GRB.EQUAL, 0, "rB"+i);
			}
			
			model.update();
				
			
			
			GRBLinExpr[] restrTipo = new GRBLinExpr[serv]; 
			for(int j = 0;j < restrTipo.length;j++){
				restrTipo[j] = new GRBLinExpr();
			}
			String tipo = null;
			for(int i = 0;i < ofertas.length;i++){
				//System.out.println("El tipo del servicio "+i+ " es "+ofertas[i][10]);
				tipo = ofertas[i][10];				
				for(int j = 0;j < serv;j++){
					String nombre = "serv"+j;
					//System.out.println("Comparo "+tipo+" con "+nombre); 
					if(tipo.equals(nombre)){
						//int idOferta = Integer.parseInt(ofertas[i][13]);
						//System.out.println("Agrego x"+i+" a la restricción "+j);
						restrTipo[j].addTerm(1.0, x[i]);
					}
				}
			}
			
			int suma = 1;
			for(int i = 0;i < restrTipo.length;i++){
				System.out.println("Agrego la restricción de tipo "+i);
				model.addConstr(restrTipo[i], GRB.EQUAL, suma, "rT"+i);
			}
			
			GRBLinExpr r0 = this.restrTiempo(x, 0);
			GRBLinExpr r1 = this.restrPorcentaje(x, 1);
			GRBLinExpr r2 = this.restrRendimiento(x);
			GRBLinExpr r3 = this.restrPorcentaje(x, 3);
			GRBLinExpr r4 = this.restrPorcentaje(x, 4);
			GRBLinExpr r5 = this.restrPorcentaje(x, 5);
			GRBLinExpr r6 = this.restrPorcentaje(x, 6);
			GRBLinExpr r7 = this.restrTiempo(x, 7);
			GRBLinExpr r8 = this.restrPorcentaje(x, 8);
			GRBLinExpr rP = this.restrPresupuesto(x);
			
			model.addConstr(r0, GRB.GREATER_EQUAL, restr[0], "rTpo1");
			//model.addConstr(r1, GRB.GREATER_EQUAL, Math.log(restr[1]), "rPor1");
			//model.addConstr(r2, GRB.GREATER_EQUAL, restr[2], "rRen");
			//model.addConstr(r3, GRB.GREATER_EQUAL, Math.log(restr[3]), "rPor2");
			//model.addConstr(r4, GRB.GREATER_EQUAL, Math.log(restr[4]), "rPor3");
			model.addConstr(r5, GRB.GREATER_EQUAL, Math.log(restr[5]), "rPor4");
			//model.addConstr(r6, GRB.GREATER_EQUAL, Math.log(restr[6]), "rPor5");
			model.addConstr(r7, GRB.GREATER_EQUAL, restr[7], "rTpo2");
			model.addConstr(r8, GRB.GREATER_EQUAL, Math.log(restr[8]), "rPor6");
			model.addConstr(rP, GRB.LESS_EQUAL, restr[9], "rP");
			
			
			
			
			model.update();
			//System.out.println(restrTipo[0].toString());
			//GRBConstr a = model.getConstrByName("rT"+0);
			//System.out.println(a);
			
			//model.addConstr(x[0], GRB.EQUAL, 0, "r0");
			
			
			
			
			
			
			
			
			System.out.println("Seteo las restricciones");
			
			model.update();
			
			
			model.write("debug.lp");
			
			model.optimize();
			
			
			for(int i = 0;i < x.length; i++){
				System.out.println(x[i].get(GRB.DoubleAttr.X));
				if(x[i].get(GRB.DoubleAttr.X) > 0.5){
					mejoresServicios[i] = 1;
					
				}
			}
			
			model.dispose();
		    env.dispose();
			
			
		}catch (GRBException e) {
			System.out.println("Error code: " + e.getErrorCode() + ". " +
			e.getMessage());
			e.printStackTrace();
		}	
		
		return mejoresServicios;
	}
	
	public void setOfertas(ArrayList ofertas) {
		this.ofertas = new String[ofertas.size()][14];
		
	    for(int i = 0;i < this.ofertas.length;i++){
	    	ArrayList serv = (ArrayList) ofertas.get(i); // obtengo un ArrayList
	    	//System.out.println("El arreglo tiene "+serv.size()+" elementos");
	    	for(int j = 0;j < serv.size();j++){
	    		if(j<9){
	    			double aux = (double) serv.get(j);
	    			this.ofertas[i][j] = String.valueOf(aux); // guardo los elementos del arraylist (atributos del serv)
	    		}else{
	    			if(j==11){
	    				int aux = Integer.parseInt((String) serv.get(j));
	    				this.ofertas[i][j] = String.valueOf(aux);
	    				//System.out.println("Se guarda el atributo "+j+" del serv "+i +" es "+this.ofertas[i][j]);
	    			}
	    			if(j==12){
	    				double aux2 = (double) serv.get(j);
	    				this.ofertas[i][j] = String.valueOf(aux2);
	    				//System.out.println("Se guarda el atributo "+j+" del serv "+i +" es "+this.ofertas[i][j]);
	    			}
	    			if(j==13){
	    				int aux3 = (int) serv.get(j);
	    				this.ofertas[i][j] = String.valueOf(aux3);
	    				//System.out.println("Se guarda el atributo "+j+" del serv "+i +" es "+this.ofertas[i][j]);
	    			}
	    			if( j== 10){			
	    				this.ofertas[i][j] = (String) serv.get(j);
	    				//System.out.println("Se guarda el atributo "+j+" del serv "+i +" es "+this.ofertas[i][j]);
	    			}
	    			if( j== 9){			
	    				this.ofertas[i][j] = (String) serv.get(j);
	    				//System.out.println("Se guarda el atributo "+j+" del serv "+i +" es "+this.ofertas[i][j]);
	    			}
	    		}
	    	}
	    }
	}
	
	public String[][] getOfertas(){
		return ofertas;
	}
	
	public GRBLinExpr restrPresupuesto(GRBVar[] x){
		GRBLinExpr lhs = new GRBLinExpr();
		for(int i = 0;i < ofertas.length;i++){
			double precio = Double.parseDouble(ofertas[i][12]);
			lhs.addTerm(precio, x[i]);
		}
		
		return lhs;
	}
	
	
	public GRBLinExpr restrTiempo(GRBVar[] x, int j){
		GRBLinExpr lhs = new GRBLinExpr();
		
		double agregado0=0.0;
		int tipoAnt = 0; // Para guardar el tipo de nodo de la corrida anterior.
		int inicio4 = 0;
		double maxTpo = 0.0; // Para guardar el máximo de tiempo de respuesta de un conjunto de nodos
		
		for(int i = 0;i < tipoNodo.length;i++){
			
			switch(tipoNodo[i]){
			case 1: // secuencia
				double agregar = Double.parseDouble(ofertas[i][j]);
				lhs.addTerm(agregar, x[i]);
				tipoAnt = 1;
				break;
			case 2: // sec con iter
				double agregar2 = Double.parseDouble(ofertas[i][j]);
				agregado0 += iter[i]*agregar2;
				lhs.addTerm(agregado0, x[i]);
				tipoAnt = 2;
				break;
			case 3: // paralelo
				double agregar3 = Double.parseDouble(ofertas[i][j]);
				if(tipoAnt == 3){ // seteo lo valores máx y mín
					if(agregar3 > maxTpo){ maxTpo=agregar3;}
				}else{
					maxTpo=agregar3;	
				}
				if(i+1<serv){
					agregado0 = 0.0;
					if(tipoNodo[i+1] != 3){ // si el siguiente es del mismo tipo, entonces no hago nada en los valores (0,3 y 5), porque se deberá compara
						agregado0 += maxTpo;
						lhs.addTerm(agregado0, x[i]);
					}
				}else{
					agregado0 += maxTpo;
					lhs.addTerm(agregado0, x[i]);
				}
				
				tipoAnt = 3;
				break;
			case 4: // branch
				if(tipoAnt != 4){// el primero de tipo 4 marca el inicio de los nodos en branch
					inicio4 = i;
				}
				double agregar4 = Double.parseDouble(ofertas[i][j]);
				if((i+1)<5){
					if(tipoNodo[i+1] != 4){// si es el último de tipo 4 en la serie...
						for(int k = 0;k < (i - inicio4 + 1);k++){
							agregado0 += prob[inicio4+k]*agregar4;
							lhs.addTerm(agregado0, x[i]);
						}
					}
				}else{//Si no hay i+1 entonces hago lo mismo
					for(int k = 0;k < (i - inicio4 + 1);k++){
						agregado0 += prob[inicio4+k]*agregar4;
						lhs.addTerm(agregado0, x[i]);
					}
				}
				tipoAnt = 4;
				break;
			}
			
			
			
		}
		
		return lhs;
	}
	
	public GRBLinExpr restrPorcentaje(GRBVar[] x, int j){
		GRBLinExpr lhs = new GRBLinExpr();
		double agregado1=1.0;
		int tipoAnt = 0; // Para guardar el tipo de nodo de la corrida anterior.
		int inicio4 = 0;
		
		for(int i = 0;i < tipoNodo.length;i++){
			
			switch(tipoNodo[i]){
			case 1: // secuencia
				double agregar = Math.log(Double.parseDouble(ofertas[i][j]));
				//agregado1 += Integer.parseInt(ofertas[i][j]);
				lhs.addTerm(agregar, x[i]);
				tipoAnt = 1;
				break;
			case 2: // sec con iter
				double agregar2 = Math.log(Double.parseDouble(ofertas[i][j]));
				agregar2 *= iter[i];
				//agregado1 *= Math.pow(data[i][j],iter[i]);
				lhs.addTerm(agregar2, x[i]);
				tipoAnt = 2;
				break;
			case 3: // paralelo
				double agregar3 = Math.log(Double.parseDouble(ofertas[i][j]));
				if(i+1<serv){
					if(tipoNodo[i+1] == 3){ // si el siguiente es del mismo tipo, entonces no hago nada en los valores (0,3 y 5), porque se deberá compara
					//agregado0 += data[i][0];
					//agregado1 *= data[i][j];
						lhs.addTerm(agregar3, x[i]);
					}else{ // sino
						lhs.addTerm(agregar3, x[i]);
					//agregado1 *= data[i][j];
					}
				}else{
					lhs.addTerm(agregar3, x[i]);
					//agregado1 *= data[i][j];

				}
				
				tipoAnt = 3;
				break;
			case 4: // branch
				if(tipoAnt != 4){// el primero de tipo 4 marca el inicio de los nodos en branch
					inicio4 = i;
				}
				double agregar4 = Math.log(Double.parseDouble(ofertas[i][j]));
				if((i+1)<5){
					if(tipoNodo[i+1] != 4){// si es el último de tipo 4 en la serie...
						double r1 = 0.0;
					
						for(int k = 0;k < (i - inicio4 + 1);k++){
						
							r1 += Math.log(prob[inicio4+k]);
							r1 += agregar4;
						}
						agregado1 *= r1;
						lhs.addTerm(agregado1 , x[i]);

					}
				}else{//Si no hay i+1 entonces hago lo mismo
					double r1 = 0.0;

					for(int k = 0;k < (i - inicio4 + 1);k++){
						agregar4 = Math.log(Double.parseDouble(ofertas[i][j]));
						r1 += Math.log(prob[inicio4+k]);
						r1 += agregar4;

					}
					agregado1 *= r1;
					lhs.addTerm(agregado1, x[i]);
				}
				tipoAnt = 4;
				break;
			}	
		}
		
		
		
		return lhs;
	}
	
	public GRBLinExpr restrRendimiento(GRBVar[] x){
		GRBLinExpr lhs = new GRBLinExpr();
		
		double agregado2=0.0;
		int tipoAnt = 0; // Para guardar el tipo de nodo de la corrida anterior.
		int inicio4 = 0;
		double minRend = 0.0; // Para guardar el mínimo de rendimiento de un conjunto de nodos
		double maxTpo = 0.0; // Para guardar el máximo de tiempo de respuesta de un conjunto de nodos
		double maxLat = 0.0; // Para guardar el máximo de latencia de un conjunto de nodos
		
		for(int i = 0;i < tipoNodo.length;i++){
			
			switch(tipoNodo[i]){
			case 1: // secuencia
				double agregar = Double.parseDouble(ofertas[i][2]);
				lhs.addTerm(agregar, x[i]);
				//agregado2 += Double.parseDouble(ofertas[i][2]);
				tipoAnt = 1;
				break;
			case 2: // sec con iter
				double agregar2 = Double.parseDouble(ofertas[i][2]);
				agregar2*=iter[i];
				lhs.addTerm(agregar2, x[i]);
				//agregado2 += iter[i]*data[i][2];
				tipoAnt = 2;
				break;
			case 3: // paralelo
				
				double agregar3 = Double.parseDouble(ofertas[i][2]);
				if(tipoAnt == 3){ // seteo lo valores máx y mín
					//if(data[i][0] > maxTpo){ maxTpo=data[i][0];}
					if(agregar3 < minRend){ minRend=agregar3;}
					//if(data[i][7] > maxLat){ maxLat=data[i][7];}
				}else{
					//maxLat=data[i][0];
					minRend=agregar3;
					//maxTpo=data[i][7];
					
				}
				if(i+1<serv){
					agregado2=0.0;
					if(tipoNodo[i+1] != 3){ // si el siguiente es del mismo tipo

						agregado2 += minRend;
						lhs.addTerm(agregado2, x[i]);
					}
				}else{
					agregado2 += minRend;
					lhs.addTerm(agregado2, x[i]);
				}
				
				tipoAnt = 3;
				break;
			case 4: // branch
				
				if(tipoAnt != 4){// el primero de tipo 4 marca el inicio de los nodos en branch
					inicio4 = i;
				}
				if((i+1)<5){
					if(tipoNodo[i+1] != 4){// si es el último de tipo 4 en la serie...
						agregado2=0.0;
						for(int j = 0;j < (i - inicio4 + 1);j++){
							double agregar4 = Double.parseDouble(ofertas[i][2]);
							agregado2 += prob[inicio4+j]*agregar4;
						}
						lhs.addTerm(agregado2, x[i]);
					}
				}else{//Si no hay i+1 entonces hago lo mismo
					agregado2=0.0;
					for(int j = 0;j < (i - inicio4 + 1);j++){
						double agregar4 = Double.parseDouble(ofertas[i][2]);
						agregado2 += prob[inicio4+j]*agregar4;
					}
					lhs.addTerm(agregado2, x[i]);
				}
				tipoAnt = 4;
				break;
			}		
		}
		
		
		return lhs;
	}
	
	
}
