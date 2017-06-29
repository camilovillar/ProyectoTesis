package linearprogramming;

import java.util.ArrayList;

import gurobi.GRB;
import gurobi.GRBEnv;
import gurobi.GRBException;
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

	public GlobalOptLP(double[] param, int[] tipoNodo, double[] restr, int[] iter, double[] prob, ArrayList ofertas){
		this.param = param;
		this.tipoNodo = tipoNodo;
		this.iter = iter;
		this.prob = prob;
		this.restr = restr;
		this.setOfertas(ofertas);
	}
		
	
	
	
		
		
		
		
		
	public void optimizar(){	
		try{
			GRBEnv env = new GRBEnv("GlobalOptLP.log");
			GRBModel model = new GRBModel(env);
			GRBVar[] x = new GRBVar[ofertas.length];
			for(int i = 0;i < ofertas.length;i++){
				x[i] = model.addVar(0, 1, 0, GRB.BINARY, "x"+i);
			}
			
			
			
			
			
			
			
		}catch (GRBException e) {
			System.out.println("Error code: " + e.getErrorCode() + ". " +
			e.getMessage());
			e.printStackTrace();
		}	
	}
	
	public void setOfertas(ArrayList ofertas) {
		this.ofertas = new String[ofertas.size()][13];
		
	    for(int i = 0;i < this.ofertas.length;i++){
	    	ArrayList serv = (ArrayList) ofertas.get(i); // obtengo un ArrayList
	    	System.out.println("El arreglo tiene "+serv.size()+" elementos");
	    	for(int j = 0;j < serv.size();j++){
	    		if(j<9){
	    			double aux = (double) serv.get(j);
	    			this.ofertas[i][j] = String.valueOf(aux); // guardo los elementos del arraylist (atributos del serv)
	    		}else{
	    			if(j==11){
	    				int aux = Integer.parseInt((String) serv.get(j));
	    				this.ofertas[i][j] = String.valueOf(aux);
	    				System.out.println("Se guarda el atributo "+j+" del serv "+i +" es "+this.ofertas[i][j]);
	    			}else{
	    				if(j==12){
	    					double aux = (double) serv.get(j);
	    					this.ofertas[i][j] = String.valueOf(aux);
	    					System.out.println("Se guarda el atributo "+j+" del serv "+i +" es "+this.ofertas[i][j]);
	    				}else{
	    					this.ofertas[i][j] = (String) serv.get(j);
	    					System.out.println("Se guarda el atributo "+j+" del serv "+i +" es "+this.ofertas[i][j]);
	    				}
	    			}
	    		}
	    	}
	    }
	}
}
