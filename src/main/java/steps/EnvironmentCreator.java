package steps;

import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyStorageException;
import tests.TestCaseDesign;
import tests.TestCaseImplementation;
import ontologies.Ontology;
import files.ProcessCSV;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static ontologies.Ontology.loadOntology;

/*Initialise the set of test designs and ontologies that are provided as input*/
public class EnvironmentCreator {
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

    public EnvironmentCreator createTestingEnvironment(String file) throws Exception {
        EnvironmentCreator testingEnvironment = new EnvironmentCreator();
        ArrayList<TestCaseImplementation> implementations = new ArrayList<>();
        HashMap<String, String> ontoAndTest = ProcessCSV.processCSVVocabs(file);
        ArrayList<TestCaseDesign> testsuiteDesign = new ArrayList<>();
        ArrayList<Ontology> ontologiesList = new ArrayList<>();
        for (Map.Entry<String, String> entry : ontoAndTest.entrySet()) {
            //load tests
            testsuiteDesign.addAll(loadTest(entry.getValue()));
            //get provenance
            String testsuiteProv = TestDesigner.retrieveTestSuiteProv(entry.getValue());
            //load ontology
            Ontology ontology = loadOntology(entry.getKey(),testsuiteDesign, testsuiteProv);
            ontologiesList.add(ontology);
            //executetBox
            ArrayList<TestCaseDesign> testCaseDesignsTerms = generateTermsTests(ontology, testsuiteProv);
            ArrayList<TestCaseImplementation> testCaseImplementationsTerms=generateTermsTestsImplementation(testCaseDesignsTerms);
            for(TestCaseDesign testCaseDesign1  : testCaseDesignsTerms)
                testsuiteDesign.add(testCaseDesign1);
            for(TestCaseImplementation testCaseImplementation  : testCaseImplementationsTerms)
                implementations.add(testCaseImplementation);
            //create implementation
            for (TestCaseDesign testCaseDesign : testsuiteDesign) {
                TestImplementer impl = new TestImplementer();
                ArrayList<TestCaseImplementation> testsuiteImpl = impl.createTestImplementation(testsuiteDesign);
                //first the implementation of the test is created
                for (TestCaseImplementation tci : testsuiteImpl) {
                    if (testCaseDesign.getUri().toString().equals(tci.getRelatedTestDesign().toString())) {
                        implementations.add(tci);
                    }
                }
            }
        }
        testingEnvironment.setOntologies(ontologiesList);
        testingEnvironment.setTestCaseDesigns(testsuiteDesign);
        testingEnvironment.setTestCaseImplementations(implementations);
        return testingEnvironment;
    }

    /*Generate a set of tests (type: exists class A and exists property P) from the glossary of terms of each ontology*/
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

    /*Implement tests extracted from the glossary of terms*/
    public static ArrayList<TestCaseImplementation> generateTermsTestsImplementation(ArrayList<TestCaseDesign> testCaseDesignsTerms) throws OWLOntologyCreationException, OWLOntologyStorageException {
        TestImplementer impl = new TestImplementer();
        ArrayList<TestCaseImplementation> testsuiteImpl = impl.createTestImplementation(testCaseDesignsTerms);
        return testsuiteImpl;
    }

    /*Load tests design from file*/
    public static ArrayList<TestCaseDesign> loadTest(String file) throws Exception {

        ArrayList<TestCaseDesign> testsuiteDesign = new ArrayList<>();
        testsuiteDesign.addAll(TestDesigner.processTestCaseDesign(file));
        return testsuiteDesign;
    }

}
