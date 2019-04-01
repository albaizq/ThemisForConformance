import org.apache.commons.io.FileUtils;
import org.json.*;
import org.semanticweb.owlapi.model.*;
import testingsteps.Execution;
import testingsteps.Implementation;
import testingsteps.Mapping;
import tests.TestCaseDesign;
import tests.TestCaseImplementation;
import tests.TestCaseResult;
import utils.Ontology;
import utils.ProcessCSV;


import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.net.URISyntaxException;
import java.util.*;

import static testingsteps.Mapping.processTestExpressionToExtractTerms;

/**
 * Created by Alba on 01/02/2017.
 */
public class main {
    public static void main(String[] args) throws Exception {


        if (args.length != 2) {
            System.out.println("usage: java -jar coverage.jar -i testfile ");
            System.exit(1);
        }

        ArrayList<Ontology> ontologies = loadOntologies(args[1]);
        ArrayList<TestCaseDesign> testsuiteDesign = loadTests(args[1]);
        JSONArray tableResultsComplete = new JSONArray();
        JSONArray gottotal = new JSONArray();
        //Create GoT
        int i = 1;
        for(Ontology ontology: ontologies){
            JSONArray got = createGot(testsuiteDesign, ontology);
            String csv = CDL.toString(got).replace(",", ";");
            FileUtils.writeStringToFile(new File("got-o"+i+".csv"), csv);
            got = ProcessCSV.processCSVGoT("got-o"+i+".csv");
            //if ok then
            System.out.println("Is the GoT ok? ");
            Scanner scanner = new Scanner(System.in);
            String result = scanner.nextLine();
            while (!result.equals("y")) {
                System.out.println("Is the GoT ok? ");
                scanner = new Scanner(System.in);
                result = scanner.nextLine();
            }
            System.out.println("implement tests...");
            //Implement tests
            got = ProcessCSV.processCSVGoT("got-o"+i+".csv");
            for(int j = 0; j< got.length();j++) {
                gottotal.put(got.getJSONObject(j));
            }
            ArrayList<TestCaseImplementation> testsuiteImpl = Implementation.createTestImplementation(testsuiteDesign);

            // Execute all tests on each ontology
            Execution exec = new Execution();
            JSONArray tableResults = new JSONArray();
            JSONObject array = new JSONObject();
            ArrayList<TestCaseResult> testCaseResults = new ArrayList<>();
            for (TestCaseDesign testCaseDesign : testsuiteDesign) {
                ArrayList<TestCaseImplementation> implementations = new ArrayList<>();
                for (TestCaseImplementation tci : testsuiteImpl) {
                    if (testCaseDesign.getUri().toString().equals(tci.getRelatedTestDesign().toString())) {
                        implementations.add(tci);
                    }
                }
                try {
                    ArrayList<Ontology> ontos = new ArrayList<>();
                    ontos.add(ontology);
                    testCaseResults = exec.execute(implementations, testCaseDesign,ontology , got);
                    array = Execution.printReportPerOntology(ontology, testCaseDesign, testCaseResults);
                } catch (URISyntaxException e) {
                    e.printStackTrace();
                }
                tableResults.put(array);
            }
            /*Test results*/
            String csvResults = CDL.toString(tableResults).replace(",", ";").replace("\";\"", "\",\"");
            System.out.println("store results...");
            FileUtils.writeStringToFile(new File("results-o"+i+".csv"), csvResults);
            for(int j = 0; j< tableResults.length();j++) {
                tableResultsComplete.put(tableResults.getJSONObject(j));
            }
            i++;
        }
        /*Analyse joint results*/

        ArrayList<String> terms = new ArrayList<>();
        ArrayList<String> terms2 = new ArrayList<>();
        JSONArray jointResults = new JSONArray();
        HashMap<Ontology, ArrayList<String>> termsInvolved = new HashMap<>();
        for (TestCaseDesign testCaseDesign : testsuiteDesign) {
            ArrayList<String> ontologies1= new ArrayList<>();
            for(Ontology ontology: ontologies){
                for(int j = 0; j< tableResultsComplete.length();j++) {
                    JSONObject obj = tableResultsComplete.getJSONObject(j);
                    if (obj.has("Requirement description")) {
                        if (obj.getString("Requirement description").equals(testCaseDesign.getDescription()) && obj.getString("Ontology").equals(ontology.getProv().toString()) && obj.getString("Result").equals("passed")) {
                            ontologies1.add(ontology.getProv().toString());
                            for (String purpose : testCaseDesign.getPurpose())
                                terms2.addAll(processTestExpressionToExtractTerms(purpose));

                            for (String term : terms2) {

                                for (int k = 0; k < gottotal.length(); k++) {
                                    JSONObject gotelement = new JSONObject();
                                    gotelement = gottotal.getJSONObject(k);
                                    if (gotelement.getString("Term").equals(term) && gotelement.getString("Ontology").equals(ontology.getProv().toString())) {
                                        if(!terms.contains(gotelement.getString("Value")))
                                            terms.add(gotelement.getString("Value"));
                                    }
                                }

                            }

                            termsInvolved.put(ontology, terms2);
                        }
                    }
                }
            }
            if(ontologies1.size()>1) {
                JSONObject objresults = new JSONObject();
                objresults.put("Requirement description", testCaseDesign.getDescription());
                objresults.put("Ontologies that passes the test", ontologies1);
                objresults.put("Provenance", testCaseDesign.getProvenance());
                objresults.put("Terms related", terms);
                jointResults.put(objresults);
            }

        }
        String csvResults = CDL.toString(jointResults);
        if(csvResults.length()>0){
            csvResults.replace(",", ";").replace("\";\"", "\",\"");
        }
        System.out.println("store results...");
        FileUtils.writeStringToFile(new File("jointResults.csv"), csvResults);


    }

    public static JSONArray createGot(ArrayList<TestCaseDesign> testsuiteDesign, Ontology ontology) throws Exception {

        //Create GoT
        System.out.println("create got...");
        ArrayList<String> terms = new ArrayList<>();
        for (TestCaseDesign test : testsuiteDesign) {
            for (String purpose : test.getPurpose())
                terms.addAll(Mapping.processTestExpressionToExtractTerms(purpose));
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
            System.out.println("load test...");
            testsuiteDesign.addAll(Implementation.processTestCaseDesign(entry.getValue()));
        }
        return testsuiteDesign;
    }


}