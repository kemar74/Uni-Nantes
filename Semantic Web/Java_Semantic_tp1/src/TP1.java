import java.io.InputStream;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;
import org.apache.jena.util.FileManager;
import org.apache.jena.vocabulary.VCARD;


/** Tutorial 1 creating a simple model
 */

public class TP1 {
	// some definitions
	static String personURI    = "http://somewhere/JohnSmith";
	static String fullName     = "John Smith";
	
	static String filename = "semantic_tp_1.txt";

	public static void main (String args[]) {
		// créer un modèle vide
		 Model model = ModelFactory.createDefaultModel();
		 /*
		  * Utilisation de fichiers :
		  *
		 // utiliser le FileManager pour trouver le fichier d'entrée
		 InputStream in = FileManager.get().open( filename );
		if (in == null) {
		    throw new IllegalArgumentException(
		                                 "Fichier: " + filename + " non trouvé");
		}
		// lire le fichier RDF/XML
		model.read(in, null);

		model.setNsPrefix("vcard", "http://www.w3.org/2001/vcard-rdf/3.0#");
		
		// l'écrire sur la sortie standard
		model.write(System.out);
		*/

		 model.setNsPrefix("vcard", "http://www.w3.org/2001/vcard-rdf/3.0#");
		 model.setNsPrefix("sw", "http://somewhere/");
		 
		 Resource johnSmith = model.createResource("http://somewhere/JohnSmith")
				 .addProperty(VCARD.FN, "John Smith")
				 .addProperty(VCARD.N, 
						 model.createResource()
							 .addProperty(VCARD.Given, "John")
							 .addProperty(VCARD.Family, "Smith")
						 );
		 
		 model.write(System.out);
	}
}
