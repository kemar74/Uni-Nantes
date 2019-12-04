package main;

import java.util.*;

public class FordFulkersonAlgorithm extends GraphAlgorithmWithFlow {
	
	
	public FordFulkersonAlgorithm(Graph graph) {
		super(graph);
	}

	@Override
	public Graph solve(Node from, Node to) {
		List<Node> path = null;
		while((path = chaineAmeliorante(from, to)) != null && getMinimumCapacityOnPath(path) > 0) {
			Float minimum = getMinimumCapacityOnPath(path);
			addFlowToGraph(path, minimum);
			Collections.reverse(path);
			path.remove(0);
			path.remove(path.size() - 1);
			addFlowToGraph(path, -minimum);
		}
		this.getMinCut(from, to);
		return this.graph;
	}
	public Graph solve(String from, String to) {
		return solve(this.graph.getNode(from), this.graph.getNode(to));
	}
	
	
	public Float getMinimumCapacityOnPath(List<Node> path) {
		float minimum = 0;
		for(int i = 0; i < path.size() - 1; i++) {
			if(minimum == 0 || minimum > this.capacities.getEdge(path.get(i), path.get(i + 1)) - this.graph.getEdge(path.get(i), path.get(i + 1)))
				minimum = this.capacities.getEdge(path.get(i), path.get(i + 1)) - this.graph.getEdge(path.get(i), path.get(i + 1));
		}
		return minimum;
	}
	
	public Graph addFlowToGraph(List<Node> path, Float adding) {
		// Ajout de flot dans le graphe
		for(int i = 0; i < path.size() - 1; i++)
			this.graph.addEdge(path.get(i), path.get(i + 1), adding);
		return this.graph;
	}
	

}
