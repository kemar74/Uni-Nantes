package main;

import java.util.*;

public class Node implements Cloneable, Comparable {
	public String ID;
	public Map<Node, Float> neighbours;
	public int hauteur;
	public float excedent;
	public Node() {
		this.ID = ((Integer) java.lang.System.identityHashCode(this)).toString();
		this.neighbours = new HashMap<Node, Float>();
		this.hauteur = 0;
		this.excedent = 0;
	}
	public Node(String ID) {
		this.ID = ID;
		this.neighbours = new HashMap<Node, Float>();
		this.hauteur = 0;
		this.excedent = 0;
	}

	public void addEdge(Node other) {
		this.addEdge(other, 1);
	}
	public void addEdge(Node other, float w) {
		if(this.neighbours.get(other) != null) {
			this.neighbours.put(other, this.neighbours.get(other) + (Float) w);
		} else {
			this.neighbours.put(other, (Float) w);
		}
	}
	public Map<Node, Float> getNeighbours() {
		return this.neighbours;
	}
	public Float getNeighbour(Node n) {
		return this.neighbours.get(n);
	}
	public void removeNeighbours() {
		this.neighbours.clear();
	}
	@Override
	protected Object clone() {
		try{  
	        return super.clone();  
	    }catch(Exception e){ 
	        return null; 
	    }
	}
	
	@Override
	public boolean equals(Object obj) {
		return this.ID == ((Node) obj).ID;
	}
	@Override
	public String toString() {
		return this.ID;
	}
	public String displayForPreflot() {
		return this.ID + "[H=" + this.hauteur + ", E=" + this.excedent + "]";
	}
	@Override 
	public int compareTo(Object obj) {
		Node n = (Node) obj;
		return n.ID.compareTo(this.ID);
	}
//	public Node copy() {
//		return (Node) clone();
//	}
}
