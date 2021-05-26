package steps;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.*;
import tests.TestCaseDesign;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class TestDesigner {


    final static String STRING = "^^xsd:string";
    final static String SOURCE = "http://purl.org/dc/terms/source";
    final static String DESCRIPTION = "http://purl.org/dc/terms/description";
    final static String IDENTIFIER = "http://purl.org/dc/terms/identifier";
    final static String BEHAVIOUR = "https://w3id.org/def/vtc#desiredBehaviour";

    /*Taking as input a file, retrieve all the test and process them to generate TestCaseDesigns*/
    public static ArrayList<TestCaseDesign> processTestCaseDesign(String filename) throws  OWLOntologyCreationException {

        OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
        OWLOntology ontology =  manager.loadOntologyFromOntologyDocument(new File(filename));
        String source = "";
        ArrayList<TestCaseDesign> testsuite = new ArrayList<>();

        //Get source of each test design
        for (OWLNamedIndividual cls: ontology.getIndividualsInSignature()) {
            for (OWLAnnotationAssertionAxiom op : ontology.getAxioms(AxiomType.ANNOTATION_ASSERTION)) {
                String clsNoSymbols = cls.toString().replace(">","").
                        replace("<","");
                String opNoSymbols = op.getSubject().toString().replace("<","").replace(">","");
                if (clsNoSymbols.equals(opNoSymbols) && op.getProperty().toString().contains(SOURCE)){
                       source = op.getValue().toString();
                       break;
                    }

            }
        }
        //Get the rest of the information related each test design (description, identifier, behaviour)
        for (OWLIndividual cls: ontology.getIndividualsInSignature()) {
            TestCaseDesign tc;
            if(!ontology.getAxioms(AxiomType.ANNOTATION_ASSERTION).isEmpty())
                tc = processAnnotation(cls, ontology); //if it is processed as an annotation (this depends on OWL API)
            else
                tc = processDataProperty(cls, ontology); //if it is process as a data property
            tc.setSource(source);
            tc.setSubject("Requirement test");
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

        if(!ontology.getAxioms(AxiomType.ANNOTATION_ASSERTION).isEmpty())
            return getTestSuiteAnnotation(ontology);
        else
            return getTestSuiteDP(ontology);

    }

    public static String getTestSuiteAnnotation( OWLOntology ontology){

        for (OWLAnnotationAssertionAxiom op : ontology.getAxioms(AxiomType.ANNOTATION_ASSERTION)) {
            if(op.getProperty().toString().contains(SOURCE)){
                return op.getValue().toString().replace("\"","").replace("  "," ").replace(STRING,"").trim();
            }
        }
        return "";
    }

    public static String getTestSuiteDP(OWLOntology ontology){

        for (OWLDataPropertyAssertionAxiom dp : ontology.getAxioms(AxiomType.DATA_PROPERTY_ASSERTION)) {
            if(dp.getProperty().toString().contains(SOURCE)){
                return dp.getObject().toString().replace("\"","").replace("  "," ").replace(STRING,"").trim();
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
                if (op.getProperty().toString().contains(BEHAVIOUR)) {
                    purpose = op.getValue().toString().replace("\"","").replace("  "," ").replace(STRING,"").trim();
                    purposes.add(purpose);
                    tc.setPurpose(purposes);

                } else if (op.getProperty().toString().contains(IDENTIFIER)) {
                    source = op.getValue().toString().replace("\"","");
                    tc.setSource(source);
                } else if (op.getProperty().toString().contains(DESCRIPTION)) {
                    description = op.getValue().toString().replace("\"","");
                    tc.setDescription(description);
                }
            }else if(op.getProperty().toString().contains(SOURCE)){
                tc.setProvenance(op.getValue().toString().replace("\"","").replace("  "," ").replace(STRING,"").trim());
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
                if (dp.getProperty().toString().contains(BEHAVIOUR)) {
                    purpose = dp.getObject().toString().replace("\"","").replace("  "," ").replace(STRING,"").trim();
                    purposes.add(purpose);
                    tc.setPurpose(purposes);
                } else if (dp.getProperty().toString().contains(IDENTIFIER)) {
                    source = dp.getObject().toString().replace("\"","");
                    tc.setSource(source);
                } else if (dp.getProperty().toString().contains(DESCRIPTION)) {
                    description = dp.getObject().toString().replace("\"","");
                    tc.setDescription(description);
                }
            }else if(dp.getProperty().toString().contains(SOURCE)){
                tc.setProvenance(dp.getObject().toString().replace("\"","").replace("  "," ").replace(STRING,"").trim());
            }
        }
        return tc;
    }


}
