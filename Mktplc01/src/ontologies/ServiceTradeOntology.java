package ontologies;

import jade.content.onto.*;
import jade.content.schema.*;

public class ServiceTradeOntology extends Ontology{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public static final String ONTOLOGY_NAME = "Service-trading.ontology";
	
	//Defino el vocabulario
	
	public static final String SERVICE = "service";
	public static final String SERVICE_NAME = "name";
	public static final String SERVICE_OWNER = "owner";
	
	public static final String COSTS = "costs";
	public static final String COSTS_ITEMS = "item";
	public static final String COSTS_PRICE = "price";
	
	public static final String SELL = "sell";
	public static final String SELL_ITEM = "item";
	
	private static Ontology theInstance = new ServiceTradeOntology();
	
	public static Ontology getInstance(){
		return theInstance;
	}
	
	private ServiceTradeOntology(){
		
		super(ONTOLOGY_NAME, BasicOntology.getInstance());
		
		try{
			
			add(new ConceptSchema(SERVICE), Service.class);
			add(new PredicateSchema(COSTS), Costs.class);
			add(new AgentActionSchema(SELL), Sell.class);
			
			//Estructura de los esquemas
			
			ConceptSchema cs = (ConceptSchema) getSchema(ITEM);
			cs.add(SERVICE_NAME, (PrimitiveSchema) getSchema(BasicOntology.STRING));
			cs.add(SERVICE_OWNER, (PrimitiveSchema) getSchema(BasicOntology.STRING), 0, ObjectSchema.UNLIMITED);
			
			
					
		}
		
		
	
		
}
