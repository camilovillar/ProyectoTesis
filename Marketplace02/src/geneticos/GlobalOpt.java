package geneticos;

import java.util.ArrayList;
import java.util.Iterator;

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
import org.jgap.impl.DoubleGene;

import jade.core.AID;

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
	
	public GlobalOpt(double[] param, int[] tipoNodo, double[] restr, int[] iter, double[] prob, ArrayList ofertas){
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
		Configuration.reset();
		Configuration config = new DefaultConfiguration();
		config.setEventManager(new EventManager());
	    //config.setFitnessEvaluator(new DefaultFitnessEvaluator());
		return config;
		
	}
	
	public IChromosome servOptimos() throws Exception {
		m_config = createConfiguration();
		FitnessFunction ff = createFitnessFunction();
		m_config.setFitnessFunction(ff);
		m_config.setPreservFittestIndividual(true); // Determina si mantener o no a los mejores individuos, Creo que también se llama elitismo	    
		m_config.setPopulationSize(populationSize);
		
		Gene[] sampleGenes = new Gene[serv];
		System.out.println("Ofertas:");
		for(int i = 0 ; i < ofertas.length;i++){
			System.out.println(ofertas[i]);
		}
		int[] asigno = this.asignarAleatorio(ofertas);
		for(int i = 0;i<asigno.length;i++){
			System.out.println("Asigno tiene el valor "+asigno[i]);
		}
		int cont = 0;
		while(cont<serv){
			CompositeGene compGene = new CompositeGene(m_config);// 9 atributos, precio
			Gene[] gen = new Gene[10]; 
			for(int j = 0;j < 9;j++){
				gen[j] = new DoubleGene(m_config,0.0,1.0);
				System.out.println("Se setea el alelo "+j+" con el valor "+ofertas[asigno[cont]][j]);
				gen[j].setAllele(Double.parseDouble(ofertas[asigno[cont]][j])); // lugar de la oferta asignada de manera aleatoria para la tarea en la posición "cont"
				compGene.addGene(gen[j]);
			}
			
			gen[9]=new DoubleGene(m_config,0.0,5.0);// se setea el precio que puede estar entre 0 y 5 
			gen[9].setAllele(Double.parseDouble(ofertas[asigno[cont]][12]));
			System.out.println("Se setea el precio del gen "+cont+" con el valor "+ofertas[cont][12]);
			compGene.addGene(gen[9]);
			// id gen queda como AID proveedor + nombre + id(serv)
			compGene.setApplicationData(ofertas[asigno[cont]][9]+"_"+ofertas[asigno[cont]][10]+"_"+ofertas[asigno[cont]][11]);
			//compGene.setUniqueIDTemplate(ofertas[asigno[cont]][9]+"_"+ofertas[asigno[cont]][10]+"_", Integer.parseInt(ofertas[asigno[cont]][11]));
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
	    		System.out.println("Se guarda el atributo "+j+" del serv "+i +" es "+this.ofertas[i][j]);
	    	}
	    	/*Iterator iter = serv.iterator();
	    	int cont = 0;
	    	while(iter.hasNext()){
	    		
	    	}*/
	    }
	}
	
	public String[] obtenerNombre(ArrayList ofertas){
		String[] aux = new String[ofertas.size()]; 
		for(int i = 0;i < ofertas.size();i++){
	    	ArrayList serv1 = (ArrayList) ofertas.get(i); // obtengo un ArrayList
	    	//System.out.println("El arreglo tiene "+serv.size()+" elementos");
	    	//for(int j = 0;j < serv1.size();j++){
	    	
	    	aux[i] = (String) serv1.get(9);
	    	//}
		}
		return aux;
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
	
	public ArrayList<Integer> ordenarServicios(String[][] ofertas, String serv){ // Genera un arraylist con las posiciones en ofertas para el servicio serv baasado en las ofertas obtenidas.
		ArrayList<Integer> ofrecidos = new ArrayList<Integer>();
		for(int i = 0;i < ofertas.length;i++){
			if(serv.equals(ofertas[i][10])){
				System.out.println("Si el nombre del servicio "+ ofertas[i][10]+ " es igual a "+serv);
				ofrecidos.add(i);
			}
		}
		return ofrecidos;
	}
	
	public int[] asignarAleatorio(String[][] ofertas){
		int[] asignar = new int[serv];
		for(int i  = 0;i < serv;i++){
			String nombre = "serv"+i;
			System.out.println("Se buscan las ofertas que contengan el servicio "+nombre);
			ArrayList<Integer> list = ordenarServicios(ofertas, nombre);
			
			int max = list.size();
			System.out.println("Se encontraron "+max+ " ofertas.");
			if(max>0){
				double aleatorio = Math.random()*max;
				int lugar = list.get((int) aleatorio);
				asignar[i] = lugar;
			}
		}
		return asignar;
	}

}
