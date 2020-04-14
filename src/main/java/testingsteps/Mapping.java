package testingsteps;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.util.OWLEntityRenamer;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/*
 * Mapping of each term in the test on each test in the ontology
 * */
public class Mapping {

    public  static ArrayList<String> processTestExpressionToExtractTerms(String purpose){
        ArrayList<String> terms = new ArrayList();
        String purposecloned= purpose.toLowerCase();
        if(purposecloned.matches("([^\\s]+) subclassof ([^\\s]+) only  ([^\\s]+) or ([^\\s]+)")){
            Pattern p = Pattern.compile("([^\\s]+) subclassof ([^\\s]+)(some|only) \\(([^\\s]+) or ([^\\s]+)\\)",Pattern.CASE_INSENSITIVE);
            Matcher m = p.matcher(purpose);
            m.find();
            String term1 = m.group(1).toString().replaceAll(" ","");
            String term2 = m.group(2).toString().replaceAll(" ","");
            String term3 = m.group(4).toString().replaceAll(" ","");
            String term4 = m.group(5).toString().replaceAll(" ","");
            terms.add(term1);
            terms.add(term2);
            terms.add(term3);
            terms.add(term4);
        }else if(purposecloned.matches("([^\\s]+) subclassof ([^\\s]+) (some|only) \\(([^\\s]+) and ([^\\s]+)\\)")){
            Pattern p = Pattern.compile("([^\\s]+) subclassof ([^\\s]+) (some|only) \\(([^\\s]+) and ([^\\s]+)\\)",Pattern.CASE_INSENSITIVE);
            Matcher m = p.matcher(purpose);
            m.find();
            String term1 = m.group(1).toString().replaceAll(" ","");
            String term2 = m.group(2).toString().replaceAll(" ","");
            String term3 = m.group(4).toString().replaceAll(" ","");
            String term4 = m.group(5).toString().replaceAll(" ","");
            terms.add(term1);
            terms.add(term2);
            terms.add(term3);
            terms.add(term4);
        }else if(purposecloned.matches("([^\\s]+) subclassof ([^\\s]+) value ([^\\s]+)")){
            Pattern p = Pattern.compile("([^\\s]+) subclassof ([^\\s]+) value ([^\\s]+)",Pattern.CASE_INSENSITIVE);
            Matcher m = p.matcher(purpose);
            m.find();
            String term1 = m.group(1).toString().replaceAll(" ","");
            String term2 = m.group(2).toString().replaceAll(" ","");
            String term3 = m.group(3).toString().replaceAll(" ","");
            terms.add(term1);
            terms.add(term2);
            terms.add(term3);
        }else if(purposecloned.matches("([^\\s]+) subclassof ([^\\s]+)\\s*and\\s*([^\\s]+) subclassof ([^\\s]+) that disjointwith ([^\\s]+)")){
            Pattern p = Pattern.compile("(([^\\s]+)) subclassof ([^\\s])+\\s*and\\s*(([^\\s]+)) subclassof (([^\\s]+)) that disjointwith (([^\\s]+))",Pattern.CASE_INSENSITIVE);
            Matcher m = p.matcher(purpose);
            m.find();
            String term1 = m.group(1).toString().replaceAll(" ","");
            String term2 = m.group(2).toString().replaceAll(" ","");
            String term3 = m.group(3).toString().replaceAll(" ","");
            String term4 = m.group(4).toString().replaceAll(" ","");
            String term5 = m.group(5).toString().replaceAll(" ","");
            if(term1.equals(term5) && term2.equals(term4) ){
                terms.add(term1);
                terms.add(term2);
                terms.add(term3);
            }
        }else if(purposecloned.matches("([^\\s]+) equivalentto ([^\\s]+)")){
            Pattern p = Pattern.compile("([^\\s]+) equivalentto ([^\\s]+)",Pattern.CASE_INSENSITIVE);
            Matcher m = p.matcher(purpose);
            m.find();
            String term1 = m.group(1).toString().replaceAll(" ","");
            String term2 = m.group(2).toString().replaceAll(" ","");
            terms.add(term1);
            terms.add(term2);
        }else if(purposecloned.matches("([^\\s]+) disjointwith ([^\\s]+)")){
            Pattern p = Pattern.compile("([^\\s]+) disjointwith ([^\\s]+)",Pattern.CASE_INSENSITIVE);
            Matcher m = p.matcher(purpose);
            m.find();
            String term1 = m.group(1).toString().replaceAll(" ","");
            String term2 = m.group(2).toString().replaceAll(" ","");
            terms.add(term1);
            terms.add(term2);
        }else if(purposecloned.matches("([^\\s]+) subclassof ([^\\s]+) min (\\d+) ([^\\s]+) and ([^\\s]+) subclassof ([^\\s]+) some ([^\\s]+)")){
            Pattern p = Pattern.compile("([^\\s]+) subclassof ([^\\s]+) min (\\d+) ([^\\s]+) and ([^\\s]+) subclassof ([^\\s]+) some ([^\\s]+)",Pattern.CASE_INSENSITIVE);
            Matcher m = p.matcher(purpose);
            m.find();
            String term1 = m.group(1).toString().replaceAll(" ","");
            String term2 = m.group(2).toString().replaceAll(" ","");
            String term3 = m.group(4).toString().replaceAll(" ","");
            String term4 = m.group(5).toString().replaceAll(" ","");
            String term5 = m.group(6).toString().replaceAll(" ","");
            String term6 = m.group(8).toString().replaceAll(" ","");
            terms.add(term1);
            terms.add(term2);
            terms.add(term3);
            terms.add(term4);
            terms.add(term5);
            terms.add(term6);
        }else if(purposecloned.matches("([^\\s]+) subclassof ([^\\s]+) (max|min|exactly) (\\d+) \\(([^\\s]+) and ([^\\s]+)\\)")){
            Pattern p = Pattern.compile("([^\\s]+) subclassof ([^\\s]+) (max|min|exactly) (\\d+) \\(([^\\s]+) and ([^\\s]+)\\)",Pattern.CASE_INSENSITIVE);
            Matcher m = p.matcher(purpose);
            m.find();
            String term1 = m.group(1).toString().replaceAll(" ","");
            String term2 = m.group(2).toString().replaceAll(" ","");
            String term3 = m.group(5).toString().replaceAll(" ","");
            String term4 = m.group(6).toString().replaceAll(" ","");
            terms.add(term1);
            terms.add(term2);
            terms.add(term3);
            terms.add(term4);
        }else if(purposecloned.matches("([^\\s]+) subclassof symmetricproperty\\(([^\\s]+)\\) (some|only) ([^\\s]+)")){
            Pattern p = Pattern.compile("([^\\s]+) subclassof symmetricproperty\\(([^\\s]+)\\) (some|only) ([^\\s]+)",Pattern.CASE_INSENSITIVE);
            Matcher m = p.matcher(purpose);
            m.find();
            String term1 = m.group(1).toString().replaceAll(" ","");
            String term2 = m.group(2).toString().replaceAll(" ","");
            String term3 = m.group(4).toString().replaceAll(" ","");
            terms.add(term1);
            terms.add(term2);
            terms.add(term3);
        }else if(purposecloned.matches("([^\\s]+) subclassof ([^\\s]+) (max|min|exactly) (\\d+) ([^\\s]+)")){
            Pattern p = Pattern.compile("([^\\s]+) subclassof ([^\\s]+) (max|min|exactly) (\\d+) ([^\\s]+)",Pattern.CASE_INSENSITIVE);
            Matcher m = p.matcher(purpose);
            m.find();
            String term1 = m.group(1).toString().replaceAll(" ","");
            String term2 = m.group(2).toString().replaceAll(" ","");
            String term3 = m.group(5).toString().replaceAll(" ","");
            terms.add(term1);
            terms.add(term2);
            terms.add(term3);
        }else if(purposecloned.matches("([^\\s]+) type class")) {
            Pattern p = Pattern.compile("([^\\s]+) type class",Pattern.CASE_INSENSITIVE);
            Matcher m = p.matcher(purpose);
            m.find();
            String term1 = m.group(1).toString().replaceAll(" ","");
            terms.add(term1);
        }else if(purposecloned.matches("([^\\s]+) type ([^\\s]+)")){
            Pattern p = Pattern.compile("([^\\s]+) type ([^\\s]+)",Pattern.CASE_INSENSITIVE);
            Matcher m = p.matcher(purpose);
            m.find();
            String term1 = m.group(1).toString().replaceAll(" ","");
            String term2 = m.group(2).toString().replaceAll(" ","");
            terms.add(term1);
            terms.add(term2);
        }else if(purposecloned.matches("(([^\\s]+)) subclassof (([^\\s]+)) that (([^\\s]+)) some (([^\\s]+))")){
            Pattern p = Pattern.compile("(([^\\s]+)) subclassof (([^\\s]+)) that (([^\\s]+)) some (([^\\s]+))",Pattern.CASE_INSENSITIVE);
            Matcher m = p.matcher(purpose);
            m.find();
            String term1 = m.group(1).toString().replaceAll(" ","");
            String term2 = m.group(2).toString().replaceAll(" ","");
            String term3 = m.group(3).toString().replaceAll(" ","");
            String term4 = m.group(4).toString().replaceAll(" ","");
            terms.add(term1.replace(" ",""));
            terms.add(term2.replace(" ",""));
            terms.add(term3.replace(" ",""));
            terms.add(term4.replace(" ",""));
        }else if(purposecloned.matches("([^\\s]+) subclassof ([^\\s]+) (some|only) ([^\\s]+)")){
            Pattern p = Pattern.compile("([^\\s]+) subclassof ([^\\s]+) (some|only) ([^\\s]+)",Pattern.CASE_INSENSITIVE);
            Matcher m = p.matcher(purpose);
            m.find();
            String term1 = m.group(1).toString().replaceAll(" ","");
            String term2 = m.group(2).toString().replaceAll(" ","");
            String term3 = m.group(4).toString().replaceAll(" ","");
            terms.add(term1);
            terms.add(term2);
            terms.add(term3);
        }else if(purposecloned.matches("([^\\s]+) subclassof ([^\\s]+) and ([^\\s]+)")){
            Pattern p = Pattern.compile("([^\\s]+) subclassof ([^\\s]+) and ([^\\s]+)",Pattern.CASE_INSENSITIVE);
            Matcher m = p.matcher(purpose);
            m.find();
            String term1 = m.group(1).toString().replaceAll(" ","");
            String term2 = m.group(2).toString().replaceAll(" ","");
            String term3 = m.group(3).toString().replaceAll(" ","");
            terms.add(term1);
            terms.add(term2);
            terms.add(term3);
        }else if(purposecloned.matches("([^\\s]+) subclassof ([^\\s]+)")){
            Pattern p = Pattern.compile("([^\\s]+) subclassof ([^\\s]+)",Pattern.CASE_INSENSITIVE);
            Matcher m = p.matcher(purpose);
            m.find();
            String term1 = m.group(1).toString().replaceAll(" ","");
            String term2 = m.group(2).toString().replaceAll(" ","");
            terms.add(term1);
            terms.add(term2);
        }
        else{
            System.out.println("not match found "+ purposecloned);
        }
        return terms;
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
