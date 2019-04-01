package tests;

import org.semanticweb.owlapi.model.IRI;

import java.util.ArrayList;
import java.util.LinkedHashMap;

public class TestCaseImplementation {
    private LinkedHashMap<String, String> axiomExpectedResult;
    private LinkedHashMap<String, String> assertions;
    private String preparation;
    private ArrayList<String> precondition;
    private ArrayList<String> preconditionQuery;
    private IRI relatedTestDesign;
    private IRI uri;
    private String type;

    public TestCaseImplementation() {
        this.axiomExpectedResult = new LinkedHashMap<String, String>();
        this.assertions = new LinkedHashMap<String, String>();
        this.precondition= new ArrayList<>();
    }

    public LinkedHashMap<String, String> getAxiomExpectedResult() {
        return axiomExpectedResult;
    }

    public void setAxiomExpectedResult(LinkedHashMap<String, String> axiomExpectedResult) {
        this.axiomExpectedResult = axiomExpectedResult;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public LinkedHashMap<String, String> getAssertions() {
        return assertions;
    }

    public void setAssertions(LinkedHashMap<String, String> assertions) {
        this.assertions = assertions;
    }

    public String getPreparation() {
        return preparation;
    }

    public void setPreparation(String preparation) {
        this.preparation = preparation;
    }

    public ArrayList<String> getPrecondition() {
        return precondition;
    }

    public void setPrecondition(ArrayList<String> precondition) {
        this.precondition = precondition;
    }

    public ArrayList<String> getPreconditionQuery() {
        return preconditionQuery;
    }

    public void setPreconditionQuery(ArrayList<String> preconditionQuery) {
        this.preconditionQuery = preconditionQuery;
    }

    public IRI getRelatedTestDesign() {
        return relatedTestDesign;
    }

    public void setRelatedTestDesign(IRI relatedTestDesign) {
        this.relatedTestDesign = relatedTestDesign;
    }

    public IRI getUri() {
        return uri;
    }

    public void setUri(IRI uri) {
        this.uri = uri;
    }
}
