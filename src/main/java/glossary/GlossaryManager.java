package glossary;

import files.ProcessCSV;
import ontologies.Ontology;
import org.semanticweb.owlapi.model.IRI;
import tests.TestCaseDesign;
import utils.Mapper;

import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GlossaryManager {

    private HashMap<String, IRI> got;

    public HashMap<String, IRI> getGot() {
        return got;
    }

    public void setGot(HashMap<String, IRI> got) {
        this.got = got;
    }

    public Map<String, IRI> createGot(Ontology ontology, int i) throws Exception {
        System.out.println("creating got...");
        HashMap<String, IRI> got = new HashMap<>();
        got.putAll(ontology.getClasses());
        got.putAll(ontology.getObjectProperties());
        got.putAll(ontology.getDatatypeProperties());
        got.putAll(ontology.getIndividuals());
        storeFile("got-o" + i + ".csv", got);
        return got;
    }

    public void updateGot(int i) throws FileNotFoundException {
        this.setGot(ProcessCSV.processCSVGoTHash("got-o" + i + ".csv"));
    }

    /* Store got in a file*/
    public void storeFile(String file, HashMap<String, IRI> got) throws IOException {
        BufferedWriter bw = new BufferedWriter(new FileWriter(file));
        bw.write("Term;Key");
        bw.newLine();
        for(String p:got.keySet())
        {
            bw.write(p + ";" + got.get(p));
            bw.newLine();
        }
        bw.flush();
        bw.close();
    }

}
