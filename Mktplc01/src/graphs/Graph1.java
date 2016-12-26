package graphs;
import org.graphstream.graph.*;
import org.graphstream.graph.implementations.*;




	public class Graph1 {
		public static void main(String args[]) {
			Graph graph = new SingleGraph("Graph 1");

			graph.addNode("A");
			graph.addNode("B");
			graph.addNode("C");
			graph.addNode("D");
			graph.addNode("E");
			graph.addNode("F");
			graph.addEdge("AB", "A", "B");
			graph.addEdge("BE", "B", "E");
			graph.addEdge("CA", "C", "A");
			graph.addEdge("DA", "D", "A");
			graph.addEdge("EC", "E", "C");
			graph.addEdge("EF", "E", "F");
			
			Node d= graph.getNode("D");
			d.setAttribute("ui.label", "1");
			Node a =graph.getNode("A");
			a.setAttribute("activity", "BuscarAmigos");
			a.addAttribute("ui.label", "2");
			Node b =graph.getNode("B");
			b.setAttribute("activity", "BuscarComida");
			b.addAttribute("ui.label", "3");
			Node c =graph.getNode("C");
			c.setAttribute("activity", "BuscarPub");
			c.addAttribute("ui.label", "4");
			Node e =graph.getNode("E");
			e.setAttribute("activity", "BuscarDescuento");
			e.addAttribute("ui.label", "5");
			Node f= graph.getNode("F");
			f.setAttribute("ui.label", "6");
			
			f.setAttribute("ui.class", "marked");
			

			graph.display();
		}
		protected String styleSheet =
		        "node {" +
		        "	fill-color: black;" +
		        "}" +
		        "node.marked {" +
		        "	fill-color: red;" +
		        "}";
	}
