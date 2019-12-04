package main;

import java.util.*;

abstract public class GraphAlgorithmWithFlow extends GraphAlgorithm {
	
	public Graph capacities;

	public Set<Node> X;
	public Set<Node> Y;
	
	public GraphAlgorithmWithFlow(Graph graph) {
		super(graph);
		this.capacities = new Graph(graph);
		this.graph.removeAllConnections();
		this.X = new HashSet<Node>();
		this.Y = new HashSet<Node>();
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
	

	public void getMinCut(Node from, Node to) {
		Graph residuel = new Graph(this.graph);
		residuel.removeAllConnections();
		for(Edge e : capacities.edges) {
			float flow = this.graph.getEdge(e.from, e.to);
			if(e.weight > 0 && flow == e.weight) {
				residuel.addEdge(e.from, e.to, 1);
			}
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
//					System.out.println(this.capacities.getEdge(b, a, true));
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

}
