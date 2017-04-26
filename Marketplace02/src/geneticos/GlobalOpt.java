package geneticos;

import java.util.ArrayList;

import org.jgap.Chromosome;
import org.jgap.Configuration;
import org.jgap.DefaultFitnessEvaluator;
import org.jgap.FitnessFunction;
import org.jgap.Gene;
import org.jgap.Genotype;
import org.jgap.IChromosome;
import org.jgap.InvalidConfigurationException;
import org.jgap.Population;
import org.jgap.event.EventManager;
import org.jgap.impl.CompositeGene;
import org.jgap.impl.DefaultConfiguration;

public class GlobalOpt {
	
	private Configuration m_config;
	private int maxEvolution = 100;
	private int populationSize = 50; 
	private int serv;
	private double[] param;
	private int[] tipoNodo;
	private double[] restr;
	private int[] iter; 
	private double[] prob;
	private String[][] ofertas;
	
	public GlobalOpt(double[] param, int[] tipoNodo, double[] restr, int[] iter, double[] prob, String[][] ofertas){
		this.setIter(iter);
		this.setParam(param);
		this.setProb(prob);
		this.setRestricciones(restr);
		this.setTipoNodo(tipoNodo);
		this.setOfertas(ofertas);
		this.serv = tipoNodo.length;
	}
	
	public FitnessFunction createFitnessFunction() { 
	    return new GlobalOptFitnessFunction(this.param, this.tipoNodo, this.restr, this.iter, this.prob); 
	}
	
	public Configuration createConfiguration() throws InvalidConfigurationException {
		Configuration config = new DefaultConfiguration();
		config.setEventManager(new EventManager());
	    config.setFitnessEvaluator(new DefaultFitnessEvaluator());
		return config;
		
	}
	
	public IChromosome servOptimos(String[][] ofertas) throws Exception {
		m_config = createConfiguration();
		FitnessFunction ff = createFitnessFunction();
		m_config.setFitnessFunction(ff);
		m_config.setPreservFittestIndividual(true); // Determina si mantener o no a los mejores individuos, Creo que también se llama elitismo	    
		m_config.setPopulationSize(populationSize);
		
		Gene[] sampleGenes = new Gene[serv];
		int[] asigno = this.asignarAleatorio(ofertas);
		int cont = 0;
		while(cont<serv){
			CompositeGene compGene = new CompositeGene();
			Gene[] gen = new Gene[11]; // 9 atributos, precio y si es bundling(nro proveedor)
			for(int j = 0;j < gen.length;j++){
				gen[j].setAllele(ofertas[asigno[cont]][j]); // lugar de la oferta asignada de manera aleatoria para la tarea en la posición "cont"
				compGene.addGene(gen[j]);
			}
			sampleGenes[cont] = compGene;
			cont++;
		}
		IChromosome a = new Chromosome(m_config,sampleGenes);
		m_config.setSampleChromosome(a);
		//Genotype population = Genotype.randomInitialGenotype(conf);
		Genotype population = new Genotype(m_config, new Population(m_config, a));
		
		for (int i = 0; i < this.maxEvolution; i++) { 
			population.evolve(); 
		} 
		
		IChromosome mejorSolucion = population.getFittestChromosome();
		
		return mejorSolucion;
	}
	
	public void setParam(double[] parametrosFU){
		param = new double[parametrosFU.length];
		for(int i = 0;i < parametrosFU.length;i++){
			param[i] = parametrosFU[i];
		}
	}
	
	public void setTipoNodo(int[] tipoNodo){
		this.tipoNodo = new int[tipoNodo.length];
		for(int i = 0;i < tipoNodo.length;i++){
			this.tipoNodo[i] = tipoNodo[i];
		}
	}
	
	public void setRestricciones(double[] restr){
		this.restr = new double[restr.length];
		for(int i = 0;i < restr.length;i++){
			this.restr[i] = restr[i];
		}
	}
	
	public void setIter(int[] iter){
		this.iter = new int[iter.length];
		for(int i = 0;i < iter.length;i++){
			this.iter[i] = iter[i];
		}
	}
	
	public void setProb(double[] prob){
		this.prob = new double[prob.length];
		for(int i = 0;i < prob.length;i++){
			this.prob[i] = prob[i];
		}
	}
	public void setOfertas(String[][] ofertas) {
	    this.ofertas = new String[ofertas.length][ofertas[0].length];
	    for(int i = 0;i < ofertas.length;i++){
		    for(int j = 0;j < ofertas[0].length;j++){
		    	this.ofertas[i][j] = ofertas[i][j];
		    }
	    }
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
	
	public ArrayList ordenarServicios(String[][] ofertas, String serv){ // Genera un arraylist con las posiciones en ofertas para el servicio serv baasado en las ofertas obtenidas.
		ArrayList ofrecidos = new ArrayList();
		for(int i = 0;i < ofertas.length;i++){
			if(serv.equals(ofertas[i][9])){
				ofrecidos.add(i);
			}
		}
		return ofrecidos;
	}
	
	public int[] asignarAleatorio(String[][] ofertas){
		int[] asignar = new int[serv];
		for(int i  = 0;i < serv;i++){
			String nombre = "serv"+i;
			ArrayList list = ordenarServicios(ofertas, nombre);
			int max = list.size();
			double aleatorio = Math.random()*max;
			int lugar = (int) aleatorio;
			asignar[i] = lugar;
		}
		return asignar;
	}

}
