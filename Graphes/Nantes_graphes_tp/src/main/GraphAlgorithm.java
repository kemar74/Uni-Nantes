package main;

public abstract class GraphAlgorithm {

	public Graph graph;
	public GraphAlgorithm(Graph graph) {
		this.graph = new Graph(graph);
	}

	abstract public Graph solve(String a, String b);
	abstract public Graph solve(Node a, Node b);

}
