package linearprogramming;

import gurobi.*;

public class RestriccionesNoLineales {
	
	
	
	
	
	/*
	 * para las restricciones en la selecci�n global
	 */
	
	public RestriccionesNoLineales(GRBVar[] x, int[] tipoNodo, double[] restr, int[] iter, double[] prob, String[][] ofertas){
		
		
		
		
	}
	
	
	
	
	/*
	 * para las restricciones en la descomposici�n de restricciones
	 * 
	 */
	public RestriccionesNoLineales(GRBVar[][] x, int[] tipoNodo, double[] restr, int[] iter, double[] prob, String[][] ofertas){
		
		
		
		
	}
	
	public GRBLinExpr restrTiempo(){
		GRBLinExpr restrTiempo = new GRBLinExpr();
		
		
		
		
		
		return restrTiempo;
	}
	
}
