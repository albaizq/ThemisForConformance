package utils;

import org.coode.owlapi.turtle.TurtleOntologyFormat;
import org.json.JSONArray;
import org.json.JSONObject;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.io.OWLOntologyDocumentSource;
import org.semanticweb.owlapi.io.OWLParserException;
import org.semanticweb.owlapi.io.StringDocumentSource;
import org.semanticweb.owlapi.model.*;
import testingsteps.Implementation;
import testingsteps.Mapping;
import tests.TestCaseDesign;
import tests.TestCaseResult;
import uk.ac.manchester.cs.owl.owlapi.turtle.parser.TurtleOntologyParser;


import java.io.File;
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

    public static ArrayList<String> getPurposeTerms(ArrayList<String> queries){

        ArrayList<String> terms = new ArrayList<>();
        Pattern p = Pattern.compile("\\<(.*?)\\>");
        for(String query: queries){
            Matcher m = p.matcher(query);
            while(m.find()){
                terms.add(m.group().toString().replace("<","").replace(">","").trim());
            }
        }
        return  terms;
    }

    public static  Set convertStringToAxioms(String axioms) {


            if (axioms == null || axioms.isEmpty()) {
                return null;
            }

            OWLOntologyManager translationManager = OWLManager.createOWLOntologyManager();
            TurtleOntologyParser p = new TurtleOntologyParser();
            OWLOntologyDocumentSource documentSource = new StringDocumentSource(axioms);

            OWLOntology ontology;
            try {
                ontology = translationManager.createOntology();

                p.parse(documentSource, ontology);
                return ontology.getAxioms();
            } catch (OWLOntologyCreationException | OWLParserException e) {
                System.out.println("ERROR: "+ e.getMessage());
                return null;
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }

    }

    public static String mappingValue(String query, String ontology, JSONArray allvalues){
        Pattern p = Pattern.compile("\\<(.*?)\\>");
        Matcher m = p.matcher(query);
        String querym = query;

        while (m.find()) {
            try {
                int flag = 0;

                for (int i = 0; i < allvalues.length(); i++) {
                    JSONObject object = allvalues.getJSONObject(i);
                    if(ontology.equals(object.getString("Ontology"))){
                        querym = querym.replace("<"+object.get("Term")+">", "<" + object.get("Value")+ ">");
                        flag++;
                    }
                }
               /* if(flag==0 && !m.group().contains("http")){
                    querym = querym.replace(m.group(), "<" + ontology.toString() +m.group().replace("<","").replace(">","")+ ">");
                }*/


            } catch (Exception e) {
                System.out.println("Error while parsing " + e.getMessage());
                e.printStackTrace();
            }
        }
        return querym;


    }


    public static void unZipIt(String resourceName, String outputFolder) {

        byte[] buffer = new byte[1024];

        try {
            java.io.InputStream in = Utils.class.getResourceAsStream(resourceName);
            java.util.zip.ZipInputStream zis =
                    new java.util.zip.ZipInputStream(Utils.class.getResourceAsStream(resourceName));
            java.util.zip.ZipEntry ze = zis.getNextEntry();

            while (ze != null) {

                String fileName = ze.getName();
                java.io.File newFile = new java.io.File(outputFolder + java.io.File.separator + fileName);
                //System.out.println("file unzip : "+ newFile.getAbsoluteFile());
                if (ze.isDirectory()) {
                    String temp = newFile.getAbsolutePath();
                    new java.io.File(temp).mkdirs();
                } else {
                    java.io.FileOutputStream fos = new java.io.FileOutputStream(newFile);
                    int len;
                    while ((len = zis.read(buffer)) > 0) {
                        fos.write(buffer, 0, len);
                    }
                    fos.close();
                }
                ze = zis.getNextEntry();
            }

            zis.closeEntry();
            zis.close();

        } catch (java.io.IOException ex) {
            System.err.println("Error while extracting the resources: " + ex.getMessage());
        }
    }
    public static JSONArray createGot(ArrayList<TestCaseDesign> testsuiteDesign, Ontology ontology) throws Exception {

        //Create GoT
        System.out.println("create got...");
        ArrayList<String> terms = new ArrayList<>();
        for (TestCaseDesign test : testsuiteDesign) {
            for (String purpose : test.getPurpose()) {
                terms.addAll(Mapping.processTestExpressionToExtractTerms(purpose));
            }
        }
        JSONArray got = new JSONArray();
        int i = 1;

        ArrayList<String> entities = new ArrayList();
        Iterator it = ontology.getOntology().getSignature(true).iterator();
        while (it.hasNext()) {
            OWLEntity entity = (OWLEntity) it.next();
            JSONObject ontologyobjterm = new JSONObject();
            if (entity.getIRI().getFragment() != null) {
                if (!entities.contains(entity.getIRI().getFragment().toString())) {
                    entities.add(entity.getIRI().getFragment().toString());
                    ontologyobjterm.put("Term", entity.getIRI().getFragment());
                    ontologyobjterm.put("Value", entity.getIRI().toString()); //get the value with higher smilarity
                    ontologyobjterm.put("Ontology", ontology.getProv());
                    got.put(ontologyobjterm);
                } else {
                    entities.add(entity.getIRI().getFragment().toString());
                    String[] uri = entity.getIRI().getNamespace().toString().split("/");
                    ontologyobjterm.put("Term", uri[uri.length - 1] + entity.getIRI().getFragment().toString());
                    ontologyobjterm.put("Value", entity.getIRI().toString()); //get the value with higher smilarity
                    ontologyobjterm.put("Ontology", ontology.getProv());
                    got.put(ontologyobjterm);
                }
            }
        }
        return got;
    }

    public static ArrayList<Ontology> loadOntologies(String file) throws Exception {

        //Process CVS, list of ontologies and  tests
        System.out.println("process csv...");
        HashMap<String, String> ontoAndTest = ProcessCSV.processCSVVocabs(file);

        ArrayList<Ontology> ontologies = new ArrayList<>();
        for (Map.Entry<String, String> entry : ontoAndTest.entrySet()) {
            //Load ontologies
            Ontology ontology = new Ontology();
            if(entry.getKey().length()!=0) {
                ontology.load_ontologyURL(entry.getKey());
                ontologies.add(ontology);
            }
        }
        return  ontologies;
    }

    public static ArrayList<TestCaseDesign> loadTests(String file) throws Exception {

        HashMap<String, String> ontoAndTest = ProcessCSV.processCSVVocabs(file);
        ArrayList<TestCaseDesign> testsuiteDesign = new ArrayList<>();
        for (Map.Entry<String, String> entry : ontoAndTest.entrySet()) {
            // Load tests
            System.out.println("loading tests...");
            testsuiteDesign.addAll(Implementation.processTestCaseDesign(entry.getValue()));
        }
        return testsuiteDesign;
    }


}
