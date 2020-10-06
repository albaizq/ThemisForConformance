package tests;

import org.semanticweb.owlapi.model.IRI;

import java.util.ArrayList;
import java.util.LinkedHashMap;

/*Class for managing test designs*/
public class TestCaseDesign {
    private IRI uri;
    private ArrayList<String> purpose;
    private String description;
    private String source; // link between the test case and the ontology requirement
    private String subject;
    private String provenance; // from which ontology each req came from

    public TestCaseDesign() {
        purpose = new ArrayList<>();
    }

    public IRI getUri() {
        return uri;
    }

    public void setUri(IRI uri) {
        this.uri = uri;
    }

    public String getDescription() {
        return description;
    }

    public ArrayList<String> getPurpose() {
        return purpose;
    }

    public void setPurpose(ArrayList<String> purpose) {
        this.purpose = purpose;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getProvenance() {
        return provenance;
    }

    public void setProvenance(String provenance) {
        this.provenance = provenance;
    }
}
