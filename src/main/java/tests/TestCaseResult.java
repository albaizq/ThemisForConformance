package tests;

import org.semanticweb.owlapi.model.IRI;

import java.util.ArrayList;

/*Class for managing test results*/
public class TestCaseResult {
    private IRI relatedTestImpl;
    private String testResult;
    private IRI ontologyURI;
    private ArrayList<String> undefinedTerms;


    public TestCaseResult() {
        testResult = "";
    }

    public ArrayList<String> getUndefinedTerms() {
        return undefinedTerms;
    }

    public void setUndefinedTerms(ArrayList<String> undefinedTerms) {
        this.undefinedTerms = undefinedTerms;
    }

    public IRI getRelatedTestImpl() {
        return relatedTestImpl;
    }

    public void setRelatedTestImpl(IRI relatedTestImpl) {
        this.relatedTestImpl = relatedTestImpl;
    }

    public String getTestResult() {
        return testResult;
    }

    public void setTestResult(String testResult) {
        this.testResult = testResult;
    }

    public IRI getOntologyURI() {
        return ontologyURI;
    }

    public void setOntologyURI(IRI ontologyURI) {
        this.ontologyURI = ontologyURI;
    }
}
