package ontologies;

import org.coode.owlapi.turtle.TurtleOntologyFormat;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.vocab.OWL2Datatype;
import tests.TestCaseDesign;
import java.io.File;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/*Class for generating an ontology "skeleton" based on the tests associated to a data model without ontology associated */
public class SkeletonGenerator {

    static final String STRING = "string";
    static final String RATIONAL = "rational";
    static final String INTEGER = "integer";
    static final String SOME = "some";
    static final String ONLY ="only";

    private SkeletonGenerator() {
    }

    public static Ontology createSkeletonFromTestCases(String key, List<TestCaseDesign> testCaseDesigns) throws OWLOntologyCreationException, OWLOntologyStorageException {
        Ontology ontology = new Ontology();
        OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
        OWLOntology onto = manager.createOntology() ;
        ontology.setManager(manager);
        OWLDataFactory dataFactory = manager.getOWLDataFactory();
        for(TestCaseDesign testCaseDesign: testCaseDesigns) {
            if (testCaseDesign.getSource().contains(key)) {
                for (String purpose : testCaseDesign.getPurpose()) {
                    String purposeCloned = purpose.toLowerCase();
                    //Create axioms based on the test expressions
                    if (purposeCloned.matches("([^\\s]+) subclassof ([^\\s]+) only ([^\\s]+) or ([^\\s]+)")) {
                        Pattern p = Pattern.compile("([^\\s]+) subclassof ([^\\s]+)(some|only) \\(([^\\s]+) or ([^\\s]+)\\)", Pattern.CASE_INSENSITIVE);
                        Matcher m = p.matcher(purpose);
                        m.find();
                        String term1 = m.group(1).replaceAll("\\s", "");
                        String term2 = m.group(2).replaceAll("\\s", "");
                        String term3 = m.group(4).replaceAll("\\s", "");
                        String term4 = m.group(5).replaceAll("\\s", "");
                        OWLClass class1 = dataFactory.getOWLClass(IRI.create(term1));
                        OWLObjectProperty prop = dataFactory.getOWLObjectProperty(IRI.create(term2));
                        OWLClass class2 = dataFactory.getOWLClass(IRI.create(term3));
                        OWLClass class3 = dataFactory.getOWLClass(IRI.create(term4));
                        manager.addAxiom(onto, dataFactory.getOWLSubClassOfAxiom(class1, dataFactory.getOWLObjectAllValuesFrom(prop, dataFactory.getOWLObjectUnionOf(class2, class3))));
                    } else if (purposeCloned.matches("([^\\s]+) subclassof ([^\\s]+) (some|only) \\(([^\\s]+) and ([^\\s]+)\\)")) {
                        Pattern p = Pattern.compile("([^\\s]+) subclassof ([^\\s]+) (some|only) \\(([^\\s]+) and ([^\\s]+)\\)", Pattern.CASE_INSENSITIVE);
                        Matcher m = p.matcher(purpose);
                        m.find();
                        String term1 = m.group(1).replaceAll("\\s", "");
                        String term2 = m.group(2).replaceAll("\\s", "");
                        String term3 = m.group(4).replaceAll("\\s", "");
                        String term4 = m.group(5).replaceAll("\\s", "");
                        OWLClass class1 = dataFactory.getOWLClass(IRI.create(term1));
                        OWLObjectProperty prop = dataFactory.getOWLObjectProperty(IRI.create(term2));
                        OWLClass class2 = dataFactory.getOWLClass(IRI.create(term3));
                        OWLClass class3 = dataFactory.getOWLClass(IRI.create(term4));
                        manager.addAxiom(onto, dataFactory.getOWLSubClassOfAxiom(class1, dataFactory.getOWLObjectAllValuesFrom(prop, dataFactory.getOWLObjectIntersectionOf(class2, class3))));
                    } else if (purposeCloned.matches("([^\\s]+) subclassof ([^\\s]+) value ([^\\s]+)")) {
                        Pattern p = Pattern.compile("([^\\s]+) subclassof ([^\\s]+) value ([^\\s]+)", Pattern.CASE_INSENSITIVE);
                        Matcher m = p.matcher(purpose);
                        m.find();
                        String term1 = m.group(1).replaceAll("\\s", "");
                        String term2 = m.group(2).replaceAll("\\s", "");
                        String term3 = m.group(3).replaceAll("\\s", "");
                        OWLClass class1 = dataFactory.getOWLClass(IRI.create(term1));
                        OWLObjectProperty prop = dataFactory.getOWLObjectProperty(IRI.create(term2));
                        OWLIndividual ind1 = dataFactory.getOWLNamedIndividual(IRI.create(term3));
                        manager.addAxiom(onto, dataFactory.getOWLSubClassOfAxiom(class1, dataFactory.getOWLObjectHasValue(prop, ind1)));
                    } else if (purposeCloned.matches("([^\\s]+) subclassof ([^\\s]+)\\s*and\\s*([^\\s]+) subclassof ([^\\s]+) that disjointwith ([^\\s]+)")) {
                        Pattern p = Pattern.compile("(([^\\s]+)) subclassof ([^\\s])+\\s*and\\s*(([^\\s]+)) subclassof (([^\\s]+)) that disjointwith (([^\\s]+))", Pattern.CASE_INSENSITIVE);
                        Matcher m = p.matcher(purpose);
                        m.find();
                        String term1 = m.group(1).replaceAll("\\s", "");
                        String term2 = m.group(2).replaceAll("\\s", "");
                        String term3 = m.group(3).replaceAll("\\s", "");
                        OWLClass class1 = dataFactory.getOWLClass(IRI.create(term1));
                        OWLClass class2 = dataFactory.getOWLClass(IRI.create(term2));
                        OWLClass class3 = dataFactory.getOWLClass(IRI.create(term3));
                        manager.addAxiom(onto, dataFactory.getOWLSubClassOfAxiom(class1, class2));
                        manager.addAxiom(onto, dataFactory.getOWLSubClassOfAxiom(class3, class2));
                        manager.addAxiom(onto, dataFactory.getOWLDisjointClassesAxiom(class3, class1));
                    } else if (purposeCloned.matches("([^\\s]+) equivalentto ([^\\s]+)")) {
                        Pattern p = Pattern.compile("([^\\s]+) equivalentto ([^\\s]+)", Pattern.CASE_INSENSITIVE);
                        Matcher m = p.matcher(purpose);
                        m.find();
                        String term1 = m.group(1).replaceAll("\\s", "");
                        String term2 = m.group(2).replaceAll("\\s", "");
                        OWLClass class1 = dataFactory.getOWLClass(IRI.create(term1));
                        OWLClass class2 = dataFactory.getOWLClass(IRI.create(term2));
                        manager.addAxiom(onto, dataFactory.getOWLEquivalentClassesAxiom(class1, class2));
                    } else if (purposeCloned.matches("([^\\s]+) disjointwith ([^\\s]+)")) {
                        Pattern p = Pattern.compile("([^\\s]+) disjointwith ([^\\s]+)", Pattern.CASE_INSENSITIVE);
                        Matcher m = p.matcher(purpose);
                        m.find();
                        String term1 = m.group(1).replaceAll("\\s", "");
                        String term2 = m.group(2).replaceAll("\\s", "");
                        OWLClass class1 = dataFactory.getOWLClass(IRI.create(term1));
                        OWLClass class2 = dataFactory.getOWLClass(IRI.create(term2));
                        manager.addAxiom(onto, dataFactory.getOWLDisjointClassesAxiom(class1, class2));
                    } else if (purposeCloned.matches("([^\\s]+) subclassof ([^\\s]+) min (\\d+) ([^\\s]+) and ([^\\s]+) subclassof ([^\\s]+) some ([^\\s]+)")) {
                        Pattern p = Pattern.compile("([^\\s]+) subclassof ([^\\s]+) min (\\d+) ([^\\s]+) and ([^\\s]+) subclassof ([^\\s]+) some ([^\\s]+)", Pattern.CASE_INSENSITIVE);
                        Matcher m = p.matcher(purpose);
                        m.find();
                        String term1 = m.group(1).replaceAll("\\s", "");
                        String term2 = m.group(2).replaceAll("\\s", "");
                        String digit = m.group(3).replaceAll("\\s", "");
                        String term3 = m.group(4).replaceAll("\\s", "");
                        String term4 = m.group(5).replaceAll("\\s", "");
                        String term5 = m.group(6).replaceAll("\\s", "");
                        String term6 = m.group(8).replaceAll("\\s", "");
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
                    } else if (purposeCloned.matches("([^\\s]+) subclassof ([^\\s]+) (max|min|exactly) (\\d+) \\(([^\\s]+) and ([^\\s]+)\\)")) {
                        Pattern p = Pattern.compile("([^\\s]+) subclassof ([^\\s]+) (max|min|exactly) (\\d+) \\(([^\\s]+) and ([^\\s]+)\\)", Pattern.CASE_INSENSITIVE);
                        Matcher m = p.matcher(purpose);
                        m.find();
                        String term1 = m.group(1).replaceAll("\\s", "");
                        String term2 = m.group(2).replaceAll("\\s", "");
                        String type = m.group(3).replaceAll("\\s", "");
                        String digit = m.group(4).replaceAll("\\s", "");
                        String term3 = m.group(5).replaceAll("\\s", "");
                        String term4 = m.group(6).replaceAll("\\s", "");
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
                    } else if (purposeCloned.matches("([^\\s]+) subclassof ([^\\s]+) (max|min|exactly) (\\d+) ([^\\s]+)")) {
                        Pattern p = Pattern.compile("([^\\s]+) subclassof ([^\\s]+) (max|min|exactly) (\\d+) ([^\\s]+)", Pattern.CASE_INSENSITIVE);
                        Matcher m = p.matcher(purpose);
                        m.find();
                        String term1 = m.group(1).replaceAll("\\s", "");
                        String term2 = m.group(2).replaceAll("\\s", "");
                        String type = m.group(3).replaceAll("\\s", "");
                        String digit = m.group(4).replaceAll("\\s", "");
                        String term3 = m.group(5).replaceAll("\\s", "");
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
                    } else if (purposeCloned.matches("([^\\s]+) type class")) {
                        Pattern p = Pattern.compile("([^\\s]+) type class", Pattern.CASE_INSENSITIVE);
                        Matcher m = p.matcher(purpose);
                        m.find();
                        String term1 = m.group(1).replaceAll("\\s", "");
                        OWLClass class1 = dataFactory.getOWLClass(IRI.create(term1));
                        manager.addAxiom(onto, dataFactory.getOWLDeclarationAxiom(class1));
                    } else if (purposeCloned.matches("([^\\s]+) type ([^\\s]+)")) {
                        Pattern p = Pattern.compile("([^\\s]+) type ([^\\s]+)", Pattern.CASE_INSENSITIVE);
                        Matcher m = p.matcher(purpose);
                        m.find();
                        String term1 = m.group(1).replaceAll("\\s", "");
                        String term2 = m.group(2).replaceAll("\\s", "");
                        OWLIndividual ind1 = dataFactory.getOWLNamedIndividual(IRI.create(term1));
                        OWLClass class2 = dataFactory.getOWLClass(IRI.create(term2));
                        manager.addAxiom(onto, dataFactory.getOWLClassAssertionAxiom(class2, ind1));
                    } else if (purposeCloned.matches("(([^\\s]+)) subclassof (([^\\s]+)) that (([^\\s]+)) some (([^\\s]+))")) {
                        Pattern p = Pattern.compile("(([^\\s]+)) subclassof (([^\\s]+)) that (([^\\s]+)) some (([^\\s]+))", Pattern.CASE_INSENSITIVE);
                        Matcher m = p.matcher(purpose);
                        m.find();
                        String term1 = m.group(1).replaceAll("\\s", "");
                        String term2 = m.group(2).replaceAll("\\s", "");
                        String term3 = m.group(3).replaceAll("\\s", "");
                        String term4 = m.group(4).replaceAll("\\s", "");
                        OWLClass class1 = dataFactory.getOWLClass(IRI.create(term1));
                        OWLClass class2 = dataFactory.getOWLClass(IRI.create(term2));
                        OWLObjectProperty prop1 = dataFactory.getOWLObjectProperty(IRI.create(term3));
                        OWLClass class3 = dataFactory.getOWLClass(IRI.create(term4));
                        manager.addAxiom(onto, dataFactory.getOWLSubClassOfAxiom(class1, class2));
                        manager.addAxiom(onto, dataFactory.getOWLSubClassOfAxiom(class1, dataFactory.getOWLObjectSomeValuesFrom(prop1, class3)));
                    } else if (purposeCloned.matches("([^\\s]+) subclassof ([^\\s]+) (some|only) ([^\\s]+)")) {
                        Pattern p = Pattern.compile("([^\\s]+) subclassof ([^\\s]+) (some|only) ([^\\s]+)", Pattern.CASE_INSENSITIVE);
                        Matcher m = p.matcher(purpose);
                        m.find();
                        String term1 = m.group(1).replaceAll("\\s", "");
                        String term2 = m.group(2).replaceAll("\\s", "");
                        String type = m.group(3).replaceAll("\\s", "");
                        String term3 = m.group(4).replaceAll("\\s", "");
                        OWLClass class1 = dataFactory.getOWLClass(IRI.create(term1));
                        OWLObjectProperty prop = dataFactory.getOWLObjectProperty(IRI.create(term2));
                        OWLDataProperty propDP = dataFactory.getOWLDataProperty(IRI.create(term2));
                        OWLClass class2 = dataFactory.getOWLClass(IRI.create(term3));
                        if (type.equals(SOME) && !term3.equals(STRING) && !term3.equals(RATIONAL) && !term3.equals(INTEGER))
                            manager.addAxiom(onto, dataFactory.getOWLSubClassOfAxiom(class1,
                                    dataFactory.getOWLObjectSomeValuesFrom(prop, class2)));
                        else if (type.equals(SOME) && term3.equals(STRING)) {
                            manager.addAxiom(onto, dataFactory.getOWLSubClassOfAxiom(class1,
                                    dataFactory.getOWLDataSomeValuesFrom(propDP, OWL2Datatype.XSD_STRING.getDatatype(dataFactory))));
                        } else if (type.equals(SOME) && !term3.equals(INTEGER)) {
                            manager.addAxiom(onto, dataFactory.getOWLSubClassOfAxiom(class1,
                                    dataFactory.getOWLDataSomeValuesFrom(propDP, OWL2Datatype.XSD_INT.getDatatype(dataFactory))));
                        } else if (type.equals(SOME) && !term3.equals(RATIONAL)) {
                            manager.addAxiom(onto, dataFactory.getOWLSubClassOfAxiom(class1,
                                    dataFactory.getOWLDataSomeValuesFrom(propDP, OWL2Datatype.XSD_STRING.getDatatype(dataFactory))));
                        } else if (type.equals(ONLY) && !term3.equals(STRING) && !term3.equals(RATIONAL) && !term3.equals(INTEGER))
                            manager.addAxiom(onto, dataFactory.getOWLSubClassOfAxiom(class1,
                                    dataFactory.getOWLObjectAllValuesFrom(prop, class2)));
                        else if (type.equals(ONLY) && term3.equals(STRING)) {
                            manager.addAxiom(onto, dataFactory.getOWLSubClassOfAxiom(class1,
                                    dataFactory.getOWLDataAllValuesFrom(propDP, OWL2Datatype.XSD_STRING.getDatatype(dataFactory))));
                        } else if (type.equals(ONLY) && !term3.equals(INTEGER)) {
                            manager.addAxiom(onto, dataFactory.getOWLSubClassOfAxiom(class1,
                                    dataFactory.getOWLDataAllValuesFrom(propDP, OWL2Datatype.XSD_INT.getDatatype(dataFactory))));
                        } else {
                            manager.addAxiom(onto, dataFactory.getOWLSubClassOfAxiom(class1,
                                    dataFactory.getOWLDataAllValuesFrom(propDP, OWL2Datatype.XSD_STRING.getDatatype(dataFactory))));
                        }

                    } else if (purposeCloned.matches("([^\\s]+) subclassof ([^\\s]+) and ([^\\s]+)")) {
                        Pattern p = Pattern.compile("([^\\s]+) subclassof ([^\\s]+) and ([^\\s]+)", Pattern.CASE_INSENSITIVE);
                        Matcher m = p.matcher(purpose);
                        m.find();
                        String term1 = m.group(1).replaceAll("\\s", "");
                        String term2 = m.group(2).replaceAll("\\s", "");
                        String term3 = m.group(3).replaceAll("\\s", "");
                        OWLClass class1 = dataFactory.getOWLClass(IRI.create(term1));
                        OWLClass class2 = dataFactory.getOWLClass(IRI.create(term2));
                        OWLClass class3 = dataFactory.getOWLClass(IRI.create(term3));
                        manager.addAxiom(onto, dataFactory.getOWLSubClassOfAxiom(class1, class2));
                        manager.addAxiom(onto, dataFactory.getOWLSubClassOfAxiom(class1, class3));
                    } else if (purposeCloned.matches("([^\\s]+) subclassof ([^\\s]+)")) {
                        Pattern p = Pattern.compile("([^\\s]+) subclassof ([^\\s]+)", Pattern.CASE_INSENSITIVE);
                        Matcher m = p.matcher(purpose);
                        m.find();
                        String term1 = m.group(1).replaceAll("\\s", "");
                        String term2 = m.group(2).replaceAll("\\s", "");
                        OWLClass class1 = dataFactory.getOWLClass(IRI.create(term1));
                        OWLClass class2 = dataFactory.getOWLClass(IRI.create(term2));
                        manager.addAxiom(onto, dataFactory.getOWLSubClassOfAxiom(class1, class2));
                    } else {
                        System.out.println("not match found " + purposeCloned);
                    }

                }
            }
        }
        // Gather all axioms and store them as an ontology
        ontology.setOwlOntology(onto);
        ontology.setProv(IRI.create(key));

        //Create a file for the new format
        File fileFormated = new File("ontology.ttl");

        //Save the ontology in a TTL file
        OWLOntologyFormat format = manager.getOntologyFormat(ontology.getOwlOntology());
        TurtleOntologyFormat ttlFormat = new TurtleOntologyFormat();
        if (format.isPrefixOWLOntologyFormat()) {
            ttlFormat.copyPrefixesFrom(format.asPrefixOWLOntologyFormat());
        }
        manager.saveOntology(ontology.getOwlOntology(), ttlFormat, IRI.create(fileFormated.toURI()));
        return ontology;

    }

}
