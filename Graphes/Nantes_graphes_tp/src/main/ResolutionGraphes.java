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
	private static PreflotAlgorithm algo;
	private static Grid grid;
	public static void main(String[] args) throws Exception {
//		try {
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
			
			ResolutionGraphes.grid = ConstructionReseau(valeursInitiales, true, verbose);
	
			if(verbose) 
				System.out.println("Graph successfully done ("+ (float) (System.currentTimeMillis() - startingTime)/1000 +" s). Applying Ford-Fulkerson's algorithm...");
				
			long algorithmTime = System.currentTimeMillis();
			//PreflotAlgorithm algo2 = new PreflotAlgorithm(grid);
			//algo2.solve("s", "t");
			algo = new PreflotAlgorithm(grid);
			ResoudreBinIm();
			
			if(verbose) {
				System.out.println("Capacité max : " + ResolutionGraphes.CalculFlotMax("s", "t"));
				System.out.println("Found in " + (float)((System.currentTimeMillis() - algorithmTime)/1000.0) + " seconds");
			}
	
			if(displayGroups) {
				for(int y = 0; y < (int) valeursInitiales.get("N"); y++) {
					for(int x = 0; x < (int) valeursInitiales.get("M"); x++) {
						boolean isB = true;
						Iterator<Node> it = algo.X.iterator();
						while(it.hasNext())
							if(it.next().equals(grid.getNode(x, y)))
								isB = false;
						if(isB) {
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
				System.out.println("Image enregistrée sous '" + returnedImage + "'.");
		        Desktop desktop = Desktop.getDesktop();
		        desktop.open(new File(returnedImage));
			}
			if(displayGridAtEnd)
				System.out.println(grid);
			
			
			if(verbose)
				System.out.println("Done. Total time : " + (float)((System.currentTimeMillis() - startingTime)/1000.0) + " s");
//		} catch(Exception e) {
//			System.out.println("Oups... " + e.toString() + (e.getMessage() != null ? " : " + e.getMessage() : "") + (e.getStackTrace() != null ? " (" + e.getStackTrace()[e.getStackTrace().length -1] + ")" : ""));
//		}
	}
	

	public static List<String> readFileInList(String fileName) 
	{ 
	    List<String> lines = Collections.emptyList(); 
	    try
	    { lines = Files.readAllLines(Paths.get(fileName), StandardCharsets.UTF_8); 
	    } catch (IOException e) 
	    {e.printStackTrace();} 
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
	
	/*private float[][] getValues(List<String> fileContent, int currentLine, int width, int height) {
		float[][] values = new float[width][height];
		for(int i = 0; i < height; i++) { 
			currentLine ++;
			if(fileContent.get(currentLine).isEmpty()) {
				throw new Exception("Line " + (currentLine+1) + " of the file is empty, "
						+ "but it should contain the " + i +"rd line of 'P vertical' values");
			}
			String[] lineSplitted = fileContent.get(currentLine).split(" ");
			if(lineSplitted.length != width) {
				throw new Exception("Line " + (currentLine+1) + " doesn't contain enough "
						+ "values (" + lineSplitted.length + " values past, " 
						+ width + " required)");
			}
			for(int j = 0; j < values[i].length; j++) { 
				values[i][j] = Float.parseFloat(lineSplitted[j]);
			}
		}
		return null;
	}*/

	public static Grid ConstructionReseau(Dictionary<String, Object> allValues) {
		return ConstructionReseau(allValues, true);
	}
	public static Grid ConstructionReseau(Dictionary<String, Object> allValues, boolean addSourceAndEnd) {
		return ConstructionReseau(allValues, true, false);
	}
	public static Grid ConstructionReseau(Dictionary<String, Object> allValues, boolean addSourceAndEnd, boolean verbose) {
		int M = (int) allValues.get("M");
		int N = (int) allValues.get("N");
		float[][] Aij = (float[][]) allValues.get("Aij");
		float[][] Bij = (float[][]) allValues.get("Bij");
		float[][] Phorizontal = (float[][]) allValues.get("Phorizontal");
		float[][] Pvertical = (float[][]) allValues.get("Pvertical");

		// Création d'une grille MxN orienté et vierge
		Grid grid = new Grid(M, N, true, false);
		
		for(int i = 0; i < M; i++) {
			if(verbose)
				System.out.println((int) ((i/(float)M)*100) + "%");
			for(int j = 0; j < N; j++) {
				if(i < M - 1) {
					// With the node on the right
					float toTheRight = Aij[j][i + 1] + Bij[j][i] + Phorizontal[j][i];
					float toTheLeft  = Bij[j][i + 1] + Aij[j][i] + Phorizontal[j][i];
					grid.addEdge(grid.getNode(i, j), grid.getNode(i+1, j), toTheRight);
					grid.addEdge(grid.getNode(i+1, j), grid.getNode(i, j), toTheLeft);
				}
				
				if(j < N - 1) {
					// With the node under
					float toBottom = Aij[j + 1][i] + Bij[j][i] + Pvertical[j][i];
					float toTop    = Bij[j + 1][i] + Aij[j][i] + Pvertical[j][i];
					grid.addEdge(grid.getNode(i, j), grid.getNode(i, j + 1), toBottom);
					grid.addEdge(grid.getNode(i, j + 1), grid.getNode(i, j), toTop);
				}
			}
		}

		if(addSourceAndEnd) {
			Node s = new Node("s");
			Node t = new Node("t");
			grid.addNode(s);
			grid.addNode(t);
			
			float maxVal = 0;
			for(Node n : grid.nodes) {
				maxVal += grid.getDegree(n, true, false);
			}
			
			for(int i = 0; i < grid.nodes.size() - 2; i++) { // On retire S et T du traitement
				int x = i % M;
				int y = i / M;
	
				float aVal = Aij[y][x];
				float bVal = Bij[y][x];
				
				grid.addEdge(s, grid.nodes.get(i), aVal*maxVal);
				grid.addEdge(grid.nodes.get(i), t, bVal*maxVal); 
			}
		}
		return grid;
	}
	
	public static float CalculFlotMax(String from, String to) {
//		algo.solve(from, to);
		return algo.getMaximumFlowCapacity(from, to);
	}
	
	public static Graph CalculCoupeMin(String from, String to) {
		algo.solve(from, to);
		
		for(Node a : algo.X) {
			for(Node b : algo.Y) {
				grid.removeEdge(a, b);
			}
		}
		return grid;
	}

	public static void ResoudreBinIm() {
		algo.solve("s", "t");
	}
	
	public static Graph getTestGrid() {
		Graph graph = new Graph(8);
		graph.getNode(0).ID = "s";
		graph.getNode(1).ID = "2";
		graph.getNode(2).ID = "3";
		graph.getNode(3).ID = "4";
		graph.getNode(4).ID = "5";
		graph.getNode(5).ID = "6";
		graph.getNode(6).ID = "7";
		graph.getNode(7).ID = "t";

		graph.addEdge("s", "2", 10);
		graph.addEdge("s", "3", 5);
		graph.addEdge("s", "4", 15);
		graph.addEdge("2", "3", 4);
		graph.addEdge("2", "5", 9);
		graph.addEdge("2", "6", 15);
		graph.addEdge("3", "4", 4);
		graph.addEdge("3", "6", 8);
		graph.addEdge("4", "7", 30);
		graph.addEdge("5", "6", 15);
		graph.addEdge("5", "t", 10);
		graph.addEdge("6", "7", 15);
		graph.addEdge("6", "t", 10);
		graph.addEdge("7", "3", 6);
		graph.addEdge("7", "t", 10);
		return graph;
	}
	

	public static BufferedImage returnAsImage(GraphAlgorithmWithFlow al, int width, int height, String path) throws IOException {
		return returnAsImage(al, width, height, path, 1);
	}
	public static BufferedImage returnAsImage(GraphAlgorithmWithFlow al, int width, int height, String path, float scale) throws IOException {

        // Constructs a BufferedImage of one of the predefined image types.
        BufferedImage bufferedImage = new BufferedImage((int) Math.ceil(width*scale), (int) Math.ceil(height*scale), BufferedImage.TYPE_INT_RGB);

        for(Node n : al.X) {
        	try {
	        	int y = Integer.parseInt(n.ID.split("-")[0]);
	        	int x = Integer.parseInt(n.ID.split("-")[1]);
	        	for(int i = 0; i < scale; i++)
	        		for(int j = 0; j < scale; j++)
	        			bufferedImage.setRGB((int) Math.floor(x*scale +i), (int) Math.floor(y * scale +j), Color.RED.getRGB());
        	} catch(NumberFormatException e) {
        		
        	} catch(ArrayIndexOutOfBoundsException e) {
        		
        	}
        }
        for(Node n : al.Y) {
        	try {
	        	int y = Integer.parseInt(n.ID.split("-")[0]);
	        	int x = Integer.parseInt(n.ID.split("-")[1]);
	        	for(int i = 0; i < scale; i++)
	        		for(int j = 0; j < scale; j++)
	        			bufferedImage.setRGB((int) Math.floor(x*scale +i), (int) Math.floor(y * scale +j), Color.GREEN.getRGB());
        	} catch(NumberFormatException e) {
        		
        	} catch(ArrayIndexOutOfBoundsException e) {
        		
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
				+ "-- Aide à l'utilisation du programme --\n"
				+ "=======================================\n"
				+ "Objectif : déduire la distinction entre le premier plan de l'arrière-plan\n"
				+ "sur une image.\n"
				+ "Auteurs : VENTE Maxime et HARTLEY Marc\n"
				+ "Usage: ResolutionGraphes <options> <path to text>\n"
				+ "Options possible :\n"
				+ "\t-verbose -v    : Les actions sont décrites dans le terminal (defaut : OFF)\n"
				+ "\t-h -help       : Affiche ce message\n"
				+ "\t-path -p -file : Indique le fichier texte à utiliser pour nos calculs (OBLIGATOIRE)\n"
				+ "\t-displayGrid   : Affiche le graphe de départ (defaut : OFF)\n"
				+ "\t-displayGroups : Affiche la distinction de plan en fin de calcul (defaut : ON)\n"
				+ "\t-groupA -A     : Motif à afficher pour representer le premier plan (si -displayGroup est actif) (defaut : \"A \")\n"
				+ "\t-groupB -B     : Motif à afficher pour representer l'arriere-plan (si -displayGroup est actif) (defaut : \"B \")\n"
				+ "\t-image -return : Chemin vers l'image où enregistrer le resultat (defaut : OFF)\n"
				+ "");
	}
}
