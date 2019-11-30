package main;

public class Edge implements Cloneable, Comparable {
	public Node from;
	public Node to;
	public Float weight;
	
	public Edge(Node from, Node to, Float weight) {
		this.from = from;
		this.to = to;
		this.weight = weight;
	}
	public Edge(Node from, Node to) {
		this(from, to, (float) 1);
	}
	@Override
	public String toString() {
		return "(" + this.from.ID + ", " + this.to.ID + ", " + this.weight + ")";
	}
	@Override
	public int compareTo(Object obj) {
		Edge e = (Edge) obj;
		if(e.from == this.from)
		{
			return e.to.compareTo(this.to);
		}
		return e.from.compareTo(this.from);
	}
	@Override
	public boolean equals(Object obj) {
		return this.from.equals(((Edge) obj).from) && this.to.equals(((Edge) obj).to);
	}
	@Override
	protected Object clone() {
		try{  
	        return super.clone();  
	    }catch(Exception e){ 
	        return null; 
	    }
	}
	
	public Edge inverse() {
		Node tmp = this.from;
		this.from = this.to;
		this.to = tmp;
		return this;
	}

}
