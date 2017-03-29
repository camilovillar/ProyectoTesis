package geneticos;

import org.jgap.BaseChromosome;
import org.jgap.Chromosome;
import org.jgap.Gene;
import org.jgap.InvalidConfigurationException;

public class Test {

	public static void main(String[] args) throws InvalidConfigurationException {
		// TODO Auto-generated method stub
		BaseChromosome sampleChromosome = new Chromosome();
		Gene[] samplegenes = sampleChromosome.getGenes();
		System.out.println(samplegenes.length);
		
	}

}
