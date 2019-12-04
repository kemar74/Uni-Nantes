package main;

import java.util.*;

public class PreflotAlgorithm extends GraphAlgorithmWithFlow {

	public PreflotAlgorithm(Graph graph) {
		super(graph);
	}
	
	public Graph solve(Node from, Node to) {
		
		this.graph.getNode(from).hauteur = this.graph.nodes.size();
		
		for(Node n : this.capacities.getSuccessors(from)) {
			float edgeValue = this.capacities.getEdge(from, n);
			this.graph.addEdge(from, n, edgeValue);
			this.graph.addEdge(n, from, -edgeValue);
			n.excedent = edgeValue;
			from.excedent -= edgeValue;
			this.graph.getNode(n).excedent = edgeValue;
			this.graph.getNode(from).excedent -= edgeValue;
		}
		
		List<Node> list = new ArrayList<Node>();
		for(Node n : this.graph.nodes) {
			if(!n.equals(from) && !n.equals(to))
				list.add(n);
		}
		
		while(list.size() > 0) {
			Node current = list.get(0);
			list.remove(current);
			int H = current.hauteur;
			decharger(current);
			if(current.hauteur > H) {
				list.add(0, current);
			}
		}
		
		return this.graph;
	}
	public Graph solve(String from, String to) {
		return solve(this.graph.getNode(from), this.graph.getNode(to));
	}
	
	public boolean avancer(Node a, Node b) {
		a = this.graph.getNode(a);
		b = this.graph.getNode(b);
		if(a.excedent <= 0 || a.hauteur != b.hauteur + 1 || this.capacities.getEdge(a, b) - this.graph.getEdge(a, b) <= 0)
			return false;
		float adding = a.excedent;
		if(this.capacities.getEdge(a, b) - this.graph.getEdge(a, b) < adding)
			adding = this.capacities.getEdge(a, b) - this.graph.getEdge(a, b);
		
		this.graph.addEdge(a, b, adding);
		this.graph.addEdge(b, a, -adding);
		this.graph.getNode(a).excedent -= adding;
		this.graph.getNode(b).excedent += adding;
		
		return true;
	}
	
	public boolean elever(Node node) {
		if(node.excedent == 0)
			return false;
		
		int lowestNeighbour = -1;
		for(Node n : this.capacities.getSuccessors(node)) {
			if(node.hauteur <= n.hauteur) {
				if(lowestNeighbour < 0 || lowestNeighbour < n.hauteur)
					lowestNeighbour = n.hauteur;
			}
		}
		if(lowestNeighbour >= 0) {
			node.hauteur = lowestNeighbour + 1;
			return true;
		}
		return false;
	}
	
	public boolean decharger(Node node) {
		boolean continuer = true;
		while(node.excedent > 0 && continuer) {
			for(Node n : this.capacities.getSuccessors(node)) {
				avancer(node, n);
			}
			continuer = elever(node);
		}
		return true;
	}
	

	public void getMinCut(Node from, Node to) {
		for(Node n : this.graph.nodes) {
			if(n.excedent > 0)
				this.Y.add(n);
			else
				this.X.add(n);
		}
	}
	public void getMinCut(String from, String to) {
		this.getMinCut(this.graph.getNode(from), this.graph.getNode(to));
	}
	
	public Float getMaximumFlowCapacity(Node from, Node to, boolean includeSourceAndEnd) {
//		solve(from, to);
		getMinCut(from, to);
		
		float maxCapacity = 0;
		for(Node a : this.X) {
			for(Node b : this.Y) {
				boolean isSourceOrEnd = (a.equals(from) || a.equals(to) || b.equals(from) || b.equals(to));
				if(includeSourceAndEnd)
					isSourceOrEnd = false; // Cette condition ne compte plus
				float capacity = this.capacities.getEdge(a, b);
				float flow = this.graph.getEdge(a, b);
				if(capacity > 0 && flow == capacity && !isSourceOrEnd) {
					maxCapacity += this.capacities.getEdge(b, a);
				}
				float capacity2 = this.capacities.getEdge(b, a);
				float flow2 = this.graph.getEdge(b, a);
				if(capacity2 > 0 && flow2 == capacity2 && !isSourceOrEnd) {
					maxCapacity += this.capacities.getEdge(b, a);
				}
			}
		}
		return maxCapacity;
	}

}
