package graphs;
import org.graphstream.algorithm.generator.Generator;
import org.graphstream.algorithm.generator.RandomGenerator;
import org.graphstream.graph.Graph;
import org.graphstream.graph.implementations.SingleGraph;

public class GraphGenerator {

	public static void main(String[] args) {
		
		Graph graph = new SingleGraph("Random");
	    Generator gen = new RandomGenerator(2);
	    gen.addSink(graph);
	    gen.begin();
	    for(int i=0; i<10; i++)
	        gen.nextEvents();
	    gen.end();
	    graph.display();

	}

}
