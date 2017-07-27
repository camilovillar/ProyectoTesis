package geneticos;

import org.jgap.*;
import org.jgap.event.EventManager;
import org.jgap.impl.BestChromosomesSelector;
import org.jgap.impl.ChromosomePool;
import org.jgap.impl.CrossoverOperator;
import org.jgap.impl.DefaultConfiguration;
import org.jgap.impl.DoubleGene;
import org.jgap.impl.GABreeder;
import org.jgap.impl.IntegerGene;
import org.jgap.impl.MutationOperator;
import org.jgap.impl.NumberGene;
import org.jgap.impl.StockRandomGenerator;

import nivelescalidad.NivelesServicio;

public class RestrLocales{
	
	private Configuration m_config;
	private int maxEvolution = 50;
	private int populationSize = 20;
	private int serv;
	private String[][] arregloServ;
	private double[] param;
	private long tiempo_i;
	private long tiempo_f;
	private double[] util;
	private int[] tipoNodo;
	private int[] iter; 
	private double[] prob; 
	private double[] restr;
	
	
	public RestrLocales(int n, String[][] arregloServ, double[] param, int[] tipoNodo, int[] iter, double[] prob, double[] restr){
		tiempo_i = System.currentTimeMillis();
		
		this.arregloServ = arregloServ;
		//System.out.println("Se setea el arreglo de servicios de dimensiones "+ arregloServ.length +" x "+ arregloServ[0].length);
		/*for(int i = 0;i<arregloServ[0].length;i++){
			System.out.println("La primera línea es : " +arregloServ[0][i]);
		}*/
		this.serv = n;
		this.param = param;
		this.util= new double[arregloServ.length];
		for(int i = 0;i < util.length;i++){
			for(int j = 0;j < 9;j++){
				util[i] += param[j]*Double.parseDouble(arregloServ[i][j+1]);
			}
		}
		this.tipoNodo=tipoNodo;
		this.iter = iter;
		this.prob = prob;
		this.restr = restr;
	}
	
	public FitnessFunction createFitnessFunction(int n, double[][][] arregloNiveles) { // n número de actividades/servicios
	    return new RestrLocalesFFunction(n, arregloServ, param, util, tipoNodo, iter, prob, restr, arregloNiveles); 
	}
	
	public Configuration createConfiguration() throws InvalidConfigurationException {
		Configuration.reset();
		Configuration config = new Configuration();
		config.setEventManager(new EventManager());
		config.setBreeder(new GABreeder());
		config.setRandomGenerator(new StockRandomGenerator());
	    BestChromosomesSelector bestChromsSelector = new BestChromosomesSelector(config, 0.90d);
	    bestChromsSelector.setDoubletteChromosomesAllowed(true);
	    config.addNaturalSelector(bestChromsSelector, false);
	    config.setMinimumPopSizePercent(0);
	    config.setSelectFromPrevGen(1.0d);
	    config.setKeepPopulationSizeConstant(true);
	    config.setFitnessEvaluator(new DefaultFitnessEvaluator());
	    config.setChromosomePool(new ChromosomePool());
	    config.addGeneticOperator(new MutationOperator(config, 2)); // rate es 1/numero
		config.addGeneticOperator(new CrossoverOperator(config, 0.9)); // xover double para porcentaje
		return config;
		
	}
	
	public int getMaxEvolution() {
	    return maxEvolution;
	}
	
	public void setMaxEvolution(final int a_maxEvolution) {
	    maxEvolution = a_maxEvolution;
	}
	

	public void setPopulationSize(final int a_populationSize) { // Debe ser el número de niveles de calidad que se quieren probar
	  populationSize = a_populationSize;
	}
	
	public int getPopulationSize(){
		return populationSize;
	}
	
	public Configuration getConfiguration() {
		return m_config;
	}
	
	public void setServ(int servc){
		serv = servc;
	}
	
	/*public void setNivelesServicio(){
		
	}*/
	
	public IChromosome restrOptimas( int niveles ) throws Exception { // recibe arreglo con todos los servicios y sus atributos [n] x [atributos] 
		tiempo_i = System.currentTimeMillis();
		
		NivelesServicio nivel = new NivelesServicio( arregloServ , serv);
		double[][][] arregloNiveles = nivel.getNiveles(niveles);
		
		
		//System.out.println("recibe el arreglo en algoritmo genético 1");
		m_config = createConfiguration();
		//System.out.println("Pasa la creación de configuración");
	    FitnessFunction myFunc = createFitnessFunction(serv, arregloNiveles);
	    //System.out.println("Se crea la función de ajuste");
	    m_config.setFitnessFunction(myFunc);
	    //System.out.println("Se setea la función de ajuste");
	    m_config.setPreservFittestIndividual(true); // Determina si mantener o no a los mejores individuos, Creo que también se llama elitismo	    
	    //System.out.println("Se setea el elitismo");
	    m_config.setPopulationSize(populationSize);
	    //System.out.println("Se setea el tamaño de la población");
	    
	    //long tf = System.currentTimeMillis();
		//System.out.println("El tiempo de configurar es "+(tf-tiempo_i));
	    
	    
	    //System.out.println("Crea los niveles de servicio aleatorios");
	    
	   // tf = System.currentTimeMillis();
		//System.out.println("El tiempo de establecer niveles de servicio es "+(tf-tiempo_i));
	    
	    int n = serv*9;
	    IChromosome cromosome = null;
	    IChromosome[] cromosomes = new IChromosome[populationSize];
	    Population pop = new Population(m_config);
	    for(int k = 0;k < populationSize;k++){
	    	
	    	double[][][] asignar = nivel.asignarAleatorio(niveles);
	    	
	    	Gene[] gen = new Gene[n];
	    	for (int i = 0; i < serv ; i++) {
	    		for(int j = 0;j < 9; j++){    
	    			gen[((i*9)+j)] = new IntegerGene(m_config, 0, niveles-1);
	    			gen[((i*9)+j)].setApplicationData(asignar[i][j][0]);
	    			
	    			gen[((i*9)+j)].setAllele((int) asignar[i][j][1]);
	    			//System.out.println("Setea el alelo "+i+", "+ j);
	    			//System.out.println("El gen tiene un alelo igual a: "+gen[(i*9)+j].getAllele());
	    		}
	    	}		    
		    
	    	//tf = System.currentTimeMillis();
	 		//System.out.println("El tiempo de crear el cromosoma es "+(tf-tiempo_i));
	 		
	    	Chromosome sampleChromosome = new Chromosome(m_config, gen);
	    	cromosomes[k] = sampleChromosome;
	    	pop.addChromosome(cromosomes[k]);
	    	cromosome = sampleChromosome;
	    }
	    
	    //tiempo_f = System.currentTimeMillis();
		//System.out.println("El problema generó un cromosoma a los (milisegundos) "+ ( tiempo_f - tiempo_i ));
		
	    m_config.setSampleChromosome(cromosome);
	    
	    Genotype population = new Genotype(m_config, pop);
	    IChromosome best = null;
	    
	    //tf = System.currentTimeMillis();
		//System.out.println("El tiempo de establecer niveles de servicio es "+(tf-tiempo_i));
		
	    //Evolution:
	    for (int i = 0; i < getMaxEvolution(); i++) {
	    	  population.evolve();
	    	  best = population.getFittestChromosome();
	    	  System.out.println("El mejor cromosoma en la evolución "+i+" tiene un ajuste de "+best.getFitnessValue());
	    	  long tf = System.currentTimeMillis();
	    	  System.out.println("El tiempo al terminar otra evolución es "+(tf-tiempo_i));
	      }
	    
	    return best;
		
	}
	

}// Cierra clase
