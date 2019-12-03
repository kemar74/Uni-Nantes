package main;

import java.util.*;

public class Graph implements Cloneable {
	public ArrayList<Node> nodes;
	public ArrayList<Edge> edges;
	public Boolean oriented;

	public Graph() {
		this(0);
	}
	public Graph(Graph copy) {
		this(0);
		for(int i = 0; i < copy.nodes.size(); i++)
			this.nodes.add((Node) copy.nodes.get(i).clone());
		for(int i = 0; i < copy.edges.size(); i++)
			this.edges.add((Edge) copy.edges.get(i).clone());
			
		this.oriented = copy.oriented;
	}
	public Graph(int numberOfNodes) {
		this(numberOfNodes, true);
	}
	public Graph(int numberOfNodes, Boolean isOriented) {
		this.edges = new ArrayList<Edge>();
		this.nodes = new ArrayList<Node>();
		for(int i = 0; i < numberOfNodes; i++) {
			this.addNode(new Node(((Integer) i).toString()));
		}
		this.oriented = isOriented;
	}
	
	public void addNode(Node n) {
		this.nodes.add(n);
	}
	
	public void addEdge(Node from, Node to, float w) {
		boolean found = false;
		for(int i = 0; i < this.edges.size(); i++) {
			if(this.edges.get(i).equals(new Edge(from, to))) {
				this.edges.get(i).weight += w;
				found = true;
			}
		}
		if(!this.oriented) {
			for(int i = 0; i < this.edges.size(); i++) {
				if(this.edges.get(i).equals(new Edge(to, from))) {
					this.edges.get(i).weight += w;
					found = true;
				}
			}
		}
		if(!found) {
			this.edges.add(new Edge(from, to, w));
			if(!this.oriented) {
				this.edges.add(new Edge(to, from, w));
			}
		}
	}
	public void addEdge(Node from, Node to) {
		this.addEdge(from, to, 1);
	}
	public void addEdge(String from, String to, float w) {
		addEdge(getNode(from), getNode(to), w);
	}
	public void addEdge(String from, String to) {
		this.addEdge(from, to, 1);
	}
	
	public void removeEdge(Node from, Node to) {
		for(int i = 0; i < this.edges.size(); i ++) {
			Edge e = this.edges.get(i);
			if(e.from == from && e.to == to) {
				this.edges.remove(i);
				return;
			}
			
		}
	}
	public void removeEdge(String from, String to) {
		this.removeEdge(this.getNode(from), this.getNode(to));
	}
	
	@Override
	public String toString() {
		String output = "";
		output += "Graphe de " + this.nodes.size() + " noeuds\n";
		
		output += displayConnections();
		
		return output;
	}

	public Node getNode(Node node) {
		for(Node n : this.nodes)
			if(node.equals(n))
				return n;
		return null;
	}
	public Node getNode(int i) {
		return this.nodes.get(i);
	}
	public Node getNode(String ID) {
		for(int i = 0; i < this.nodes.size(); i++) {
			if(this.nodes.get(i).ID.equals(ID))
				return this.nodes.get(i);
		}
		return null;
	}
	
	@SuppressWarnings("unchecked")
	public String displayConnections() {
		List<String> listOfEdges = new ArrayList<String>();
		Collections.sort(this.edges);
		for(Edge e : this.edges) {
			listOfEdges.add(e.toString());
		}
		StringBuilder sb = new StringBuilder();
		for (String s : listOfEdges)
		{
		    sb.append(s);
		    sb.append(", ");
		}
		return sb.toString();
	}
	
	public int size() {
		return this.nodes.size();
	}
	
	public void removeAllConnections() {
		this.edges.clear();
	}
	
	public Float getEdge(String from, String to) {
		return this.getEdge(this.getNode(from), this.getNode(to));
	}
	public Float getEdge(Node from, Node to) {
		for(Edge e : this.edges) {
			if(from.equals(e.from) && to.equals(e.to))
				return e.weight;
		}
		return (float) 0;
	}

	public List<Node> getSuccessors(Node node) {
		List<Node> list = new ArrayList<Node>();
		for(Edge e : this.edges) {
			if(node.equals(e.from)) {
				list.add(e.to);
			}
		}
		return list;
	}
	public List<Node> getPredecessors(Node node) {
		List<Node> list = new ArrayList<Node>();
		for(Edge e : this.edges) {
			if(node.equals(e.to)) {
				list.add(e.from);
			}
		}
		return list;
	}
	@Override
	protected Object clone() {
		try{  
	        return super.clone();  
	    }catch(Exception e){ 
	        return null; 
	    }
	}
	
	public Set<Node> connectedNodes(Node from, Set<Node> list) {
		if(list.contains(from))
			return list;
		
		list.add(from);
		
		for(Node n : this.getSuccessors(from)) {
			if(this.getEdge(from, n) > 0) {
				if(!list.contains(n)) {
					list.addAll(connectedNodes(n, list));
				}
			}
		}
		return list;
	}
	public Set<Node> connectedNodes(Node from) {
		return connectedNodes(from, new HashSet<Node>());
	}
	public Set<Node> connectedNodes(String from) {
		return connectedNodes(this.getNode(from));
	}
	
	public Graph inverseAllEdges() {
		for(Edge e : this.edges) {
			e.inverse();
		}
		return this;
	}
	
	public Float getDegree(Node n, boolean sortant, boolean entrant) {
		Float total = new Float(0);
		for(Edge e : this.edges) {
			if(entrant && e.to == n)
				total += e.weight;
			if(sortant && e.from == n)
				total += e.weight;
		}
		return total;
	}
	public Float getDegree(String n, boolean sortant, boolean entrant) {
		return getDegree(this.getNode(n), sortant, entrant);
	}
	public Float getDegree(Node n) {
		return getDegree(n, true, true);
	}
	
	public void normalizeEdges() {
		Float maxValue = new Float(0);
		for(Edge e : this.edges) {
			if(e.weight > maxValue)
				maxValue = e.weight;
		}
		for(Edge e : this.edges) {
			e.weight /= maxValue;
		}
	}
}
