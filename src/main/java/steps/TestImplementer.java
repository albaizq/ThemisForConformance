package steps;

import org.coode.owlapi.turtle.TurtleOntologyFormat;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.*;
import tests.TestCaseDesign;
import tests.TestCaseImplementation;

import java.io.File;
import java.util.*;

import static utils.Implementations.*;

/*
* Based on the test purposes associated to the TestCaseDesigns, generate the implementation of the tests that will be executed on the ontologies
* */
public class TestImplementer {


    public  void processReqsExpressionTemplates(String purpose, TestCaseImplementation testCase) throws OWLOntologyCreationException {

        String purposeCloned= purpose.toLowerCase().replace("  "," ");

        if(purposeCloned.matches("[^\\s]+ subclassof [^\\s]+ only [^\\s]+ or [^\\s]+")){
           unionTest(purpose.replace(","," "),"union", testCase);
        }else if(purposeCloned.matches("[^\\s]+ domain [^\\s]+")){
            domainTest(purpose.replace(","," "), testCase);
        }else if(purposeCloned.matches("[^\\s]+ range (xsd:string|xsd:float|xsd:integer|string|float|integer|owl:rational|rational|boolean|xsd:boolean|anyuri|xsd:anyuri|datetime|xsd:datetime|datetimestamp|xsd:datetimestamp)")){
            rangeTestDP(purpose.replace(","," "), testCase);
        }else if(purposeCloned.matches("[^\\s]+ range (rdfs:literal|literal)")){
            rangeTestDPLiteral(purpose.replace(","," "), testCase);
        }else if(purposeCloned.matches("[^\\s]+ range [^\\s]+")){
            rangeTest(purpose.replace(","," "), testCase);
        }else if(purposeCloned.matches("[^\\s]+ subclassof [^\\s]+ some [^\\s]+ and [^\\s]+")){
            intersectionTest(purpose.replace(","," "),"union", testCase);
        }else if(purposeCloned.matches("[^\\s]+ subclassof [^\\s]+ value [^\\s]+")){
            individualValue(purpose.replace(","," "),testCase);
        }else if(purposeCloned.matches("[^\\s]+ subclassof [^\\s]+ and [^\\s]+ subclassof [^\\s]+ that disjointwith [^\\s]+")){
            subclassDisjointTest(purpose, testCase);
        }else if(purposeCloned.matches("[^\\s]+ equivalentto [^\\s]+")){
            subClassTest(purpose, "equivalence",testCase);
        }else if(purposeCloned.matches("[^\\s]+ disjointwith [^\\s]+")){
            subClassTest(purpose, "disjoint",testCase);
        }else if(purposeCloned.matches("[^\\s]+ subclassof [^\\s]+ min (\\d+) [^\\s]+ and [^\\s]+ subclassof [^\\s]+ some [^\\s]+")){
            cardinalityOPTest(purpose,"min",testCase);
        }else if(purposeCloned.matches("[^\\s]+ subclassof [^\\s]+ (max|min|exactly) (\\d+) \\([^\\s]+ and [^\\s]+\\)")){
            intersectionCardTest(purpose,"max",testCase);
        }else if(purposeCloned.matches("[^\\s]+ subclassof [^\\s]+ that [^\\s]+ some [^\\s]+")){
            subClassOPTest(purpose,testCase);
        }else if(purposeCloned.matches("[^\\s]+ characteristic symmetricproperty")){
            symmetryTest(purpose, testCase);
        }else if(purposeCloned.matches("[^\\s]+ subclassof symmetricproperty\\([^\\s]+\\) (some|only) [^\\s]+") || purposeCloned.matches("[^\\s]+ subclassOf <coparticipatesWith> (some|only) [^\\s]+")){
            symmetryWithDomainRangeTest(purpose, testCase);
        }else if(purposeCloned.matches("[^\\s]+ subclassof (hasparticipant|isparticipantin|haslocation|islocationof|hasrole|isroleof) some [^\\s]+")){
            participantODPTestExistential(purpose, testCase);
        }else if(purposeCloned.matches("[^\\s]+ subclassof (hasparticipant|isparticipantin|haslocation|islocationof|hasrole|isroleof) only [^\\s]+")){
           participantODPTestUniversal(purpose, testCase);
        }else if(purposeCloned.matches("[^\\s]+ subclassof (hascoparticipant|iscoparticipantin|cooparticipates) some [^\\s]+")){
           coParticipantODPTestExistential(purpose, testCase);
        }else if(purposeCloned.matches("[^\\s]+ and [^\\s]+ subclassof (hascoparticipant|iscoparticipantin|cooparticipates) only [^\\s]+")){
            coParticipantODPTestUniversal(purpose, testCase);
        }else if(purposeCloned.matches("[^\\s]+ subclassof [^\\s]+ some (xsd:string|xsd:float|xsd:integer|string|float|integer|owl:rational|rational|boolean|xsd:boolean|anyuri|xsd:anyuri|datetime|xsd:datetime|datetimestamp|xsd:datetimestamp)")){
            existentialRangeDP(purpose.replace("\\","\\\""),testCase);
        }else if(purposeCloned.matches("[^\\s]+ subclassof [^\\s]+ only (xsd:string|xsd:float|xsd:integer|string|float|integer|owl:rational|rational|boolean|xsd:boolean|anyuri|xsd:anyuri|datetime|xsd:datetime|datetimestamp|xsd:datetimestamp)")){
            universalRangeDP(purpose.replace("\\","\\\""),testCase);
        }else if(purposeCloned.matches("[^\\s]+ subclassof (\\w*)(?!hasparticipant | ?!isparticipantin | ?!haslocation| ?!islocationof | ?!hasrole| ?!isroleof) some [^\\s]+")){
            existentialRange(purpose, testCase);
        }else if(purposeCloned.matches("[^\\s]+ subclassof (\\w*)(?!hasparticipant | ?!isparticipantin | ?!haslocation| ?!islocationof | ?!hasrole| ?!isroleof) only [^\\s]+")){
            universalRange(purpose, testCase);
        }else if(purposeCloned.matches("[^\\s]+ subclassof [^\\s]+ min (\\d+) (xsd:string|xsd:float|xsd:integer|string|float|integer|owl:rational|rational|boolean|xsd:boolean|anyuri|xsd:anyuri|datetime|xsd:datetime|datetimestamp|xsd:datetimestamp)")){
            cardinalityDP(purpose, "min",testCase);
        }else if(purposeCloned.matches("[^\\s]+ subclassof [^\\s]+ max (\\d+) (xsd:string|xsd:float|xsd:integer|string|float|integer|owl:rational|rational|boolean|xsd:boolean|anyuri|xsd:anyuri|datetime|xsd:datetime|datetimestamp|xsd:datetimestamp)")){
            cardinalityDP(purpose, "max",testCase);
        }else if(purposeCloned.matches("[^\\s]+ subclassof [^\\s]+ exactly (\\d+) (xsd:string|xsd:float|xsd:integer|string|float|integer|owl:rational|rational|boolean|xsd:boolean|anyuri|xsd:anyuri|datetime|xsd:datetime|datetimestamp|xsd:datetimestamp)")){
            cardinalityDP(purpose, "exactly",testCase);
        }else if(purposeCloned.matches("[^\\s]+ subclassof [^\\s]+ min (\\d+) [^\\s]+")){
            cardinality(purpose, "min",testCase);
        }else if(purposeCloned.matches("[^\\s]+ subclassof [^\\s]+ max (\\d+) [^\\s]+")){
           cardinality(purpose, "max",testCase);
        }else if(purposeCloned.matches("[^\\s]+ subclassof [^\\s]+ exactly (\\d+) [^\\s]+")){
            cardinality(purpose, "exactly",testCase);
        }else if(purposeCloned.matches("[^\\s]+ type class")) {
            classDefinitionTest(purpose, testCase);
        }else if(purposeCloned.matches("[^\\s]+ type property")) {
            propertyDefinitionTest(purpose, testCase);
        }else if(purposeCloned.matches("[^\\s]+ type [^\\s]+")){
            typeTest(purpose, testCase);
        }else if(purposeCloned.matches("[^\\s]+ subclassof (ispartof|partof) some [^\\s]+")){
            partWholeTestExistential(purpose,testCase);
        }else if(purposeCloned.matches("[^\\s]+ subclassof (ispartof|partof) only [^\\s]+")){
            partWholeTestUniversal(purpose,testCase);
        }else if(purposeCloned.matches("[^\\s]+ subclassof [^\\s]+ and [^\\s]+")){
            multipleSubClassTest(purpose,testCase);
        }else if(purposeCloned.matches("[^\\s]+ subclassof [^\\s]+")){
            subClassTest(purpose, "strict subclass",testCase);
        }else if(purposeCloned.matches("[^\\s]+ [^\\s]+ (xsd:string|xsd:float|xsd:integer|rdfs:literal|xsd:datetime|xsd:datetimestamp|string|float|integer|datetime|owl:rational|rational|boolean|xsd:boolean|anyuri|xsd:anyuri|literal|xsd:literal)")){
            domainRangeTestDP(purpose, testCase);
        } else if(purposeCloned.matches("[^\\s]+ [^\\s]+ [^\\s]+")){
            domainRangeTest(purpose, testCase);
        }
        else{
           System.out.print("NOT MATCH FOUND IN " +purpose+": ");
        }
    }

    //create the RDF of the implementation, which will be executed on the ontology
    public  ArrayList<TestCaseImplementation>  createTestImplementation(ArrayList<TestCaseDesign> tcs) throws OWLOntologyCreationException, OWLOntologyStorageException {
        OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
        String base = "http://themis.linkeddata.es/tests/implementation#";
        String verif = "http://w3id.org/def/vtc#";
        OWLOntology ont = manager.createOntology(IRI.create(base));
        OWLDataFactory dataFactory = manager.getOWLDataFactory();

        OWLClass verifTestImplClass = dataFactory.getOWLClass(IRI.create(verif + "TestCaseImplementation"));
        OWLClass verifTestPrepClass = dataFactory.getOWLClass(IRI.create(verif + "TestPreparation"));
        OWLClass verifTestAssertionClass = dataFactory.getOWLClass(IRI.create(verif + "TestAssertion"));

        ArrayList<TestCaseImplementation> tis = new ArrayList<>();
        for(TestCaseDesign tc: tcs) {
            int value = 1;
            for (String purpose : tc.getPurpose()) {
                TestCaseImplementation ti = new TestCaseImplementation();
                ti.setRelatedTestDesign(tc.getUri());
                ti.setUri(IRI.create(base + "testImplementation" + value));
                value++;
                /*Create individual of type testImplementation*/
                OWLIndividual subject = dataFactory.getOWLNamedIndividual(IRI.create(base + "testImplementation" + value));
                OWLClassAssertionAxiom classAssertion = dataFactory.getOWLClassAssertionAxiom(verifTestImplClass, subject);
                manager.addAxiom(ont, classAssertion);
                /*Add related test design*/
                OWLObjectProperty relatedToDesign = dataFactory.getOWLObjectProperty(IRI.create(verif + "relatedToDesign"));
                OWLIndividual design = dataFactory.getOWLNamedIndividual(IRI.create(base));
                OWLAxiom relatedToDesignAssertion = dataFactory.getOWLObjectPropertyAssertionAxiom(relatedToDesign, subject, design);
                manager.addAxiom(ont, relatedToDesignAssertion);
                /*Add precondition*/
                processReqsExpressionTemplates(purpose, ti);
                ArrayList<String> precondarray = new ArrayList<>();
                String preconditionquery = "";

                for (String query : ti.getPrecondition()) {
                    preconditionquery += "ASK{"+ query +"}";
                    precondarray.add(preconditionquery);
                }

                ti.setPreconditionQuery(precondarray);
                if (ti.getPreparation() != null) {
                    OWLDataProperty preconditionProp = dataFactory.getOWLDataProperty(IRI.create(verif + "precondition"));
                    for (String prec : ti.getPrecondition()) {
                        OWLAxiom axiomprecondition = dataFactory.getOWLDataPropertyAssertionAxiom(preconditionProp, subject, prec);
                        manager.addAxiom(ont, axiomprecondition);
                    }
                    /*Add test preparation*/
                    OWLObjectProperty preparationProperty = dataFactory.getOWLObjectProperty(IRI.create(verif + "hasPreparation"));
                    OWLIndividual preparation = dataFactory.getOWLNamedIndividual(IRI.create(base + "preparation1"));
                    OWLAxiom preparationAssertion = dataFactory.getOWLObjectPropertyAssertionAxiom(preparationProperty, subject, preparation);
                    manager.addAxiom(ont, preparationAssertion);
                    /*Add assertions*/
                    if (ti.getAssertions().size() > 1) {
                        OWLObjectProperty assertionProperty = dataFactory.getOWLObjectProperty(IRI.create(verif + "hasAssertion"));
                        for (int j = 1; j <= ti.getAssertions().size(); j++) {
                            OWLIndividual assertion = dataFactory.getOWLNamedIndividual(IRI.create(base + "assertion" + j));
                            OWLAxiom assertionAssertion = dataFactory.getOWLObjectPropertyAssertionAxiom(assertionProperty, subject, assertion);
                            manager.addAxiom(ont, assertionAssertion);
                        }
                    }
                    /*Create preparation individual*/
                    OWLIndividual preparation1 = dataFactory.getOWLNamedIndividual(IRI.create(base + "preparation1"));
                    OWLClassAssertionAxiom classAssertion2 = dataFactory.getOWLClassAssertionAxiom(verifTestPrepClass, preparation1);
                    manager.addAxiom(ont, classAssertion2);
                    /*Add description*/
                    OWLDataProperty descrProp = dataFactory.getOWLDataProperty(IRI.create("http://purl.org/dc/terms/description"));
                    OWLAxiom descrAssertion = dataFactory.getOWLDataPropertyAssertionAxiom(descrProp, preparation1, "");
                    manager.addAxiom(ont, descrAssertion);
                    /*add preparation*/
                    OWLDataProperty axiomsProp = dataFactory.getOWLDataProperty(IRI.create(verif + "testAxioms"));
                    OWLAxiom axiomsAssertion = dataFactory.getOWLDataPropertyAssertionAxiom(axiomsProp, preparation1, ti.getPreparation());
                    manager.addAxiom(ont, axiomsAssertion);
                    /*Create assertion individual*/
                    String result = "";
                    int i = 1;
                    for (Map.Entry<String, String> entry : ti.getAssertions().entrySet()) {
                        OWLIndividual assertion1 = dataFactory.getOWLNamedIndividual(IRI.create(base + "assertion" + i));
                        OWLClassAssertionAxiom classAssertion3 = dataFactory.getOWLClassAssertionAxiom(verifTestAssertionClass, assertion1);
                        manager.addAxiom(ont, classAssertion3);
                        /*add description*/
                        OWLAxiom descrAssertion2 = dataFactory.getOWLDataPropertyAssertionAxiom(descrProp, assertion1, "");
                        manager.addAxiom(ont, descrAssertion2);
                        /*add test axioms*/
                        OWLAxiom axiomsAssertion2 = dataFactory.getOWLDataPropertyAssertionAxiom(axiomsProp, assertion1, entry.getKey());
                        manager.addAxiom(ont, axiomsAssertion2);
                        OWLIndividual assresult = null;
                        if (ti.getAxiomExpectedResult().get(entry.getKey()).equals("unsatisfiable"))
                            assresult = dataFactory.getOWLNamedIndividual(IRI.create(verif + "Unsatisfiable"));
                        else if (ti.getAxiomExpectedResult().get(entry.getKey()).equals("inconsistent"))
                            assresult = dataFactory.getOWLNamedIndividual(IRI.create(verif + "Inconsistent"));
                        else
                            assresult = dataFactory.getOWLNamedIndividual(IRI.create(verif + "Consistent"));
                        OWLObjectProperty assertionResultProperty = dataFactory.getOWLObjectProperty(IRI.create(verif + "hasAssertionResult"));
                        OWLAxiom assertionResultAssertion = dataFactory.getOWLObjectPropertyAssertionAxiom(assertionResultProperty, assertion1, assresult);
                        manager.addAxiom(ont, assertionResultAssertion);
                        i++;
                    }
                }
                tis.add(ti);
            }

        }

        File file = new File("test-implementation.ttl");
        TurtleOntologyFormat turtleFormat = new TurtleOntologyFormat();
        turtleFormat.setDefaultPrefix(base);
        manager.saveOntology(ont, turtleFormat, IRI.create(file));

        return tis;

    }


}
