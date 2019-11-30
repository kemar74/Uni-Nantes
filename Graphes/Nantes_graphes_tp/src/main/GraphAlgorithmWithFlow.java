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
		return this.chaineAmeliorante(this.graph.getNode(from), this.graph.getNode(to), 0);
	}
	public List<Node> chaineAmeliorante(String from, String to, int minNodes) {
		return this.chaineAmeliorante(this.graph.getNode(from), this.graph.getNode(to), minNodes);
	}
	public List<Node> chaineAmeliorante(Node from, Node to) {
		return chaineAmeliorante(from, to, 0);
	}
	public List<Node> chaineAmeliorante(Node from, Node to, int minNodes) {
		List<Node> marked = new ArrayList<Node>();
		List<Node> path = new ArrayList<Node>();
		List<Node> Z = new ArrayList<Node>();
		List<Edge> myPath = new ArrayList<Edge>();
		
		marked.add(from);
		Z.add(from);
		int depth = 0;
		while(!Z.isEmpty() && !marked.contains(to)) {
			Node current = Z.get(0);
			Z.remove(0);

			for(Node n : this.capacities.getSuccessors(current)) {
				if(n.equals(to) && depth > 0 && depth < minNodes) {	// Petit ajout pour avoir des chaines de taille >= minNodes
//					System.out.println("D=" + depth + " : forget " + current + "->" + n);
					continue;
				} else {
					if(!marked.contains(n) && this.graph.getEdge(current, n) < this.capacities.getEdge(current, n)) {
						marked.add(n);
						Z.add(n);
						myPath.add(new Edge(current, n));
					}
				}
			}
			for(Node n : this.capacities.getPredecessors(current)) {
				if(n.equals(to) && depth > 0 && depth < minNodes) {	// Petit ajout pour avoir des chaines de taille >= minNodes
					continue;
				} else {
					if(!marked.contains(n) && this.graph.getEdge(n, current) > 0) {
						marked.add(n);
						Z.add(n);
						myPath.add(new Edge(current, n));
					}
				}
			}
			depth ++;
		}
		if(marked.contains(to)) {
//			System.out.println("\nMinNodes = " + minNodes + "\n---------------");
//			for(Edge e : myPath) {
//				System.out.println(e.from + " -> " + e.to);
//			}
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
		} else if(minNodes > 0) { // S'il n'y a pas de solution parce que nous avons demandé des chaines de taille supérieures,
//			System.out.println("Pas de chemin trouve entre " + from + " et " + to + " pour minNodes = " + minNodes);
			return chaineAmeliorante(from, to, minNodes-1); // On relance la fonction pour trouver une chaine, meme de petite taille.
		}
		return path;
	}

}
