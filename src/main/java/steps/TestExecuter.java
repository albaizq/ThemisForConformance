package steps;

import com.clarkparsia.pellet.owlapiv3.PelletReasoner;
import de.derivo.sparqldlapi.Query;
import de.derivo.sparqldlapi.QueryEngine;
import de.derivo.sparqldlapi.QueryResult;
import org.semanticweb.HermiT.Configuration;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.reasoner.*;
import org.semanticweb.owlapi.reasoner.structural.StructuralReasonerFactory;
import tests.TestCaseImplementation;
import tests.TestCaseResult;
import ontologies.Ontology;
import utils.Mapper;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static utils.Mapper.mapTermsInTestImplementation;

/**
 * Created by albafernandez on 19/06/2017.
 * Class to execute all the tests in an ontology
 */
public class TestExecuter {

    static final String PASSED="passed";
    static final String UNDEFINED="undefined";
    static final String UNSATISFIABLE="unsatisfiable";
    static final String NOTPASSED="not passed";
    static final String CONSISTENT="consistent";
    static final String INCONSISTENT="inconsistent";
    static final String ABSENT ="absent";

    /*Here we check the ontology consistency regarding at model-level*/
    public String tboxTest(String query, IRI key, OWLOntologyManager manager, OWLOntology ontology) {
        StructuralReasonerFactory factory = new StructuralReasonerFactory();
        OWLReasonerConfiguration config = new SimpleConfiguration();
        OWLReasoner reasoner = factory.createReasoner(ontology,config);
        Query queryOWLAPI = null;
        try {
            queryOWLAPI = Query.create(query);
            QueryEngine engine = QueryEngine.create(manager, reasoner);
            QueryResult result;
            // Execute the query and generate the results set
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
    public String aboxTest(Set<OWLAxiom> textAxioms, Ontology ontology, String type) {
        //add changes to the ontology
        ontology = addChanges(textAxioms, ontology);
        //executeTest the reasoner to check the ontolgy status
        String result = executeReasoner( textAxioms, ontology);
        //remove axioms if it is a preparation set
        if(!type.equals("preparation")) {
            ontology.getManager().removeAxioms(ontology.getOwlOntology(), textAxioms);
        }
        return result;
    }

    /*Method to add changes to the ontology */
    public Ontology addChanges(Set<OWLAxiom> axioms, Ontology ontology) {

        // We will create several things, so we save an instance of the data factory
        OWLDataFactory dataFactory = ontology.getManager().getOWLDataFactory();

        for (OWLAxiom axiom : axioms) {
            AddAxiom addAxiom = null;
            if(axiom.isAnnotationAxiom()){ //Sometimes the object properties are translated as annotated axioms incorrectly and they have to be changed.
                OWLAnnotationAssertionAxiom annotationAssertionAxiom = (OWLAnnotationAssertionAxiom)axiom;
                OWLNamedIndividual ind1 = dataFactory.getOWLNamedIndividual(IRI.create(annotationAssertionAxiom.getSubject().toString()));
                OWLNamedIndividual ind2 = dataFactory.getOWLNamedIndividual(IRI.create(annotationAssertionAxiom.getValue().toString()));

                if(ontology.getOwlOntology().containsObjectPropertyInSignature(IRI.create(annotationAssertionAxiom.getProperty().toString().replace(">","").replace("<","")),true)){
                    OWLObjectProperty prop = dataFactory.getOWLObjectProperty(IRI.create(annotationAssertionAxiom.getProperty().toString().replace(">","").replace("<","")));
                    OWLObjectPropertyAssertionAxiom owlObjectPropertyAssertionAxiom = dataFactory.getOWLObjectPropertyAssertionAxiom(
                            prop, ind1,ind2);
                    if(!ontology.getOwlOntology().getAxioms().contains(axiom)) {
                        addAxiom = new AddAxiom(ontology.getOwlOntology(), owlObjectPropertyAssertionAxiom);
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

                    if(!ontology.getOwlOntology().getAxioms().contains(axiom)) {
                        addAxiom = new AddAxiom(ontology.getOwlOntology(), owlDataPropertyAssertionAxiom);
                    }
                }
            }else {
                if(!ontology.getOwlOntology().getAxioms().contains(axiom)){
                    addAxiom = new AddAxiom(ontology.getOwlOntology(), axiom);
                }
            }
            if(addAxiom!=null)
                ontology.getManager().applyChange(addAxiom);
        }
        return ontology;
    }

    /*Method to add changes to the ontology */
    public String executeReasoner(Set<OWLAxiom> axioms, Ontology ontology)  {

        PelletReasoner reasoner = null;
        Configuration configuration = new Configuration();
        configuration.throwInconsistentOntologyException = false;
        configuration.ignoreUnsupportedDatatypes = true;
        configuration.getProgressMonitor();
        reasoner = com.clarkparsia.pellet.owlapiv3.PelletReasonerFactory.getInstance().createReasoner(ontology.getOwlOntology(), configuration);
        reasoner.precomputeInferences();
        Set<OWLClass> classesInTest =  new HashSet();
        for (OWLAxiom axiom : axioms) {
            classesInTest.addAll(axiom.getClassesInSignature());
        }
        String result = "";

        if(!reasoner.isConsistent()) {
            result = INCONSISTENT;
        }else if(reasoner.getUnsatisfiableClasses().getSize()>1) {
            int flag = 0;
            // check if the unsatisfiable classes are because of the test
            for(OWLClass classintest: classesInTest){
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
                if (ontology.getOwlOntology().getAxioms().contains(owlObjectPropertyAssertionAxiom)) {
                    ontology.getManager().removeAxiom(ontology.getOwlOntology(),owlObjectPropertyAssertionAxiom);
                }

            }else{
                if (ontology.getOwlOntology().getAxioms().contains(axiom)) {
                    ontology.getManager().removeAxiom(ontology.getOwlOntology(),axiom);
                }
            }
        }
    }

    /*This method execute the ABox and Tbox tests. The errors are printed in a report*/
    public ArrayList<TestCaseResult> execute(ArrayList<TestCaseImplementation> testCaseImplementations, Ontology ontology, HashMap<String, IRI> got) {
        String realResult;
        ArrayList<TestCaseResult> testsuiteResult = new ArrayList<>();
            for (TestCaseImplementation tc : testCaseImplementations) {
                ArrayList<String> undefinedTerms;
                TestCaseResult tr = new TestCaseResult();
                tr.setRelatedTestImpl(tc.getUri());
                tr.setOntologyURI(ontology.getOwlOntology().getOntologyID().getOntologyIRI());
                if (tc.getPreconditionQuery() != null) { //map the test terms with ontology terms and add the axioms to the ontology. If the addition results in a consistent ontology the preconditions are passed
                    undefinedTerms = checkPrecondition(tc, ontology, got);
                    tr.setUndefinedTerms(undefinedTerms);
                    if (undefinedTerms.isEmpty() && tc.getPreparationAxioms() != null) {
                        // test preparation
                        Set<OWLAxiom> prepWithURI = Mapper.mapTermsInTestImplementation(tc.getPreparationAxioms(), got);
                        realResult = aboxTest(prepWithURI, ontology, "preparation");
                        // analyse results
                        tr  = checkAssertion(realResult, ontology, got, tc);
                    } else if(undefinedTerms.isEmpty() && tc.getPreparationAxioms() == null){
                        tr.setTestResult(PASSED);
                    }else{
                        tr.setTestResult(UNDEFINED);
                    }
                } else {
                    tr.setTestResult(UNDEFINED);
                }

                testsuiteResult.add(tr);
            }

        return  testsuiteResult;
    }

    public ArrayList<String> checkPrecondition(TestCaseImplementation tc, Ontology ontology, HashMap<String, IRI> got ) {
        ArrayList<String> undefinedTerms = new ArrayList<>();
        String realResult;
        for (String precondition : tc.getPreconditionQuery()) {
            String preconditionWithURI = Mapper.mapTermsInTestImplementation(precondition, got);
            realResult = tboxTest(preconditionWithURI, tc.getUri(), ontology.getManager(), ontology.getOwlOntology());
            if (realResult.equals("false")) {
                undefinedTerms.add(getPreconditionTerm(precondition).replace(">", "").replace("<", ""));
            }
        }
        return undefinedTerms;
    }

    public String getPreconditionTerm(String query){
        Pattern p = Pattern.compile("\\<(.*?)\\>");
        Matcher m = p.matcher(query);
        while(m.find()){
            return  m.group();
        }
        return  m.group();
    }

    public TestCaseResult checkAssertion(String realResult, Ontology ontology, HashMap<String, IRI> got, TestCaseImplementation tc) {
        int absentCandidate = 0;
        TestCaseResult tr = new TestCaseResult();
        tr.setTestResult(PASSED);
        if (!realResult.toLowerCase().contains(CONSISTENT)) {
            tr.setTestResult(NOTPASSED);
            Set<OWLAxiom> prepWithURI = Mapper.mapTermsInTestImplementation(tc.getPreparationAxioms(), got);
            removePreparationAxioms(prepWithURI, ontology);
            return tr;
        } else {
            //add assertions to the ontology, after mapping the test terms with ontology terms. Check if the real results is the same as the expected results
            for (Map.Entry<String, OWLOntology> entry : tc.getAssertionsAxioms().entrySet()) {
                Set<OWLAxiom> assertionWithURI = Mapper.mapTermsInTestImplementation(entry.getValue(), got);
                realResult = aboxTest(assertionWithURI, ontology, "assertion");
                if (realResult.equalsIgnoreCase(CONSISTENT)) {
                    absentCandidate++;
                } else {
                    //special case
                    if (tc.getType().equals("existential") && entry.getKey().equals("Assertion 2") && realResult.equals(INCONSISTENT)) {
                        absentCandidate++;
                    }else {
                        tr.setTestResult(NOTPASSED);
                        return tr;
                    }
                }
                // other special cases
                if ((tc.getType().equals("individuals")) && realResult.equalsIgnoreCase(tc.getAxiomExpectedResultAxioms().get(entry.getKey()))) {
                    absentCandidate--;
                } else if ((tc.getType().equals("literal")) && realResult.equalsIgnoreCase(tc.getAxiomExpectedResultAxioms().get(entry.getKey()))) {
                    absentCandidate--;
                } else if (!realResult.equalsIgnoreCase(CONSISTENT) && !realResult.equalsIgnoreCase(tc.getAxiomExpectedResultAxioms().get(entry.getKey()))) {
                    tr.setTestResult(NOTPASSED);
                    return tr;
                } else if (realResult.equalsIgnoreCase(CONSISTENT) && !realResult.equalsIgnoreCase(tc.getAxiomExpectedResultAxioms().get(entry.getKey()))) {
                    absentCandidate++;
                }
            }
            removePreparationAxioms(tc.getPreparationAxioms().getAxioms(), ontology);
        }
        tr = checkIfAbsent(absentCandidate, tr); //check whether the problem is that there is an absent relation (no conflict)

        return tr;
    }

    // check if the result is an absence
    public TestCaseResult checkIfAbsent(int absentCandidate, TestCaseResult tr) {

        if (absentCandidate>0) {
            tr.setTestResult(ABSENT);
        }else{
            tr.setTestResult(PASSED);
        }

        return tr;
    }

}
