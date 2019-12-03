package main;

import java.util.*;

abstract public class GraphAlgorithmWithFlow extends GraphAlgorithm {
	
	public Graph capacities;
	public GraphAlgorithmWithFlow(Graph graph) {
		super(graph);
		this.capacities = new Graph(graph);
		this.graph.removeAllConnections();
	}

	public List<Node> chaineAmeliorante(String from, String to) {
		return this.chaineAmeliorante(this.graph.getNode(from), this.graph.getNode(to));
	}
	public List<Node> chaineAmeliorante(Node from, Node to) {
		List<Node> marked = new ArrayList<Node>();
		List<Node> path = new ArrayList<Node>();
		List<Node> Z = new ArrayList<Node>();
		List<Edge> myPath = new ArrayList<Edge>();
		
		marked.add(from);
		Z.add(from);
		while(!Z.isEmpty() && !marked.contains(to)) {
			Node current = Z.get(0);
			Z.remove(0);

			for(Node n : this.capacities.getSuccessors(current)) {
				if(!marked.contains(n) && this.graph.getEdge(current, n) < this.capacities.getEdge(current, n)) {
					marked.add(n);
					Z.add(n);
					myPath.add(new Edge(current, n));
				}
			}
			for(Node n : this.capacities.getPredecessors(current)) {
				if(!marked.contains(n) && this.graph.getEdge(n, current) > 0) {
					marked.add(n);
					Z.add(n);
					myPath.add(new Edge(current, n));
				}
			}
		}
		if(marked.contains(to)) {
			Node lookingFor = to;
			path.add(to);
			while(path.get(path.size() - 1) != from) {
				for(int i = 0; i < myPath.size(); i++) {
					if(myPath.get(i).to.equals(lookingFor)) {
						path.add(myPath.get(i).from);
						lookingFor = myPath.get(i).from;
					}
				}
			}
			Collections.reverse(path);
		}
		return path;
	}

}
