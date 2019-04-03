package testingsteps;

import org.coode.owlapi.turtle.TurtleOntologyFormat;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.*;
import tests.TestCaseDesign;
import tests.TestCaseImplementation;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class Implementation {


    public static ArrayList<TestCaseDesign> processTestCaseDesign(String filename) throws IOException, OWLOntologyCreationException {

        OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
        OWLOntology ontology =  manager.loadOntologyFromOntologyDocument(new File(filename));

        ArrayList<TestCaseDesign> testsuite = new ArrayList<>();
        for (OWLIndividual cls: ontology.getIndividualsInSignature()) {
            IRI uri = null;
            String purpose = "";
            String requirement = "";
            String source = "";
            String description = "";
            ArrayList<String> purposes = new ArrayList<>();
            TestCaseDesign tc = new TestCaseDesign();
            tc.setUri(IRI.create(cls.toString().replace("<","").replace(">","")));
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
                }else if(dp.getProperty().toString().contains("http://purl.org/dc/terms/references")){
                    tc.setProvenance(dp.getObject().toString().replace("\"","").replace("  "," ").replace("^^xsd:string","").trim());
                }
            }
            if(tc.getPurpose().size()>0)
                testsuite.add(tc);
        }
        return testsuite;
    }

    public static void processReqsExpressionTemplates(String purpose, TestCaseImplementation testCase){

        purpose.replaceAll("  "," ");
        String purposecloned= purpose.toLowerCase();
        if(purposecloned.matches("[^\\s]+ subclassof [^\\s]+ only [^\\s]+ or [^\\s]+")){
            unionTest(purpose.replace(","," "),"union", testCase);
        }else if(purposecloned.matches("[^\\s]+ subclassof [^\\s]+ value [^\\s]+")){
            individualValue(purpose.replace(","," "),testCase);
        }else if(purposecloned.matches("[^\\s]+ subclassof [^\\s]+ some [^\\s]+ and [^\\s]+")){
            intersectionTest(purpose.replace(","," "),"union", testCase);
        }else if(purposecloned.matches("[^\\s]+ subclassof [^\\s]+ and [^\\s]+ subclassof [^\\s]+ that disjointwith [^\\s]+")){
            subclassDisjointTest(purpose, testCase);
        }else if(purposecloned.matches("[^\\s]+ equivalentto [^\\s]+")){
            //  System.out.println("equivalent");
            subClassTest(purpose, "equivalence",testCase);
        }else if(purposecloned.matches("[^\\s]+ disjointwith [^\\s]+")){
            //System.out.println("disjoint");
            subClassTest(purpose, "disjoint",testCase);
        }else if(purposecloned.matches("[^\\s]+ subclassof [^\\s]+ min (\\d+) [^\\s]+ and [^\\s]+ subclassof [^\\s]+ some [^\\s]+")){
            cardinalityOPTest(purpose,"min",testCase);
        }else if(purposecloned.matches("[^\\s]+ subclassof [^\\s]+ (max|min|exactly) (\\d+) \\([^\\s]+ and [^\\s]+\\)")){
            //System.out.println("intersection max");
            intersectionCardTest(purpose,"max",testCase);
        }else if(purposecloned.matches("[^\\s]+ subclassof [^\\s]+ that [^\\s]+ some [^\\s]+")){
            subClassOPTest(purpose,testCase);
        }else if(purposecloned.matches("[^\\s]+ subclassof symmetricproperty\\([^\\s]+\\) (some|only) [^\\s]+") || purposecloned.matches("[^\\s]+ subclassOf <coparticipatesWith> (some|only) [^\\s]+")){
            symmetryTest(purpose, testCase);
        }else if(purposecloned.matches("[^\\s]+ subclassof (hasparticipant|isparticipantin|haslocation|islocationof|hasrole|isroleof) (some|only) [^\\s]+")){
            participantODPTest(purpose, testCase);
        }else if(purposecloned.matches("[^\\s]+ subclassof [^\\s]+ some (xsd:string|xsd:float|xsd:integer|string|float|integer|owl:rational|rational|xsd:boolean|boolean)")){
            existentialRangeDP(purpose.replace("\\","\\\""),testCase);
        }else if(purposecloned.matches("[^\\s]+ subclassof [^\\s]+ only (xsd:string|xsd:float|xsd:integer|string|float|integer|owl:rational|rational|xsd:boolean|boolean)")){
            universalRangeDP(purpose.replace("\\","\\\""),testCase);
        }else if(purposecloned.matches("[^\\s]+ subclassof (\\w*)(?!hasparticipant | ?!isparticipantin | ?!haslocation| ?!islocationof | ?!hasrole| ?!isroleof) some [^\\s]+")){
            existentialRange(purpose, testCase);
        }else if(purposecloned.matches("[^\\s]+ subclassof (\\w*)(?!hasparticipant | ?!isparticipantin | ?!haslocation| ?!islocationof | ?!hasrole| ?!isroleof) only [^\\s]+")){
            universalRange(purpose, testCase);
        }else if(purposecloned.matches("[^\\s]+ subclassof [^\\s]+ min 0 (xsd:string|xsd:float|xsd:integer|string|float|integer|owl:rational|rational|xsd:boolean|boolean)")){
            System.out.println("not match found");
        }else if(purposecloned.matches("[^\\s]+ subclassof [^\\s]+ max 0 (xsd:string|xsd:float|xsd:integer|string|float|integer|owl:rational|rational|xsd:boolean|boolean)")){
            System.out.println("not match found");
        }else if(purposecloned.matches("[^\\s]+ subclassof [^\\s]+ exactly 0 (xsd:string|xsd:float|xsd:integer|string|float|integer|owl:rational|rational|xsd:boolean|boolean)")){
            System.out.println("not match found");
        }else if(purposecloned.matches("[^\\s]+ subclassof [^\\s]+ min 0 [^\\s]+")){
            System.out.println("not match found");
        }else if(purposecloned.matches("[^\\s]+ subclassof [^\\s]+ max 0 [^\\s]+")){
            System.out.println("not match found");
        }else if(purposecloned.matches("[^\\s]+ subclassof [^\\s]+ exactly 0 [^\\s]+")){
            System.out.println("not match found");
        }else if(purposecloned.matches("[^\\s]+ subclassof [^\\s]+ min (\\d+) (xsd:string|xsd:float|xsd:integer|string|float|integer|owl:rational|rational|xsd:boolean|boolean)")){
            cardinalityDP(purpose, "min",testCase);
        }else if(purposecloned.matches("[^\\s]+ subclassof [^\\s]+ max (\\d+) (xsd:string|xsd:float|xsd:integer|string|float|integer|owl:rational|rational|xsd:boolean|boolean)")){
            cardinalityDP(purpose, "max",testCase);
        }else if(purposecloned.matches("[^\\s]+ subclassof [^\\s]+ exactly (\\d+) (xsd:string|xsd:float|xsd:integer|string|float|integer|owl:rational|rational|xsd:boolean|boolean)")){
            cardinalityDP(purpose, "exactly",testCase);
        }else if(purposecloned.matches("[^\\s]+ subclassof [^\\s]+ min (\\d+) [^\\s]+")){
            cardinality(purpose, "min",testCase);
        }else if(purposecloned.matches("[^\\s]+ subclassof [^\\s]+ max (\\d+) [^\\s]+")){
            cardinality(purpose, "max",testCase);
        }else if(purposecloned.matches("[^\\s]+ subclassof [^\\s]+ exactly (\\d+) [^\\s]+")){
            cardinality(purpose, "exactly",testCase);
        }else if(purposecloned.matches("[^\\s]+ type class")) {
            definitionTest(purpose, testCase);
        }else if(purposecloned.matches("[^\\s]+ type [^\\s]+")){
            typeTest(purpose, testCase);
        }else if(purposecloned.matches("[^\\s]+ subclassof (ispartof|partof) (some|only) [^\\s]+")){
            System.out.println("part");
            partWholeTest(purpose,testCase);
        }else if(purposecloned.matches("[^\\s]+ subclassof [^\\s]+ and [^\\s]+")){
            multipleSubClassTest(purpose,testCase);
        }else if(purposecloned.matches("[^\\s]+ subclassof [^\\s]+")){
            subClassTest(purpose, "strict subclass",testCase);
        }
        else{
            System.out.println("not match found  in implementation"+ purpose);
        }
    }

    public static ArrayList<TestCaseImplementation>  createTestImplementation(ArrayList<TestCaseDesign> tcs) throws OWLOntologyCreationException, OWLOntologyStorageException {
        OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
        String base = "http://www.semanticweb.org/untitled-ontology-53#";
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
                /*Create individual of type testimplementation*/
                OWLIndividual subject = dataFactory.getOWLNamedIndividual(IRI.create(base + "testImplementation" + value));
                OWLClassAssertionAxiom classAssertion = dataFactory.getOWLClassAssertionAxiom(verifTestImplClass, subject);
                manager.addAxiom(ont, classAssertion);
                /*Add related test design*/
                OWLObjectProperty relatedToDesign = dataFactory.getOWLObjectProperty(IRI.create(verif + "relatedToDesign"));
                OWLIndividual design = dataFactory.getOWLNamedIndividual(IRI.create(base));
                OWLAxiom relatedToDesignAssertion = dataFactory.getOWLObjectPropertyAssertionAxiom(relatedToDesign, subject, design);
                manager.addAxiom(ont, relatedToDesignAssertion);
                /*Add precondition*/
                ArrayList<Integer> array = new ArrayList();
                processReqsExpressionTemplates(purpose, ti);
                ArrayList<String> precondarray = new ArrayList<>();
                String preconditionquery = "";

                for (String query : ti.getPrecondition()) {
                    preconditionquery = "";
                    preconditionquery += "ASK{";
                    preconditionquery += query;
                    preconditionquery += "}";
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
        //turtleFormat.copyPrefixesFrom(pm);
        turtleFormat.setDefaultPrefix(base);
        manager.saveOntology(ont, turtleFormat, IRI.create(file));

        return tis;

    }

    /*****The following functions implement each type of test implementation. They include the precondition, the
     * preparation and the assertion for each type of test*****/

    /*cardinality for DP*/
    public static ArrayList<String> cardinalityDP(String purpose, String type, TestCaseImplementation testCase){
        Pattern p1 = Pattern.compile("(.*) subclassof (.*) (min|max|exactly) (\\d+) (xsd:string|xsd:float|xsd:integer|string|float|integer|owl:rational|rational|xsd:boolean|boolean)", Pattern.CASE_INSENSITIVE);
        Matcher m = p1.matcher(purpose);

        /*Generation of classes*/
        m.find();
        String classA = m.group(1).toString();

        String classA1 = "<"+classA+"1>";
        classA="<"+classA+">";
        String R = "<"+m.group(2).toString()+">";

        String datatype = "";
        datatype = m.group(5).toString();
        datatype="<"+"http://www.w3.org/2001/XMLSchema#"+datatype+">";

        String noClassB =  "<No"+datatype.split("(#|\\/)")[datatype.split("(#|\\/)").length-1]+">";

        Integer num = Integer.parseInt(m.group(4).toString());
        /*Preconditions*/
        ArrayList<String> precond = new ArrayList<>();
        precond.add("Class("+classA+")");
        precond.add("Property("+R+")");
        testCase.getPrecondition().addAll(precond);

        /*Axioms to be added*/

        /*A1 rdf:type owl:Class
         * A1 owl:subClassOf A
         * */
        String preparation =  "\n                       \t"+classA+" rdf:type owl:Class.\n "+
                "                       \t"+classA1+" rdfs:subClassOf "+classA+".\n ";

        testCase.setPreparation(preparation);

        /*A1 R max [num-1] B
         * */

        String assertion1 =   "\n                       \t"+classA1+"    rdfs:subClassOf "+classA+" ,\n" +
                "                                                                     "+"[ rdf:type owl:Restriction ;\n" +
                "                                                                     "+ "  owl:onProperty "+R+" ;\n" +
                "                                                                     "+ "  owl:maxQualifiedCardinality \""+(num-1)+"\"^^xsd:nonNegativeInteger ;\n" +
                "                                                                     "+ "  owl:onDataRange "+datatype+"\n" +
                "                                                                     "+ "] .\n"+
                "                                                                     "+ R+ "a owl:DatatypeProperty.\n";
        String expectedoutputassertion1 = "";
        if (type == "min" || type == "exactly")
            expectedoutputassertion1 = "unsatisfiable";
        else
            expectedoutputassertion1 = "consistent";


        /*A1 R min [num+1] B
         * */
        String assertion2 =  "\n                       \t"+classA1+" rdfs:subClassOf "+classA+" ,\n" +
                "                                                                     "+ "[ rdf:type owl:Restriction ;\n" +
                "                                                                     "+ "  owl:onProperty "+R+" ;\n" +
                "                                                                     "+ "  owl:minQualifiedCardinality \""+(num+1)+"\"^^xsd:nonNegativeInteger ;\n" +
                "                                                                     "+ "  owl:onDataRange "+datatype+"\n" +
                "                                                                     "+ "] .\n"+
                "                                                                     "+ R+ "a owl:DatatypeProperty.\n";
        String expectedoutputassertion2 = "";
        if (type == "max" || type == "exactly")
            expectedoutputassertion2 = "unsatisfiable";
        else
            expectedoutputassertion2 = "consistent";

        /*A1 R min [num+1] B
         * */
        String assertion3 ="";
        if(type == "max") {
            assertion3 = "\n                       \t" + classA1 + " rdfs:subClassOf " + classA + " ,\n" +
                    "                                                                     " + "[ rdf:type owl:Restriction ;\n" +
                    "                                                                     " + "  owl:onProperty " + R + " ;\n" +
                    "                                                                     " + "  owl:minQualifiedCardinality \"" + (num) + "\"^^xsd:nonNegativeInteger ;\n" +
                    "                                                                     "+ "   owl:onDataRange "+datatype+"\n" +
                    "                                                                     "+ "] .\n"+
                    "                                                                     "+ R+ "a owl:DatatypeProperty.\n";
        }else{
            assertion3 = "\n                       \t" + classA1 + " rdfs:subClassOf " + classA + " ,\n" +
                    "                                                                     " + "[ rdf:type owl:Restriction ;\n" +
                    "                                                                     " + "  owl:onProperty " + R + " ;\n" +
                    "                                                                     " + "  owl:maxQualifiedCardinality \"" + (num) + "\"^^xsd:nonNegativeInteger ;\n" +
                    "                                                                     "+ "   owl:onDataRange "+datatype+"\n" +
                    "                                                                     "+ "] .\n"+
                    "                                                                     "+ R+ "a owl:DatatypeProperty.\n";
        }
        String expectedoutputassertion3 = "consistent";



        LinkedHashMap hashinput = new LinkedHashMap();
        hashinput.put(assertion1,"Assertion 1 ");
        hashinput.put(assertion2,"Assertion 2");
        hashinput.put(assertion3,"Assertion 3");


        LinkedHashMap hashoutput = new LinkedHashMap();
        hashoutput.put(assertion1,expectedoutputassertion1);
        hashoutput.put(assertion2,expectedoutputassertion2);
        hashoutput.put(assertion3,expectedoutputassertion3);

        hashoutput.putAll(testCase.getAxiomExpectedResult());
        testCase.setAxiomExpectedResult(hashoutput);

        hashinput.putAll(testCase.getAssertions());
        testCase.setAssertions(hashinput);
        testCase.setType("cardinality"); // add the type of the requirement for the analysis of results
        return precond;

    }

    /*for the generation of an individual of a given class*/
    public static ArrayList<String> typeTest(String purpose, TestCaseImplementation testCase){
        // Pattern p = Pattern.compile("\\<(.*?)\\>");
        Pattern p = Pattern.compile("(.*) type (.*)",Pattern.CASE_INSENSITIVE);
        Matcher m = p.matcher(purpose);
        /*Generation of classes*/
        m.find();
        String indA = m.group(1).toString();
        String indAnoSymb = indA;
        String noIndA =  "<No"+indAnoSymb.split("(#|\\/)")[indA.split("(#)").length-1]+">";
        indA = "<"+indA+">";
        String classB = m.group(2).toString();
        String noClassB =  "<No"+classB.split("(#|\\/)")[classB.split("(#|\\/)").length-1]+">";
        classB = "<"+classB+">";
        /*Preconditions*/
        ArrayList<String> precond = new ArrayList<>();
        precond.add("Individual("+indA+")");
        testCase.getPrecondition().addAll(precond);

        /*Axioms to be added*/

        String prep1 = "                       "+noClassB+" rdf:type owl:Class .\n" +
                "                       "+noClassB+" owl:complementOf " + classB+".\n"+
                "                       ";

        testCase.setPreparation(prep1);

        String axiomgroup2 = "                       "+indA + " rdf:type owl:NamedIndividual.\n" +
                "                       "+indA + " rdf:type " +noClassB+".\n";
        String expectedoutputgroup2 ="";
        expectedoutputgroup2 = "inconsistent";

        LinkedHashMap hashinput = new LinkedHashMap();
        hashinput.put(axiomgroup2,"Assertion 1");


        LinkedHashMap hashoutput = new LinkedHashMap();
        hashoutput.put(axiomgroup2,expectedoutputgroup2);


        hashoutput.putAll(testCase.getAxiomExpectedResult());
        testCase.setAxiomExpectedResult(hashoutput);

        hashinput.putAll(testCase.getAssertions());
        testCase.setAssertions(hashinput);
        testCase.setType("type"); // add the type of the requirement for the analysis of results
        return null;
    }

    /*for the generation of symmetry*/
    public static ArrayList<String> symmetryTest(String purpose, TestCaseImplementation testCase){
        Pattern p = Pattern.compile("(.*) subclassof symmetricproperty\\((.*)\\) (some|only) (.*)",Pattern.CASE_INSENSITIVE );
        Matcher m = p.matcher(purpose);

        //System.out.println(m.matches());
        String classA;
        String classB;
        String R;
        String classA1;

        /*Generation of classes*/
        if(!m.find()){
            p = Pattern.compile("(.*) subclassOf coparticipateswith (some|only) (.*)",Pattern.CASE_INSENSITIVE);
            m = p.matcher(purpose);
            m.find();
            classA = m.group(1).toString().replace(" ","");
            classA1 = "<"+classA+"1>";
            classA="<"+classA+">";
            R = "<coParticipatesWith>";
            classB = m.group(2).toString().replace(" ","");
            classB = "<"+classB+">";
        }else{
            classA = m.group(1).toString().replace(" ","");
            classA1 = "<"+classA+"1>";
            classA="<"+classA+">";
            R = m.group(2).toString().replace(" ","");
            R = "<"+R+">";
            classB = m.group(4).toString().replace(" ","");
            classB = "<"+classB+">";
        }


        /*Preconditions*/
        ArrayList<String> precond = new ArrayList<>();
        precond.add("Class("+classA+")\n");
        precond.add("Property("+R+")\n");
        precond.add("Class("+classB+")\n");
        testCase.getPrecondition().addAll(precond);

        /*Axioms to be added*/

        String ind1= "<individual001>";
        String ind2 = "<individua002>";
        String ind3 = "<individua003>";

        String prep1 = "\n                       "+ind2+" rdf:type owl:NamedIndividual, "+classB+".\n"+

                "                       "+classA1+" rdf:type owl:Class ;\n" +
                "                       "+"    rdfs:subClassOf "+classA+",\n" +
                "                       "+"                    [ rdf:type owl:Restriction ;\n" +
                "                       "+"                      owl:onProperty "+R+" ;\n" +
                "                       "+"                      owl:allValuesFrom [ rdf:type owl:Class ;\n" +
                "                       "+"                                          owl:oneOf ( "+ind2+"\n" +
                "                       "+"                                                    )\n" +
                "                       "+"                                        ]\n" +
                "                       "+"                    ] .\n"+
                "                       "+ ind2+" owl:differentFrom "+ ind3+".\n"+
                ind1+" rdf:type owl:NamedIndividual, "+classA1+".\n"+
                ind3+" rdf:type owl:NamedIndividual, "+classB+".\n"+

                ind2+" owl:differentFrom "+ ind3+".\n";

        testCase.setPreparation(prep1);

        String assertion1 = "                       "+ ind1+" "+ R + " "+ind2+".\n";
        String expectedoutputassertion1 = "consistent";

        String assertion2 = "                       "+ ind3+" "+R+" "+ind1+".\n";
        String expectedoutputassertion2 = "inconsistent";

        LinkedHashMap hashinput = new LinkedHashMap();
        hashinput.put(assertion1,"Assertion 1");
        hashinput.put(assertion2,"Assertion 2");

        LinkedHashMap hashoutput = new LinkedHashMap();
        hashoutput.put(assertion1,expectedoutputassertion1);
        hashoutput.put(assertion2,expectedoutputassertion2);

        hashoutput.putAll(testCase.getAxiomExpectedResult());
        testCase.setAxiomExpectedResult(hashoutput);

        hashinput.putAll(testCase.getAssertions());
        testCase.setAssertions(hashinput);
        testCase.setType("symmetry"); // add the type of the requirement for the analysis of results
        return precond;
    }

    /*for domain*/
    public ArrayList<String> domain(String purpose, TestCaseImplementation testCase){
        Pattern p = Pattern.compile("\\<(.*?)\\>");
        Matcher m = p.matcher(purpose);

        /*Generation of classes*/
        m.find();
        String classA = m.group().toString();
        String classAnoSymb = classA.replace(">","").replace("<","");
        String noClassA =  "<No"+classAnoSymb.split("(#|\\/)")[classA.split("(#)").length-1]+">";
        m.find();
        String R = m.group().toString();
        m.find();
        String classB = m.group().toString();


        /*Preconditions*/
        ArrayList<String> precond = new ArrayList<>();
        precond.add("Class("+classA+")");
        precond.add("Property("+R+")");
        precond.add("Class("+classB+")");

        /*Axioms to be added*/

        /*NoA*/
        String prep1 = "                       "+noClassA+" rdf:type owl:Class .\n" +
                "                       "+noClassA+" owl:complementOf " + classA+".\n";

        testCase.setPreparation(prep1);

        String classAwithouturi = noClassA.split("(#|\\/)")[classA.split("(#|\\/)").length-1];
        String classBwithouturi = classB.split("(#|\\/)")[classB.split("(#|\\/)").length-1];

        String ind1= "<"+classAwithouturi.toLowerCase().replace(">","").replace("<","")+"001>";
        String ind2 = "<"+classBwithouturi.toLowerCase().replace(">","").replace("<","")+"002>";

        String assertion1 = "                       "+ind1+" rdf:type owl:NamedIndividual, "+ noClassA+".\n"+
                "                       "+ind2 + " rdf:type owl:NamedIndividual,"+classB+".\n"+
                "                       "+ind1 +" "+R+" "+ind2+".\n";
        String expectedoutputassertion1 = "inconsistent";


        LinkedHashMap hashinput = new LinkedHashMap();
        hashinput.put(assertion1,"Assertion 1");

        LinkedHashMap hashoutput = new LinkedHashMap();
        hashoutput.put(assertion1,expectedoutputassertion1);

        hashoutput.putAll(testCase.getAxiomExpectedResult());
        testCase.setAxiomExpectedResult(hashoutput);

        hashinput.putAll(testCase.getAssertions());
        testCase.setAssertions(hashinput);
        testCase.setType("domain"); // add the type of the requirement for the analysis of results
        return precond;

    }

    /*for the generation of subclass, disjoint and equivalence*/
    public static ArrayList<String> multipleSubClassTest(String purpose, TestCaseImplementation testCase){

        Pattern p = Pattern.compile("(.*) subclassof (.*) and (.*)",Pattern.CASE_INSENSITIVE);
        Matcher m = p.matcher(purpose);
        /*Generation of classes*/
        m.find();
        String classA = m.group(1).toString();
        String classAnoSymb = classA;
        String noClassA =  "<No"+classAnoSymb.split("(#|\\/)")[classA.split("(#)").length-1]+">";
        String classA1 =  "<"+classAnoSymb.split("(#|\\/)")[classA.split("(#)").length-1]+"1>";
        classA = "<"+classA+">";
        String classB = m.group(2).toString();
        String noClassB =  "<No"+classB.split("(#|\\/)")[classB.split("(#|\\/)").length-1]+">";
        classB = "<"+classB+">";
        String classC = m.group(3).toString();
        String noClassC =  "<No"+classC.split("(#|\\/)")[classC.split("(#|\\/)").length-1]+">";
        classC = "<"+classC+">";
        /*Preconditions*/
        ArrayList<String> precond = new ArrayList<>();
        precond.add("Class("+classA.replaceAll(" ","")+")");
        precond.add("Class("+classB.replaceAll(" ","")+")");
        precond.add("Class("+classC.replaceAll(" ","")+")");
        testCase.getPrecondition().addAll(precond);

        /*Axioms to be added*/

        String prep1 = "\n                       "+noClassB+" rdf:type owl:Class .\n" +
                "                       "+ noClassB+" owl:complementOf " + classB+".\n"+
                "                       "+ noClassA+" rdf:type owl:Class .\n" +
                "                       "+ noClassA+" owl:complementOf "+classA +" .\n"+
                "                       "+ noClassC+" rdf:type owl:Class .\n" +
                "                       "+ noClassC+" owl:complementOf "+classC +" .\n"+
                "                       ";

        testCase.setPreparation(prep1);

        String assertion1 = "\n                      "+classA1+"      rdfs:subClassOf [\n" +
                "                       "+"                    owl:intersectionOf ( "+noClassA+" \n" +
                "                       "+"                                         "+classB+"\n" +
                "                       "+"                                        );\n" +
                "                       "+"                    rdf:type owl:Class     ] .";
        String expectedoutputassertion1 ="";
        expectedoutputassertion1 = "consistent";

        String assertion2 = "\n                      "+classA1+"      rdfs:subClassOf [\n" +
                "                       "+"                    owl:intersectionOf ( "+classA+" \n" +
                "                       "+"                                         "+noClassB+"\n" +
                "                       "+"                                        );\n" +
                "                       "+"                    rdf:type owl:Class    ] .";
        String expectedoutputassertion2 = "";
        expectedoutputassertion2 = "unsatisfiable";

        String assertion3 = "\n                      "+classA1+"      rdfs:subClassOf [\n" +
                "                       "+"                    owl:intersectionOf ( "+classA+" \n" +
                "                       "+"                                         "+classB+"\n" +
                "                       "+"                                        );\n" +
                "                       "+"                    rdf:type owl:Class    ] .";
        String expectedoutputassertion3 = "";
        expectedoutputassertion3 = "consistent";

        String assertion4 = "\n                      "+classA1+"      rdfs:subClassOf [\n" +
                "                       "+"                    owl:intersectionOf ( "+noClassA+" \n" +
                "                       "+"                                         "+classC+"\n" +
                "                       "+"                                        );\n" +
                "                       "+"                    rdf:type owl:Class     ] .";
        String expectedoutputassertion4 ="";
        expectedoutputassertion4 = "consistent";

        String assertion5 = "\n                      "+classA1+"      rdfs:subClassOf [\n" +
                "                       "+"                    owl:intersectionOf ( "+classA+" \n" +
                "                       "+"                                         "+noClassC+"\n" +
                "                       "+"                                        );\n" +
                "                       "+"                    rdf:type owl:Class    ] .";
        String expectedoutputassertion5 = "";
        expectedoutputassertion5 = "unsatisfiable";


        String assertion6 = "\n                      "+classA1+"      rdfs:subClassOf [\n" +
                "                       "+"                    owl:intersectionOf ( "+classA+" \n" +
                "                       "+"                                         "+classC+"\n" +
                "                       "+"                                        );\n" +
                "                       "+"                    rdf:type owl:Class    ] .";
        String expectedoutputassertion6 = "";
        expectedoutputassertion6 = "consistent";

        LinkedHashMap hashinput = new LinkedHashMap();
        hashinput.put(assertion1,"Assertion 1, "+classA +"and "+classB);
        hashinput.put(assertion2,"Assertion 2, "+classA +"and "+classB);
        hashinput.put(assertion3,"Assertion 3, "+classA +"and "+classB);
        hashinput.put(assertion4,"Assertion 1, "+classA +"and "+classB);
        hashinput.put(assertion5,"Assertion 2, "+classA +"and "+classB);
        hashinput.put(assertion6,"Assertion 3, "+classA +"and "+classB);

        LinkedHashMap hashoutput = new LinkedHashMap();
        hashoutput.put(assertion1,expectedoutputassertion1);
        hashoutput.put(assertion2,expectedoutputassertion2);
        hashoutput.put(assertion3,expectedoutputassertion3);
        hashoutput.put(assertion4,expectedoutputassertion4);
        hashoutput.put(assertion5,expectedoutputassertion5);
        hashoutput.put(assertion6,expectedoutputassertion6);


        hashoutput.putAll(testCase.getAxiomExpectedResult());
        testCase.setAxiomExpectedResult(hashoutput);

        hashinput.putAll(testCase.getAssertions());
        testCase.setAssertions(hashinput);
        testCase.setType("subclass"); // add the type of the requirement for the analysis of results
        return null;
    }

    /*for the generation of subclass, disjoint and equivalence*/
    public static ArrayList<String> subClassTest(String purpose, String type, TestCaseImplementation testCase){

        Pattern p = Pattern.compile("([^\\s]+) (subclassof|equivalentto|disjointwith) ([^\\s]+)",Pattern.CASE_INSENSITIVE);
        Matcher m = p.matcher(purpose);
        String classA="";
        String classB="";
        String noClassA = "";
        String classA1 ="";
        String noClassB="";
        m.find();

        /*Generation of classes*/
        classA = m.group(1).toString();
        String classAnoSymb = classA;
        noClassA = "<No" + classAnoSymb.split("(#|\\/)")[classA.split("(#)").length - 1] + ">";
        classA1 = "<" + classAnoSymb.split("(#|\\/)")[classA.split("(#)").length - 1] + "1>";
        classA = "<" + classA + ">";

        classB = m.group(3).toString();
        noClassB = "<No" + classB.split("(#|\\/)")[classB.split("(#|\\/)").length - 1] + ">";
        classB = "<" + classB + ">";

        /*Preconditions*/
        ArrayList<String> precond = new ArrayList<>();
        precond.add("Class("+classA.replaceAll(" ","")+")");
        precond.add("Class("+classB.replaceAll(" ","")+")");
        testCase.getPrecondition().addAll(precond);

        /*Axioms to be added*/
        LinkedHashMap hashinput = new LinkedHashMap();
        LinkedHashMap hashoutput = new LinkedHashMap();

        if(!classA.equals(classB)) {
            String prep1 = "\n                       " + noClassB + " rdf:type owl:Class .\n" +
                    "                       " + noClassB + " owl:complementOf " + classB + ".\n" +
                    "                       " + noClassA + " rdf:type owl:Class .\n" +
                    "                       " + noClassA + " owl:complementOf " + classA + " .\n" +
                    "                       ";

            testCase.setPreparation(prep1);

            String assertion1 = "\n                      " + classA1 + "      rdfs:subClassOf [\n" +
                    "                       " + "                    owl:intersectionOf ( " + noClassA + " \n" +
                    "                       " + "                                         " + classB + "\n" +
                    "                       " + "                                        );\n" +
                    "                       " + "                    rdf:type owl:Class     ] .";
            String expectedoutputassertion1 = "";
            if (type == "equivalence")
                expectedoutputassertion1 = "unsatisfiable";
            else
                expectedoutputassertion1 = "consistent";

            String assertion2 = "\n                      " + classA1 + "      rdfs:subClassOf [\n" +
                    "                       " + "                    owl:intersectionOf ( " + classA + " \n" +
                    "                       " + "                                         " + noClassB + "\n" +
                    "                       " + "                                        );\n" +
                    "                       " + "                    rdf:type owl:Class    ] .";
            String expectedoutputassertion2 = "";
            if (type == "strict subclass" || type == "equivalence")
                expectedoutputassertion2 = "unsatisfiable";
            else
                expectedoutputassertion2 = "consistent";

            String assertion3 = "\n                      " + classA1 + "      rdfs:subClassOf [\n" +
                    "                       " + "                    owl:intersectionOf ( " + classA + " \n" +
                    "                       " + "                                         " + classB + "\n" +
                    "                       " + "                                        );\n" +
                    "                       " + "                    rdf:type owl:Class    ] .";
            String expectedoutputassertion3 = "";
            if (type == "disjoint")
                expectedoutputassertion3 = "unsatisfiable";
            else
                expectedoutputassertion3 = "consistent";


            hashinput.put(assertion1, "Assertion 1, " + classA + "and " + classB);
            hashinput.put(assertion2, "Assertion 2, " + classA + "and " + classB);
            hashinput.put(assertion3, "Assertion 3, " + classA + "and " + classB);


            hashoutput.put(assertion1, expectedoutputassertion1);
            hashoutput.put(assertion2, expectedoutputassertion2);
            hashoutput.put(assertion3, expectedoutputassertion3);


            hashoutput.putAll(testCase.getAxiomExpectedResult());
            hashinput.putAll(testCase.getAssertions());

        }

        testCase.setAxiomExpectedResult(hashoutput);
        testCase.setAssertions(hashinput);
        testCase.setType("subclass"); // add the type of the requirement for the analysis of results
        return null;
    }

    /*for the generation of an individual of a given class*/
    public static ArrayList<String> individualValue(String purpose, TestCaseImplementation testCase){
        // Pattern p = Pattern.compile("\\<(.*?)\\>");
        Pattern p = Pattern.compile("(.*) subclassOf (.*) value (.*)",Pattern.CASE_INSENSITIVE);
        Matcher m = p.matcher(purpose);
        /*Generation of classes*/
        m.find();
        String classA = m.group(1).toString();
        String indAnoSymb = classA;
        String noClassA =  "<No"+indAnoSymb.split("(#|\\/)")[classA.split("(#)").length-1]+">";
        String classA1 = "<"+classA+"1>";
        classA = "<"+classA+">";
        String P ="<"+ m.group(2).toString()+">";;
        String ind1 = m.group(3).toString();
        ind1 = "<"+ind1+">";
        /*Preconditions*/
        ArrayList<String> precond = new ArrayList<>();
        precond.add("Class("+classA+")");
        precond.add("Property("+P+")");
        precond.add("Individual("+ind1+")");
        testCase.getPrecondition().addAll(precond);

        String ind2= "<individual001>";

        /*Axioms to be added*/
        String preparation ="";

        preparation += "                       "+ind2+" rdf:type  owl:NamedIndividual.\n";
        preparation += "\n                       " + classA1 + " rdf:type owl:Class .\n" ;
        preparation += "\n                       "+ind2+" owl:differentFrom  "+ind1+".\n";



        testCase.setPreparation(preparation);


        String assertion1 =                "                       "+classA1+" rdfs:subClassOf "+classA+",\n" +
                "                       "+"                    [ rdf:type owl:Restriction ;\n" +
                "                       "+"                      owl:onProperty "+P+" ;\n" +
                "                       "+"                      owl:allValuesFrom [ rdf:type owl:Class ;\n" +
                "                       "+"                                          owl:oneOf ( "+ind2+"\n" +
                "                       "+"                                                    )\n" +
                "                       "+"                                        ]\n" +
                "                       "+"                    ] .\n";

        String expectedoutputassertion1 = "unsatisfiable";

        String assertion2 =                "                       "+classA1+" rdfs:subClassOf "+classA+",\n" +
                "                       "+"                    [ rdf:type owl:Restriction ;\n" +
                "                       "+"                      owl:onProperty "+P+" ;\n" +
                "                       "+"                      owl:someValuesOf [ rdf:type owl:Class ;\n" +
                "                       "+"                                          owl:oneOf ( "+ind2+"\n" +
                "                       "+"                                                    )\n" +
                "                       "+"                                        ]\n" +
                "                       "+"                    ] .\n";

        String expectedoutputassertion2 = "consistent";

        LinkedHashMap hashinput = new LinkedHashMap();
        hashinput.put(assertion1,"Assertion 1");
        hashinput.put(assertion2,"Assertion 2");


        LinkedHashMap hashoutput = new LinkedHashMap();
        hashoutput.put(assertion1,expectedoutputassertion1);
        hashoutput.put(assertion2,expectedoutputassertion2);


        testCase.setAxiomExpectedResult(hashoutput);

        testCase.setAssertions(hashinput);
        return null;
    }

    /*for range (strict) universal restriction*/
    public static ArrayList<String> existentialRange(String purpose, TestCaseImplementation testCase){
        ArrayList<String> complementClasses = new ArrayList<>();
        if(purpose.contains("not(")){
            Pattern p = Pattern.compile("not\\((.*?)\\)");
            Matcher m = p.matcher(purpose);
            while(m.find()){
                complementClasses.add(m.group().toString().replace("not(","").replace(")",""));
            }
        }

        Pattern p = Pattern.compile("(.*) subclassof (\\w*)(?!hasparticipant | ?!isparticipantin | ?!haslocation| ?!islocationof | ?!hasrole| ?!isroleof) some (.*)", Pattern.CASE_INSENSITIVE);
        Matcher m = p.matcher(purpose);

        /*Generation of classes*/
        m.find();
        String classA = m.group(1).toString().replaceAll("\\(","").replaceAll("\\)","").replaceAll("  "," ");;
        String classA1 = "<"+classA+"1>";
        classA="<"+classA+">";
        String R = "<"+m.group(2).toString().replaceAll("  ","")+">";

        String classB = "";
        classB = m.group(3).toString().replaceAll("\\(","").replaceAll("\\)","").replaceAll("  "," ");
        String noClassB =  "<No"+classB.split("(#|\\/)")[classB.split("(#|\\/)").length-1]+">";
        classB="<"+classB+">";

        /*Preconditions*/
        ArrayList<String> precond = new ArrayList<>();
        precond.add("Class("+classA+")");
        precond.add("Property("+R+")");
        precond.add("Class("+classB+")");
        testCase.getPrecondition().addAll(precond);

        String ind1= "<individua001>";
        String ind2 = "<individua002>";
        String ind3 = "<individua003>";

        /*Axioms to be added*/
        String preparation ="";
        if(complementClasses.contains(classB)){
            preparation += "\n                       " + noClassB + " rdf:type owl:Class .\n";
        }
        else{
            preparation += "\n                       " + noClassB + " rdf:type owl:Class .\n" +
                    "                       " + noClassB + " owl:complementOf " + classB + ".\n";
        }

        preparation += "                       "+ind1+" rdf:type  owl:NamedIndividual, "+ classA1+".\n"+
                "                       "+ind2 + " rdf:type owl:NamedIndividual, "+noClassB+".\n" ;
        preparation += "\n                       " + classA1 + " rdf:type owl:Class .\n" ;


        testCase.setPreparation(preparation);

        String assertion1 =            "\n                       "+classA1+" rdfs:subClassOf "+classA+",\n" +
                "                       "+"                    [ rdf:type owl:Restriction ;\n" +
                "                       "+"                      owl:onProperty "+R+" ;\n" +
                "                       "+"                      owl:allValuesFrom [ rdf:type owl:Class ;\n" +
                "                       "+"                                          owl:oneOf ( "+ind2+"\n" +
                "                       "+"                                                    )\n" +
                "                       "+"                                        ]\n" +
                "                       "+"                    ] .\n"+
                "                       "+ind1 +" "+R+ " "+ind2+".\n";

        String expectedoutputassertion1 = "inconsistent";

        String assertion2 =                "                       "+classA1+" rdfs:subClassOf "+classA+",\n" +
                "                       "+"                    [ rdf:type owl:Restriction ;\n" +
                "                       "+"                      owl:onProperty "+R+" ;\n" +
                "                       "+"                      owl:allValuesFrom [ rdf:type owl:Class ;\n" +
                "                       "+"                                          owl:oneOf ( "+ind2+"\n" +
                "                       "+"                                                      "+ind3+"\n" +
                "                       "+"                                                    )\n" +
                "                       "+"                                        ]\n" +
                "                       "+"                    ] .\n"+
                "                       "+ind1 +" "+R+ " "+ind2+".\n"+
                "                       "+ind1 +" "+R+ " "+ind3+".\n";



        String expectedoutputassertion2 = "consistent";

        LinkedHashMap hashinput = new LinkedHashMap();
        hashinput.put(assertion1,"Assertion 1");
        hashinput.put(assertion2,"Assertion 2");

        LinkedHashMap hashoutput = new LinkedHashMap();
        hashoutput.put(assertion1,expectedoutputassertion1);
        hashoutput.put(assertion2,expectedoutputassertion2);


        testCase.setAxiomExpectedResult(hashoutput);
        testCase.setAssertions(hashinput);
        testCase.setType("relation-existential"); // add the type of the requirement for the analysis of results
        return precond;

    }

    /*for range (strict) and for OP + universal restriction*/
    public static ArrayList<String> universalRange(String purpose, TestCaseImplementation testCase){
        ArrayList<String> complementClasses = new ArrayList<>();
        if(purpose.contains("not(")){
            Pattern p = Pattern.compile("not\\((.*?)\\)");
            Matcher m = p.matcher(purpose);
            while(m.find()){
                complementClasses.add(m.group().toString().replace("not(","").replace(")",""));
            }
        }

        Pattern p = Pattern.compile("(.*) subclassof (\\w*)(?!hasparticipant | ?!isparticipantin | ?!haslocation| ?!islocationof | ?!hasrole| ?!isroleof) only (.*)", Pattern.CASE_INSENSITIVE);
        Matcher m = p.matcher(purpose);

        /*Generation of classes*/
        m.find();
        String classA = m.group(1).toString().replaceAll("\\(","").replaceAll("\\)","");;
        String classA1 = "<"+classA+"1>";
        classA="<"+classA+">";
        String R = "<"+m.group(2).toString()+">";

        String classB = "";
        classB = m.group(3).toString().replaceAll("\\(","").replaceAll("\\)","");;
        String noClassB =  "<No"+classB.split("(#|\\/)")[classB.split("(#|\\/)").length-1]+">";
        classB="<"+classB+">";

        /*Preconditions*/
        ArrayList<String> precond = new ArrayList<>();
        precond.add("Class("+classA+")");
        precond.add("Property("+R+")");
        precond.add("Class("+classB+")");
        testCase.getPrecondition().addAll(precond);

        String ind1= "<individual001>";
        String ind2 = "<individual002>";
        String ind3 = "<individual003>";

        /*Axioms to be added*/
        String preparation ="";
        if(complementClasses.contains(classB)){
            preparation += "\n                       " + noClassB + " rdf:type owl:Class .\n";
        }
        else{
            preparation += "\n                       " + noClassB + " rdf:type owl:Class .\n" +
                    "                       " + noClassB + " owl:complementOf " + classB + ".\n";
        }
        preparation += "                       "+ind1+" rdf:type  owl:NamedIndividual, "+ classA1+".\n"+
                "                       "+ind2 + " rdf:type owl:NamedIndividual, "+noClassB+".\n";
        preparation += "\n                       " + classA1 + " rdf:type owl:Class .\n" ;


        testCase.setPreparation(preparation);

        String assertion1 =                "                       "+classA1+" rdfs:subClassOf "+classA+",\n" +
                "                       "+"                    [ rdf:type owl:Restriction ;\n" +
                "                       "+"                      owl:onProperty "+R+" ;\n" +
                "                       "+"                      owl:someValuesOf [ rdf:type owl:Class ;\n" +
                "                       "+"                                          owl:oneOf ( "+ind2+"\n" +
                "                       "+"                                                    )\n" +
                "                       "+"                                        ]\n" +
                "                       "+"                    ] .\n"+
                "                       "+ind1 +" "+R+ " "+ind2+".\n";

        String expectedoutputassertion1 = "inconsistent";

        String assertion2 =                "                       "+classA1+" rdfs:subClassOf "+classA+",\n" +
                "                       "+"                    [ rdf:type owl:Restriction ;\n" +
                "                       "+"                      owl:onProperty "+R+" ;\n" +
                "                       "+"                      owl:allValuesFrom [ rdf:type owl:Class ;\n" +
                "                       "+"                                          owl:oneOf ( "+ind2+"\n" +
                "                       "+"                                                      "+ind3+"\n" +
                "                       "+"                                                    )\n" +
                "                       "+"                                        ]\n" +
                "                       "+"                    ] .\n"+
                "                       "+ ind2+" owl:differentFrom "+ ind3+".\n"+
                "                       "+ind1 +" "+R+ " "+ind2+".\n"+
                "                       "+ind1 +" "+R+ " "+ind3+".\n";


        String expectedoutputassertion2 = "inconsistent";


        LinkedHashMap hashinput = new LinkedHashMap();
        hashinput.put(assertion1,"Assertion 1");
        hashinput.put(assertion2,"Assertion 2");


        LinkedHashMap hashoutput = new LinkedHashMap();
        hashoutput.put(assertion1,expectedoutputassertion1);
        hashoutput.put(assertion2,expectedoutputassertion2);


        testCase.setAxiomExpectedResult(hashoutput);
        testCase.setAssertions(hashinput);
        testCase.setType("relation-universal"); // add the type of the requirement for the analysis of results

        return precond;

    }

    /*for range (strict) universal restriction DP*/
    public static ArrayList<String> existentialRangeDP(String purpose, TestCaseImplementation testCase){
        ArrayList<String> complementClasses = new ArrayList<>();
        if(purpose.contains("not(")){
            Pattern p = Pattern.compile("not\\((.*?)\\)");
            Matcher m = p.matcher(purpose);
            while(m.find()){
                complementClasses.add(m.group().toString().replace("not(","").replace(")",""));
            }
        }

        Pattern p = Pattern.compile("(.*) subclassof (.*) some (xsd:boolean|boolean|xsd:string|xsd:float|xsd:integer|string|float|integer)", Pattern.CASE_INSENSITIVE);
        Matcher m = p.matcher(purpose);

        /*Generation of classes*/
        m.find();
        String classA = m.group(1).toString().replaceAll("\\(","").replaceAll("\\)","");;
        String classA1 = "<"+classA+"1>";
        classA="<"+classA+">";
        String R = "<"+m.group(2).toString()+">";

        String datatype = "";
        datatype = m.group(3).toString().replaceAll("\\(","").replaceAll("\\)","");;
        String nodatatype =  "<No"+datatype.split("(#|\\/)")[datatype.split("(#|\\/)").length-1]+">";
        datatype="<"+"http://www.w3.org/2001/XMLSchema#"+datatype+">";

        /*Preconditions*/
        ArrayList<String> precond = new ArrayList<>();
        precond.add("Class("+classA+")");
        precond.add("Property("+R+")");
        testCase.getPrecondition().addAll(precond);

        String ind1= "<individual001>";

        /*Axioms to be added*/
        String preparation ="\n                       "+classA1+" rdfs:subClassOf "+classA+".\n";
        if(complementClasses.contains(datatype)){
            preparation += "\n                       " + nodatatype + " rdf:type rdfs:Datatype .\n";
        }
        else{
            preparation += "\n                       " + nodatatype + " rdf:type rdfs:Datatype .\n" ;
            preparation+="\n                       " + nodatatype +" owl:equivalentClass [ rdf:type rdfs:Datatype ;\n" +
                    "                                owl:datatypeComplementOf "+datatype+"\n" +
                    "                              ] .\n";

        }

        preparation += "                       "+ind1+" rdf:type  owl:NamedIndividual, "+ classA1+".\n";
        preparation += "\n                       " + classA1 + " rdf:type owl:Class .\n" ;

        testCase.setPreparation(preparation);

        String assertion1 =   "\n                       "+ind1 +" "+R+ " \"1\"^^"+nodatatype+".\n";

        String expectedoutputassertion1 = "inconsistent";

        String assertion2 =
                "                       "+ind1 +" "+R+ " "+"\"1\"^^"+nodatatype+".\n"+
                        "                       "+ind1 +" "+R+ " "+"\"1\"^^"+datatype+".\n";

        String expectedoutputassertion2 = "consistent";

        LinkedHashMap hashinput = new LinkedHashMap();
        hashinput.put(assertion1,"Assertion 1");
        hashinput.put(assertion2,"Assertion 2");

        LinkedHashMap hashoutput = new LinkedHashMap();
        hashoutput.put(assertion1,expectedoutputassertion1);
        hashoutput.put(assertion2,expectedoutputassertion2);

        testCase.setAxiomExpectedResult(hashoutput);
        testCase.setAssertions(hashinput);
        testCase.setType("relation-existential"); // add the type of the requirement for the analysis of results
        return precond;
    }

    /*for range (strict) and for DP + universal restriction*/
    public static ArrayList<String> universalRangeDP(String purpose, TestCaseImplementation testCase){
        ArrayList<String> complementClasses = new ArrayList<>();
        if(purpose.contains("not(")){
            Pattern p = Pattern.compile("not\\((.*?)\\)");
            Matcher m = p.matcher(purpose);
            while(m.find()){
                complementClasses.add(m.group().toString().replace("not(","").replace(")",""));
            }
        }

        Pattern p = Pattern.compile("(.*) subclassof (.*) only (xsd:string|xsd:float|xsd:integer|string|float|integer|boolean|xsd:boolean)", Pattern.CASE_INSENSITIVE);
        Matcher m = p.matcher(purpose);

        /*Generation of classes*/
        m.find();
        String classA = m.group(1).toString().replaceAll("\\(","").replaceAll("\\)","");;
        String classA1 = "<"+classA+"1>";
        classA="<"+classA+">";
        String R = "<"+m.group(2).toString()+">";

        String datatype = "";
        datatype = m.group(3).toString().replaceAll("\\(","").replaceAll("\\)","");;
        String nodatatype =  "<No"+datatype.split("(#|\\/)")[datatype.split("(#|\\/)").length-1]+">";
        datatype="<"+"http://www.w3.org/2001/XMLSchema#"+datatype+">";

        /*Preconditions*/
        ArrayList<String> precond = new ArrayList<>();
        precond.add("Class("+classA+")");
        precond.add("Property("+R+")");
        testCase.getPrecondition().addAll(precond);

        String ind1= "<individual001>";

        /*Axioms to be added*/
        String preparation ="\n                       "+classA1+" rdfs:subClassOf "+classA+".\n";
        if(complementClasses.contains(datatype)){
            preparation += "\n                       " + nodatatype + " rdf:type rdfs:Datatype .\n";
        }
        else{
            preparation += "\n                       " + nodatatype + " rdf:type rdfs:Datatype .\n" ;
            preparation+="\n                       " + nodatatype +" owl:equivalentClass [ rdf:type rdfs:Datatype ;\n" +
                    "                                owl:datatypeComplementOf "+datatype+"\n" +
                    "                              ] .\n";
        }

        preparation += "                       "+ind1+" rdf:type  owl:NamedIndividual, "+ classA1+".\n";


        testCase.setPreparation(preparation);

        String assertion1 =   "\n                       "+ind1 +" "+R+ " \"1\"^^"+nodatatype+".\n";
        String expectedoutputassertion1 = "inconsistent";

        String assertion2 =
                "                       "+ind1 +" "+R+ " "+"\"1\"^^"+nodatatype+".\n"+
                        "                       "+ind1 +" "+R+ " "+"\"1\"^^"+datatype+".\n";

        String expectedoutputassertion2 = "inconsistent";

        LinkedHashMap hashinput = new LinkedHashMap();
        hashinput.put(assertion1,"Assertion 1");
        hashinput.put(assertion2,"Assertion 2");

        LinkedHashMap hashoutput = new LinkedHashMap();
        hashoutput.put(assertion1,expectedoutputassertion1);
        hashoutput.put(assertion2,expectedoutputassertion2);


        testCase.setAxiomExpectedResult(hashoutput);
        testCase.setAssertions(hashinput);
        testCase.setType("relation-universal"); // add the type of the requirement for the analysis of results

        return precond;
    }

    /*for  cardinality */
    public static ArrayList<String> cardinality(String purpose, String type, TestCaseImplementation testCase){
        Pattern p1 = Pattern.compile("(.*) subclassof (.*) (min|max|exactly) ([1-9][0-9]*) (.*)", Pattern.CASE_INSENSITIVE);
        Matcher m = p1.matcher(purpose);

        /*Generation of classes*/
        m.find();
        String classA = m.group(1).toString();

        String classA1 = "<"+classA+"1>";
        classA="<"+classA+">";
        String R = "<"+m.group(2).toString()+">";

        String classB = "";
        classB = m.group(5).toString();
        String noClassB =  "<No"+classB.split("(#|\\/)")[classB.split("(#|\\/)").length-1]+">";
        classB="<"+classB+">";

        Integer num = Integer.parseInt(m.group(4).toString());
        /*Preconditions*/
        ArrayList<String> precond = new ArrayList<>();
        precond.add("Class("+classA+")");
        precond.add("Property("+R+")");
        precond.add("Class("+classB+")");
        testCase.getPrecondition().addAll(precond);

        /*Axioms to be added*/

        String preparation =  "\n                       \t"+classA+" rdf:type owl:Class.\n "+
                "                       \t"+classA1+" rdfs:subClassOf "+classA+".\n ";

        testCase.setPreparation(preparation);
        String assertion1 =   "\n                       \t"+classA1+"    rdfs:subClassOf "+classA+" ,\n" +
                "                                                                     "+"[ rdf:type owl:Restriction ;\n" +
                "                                                                     "+ "  owl:onProperty "+R+" ;\n" +
                "                                                                     "+ "  owl:maxQualifiedCardinality \""+(num-1)+"\"^^xsd:nonNegativeInteger ;\n" +
                "                                                                     "+ "  owl:onClass "+classB+"\n" +
                "                                                                     "+ "] .\n"+
                "                                                                     "+ R+ " a owl:ObjectProperty.\n";
        String expectedoutputassertion1 = "";
        if (type == "min" || type == "exactly")
            expectedoutputassertion1 = "unsatisfiable";
        else
            expectedoutputassertion1 = "consistent";

        String assertion2 =  "\n                       \t"+classA1+" rdfs:subClassOf "+classA+" ,\n" +
                "                                                                     "+ "[ rdf:type owl:Restriction ;\n" +
                "                                                                     "+ "  owl:onProperty "+R+" ;\n" +
                "                                                                     "+ "  owl:minQualifiedCardinality \""+(num+1)+"\"^^xsd:nonNegativeInteger ;\n" +
                "                                                                     "+"  owl:onClass "+classB+"\n" +
                "                                                                     "+ "] .\n"+
                "                                                                     "+ R+ " a owl:ObjectProperty.\n";
        String expectedoutputassertion2 = "";
        if (type == "max" || type == "exactly")
            expectedoutputassertion2 = "unsatisfiable";
        else
            expectedoutputassertion2 = "consistent";

        String assertion3 ="";
        if(type == "max") {
            assertion3 = "\n                       \t" + classA1 + " rdfs:subClassOf " + classA + " ,\n" +
                    "                                                                     " + "[ rdf:type owl:Restriction ;\n" +
                    "                                                                     " + "  owl:onProperty " + R + " ;\n" +
                    "                                                                     " + "  owl:minQualifiedCardinality \"" + (num) + "\"^^xsd:nonNegativeInteger ;\n" +
                    "                                                                     " + "  owl:onClass " + classB + "\n" +
                    "                                                                     " + "] .\n" +
                    "                                                                     " + R + " a owl:ObjectProperty.\n";
        }else{
            assertion3 = "\n                       \t" + classA1 + " rdfs:subClassOf " + classA + " ,\n" +
                    "                                                                     " + "[ rdf:type owl:Restriction ;\n" +
                    "                                                                     " + "  owl:onProperty " + R + " ;\n" +
                    "                                                                     " + "  owl:maxQualifiedCardinality \"" + (num) + "\"^^xsd:nonNegativeInteger ;\n" +
                    "                                                                     " + "  owl:onClass " + classB + "\n" +
                    "                                                                     " + "] .\n" +
                    "                                                                     " + R + " a owl:ObjectProperty.\n";
        }
        String expectedoutputassertion3 = "consistent";



        LinkedHashMap hashinput = new LinkedHashMap();
        hashinput.put(assertion1,"Assertion 1 ");
        hashinput.put(assertion2,"Assertion 2");
        hashinput.put(assertion3,"Assertion 3");


        LinkedHashMap hashoutput = new LinkedHashMap();
        hashoutput.put(assertion1,expectedoutputassertion1);
        hashoutput.put(assertion2,expectedoutputassertion2);
        hashoutput.put(assertion3,expectedoutputassertion3);

        hashoutput.putAll(testCase.getAxiomExpectedResult());
        testCase.setAxiomExpectedResult(hashoutput);

        hashinput.putAll(testCase.getAssertions());
        testCase.setAssertions(hashinput);
        testCase.setType("cardinality"); // add the type of the requirement for the analysis of results
        return precond;

    }

    /*for intersection + cardinality*/ /*Poner opciones para min, exact y sin cardinalidad?*/
    public static ArrayList<String> intersectionCardTest(String purpose, String type, TestCaseImplementation testCase){
        ArrayList<String> complementClasses = new ArrayList<>();
        if(purpose.contains("not(")){
            Pattern p = Pattern.compile("not\\((.*?)\\)");
            Matcher m = p.matcher(purpose);
            while(m.find()){
                complementClasses.add(m.group().toString().replace("not(","").replace(")",""));
            }
        }
        Pattern p = Pattern.compile("(.*) subclassof (.*) (max|min|exactly) (\\d+) \\((.*) and (.*)\\)", Pattern.CASE_INSENSITIVE);
        Matcher m = p.matcher(purpose);

        /*Generation of classes*/
        m.find();
        String classA = m.group(1).toString().replaceAll("\\(","").replaceAll("\\)","");
        String classA1 = "<"+classA+"1>";
        classA = "<"+classA+">";

        String R = "<"+m.group(2).toString()+">";

        String classB = m.group(5).toString().replaceAll("\\(","").replaceAll("\\)","");
        String noClassB =  "<No"+classB.split("(#|\\/)")[classB.split("(#|\\/)").length-1]+">";
        classB ="<"+classB+">";

        String classC = m.group(6).toString().replaceAll("\\(","").replaceAll("\\)","");
        String noClassC =  "<No"+classC.split("(#|\\/)")[classB.split("(#|\\/)").length-1]+">";
        classC ="<"+classC+">";

        Integer num = Integer.parseInt(m.group(4).toString().replace("min","").replace("max","").replace("exactly","").replace(" ",""));

        /*Preconditions*/
        ArrayList<String> precond = new ArrayList<>();
        precond.add("Class("+classA+")\n");
        precond.add("Property("+R+")\n");
        precond.add("Class("+classB+")\n");
        precond.add("Class("+classC+")\n");

        testCase.getPrecondition().addAll(precond);

        /*Axioms to be added*/

        String prep1 = "";
        if(complementClasses.contains(classB)){
            prep1 += "\n                       " + noClassB + " rdf:type owl:Class .\n";
        }
        else{
            prep1 += "\n                       " + noClassB + " rdf:type owl:Class .\n" +
                    "                       " + noClassB + " owl:complementOf " + classB + ".\n";
        }

        if(complementClasses.contains(classC)){
            prep1+= "                       " + noClassC + " rdf:type owl:Class .\n";
        }
        else{
            prep1 +=  "                       " + noClassC + " rdf:type owl:Class .\n" +
                    "                       " + noClassC + " owl:complementOf " + classC + " .\n";
        }

        testCase.setPreparation(prep1);

        String compclassB = "";
        String compClassC = "";

        if(complementClasses.contains(classB)) {
            compclassB = "[ rdf:type owl:Class ;\n" +
                    "          owl:complementOf " + classB + "\n" +
                    "]";
        }
        else {
            compclassB = compclassB.concat(classB);
        }

        if(complementClasses.contains(classC)) {
            compClassC = "[ rdf:type owl:Class ;\n" +
                    "          owl:complementOf " + classC + "\n" +
                    "]";
        }
        else {
            compClassC = classC.toString();
        }
        String assertion1 =   "                       \t"+classA1+"    rdfs:subClassOf "+classA+" ,\n" +
                "                      "+"[ rdf:type owl:Restriction ;\n" +
                "                      "+ "  owl:onProperty "+R+" ;\n" +
                "                      "+ "  owl:maxQualifiedCardinality \""+(num-1)+"\"^^xsd:nonNegativeInteger ;\n" +
                "                        "+ "  owl:onClass  [ owl:intersectionOf ( \n" +
                "                                  "+compclassB+"\n" +
                "                                  "+compClassC+"\n"+
                "                                  );"+
                "                                   rdf:type owl:Class\n" +
                "                             ]"+
                "                          "+ "] .\n"+
                "                           "+ R+ "a owl:ObjectProperty.\n";
        String expectedoutputassertion1 = "";
        if (type == "min" || type == "exactly")
            expectedoutputassertion1 = "unsatisfiable";
        else
            expectedoutputassertion1 = "consistent";

        String assertion2 =  "                       \t"+classA1+" rdfs:subClassOf "+classA+" ,\n" +
                "                                                                     "+ "[ rdf:type owl:Restriction ;\n" +
                "                                                                     "+ "  owl:onProperty "+R+" ;\n" +
                "                                                                     "+ "  owl:minQualifiedCardinality \""+(num+1)+"\"^^xsd:nonNegativeInteger ;\n" +
                "                                                                     "+ "  owl:onClass  [ owl:intersectionOf ( \n" +
                "                                                                                               "+compclassB+"\n" +
                "                                                                                               "+compClassC+"\n"+
                "                                                                                               );"+
                "                                                                                         rdf:type owl:Class\n" +
                "                                                                                   ]"+
                "                                                                     "+ "] .\n"+
                "                                                                     "+ R+ " a owl:ObjectProperty.\n";
        String expectedoutputassertion2 = "";
        if (type == "max" || type == "exactly")
            expectedoutputassertion2 = "unsatisfiable";
        else
            expectedoutputassertion2 = "consistent";

        String assertion3 ="";
        if(type == "max") {
            assertion3 = "                       \t" + classA1 + " rdfs:subClassOf " + classA + " ,\n" +
                    "                                                                     " + "[ rdf:type owl:Restriction ;\n" +
                    "                                                                     " + "  owl:onProperty " + R + " ;\n" +
                    "                                                                     " + "  owl:minQualifiedCardinality \"" + (num) + "\"^^xsd:nonNegativeInteger ;\n" +
                    "                                                                     "+ "  owl:onClass  [ owl:intersectionOf ( \n" +
                    "                                                                                               "+compclassB+"\n" +
                    "                                                                                               "+compClassC+"\n"+
                    "                                                                                               );"+
                    "                                                                                         rdf:type owl:Class\n" +
                    "                                                                                   ]"+
                    "                                                                     " + "] .\n" +
                    "                                                                     " + R + " a owl:ObjectProperty.\n";
        }else{
            assertion3 = "                       \t" + classA1 + " rdfs:subClassOf " + classA + " ,\n" +
                    "                                                                     " + "[ rdf:type owl:Restriction ;\n" +
                    "                                                                     " + "  owl:onProperty " + R + " ;\n" +
                    "                                                                     " + "  owl:maxQualifiedCardinality \"" + (num) + "\"^^xsd:nonNegativeInteger ;\n" +
                    "                                                                     "+ "  owl:onClass  [ owl:intersectionOf ( " +
                    "                                                                                               "+compclassB+"" +
                    "                                                                                               "+compClassC+""+
                    "                                                                                               );"+
                    "                                                                                         rdf:type owl:Class\n" +
                    "                                                                                   ]"+
                    "                                                                     " + "] .\n" +
                    "                                                                     " + R + " a owl:ObjectProperty.\n";
        }
        String expectedoutputassertion3 = "consistent";

        String assertion4 ="";
        if(type== "max") {
            //String assertion4 = "                       " + a1 + " " + R + " " + nob1 + ".";
            assertion4 =   "                       \t"+classA1+"    rdfs:subClassOf "+classA+" ,\n" +
                    "                      "+"[ rdf:type owl:Restriction ;\n" +
                    "                      "+ "  owl:onProperty "+R+" ;\n" +
                    "                      "+ "  owl:minQualifiedCardinality \""+(num+1)+"\"^^xsd:nonNegativeInteger ;\n" +
                    "                        "+ " owl:onClass  "+compclassB+"\n"+
                    "                          "+ "] .\n"+
                    "                           "+ R+ "a owl:ObjectProperty.\n";
        }
        String expectedoutputassertion4 = "consistent";

        String assertion5 ="";
        if(type =="max"){

            //String assertion4 = "                       " + a1 + " " + R + " " + nob1 + ".";
            assertion5 =   "                       \t"+classA1+"    rdfs:subClassOf "+classA+" ,\n" +
                    "                      "+"[ rdf:type owl:Restriction ;\n" +
                    "                      "+ "  owl:onProperty "+R+" ;\n" +
                    "                      "+ "  owl:minQualifiedCardinality \""+(num+1)+"\"^^xsd:nonNegativeInteger ;\n" +
                    "                        "+ " owl:onClass  "+compClassC+"\n"+
                    "                          "+ "] .\n"+
                    "                           "+ R+ "a owl:ObjectProperty.\n";


        }
        String expectedoutputassertion5 = "consistent";

        String assertion6 ="";
        if(type == "max"){
            assertion6 = "                       \t" + classA1 + " rdfs:subClassOf " + classA + " ,\n" +
                    "                                                                     " + "[ rdf:type owl:Restriction ;\n" +
                    "                                                                     " + "  owl:onProperty " + R + " ;\n" +
                    "                                                                     " + "  owl:minQualifiedCardinality \"" + (num+1) + "\"^^xsd:nonNegativeInteger ;\n" +
                    "                                                                     "+ "   owl:onClass  [ owl:intersectionOf ( \n" +
                    "                                                                                               "+compclassB+"\n" +
                    "                                                                                               "+compClassC+"\n"+
                    "                                                                                               );"+
                    "                                                                                         rdf:type owl:Class\n" +
                    "                                                                                   ]"+
                    "                                                                     " + "] .\n" +
                    "                                                                     " + R + " a owl:ObjectProperty.\n";
        }
        String expectedoutputassertion6 = "unsatisfiable";

        LinkedHashMap hashinput = new LinkedHashMap();
        hashinput.put(assertion1,"Assertion 1");
        hashinput.put(assertion2,"Assertion 2");
        hashinput.put(assertion3,"Assertion 3");
        hashinput.put(assertion4,"Assertion 4");
        hashinput.put(assertion5,"Assertion 5");
        hashinput.put(assertion6,"Assertion 6");

        LinkedHashMap hashoutput = new LinkedHashMap();
        hashoutput.put(assertion1,expectedoutputassertion1);
        hashoutput.put(assertion2,expectedoutputassertion2);
        hashoutput.put(assertion3,expectedoutputassertion3);
        hashoutput.put(assertion4,expectedoutputassertion4);
        hashoutput.put(assertion5,expectedoutputassertion5);
        hashoutput.put(assertion6,expectedoutputassertion6);


        hashoutput.putAll(testCase.getAxiomExpectedResult());
        testCase.setAxiomExpectedResult(hashoutput);

        hashinput.putAll(testCase.getAssertions());
        testCase.setAssertions(hashinput);
        testCase.setType("cardinality-intersection"); // add the type of the requirement for the analysis of results

        return precond;
    }

    /*for union */
    public static ArrayList<String> unionTest(String purpose, String type, TestCaseImplementation testCase){
        Pattern p = Pattern.compile("(.*) subclassOf (.*) only (.*) or (.*)", Pattern.CASE_INSENSITIVE);
        Matcher m = p.matcher(purpose);

        /*Generation of classes*/
        m.find();
        String classA = m.group(1).toString();
        String classA1 =  "<"+classA.split("(#|\\/)")[classA.split("(#)").length-1]+"1>";
        classA = "<"+classA+">";

        String R ="<"+m.group(2).toString()+">";
        String classB = m.group(3).toString();
        String noClassB =  "<No"+classB.split("(#|\\/)")[classB.split("(#|\\/)").length-1]+">";
        classB = "<"+classB+">";
        String classC = m.group(4).toString();
        String noClassC =  "<No"+classC.split("(#|\\/)")[classB.split("(#|\\/)").length-1]+">";
        classC = "<"+classC+">";

        /*Preconditions*/
        ArrayList<String> precond = new ArrayList<>();
        precond.add("Class("+classA+")");
        precond.add("Class("+classB+")");
        precond.add("Class("+classC+")");
        precond.add("Property("+R+")");
        testCase.getPrecondition().addAll(precond);

        /*Axioms to be added*/

        String prep1 = "\n                       "+noClassB+" rdf:type owl:Class .\n" +
                "                       "+ noClassB+" owl:complementOf " + classB+".\n"+
                "                       "+noClassC+" rdf:type owl:Class .\n" +
                "                       "+ noClassC+" owl:complementOf "+classC +" .\n"+
                "                       "+ classA1+" rdf:type owl:Class .\n"+
                "                       "+ classA1 +" rdfs:subClassOf "+classA +" .\n"+
                "                       ";

        testCase.setPreparation(prep1);

        String assertion1 = "\n                      "+ classA1 +"rdf:type owl:Class ;\n" +
                "                       "+ "    rdfs:subClassOf [ rdf:type owl:Restriction ;\n" +
                "                       "+ "                      owl:onProperty "+R+" ;\n" +
                "                       "+ "                      owl:allValuesFrom [ owl:intersectionOf ( "+classB+"\n" +
                "                       "+ "                                                               "+classB+"\n" +
                "                       "+ "                                                             ) ;\n" +
                "                       "+ "                                          rdf:type owl:Class\n" +
                "                       "+  "                                        ]\n" +
                "                       "+  "                    ] .";
        String expectedoutputassertion1 ="";
        expectedoutputassertion1 = "consistent";

        String assertion2 = "\n                      "+ classA1 +"rdf:type owl:Class ;\n" +
                "                       "+ "    rdfs:subClassOf [ rdf:type owl:Restriction ;\n" +
                "                       "+ "                      owl:onProperty "+R+" ;\n" +
                "                       "+ "                      owl:allValuesFrom [ owl:intersectionOf ( "+noClassB+"\n" +
                "                       "+ "                                                               "+noClassC+"\n" +
                "                       "+ "                                                             ) ;\n" +
                "                       "+ "                                          rdf:type owl:Class\n" +
                "                       "+  "                                        ]\n" +
                "                       "+  "                    ] .";

        String expectedoutputassertion2 ="";
        expectedoutputassertion2 = "unsatisfiable";

        String assertion3 = "\n                      "+ classA1 +"rdf:type owl:Class ;\n" +
                "                       "+ "    rdfs:subClassOf [ rdf:type owl:Restriction ;\n" +
                "                       "+ "                      owl:onProperty "+R+" ;\n" +
                "                       "+ "                      owl:allValuesFrom [ owl:intersectionOf ( "+classB+"\n" +
                "                       "+ "                                                               "+noClassC+"\n" +
                "                       "+ "                                                             ) ;\n" +
                "                       "+ "                                          rdf:type owl:Class\n" +
                "                       "+  "                                        ]\n" +
                "                       "+  "                    ] .";
        String expectedoutputassertion3 = "";

        expectedoutputassertion3 = "consistent";

        String assertion4 = "\n                      "+ classA1 +"rdf:type owl:Class ;\n" +
                "                       "+ "    rdfs:subClassOf [ rdf:type owl:Restriction ;\n" +
                "                       "+ "                      owl:onProperty "+R+" ;\n" +
                "                       "+ "                      owl:allValuesFrom [ owl:intersectionOf ( "+noClassB+"\n" +
                "                       "+ "                                                               "+classC+"\n" +
                "                       "+ "                                                             ) ;\n" +
                "                       "+ "                                          rdf:type owl:Class\n" +
                "                       "+  "                                        ]\n" +
                "                       "+  "                    ] .";
        String expectedoutputassertion4 = "";

        expectedoutputassertion4 = "consistent";

        LinkedHashMap hashinput = new LinkedHashMap();
        hashinput.put(assertion1,"Assertion 1 ");
        hashinput.put(assertion2,"Assertion 2");
        hashinput.put(assertion3,"Assertion 3");
        hashinput.put(assertion4,"Assertion 4");


        LinkedHashMap hashoutput = new LinkedHashMap();
        hashoutput.put(assertion1,expectedoutputassertion1);
        hashoutput.put(assertion2,expectedoutputassertion2);
        hashoutput.put(assertion3,expectedoutputassertion3);
        hashoutput.put(assertion4,expectedoutputassertion4);


        hashoutput.putAll(testCase.getAxiomExpectedResult());
        testCase.setAxiomExpectedResult(hashoutput);

        hashinput.putAll(testCase.getAssertions());
        testCase.setAssertions(hashinput);
        testCase.setType("union");
        return null;
    }

    /*for intersection*/
    public static ArrayList<String> intersectionTest(String purpose, String type, TestCaseImplementation testCase){
        Pattern p = Pattern.compile("(.*) subclassOf (.*) some (.*) and (.*)", Pattern.CASE_INSENSITIVE);
        Matcher m = p.matcher(purpose);

        /*Generation of classes*/
        m.find();
        String classA = m.group(1).toString();
        String classA1 =  "<"+classA.split("(#|\\/)")[classA.split("(#)").length-1]+"1>";
        classA = "<"+classA+">";

        String R ="<"+m.group(2).toString()+">";
        String classB = m.group(3).toString();

        String noClassB =  "<No"+classB.split("(#|\\/)")[classB.split("(#|\\/)").length-1]+">";
        classB = "<"+classB+">";

        String classC = m.group(4).toString();
        //System.out.println(classC);
        String noClassC =  "<No"+classC.split("(#|\\/)")[classB.split("(#|\\/)").length-1]+">";
        classC = "<"+classC+">";

        /*Preconditions*/
        ArrayList<String> precond = new ArrayList<>();
        precond.add("Class("+classA+")");
        precond.add("Class("+classB+")");
        precond.add("Class("+classC+")");
        precond.add("Property("+R+")");
        testCase.getPrecondition().addAll(precond);

        /*Axioms to be added*/

        String prep1 = "\n                       "+noClassB+" rdf:type owl:Class .\n" +
                "                       "+ noClassB+" owl:complementOf " + classB+".\n"+
                "                       "+noClassC+" rdf:type owl:Class .\n" +
                "                       "+ noClassC+" owl:complementOf "+classC +" .\n"+
                "                       "+ classA1+" rdf:type owl:Class .\n"+
                "                       "+ classA1 +" rdfs:subClassOf "+classA +" .\n"+
                "                       ";

        testCase.setPreparation(prep1);

        String assertion1 = "\n                      "+ classA1 +"rdf:type owl:Class ;\n" +
                "                       "+ "    rdfs:subClassOf [ rdf:type owl:Restriction ;\n" +
                "                       "+ "                      owl:onProperty "+R+" ;\n" +
                "                       "+ "                      owl:allValuesFrom [ owl:intersectionOf ( "+classB+"\n" +
                "                       "+ "                                                               "+classB+"\n" +
                "                       "+ "                                                             ) ;\n" +
                "                       "+ "                                          rdf:type owl:Class\n" +
                "                       "+  "                                        ]\n" +
                "                       "+  "                    ] .";
        String expectedoutputassertion1 ="";
        expectedoutputassertion1 = "consistent";

        String assertion2 = "\n                      "+ classA1 +"rdf:type owl:Class ;\n" +
                "                       "+ "    rdfs:subClassOf [ rdf:type owl:Restriction ;\n" +
                "                       "+ "                      owl:onProperty "+R+" ;\n" +
                "                       "+ "                      owl:allValuesFrom [ owl:intersectionOf ( "+noClassB+"\n" +
                "                       "+ "                                                               "+noClassC+"\n" +
                "                       "+ "                                                             ) ;\n" +
                "                       "+ "                                          rdf:type owl:Class\n" +
                "                       "+  "                                        ]\n" +
                "                       "+  "                    ] .";

        String expectedoutputassertion2 ="";
        expectedoutputassertion2 = "unsatisfiable";

        String assertion3 = "\n                      "+ classA1 +"rdf:type owl:Class ;\n" +
                "                       "+ "    rdfs:subClassOf [ rdf:type owl:Restriction ;\n" +
                "                       "+ "                      owl:onProperty "+R+" ;\n" +
                "                       "+ "                      owl:allValuesFrom [ owl:intersectionOf ( "+classB+"\n" +
                "                       "+ "                                                               "+noClassC+"\n" +
                "                       "+ "                                                             ) ;\n" +
                "                       "+ "                                          rdf:type owl:Class\n" +
                "                       "+  "                                        ]\n" +
                "                       "+  "                    ] .";
        String expectedoutputassertion3 = "";

        expectedoutputassertion3 = "unsatisfiable";

        String assertion4 = "\n                      "+ classA1 +"rdf:type owl:Class ;\n" +
                "                       "+ "    rdfs:subClassOf [ rdf:type owl:Restriction ;\n" +
                "                       "+ "                      owl:onProperty "+R+" ;\n" +
                "                       "+ "                      owl:allValuesFrom [ owl:intersectionOf ( "+noClassB+"\n" +
                "                       "+ "                                                               "+classC+"\n" +
                "                       "+ "                                                             ) ;\n" +
                "                       "+ "                                          rdf:type owl:Class\n" +
                "                       "+  "                                        ]\n" +
                "                       "+  "                    ] .";
        String expectedoutputassertion4 = "";

        expectedoutputassertion4 = "unsatisfiable";

        LinkedHashMap hashinput = new LinkedHashMap();
        hashinput.put(assertion1,"Assertion 1 ");
        hashinput.put(assertion2,"Assertion 2");
        hashinput.put(assertion3,"Assertion 3");
        hashinput.put(assertion4,"Assertion 4");


        LinkedHashMap hashoutput = new LinkedHashMap();
        hashoutput.put(assertion1,expectedoutputassertion1);
        hashoutput.put(assertion2,expectedoutputassertion2);
        hashoutput.put(assertion3,expectedoutputassertion3);
        hashoutput.put(assertion4,expectedoutputassertion4);


        hashoutput.putAll(testCase.getAxiomExpectedResult());
        testCase.setAxiomExpectedResult(hashoutput);

        hashinput.putAll(testCase.getAssertions());
        testCase.setAssertions(hashinput);
        testCase.setType("intersection"); // add the type of the requirement for the analysis of results

        return null;
    }

    /*for part-whole relations*/
    public static ArrayList<String> partWholeTest(String purpose, TestCaseImplementation testCase){
        Pattern p = Pattern.compile("(.*) subclassof (ispartof|partof) (some|only) (.*)", Pattern.CASE_INSENSITIVE);
        Matcher m = p.matcher(purpose);

        /*Generation of classes*/
        m.find();
        String classA = m.group(1).toString();
        String classA1 = "<"+classA+"1>";
        classA = "<"+classA+">";
        String R = "<"+m.group(2).toString()+">";
        String classB = "";
        classB = m.group(4).toString();
        String noClassB =  "<No"+classB.split("(#|\\/)")[classB.split("(#|\\/)").length-1]+">";
        classB = "<"+classB+">";

        /*Preconditions*/
        ArrayList<String> precond = new ArrayList<>();
        precond.add("Class("+classA+")");
        precond.add("Property("+R+")");
        precond.add("Class("+classB+")");
        testCase.getPrecondition().addAll(precond);

        String classA1withouturi = classA1.split("(#|\\/)")[classA1.split("(#|\\/)").length-1];
        String classBwithouturi = classB.split("(#|\\/)")[noClassB.split("(#|\\/)").length-1];

        String indNoB= "<"+classA1withouturi.toLowerCase().replace(">","").replace("<","")+"001>";
        String indB = "<"+classBwithouturi.toLowerCase().replace(">","").replace("<","")+"002>";


        String preparation = "                       "+noClassB+" rdf:type owl:Class .\n" +
                "                       "+noClassB+" owl:complementOf " + classB+" .\n"+
                "                       "+indB+" rdf:type  owl:NamedIndividual, "+ classB+".\n"+
                "                       "+indNoB + " rdf:type owl:NamedIndividual, "+noClassB+".\n" +
                "                        indC rdf:type owl:NamedIndividual, C.\n" +
                "                        C rdf:type owl:Class .\n" +
                "                        C rdfs:subClassOf   [ rdf:type owl:Restriction ;\n" +
                "                       "+"                      owl:onProperty "+R+" ;\n" +
                "                       "+"                      owl:someValuesOf "+classA+" ;\n" +
                "                       "+"                    ] .\n";


        testCase.setPreparation(preparation);

        String assertion1 = "                       "+ ":indC rdf:type owl:NamedIndividual ,\n" +
                "                       "+ "            :C ,\n" +
                "                       "+ "            [ rdf:type owl:Restriction ;\n" +
                "                       "+  "              owl:onProperty "+R+" ;\n" +
                "                       "+ "              owl:allValuesFrom [ rdf:type owl:Class ;\n" +
                "                       "+ "                                  owl:oneOf ( "+indNoB+"\n" +
                "                       "+ "                                            )\n" +
                "                       "+ "                                ]\n" +
                "                       "+ "            ] ;\n" +
                "                       "+ "   :R "+indNoB+" .\n";

        String expectedoutputassertion1 = "inconsistent";

        String assertion2 = "                       "+ ":indC rdf:type owl:NamedIndividual ,\n" +
                "                       "+ "            :C ,\n" +
                "                       "+ "            [ rdf:type owl:Restriction ;\n" +
                "                       "+  "              owl:onProperty "+R+" ;\n" +
                "                       "+ "              owl:allValuesFrom [ rdf:type owl:Class ;\n" +
                "                       "+ "                                  owl:oneOf ( "+indB+"\n" +
                "                       "+ "                                            )\n" +
                "                       "+ "                                ]\n" +
                "                       "+ "            ] ;\n" +
                "                       "+ "   :R "+indNoB+" .\n";

        String expectedoutputassertion2 = "consistent";

        LinkedHashMap hashinput = new LinkedHashMap();
        hashinput.put(assertion1,"Assertion 1");
        hashinput.put(assertion2,"Assertion 2");

        LinkedHashMap hashoutput = new LinkedHashMap();
        hashoutput.put(assertion1,expectedoutputassertion1);
        hashoutput.put(assertion2,expectedoutputassertion2);

        testCase.setAxiomExpectedResult(hashoutput);
        testCase.setAssertions(hashinput);
        testCase.setType("partWhole");
        return precond;

    }

    /* for subclass of two classes and disjointness between them*/
    public static ArrayList<String> subclassDisjointTest(String purpose, TestCaseImplementation testCase){
        Pattern p = Pattern.compile("(.*) subclassof (.*); (.*) subclassof (.*) and (.*) disjointwith (.*)",Pattern.CASE_INSENSITIVE);
        Matcher m = p.matcher(purpose);
        /*Generation of classes*/
        m.find();
        String classA = m.group(1).toString();
        String classAnoSymb = classA;
        String noClassA =  "<No"+classAnoSymb.split("(#|\\/)")[classA.split("(#)").length-1]+">";
        String classA1 =  "<"+classAnoSymb.split("(#|\\/)")[classA.split("(#)").length-1]+"1>";
        classA = "<"+classA+">";
        String classB = m.group(2).toString();
        String noClassB =  "<No"+classB.split("(#|\\/)")[classB.split("(#|\\/)").length-1]+">";
        classB = "<"+classB+">";
        String classC = m.group(3).toString();
        String noClassC =  "<No"+classC.split("(#|\\/)")[classC.split("(#|\\/)").length-1]+">";
        String classC1 =  "<"+classC.split("(#|\\/)")[classC.split("(#)").length-1]+"1>";
        classC = "<"+classC+">";

        /*Preconditions*/
        ArrayList<String> precond = new ArrayList<>();
        precond.add("Class("+classA+")");
        precond.add("Class("+classC+")");
        precond.add("Class("+classB+")");
        testCase.getPrecondition().addAll(precond);

        /*Axioms to be added*/
        String prep1 = "\n                       "+noClassB+" rdf:type owl:Class .\n" +
                "                       "+ noClassB+" owl:complementOf " + classB+".\n"+
                "                       "+ noClassA+" rdf:type owl:Class .\n" +
                "                       "+ noClassA+" owl:complementOf "+classA +" .\n"+
                "                       "+ noClassC+" rdf:type owl:Class .\n" +
                "                       "+ noClassC+" owl:complementOf "+classC +" .\n"+
                "                       ";

        testCase.setPreparation(prep1);

        String assertion1 = "\n                      "+classA1+"      rdfs:subClassOf [\n" +
                "                       "+"                    owl:intersectionOf ( "+noClassA+" \n" +
                "                       "+"                                         "+classB+"\n" +
                "                       "+"                                        );\n" +
                "                       "+"                    rdf:type owl:Class     ] .";
        String expectedoutputassertion1 ="";
        expectedoutputassertion1 = "consistent";

        String assertion2 = "\n                      "+classA1+"      rdfs:subClassOf [\n" +
                "                       "+"                    owl:intersectionOf ( "+classA+" \n" +
                "                       "+"                                         "+noClassB+"\n" +
                "                       "+"                                        );\n" +
                "                       "+"                    rdf:type owl:Class    ] .";
        String expectedoutputassertion2 = "";
        expectedoutputassertion2 = "unsatisfiable";

        String assertion3 = "\n                      "+classA1+"      rdfs:subClassOf [\n" +
                "                       "+"                    owl:intersectionOf ( "+classA+" \n" +
                "                       "+"                                         "+classB+"\n" +
                "                       "+"                                        );\n" +
                "                       "+"                    rdf:type owl:Class    ] .";
        String expectedoutputassertion3 = "";

        expectedoutputassertion3 = "consistent";

        String assertion4 = "\n                      "+classC1+"      rdfs:subClassOf [\n" +
                "                       "+"                    owl:intersectionOf ( "+noClassC+" \n" +
                "                       "+"                                         "+classB+"\n" +
                "                       "+"                                        );\n" +
                "                       "+"                    rdf:type owl:Class     ] .";
        String expectedoutputassertion4 ="";
        expectedoutputassertion4 = "consistent";

        String assertion5 = "\n                      "+classC1+"      rdfs:subClassOf [\n" +
                "                       "+"                    owl:intersectionOf ( "+classC+" \n" +
                "                       "+"                                         "+noClassB+"\n" +
                "                       "+"                                        );\n" +
                "                       "+"                    rdf:type owl:Class    ] .";
        String expectedoutputassertion5 = "";
        expectedoutputassertion5 = "unsatisfiable";

        String assertion6 = "\n                      "+classC1+"      rdfs:subClassOf [\n" +
                "                       "+"                    owl:intersectionOf ( "+classC+" \n" +
                "                       "+"                                         "+classB+"\n" +
                "                       "+"                                        );\n" +
                "                       "+"                    rdf:type owl:Class    ] .";
        String expectedoutputassertion6 = "";

        expectedoutputassertion6 = "consistent";


        String assertion7 = "\n                      "+classC1+"      rdfs:subClassOf [\n" +
                "                       "+"                    owl:intersectionOf ( "+noClassC+" \n" +
                "                       "+"                                         "+classA+"\n" +
                "                       "+"                                        );\n" +
                "                       "+"                    rdf:type owl:Class     ] .";
        String expectedoutputassertion7 ="";

        expectedoutputassertion7 = "consistent";

        String assertion8 = "\n                      "+classC1+"      rdfs:subClassOf [\n" +
                "                       "+"                    owl:intersectionOf ( "+classC+" \n" +
                "                       "+"                                         "+noClassA+"\n" +
                "                       "+"                                        );\n" +
                "                       "+"                    rdf:type owl:Class    ] .";
        String expectedoutputassertion8 = "";
        expectedoutputassertion8 = "consistent";

        String assertion9 = "\n                      "+classC1+"      rdfs:subClassOf [\n" +
                "                       "+"                    owl:intersectionOf ( "+classC+" \n" +
                "                       "+"                                         "+classA+"\n" +
                "                       "+"                                        );\n" +
                "                       "+"                    rdf:type owl:Class    ] .";
        String expectedoutputassertion9 = "";

        expectedoutputassertion9 = "unsatisfiable";

        LinkedHashMap hashinput = new LinkedHashMap();
        hashinput.put(assertion1,"Assertion 1, "+classA +"and "+classB);
        hashinput.put(assertion2,"Assertion 2, "+classA +"and "+classB);
        hashinput.put(assertion3,"Assertion 3, "+classA +"and "+classB);
        hashinput.put(assertion4,"Assertion 4, "+classA +"and "+classB);
        hashinput.put(assertion5,"Assertion 5, "+classA +"and "+classB);
        hashinput.put(assertion6,"Assertion 6, "+classA +"and "+classB);
        hashinput.put(assertion7,"Assertion 7, "+classA +"and "+classB);
        hashinput.put(assertion8,"Assertion 8, "+classA +"and "+classB);
        hashinput.put(assertion9,"Assertion 9, "+classA +"and "+classB);

        LinkedHashMap hashoutput = new LinkedHashMap();
        hashoutput.put(assertion1,expectedoutputassertion1);
        hashoutput.put(assertion2,expectedoutputassertion2);
        hashoutput.put(assertion3,expectedoutputassertion3);
        hashoutput.put(assertion4,expectedoutputassertion4);
        hashoutput.put(assertion5,expectedoutputassertion5);
        hashoutput.put(assertion6,expectedoutputassertion6);
        hashoutput.put(assertion7,expectedoutputassertion7);
        hashoutput.put(assertion8,expectedoutputassertion8);
        hashoutput.put(assertion9,expectedoutputassertion9);

        hashoutput.putAll(testCase.getAxiomExpectedResult());
        testCase.setAxiomExpectedResult(hashoutput);

        hashinput.putAll(testCase.getAssertions());
        testCase.setAssertions(hashinput);
        testCase.setType("subclass-disjoint");
        return null;

    }

    /*for participant ODP, location ODP and ObjectRole ODP*/
    public static ArrayList<String> participantODPTest(String purpose, TestCaseImplementation testCase){
        Pattern p = Pattern.compile("(.*) subclassof (hasparticipant|isparticipantin|haslocation|islocationof|hasrole|isroleof) (some|only) (.*)", Pattern.CASE_INSENSITIVE);
        Matcher m = p.matcher(purpose);

        /*Generation of classes*/
        m.find();
        String classA = m.group(1).toString();
        String classA1 = "<"+classA+"1>";
        String noClassA =  "<No"+classA.split("(#|\\/)")[classA.split("(#|\\/)").length-1]+">";
        classA = "<"+classA+">";
        String R = "<"+m.group(2).toString()+">";
        String classB =  m.group(4).toString();
        String noClassB =  "<No"+classB.split("(#|\\/)")[classB.split("(#|\\/)").length-1]+">";
        classB = "<"+classB+">";

        /*Preconditions*/
        ArrayList<String> precond = new ArrayList<>();
        precond.add("Class("+classA+")");
        precond.add("Property("+R+")");
        precond.add("Class("+classB+")");
        testCase.getPrecondition().addAll(precond);

        String indA= "<individual001>";
        String indB= "<individual002>";
        String indNoB = "<individual003>";
        String indNoA= "<individual004>";

        /*Axioms to be added*/
        String preparation = "                       "+noClassB+" rdf:type owl:Class .\n" +
                "                       "+noClassB+" owl:complementOf " + classB+" .\n"+
                "                       "+indA+" rdf:type  owl:NamedIndividual, "+ classA1+".\n"+
                "                       "+indB+" rdf:type  owl:NamedIndividual, "+ classB+".\n"+
                "                       "+indNoB + " rdf:type owl:NamedIndividual, "+noClassB+".\n" +
                "                       "+classA1+" rdfs:subClassOf "+classA+",\n" +
                "                       "+"                    [ rdf:type owl:Restriction ;\n" +
                "                       "+"                      owl:onProperty "+R+" ;\n" +
                "                       "+"                      owl:allValuesFrom [ rdf:type owl:Class ;\n" +
                "                       "+"                                          owl:oneOf ( "+indNoB+"\n" +
                "                       "+"                                                    )\n" +
                "                       "+"                                        ]\n" +
                "                       "+"                    ] .\n"+
                "                       "+classB+" rdfs:subClassOf  [ rdf:type owl:Restriction ;\n" +
                "                       "+"                                         owl:onProperty "+R+" ;\n" +
                "                       "+"                                         owl:allValuesFrom [ rdf:type owl:Class ;\n" +
                "                       "+"                                                                 owl:oneOf ( "+indNoA+"\n" +
                "                       "+"                                                                            )\n" +
                "                       "+"                                                           ]\n" +
                "                       "+"                         ] .\n";

        testCase.setPreparation(preparation);

        String assertion1 =  "                       "+indA +" "+R+ " "+indNoB+".\n";
        String expectedoutputassertion1 = "inconsistent";
        String assertion2 = "";
        if(R.contains("isParticipantIn")) {
            assertion2 = "                       " + indB + " :hasParticipant " + indNoA + ".\n";
        }
        else if(R.contains("isLocationOf")) {
            assertion2 = "                       " + indB + " :hasLocation " + indNoA + ".\n";
        }
        else if(R.contains("isRoleOf")){
            assertion2 = "                       " + indB + " :hasRole " + indNoA + ".\n";
        }
        else if(R.contains("hasParticipant")){
            assertion2 = "                       " + indB + " :isParticipantIn " + indNoA + ".\n";
        }
        else if(R.contains("hasLocation")){
            assertion2 = "                       " + indB + " :isLocationOf " + indNoA + ".\n";
        }
        else if(R.contains("hasRole")){
            assertion2 = "                       " + indB + " :isRoleOf " + indNoA + ".\n";
        }
        String expectedoutputassertion2 = "inconsistent";

        LinkedHashMap hashinput = new LinkedHashMap();
        hashinput.put(assertion1,"Assertion 1");
        hashinput.put(assertion2,"Assertion 2");

        LinkedHashMap hashoutput = new LinkedHashMap();
        hashoutput.put(assertion1,expectedoutputassertion1);
        hashoutput.put(assertion2,expectedoutputassertion2);

        testCase.setAxiomExpectedResult(hashoutput);
        testCase.setAssertions(hashinput);
        testCase.setType("participant");
        return precond;
    }

    /*for subclass + OP Test*/
    public static ArrayList<String> subClassOPTest(String purpose, TestCaseImplementation testCase){
        Pattern p = Pattern.compile("(.*) subclassof (.*) that (.*) some (.*)",Pattern.CASE_INSENSITIVE);
        Matcher m = p.matcher(purpose);

        /*Generation of classes*/
        m.find();
        String classA = m.group(1).toString();
        String classAnoSymb = classA.replace(">","").replace("<","");
        String noClassA =  "<No"+classAnoSymb.split("(#|\\/)")[classA.split("(#)").length-1]+">";
        String classA1 =  "<"+classAnoSymb.split("(#|\\/)")[classA.split("(#)").length-1]+"1>";
        classA="<"+classA+">";

        String classB = m.group(2).toString();
        String classBnoSymb = classB.replace(">","").replace("<","");
        String classB1 =  "<"+classBnoSymb.split("(#|\\/)")[classB.split("(#)").length-1]+"1>";
        String noClassB =  "<No"+classB.replace(">","").replace("<","").replace(">","").replace("<","").split("(#|\\/)")[classB.split("(#|\\/)").length-1]+">";
        classB="<"+classB+">";

        String R = m.group(3).toString();
        R="<"+R+">";

        String classC = "";
        classC = m.group(4).toString();
        String noClassC =  "<No"+classC.replace(">","").replace("<","").replace(">","").replace("<","").split("(#|\\/)")[classC.split("(#|\\/)").length-1]+">";
        classC="<"+classC+">";

        String indA= "<individual001>";
        String indNoC = "<individual002>";

        /*Preconditions*/
        ArrayList<String> precond = new ArrayList<>();
        precond.add("Class("+classA+")");
        precond.add("Class("+classB+")");
        precond.add("Property("+R+")");
        precond.add("Class("+classC+")");
        testCase.getPrecondition().addAll(precond);

        /*Axioms to be added*/
        String prep1 = "\n                       "+noClassB+" rdf:type owl:Class .\n" +
                "                       "+ noClassB+" owl:complementOf " + classB+".\n"+
                "                       "+noClassA+" rdf:type owl:Class .\n" +
                "                       "+ noClassA+" owl:complementOf "+classA +" .\n"+
                "                       "+noClassC+" rdf:type owl:Class .\n" +
                "                       "+noClassC+" owl:complementOf " + classC+" .\n"+
                "                       "+indA+" rdf:type  owl:NamedIndividual, "+ classB1+".\n"+
                "                       "+indNoC + " rdf:type owl:NamedIndividual, "+noClassC+".\n" +
                "                       "+classB1+" rdfs:subClassOf "+classB+",\n" +
                "                       "+"                    [ rdf:type owl:Restriction ;\n" +
                "                       "+"                      owl:onProperty "+R+" ;\n" +
                "                       "+"                      owl:allValuesFrom [ rdf:type owl:Class ;\n" +
                "                       "+"                                          owl:oneOf ( "+indNoC+"\n" +
                "                       "+"                                                    )\n" +
                "                       "+"                                        ]\n" +
                "                       "+"                    ] .\n";

        testCase.setPreparation(prep1);

        /************SUBCLASS TEST***************/
        String assertion1 = "\n                      "+classA1+"     rdfs:subClassOf [\n" +
                "                       "+"                    owl:intersectionOf ( "+noClassA+" \n" +
                "                       "+"                                         "+classB+"\n" +
                "                       "+"                                        );\n" +
                "                       "+"                    rdf:type owl:Class" +
                "                       "+"                                  ] .\n";
        String expectedoutputassertion1 ="";
        expectedoutputassertion1 = "consistent";

        String assertion2 = "\n                      "+classA1+"     rdfs:subClassOf [\n" +
                "                       "+"                    owl:intersectionOf ( "+classA+" \n" +
                "                       "+"                                         "+noClassB+"\n" +
                "                       "+"                                        );\n" +
                "                       "+"                    rdf:type owl:Class" +
                "                       "+"                                  ] .\n";
        String expectedoutputassertion2 = "";
        expectedoutputassertion2 = "unsatisfiable";

        String assertion3 = "\n                      "+classA1+"     rdfs:subClassOf [\n" +
                "                       "+"                    owl:intersectionOf ( "+classA+" \n" +
                "                       "+"                                         "+classB+"\n" +
                "                       "+"                                        );\n" +
                "                       "+"                    rdf:type owl:Class" +
                "                       "+"                                  ] .\n";
        String expectedoutputassertion3 = "";
        expectedoutputassertion3 = "consistent";

        /************OP TEST***************/
        String assertion4 =  "                       "+indA +" "+R+ " "+indNoC+".\n";

        String expectedoutputassertion4 = "inconsistent";

        LinkedHashMap hashinput = new LinkedHashMap();
        hashinput.put(assertion1,"Assertion 1");
        hashinput.put(assertion2,"Assertion 2");
        hashinput.put(assertion3,"Assertion 3");
        hashinput.put(assertion4,"Assertion 4");

        LinkedHashMap hashoutput = new LinkedHashMap();
        hashoutput.put(assertion1,expectedoutputassertion1);
        hashoutput.put(assertion2,expectedoutputassertion2);
        hashoutput.put(assertion3,expectedoutputassertion3);
        hashoutput.put(assertion4,expectedoutputassertion4);

        hashoutput.putAll(testCase.getAxiomExpectedResult());
        testCase.setAxiomExpectedResult(hashoutput);

        hashinput.putAll(testCase.getAssertions());
        testCase.setAssertions(hashinput);
        testCase.setType("subclass-relation");
        return null;
    }

    /*for  cardinality + OP*/
    public static ArrayList<String> cardinalityOPTest(String purpose, String type, TestCaseImplementation testCase){
        Pattern p1 = Pattern.compile("(.*) subclassof (.*) min (\\d+) (.*) and (.*) subclassof (.*) some (.*)", Pattern.CASE_INSENSITIVE);
        Matcher m = p1.matcher(purpose);

        /*Generation of classes*/
        m.find();
        String classA = m.group(1).toString();
        String classA1 = "<"+classA.replace(">","").replace("<","")+"1>";
        String R1 = m.group(2).toString();
        String classB = m.group(4).toString();
        String classB1 = "<"+classB.replace(">","").replace("<","")+"1>";
        String R2 = m.group(6).toString();
        String classC = m.group(7).toString();
        String noClassC =  "<No"+classC.replace(">","").replace("<","").replace(">","").replace("<","").split("(#|\\/)")[classC.split("(#|\\/)").length-1]+">";
        classA="<"+classA+">";
        R1="<"+R1+">";
        classB="<"+classB+">";
        classC="<"+classC+">";
        R2="<"+R2+">";
        Integer num = Integer.parseInt(m.group(3).toString().replace(" ",""));


        String indB= "<individual001>";
        String indNoC = "<individual002>";
        String ind3 = "<individual003>";

        /*Preconditions*/
        ArrayList<String> precond = new ArrayList<>();
        precond.add("Class("+classA+")");
        precond.add("Property("+R1+")");
        precond.add("Property("+R2+")");
        precond.add("Class("+classB+")");
        precond.add("Class("+classC+")");
        testCase.getPrecondition().addAll(precond);

        /*Axioms to be added*/
        String preparation =  "\n                       \t"+classA+" rdf:type owl:Class.\n "+
                "                       \t"+classA1+" rdfs:subClassOf "+classA+".\n "+
                "                       "+noClassC+" rdf:type owl:Class .\n" +
                "                       "+noClassC+" owl:complementOf " + classC+" .\n"+
                "                       "+indB+" rdf:type  owl:NamedIndividual, "+ classB1+".\n"+
                "                       "+indNoC + " rdf:type owl:NamedIndividual, "+noClassC+".\n" ;

        testCase.setPreparation(preparation);

        String assertion1 =   "\n                       \t"+classA1+"    rdfs:subClassOf "+classA+" ,\n" +
                "                                                                     "+"[ rdf:type owl:Restriction ;\n" +
                "                                                                     "+ "  owl:onProperty "+R1+" ;\n" +
                "                                                                     "+ "  owl:maxQualifiedCardinality \""+(num-1)+"\"^^xsd:nonNegativeInteger ;\n" +
                "                                                                     "+ "  owl:onClass "+classB+"\n" +
                "                                                                     "+ "] .\n"+
                "                                                                     "+ R1+ " a owl:ObjectProperty.\n";
        String expectedoutputassertion1 = "";

        expectedoutputassertion1 = "unsatisfiable";

        String assertion2 =  "\n                       \t"+classA1+" rdfs:subClassOf "+classA+" ,\n" +
                "                                                                     "+ "[ rdf:type owl:Restriction ;\n" +
                "                                                                     "+ "  owl:onProperty "+R1+" ;\n" +
                "                                                                     "+ "  owl:minQualifiedCardinality \""+(num+1)+"\"^^xsd:nonNegativeInteger ;\n" +
                "                                                                     "+"  owl:onClass "+classB+"\n" +
                "                                                                     "+ "] .\n"+
                "                                                                     "+ R1+ " a owl:ObjectProperty.\n";
        String expectedoutputassertion2 = "";

        expectedoutputassertion2 = "consistent";

        String assertion3 ="";
        if(type == "max") {
            assertion3 = "\n                       \t" + classA1 + " rdfs:subClassOf " + classA + " ,\n" +
                    "                                                                     " + "[ rdf:type owl:Restriction ;\n" +
                    "                                                                     " + "  owl:onProperty " + R1 + " ;\n" +
                    "                                                                     " + "  owl:minQualifiedCardinality \"" + (num) + "\"^^xsd:nonNegativeInteger ;\n" +
                    "                                                                     " + "  owl:onClass " + classB + "\n" +
                    "                                                                     " + "] .\n" +
                    "                                                                     " + R1 + " a owl:ObjectProperty.\n";
        }else{
            assertion3 = "\n                       \t" + classA1 + " rdfs:subClassOf " + classA + " ,\n" +
                    "                                                                     " + "[ rdf:type owl:Restriction ;\n" +
                    "                                                                     " + "  owl:onProperty " + R1 + " ;\n" +
                    "                                                                     " + "  owl:maxQualifiedCardinality \"" + (num) + "\"^^xsd:nonNegativeInteger ;\n" +
                    "                                                                     " + "  owl:onClass " + classB + "\n" +
                    "                                                                     " + "] .\n" +
                    "                                                                     " + R1 + " a owl:ObjectProperty.\n";
        }
        String expectedoutputassertion3 = "consistent";

        String assertion4 =                "                       "+classB1+" rdfs:subClassOf "+classB+",\n" +
                "                       "+"                    [ rdf:type owl:Restriction ;\n" +
                "                       "+"                      owl:onProperty "+R2+" ;\n" +
                "                       "+"                      owl:allValuesFrom [ rdf:type owl:Class ;\n" +
                "                       "+"                                          owl:oneOf ( "+indNoC+"\n" +
                "                       "+"                                                    )\n" +
                "                       "+"                                        ]\n" +
                "                       "+"                    ] .\n"+
                "                       "+indB +" "+R2+ " "+indNoC+".\n";
        String expectedoutputassertion4 ="";
        expectedoutputassertion4 = "inconsistent";

        String assertion5 =                "                       "+classB1+" rdfs:subClassOf "+classB+",\n" +
                "                       "+"                    [ rdf:type owl:Restriction ;\n" +
                "                       "+"                      owl:onProperty "+R2+" ;\n" +
                "                       "+"                      owl:allValuesFrom [ rdf:type owl:Class ;\n" +
                "                       "+"                                          owl:oneOf ( "+indNoC+"\n" +
                "                       "+"                                                      "+ind3+"\n" +
                "                       "+"                                                    )\n" +
                "                       "+"                                        ]\n" +
                "                       "+"                    ] .\n"+
                "                       "+indB +" "+R2+ " "+indNoC+".\n"+
                "                       "+indB +" "+R2+ " "+ind3+".\n";


        String expectedoutputassertion5 ="";
        if(purpose.contains("only"))
            expectedoutputassertion5 = "inconsistent";
        else
            expectedoutputassertion5 = "consistent";

        LinkedHashMap hashinput = new LinkedHashMap();
        hashinput.put(assertion1,"Assertion 1");
        hashinput.put(assertion2,"Assertion 2");
        hashinput.put(assertion3,"Assertion 3");
        hashinput.put(assertion4,"Assertion 4");
        hashinput.put(assertion5,"Assertion 5");

        LinkedHashMap hashoutput = new LinkedHashMap();
        hashoutput.put(assertion1,expectedoutputassertion1);
        hashoutput.put(assertion2,expectedoutputassertion2);
        hashoutput.put(assertion3,expectedoutputassertion3);
        hashoutput.put(assertion4,expectedoutputassertion4);
        hashoutput.put(assertion5,expectedoutputassertion5);

        hashoutput.putAll(testCase.getAxiomExpectedResult());
        testCase.setAxiomExpectedResult(hashoutput);

        hashinput.putAll(testCase.getAssertions());
        testCase.setAssertions(hashinput);
        testCase.setType("cardinality-relation");
        return precond;

    }

    /*for class definition*/
    public static ArrayList<String> definitionTest(String purpose, TestCaseImplementation testCase){

        Pattern p = Pattern.compile("(.*) type class",Pattern.CASE_INSENSITIVE);
        Matcher m = p.matcher(purpose);

        /*Generation of classes*/
        m.find();
        String classA = "<"+m.group(1).toString()+">";


        /*Preconditions*/
        ArrayList<String> precond = new ArrayList<>();
        precond.add("Class("+classA+")");
        testCase.getPrecondition().addAll(precond);
        /*There are no axioms to be added*/
        LinkedHashMap hashinput = new LinkedHashMap();

        LinkedHashMap hashoutput = new LinkedHashMap();

        testCase.setAxiomExpectedResult(hashoutput);

        hashinput.putAll(testCase.getAssertions());
        testCase.setAssertions(hashinput);
        testCase.setType("definition");
        return precond;
    }
}
