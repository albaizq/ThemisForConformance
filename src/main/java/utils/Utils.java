package utils;

import org.coode.owlapi.turtle.TurtleOntologyFormat;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.Test;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.io.OWLOntologyDocumentSource;
import org.semanticweb.owlapi.io.OWLParserException;
import org.semanticweb.owlapi.io.StringDocumentSource;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.util.OWLEntityRenamer;
import org.semanticweb.owlapi.vocab.OWL2Datatype;
import testingsteps.Design;
import testingsteps.Implementation;
import testingsteps.Mapping;
import tests.TestCaseDesign;
import tests.TestCaseResult;
import uk.ac.manchester.cs.owl.owlapi.turtle.parser.TurtleOntologyParser;


import javax.print.DocFlavor;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by albafernandez on 19/06/2017.
 */
public class Utils {


    public static String getPrecTerm(String query){

        Pattern p = Pattern.compile("\\<(.*?)\\>");
        Matcher m = p.matcher(query);
        while(m.find()){
            return  m.group();
        }
        return  m.group();
    }


    public static HashMap<String, IRI> createGot(ArrayList<TestCaseDesign> testsuiteDesign, Ontology ontology) throws Exception {

        //Create GoT
        System.out.println("creating got...");
        ArrayList<String> terms = new ArrayList<>();
        for (TestCaseDesign test : testsuiteDesign) {
            for (String purpose : test.getPurpose()) {
                terms.addAll(Mapping.processTestExpressionToExtractTerms(purpose));
            }
        }
        HashMap<String, IRI> got = new HashMap<>();
        got.putAll(ontology.getClasses());
        got.putAll(ontology.getObjectProperties());
        got.putAll(ontology.getDatatypeProperties());
        got.putAll(ontology.getIndividuals());

        return got;
    }

    public static Ontology loadOntology(String url, ArrayList<TestCaseDesign> testCaseDesigns, String testSuiteProv) throws Exception {

            //Load ontologies
            Ontology ontology = new Ontology();

            if(isValid(url)) {
                ontology.load_ontologyURL(url);
            }else{
                ontology = createSkeletonFromTestCases(testSuiteProv,testCaseDesigns);
            }
           return ontology;
    }

    /* Returns true if url is valid */
    public static boolean isValid(String url)
    {
        /* Try creating a valid URL */
        try {
            new URL(url).toURI();
            return true;
        }

        // If there was an Exception
        // while creating URL object
        catch (Exception e) {
            return false;
        }
    }


    public static Ontology createSkeletonFromTestCases(String key, ArrayList<TestCaseDesign> testCaseDesigns) throws OWLOntologyCreationException {
        Ontology ontology = new Ontology();
        OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
        OWLOntology onto = manager.createOntology() ;
        ontology.setManager(manager);
        OWLDataFactory dataFactory = manager.getOWLDataFactory();
        for(TestCaseDesign testCaseDesign: testCaseDesigns) {
            if (testCaseDesign.getSource().contains(key)) {
                for (String purpose : testCaseDesign.getPurpose()) {
                    String purposecloned = purpose.toLowerCase();
                    if (purposecloned.matches("([^\\s]+) subclassof ([^\\s]+) only ([^\\s]+) or ([^\\s]+)")) {
                        Pattern p = Pattern.compile("([^\\s]+) subclassof ([^\\s]+)(some|only) \\(([^\\s]+) or ([^\\s]+)\\)", Pattern.CASE_INSENSITIVE);
                        Matcher m = p.matcher(purpose);
                        m.find();
                        String term1 = m.group(1).toString().replaceAll(" ", "");
                        String term2 = m.group(2).toString().replaceAll(" ", "");
                        String term3 = m.group(4).toString().replaceAll(" ", "");
                        String term4 = m.group(5).toString().replaceAll(" ", "");
                        OWLClass class1 = dataFactory.getOWLClass(IRI.create(term1));
                        OWLObjectProperty prop = dataFactory.getOWLObjectProperty(IRI.create(term2));
                        OWLClass class2 = dataFactory.getOWLClass(IRI.create(term3));
                        OWLClass class3 = dataFactory.getOWLClass(IRI.create(term4));
                        manager.addAxiom(onto, dataFactory.getOWLSubClassOfAxiom(class1, dataFactory.getOWLObjectAllValuesFrom(prop, dataFactory.getOWLObjectUnionOf(class2, class3))));
                    } else if (purposecloned.matches("([^\\s]+) subclassof ([^\\s]+) (some|only) \\(([^\\s]+) and ([^\\s]+)\\)")) {
                        Pattern p = Pattern.compile("([^\\s]+) subclassof ([^\\s]+) (some|only) \\(([^\\s]+) and ([^\\s]+)\\)", Pattern.CASE_INSENSITIVE);
                        Matcher m = p.matcher(purpose);
                        m.find();
                        String term1 = m.group(1).toString().replaceAll(" ", "");
                        String term2 = m.group(2).toString().replaceAll(" ", "");
                        String term3 = m.group(4).toString().replaceAll(" ", "");
                        String term4 = m.group(5).toString().replaceAll(" ", "");
                        OWLClass class1 = dataFactory.getOWLClass(IRI.create(term1));
                        OWLObjectProperty prop = dataFactory.getOWLObjectProperty(IRI.create(term2));
                        OWLClass class2 = dataFactory.getOWLClass(IRI.create(term3));
                        OWLClass class3 = dataFactory.getOWLClass(IRI.create(term4));
                        manager.addAxiom(onto, dataFactory.getOWLSubClassOfAxiom(class1, dataFactory.getOWLObjectAllValuesFrom(prop, dataFactory.getOWLObjectIntersectionOf(class2, class3))));
                    } else if (purposecloned.matches("([^\\s]+) subclassof ([^\\s]+) value ([^\\s]+)")) {
                        Pattern p = Pattern.compile("([^\\s]+) subclassof ([^\\s]+) value ([^\\s]+)", Pattern.CASE_INSENSITIVE);
                        Matcher m = p.matcher(purpose);
                        m.find();
                        String term1 = m.group(1).toString().replaceAll(" ", "");
                        String term2 = m.group(2).toString().replaceAll(" ", "");
                        String term3 = m.group(3).toString().replaceAll(" ", "");
                        OWLClass class1 = dataFactory.getOWLClass(IRI.create(term1));
                        OWLObjectProperty prop = dataFactory.getOWLObjectProperty(IRI.create(term2));
                        OWLIndividual ind1 = dataFactory.getOWLNamedIndividual(IRI.create(term3));
                        manager.addAxiom(onto, dataFactory.getOWLSubClassOfAxiom(class1, dataFactory.getOWLObjectHasValue(prop, ind1)));
                    } else if (purposecloned.matches("([^\\s]+) subclassof ([^\\s]+)\\s*and\\s*([^\\s]+) subclassof ([^\\s]+) that disjointwith ([^\\s]+)")) {
                        Pattern p = Pattern.compile("(([^\\s]+)) subclassof ([^\\s])+\\s*and\\s*(([^\\s]+)) subclassof (([^\\s]+)) that disjointwith (([^\\s]+))", Pattern.CASE_INSENSITIVE);
                        Matcher m = p.matcher(purpose);
                        m.find();
                        String term1 = m.group(1).toString().replaceAll(" ", "");
                        String term2 = m.group(2).toString().replaceAll(" ", "");
                        String term3 = m.group(3).toString().replaceAll(" ", "");
                        OWLClass class1 = dataFactory.getOWLClass(IRI.create(term1));
                        OWLClass class2 = dataFactory.getOWLClass(IRI.create(term2));
                        OWLClass class3 = dataFactory.getOWLClass(IRI.create(term3));
                        manager.addAxiom(onto, dataFactory.getOWLSubClassOfAxiom(class1, class2));
                        manager.addAxiom(onto, dataFactory.getOWLSubClassOfAxiom(class3, class2));
                        manager.addAxiom(onto, dataFactory.getOWLDisjointClassesAxiom(class3, class1));
                    } else if (purposecloned.matches("([^\\s]+) equivalentto ([^\\s]+)")) {
                        Pattern p = Pattern.compile("([^\\s]+) equivalentto ([^\\s]+)", Pattern.CASE_INSENSITIVE);
                        Matcher m = p.matcher(purpose);
                        m.find();
                        String term1 = m.group(1).toString().replaceAll(" ", "");
                        String term2 = m.group(2).toString().replaceAll(" ", "");
                        OWLClass class1 = dataFactory.getOWLClass(IRI.create(term1));
                        OWLClass class2 = dataFactory.getOWLClass(IRI.create(term2));
                        manager.addAxiom(onto, dataFactory.getOWLEquivalentClassesAxiom(class1, class2));
                    } else if (purposecloned.matches("([^\\s]+) disjointwith ([^\\s]+)")) {
                        Pattern p = Pattern.compile("([^\\s]+) disjointwith ([^\\s]+)", Pattern.CASE_INSENSITIVE);
                        Matcher m = p.matcher(purpose);
                        m.find();
                        String term1 = m.group(1).toString().replaceAll(" ", "");
                        String term2 = m.group(2).toString().replaceAll(" ", "");
                        OWLClass class1 = dataFactory.getOWLClass(IRI.create(term1));
                        OWLClass class2 = dataFactory.getOWLClass(IRI.create(term2));
                        manager.addAxiom(onto, dataFactory.getOWLDisjointClassesAxiom(class1, class2));
                    } else if (purposecloned.matches("([^\\s]+) subclassof ([^\\s]+) min (\\d+) ([^\\s]+) and ([^\\s]+) subclassof ([^\\s]+) some ([^\\s]+)")) {
                        Pattern p = Pattern.compile("([^\\s]+) subclassof ([^\\s]+) min (\\d+) ([^\\s]+) and ([^\\s]+) subclassof ([^\\s]+) some ([^\\s]+)", Pattern.CASE_INSENSITIVE);
                        Matcher m = p.matcher(purpose);
                        m.find();
                        String term1 = m.group(1).toString().replaceAll(" ", "");
                        String term2 = m.group(2).toString().replaceAll(" ", "");
                        String digit = m.group(3).toString().replaceAll(" ", "");
                        String term3 = m.group(4).toString().replaceAll(" ", "");
                        String term4 = m.group(5).toString().replaceAll(" ", "");
                        String term5 = m.group(6).toString().replaceAll(" ", "");
                        String term6 = m.group(8).toString().replaceAll(" ", "");
                        OWLClass class1 = dataFactory.getOWLClass(IRI.create(term1));
                        OWLObjectProperty prop1 = dataFactory.getOWLObjectProperty(IRI.create(term2));
                        OWLClass class2 = dataFactory.getOWLClass(IRI.create(term3));
                        OWLClass class3 = dataFactory.getOWLClass(IRI.create(term4));
                        OWLObjectProperty prop2 = dataFactory.getOWLObjectProperty(IRI.create(term5));
                        OWLClass class4 = dataFactory.getOWLClass(IRI.create(term6));
                        manager.addAxiom(onto, dataFactory.getOWLSubClassOfAxiom(class1,
                                dataFactory.getOWLObjectMinCardinality(Integer.parseInt(digit), prop1, class2)));
                        manager.addAxiom(onto, dataFactory.getOWLSubClassOfAxiom(class3,
                                dataFactory.getOWLObjectSomeValuesFrom(prop2, class4)));
                    } else if (purposecloned.matches("([^\\s]+) subclassof ([^\\s]+) (max|min|exactly) (\\d+) \\(([^\\s]+) and ([^\\s]+)\\)")) {
                        Pattern p = Pattern.compile("([^\\s]+) subclassof ([^\\s]+) (max|min|exactly) (\\d+) \\(([^\\s]+) and ([^\\s]+)\\)", Pattern.CASE_INSENSITIVE);
                        Matcher m = p.matcher(purpose);
                        m.find();
                        String term1 = m.group(1).toString().replaceAll(" ", "");
                        String term2 = m.group(2).toString().replaceAll(" ", "");
                        String type = m.group(3).toString().replaceAll(" ", "");
                        String digit = m.group(4).toString().replaceAll(" ", "");
                        String term3 = m.group(5).toString().replaceAll(" ", "");
                        String term4 = m.group(6).toString().replaceAll(" ", "");
                        OWLClass class1 = dataFactory.getOWLClass(IRI.create(term1));
                        OWLObjectProperty prop1 = dataFactory.getOWLObjectProperty(IRI.create(term2));
                        OWLClass class2 = dataFactory.getOWLClass(IRI.create(term3));
                        OWLClass class3 = dataFactory.getOWLClass(IRI.create(term4));
                        if (type.equals("min"))
                            manager.addAxiom(onto, dataFactory.getOWLSubClassOfAxiom(class1,
                                    dataFactory.getOWLObjectMinCardinality(Integer.parseInt(digit), prop1, dataFactory.getOWLObjectIntersectionOf(class2, class3))));
                        else if (type.equals("max"))
                            manager.addAxiom(onto, dataFactory.getOWLSubClassOfAxiom(class1,
                                    dataFactory.getOWLObjectMaxCardinality(Integer.parseInt(digit), prop1, dataFactory.getOWLObjectIntersectionOf(class2, class3))));
                        else
                            manager.addAxiom(onto, dataFactory.getOWLSubClassOfAxiom(class1,
                                    dataFactory.getOWLObjectExactCardinality(Integer.parseInt(digit), prop1, dataFactory.getOWLObjectIntersectionOf(class2, class3))));
                    } else if (purposecloned.matches("([^\\s]+) subclassof symmetricproperty\\(([^\\s]+)\\) (some|only) ([^\\s]+)")) {
                        Pattern p = Pattern.compile("([^\\s]+) subclassof symmetricproperty\\(([^\\s]+)\\) (some|only) ([^\\s]+)", Pattern.CASE_INSENSITIVE);
                        Matcher m = p.matcher(purpose);
                        m.find();
                        String term1 = m.group(1).toString().replaceAll(" ", "");
                        String term2 = m.group(2).toString().replaceAll(" ", "");
                        String term3 = m.group(4).toString().replaceAll(" ", "");
                    } else if (purposecloned.matches("([^\\s]+) subclassof ([^\\s]+) (max|min|exactly) (\\d+) ([^\\s]+)")) {
                        Pattern p = Pattern.compile("([^\\s]+) subclassof ([^\\s]+) (max|min|exactly) (\\d+) ([^\\s]+)", Pattern.CASE_INSENSITIVE);
                        Matcher m = p.matcher(purpose);
                        m.find();
                        String term1 = m.group(1).toString().replaceAll(" ", "");
                        String term2 = m.group(2).toString().replaceAll(" ", "");
                        String type = m.group(3).toString().replaceAll(" ", "");
                        String digit = m.group(4).toString().replaceAll(" ", "");
                        String term3 = m.group(5).toString().replaceAll(" ", "");
                        OWLClass class1 = dataFactory.getOWLClass(IRI.create(term1));
                        OWLObjectProperty prop1 = dataFactory.getOWLObjectProperty(IRI.create(term2));
                        OWLClass class2 = dataFactory.getOWLClass(IRI.create(term3));
                        if (type.equals("min"))
                            manager.addAxiom(onto, dataFactory.getOWLSubClassOfAxiom(class1,
                                    dataFactory.getOWLObjectMinCardinality(Integer.parseInt(digit), prop1, class2)));
                        else if (type.equals("max"))
                            manager.addAxiom(onto, dataFactory.getOWLSubClassOfAxiom(class1,
                                    dataFactory.getOWLObjectMaxCardinality(Integer.parseInt(digit), prop1, class2)));
                        else
                            manager.addAxiom(onto, dataFactory.getOWLSubClassOfAxiom(class1,
                                    dataFactory.getOWLObjectExactCardinality(Integer.parseInt(digit), prop1, class2)));
                    } else if (purposecloned.matches("([^\\s]+) type class")) {
                        Pattern p = Pattern.compile("([^\\s]+) type class", Pattern.CASE_INSENSITIVE);
                        Matcher m = p.matcher(purpose);
                        m.find();
                        String term1 = m.group(1).toString().replaceAll(" ", "");
                        OWLClass class1 = dataFactory.getOWLClass(IRI.create(term1));
                        manager.addAxiom(onto, dataFactory.getOWLDeclarationAxiom(class1));
                    } else if (purposecloned.matches("([^\\s]+) type ([^\\s]+)")) {
                        Pattern p = Pattern.compile("([^\\s]+) type ([^\\s]+)", Pattern.CASE_INSENSITIVE);
                        Matcher m = p.matcher(purpose);
                        m.find();
                        String term1 = m.group(1).toString().replaceAll(" ", "");
                        String term2 = m.group(2).toString().replaceAll(" ", "");
                        OWLIndividual ind1 = dataFactory.getOWLNamedIndividual(IRI.create(term1));
                        OWLClass class2 = dataFactory.getOWLClass(IRI.create(term2));
                        manager.addAxiom(onto, dataFactory.getOWLClassAssertionAxiom(class2, ind1));
                    } else if (purposecloned.matches("(([^\\s]+)) subclassof (([^\\s]+)) that (([^\\s]+)) some (([^\\s]+))")) {
                        Pattern p = Pattern.compile("(([^\\s]+)) subclassof (([^\\s]+)) that (([^\\s]+)) some (([^\\s]+))", Pattern.CASE_INSENSITIVE);
                        Matcher m = p.matcher(purpose);
                        m.find();
                        String term1 = m.group(1).toString().replaceAll(" ", "");
                        String term2 = m.group(2).toString().replaceAll(" ", "");
                        String term3 = m.group(3).toString().replaceAll(" ", "");
                        String term4 = m.group(4).toString().replaceAll(" ", "");
                        OWLClass class1 = dataFactory.getOWLClass(IRI.create(term1));
                        OWLClass class2 = dataFactory.getOWLClass(IRI.create(term2));
                        OWLObjectProperty prop1 = dataFactory.getOWLObjectProperty(IRI.create(term3));
                        OWLClass class3 = dataFactory.getOWLClass(IRI.create(term4));
                        manager.addAxiom(onto, dataFactory.getOWLSubClassOfAxiom(class1, class2));
                        manager.addAxiom(onto, dataFactory.getOWLSubClassOfAxiom(class1, dataFactory.getOWLObjectSomeValuesFrom(prop1, class3)));
                    } else if (purposecloned.matches("([^\\s]+) subclassof ([^\\s]+) (some|only) ([^\\s]+)")) {
                        Pattern p = Pattern.compile("([^\\s]+) subclassof ([^\\s]+) (some|only) ([^\\s]+)", Pattern.CASE_INSENSITIVE);
                        Matcher m = p.matcher(purpose);
                        m.find();
                        String term1 = m.group(1).toString().replaceAll(" ", "");
                        String term2 = m.group(2).toString().replaceAll(" ", "");
                        String type = m.group(3).toString().replaceAll(" ", "");
                        String term3 = m.group(4).toString().replaceAll(" ", "");
                        OWLClass class1 = dataFactory.getOWLClass(IRI.create(term1));
                        OWLObjectProperty prop = dataFactory.getOWLObjectProperty(IRI.create(term2));
                        OWLDataProperty propDP = dataFactory.getOWLDataProperty(IRI.create(term2));
                        OWLClass class2 = dataFactory.getOWLClass(IRI.create(term3));
                        if (type.equals("some") && !term3.equals("string") && !term3.equals("rational") && !term3.equals("integer"))
                            manager.addAxiom(onto, dataFactory.getOWLSubClassOfAxiom(class1,
                                    dataFactory.getOWLObjectSomeValuesFrom(prop, class2)));
                        else if (type.equals("some") && term3.equals("string")) {
                            manager.addAxiom(onto, dataFactory.getOWLSubClassOfAxiom(class1,
                                    dataFactory.getOWLDataSomeValuesFrom(propDP, OWL2Datatype.XSD_STRING.getDatatype(dataFactory))));
                        } else if (type.equals("some") && !term3.equals("integer")) {
                            manager.addAxiom(onto, dataFactory.getOWLSubClassOfAxiom(class1,
                                    dataFactory.getOWLDataSomeValuesFrom(propDP, OWL2Datatype.XSD_INT.getDatatype(dataFactory))));
                        } else if (type.equals("some") && !term3.equals("rational")) {
                            manager.addAxiom(onto, dataFactory.getOWLSubClassOfAxiom(class1,
                                    dataFactory.getOWLDataSomeValuesFrom(propDP, OWL2Datatype.XSD_STRING.getDatatype(dataFactory))));
                        } else if (type.equals("only") && !term3.equals("string") && !term3.equals("rational") && !term3.equals("integer"))
                            manager.addAxiom(onto, dataFactory.getOWLSubClassOfAxiom(class1,
                                    dataFactory.getOWLObjectAllValuesFrom(prop, class2)));
                        else if (type.equals("only") && term3.equals("string")) {
                            manager.addAxiom(onto, dataFactory.getOWLSubClassOfAxiom(class1,
                                    dataFactory.getOWLDataAllValuesFrom(propDP, OWL2Datatype.XSD_STRING.getDatatype(dataFactory))));
                        } else if (type.equals("only") && !term3.equals("integer")) {
                            manager.addAxiom(onto, dataFactory.getOWLSubClassOfAxiom(class1,
                                    dataFactory.getOWLDataAllValuesFrom(propDP, OWL2Datatype.XSD_INT.getDatatype(dataFactory))));
                        } else {
                            manager.addAxiom(onto, dataFactory.getOWLSubClassOfAxiom(class1,
                                    dataFactory.getOWLDataAllValuesFrom(propDP, OWL2Datatype.XSD_STRING.getDatatype(dataFactory))));
                        }

                    } else if (purposecloned.matches("([^\\s]+) subclassof ([^\\s]+) and ([^\\s]+)")) {
                        Pattern p = Pattern.compile("([^\\s]+) subclassof ([^\\s]+) and ([^\\s]+)", Pattern.CASE_INSENSITIVE);
                        Matcher m = p.matcher(purpose);
                        m.find();
                        String term1 = m.group(1).toString().replaceAll(" ", "");
                        String term2 = m.group(2).toString().replaceAll(" ", "");
                        String term3 = m.group(3).toString().replaceAll(" ", "");
                        OWLClass class1 = dataFactory.getOWLClass(IRI.create(term1));
                        OWLClass class2 = dataFactory.getOWLClass(IRI.create(term2));
                        OWLClass class3 = dataFactory.getOWLClass(IRI.create(term3));
                        manager.addAxiom(onto, dataFactory.getOWLSubClassOfAxiom(class1, class2));
                        manager.addAxiom(onto, dataFactory.getOWLSubClassOfAxiom(class1, class3));
                    } else if (purposecloned.matches("([^\\s]+) subclassof ([^\\s]+)")) {
                        Pattern p = Pattern.compile("([^\\s]+) subclassof ([^\\s]+)", Pattern.CASE_INSENSITIVE);
                        Matcher m = p.matcher(purpose);
                        m.find();
                        String term1 = m.group(1).toString().replaceAll(" ", "");
                        String term2 = m.group(2).toString().replaceAll(" ", "");
                        OWLClass class1 = dataFactory.getOWLClass(IRI.create(term1));
                        OWLClass class2 = dataFactory.getOWLClass(IRI.create(term2));
                        manager.addAxiom(onto, dataFactory.getOWLSubClassOfAxiom(class1, class2));
                    } else {
                        System.out.println("not match found " + purposecloned);
                    }

                }
            }
        }
        ontology.setOntology(onto);
        ontology.setProv(IRI.create(key));
        return ontology;

    }


    public static ArrayList<TestCaseDesign> loadTest(String file) throws Exception {

        ArrayList<TestCaseDesign> testsuiteDesign = new ArrayList<>();
        testsuiteDesign.addAll(Design.processTestCaseDesign(file));
        return testsuiteDesign;
    }

    public static void storeFile(String file, HashMap<String, IRI> got) throws IOException {
        BufferedWriter bw = new BufferedWriter(new FileWriter(file));
        bw.write("Term;Key");
        bw.newLine();
        for(String p:got.keySet())
        {
            bw.write(p + ";" + got.get(p));
            bw.newLine();
        }
        bw.flush();
        bw.close();
    }

    public static String mapImplementationTerms(String  query, HashMap<String, IRI> allvalues) {
        Pattern p = Pattern.compile("\\<(.*?)\\>");
        Matcher m = p.matcher(query);
        String querym = query;
        while (m.find()) {
            try {
                int flag = 0;
                for (Map.Entry<String, IRI> entry : allvalues.entrySet()) {
                    if (entry.getKey().toLowerCase().equals(m.group().toLowerCase().replace("<", "").replace(">", ""))) {
                        querym = querym.replace("<" + entry.getKey() + ">", "<" + entry.getValue() + ">");
                        flag++;
                    }
                }
                querym = querym.replace("<string>", "<http://www.w3.org/2001/XMLSchema#string>");

            } catch (Exception e) {
                System.out.println("ERROR WHILE PARSING IMPLEMENTATION TERMS: "+ e.getMessage());
            }
        }
        return querym;

    }

    public static Set<OWLAxiom> mapImplementationTerms(OWLOntology queries, HashMap<String, IRI> allvalues) {
        OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
        OWLEntityRenamer renamer = new OWLEntityRenamer(manager, Collections.singleton(queries));

        for(OWLAxiom axiom: queries.getAxioms()) {

            Pattern p = Pattern.compile("\\<(.*?)\\>");
            String query = axiom.toString();
            Matcher m = p.matcher(query);
            while (m.find()) {
                try {
                    int flag = 0;
                    for (Map.Entry<String, IRI> entry : allvalues.entrySet()) {
                        if (entry.getKey().equals(m.group().replace("<", "").replace(">", ""))) {
                            queries.getOWLOntologyManager().applyChanges(renamer.changeIRI(IRI.create(entry.getKey()), entry.getValue()));
                            flag++;
                        }

                    }
                    queries.getOWLOntologyManager().applyChanges(renamer.changeIRI(IRI.create("string"), IRI.create("http://www.w3.org/2001/XMLSchema#string")));

                } catch (Exception e) {
                    System.out.println("ERROR WHILE PARSING IMPLEMENTATION TERMS: "+ e.getMessage());
                }
            }
        }
        return queries.getAxioms();
    }
}
