package geneticos;

import org.jgap.*;
import org.jgap.event.EventManager;
import org.jgap.impl.DefaultConfiguration;

import geneticos.NivelesServicio;

public class RestrLocales{
	
	private Configuration m_config;
	private int maxEvolution = 100;
	private int populationSize = 50; 
	private int serv = 20;
	
	public FitnessFunction createFitnessFunction(int n) { // n número de actividades/servicios
	    return new RestrLocalesFFunction(n); 
	}
	
	public Configuration createConfiguration() throws InvalidConfigurationException {
		
		Configuration config = new DefaultConfiguration();
		//BestChromosomesSelector bestChromsSelector = new BestChromosomesSelector(config, 1.0d);
		//bestChromsSelector.setDoubletteChromosomesAllowed(false);
		//config.addNaturalSelector(bestChromsSelector, true);
		//config.setMinimumPopSizePercent(0); // significante para evolución con selectores naturales, los que pueden seleccionar menos cromosomas que la población inicial
	    config.setEventManager(new EventManager());
	    config.setFitnessEvaluator(new DefaultFitnessEvaluator());
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
	
	public IChromosome restrOptimas( String arregloServ[][], int niveles ) throws Exception { // recibe arreglo con todos los servicios y sus atributos [n] x [atributos] 
		
		m_config = createConfiguration();
	    FitnessFunction myFunc = createFitnessFunction(serv);
	    m_config.setFitnessFunction(myFunc);
	    m_config.setPreservFittestIndividual(true); // Determina si mantener o no a los mejores individuos, Creo que también se llama elitismo	    
	    m_config.setPopulationSize(populationSize);
	    
	    NivelesServicio nivel = new NivelesServicio( arregloServ , serv);
	    double[][] asignar = nivel.asignarAleatorio(niveles);
	    
	    BaseChromosome sampleChromosome = new Chromosome();
	    m_config.setSampleChromosome(sampleChromosome);
	    int n = serv*9;
	    IChromosome[] cromosomes = new IChromosome[populationSize];
	    
	    for(int k = 0;k < populationSize;k++){
	    	Gene[] genes = new Gene[n];
	    	
	    	for (int i = 0; i < serv ; i++) {
	    		for(int j = 0;j < 9; j++){
	    			genes[(i*9) + j].setAllele(asignar[i][j]);
	    		}
	    	}
	    	Chromosome samplechromosome = new Chromosome(m_config, genes);
	    	cromosomes[k] = samplechromosome;
	    }
	    
	    Genotype population = new Genotype(m_config, new Population(m_config, cromosomes));
	    IChromosome best = null;
	    
	    Evolution:
	      for (int i = 0; i < getMaxEvolution(); i++) {
	    	  population.evolve();
	    	  best = population.getFittestChromosome();
	      }
	    
	    return best;
		
	}
	

}// Cierra clase
