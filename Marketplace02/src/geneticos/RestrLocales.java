package geneticos;

import org.jgap.*;
import org.jgap.event.EventManager;
import org.jgap.impl.CrossoverOperator;
import org.jgap.impl.DefaultConfiguration;
import org.jgap.impl.DoubleGene;
import org.jgap.impl.MutationOperator;

import geneticos.NivelesServicio;

public class RestrLocales{
	
	private Configuration m_config;
	private int maxEvolution = 100;
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
	
	public FitnessFunction createFitnessFunction(int n) { // n número de actividades/servicios
	    return new RestrLocalesFFunction(n, arregloServ, param, util, tipoNodo, iter, prob, restr); 
	}
	
	public Configuration createConfiguration() throws InvalidConfigurationException {
		Configuration.reset();
		DefaultConfiguration config = new DefaultConfiguration();
		//BestChromosomesSelector bestChromsSelector = new BestChromosomesSelector(config, 1.0d);
		//bestChromsSelector.setDoubletteChromosomesAllowed(false);
		//config.addNaturalSelector(bestChromsSelector, true);
		//config.setMinimumPopSizePercent(0); // significante para evolución con selectores naturales, los que pueden seleccionar menos cromosomas que la población inicial
	    config.setEventManager(new EventManager());
	   //m_config.setFitnessEvaluator(new DefaultFitnessEvaluator());
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
		//System.out.println("recibe el arreglo en algoritmo genético 1");
		m_config = createConfiguration();
		//System.out.println("Pasa la creación de configuración");
	    FitnessFunction myFunc = createFitnessFunction(serv);
	    //System.out.println("Se crea la función de ajuste");
	    m_config.setFitnessFunction(myFunc);
	    //System.out.println("Se setea la función de ajuste");
	    m_config.setPreservFittestIndividual(true); // Determina si mantener o no a los mejores individuos, Creo que también se llama elitismo	    
	    //System.out.println("Se setea el elitismo");
	    m_config.setPopulationSize(populationSize);
	    //System.out.println("Se setea el tamaño de la población");
	    m_config.addGeneticOperator(new CrossoverOperator(m_config, 20));
	    m_config.addGeneticOperator(new MutationOperator(m_config, 0));
	    
	    NivelesServicio nivel = new NivelesServicio( arregloServ , serv);
	    //System.out.println("Crea los niveles de servicio aleatorios");
	    
	    BaseChromosome sampleChromosome = new Chromosome(m_config);
	    
	    int n = serv*9;
	    IChromosome[] cromosomes = new IChromosome[populationSize];
	    for(int k = 0;k < populationSize;k++){
	    	Gene[][] genes = new Gene[serv][9];
	    	
	    	double[][] asignar = nivel.asignarAleatorio(niveles);
		   // System.out.println("Asigna los niveles de servicio aleatorios");
		    for(int x = 0;x<asignar.length;x++){
		    	for(int y = 0;y < asignar[0].length;y++){
		    		//System.out.println("Asignar " + x + ", "+ y +" tiene el valor "+asignar[x][y]);
		    	}
		    }
	    	
		    /*for (int i = 0; i < serv ; i++) {
	    		for(int j = 0;j < 9; j++){    
	    			System.out.println("Se asignan los valores correctamente.");
	    			genes[i][j].setAllele((double) asignar[i][j]);
	    			System.out.println("Setea el alelo "+i+", "+ j);
	    			System.out.println("El gen tiene un alelo igual a: "+genes[i][j].getAllele());
	    		}
	    	}*/
	    	Gene[] gen = new Gene[n];
	    	for (int i = 0; i < serv ; i++) {
	    		for(int j = 0;j < 9; j++){    
	    			//System.out.println("Se asignan los valores correctamente.");
	    			gen[((i*9)+j)]= new DoubleGene(m_config,0.0,1.0);
	    			gen[((i*9)+j)].setAllele((double) asignar[i][j]);
	    			//System.out.println("Setea el alelo "+(i*9+j));
	    			//System.out.println("El gen tiene un alelo igual a: "+gen[((i*9)+j)].getAllele());
	    		}
	    	}		    
		    
	    	Chromosome samplechromosome = new Chromosome(m_config, gen);
	    	cromosomes[k] = samplechromosome;
	    }
	    
	    tiempo_f = System.currentTimeMillis();
		System.out.println("El problema generó un cromosoma a los (milisegundos) "+ ( tiempo_f - tiempo_i ));
		
	    m_config.setSampleChromosome(cromosomes[0]);
	    Population pop = new Population(m_config, cromosomes);
	    Genotype population = new Genotype(m_config, pop);
	    IChromosome best = null;
	    
	    Evolution:
	      for (int i = 0; i < getMaxEvolution(); i++) {
	    	  population.evolve();
	    	  best = population.getFittestChromosome();
	    	  System.out.println("El mejor cromosoma en esta evolución tiene un ajuste de "+best.getFitnessValue());
	    	  /*for(int j = 0;j < best.getGenes().length;j++){
	    		  System.out.println("El gen "+j+" tiene un valor "+best.getGenes()[j].getAllele());  
	    	  }*/
	      }
	    
	    return best;
		
	}
	

}// Cierra clase
