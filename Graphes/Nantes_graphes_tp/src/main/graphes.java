package main;

import java.util.*;

import javax.imageio.*;

import java.nio.charset.StandardCharsets; 
import java.nio.file.*;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.*; 
import java.lang.*;

public class graphes {

	public graphes() {
		// TODO Auto-generated constructor stub
	}

	public static void main(String[] args) throws Exception {
		String filename = "graph_mushroom.txt";
		boolean verbose = true;
		boolean displayGridAtEnd = false;
		boolean displayGroups = false;
		String displayForGroupA = "A ";
		String displayForGroupB = "B ";

		try {
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
			Grid grid = ConstructionReseau(valeursInitiales, verbose);

			if(verbose) 
				System.out.println("Graph successfully done ("+ (float) (System.currentTimeMillis() - startingTime)/100 +" s). Applying Ford-Fulkerson's algorithm...");
				
			long algorithmTime = System.currentTimeMillis();
			Node s = new Node("s");
			Node t = new Node("t");
			grid.addNode(s);
			grid.addNode(t);
			
			float maxVal = 0;
			for(Node n : grid.nodes) {
				maxVal += grid.getDegree(n, true, false);
			}
			
			for(int i = 0; i < grid.nodes.size() - 2; i++) { // On retire S et T du traitement
				int x = i % (int) valeursInitiales.get("M");
				int y = i / (int) valeursInitiales.get("N");

				Float aVal = (Float) ((Float[][]) valeursInitiales.get("Aij"))[x][y];
				Float bVal = (Float) ((Float[][]) valeursInitiales.get("Bij"))[x][y];
				
				grid.addEdge(s, grid.nodes.get(i), aVal*maxVal);
				grid.addEdge(grid.nodes.get(i), t, bVal*maxVal); 
			}

			FordFulkersonAlgorithm algo = new FordFulkersonAlgorithm(grid);
			algo.solve("s", "t", 6);
			
			if(verbose) {
				System.out.println("Capacité max : " + algo.getMaximumFlowCapacity("s", "t"));
				System.out.println("Found in " + (float)(System.currentTimeMillis() - algorithmTime)/1000 + " seconds");
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
			if(displayGridAtEnd)
				System.out.println(grid);
			
			if(verbose)
				System.out.println("Done. Total time : " + (float)(System.currentTimeMillis() - startingTime)/1000 + " s");
		} catch(Exception e) {
			System.out.println("Abort... Reason : " + e.getClass().getName() + " says '" + e.getMessage() + "'");
		}
	}
	
	public static void returnAsImage(FordFulkersonAlgorithm al, int width, int height) throws IOException {

		 
        // Constructs a BufferedImage of one of the predefined image types.
        BufferedImage bufferedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
 
        // Create a graphics which can be used to draw into the buffered image
        Graphics2D g2d = bufferedImage.createGraphics();
 
        // fill all the image with white
        g2d.setColor(Color.white);
        g2d.fillRect(0, 0, width, height);
 
        // create a circle with black
        g2d.setColor(Color.black);
        for(Node n : al.X) {
        	int x = Integer.parseInt(n.ID.split("-")[0]);
        	int y = Integer.parseInt(n.ID.split("-")[1]);
        	bufferedImage.setRGB(x, y, Color.BLACK.getRGB());
        }
// 
//        // Disposes of this graphics context and releases any system resources that it is using. 
//        g2d.dispose();
 
        // Save as PNG
        File file = new File("myimage.png");
        ImageIO.write(bufferedImage, "png", file);
 
        // Save as JPEG
        file = new File("myimage.jpg");
        ImageIO.write(bufferedImage, "jpg", file);
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

		Float[][] aValues = new Float[(int) values.get("N")][(int) values.get("M")];
		Float[][] bValues = new Float[(int) values.get("N")][(int) values.get("M")];
		Float[][] pHorizontalValues = new Float[(int) values.get("N")][(int) values.get("M") -1];
		Float[][] pVerticalValues = new Float[(int) values.get("N") -1][(int) values.get("M")];
		
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
			if(fileContent.get(currentLine).isEmpty()) {
				throw new Exception("Line " + (currentLine+1) + " of the file is empty, "
						+ "but it should contain the " + i +"rd line of 'P horizontal' values");
			}
			String[] lineSplitted = fileContent.get(currentLine).split(" ");
			if(lineSplitted.length != (int) values.get("M") - 1) {
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
	
	public static Grid ConstructionReseau(Dictionary<String, Object> allValues) {
		return ConstructionReseau(allValues, false);
	}
	public static Grid ConstructionReseau(Dictionary<String, Object> allValues, boolean verbose) {
		int M = (int) allValues.get("M");
		int N = (int) allValues.get("N");
		Float[][] Aij = (Float[][]) allValues.get("Aij");
		Float[][] Bij = (Float[][]) allValues.get("Bij");
		Float[][] Phorizontal = (Float[][]) allValues.get("Phorizontal");
		Float[][] Pvertical = (Float[][]) allValues.get("Pvertical");

		// Création d'une grille MxN orienté et vierge
		Grid grid = new Grid(M, N, true, false);
		
		for(int i = 0; i < N; i++) {
			if(verbose)
				System.out.println((int) ((i/(float)N)*100) + "%");
			for(int j = 0; j < M; j++) {
				if( j < M - 1) {
					// With the node on the right
					Float toTheRight = Bij[i][j + 1] + Aij[i][j] + Phorizontal[i][j];
					Float toTheLeft  = Aij[i][j + 1] + Bij[i][j] + Phorizontal[i][j];
//					System.out.println(i + " " + j + " (" + N + " " + M + ")");
					grid.addEdge(grid.getNode(i, j), grid.getNode(i, j + 1), toTheRight);
					grid.addEdge(grid.getNode(i, j + 1), grid.getNode(i, j), toTheLeft);
				}
				
				if(i < N - 1) {
					// With the node under
					Float toBottom = Bij[i + 1][j] + Aij[i][j] + Pvertical[i][j];
					Float toTop    = Aij[i + 1][j] + Bij[i][j] + Pvertical[i][j];
					grid.addEdge(grid.getNode(i, j), grid.getNode(i + 1, j), toBottom);
					grid.addEdge(grid.getNode(i + 1, j), grid.getNode(i, j), toTop);
				}
			}
		}
//		grid.normalizeEdges();
		return grid;
	}
	
	public static float CalculFlotMax(Graph graph) {
		FordFulkersonAlgorithm algo = new FordFulkersonAlgorithm(graph);
		algo.solve("s", "t");
		return algo.getMaximumFlowCapacity("s", "t");
	}
	
	public static Graph CalculCoupeMin(Graph graph) {
		FordFulkersonAlgorithm algo = new FordFulkersonAlgorithm(graph);
		algo.solve("s", "t");
		
		for(Node a : algo.X) {
			for(Node b : algo.Y) {
				graph.removeEdge(a, b);
			}
		}
		return graph;
	}
	/*
	public Graph ResoudreBinIm(Graph graph) {
		FordFulkersonAlgorithm algo = new FordFulkersonAlgorithm(graph);
		algo.solve("s", "t", 6);
		for(int y = 0; y < (int) valeursInitiales.get("N"); y++) {
			for(int x = 0; x < (int) valeursInitiales.get("N"); x++) {
				if(algo.X.contains(grid.getNode(x, y))) {
					System.out.print("A ");
				} else {
					System.out.print("B ");
				}
			}
			System.out.println("");
		}
	}*/
	
	public static Dictionary<String, Object> getImageValues(String firstImage, String secondImage) throws IOException {
		File imageFile = new File(firstImage);
		BufferedImage image1 = ImageIO.read(imageFile);
		File imageFile2 = new File(secondImage);
		BufferedImage image2 = ImageIO.read(imageFile2);
		int M = image1.getHeight();
		int N = image1.getWidth();

		Float[][] aValues = new Float[N][M];
		Float[][] bValues = new Float[N][M];
		Float[][] pHorizontalValues = new Float[N][M -1];
		Float[][] pVerticalValues = new Float[N -1][M];
		
		for(int x = 0; x < M; x++ ) {
			for(int y = 0; y < N; y++) {
				Color col = new Color(image2.getRGB(x,y));
				aValues[y][x] = new Float(grayscale(col));
				bValues[y][x] = new Float(1 - grayscale(col));
				
				if(x < M - 1 ) {
					Color horizontalColor = new Color(image1.getRGB(x + 1, y));
					pHorizontalValues[y][x] = 1-Math.abs((grayscale(col) - grayscale(horizontalColor)));
				}
				
				if(y < N - 1) {
					Color verticalColor = new Color(image1.getRGB(x, y + 1));
					pVerticalValues[y][x] = 1-Math.abs((grayscale(col) - grayscale(verticalColor)));
//					System.out.println("1 - abs(" + col.getRed() + " - " + verticalColor.getRed() + ") = " + pVerticalValues[y][x]);
				}
			}
		}
		Dictionary<String, Object> dict = new Hashtable<String, Object>();
		dict.put("M", M);
		dict.put("N", N);
		dict.put("Aij", aValues);
		dict.put("Bij", bValues);
		dict.put("Phorizontal", pHorizontalValues);
		dict.put("Pvertical", pVerticalValues);
		
		return dict;
	}
	
	public static float grayscale(Color col) {
		float adding = col.getRed() + col.getBlue() + col.getGreen();
		float mean = adding/3;
		float normalized = mean / 255;
		return normalized;
	}

}
