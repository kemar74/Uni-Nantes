package main;

import java.util.*;

public class FordFulkersonAlgorithm {
	public float[][] initial;
	public float[][] graph;
	public float[][] residuel;
	public float maxFlow;
	public int S;
	public int T;
	public boolean[] X;
	
	public FordFulkersonAlgorithm(float graph[][], int s, int t) 
    {
		this.initial = graph;
		this.graph = graph;
		this.solve(s, t);
    }
	public void solve() {
		solve(this.S, this.T);
	}
	public void solve(int s, int t)
	{
		this.maxFlow = 0;
		this.S = s;
		this.T = t;
        this.X = new boolean[this.graph.length];
        this.residuel = new float[this.graph.length][this.graph.length]; 
  
        for (int u = 0; u < this.graph.length; u++) 
            for (int v = 0; v < this.graph.length; v++) 
                this.residuel[u][v] = graph[u][v]; 
  
        int predecesseurs[] = new int[this.graph.length]; 
  
        while (parcoursProfondeur(predecesseurs)[this.T]) 
        { 
            float flow = Float.MAX_VALUE; 
            for (int v = t; v != s; v = predecesseurs[v]) 
            { 
                flow = Math.min(flow, this.residuel[predecesseurs[v]][v]); 
            } 
            for (int v = t; v != s; v = predecesseurs[v]) 
            { 
                this.residuel[predecesseurs[v]][v] -= flow; 
                this.residuel[v][predecesseurs[v]] += flow; 
            } 
            this.maxFlow += flow; 
        } 
        this.X = parcoursProfondeur(predecesseurs);
        return;
    } 

    boolean[] parcoursProfondeur(int predecesseurs[]) 
    { 
        boolean visites[] = new boolean[this.residuel.length]; 
        for(int i = 0; i < this.residuel.length; ++i) 
            visites[i] = false; 
  
        LinkedList<Integer> liste = new LinkedList<Integer>(); 
        liste.add(this.S); 
        visites[this.S] = true; 
        predecesseurs[this.S] = -1; 

        while (liste.size()!=0) 
        { 
            int u = liste.poll(); 
  
            for (int v = 0; v < this.residuel.length; v++) 
            { 
                if (visites[v]==false && this.residuel[u][v] > 0) 
                { 
                    liste.add(v); 
                    predecesseurs[v] = u; 
                    visites[v] = true; 
                } 
            } 
        } 
        return visites; 
    } 
    
    public boolean isX(int point) {
    	return this.X[point];
    }
    
    public float getMaximumFlowCapacity() {
    	this.solve();
    	return this.maxFlow;
    }
  
}
