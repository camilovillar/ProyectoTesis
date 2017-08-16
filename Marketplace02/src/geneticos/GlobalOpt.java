package geneticos;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

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
import org.jgap.impl.BestChromosomesSelector;
import org.jgap.impl.ChromosomePool;
import org.jgap.impl.CompositeGene;
import org.jgap.impl.CrossoverOperator;
import org.jgap.impl.DoubleGene;
import org.jgap.impl.GABreeder;
import org.jgap.impl.IntegerGene;
import org.jgap.impl.MutationOperator;
import org.jgap.impl.StockRandomGenerator;

import ofertas.Ofertas;
import parametros.Parametros;

public class GlobalOpt {
	
	private Configuration m_config;
	private int maxEvolution;
	private int populationSize; 
	private int serv;
	private double[] param;
	private int[] tipoNodo;
	private double[] restr;
	private int[] iter; 
	private double[] prob;
	private String[][] ofertas;
	private int[] bundling;
	private String[] nombresActividades;
	private ArrayList<ArrayList> ofertasOrdenadas;
	private Ofertas ordenarOfertas;
	private Parametros parametros;
	private long ti;
	
	public GlobalOpt(double[] param, int[] tipoNodo, double[] restr, int[] iter, double[] prob, ArrayList<ArrayList> ofertas, int[] bundling, String[] nombres, Parametros p){
		this.setIter(iter);
		this.setParam(param);
		this.setProb(prob);
		this.setRestricciones(restr);
		this.setTipoNodo(tipoNodo);
		this.ofertas = setOfertas(ofertas);
		this.serv = tipoNodo.length;
		this.bundling = bundling;
		this.nombresActividades = nombres;
		ordenarOfertas = new Ofertas(this.ofertas, serv);
		ofertasOrdenadas = ordenarOfertas.buscarOfertas(nombresActividades);
		this.parametros = p;
		this.maxEvolution = (int) parametros.evolucionesGlobal;
		this.populationSize = (int) parametros.poblacionInicialGlobal;
	}
	
	public FitnessFunction createFitnessFunction() { 
	    return new GlobalOptFitnessFunction(this.param, this.tipoNodo, this.restr, this.iter, this.prob, this.ofertas, this.bundling, this.ofertasOrdenadas); 
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
	    config.addGeneticOperator(new MutationOperator(config, (int) parametros.mutacionGlobal)); // rate es 1/numero
		config.addGeneticOperator(new CrossoverOperator(config, (double) parametros.crossoverGlobal)); // xover double para porcentaje
		
	    return config;
	}
	
	public IChromosome servOptimos() throws Exception {
		ti = System.currentTimeMillis();
		m_config = createConfiguration();
		FitnessFunction ff = createFitnessFunction();
		m_config.setFitnessFunction(ff);
		m_config.setPreservFittestIndividual(true); // Determina si mantener o no a los mejores individuos, Creo que también se llama elitismo	    
		m_config.setPopulationSize(populationSize);
		//List geneticOp = m_config.getGeneticOperators();
		//Iterator it = geneticOp.iterator();
		/*while(it.hasNext()){
		*	System.out.println(it.next());
		}*/
		
		IChromosome cromosome = null;
		IChromosome[] cromosomes = new IChromosome[populationSize];
		Population pop = new Population(m_config);
		long tf = System.currentTimeMillis();
		System.out.println("El tiempo de obtener los genes es "+(tf-ti));
		for(int a = 0;a < populationSize;a++){
			int[] asignar = ordenarOfertas.asignarAleatorio(ofertasOrdenadas);
			//int[] asigno = this.asignarAleatorio(ofertas);
			/*for(int x = 0;x < asignar.length;x++){
			*	System.out.println("asigno "+ x+" tiene el valor "+asignar[x]);
			*}
			**/
			int cont = 0;
			Gene[] genes = new Gene[serv];
			while(cont<serv){			
				int max = (((ArrayList) ofertasOrdenadas.get(cont)).size() - 1);
				genes[cont] = new IntegerGene(m_config, 0, max);
				genes[cont].setAllele(asignar[cont]);
				cont++;
			}
			
			Chromosome sampleCromosome= new Chromosome(m_config, genes); 
			cromosomes[a] = sampleCromosome;
			pop.addChromosome(cromosomes[a]);
			cromosome = sampleCromosome;
		
		}
		
		m_config.setSampleChromosome(cromosome);
		//Genotype population = Genotype.randomInitialGenotype(conf);
		Genotype population = new Genotype(m_config, pop);
		IChromosome best;
		
		tf = System.currentTimeMillis();
		System.out.println("El tiempo de configurar la población es "+(tf-ti));
		
		for (int i = 0; i < this.maxEvolution; i++) { 
			
			population.evolve();
			best = population.getFittestChromosome();
	    	System.out.println("El mejor cromosoma en la evolución "+i+" tiene un ajuste de "+best.getFitnessValue());
	    	
	    	System.out.println("La edad del mejor cromosoma es "+best.getAge());
	    	
	    	tf = System.currentTimeMillis();
			System.out.println("El tiempo de una evolución es "+(tf-ti));
		} 
		
		IChromosome mejorSolucion = population.getFittestChromosome();
		
		return mejorSolucion;
	}
	
	public ArrayList obtenerOfertasOrdenadas(){
		return ofertasOrdenadas;
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
	public String[][] setOfertas(ArrayList<ArrayList> ofertas) {
		String[][] ofertas0 = new String[ofertas.size()][14];
		
	    for(int i = 0;i < ofertas0.length;i++){
	    	ArrayList serv = (ArrayList) ofertas.get(i); // obtengo un ArrayList
	    	//System.out.println("El arreglo tiene "+serv.size()+" elementos");
	    	for(int j = 0;j < serv.size();j++){
	    		if(j<9){
	    			double aux = (double) serv.get(j);
	    			ofertas0[i][j] = String.valueOf(aux); // guardo los elementos del arraylist (atributos del serv)
	    		}else{
	    			if(j==11){
	    				int aux = Integer.parseInt((String) serv.get(j));
	    				ofertas0[i][j] = String.valueOf(aux);
	    				//System.out.println("Se guarda el atributo "+j+" del serv "+i +" es "+this.ofertas[i][j]);
	    			}
	    			if(j==12){
	    				double aux2 = (double) serv.get(j);
	    				ofertas0[i][j] = String.valueOf(aux2);
	    				//System.out.println("Se guarda el atributo "+j+" del serv "+i +" es "+this.ofertas[i][j]);
	    			}
	    			if(j==13){
	    				int aux3 = (int) serv.get(j);
	    				ofertas0[i][j] = String.valueOf(aux3);
	    				//System.out.println("Se guarda el atributo "+j+" del serv "+i +" es "+this.ofertas[i][j]);
	    			}
	    			if( j== 10){			
	    				ofertas0[i][j] = (String) serv.get(j);
	    				//System.out.println("Se guarda el atributo "+j+" del serv "+i +" es "+this.ofertas[i][j]);
	    			}
	    			if( j== 9){			
	    				ofertas0[i][j] = (String) serv.get(j);
	    				//System.out.println("Se guarda el atributo "+j+" del serv "+i +" es "+this.ofertas[i][j]);
	    			}
	    		}
	    		//System.out.println("Se guarda el atributo "+j+" del serv "+i +" es "+this.ofertas[i][j]);
	    	}
	    	/*Iterator iter = serv.iterator();
	    	int cont = 0;
	    	while(iter.hasNext()){
	    		
	    	}*/
	    }
	    return ofertas0;
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
				ofrecidos.add(i);
			}
		}
		return ofrecidos;
	}
	
	public int[] asignarAleatorio(String[][] ofertas){
		int[] asignar = new int[serv];
		for(int i  = 0;i < serv;i++){
			String nombre = "serv"+i;
			//System.out.println("Se buscan las ofertas que contengan el servicio "+nombre);
			ArrayList<Integer> list = ordenarServicios(ofertas, nombre);
			int max = list.size();
			System.out.println("Se encontraron "+max+ " ofertas.");
			if(max>0){
				double aleatorio = Math.random()*max;
				int lugar = list.get((int) aleatorio);
				System.out.println("Asigno la oferta "+lugar+" al servicio "+i);
				asignar[i] = lugar;
				
			}
		}
		return asignar;
	}
	
	public int[] asignarAleatorio2(String[][] ofertas){
		int[] asignar = new int[serv];
		for(int a = 0;a < asignar.length;a++){
			asignar[a] = -1;
		}
		for(int i  = 0;i < serv;i++){
			if(asignar[i] == -1){
				String nombre = "serv"+i;
				//System.out.println("Se buscan las ofertas que contengan el servicio "+nombre);
				ArrayList<Integer> list = ordenarServicios(ofertas, nombre);
				int max = list.size();
				//System.out.println("Se encontraron "+max+ " ofertas.");
				if(max>0){ // hay ofertas para el servicio
					double aleatorio = Math.random()*max;
					int lugar = list.get((int) aleatorio);
					ArrayList<Integer> list2 = buscarBundling(ofertas,lugar);
					int max2 = list2.size();
					int[][] bundling = new int[max2][2];
					bundling[i][1] = lugar;
					if(max2>0){ //Si el servicio es parte de un pack
						boolean vof = true;
						for(int j = 0;j < max2;j++){
							String serv = ofertas[list2.get(j)][10];
							String[] aux = serv.split("v");
							int aux2 = Integer.parseInt(aux[1]);
							if(asignar[aux2] != -1 && asignar[aux2] != list2.get(j)){
								vof = false;
							}else{
								bundling[j+1][1]=list.get(j);
							}
						}
						if(vof){
							for(int k = 0;k < bundling.length;k++){
								asignar[i+k]=bundling[i][1];
							}
						}
						
					}else{
						System.out.println("El servicio "+nombre+" no era parte de un pack");
					}
					asignar[i] = lugar;
				}else{
					System.out.println("No hay ofertas para el servicio "+nombre);
				}
			}
		}
		return asignar;
	}
		
	
	public ArrayList<Integer> buscarBundling(String[][] ofertas, int posicion){
		ArrayList<Integer> list = new ArrayList<Integer>();
		for(int i = 0;i < ofertas.length;i++){
			if(posicion!=i && ofertas[posicion][13].equals(ofertas[i][13])){
				list.add(i);
			}
		}
		return list;
	}

}
