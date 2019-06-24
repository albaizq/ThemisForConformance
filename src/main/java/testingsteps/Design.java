package testingsteps;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.*;
import tests.TestCaseDesign;
import tests.TestCaseImplementation;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class Design {
    public static ArrayList<TestCaseDesign> processTestCaseDesign(String filename) throws IOException, OWLOntologyCreationException {

        OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
        OWLOntology ontology =  manager.loadOntologyFromOntologyDocument(new File(filename));
        String source = "";
        ArrayList<TestCaseDesign> testsuite = new ArrayList<>();

        for (OWLNamedIndividual cls: ontology.getIndividualsInSignature()) {
            for (OWLAnnotationAssertionAxiom op : ontology.getAxioms(AxiomType.ANNOTATION_ASSERTION)) {
                if (cls.toString().replace(">","").
                        replace("<","").equals(op.getSubject().toString().replace("<","").replace(">",""))) {
                    if(op.getProperty().toString().contains("http://purl.org/dc/terms/source")){
                       source = op.getValue().toString();
                       break;
                    }
                }
            }
        }

        for (OWLIndividual cls: ontology.getIndividualsInSignature()) {
            TestCaseDesign tc = new TestCaseDesign();
            if(!ontology.getAxioms(AxiomType.ANNOTATION_ASSERTION).isEmpty())
                tc = processAnnotation(cls, ontology);
            else
                tc = processDataProperty(cls, ontology);
            tc.setSource(source);
            tc.setUri(IRI.create(cls.toString().replace("<","").replace(">","")));
            if(tc.getPurpose().size()>0){
                testsuite.add(tc);
            }
        }
        return testsuite;
    }

    public static String retrieveTestSuiteProv(String filename) throws OWLOntologyCreationException {
        OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
        OWLOntology ontology =  manager.loadOntologyFromOntologyDocument(new File(filename));

        for (OWLIndividual cls: ontology.getIndividualsInSignature()) {
            if(!ontology.getAxioms(AxiomType.ANNOTATION_ASSERTION).isEmpty())
                return getTestSuiteAnnotation(ontology);
            else
                return getTestSuiteDP(ontology);
        }
        return "";
    }

    public static String getTestSuiteAnnotation( OWLOntology ontology){

        for (OWLAnnotationAssertionAxiom op : ontology.getAxioms(AxiomType.ANNOTATION_ASSERTION)) {
            if(op.getProperty().toString().contains("http://purl.org/dc/terms/source")){
                return op.getValue().toString().replace("\"","").replace("  "," ").replace("^^xsd:string","").trim();
            }
        }
        return "";
    }

    public static String getTestSuiteDP(OWLOntology ontology){

        for (OWLDataPropertyAssertionAxiom dp : ontology.getAxioms(AxiomType.DATA_PROPERTY_ASSERTION)) {
            if(dp.getProperty().toString().contains("http://purl.org/dc/terms/source")){
                return dp.getObject().toString().replace("\"","").replace("  "," ").replace("^^xsd:string","").trim();
            }
        }
        return "";
    }

    public static TestCaseDesign processAnnotation(OWLIndividual cls, OWLOntology ontology){
        TestCaseDesign tc = new TestCaseDesign();
        String purpose = "";
        String source = "";
        String description = "";
        ArrayList<String> purposes = new ArrayList<>();
        tc.setUri((IRI) cls.asOWLNamedIndividual().getIRI());
        for (OWLAnnotationAssertionAxiom op : ontology.getAxioms(AxiomType.ANNOTATION_ASSERTION)) {
            if (cls.toString().replace(">","").replace("<","").equals(op.getSubject().toString().replace("<","").replace(">",""))) {
                if (op.getProperty().toString().contains("http://w3id.org/def/vtc#desiredBehaviour")) {
                    purpose = op.getValue().toString().replace("\"","").replace("  "," ").replace("^^xsd:string","").trim();
                    purposes.add(purpose);
                    tc.setPurpose(purposes);

                } else if (op.getProperty().toString().contains("http://purl.org/dc/terms/identifier")) {
                    source = op.getValue().toString().replace("\"","");
                    tc.setSource(source);
                } else if (op.getProperty().toString().contains("http://purl.org/dc/terms/description")) {
                    description = op.getValue().toString().replace("\"","");
                    tc.setDescription(description);
                }
            }else if(op.getProperty().toString().contains("http://purl.org/dc/terms/source")){
                tc.setProvenance(op.getValue().toString().replace("\"","").replace("  "," ").replace("^^xsd:string","").trim());
            }
        }
        return tc;
    }

    public static TestCaseDesign processDataProperty(OWLIndividual cls, OWLOntology ontology){
        TestCaseDesign tc = new TestCaseDesign();
        String purpose = "";
        String source = "";
        String description = "";
        ArrayList<String> purposes = new ArrayList<>();
        for (OWLDataPropertyAssertionAxiom dp : ontology.getAxioms(AxiomType.DATA_PROPERTY_ASSERTION)) {
            if (cls.toString().replace(">","").replace("<","").equals(dp.getSubject().toString().replace("<","").replace(">",""))) {
                if (dp.getProperty().toString().contains("http://w3id.org/def/vtc#desiredBehaviour")) {
                    purpose = dp.getObject().toString().replace("\"","").replace("  "," ").replace("^^xsd:string","").trim();
                    purposes.add(purpose);
                    tc.setPurpose(purposes);
                } else if (dp.getProperty().toString().contains("http://purl.org/dc/terms/identifier")) {
                    source = dp.getObject().toString().replace("\"","");
                    tc.setSource(source);
                } else if (dp.getProperty().toString().contains("http://purl.org/dc/terms/description")) {
                    description = dp.getObject().toString().replace("\"","");
                    tc.setDescription(description);
                }
            }else if(dp.getProperty().toString().contains("http://purl.org/dc/terms/source")){
                tc.setProvenance(dp.getObject().toString().replace("\"","").replace("  "," ").replace("^^xsd:string","").trim());
            }
        }
        return tc;
    }


}
