import glossary.GlossaryManager;
import org.json.*;
import org.semanticweb.owlapi.model.*;
import results.ResultGenerator;
import tests.TestCaseDesign;
import tests.TestCaseImplementation;
import steps.EnvironmentCreator;
import ontologies.Ontology;


import java.util.*;

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
        System.out.println("Loading tests....");
        EnvironmentCreator envClass= new EnvironmentCreator();
        EnvironmentCreator env = envClass.createTestingEnvironment(args[1]);
        JSONArray tableResultsAllOntologies = new JSONArray();
        ArrayList<Ontology> ontologies = env.getOntologies();

        ArrayList<TestCaseDesign> testsuiteDesign = env.getTestCaseDesigns();
        ArrayList<TestCaseImplementation> implementations = env.getTestCaseImplementations();
        HashMap<String, IRI> gotTotal = new HashMap<String, IRI>();
        // execute tests
        ResultGenerator resultGenerator = new ResultGenerator();
        GlossaryManager glossaryManager = new GlossaryManager();
        int i = 1;
        for(Ontology ontology: ontologies){
             //create got per ontology
            System.out.println("ONTOLOGY: "+ ontology.getProv());
            resultGenerator.setTestCaseDesigns(testsuiteDesign); // load tests for the ontology
            HashMap<String, IRI> got = (HashMap<String, IRI>) glossaryManager.createGot(ontology, i);
            System.out.println("---------Glossary of terms---------");
            System.out.println(got); // print got
            System.out.println("---------End glossary of terms---------");
            //update the got if needed
            System.out.println("Is the GoT ok? (if not, you can change the got and store it)");
            Scanner scanner = new Scanner(System.in);
            String result = scanner.nextLine();
            while (!result.equals("y")) {
                System.out.println("Is the GoT ok? ");
                scanner = new Scanner(System.in);
                result = scanner.nextLine();
            }
            glossaryManager.updateGot(i);

            // Execute all tests on each ontology using the got
            JSONArray tableResults = resultGenerator.executeTests(ontology, implementations, glossaryManager.getGot());
            //execute tests terms
            JSONArray tableResultsTerms = resultGenerator.executeTestsTerms(ontology, implementations, glossaryManager.getGot());
            gotTotal.putAll(glossaryManager.getGot());
            /*Store the results*/
            resultGenerator.storeIndividualResults(tableResults, tableResultsTerms, tableResultsAllOntologies, i);
            i++;
        }

        /*Join results*/
        //storeJointResults(testsuiteDesign, ontologies, tableResultsAllOntologies, gotTotal);
        //analyseReqs(testsuiteDesign);
    }



}