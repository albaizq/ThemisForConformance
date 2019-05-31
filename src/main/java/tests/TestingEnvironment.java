package tests;

import utils.Ontology;

import java.util.ArrayList;

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
}
