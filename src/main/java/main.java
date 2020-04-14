import org.json.*;
import org.semanticweb.owlapi.model.*;
import tests.TestCaseDesign;
import tests.TestCaseImplementation;
import result.TestingEnvironment;
import ontologies.Ontology;


import java.util.*;

import static result.ResultGenerator.*;

/**
 * Created by Alba on 01/02/2017.
 */
public class main {
    public static void main(String[] args) throws Exception {
        if (args.length != 2) {
            System.out.println("usage: java -jar themisForConformance.jar -i testfile ");
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
            System.out.println("Is the GoT ok? (if not, you can change the got and store it)");
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



}