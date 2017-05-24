package funciones;

public class FuncionUtilidad {
	
	private double[] parametros = new double[9];
	private double descuento;
	private double probBundling;
	private int nroBundling;
	
	public FuncionUtilidad(int tipo){// tipo indica si el agente es consumidor o proveedor.
		setParametros(tipo);
	}
	
	public void setParametros(int tipo){
		double suma;
		int numero = 9; // número de atributos de calidad
		double media = (double) 1/(numero+2);
		double sd = (double) 1/(2*numero);
		switch(tipo){
		case 1:
			while(parametros[8]<=0){
				suma=0;
			for(int i = 0;i < numero;i++){
				if(i==(numero-1)){
					double y = 0;
					y = 1-suma;
					parametros[i] = y;
					//System.out.println("El parametro "+i+ " se setea a "+parametros[i]);
					suma+=y;
				}else{
					double x = Math.random();
					double y = x*sd + media;
					parametros[i] = y;
					//System.out.println("El parametro "+i+ " se setea a "+parametros[i]);
					suma+=y;
				}
			}
			}
			descuento = 0.0;
			probBundling = 0.0;
			nroBundling = 0;
			//System.out.println("Los parametros de la funcion son: descuento "+ descuento + " prob " + probBundling + " nro "+nroBundling);
			
			break;
		case 2:		
			while(parametros[8]<=0){
				suma=0;
			for(int i =0; i < numero ; i++){
				if(i==(numero-1)){
					double y = 0;
					y = 1-suma;
					parametros[i] = y;
					//System.out.println("El parametro "+i+ " se setea a "+parametros[i]);
					suma+=y;
				}else{
					double x = Math.random();
					double y = x*sd + media;
					parametros[i] = y;
					//System.out.println("El parametro "+i+ " se setea a "+parametros[i]);
					suma+=y;
				}
			}
			}
			descuento = (Math.random()*0.6)+0.1;
			probBundling = Math.random();
			nroBundling = (int) (Math.random()*4)+2;
			//System.out.println("Los parametros de la funcion son: descuento "+ descuento + " prob " + probBundling + " nro "+nroBundling);
			break;
		}
		
	}
	
	public double[] getParametros(){
		return parametros;
	}
	
	public double getDescuento(){
		return descuento;
	}
	
	public double getProbBundling(){
		return probBundling;
	}
	
	public int getNroBundling(){
		return nroBundling;
	}
	
	
	
}
