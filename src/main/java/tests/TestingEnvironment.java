package tests;

import testingsteps.Implementation;
import utils.Ontology;
import utils.ProcessCSV;
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
        for (Map.Entry<String, String> entry : ontoAndTest.entrySet()) {
            //load tests
            testsuiteDesign.addAll(Utils.loadTest(entry.getValue()));
            //load ontology
            ontologies.add(Utils.loadOntology(entry.getKey(),testsuiteDesign));
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
}
