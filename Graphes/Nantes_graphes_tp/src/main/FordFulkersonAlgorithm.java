package main;

import java.util.*;

public class FordFulkersonAlgorithm extends GraphAlgorithmWithFlow {
	
	public Set<Node> X;
	public Set<Node> Y;
	
	public FordFulkersonAlgorithm(Graph graph) {
		super(graph);
		this.X = new HashSet<Node>();
		this.Y = new HashSet<Node>();
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
	
	public Float getMaximumFlowCapacity(Node from, Node to, boolean includeSourceAndEnd) {
		solve(from, to);
		
		float maxCapacity = 0;
		for(Node a : this.X) {
			for(Node b : this.Y) {
				boolean isSourceOrEnd = (a.equals(from) || a.equals(to) || b.equals(from) || b.equals(to));
				if(includeSourceAndEnd)
					isSourceOrEnd = false; // Cette condition ne compte plus
				float capacity = this.capacities.getEdge(a, b);
				float flow = this.graph.getEdge(a, b);
				if(capacity > 0 && flow == capacity && !isSourceOrEnd) {
					maxCapacity += this.capacities.getEdge(a, b);
				}
			}
		}
		return maxCapacity;
	}
	public Float getMaximumFlowCapacity(Node from, Node to) {
		return getMaximumFlowCapacity(from, to, false);
	}
	public Float getMaximumFlowCapacity(String from, String to) {
		return getMaximumFlowCapacity(from, to, false);
	}
	public Float getMaximumFlowCapacity(String from, String to, boolean includeSourceAndEnd) {
		return getMaximumFlowCapacity(this.graph.getNode(from), this.graph.getNode(to), includeSourceAndEnd);
	}
	
	public Float getMinimumCapacityOnPath(List<Node> path) {
		Float minimum = new Float(0);
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
	
	public void getMinCut(Node from, Node to) {
		Graph residuel = new Graph(this.graph);
		for(Edge e : residuel.edges) {
			e.weight = this.capacities.getEdge(e.from, e.to) - e.weight;
		}
		List<Node> groupX = new ArrayList<Node>(residuel.connectedNodes(from));
		for(int i = 0; i < residuel.nodes.size(); i++) {
			if(groupX.contains(residuel.getNode(i))) {
				boolean copy = true;
				for(int x = 0; x < this.X.size(); x++)
					if(this.X.toArray()[x].equals(residuel.getNode(i)))
						copy = false;
				if(copy)
					this.X.add(residuel.getNode(i));
			} else {
				boolean copy = true;
				for(int x = 0; x < this.Y.size(); x++)
					if(this.Y.toArray()[x].equals(residuel.getNode(i)))
						copy = false;
				if(copy)
					this.Y.add(residuel.getNode(i));
			}
		}		
	}
	public void getMinCut(String from, String to) {
		this.getMinCut(this.graph.getNode(from), this.graph.getNode(to));
	}

}
