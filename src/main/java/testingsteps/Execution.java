package testingsteps;

import com.clarkparsia.pellet.owlapiv3.PelletReasoner;
import de.derivo.sparqldlapi.Query;
import de.derivo.sparqldlapi.QueryEngine;
import de.derivo.sparqldlapi.QueryResult;
import de.derivo.sparqldlapi.exceptions.QueryEngineException;
import de.derivo.sparqldlapi.exceptions.QueryParserException;
import org.semanticweb.HermiT.Configuration;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.reasoner.*;
import org.semanticweb.owlapi.reasoner.structural.StructuralReasonerFactory;
import tests.TestCaseImplementation;
import tests.TestCaseResult;
import ontologies.Ontology;
import utils.Utils;
import java.util.*;

import static testingsteps.Mapping.mapImplementationTerms;

/**
 * Created by albafernandez on 19/06/2017.
 * Class to execute all the tests in an ontology
 */
public class Execution {

    static final String PASSED="passed";
    static final String UNDEFINED="undefined";
    static final String UNSATISFIABLE="unsatisfiable";
    static final String NOTPASSED="not passed";
    static final String CONSISTENT="consistent";
    static final String INCONSISTENT="inconsistent";

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
    public  String aboxTest(Set<OWLAxiom> textAxioms, Ontology ontology, String type) throws OWLOntologyStorageException, OWLOntologyCreationException {
        //add changes to the ontology
        ontology = addChanges(textAxioms, ontology);
        //executeTest the reasoner to check the ontolgy status
        String result = executeReasoner( textAxioms, ontology);
        //remove axioms if it is a preparation set
        if(!type.equals("preparation")) {
            ontology.getManager().removeAxioms(ontology.getOntology(), textAxioms);
        }
        return result;
    }

    /*Method to add changes to the ontology */
    public  Ontology addChanges(Set<OWLAxiom> axioms, Ontology ontology) throws OWLOntologyStorageException, OWLOntologyCreationException {

        // We will create several things, so we save an instance of the data factory
        OWLDataFactory dataFactory = ontology.getManager().getOWLDataFactory();

        for (OWLAxiom axiom : axioms) {
            AddAxiom addAxiom = null;
            if(axiom.isAnnotationAxiom()){ //Sometimes the object properties are translated as annotated axioms incorrectly and they have to be changed.
                OWLAnnotationAssertionAxiom annotationAssertionAxiom = (OWLAnnotationAssertionAxiom)axiom;
                OWLNamedIndividual ind1 = dataFactory.getOWLNamedIndividual(IRI.create(annotationAssertionAxiom.getSubject().toString()));
                OWLNamedIndividual ind2 = dataFactory.getOWLNamedIndividual(IRI.create(annotationAssertionAxiom.getValue().toString()));

                if(ontology.getOntology().containsObjectPropertyInSignature(IRI.create(annotationAssertionAxiom.getProperty().toString().replace(">","").replace("<","")),true)){
                    OWLObjectProperty prop = dataFactory.getOWLObjectProperty(IRI.create(annotationAssertionAxiom.getProperty().toString().replace(">","").replace("<","")));
                    OWLObjectPropertyAssertionAxiom owlObjectPropertyAssertionAxiom = dataFactory.getOWLObjectPropertyAssertionAxiom(
                            prop, ind1,ind2);
                    if(!ontology.getOntology().getAxioms().contains(axiom)) {
                        addAxiom = new AddAxiom(ontology.getOntology(), owlObjectPropertyAssertionAxiom);
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
                    }
                }
            }else {
                if(!ontology.getOntology().getAxioms().contains(axiom)){
                    addAxiom = new AddAxiom(ontology.getOntology(), axiom);
                }
            }
            if(addAxiom!=null)
                ontology.getManager().applyChange(addAxiom);
        }
        return ontology;
    }

    /*Method to add changes to the ontology */
    public  String executeReasoner(Set<OWLAxiom> axioms, Ontology ontology) throws OWLOntologyStorageException, OWLOntologyCreationException {

        PelletReasoner reasoner = null;
        Configuration configuration = new Configuration();
        configuration.throwInconsistentOntologyException = false;
        configuration.ignoreUnsupportedDatatypes = true;
        configuration.getProgressMonitor();
        reasoner = com.clarkparsia.pellet.owlapiv3.PelletReasonerFactory.getInstance().createReasoner(ontology.getOntology(), configuration);
        reasoner.precomputeInferences();
        Set<OWLClass> classesintest =  new HashSet();
        for (OWLAxiom axiom : axioms) {
            classesintest.addAll(axiom.getClassesInSignature());
        }
        String result = "";

        if(!reasoner.isConsistent()) {
            result = INCONSISTENT;
        }else if(reasoner.getUnsatisfiableClasses().getSize()>1) {
            int flag = 0;
            // check if the unsatisfiable classes are because of the test
            for(OWLClass classintest: classesintest){
                if(reasoner.getUnsatisfiableClasses().contains(classintest)){
                    result=UNSATISFIABLE;
                    flag++;
                }
            }
            if(flag==0)
                result = CONSISTENT;
        }else {
            result = CONSISTENT;
        }

        return result;
    }

    public void removePreparationAxioms(Set<OWLAxiom> textAxioms, Ontology ontology){

        OWLDataFactory dataFactory = ontology.getManager().getOWLDataFactory();
        Configuration configuration = new Configuration();
        configuration.throwInconsistentOntologyException = false;
        configuration.ignoreUnsupportedDatatypes = true;

        for (OWLAxiom axiom : textAxioms) {
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
    public   ArrayList<TestCaseResult> execute(ArrayList<TestCaseImplementation> testCaseImplementations, Ontology ontology, HashMap<String, IRI> got) throws  OWLOntologyCreationException, QueryParserException, QueryEngineException, OWLOntologyStorageException{
        String realResult;
        ArrayList<TestCaseResult> testsuiteResult = new ArrayList<>();
            for (TestCaseImplementation tc : testCaseImplementations) {
                ArrayList<String> undefinedterms;
                TestCaseResult tr = new TestCaseResult();
                ArrayList<String> resultsforabsence = new ArrayList<>();
                tr.setRelatedTestImpl(tc.getUri());
                tr.setOntologyURI(ontology.getOntology().getOntologyID().getOntologyIRI());
                if (tc.getPreconditionQuery() != null) { //map the test terms with ontology terms and add the axioms to the ontology. If the addition results in a consistent ontology the preconditions are passed
                    undefinedterms = checkPrecondition(tc, ontology, got);
                    tr.setUndefinedTerms(undefinedterms);
                    if (undefinedterms.isEmpty() && tc.getPreparationaxioms() != null) {
                        // test preparation
                        Set<OWLAxiom> prepWithURI = mapImplementationTerms(tc.getPreparationaxioms(), got);
                        realResult = aboxTest(prepWithURI, ontology, "preparation");
                        // analyse results
                        tr  = checkAssertion(realResult, ontology, got, tc);
                    } else if(undefinedterms.isEmpty() && tc.getPreparationaxioms() == null){
                        resultsforabsence.add(PASSED);
                        tr.setTestResult(PASSED);
                    }else{
                        resultsforabsence.add(UNDEFINED);
                        tr.setTestResult(UNDEFINED);
                    }
                } else {
                    resultsforabsence.add(UNDEFINED);
                    tr.setTestResult(UNDEFINED);
                }

                testsuiteResult.add(tr);
            }

        return  testsuiteResult;
    }

    public TestCaseResult checkAssertion(String realResult, Ontology ontology, HashMap<String, IRI> got, TestCaseImplementation tc) throws OWLOntologyStorageException, QueryParserException, QueryEngineException, OWLOntologyCreationException {
        ArrayList<String> resultsforabsence= new ArrayList<>();
        int absent = 0;
        TestCaseResult tr = new TestCaseResult();
        tr.setTestResult(PASSED);
        if (!realResult.toLowerCase().contains(CONSISTENT)) {
            resultsforabsence.add(INCONSISTENT);
            tr.setTestResult(NOTPASSED);
            Set<OWLAxiom> prepWithURI = mapImplementationTerms(tc.getPreparationaxioms(), got);
            removePreparationAxioms(prepWithURI, ontology);
        } else {
            //add assertions to the ontology, after mapping the test terms with ontology terms. Check if the real result is the same as the expected result
            for (Map.Entry<String, OWLOntology> entry : tc.getAssertionsAxioms().entrySet()) {
                Set<OWLAxiom> assertionWithURI = mapImplementationTerms(entry.getValue(), got);
                realResult = aboxTest(assertionWithURI, ontology, "assertion");
                if (realResult.equalsIgnoreCase(CONSISTENT)) {
                    resultsforabsence.add(CONSISTENT);
                } else {
                    resultsforabsence.add(INCONSISTENT);
                }
                if(tc.getType().equals("existential") && entry.getKey().equals("Assertion 2") && realResult.equals(INCONSISTENT)) { // excepcion en existential
                    absent=1;
                }else if(tc.getType().equals("existential DP") &&  realResult.equals(CONSISTENT)) { // excepcion with literals
                    tr.setTestResult(PASSED);
                }else if (!realResult.equalsIgnoreCase(CONSISTENT) && !realResult.equalsIgnoreCase(tc.getAxiomExpectedResultAxioms().get(entry.getKey()))) {
                    tr.setTestResult(NOTPASSED);
                }else if(realResult.equalsIgnoreCase(CONSISTENT) && !realResult.equalsIgnoreCase(tc.getAxiomExpectedResultAxioms().get(entry.getKey()))){
                    absent=1;
                }
            }
            removePreparationAxioms(tc.getPreparationaxioms().getAxioms(), ontology);
        }
        // check if the result could be absent relation
        int flag = 0;
        for(String result: resultsforabsence){
            if(!result.equals(CONSISTENT)){
                flag++;
            }
        }
        if(flag==0 && !resultsforabsence.isEmpty() && !tc.getType().equals("existential DP") || !tr.getTestResult().equals(NOTPASSED) && absent == 1 ){
            tr.setTestResult("absent");
        }
        return tr;
    }

    public ArrayList<String> checkPrecondition(TestCaseImplementation tc, Ontology ontology, HashMap<String, IRI> got ) throws OWLOntologyCreationException, QueryParserException, QueryEngineException {
        ArrayList<String> undefinedterms = new ArrayList<>();
        String realResult;
        for (String prec : tc.getPreconditionQuery()) {

            String precondWithURI = mapImplementationTerms(prec, got);
            //String precondWithURI = Utils.mappingValue(prec, ontology.getProv().toString(), got); // all  mappings received by the webapp
            realResult = tboxTest(precondWithURI, tc.getUri(), ontology.getManager(), ontology.getOntology());
            if (realResult.equals("false")) {
                undefinedterms.add(Utils.getPrecTerm(prec).replace(">", "").replace("<", ""));
            }
        }
        return undefinedterms;
    }




}
