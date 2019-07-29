package tests;

import org.json.JSONException;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyStorageException;
import testingsteps.Design;
import testingsteps.Implementation;
import utils.Ontology;
import files.ProcessCSV;
import utils.Utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class TestingEnvironment {
    private ArrayList<Ontology> ontologies;
    private ArrayList<TestCaseDesign> testCaseDesigns;
    private ArrayList<TestCaseImplementation> testCaseImplementations;

    public ArrayList<Ontology> getOntologies() {
        return ontologies;
    }

    public void setOntologies(ArrayList<Ontology> ontologies) {
        this.ontologies = ontologies;
    }

    public ArrayList<TestCaseDesign> getTestCaseDesigns() {
        return testCaseDesigns;
    }

    public void setTestCaseDesigns(ArrayList<TestCaseDesign> testCaseDesigns) {
        this.testCaseDesigns = testCaseDesigns;
    }

    public ArrayList<TestCaseImplementation> getTestCaseImplementations() {
        return testCaseImplementations;
    }

    public void setTestCaseImplementations(ArrayList<TestCaseImplementation> testCaseImplementations) {
        this.testCaseImplementations = testCaseImplementations;
    }

    public TestingEnvironment createTestingEnvironment(String file) throws Exception {
        TestingEnvironment testingEnvironment = new TestingEnvironment();
        ArrayList<TestCaseImplementation> implementations = new ArrayList<>();
        HashMap<String, String> ontoAndTest = ProcessCSV.processCSVVocabs(file);
        ArrayList<TestCaseDesign> testsuiteDesign = new ArrayList<>();
        ArrayList<Ontology> ontologies = new ArrayList<>();
        HashMap<String, ArrayList<String>> termsResult = new HashMap<>();
        for (Map.Entry<String, String> entry : ontoAndTest.entrySet()) {
            //load tests
            testsuiteDesign.addAll(Utils.loadTest(entry.getValue()));
            //get provenance
            String testsuiteProv = Design.retrieveTestSuiteProv(entry.getValue());

            System.out.println("--"+testsuiteProv);
            //load ontology
            Ontology ontology = Utils.loadOntology(entry.getKey(),testsuiteDesign, testsuiteProv);
            ontologies.add(ontology);
            //executetBox
            ArrayList<TestCaseDesign> testCaseDesignsTerms = generateTermsTests(ontology, testsuiteProv);
            ArrayList<TestCaseImplementation> testCaseImplementationsTerms=  generateTermsTestsImplementation(testCaseDesignsTerms);
            for(TestCaseDesign testCaseDesign1  : testCaseDesignsTerms)
                testsuiteDesign.add(testCaseDesign1);
            for(TestCaseImplementation testCaseImplementation  : testCaseImplementationsTerms)
                implementations.add(testCaseImplementation);
            //create implementation
            for (TestCaseDesign testCaseDesign : testsuiteDesign) {
                Implementation impl = new Implementation();
                ArrayList<TestCaseImplementation> testsuiteImpl = impl.createTestImplementation(testsuiteDesign);
                //first the implementation of the test is created
                for (TestCaseImplementation tci : testsuiteImpl) {
                    if (testCaseDesign.getUri().toString().equals(tci.getRelatedTestDesign().toString())) {
                        implementations.add(tci);
                    }
                }
            }
        }
        testingEnvironment.setOntologies(ontologies);
        testingEnvironment.setTestCaseDesigns(testsuiteDesign);
        testingEnvironment.setTestCaseImplementations(implementations);
        return testingEnvironment;
    }

    public static ArrayList<TestCaseDesign> generateTermsTests(Ontology ontology, String testsuiteProv){
        int i = 1;
        ArrayList<TestCaseDesign> testCaseDesigns = new ArrayList<>();

        for (Map.Entry<String, IRI> entry : ontology.getClasses().entrySet()) {
            String term = entry.getKey();
            if(!term.isEmpty()) {
                TestCaseDesign testCaseDesign = new TestCaseDesign();
                ArrayList<String> purpose = new ArrayList<>();
                purpose.add(term + " type Class");
                testCaseDesign.setSource(testsuiteProv);
                testCaseDesign.setPurpose(purpose);
                testCaseDesign.setDescription(term + " exists in the ontology");
                testCaseDesign.setProvenance(ontology.getProv().toString());
                testCaseDesign.setUri(IRI.create(":testDesignTerms" + i + ontology.getProv().getFragment()));
                testCaseDesigns.add(testCaseDesign);
                testCaseDesign.setSubject("Term test");
                i++;
            }
        }
        for (Map.Entry<String, IRI> entry : ontology.getProperties().entrySet()) {
            String term = entry.getKey();
            if(!term.isEmpty()) {
                TestCaseDesign testCaseDesign = new TestCaseDesign();
                ArrayList<String> purpose = new ArrayList<>();
                purpose.add(term + " type Property");
                testCaseDesign.setSource(testsuiteProv);
                testCaseDesign.setPurpose(purpose);
                testCaseDesign.setProvenance(ontology.getProv().toString());
                testCaseDesign.setDescription(term + " exists in the ontology");
                testCaseDesign.setUri(IRI.create(":testDesignTerms" + i + ontology.getProv().getFragment()));
                testCaseDesigns.add(testCaseDesign);
                testCaseDesign.setSubject("Term test");
                i++;
            }
        }
        return  testCaseDesigns;
    }

    public static ArrayList<TestCaseImplementation> generateTermsTestsImplementation(ArrayList<TestCaseDesign> testCaseDesignsTerms) throws OWLOntologyCreationException, OWLOntologyStorageException {
        Implementation impl = new Implementation();
        ArrayList<TestCaseImplementation> testsuiteImpl = impl.createTestImplementation(testCaseDesignsTerms);
        return testsuiteImpl;
    }



}
