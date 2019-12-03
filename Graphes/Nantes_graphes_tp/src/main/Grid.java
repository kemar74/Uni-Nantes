package main;

import java.util.List;

public class Grid extends Graph {
	public int width;
	public int height;
	
	public Grid() {
		this(0, 0);
	}
	public Grid(int width, int height) {
		this(width, height, true);
	}
	public Grid(int width, int height, Boolean oriented) {
		this(width, height, oriented, true, 1);
	}
	public Grid(int width, int height, Boolean oriented, Boolean connectNodes) {
		this(width, height, oriented, connectNodes, 1);
	}
	public Grid(int width, int height, Boolean oriented, Boolean connectNodes, float valueOfConnexion) {
		for(int i = 0; i < width; i++) {
			for(int j = 0; j < height; j++) {
				this.addNode(new Node(i + "-" + j));
			}
		}
		this.width = width;
		this.height = height;
		this.oriented = oriented;
		this.edges.ensureCapacity(width * height * 4);
		
		if(connectNodes) {
			for(int i = 0; i < width; i++) {
				for(int j = 0; j < height; j++) {
					// Lower connection 
					if(j < height-1)
						addEdge(this.getNode(i, j), this.getNode(i, j + 1), valueOfConnexion);
					// Right connection
					if(i < width - 1)
						addEdge(this.getNode(i, j), this.getNode(i + 1, j), valueOfConnexion);
					
					// If non-oriented, the first two steps did it all
					if(this.oriented) {
						// Upper connection
						if(j > 0) 
							addEdge(this.getNode(i, j), this.getNode(i, j - 1), valueOfConnexion);
						// Left connection
						if(i > 0) 
							addEdge(this.getNode(i, j), this.getNode(i - 1, j), valueOfConnexion);
					}
				}
			}
		}
	}
	
	public Node getNode(int x, int y) {
		return this.getNode(y * this.width + x);
	}
	
	@Override
	public String toString() {
		String output = "";
		output += "Grille " + this.width + "x" + this.height + " (" + this.edges.size() + " liens)";
		
		for(int i = 0; i < this.width; i++) {
			output += "\n";
			for(int j = 0; j < this.width; j++) {
				output += this.getNode(i, j).ID + " ";
			}
		}
		output += "\n" + this.displayConnections();
		
		return output;
	}
	
	// TODO : Si nouvelles dimensions supérieures aux anciennes
	public void setDimensions(int newWidth, int newHeight, Boolean connectNewNodes) {
		List<Node> newNodes = this.nodes;
		for(int i = 0; i < this.nodes.size(); i++) {
			if((Integer) i % (Integer) this.width > newWidth || (Integer) i / (Integer) this.height > newHeight) {
				newNodes.remove(this.getNode(i));
			}
		}
	}

}
