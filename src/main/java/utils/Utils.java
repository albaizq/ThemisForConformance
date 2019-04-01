package utils;

import org.coode.owlapi.turtle.TurtleOntologyFormat;
import org.json.JSONArray;
import org.json.JSONObject;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.io.OWLOntologyDocumentSource;
import org.semanticweb.owlapi.io.OWLParserException;
import org.semanticweb.owlapi.io.StringDocumentSource;
import org.semanticweb.owlapi.model.*;
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

    public static  String  getMatchingStrings(List list, String regex, String uri) {
        String match = "";
        try {
            Pattern p = Pattern.compile(regex);
            for (int i = 0; i < list.size(); i++) {
                if (p.matcher(list.get(i).toString()).matches()) {
                    match = list.get(i).toString();
                    if(match.contains(uri))
                        return match;
                }
            }
        }catch (Exception e){
            System.out.println("error while matching string " +regex );
            return "";
        }
        return match;
    }

    public static void  storeTestResults(ArrayList<TestCaseResult> testsuite) throws OWLOntologyCreationException, OWLOntologyStorageException {

        OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
        String base = "http://www.semanticweb.org/untitled-ontology-53-results#";
        String verif = "http://w3id.org/def/vtc#";
        OWLOntology ont = manager.createOntology(IRI.create(base));
        OWLDataFactory dataFactory = manager.getOWLDataFactory();

        OWLClass verifTestResultClass = dataFactory.getOWLClass(IRI.create(verif + "tests.TestCaseResult"));
        OWLClass verifExecutionClass = dataFactory.getOWLClass(IRI.create(verif + "testingsteps.Execution"));
        int num = 0;
        for(TestCaseResult tc: testsuite){
            num++;
            /*Add test result individual*/
            OWLIndividual subject = dataFactory.getOWLNamedIndividual(IRI.create(base + "testResult"+num));
            OWLClassAssertionAxiom classAssertion = dataFactory.getOWLClassAssertionAxiom(verifTestResultClass, subject);
            manager.addAxiom(ont, classAssertion);
            /*Add test result*/
            OWLIndividual assresult = null;
            if(tc.getTestResult().equals("passed"))
                assresult = dataFactory.getOWLNamedIndividual(IRI.create(verif + "Passed"));
            else if (tc.getTestResult().equals("undefined"))
                assresult = dataFactory.getOWLNamedIndividual(IRI.create(verif + "Undefined"));
            else
                assresult = dataFactory.getOWLNamedIndividual(IRI.create(verif + "NotPassed"));

            OWLObjectProperty testResult = dataFactory.getOWLObjectProperty(IRI.create(verif + "hasTestResult"));
            OWLAxiom testResultAssertion = dataFactory.getOWLObjectPropertyAssertionAxiom(testResult, subject, assresult);
            manager.addAxiom(ont, testResultAssertion);

            /*Add execution individidual*/
            OWLIndividual execution1= dataFactory.getOWLNamedIndividual(IRI.create(base + "execution"+num));
            OWLClassAssertionAxiom classAssertion2 = dataFactory.getOWLClassAssertionAxiom(verifExecutionClass, execution1);
            manager.addAxiom(ont, classAssertion2);
            /*Add executed on*/
            OWLDataProperty executedOnProp = dataFactory.getOWLDataProperty(IRI.create(verif+"executedOn"));
            OWLAxiom exectedOnAssertion = dataFactory.getOWLDataPropertyAssertionAxiom(executedOnProp, execution1, tc.getOntologyURI().toString());
            manager.addAxiom(ont, exectedOnAssertion);
            /*Add related test implem*/
            OWLObjectProperty relatedToProp = dataFactory.getOWLObjectProperty(IRI.create(verif+"isRelatedToImplementation"));
            OWLIndividual relatedImpl = dataFactory.getOWLNamedIndividual(tc.getRelatedTestImpl());
            OWLAxiom relatedToAssertion = dataFactory.getOWLObjectPropertyAssertionAxiom(relatedToProp, execution1, relatedImpl);
            manager.addAxiom(ont, relatedToAssertion);

            /*Add to the test result*/
            OWLObjectProperty testExecution = dataFactory.getOWLObjectProperty(IRI.create(verif + "hasExecution"));
            OWLAxiom testExecutionAssertion = dataFactory.getOWLObjectPropertyAssertionAxiom(testExecution, subject, execution1);
            manager.addAxiom(ont, testExecutionAssertion);
        }

        File file = new File("test-result.ttl");
        TurtleOntologyFormat turtleFormat = new TurtleOntologyFormat();
        //turtleFormat.copyPrefixesFrom(pm);
        turtleFormat.setDefaultPrefix(base );
        manager.saveOntology(ont, turtleFormat, IRI.create(file));
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

    public static String mapImplementationTerms(String query, HashMap<String, IRI> allvalues, Ontology ontology){

        Pattern p = Pattern.compile("\\<(.*?)\\>");
        Matcher m = p.matcher(query);
        String querym = query;
        String uri=ontology.getOntology().getOntologyID().getOntologyIRI().toString();
        if(!ontology.getOntology().getOntologyID().getOntologyIRI().toString().endsWith("#") && !ontology.getOntology().getOntologyID().getOntologyIRI().toString().endsWith("||/"))
            uri= uri+"#";
        while (m.find()) {
            try {
                int flag = 0;
                for (Map.Entry<String, IRI> entry : allvalues.entrySet()) {
                    if(entry.getKey().equals(m.group().replace("<","").replace(">",""))) {
                        querym = querym.replace("<" + entry.getKey() + ">", "<" + entry.getValue() + ">");
                        flag++;
                    }

                }
                if(flag==0){

                    querym = querym.replace(m.group(), "<" + uri.toString() +m.group().replace("<","").replace(">","")+ ">");
                }
                Matcher m2 = p.matcher(query);
                while (m2.find()) {
                    if(!m.group().contains("http")) { // tiene alguna uri
                        querym = querym.replace( m.group() , "<" +  uri +m.group().replace("<","").replace(">","")+ ">" );

                    }
                }
            } catch (Exception e) {
                System.out.println("Error while parsing " + e.getMessage());
                e.printStackTrace();
            }
        }
        //System.out.println("final query "+querym);
        return querym;


    }


}
