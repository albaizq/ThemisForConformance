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
import org.semanticweb.HermiT.Reasoner;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.reasoner.*;
import org.semanticweb.owlapi.reasoner.structural.StructuralReasonerFactory;
import tests.TestCaseDesign;
import tests.TestCaseImplementation;
import tests.TestCaseResult;
import utils.Ontology;
import utils.Utils;

import java.io.*;
import java.net.URISyntaxException;
import java.util.*;
import java.util.stream.Collectors;

import static testingsteps.Mapping.processTestExpressionToExtractTerms;


/**
 * Created by albafernandez on 19/06/2017.
 */
public class Execution {

    /*Here we check the ontology consistency regarding at model-level*/
    public  String tbox_test(String query, IRI key, OWLOntologyManager manager, OWLOntology ontology) throws QueryParserException, QueryEngineException, OWLOntologyCreationException {


        StructuralReasonerFactory factory = new StructuralReasonerFactory();

        OWLReasonerConfiguration config = new SimpleConfiguration();


        OWLReasoner reasoner = factory.createReasoner(ontology,config);
        reasoner.precomputeInferences();

        Query queryOWLAPI = null;
        try {
            queryOWLAPI = Query.create(query);
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Check SPARQL-DL syntax: " + e.getMessage() + " ID: " + key);
        }

        QueryEngine engine = QueryEngine.create(manager, reasoner);
        QueryResult result;

        // Execute the query and generate the result set
        try {
            result = engine.execute(queryOWLAPI);

        } catch (de.derivo.sparqldlapi.exceptions.QueryEngineException e) {
            return "false";
        }

        if (queryOWLAPI.isAsk()) {
            if (result.ask()) {
                return "true";
            } else {
                return "false";
            }
        } else
            return "false";


    }

    /*Here we check the ontology consistency regarding at instance-level*/
    public  String abox_test(String textAxioms, Ontology ontology, String type) throws OWLOntologyCreationException, QueryParserException, QueryEngineException, OWLOntologyStorageException {

        // We will create several things, so we save an instance of the data factory
        OWLDataFactory dataFactory = ontology.getManager().getOWLDataFactory();
        Configuration configuration = new Configuration();
        configuration.throwInconsistentOntologyException = false;
        configuration.ignoreUnsupportedDatatypes = true;


        Reasoner.ReasonerFactory factory = new Reasoner.ReasonerFactory();
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
                        //  System.out.println( owlObjectPropertyAssertionAxiom);
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
                    // System.out.println(axiom);
                    addAxiom = new AddAxiom(ontology.getOntology(), axiom);
                    axiommodify.add(axiom);

                }
            }
            if(addAxiom!=null)
                ontology.getManager().applyChange(addAxiom);
        }

        //File file = new File("corefin.ttl");
        //ontology.manager.saveOntology(ontology.getOntology(), IRI.create(file.toURI()));
        // System.out.println("*************************");
        //System.out.println(ontology.getOntology().getAxioms());
        //System.out.println("************************");

        // System.exit(1);
        ConsoleProgressMonitor progressMonitor = new ConsoleProgressMonitor();
        //configuration.reasonerProgressMonitor= progressMonitor;
        //OWLReasoner reasoner = factory.createReasoner(ontology.getOntology());
        //System.out.println(ontology.getOntology());
       // Reasoner reasoner=new Reasoner( ontology.getOntology());
        PelletReasoner reasoner = com.clarkparsia.pellet.owlapiv3.PelletReasonerFactory.getInstance().createReasoner( ontology.ontology );
       // reasoner.precomputeInferences();
        //      /* System.out.println(reasoner.getReasonerName());
       /* System.out.println(reasoner.isConsistent());
        System.out.println(reasoner.getBufferingMode());
        reasoner.precomputeInferences();*/

        String result;
        if(!reasoner.isConsistent())
            result = "inconsistent";
        else if(reasoner.getUnsatisfiableClasses().getSize()>1)
            result = "unsatisfiable";
        else
            result= "consistent";

        // if(result == "inconsistent" || result == "unsatisfiable") {
        // System.out.println(type);
        if(!type.equals("preparation")) {
            ontology.getManager().removeAxioms(ontology.getOntology(), axiommodify.stream().collect(Collectors.toSet()));
        }
        return result;
    }

    public void removePreparationAxioms(String textAxioms, Ontology ontology){

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
    public   ArrayList<TestCaseResult> execute(ArrayList<TestCaseImplementation> testCaseImplementations, TestCaseDesign testdesign, Ontology ontology, JSONArray got) throws IOException, OWLOntologyCreationException, QueryParserException, QueryEngineException, OWLOntologyStorageException, URISyntaxException, JSONException {

        String realResult = "false";
        ArrayList<TestCaseResult> testsuiteResult = new ArrayList<>();
            for (TestCaseImplementation tc : testCaseImplementations) {
                ArrayList<String> undefinedterms = new ArrayList<>();
                TestCaseResult tr = new TestCaseResult();
                ArrayList<String> resultsforabsence = new ArrayList<>();
                tr.setRelatedTestImpl(tc.getUri());
                tr.setOntologyURI(ontology.getOntology().getOntologyID().getOntologyIRI());
                int absent = 0;
                if (tc.getPreconditionQuery() != null) { //map the test terms with ontology terms and add the axioms to the ontology. If the addition results in a consistent ontology the preconditions are passed
                    for (String prec : tc.getPreconditionQuery()) {
                        String precondWithURI = Utils.mappingValue(prec, ontology.getProv().toString(), got); // all  mappings received by the webapp
                        realResult = tbox_test(precondWithURI, tc.getUri(), ontology.getManager(), ontology.getOntology());
                        //System.out.println(realResult);
                        //System.out.println(precondWithURI);
                        if (realResult.equals("false")) {
                            undefinedterms.add(Utils.getPrecTerm(prec).replace(">", "").replace("<", ""));
                        }
                    }
                    tr.setUndefinedTerms(undefinedterms);
                    if (undefinedterms.size() == 0) {
                        if (tc.getPreparation() != null) {
                            // test preparation
                            String prepWithURI = Utils.mappingValue(tc.getPreparation(), ontology.getProv().toString(), got);
                            //
                            //System.out.println(prepWithURI);
                            realResult = abox_test(prepWithURI, ontology, "preparation");
                            if (!realResult.toLowerCase().contains("consistent")) {
                                resultsforabsence.add("inconsistent");
                                tr.setTestResult("not passed");
                                //System.out.println("not passed");
                                removePreparationAxioms(prepWithURI, ontology);
                            } else {
                                //add assertions to the ontology, after mapping the test terms with ontology terms. Check if the real result is the same as the expected result
                                for (Map.Entry<String, String> entry : tc.getAssertions().entrySet()) {
                                    String assertionWithURI = Utils.mappingValue(entry.getKey(), ontology.getProv().toString(), got);
                                    //System.out.println(assertionWithURI);
                                    realResult = abox_test(assertionWithURI, ontology, "assertion");
                                    //System.out.println(realResult);
                                    //System.out.println(tc.getAxiomExpectedResult().get(entry.getKey()));
                                    if (realResult.toLowerCase().equals("consistent")) {
                                        resultsforabsence.add("consistent");
                                    } else {
                                        resultsforabsence.add("inconsistent");
                                    }
                                    if (!realResult.toLowerCase().equals("consistent") && !realResult.toLowerCase().equals(tc.getAxiomExpectedResult().get(entry.getKey()))) {
                                        tr.setTestResult("not passed");
                                    } else if (realResult.toLowerCase().equals("consistent") && !realResult.toLowerCase().equals(tc.getAxiomExpectedResult().get(entry.getKey()))) {
                                        absent = 1;
                                    }
                                }
                                removePreparationAxioms(prepWithURI, ontology);
                            }
                        }
                    } else {
                        resultsforabsence.add("undefined");
                        tr.setTestResult("undefined");
                        //System.out.println("undefined");
                    }
                } else {
                    resultsforabsence.add("undefined");
                    tr.setTestResult("undefined");
                    //System.out.println("undefined");
                }


                int flag = 0;
                for (String result : resultsforabsence) {
                    if (!result.equals("consistent")) {
                        flag++;
                        //System.out.println("consistent");
                    }
                }
                if (flag == 0 && resultsforabsence.size() > 0 || !tr.getTestResult().equals("not passed") && absent == 1) {
                    tr.setTestResult("absent");
                    //System.out.println("absent");
                }

                if (tr.getTestResult() == ""){
                    tr.setTestResult("passed");
                //    System.out.println("passed");
                }
                testsuiteResult.add(tr);
            }


        return  testsuiteResult;
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
        for(String purpose: requirement.getPurpose())
            terms.addAll(processTestExpressionToExtractTerms(purpose));
        objresults.put("Terms involved", terms);

        return objresults;
    }

    /*Store the result of each test design*/
    public  static JSONObject printTotalReport(ArrayList<Ontology> ontos, TestCaseDesign requirement, ArrayList<TestCaseResult> results) throws JSONException {
        JSONObject objresults = new JSONObject();
        ArrayList<Ontology> ontostoRemove = new ArrayList<>();
        ontostoRemove.addAll(ontos);
        ArrayList<Ontology> ontosToiterate = new ArrayList<>();
        ontosToiterate.addAll(ontos);
        ArrayList<String> notpassed = new ArrayList<>();
        ArrayList<String> undefined = new ArrayList<>();
        ArrayList<String> absent = new ArrayList<>();
        for (TestCaseResult result : results) {
            if (result.getTestResult().equals("not passed")){
                for(int i = 0;i<ontosToiterate.size();i++){
                    if(ontosToiterate.get(i).getProv().toString().equals(result.getOntologyURI().toString().replace("#",""))){
                        if(!notpassed.contains(ontosToiterate.get(i).prov.toString())) {
                            notpassed.add(ontosToiterate.get(i).prov.toString());
                            ontostoRemove.remove(ontosToiterate.get(i));
                        }
                        break;
                    }
                }
            }else if(result.getTestResult().equals("undefined")){
                for(int i = 0;i<ontosToiterate.size();i++){
                    if(ontosToiterate.get(i).getProv().toString().equals(result.getOntologyURI().toString().replace("#",""))){
                        if(!undefined.contains(ontosToiterate.get(i).prov.toString())) {
                            undefined.add(ontosToiterate.get(i).prov.toString());
                            ontostoRemove.remove(ontosToiterate.get(i));
                        }
                        break;
                    }
                }
            }else if(result.getTestResult().equals("absent")){
                for(int i = 0;i<ontosToiterate.size();i++){
                    if(ontosToiterate.get(i).getProv().toString().equals(result.getOntologyURI().toString().replace("#",""))){
                        if(!absent.contains(ontosToiterate.get(i).prov.toString())) {
                            absent.add(ontosToiterate.get(i).prov.toString());
                            ontostoRemove.remove(ontosToiterate.get(i));
                        }
                        break;
                    }
                }
            }
        }

        ArrayList<String> requirements = new ArrayList<>();
        for(Ontology p: ontostoRemove){
            requirements.add(p.getProv().toString());
        }

        objresults.put("Requirement description", requirement.getDescription());
        objresults.put("Requirement not passed ", notpassed);
        objresults.put("Requirement undefined", undefined);
        objresults.put("Requirement absent", absent);
        objresults.put("Requirement passed", requirements);
        objresults.put("Percentage of passed ontologies", (float)requirements.size()/(requirements.size()+undefined.size()+absent.size()+notpassed.size())*100);
        objresults.put("Percentage of passed/absent ontologies", (float)(requirements.size()+absent.size())/(requirements.size()+undefined.size()+absent.size()+notpassed.size())*100);

        return objresults;
    }
}
