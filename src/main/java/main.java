import de.derivo.sparqldlapi.exceptions.QueryEngineException;
import de.derivo.sparqldlapi.exceptions.QueryParserException;
import org.apache.commons.io.FileUtils;
import org.json.*;
import org.semanticweb.owlapi.model.*;
import testingsteps.Execution;
import testingsteps.Implementation;
import tests.TestCaseDesign;
import tests.TestCaseImplementation;
import tests.TestCaseResult;
import tests.TestingEnvironment;
import utils.Ontology;
import files.ProcessCSV;
import files.Report;
import utils.Utils;


import java.io.*;
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
        //initialize variables
        TestingEnvironment envClass= new TestingEnvironment();
        TestingEnvironment env = envClass.createTestingEnvironment(args[1]);
        JSONArray tableResultsAllOntologies = new JSONArray();
        ArrayList<Ontology> ontologies = env.getOntologies();
        ArrayList<TestCaseDesign> testsuiteDesign = env.getTestCaseDesigns();
        ArrayList<TestCaseImplementation> implementations = env.getTestCaseImplementations();
        HashMap<String, IRI> gotTotal = new HashMap<String, IRI>();

        // execute tests
        int i = 1;
        for(Ontology ontology: ontologies){
             //create got per ontology
            System.out.println("ONTOLOGY "+ ontology.getProv());
            HashMap<String, IRI> got = createGot(testsuiteDesign,ontology, i);
            //if the got is ok then
            System.out.println("Is the GoT ok? ");
            Scanner scanner = new Scanner(System.in);
            String result = scanner.nextLine();
            while (!result.equals("y")) {
                System.out.println("Is the GoT ok? ");
                scanner = new Scanner(System.in);
                result = scanner.nextLine();
            }
            got = updateGot(i);


            // Execute all tests on each ontology using the got
            JSONArray tableResults = executeTests(testsuiteDesign, ontology, implementations, got);
            //execute tests terms
            JSONArray tableResultsTerms = executeTestsTerms(testsuiteDesign, ontology, implementations, got);
            gotTotal.putAll(got);
            /*Store the results*/
            storeIndividualResults(tableResults, tableResultsTerms, tableResultsAllOntologies, i);
            i++;
        }

        /*Join results*/
        storeJointResults(testsuiteDesign, ontologies, tableResultsAllOntologies, gotTotal);
        analyseReqs(testsuiteDesign);
    }


    public static  JSONArray storeIndividualResults(JSONArray tableResults, JSONArray tableResultsTerms, JSONArray tableResultsAllOntologies, int i) throws JSONException, IOException {
         /*Individual results*/
        String csvResults = CDL.toString(tableResults).replace(",", ";").replace("\";\"", "\",\"");
        System.out.println("store individual results...");
        FileUtils.writeStringToFile(new File("results-o"+i+".csv"), csvResults);
        String csvResultsTerms = CDL.toString(tableResultsTerms).replace(",", ";").replace("\";\"", "\",\"");
        System.out.println("store individual results...");
        FileUtils.writeStringToFile(new File("results-o"+i+"Terms.csv"), csvResultsTerms);
        for(int j = 0; j< tableResults.length();j++) {
            tableResultsAllOntologies.put(tableResults.getJSONObject(j));
        }
        for(int j = 0; j< tableResultsTerms.length();j++) {
            tableResultsAllOntologies.put(tableResultsTerms.getJSONObject(j));
        }
        return tableResultsAllOntologies;
    }

    public static void analyseReqs(ArrayList<TestCaseDesign> testsuiteDesign) throws JSONException, IOException {
        /*if two requirements has the same formalization --> they are related*/
        JSONArray relatedRequirments = new JSONArray();
        for (TestCaseDesign testCaseDesign : testsuiteDesign) {
            TestCaseDesign testCaseDesignComparar = new TestCaseDesign();
            ArrayList<String> realtedReqs = new ArrayList<>();
            realtedReqs.add(testCaseDesign.getDescription());
            for (TestCaseDesign testCaseDesign2 : testsuiteDesign) {
                if (!testCaseDesign.getProvenance().toString().equals(testCaseDesign2.getProvenance().toString())) {
                    for(String purpose1: testCaseDesign.getPurpose()){
                        for(String purpose2: testCaseDesign2.getPurpose()){
                            if(purpose1.trim().equals(purpose2.trim())){
                                testCaseDesignComparar = testCaseDesign2;
                                realtedReqs.add(testCaseDesign2.getDescription());
                                break;
                            }
                        }
                    }
                }
            }
            if(realtedReqs.size()>1) {
                JSONObject objresults = new JSONObject();
                objresults.put("Relations", testCaseDesign.getDescription()+" - "+ testCaseDesignComparar.getDescription());
                objresults.put("Ontologies", testCaseDesign.getProvenance()+" - "+ testCaseDesignComparar.getProvenance());
                relatedRequirments.put(objresults);
            }
        }
        String csvResults = CDL.toString(relatedRequirments);
        if(csvResults != null){
            csvResults = csvResults.replace(",", ";").replace("\";\"", "\",\"");
        }
        FileUtils.writeStringToFile(new File("relatedRequirements.csv"), csvResults);
    }

    public static  void storeJointResults(ArrayList<TestCaseDesign> testsuiteDesign, ArrayList<Ontology> ontologies, JSONArray tableResultsAllOntologies, HashMap<String, IRI> gottotal) throws JSONException, IOException {
        System.out.println("create joint results...");
        JSONArray jointResults = jointResults( testsuiteDesign,  ontologies,  tableResultsAllOntologies,  gottotal );
        System.out.println("store joint results...");
        String csvResults = CDL.toString(jointResults);
        if(csvResults != null){
            csvResults = csvResults.replace(",", ";").replace("\";\"", "\",\"");
        }
        FileUtils.writeStringToFile(new File("jointResults.csv"), csvResults);
    }

    public  static JSONArray executeTests(ArrayList<TestCaseDesign> testsuiteDesign, Ontology ontology, ArrayList<TestCaseImplementation> implementations, HashMap<String, IRI> got) throws OWLOntologyStorageException, QueryParserException, QueryEngineException, OWLOntologyCreationException, JSONException {
        Execution exec = new Execution();
        JSONArray tableResults = new JSONArray();
        JSONObject array = new JSONObject();
        ArrayList<TestCaseResult> testCaseResults = new ArrayList<>();
        System.out.println("create individual results...");
        for (TestCaseDesign testCaseDesign : testsuiteDesign) {
            // this is a report PER ONTOLOGY (individual)
            if(!testCaseDesign.getSubject().equals("Term test")) {
                ArrayList<TestCaseImplementation> implementationsForTestDesign = new ArrayList<>();
                for (TestCaseImplementation tci : implementations) {
                    if (testCaseDesign.getUri().toString().equals(tci.getRelatedTestDesign().toString())) {
                        implementationsForTestDesign.add(tci);
                    }
                }

                testCaseResults = exec.execute(implementationsForTestDesign, ontology, got);

                array = Report.printReportPerOntology(ontology, testCaseDesign, testCaseResults);
                tableResults.put(array);
            }
        }
        return tableResults;
    }

    public  static JSONArray executeTestsTerms(ArrayList<TestCaseDesign> testsuiteDesign, Ontology ontology, ArrayList<TestCaseImplementation> implementations, HashMap<String, IRI> got) throws OWLOntologyStorageException, QueryParserException, QueryEngineException, OWLOntologyCreationException, JSONException {
        Execution exec = new Execution();
        JSONArray tableResults = new JSONArray();
        JSONObject array = new JSONObject();
        ArrayList<TestCaseResult> testCaseResults = new ArrayList<>();
        System.out.println("create individual results...");
        for (TestCaseDesign testCaseDesign : testsuiteDesign) {
            // this is a report PER ONTOLOGY (individual)
            if(testCaseDesign.getSubject().equals("Term test")) {
                ArrayList<TestCaseImplementation> implementationsForTestDesign = new ArrayList<>();
                for (TestCaseImplementation tci : implementations) {
                    if (testCaseDesign.getUri().toString().equals(tci.getRelatedTestDesign().toString())) {
                        implementationsForTestDesign.add(tci);
                    }
                }

                testCaseResults = exec.execute(implementationsForTestDesign, ontology, got);

                array = Report.printReportPerOntology(ontology, testCaseDesign, testCaseResults);
                tableResults.put(array);
            }
        }
        return tableResults;
    }

    public static HashMap<String, IRI> createGot(ArrayList<TestCaseDesign> testsuiteDesign, Ontology ontology, int i) throws Exception {
        HashMap<String, IRI> got = Utils.createGot(testsuiteDesign, ontology);
        Utils.storeFile("got-o"+i+".csv", got);
        return got;
    }

    public static HashMap<String, IRI>  updateGot(int i) throws JSONException, FileNotFoundException {
        HashMap<String, IRI>  gottotal = ProcessCSV.processCSVGoTHash("got-o"+i+".csv");
        return  gottotal;
    }

    public static JSONArray jointResults(ArrayList<TestCaseDesign> testsuiteDesign, ArrayList<Ontology> ontologies, JSONArray tableResultsAllOntologies, HashMap<String, IRI> gottotal ) throws JSONException {

        JSONArray jointResults = new JSONArray();
        HashMap<Ontology, ArrayList<String>> termsInvolvedInTest = new HashMap<>();
        //if several ontologies passes the same tests, the terms involved in that test may be related
        for (TestCaseDesign testCaseDesign : testsuiteDesign) {
            ArrayList<String> termValueInOntology = new ArrayList<>(); // term with uri
            ArrayList<String> termKeys = new ArrayList<>(); //term without uri
            ArrayList<String> ontologiesRelated = new ArrayList<>();
            for (Ontology ontology : ontologies) {
                for (int j = 0; j < tableResultsAllOntologies.length(); j++) {
                    JSONObject obj = tableResultsAllOntologies.getJSONObject(j);
                   //get competency question of the requirement, and the ontology that passes the associated test
                    if (obj.has("Requirement description")) {
                        if (obj.getString("Requirement description").equals(testCaseDesign.getDescription()) && obj.getString("Ontology").equals(ontology.getProv().toString()) && obj.getString("Result").equals("passed")&& !ontologiesRelated.contains(ontology.getProv().toString())) {
                            ontologiesRelated.add(ontology.getProv().toString());
                            for (String purpose : testCaseDesign.getPurpose())
                                termKeys.addAll(processTestExpressionToExtractTerms(purpose));
                            //get terms involved in the test
                            for (String term : termKeys) {
                                for (Map.Entry<String, IRI> entry : gottotal.entrySet()) {
                                    if (entry.getKey().equals(term) && !termValueInOntology.contains(entry.getKey())) {
                                        termValueInOntology.add(entry.getKey()); //get uri of the term
                                    }
                                }

                            }

                            termsInvolvedInTest.put(ontology, termValueInOntology);
                        }
                    }
                }
            }
            //store results
            if (ontologiesRelated.size() > 1) {
                JSONObject objresults = new JSONObject();
                objresults.put("Requirement description", testCaseDesign.getDescription());
                objresults.put("Ontologies that passes the test", ontologiesRelated);
                objresults.put("Provenance", testCaseDesign.getProvenance());
                objresults.put("Terms related", termValueInOntology);
                jointResults.put(objresults);
            }
        }
        return jointResults;
    }


}