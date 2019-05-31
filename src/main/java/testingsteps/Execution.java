package testingsteps;

import com.clarkparsia.pellet.owlapiv3.PelletReasoner;
import de.derivo.sparqldlapi.Query;
import de.derivo.sparqldlapi.QueryEngine;
import de.derivo.sparqldlapi.QueryResult;
import de.derivo.sparqldlapi.exceptions.QueryEngineException;
import de.derivo.sparqldlapi.exceptions.QueryParserException;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.semanticweb.HermiT.Configuration;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.reasoner.*;
import org.semanticweb.owlapi.reasoner.structural.StructuralReasonerFactory;
import tests.TestCaseDesign;
import tests.TestCaseImplementation;
import tests.TestCaseResult;
import utils.Ontology;
import utils.Utils;
import java.util.*;
import java.util.stream.Collectors;
import static testingsteps.Mapping.processTestExpressionToExtractTerms;


/**
 * Created by albafernandez on 19/06/2017.
 */
public class Execution {

    /*Here we check the ontology consistency regarding at model-level*/
    public  String tboxTest(String query, IRI key, OWLOntologyManager manager, OWLOntology ontology) throws QueryParserException, QueryEngineException {

        StructuralReasonerFactory factory = new StructuralReasonerFactory();
        OWLReasonerConfiguration config = new SimpleConfiguration();
        OWLReasoner reasoner = factory.createReasoner(ontology,config);
        reasoner.precomputeInferences();
        Query queryOWLAPI = null;
        try {
            queryOWLAPI = Query.create(query);
            QueryEngine engine = QueryEngine.create(manager, reasoner);
            QueryResult result;
            // Execute the query and generate the result set
            result = engine.execute(queryOWLAPI);
            if (queryOWLAPI.isAsk()) {
                if (result.ask()) {
                    return "true";
                } else {
                    return "false";
                }
            } else
                return "false";
        } catch (Exception e) {
            System.out.println("Check SPARQL-DL syntax: " + e.getMessage() + " ID: " + key);
        }
        return "false";
    }

    /*Here we check the ontology consistency regarding at instance-level*/
    public  String aboxTest(String preparation, Ontology ontology, String type, JSONArray got) {

        String textAxioms = Utils.mappingValue(preparation, ontology.getProv().toString(), got);
        // We will create several things, so we save an instance of the data factory
        OWLDataFactory dataFactory = ontology.getManager().getOWLDataFactory();
        Configuration configuration = new Configuration();
        configuration.throwInconsistentOntologyException = false;
        configuration.ignoreUnsupportedDatatypes = true;

        Set<OWLAxiom> axioms = Utils.convertStringToAxioms(textAxioms.replace("\\\"", "\""));
        ArrayList<OWLAxiom> axiommodify = new ArrayList<>();
        for (OWLAxiom axiom : axioms) {
            AddAxiom addAxiom = null;
            if(axiom.isAnnotationAxiom()){ //Sometimes the object properties are translated as annotated axioms incorrectly and they have to be changed.
                OWLAnnotationAssertionAxiom annotationAssertionAxiom = (OWLAnnotationAssertionAxiom)axiom;
                OWLNamedIndividual ind1 = dataFactory.getOWLNamedIndividual(IRI.create(annotationAssertionAxiom.getSubject().toString()));
                OWLNamedIndividual ind2 = dataFactory.getOWLNamedIndividual(IRI.create(annotationAssertionAxiom.getValue().toString()));
                if(ontology.getOntology().containsObjectPropertyInSignature(IRI.create(annotationAssertionAxiom.getProperty().toString().replace(">","").replace("<","")))){
                    OWLObjectProperty prop = dataFactory.getOWLObjectProperty(IRI.create(annotationAssertionAxiom.getProperty().toString().replace(">","").replace("<","")));
                    OWLObjectPropertyAssertionAxiom owlObjectPropertyAssertionAxiom = dataFactory.getOWLObjectPropertyAssertionAxiom(
                            prop, ind1,ind2);
                    if(!ontology.getOntology().getAxioms().contains(axiom)) {
                        addAxiom = new AddAxiom(ontology.getOntology(), owlObjectPropertyAssertionAxiom);
                        axiommodify.add(owlObjectPropertyAssertionAxiom);
                    }
                }else{
                    OWLDataProperty prop = dataFactory.getOWLDataProperty(IRI.create(annotationAssertionAxiom.getProperty().toString().replace(">","").replace("<","")));
                    OWLDataPropertyAssertionAxiom owlDataPropertyAssertionAxiom = null;
                    if(annotationAssertionAxiom.getValue().toString().contains("string")){
                        int i = 1;
                        owlDataPropertyAssertionAxiom = dataFactory.getOWLDataPropertyAssertionAxiom(
                                prop, ind1,i);
                    }else if(annotationAssertionAxiom.getValue().toString().contains("int") || annotationAssertionAxiom.getValue().toString().contains("float")){
                        owlDataPropertyAssertionAxiom = dataFactory.getOWLDataPropertyAssertionAxiom(
                                prop, ind1,"value");
                    }else{
                        float i = (float)1.0;
                        owlDataPropertyAssertionAxiom = dataFactory.getOWLDataPropertyAssertionAxiom(
                                prop, ind1,i);
                    }

                    if(!ontology.getOntology().getAxioms().contains(axiom)) {
                        addAxiom = new AddAxiom(ontology.getOntology(), owlDataPropertyAssertionAxiom);
                        axiommodify.add(owlDataPropertyAssertionAxiom);
                    }
                }
            }else {
                if(!ontology.getOntology().getAxioms().contains(axiom)){
                    addAxiom = new AddAxiom(ontology.getOntology(), axiom);
                    axiommodify.add(axiom);
                }
            }
            if(addAxiom!=null)
                ontology.getManager().applyChange(addAxiom);
        }
        PelletReasoner reasoner = com.clarkparsia.pellet.owlapiv3.PelletReasonerFactory.getInstance().createReasoner( ontology.ontology );
        String result;
        if(!reasoner.isConsistent())
            result = "inconsistent";
        else if(reasoner.getUnsatisfiableClasses().getSize()>1)
            result = "unsatisfiable";
        else
            result= "consistent";
        if(!type.equals("preparation")) {
            ontology.getManager().removeAxioms(ontology.getOntology(), axiommodify.stream().collect(Collectors.toSet()));
        }
        return result;
    }

    public void removePreparationAxioms(String preparation, Ontology ontology, JSONArray got){

        String textAxioms = Utils.mappingValue(preparation, ontology.getProv().toString(), got);

        Set<OWLAxiom> axioms = Utils.convertStringToAxioms(textAxioms.replace("\\\"", "\""));
        OWLDataFactory dataFactory = ontology.getManager().getOWLDataFactory();
        Configuration configuration = new Configuration();
        configuration.throwInconsistentOntologyException = false;
        configuration.ignoreUnsupportedDatatypes = true;

        for (OWLAxiom axiom : axioms) {
            if (axiom.isAnnotationAxiom()) {
                OWLAnnotationAssertionAxiom annotationAssertionAxiom = (OWLAnnotationAssertionAxiom) axiom;
                OWLNamedIndividual ind1 = dataFactory.getOWLNamedIndividual(IRI.create(annotationAssertionAxiom.getSubject().toString()));
                OWLNamedIndividual ind2 = dataFactory.getOWLNamedIndividual(IRI.create(annotationAssertionAxiom.getValue().toString()));
                OWLObjectProperty prop = dataFactory.getOWLObjectProperty(IRI.create(annotationAssertionAxiom.getProperty().toString().replace(">", "").replace("<", "")));
                OWLObjectPropertyAssertionAxiom owlObjectPropertyAssertionAxiom = dataFactory.getOWLObjectPropertyAssertionAxiom(
                        prop, ind1, ind2);
                if (ontology.getOntology().getAxioms().contains(owlObjectPropertyAssertionAxiom)) {
                    ontology.getManager().removeAxiom(ontology.getOntology(),owlObjectPropertyAssertionAxiom);
                }

            }else{
                if (ontology.getOntology().getAxioms().contains(axiom)) {
                    ontology.getManager().removeAxiom(ontology.getOntology(),axiom);
                }
            }
        }
    }

    /*This method execute the ABox and Tbox tests. The errors are printed in a report*/
    public   ArrayList<TestCaseResult> execute(ArrayList<TestCaseImplementation> testCaseImplementations, Ontology ontology, JSONArray got) throws  OWLOntologyCreationException, QueryParserException, QueryEngineException, OWLOntologyStorageException{

        String realResult;
        ArrayList<TestCaseResult> testsuiteResult = new ArrayList<>();
            for (TestCaseImplementation tc : testCaseImplementations) {
                ArrayList<String> undefinedterms = new ArrayList<>();
                TestCaseResult tr = new TestCaseResult();
                ArrayList<String> resultsforabsence = new ArrayList<>();
                tr.setRelatedTestImpl(tc.getUri());
                tr.setOntologyURI(ontology.getOntology().getOntologyID().getOntologyIRI());
                if (tc.getPreconditionQuery() != null) { //map the test terms with ontology terms and add the axioms to the ontology. If the addition results in a consistent ontology the preconditions are passed
                    checkPrecondition(tc, ontology, got);
                    tr.setUndefinedTerms(undefinedterms);
                    if (undefinedterms.isEmpty() && tc.getPreparation() != null) {
                        // test preparation
                        realResult = aboxTest(tc.getPreparation(), ontology, "preparation", got);
                        // analyse results
                        tr  = checkAssertion(realResult, ontology, got, tc);
                    } else {
                        resultsforabsence.add("undefined");
                        tr.setTestResult("undefined");
                    }
                } else {
                    resultsforabsence.add("undefined");
                    tr.setTestResult("undefined");
                }

                testsuiteResult.add(tr);
            }

        return  testsuiteResult;
    }

    public TestCaseResult checkAssertion(String realResult, Ontology ontology, JSONArray got, TestCaseImplementation tc) throws OWLOntologyStorageException, QueryParserException, QueryEngineException, OWLOntologyCreationException {
        ArrayList<String> resultsforabsence= new ArrayList<>();
        int absent = 0;
        TestCaseResult tr = new TestCaseResult();
        tr.setTestResult("passed");
        if (!realResult.toLowerCase().contains("consistent")) {
            resultsforabsence.add("inconsistent");
            tr.setTestResult("not passed");
            removePreparationAxioms(tc.getPreparation(), ontology, got);
        } else {
            //add assertions to the ontology, after mapping the test terms with ontology terms. Check if the real result is the same as the expected result
            for (Map.Entry<String, String> entry : tc.getAssertions().entrySet()) {
                realResult = aboxTest(entry.getKey(), ontology, "assertion", got);
                if (realResult.equalsIgnoreCase("consistent")) {
                    resultsforabsence.add("consistent");
                } else {
                    resultsforabsence.add("inconsistent");
                }
                if(tc.getType().equals("existential") && entry.getKey().equals("Assertion 2") && realResult.equals("inconsistent")) {
                    absent=1;
                }else if (!realResult.equalsIgnoreCase("consistent") && !realResult.equalsIgnoreCase(tc.getAxiomExpectedResultAxioms().get(entry.getKey()))) {
                    tr.setTestResult("not passed");
                }else if(realResult.equalsIgnoreCase("consistent") && !realResult.equalsIgnoreCase(tc.getAxiomExpectedResultAxioms().get(entry.getKey()))){
                    absent=1;
                }
            }
            removePreparationAxioms(tc.getPreparation(), ontology, got);
        }
        // check if the result could be absent relation
        int flag = 0;
        for(String result: resultsforabsence){
            if(!result.equals("consistent")){
                flag++;
            }
        }
        if(flag==0 && !resultsforabsence.isEmpty() || !tr.getTestResult().equals("not passed") && absent == 1 ){
            tr.setTestResult("absent");
        }
        return tr;
    }

    public ArrayList<String> checkPrecondition(TestCaseImplementation tc, Ontology ontology, JSONArray got ) throws OWLOntologyCreationException, QueryParserException, QueryEngineException {
        ArrayList<String> undefinedterms = new ArrayList<>();
        String realResult;
        for (String prec : tc.getPreconditionQuery()) {
            String precondWithURI = Utils.mappingValue(prec, ontology.getProv().toString(), got); // all  mappings received by the webapp
            realResult = tboxTest(precondWithURI, tc.getUri(), ontology.getManager(), ontology.getOntology());
            if (realResult.equals("false")) {
                undefinedterms.add(Utils.getPrecTerm(prec).replace(">", "").replace("<", ""));
            }
        }
        return undefinedterms;
    }

    /*Store the result of each test design*/
    public  static JSONObject printReportPerOntology(Ontology onto, TestCaseDesign requirement, ArrayList<TestCaseResult> results) throws JSONException {
        JSONObject objresults = new JSONObject();
        String finalResult = "";
        for (TestCaseResult result : results) {
            if (result.getTestResult().equals("not passed")){
                finalResult = "not passed";
                break;
            }else if(result.getTestResult().equals("undefined")){
                finalResult = "undefined";
                break;
            }else if(result.getTestResult().equals("absent")){
                finalResult = "absent";
                break;
            }
        }
        if(finalResult==""){
            finalResult="passed";
        }
        objresults.put("Requirement description", requirement.getDescription());
        objresults.put("Result", finalResult);
        objresults.put("Ontology", onto.getProv());
        objresults.put("Provenance", requirement.getProvenance());
        ArrayList<String> terms = new ArrayList<>();
        for(String purpose: requirement.getPurpose()) {
            for (String term: processTestExpressionToExtractTerms(purpose)) {
                if(!terms.contains(term))
                    terms.add(term);
            }
        }
        objresults.put("Terms involved", terms);
        return objresults;
    }


}
