package parametros;

public class Parametros {
	// para la descomposición
	public int niveles = 50;
	public int poblacionInicialLocal = 200;
	public int mutacionLocal = 10;
	public double crossoverLocal = 0.35;
	public double evolucionesLocal = 60;
	public int geneticosLocal;
	
	// para la optimización global
	public int poblacionInicialGlobal = 20;
	public int mutacionGlobal = 10;
	public double crossoverGlobal = 0.8;
	public double evolucionesGlobal = 50;
	public int geneticosGlobal;
	
	public Parametros (int niveles, int poblacionInicialLocal,int mutacionLocal, double crossoverLocal, double evolucionesLocal, int geneticosLocal, 
			int poblacionInicialGlobal,int mutacionGlobal, double crossoverGlobal, int evolucionesGlobal, int geneticosGlobal){
		this.niveles=niveles;
		this.poblacionInicialLocal = poblacionInicialLocal;
		this.crossoverLocal = crossoverLocal;
		this.mutacionLocal = mutacionLocal;
		this.evolucionesLocal = evolucionesLocal;
		this.geneticosLocal = geneticosLocal;
		this.poblacionInicialGlobal = poblacionInicialGlobal;
		this.crossoverGlobal = crossoverGlobal;
		this.mutacionGlobal = mutacionGlobal;	
		this.evolucionesGlobal = evolucionesGlobal;
		this.geneticosGlobal = geneticosGlobal;
		
	}
	
	public Parametros (int niveles, int poblacionInicialLocal,int mutacionLocal, double crossoverLocal, double evolucionesLocal, int geneticosLocal){
		
		this.niveles=niveles;
		this.poblacionInicialLocal = poblacionInicialLocal;
		this.crossoverLocal = crossoverLocal;
		this.mutacionLocal = mutacionLocal;
		this.evolucionesLocal = evolucionesLocal;
		this.geneticosLocal = geneticosLocal;
	}
			
	public Parametros(int poblacionInicialGlobal,int mutacionGlobal, double crossoverGlobal, int evolucionesGlobal, int geneticosGlobal){
		this.poblacionInicialGlobal = poblacionInicialGlobal;
		this.crossoverGlobal = crossoverGlobal;
		this.mutacionGlobal = mutacionGlobal;	
		this.evolucionesGlobal = evolucionesGlobal;
		this.geneticosGlobal = geneticosGlobal;
	}
}
