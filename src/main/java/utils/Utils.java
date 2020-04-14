package utils;

import ontologies.Ontology;
import org.semanticweb.owlapi.model.*;
import testingsteps.Design;
import testingsteps.Mapping;
import tests.TestCaseDesign;


import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by albafernandez on 19/06/2017.
 */
public class Utils {


    public static String getPrecTerm(String query){

        Pattern p = Pattern.compile("\\<(.*?)\\>");
        Matcher m = p.matcher(query);
        while(m.find()){
            return  m.group();
        }
        return  m.group();
    }


    public static HashMap<String, IRI> createGot(ArrayList<TestCaseDesign> testsuiteDesign, Ontology ontology) throws Exception {

        //Create GoT
        System.out.println("creating got...");
        ArrayList<String> terms = new ArrayList<>();
        for (TestCaseDesign test : testsuiteDesign) {
            for (String purpose : test.getPurpose()) {
                terms.addAll(Mapping.processTestExpressionToExtractTerms(purpose));
            }
        }
        HashMap<String, IRI> got = new HashMap<>();
        got.putAll(ontology.getClasses());
        got.putAll(ontology.getObjectProperties());
        got.putAll(ontology.getDatatypeProperties());
        got.putAll(ontology.getIndividuals());

        return got;
    }


    /* Load tests*/
    public static ArrayList<TestCaseDesign> loadTest(String file) throws Exception {

        ArrayList<TestCaseDesign> testsuiteDesign = new ArrayList<>();
        testsuiteDesign.addAll(Design.processTestCaseDesign(file));
        return testsuiteDesign;
    }
    /* Store got in a file*/
    public static void storeFile(String file, HashMap<String, IRI> got) throws IOException {
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
