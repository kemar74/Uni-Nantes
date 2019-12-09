package main;

import java.util.*;

import javax.imageio.*;

import java.nio.charset.StandardCharsets; 
import java.nio.file.*;
import java.awt.Color;
import java.awt.Desktop;
import java.awt.image.BufferedImage;
import java.io.*;

public class ResolutionGraphes {
	private static FordFulkersonAlgorithm algo;
	
	public static void main(String[] args) throws Exception {
		if(getOption(new String[] {"h", "help"}, args) != null) {
			displayHelp();
			return;
		}
		String filename = args.length > 0 ? args[args.length -1] : getOption(new String[] {"path", "p", "file"}, args) != null ? getOption(new String[] {"path", "p", "file"}, args) : null;
		boolean verbose = getOption(new String[] {"verbose", "v"}, args) != null ? getOption(new String[] {"verbose", "v"}, args) == "1" : false;
		boolean displayGridAtEnd = getOption(new String[] {"display-grid", "display-all"}, args) != null ? getOption(new String[] {"display-grid", "display-all"}, args) == "1" : false;
		boolean displayGroups = getOption(new String[] {"display-groups", "display-all"}, args) != null ? getOption(new String[] {"display-groups", "display-all"}, args) == "1" : true;
		String displayForGroupA = getOption(new String[] {"groupA", "A", "groupeA"}, args) != null ? getOption(new String[] {"groupA", "A", "groupeA"}, args) : "A ";
		String displayForGroupB = getOption(new String[] {"groupB", "B", "groupeB"}, args) != null ? getOption(new String[] {"groupB", "B", "groupeB"}, args) : "B ";
		String returnedImage = getOption(new String[] {"image", "return"}, args) != null ? getOption(new String[] {"image", "return"}, args) : "";
		
		if(filename == null) {
			System.out.println("Merci de renseigner un fichier texte sur lequel le programme peut se baser (-file ou -path)");
			displayHelp();
			return;
		}
		
		long startingTime = System.currentTimeMillis();
		if(verbose)
			System.out.println("Starting...");

		List<String> lines = readFileInList(filename);

		if(verbose)
			System.out.println("File '" + filename + "' read, gathering informations...");
		Dictionary<String, Object> valeursInitiales = getImageValues(lines);

		if(verbose)
			System.out.println("Graph of size " + (int)valeursInitiales.get("M") + "x" + (int)valeursInitiales.get("N") +
					" being processed...");
		
		float[][] graphe = ConstructionReseau(valeursInitiales, true, verbose);

		int S = graphe.length - 2;
		int T = graphe.length - 1;

		if(verbose) 
			System.out.println("Graph successfully done ("+ (float) (System.currentTimeMillis() - startingTime)/1000 +" s). Applying Ford-Fulkerson's algorithm...");
			
		long algorithmTime = System.currentTimeMillis();
		algo = new FordFulkersonAlgorithm(graphe, S, T);
		ResoudreBinIm(graphe, S, T);
		
		if(verbose) {
			System.out.println("Capacite max : " + ResolutionGraphes.CalculFlotMax(S, T));
			System.out.println("Found in " + (float)((System.currentTimeMillis() - algorithmTime)/1000.0) + " seconds");
		}

		if(displayGroups) {
			for(int y = 0; y < (int) valeursInitiales.get("N"); y++) {
				for(int x = 0; x < (int) valeursInitiales.get("M"); x++) {
					if(algo.isX(y*(int) valeursInitiales.get("M") + x)) {
						System.out.print(displayForGroupA);
					} else {
						System.out.print(displayForGroupB);
					}
				}
				System.out.println("");
			}
		}
		if(returnedImage != "") {
			returnAsImage(algo, (int) valeursInitiales.get("M"), (int) valeursInitiales.get("N"), returnedImage, 100);
			System.out.println("Image enregistr�e sous '" + returnedImage + "'.");
	        Desktop desktop = Desktop.getDesktop();
	        desktop.open(new File(returnedImage));
		}
		if(displayGridAtEnd)
			displayGraph(graphe, (float[][]) valeursInitiales.get("Aij"), (float[][]) valeursInitiales.get("Bij"), (int) valeursInitiales.get("M"));
		
		
		if(verbose)
			System.out.println("Done. Total time : " + (float)((System.currentTimeMillis() - startingTime)/1000.0) + " s");

	}
	

	public static List<String> readFileInList(String fileName) throws IOException 
	{ 
	    List<String> lines = Collections.emptyList(); 
	    lines = Files.readAllLines(Paths.get(fileName), StandardCharsets.UTF_8); 
	    return lines; 
	} 
	public static Dictionary<String, Object> getImageValues(List<String> fileContent) throws Exception {
		Dictionary<String, Object> values = new Hashtable<String, Object>();
		int currentLine = 0;
		values.put("N", Integer.parseInt(fileContent.get(currentLine).split(" ")[0]));
		values.put("M", Integer.parseInt(fileContent.get(currentLine).split(" ")[1]));

		currentLine ++;

		float[][] aValues = new float[(int) values.get("N")][(int) values.get("M")];
		float[][] bValues = new float[(int) values.get("N")][(int) values.get("M")];
		float[][] pHorizontalValues = new float[(int) values.get("N")][(int) values.get("M") - 1];
		float[][] pVerticalValues = new float[(int) values.get("N") - 1][(int) values.get("M")];
		
		for(int i = 0; i < aValues.length; i++) {
			currentLine ++;
			if(fileContent.get(currentLine).isEmpty()) {
				throw new Exception("Line " + (currentLine+1) + " of the file is empty, "
						+ "but it should contain the " + i +"rd line of 'A' values");
			}
			String[] lineSplitted = fileContent.get(currentLine).split(" ");
			if(lineSplitted.length != (int) values.get("M")) {
				throw new Exception("Line " + (currentLine+1) + " doesn't contain enough "
						+ "values (" + lineSplitted.length + " values past, " 
						+ values.get("M") + " required)");
			}
			for(int j = 0; j < aValues[i].length; j++) {
				aValues[i][j] = Float.parseFloat(lineSplitted[j]);
			}
		}
		
		currentLine ++;
		
		for(int i = 0; i < bValues.length; i++) { 
			currentLine ++;
			if(fileContent.get(currentLine).isEmpty()) {
				throw new Exception("Line " + (currentLine+1) + " of the file is empty, "
						+ "but it should contain the " + i +"rd line of 'B' values");
			}
			String[] lineSplitted = fileContent.get(currentLine).split(" ");
			if(lineSplitted.length != (int) values.get("M")) {
				throw new Exception("Line " + (currentLine+1) + " doesn't contain enough "
						+ "values (" + lineSplitted.length + " values past, " 
						+ values.get("M") + " required)");
			}
			for(int j = 0; j < bValues[i].length; j++) { 
				bValues[i][j] = Float.parseFloat(lineSplitted[j]);
			}
		}
			
		currentLine ++;
		
		for(int i = 0; i < pHorizontalValues.length; i++) { 
			currentLine ++;
			if(fileContent.get(currentLine).isEmpty() && (int) values.get("M") > 1) {
				throw new Exception("Line " + (currentLine+1) + " of the file is empty, "
						+ "but it should contain the " + i +"rd line of 'P horizontal' values");
			}
			String[] lineSplitted = fileContent.get(currentLine).split(" ");
			if(lineSplitted.length != (int) values.get("M") - 1 && (int) values.get("M") > 1) {
				throw new Exception("Line " + (currentLine+1) + " doesn't contain enough "
						+ "values (" + lineSplitted.length + " values past, " 
						+ ((int) values.get("M") - 1) + " required)");
			}
			for(int j = 0; j < pHorizontalValues[i].length; j++) { 
				pHorizontalValues[i][j] = Float.parseFloat(lineSplitted[j]);
			}
		}
		
		currentLine ++;
		
		for(int i = 0; i < pVerticalValues.length; i++) { 
			currentLine ++;
			if(fileContent.get(currentLine).isEmpty()) {
				throw new Exception("Line " + (currentLine+1) + " of the file is empty, "
						+ "but it should contain the " + i +"rd line of 'P vertical' values");
			}
			String[] lineSplitted = fileContent.get(currentLine).split(" ");
			if(lineSplitted.length != (int) values.get("M")) {
				throw new Exception("Line " + (currentLine+1) + " doesn't contain enough "
						+ "values (" + lineSplitted.length + " values past, " 
						+ values.get("M") + " required)");
			}
			for(int j = 0; j < pVerticalValues[i].length; j++) { 
				pVerticalValues[i][j] = Float.parseFloat(lineSplitted[j]);
			}
		}
		values.put("Aij", aValues);
		values.put("Bij", bValues);
		values.put("Phorizontal", pHorizontalValues);
		values.put("Pvertical", pVerticalValues);
		 
		return values;
	}

	public static float[][] ConstructionReseau(Dictionary<String, Object> allValues) {
		return ConstructionReseau(allValues, true);
	}
	public static float[][] ConstructionReseau(Dictionary<String, Object> allValues, boolean addSourceAndEnd) {
		return ConstructionReseau(allValues, true, false);
	}
	public static float[][] ConstructionReseau(Dictionary<String, Object> allValues, boolean addSourceAndEnd, boolean verbose) {

    	int M = (int) allValues.get("M");
    	int N = (int) allValues.get("N");
    	float[][] Aij = (float[][]) allValues.get("Aij");
    	float[][] Bij = (float[][]) allValues.get("Bij");
    	float[][] Pvertical = (float[][]) allValues.get("Pvertical");
    	float[][] Phorizontal = (float[][]) allValues.get("Phorizontal");
    	
    	float graphe[][] = new float[M*N + 2][M*N + 2]; // +2 pour S et T
		int s = M*N;
		int t = s + 1;
		
    	for(int i = 0; i < M*N; i++) {
    		if(verbose && i % M == 0)
    			System.out.println(((float) i)/((float) (M*N))*100 + "%...");
    		int x = i % M;
    		int y = i / M;

    		if(y > 0)
    			graphe[i][(y-1)*M + x] = Pvertical[y - 1][x];
    		if(y < N - 1)
    			graphe[i][(y+1)*M + x] = Pvertical[y][x];
    		if(x > 0)
    			graphe[i][y*M + (x -1)] = Phorizontal[y][x - 1];
    		if(x < M - 1)
    			graphe[i][y*M + (x +1)] = Phorizontal[y][x];
    		
    		if(addSourceAndEnd) {
	    		// S :
	    		graphe[s][i] = Aij[y][x];
	    		// T :
	    		graphe[i][t] = Bij[y][x];
    		}
    	}
    	if(verbose)
    		System.out.println("100%!");
    	return graphe;
	}
	
	public static float CalculFlotMax(int from, int to) {
		algo.solve(from, to);
		return algo.getMaximumFlowCapacity();
	}
	
	public static float[][] CalculCoupeMin(int from, int to) {
		algo.solve(from, to);
		float[][] graph = algo.graph;
		
		for(int i = 0; i < graph.length; i++) {
			for(int j = 0; j < graph.length; j++) {
				if(algo.isX(i) != algo.isX(j)) {
					graph[i][j] = 0;
					graph[j][i] = 0;
				}
			}
		}
		return graph;
	}

	public static void displayGraph(float[][] graph, float[][] Aij, float[][] Bij, int width) {
		int height = graph.length / width;
		int i = 0;
		for(int y = 0; y < height; y++) {
			for(int x = 0; x < width-1; x++) {
				System.out.print("(" + Aij[y][x] + ", " + Bij[y][x] + ")\t--- " + graph[i][i+1] + " --- ");
				i++;
			}
			System.out.println("(" + Aij[y][width-1] + ", " + Bij[y][width-1] + ")");
			if(y < height-1) {
				for(int x = 0; x < width; x++) {
					System.out.print("   |                         ");
				}
				System.out.println();
				for(int x = 0; x < width; x++) {
					System.out.print("  " + graph[i][i + width] + "                       ");
				}
				System.out.println();
				for(int x = 0; x < width; x++) {
					System.out.print("   |                         ");
				}
				System.out.println();
			}
		}
	}
	public static void ResoudreBinIm(float[][] graph, int s, int t) {
		algo = new FordFulkersonAlgorithm(graph, s, t);
	}
	
	public static BufferedImage returnAsImage(FordFulkersonAlgorithm al, int width, int height, String path) throws IOException {
		return returnAsImage(al, width, height, path, 1);
	}
	public static BufferedImage returnAsImage(FordFulkersonAlgorithm al, int width, int height, String path, float scale) throws IOException {

        // Constructs a BufferedImage of one of the predefined image types.
        BufferedImage bufferedImage = new BufferedImage((int) Math.ceil(width*scale), (int) Math.ceil(height*scale), BufferedImage.TYPE_INT_RGB);

        for(int dot = 0; dot < width*height; dot++) {
        	int y = dot/width;
        	int x = dot%width;
        	for(int i = 0; i < scale; i++) {
        		for(int j = 0; j < scale; j++) {
        			if(al.X[dot])
        				bufferedImage.setRGB((int) Math.floor(x*scale +i), (int) Math.floor(y * scale +j), Color.RED.getRGB());
        			else
        				bufferedImage.setRGB((int) Math.floor(x*scale +i), (int) Math.floor(y * scale +j), Color.GREEN.getRGB());
        		}
        	}
        }
        // Save as PNG
        File file = new File(path);
        String extension = "";
        int i = file.getPath().lastIndexOf('.');
        if (i > 0) {
            extension = file.getPath().substring(i+1);
        }
        ImageIO.write(bufferedImage, extension, file);
        return bufferedImage;
	}
	

	public static String getOption(String[] names, String[] args) {
		for(String name : names) {
			String result = getOption(name, args);
			if(result != null)
				return result;
		}
		return null;
	}
	public static String getOption(String name, String[] args) {
		for(String opt : args) {
			if(opt.length() < name.length()) continue;
			if(opt.equals(name) || opt.equals("-" + name)) return "1";
			if(opt.length() > name.length()+1 && opt.substring(0, name.length()).equals(name + "=")) return opt.substring(opt.indexOf("=")+1);
			if(opt.length() > name.length()+2 && opt.substring(0, name.length()+2).equals("-" + name + "=")) return opt.substring(opt.indexOf("=")+1);
		}
		return null;
	}
	
	public static void displayHelp() {
		System.out.println(""
				+ "=======================================\n"
				+ "-- Aide a l'utilisation du programme --\n"
				+ "=======================================\n"
				+ "Objectif : deduire la distinction entre le premier plan de l'arriere-plan\n"
				+ "sur une image.\n"
				+ "Auteurs : VENTE Maxime et HARTLEY Marc\n"
				+ "Usage: ResolutionGraphes <options> <path to text>\n"
				+ "Options possible :\n"
				+ "\t-verbose -v    : Les actions sont decrites dans le terminal (defaut : OFF)\n"
				+ "\t-h -help       : Affiche ce message\n"
				+ "\t-path -p -file : Indique le fichier texte a utiliser pour nos calculs (OBLIGATOIRE)\n"
				+ "\t-displayGrid   : Affiche le graphe de depart (defaut : OFF)\n"
				+ "\t-displayGroups : Affiche la distinction de plan en fin de calcul (defaut : ON)\n"
				+ "\t-groupA -A     : Motif a afficher pour representer le premier plan (si -displayGroup est actif) (defaut : \"A \")\n"
				+ "\t-groupB -B     : Motif a afficher pour representer l'arriere-plan (si -displayGroup est actif) (defaut : \"B \")\n"
				+ "\t-image -return : Chemin vers l'image o� enregistrer le resultat (defaut : OFF)\n"
				+ "");
	}
}
