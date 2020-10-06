package results;

import de.derivo.sparqldlapi.exceptions.QueryEngineException;
import de.derivo.sparqldlapi.exceptions.QueryParserException;
import org.apache.commons.io.FileUtils;
import org.json.CDL;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyStorageException;
import steps.TestExecuter;
import tests.TestCaseDesign;
import tests.TestCaseImplementation;
import tests.TestCaseResult;
import ontologies.Ontology;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static results.TermsExtractor.processTestExpressionToExtractTerms;


/*Class for executing tests and generating the results to be presented to the users. Individual and joint results. */
public class ResultGenerator {

    final static String DESCRIPTION = "Requirements description";
    static final String SEMICOLON = "\";\"";
    static final String COMA = "\",\"";

    private ArrayList<TestCaseDesign> testCaseDesigns;

    public ResultGenerator() {
    }

    public ArrayList<TestCaseDesign> getTestCaseDesigns() {
        return testCaseDesigns;
    }

    public void setTestCaseDesigns(ArrayList<TestCaseDesign> testCaseDesigns) {
        this.testCaseDesigns = testCaseDesigns;
    }


    public  JSONArray executeTests(Ontology ontology, List<TestCaseImplementation> implementations, Map<String, IRI> got) throws OWLOntologyStorageException, QueryParserException, QueryEngineException, OWLOntologyCreationException, JSONException {
        JSONArray tableResults = new JSONArray();
        System.out.println("create individual results for tests...");
        for (TestCaseDesign testCaseDesign : this.getTestCaseDesigns()) {
            // this is a report PER ONTOLOGY (individual)
            if (!testCaseDesign.getSubject().equals("Term test")) {
                tableResults = executeTest(ontology, testCaseDesign, implementations,  tableResults, (HashMap<String, IRI>) got);
            }
        }
        return tableResults;
    }

    public JSONArray executeTestsTerms( Ontology ontology, List<TestCaseImplementation> implementations, Map<String, IRI> got) throws OWLOntologyStorageException, QueryParserException, QueryEngineException, OWLOntologyCreationException, JSONException {
        JSONArray tableResults = new JSONArray();
        System.out.println("create individual results for term tests...");
        for (TestCaseDesign testCaseDesign : this.getTestCaseDesigns()) {
            // this is a report PER ONTOLOGY (individual)
            if (testCaseDesign.getSubject().equals("Term test")) {
               tableResults = executeTest(ontology, testCaseDesign, implementations,  tableResults, (HashMap<String, IRI>) got);
            }
        }
        return tableResults;
    }


    public JSONArray executeTest(Ontology ontology, TestCaseDesign testCaseDesign, List<TestCaseImplementation> implementations, JSONArray tableResults, HashMap<String, IRI> got){
        ArrayList<TestCaseImplementation> implementationsForTestDesign = new ArrayList<>();
        for (TestCaseImplementation tci : implementations) {
            if (testCaseDesign.getUri().toString().equals(tci.getRelatedTestDesign().toString())) {
                implementationsForTestDesign.add(tci);
            }
        }

        TestExecuter exec = new TestExecuter();
        ArrayList<TestCaseResult> testCaseResults = new ArrayList<>();

        testCaseResults = exec.execute(implementationsForTestDesign, ontology, (HashMap<String, IRI>) got);


        JSONObject object = null;
        try {
            object = Report.printReportPerOntology(ontology, testCaseDesign, testCaseResults);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return  tableResults.put(object);

    }

    public  JSONArray jointResults( List<Ontology> ontologies, JSONArray tableResultsAllOntologies, Map<String, IRI> gottotal ) throws JSONException {
        JSONArray jointResults = new JSONArray();
        HashMap<Ontology, ArrayList<String>> termsInvolvedInTest = new HashMap<>();
        //if several ontologies passes the same tests, the terms involved in that test may be related
        for (TestCaseDesign testCaseDesign : this.getTestCaseDesigns()) {
            ArrayList<String> termValueInOntology = new ArrayList<>(); // term with uri
            ArrayList<String> termKeys = new ArrayList<>(); //term without uri
            ArrayList<String> relatedOntologies = new ArrayList<>();
            for (Ontology ontology : ontologies) {
                for (int j = 0; j < tableResultsAllOntologies.length(); j++) {
                    JSONObject obj = tableResultsAllOntologies.getJSONObject(j);
                    //get competency question of the requirement, and the ontology that passes the associated test
                    if (obj.has(DESCRIPTION) && obj.getString(DESCRIPTION).equals(testCaseDesign.getDescription()) && obj.getString("Ontology").equals(ontology.getProv().toString()) && obj.getString("Result").equals("passed")&& !relatedOntologies.contains(ontology.getProv().toString())) {
                            relatedOntologies.add(ontology.getProv().toString());
                            //get terms involved in the test
                            termKeys  = (ArrayList<String>) getTermsInPurpose ( testCaseDesign, termKeys);
                            //get terms from the ontology
                            termValueInOntology = (ArrayList<String>) getTermsInvolvedInTest(gottotal,termKeys, termValueInOntology);

                            termsInvolvedInTest.put(ontology, termValueInOntology);
                        }
                    }
                }
           //store results
            storeJointResults(testCaseDesign,   relatedOntologies,   termValueInOntology,  jointResults);
        }
        return jointResults;
    }

    public  List<String> getTermsInPurpose (TestCaseDesign testCaseDesign, List<String> termKeys){
        for (String purpose : testCaseDesign.getPurpose())
            termKeys.addAll(processTestExpressionToExtractTerms(purpose));
        return termKeys;
    }
    public  List<String> getTermsInvolvedInTest(Map<String, IRI> gottotal, List<String> termKeys, List<String> termValueInOntology){
        for (String term : termKeys) {
            for (Map.Entry<String, IRI> entry : gottotal.entrySet()) {
                if (entry.getKey().equals(term) && !termValueInOntology.contains(entry.getKey())) {
                    termValueInOntology.add(entry.getKey()); //get uri of the term
                }
            }

        }
        return termValueInOntology;
    }


    public static JSONArray storeJointResults(TestCaseDesign testCaseDesign,  List<String> relatedOntologies,  List<String> termValueInOntology, JSONArray jointResults) throws JSONException {
        if (relatedOntologies.size() > 1) {
            JSONObject reqResult = new JSONObject();
            reqResult.put("Requirement description", testCaseDesign.getDescription());
            reqResult.put("Ontologies that passes the test", relatedOntologies);
            reqResult.put("Provenance", testCaseDesign.getProvenance());
            reqResult.put("Terms related", termValueInOntology);
            jointResults.put(reqResult);
        }
        return jointResults;
    }

    public JSONArray storeIndividualResults(JSONArray tableResults, JSONArray tableResultsTerms, JSONArray tableResultsAllOntologies, int i) throws JSONException, IOException {
        /*Individual results*/
        String csvResults = CDL.toString(tableResults).replace(",", ";").replace(SEMICOLON, COMA);
        System.out.println("store individual results...");
        FileUtils.writeStringToFile(new File("results-o" + i + ".csv"), csvResults);
        String csvResultsTerms = CDL.toString(tableResultsTerms).replace(",", ";").replace(SEMICOLON, COMA);
        System.out.println("store individual results...");
        FileUtils.writeStringToFile(new File("results-o" + i + "Terms.csv"), csvResultsTerms);
        for (int j = 0; j < tableResults.length(); j++) {
            tableResultsAllOntologies.put(tableResults.getJSONObject(j));
        }
        for (int j = 0; j < tableResultsTerms.length(); j++) {
            tableResultsAllOntologies.put(tableResultsTerms.getJSONObject(j));
        }
        return tableResultsAllOntologies;
    }


    public  void storeJointResults(List<TestCaseDesign> testsuiteDesign, List<Ontology> ontologies, JSONArray tableResultsAllOntologies, Map<String, IRI> gotTotal) throws JSONException, IOException {
        System.out.println("create joint results...");
        JSONArray jointResults = jointResults((ArrayList<Ontology>) ontologies, tableResultsAllOntologies, gotTotal);
        System.out.println("store joint results...");
        String csvResults = CDL.toString(jointResults);
        if (csvResults != null) {
            csvResults = csvResults.replace(",", ";").replace(SEMICOLON, COMA);
        }
        FileUtils.writeStringToFile(new File("jointResults.csv"), csvResults);
    }
}
