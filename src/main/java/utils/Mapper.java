package utils;

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
 * Mapper of each term in the test on each term in the ontology
 * */
public class Mapper {

    public static String mapTermsInTestImplementation(String  query, HashMap<String, IRI> allvalues) {
        Pattern p = Pattern.compile("\\<(.*?)\\>");
        Matcher m = p.matcher(query);
        String querym = query;
        while (m.find()) {
            try {
                for (Map.Entry<String, IRI> entry : allvalues.entrySet()) {
                    if (entry.getKey().toLowerCase().equals(m.group().toLowerCase().replace("<", "").replace(">", ""))) {
                        querym = querym.replace("<" + entry.getKey() + ">", "<" + entry.getValue() + ">");
                    }
                }
                querym = querym.replace("<string>", "<http://www.w3.org/2001/XMLSchema#string>");

            } catch (Exception e) {
                System.out.println("ERROR WHILE PARSING IMPLEMENTATION TERMS: "+ e.getMessage());
            }
        }
        return querym;

    }

    public static Set<OWLAxiom> mapTermsInTestImplementation(OWLOntology queries, HashMap<String, IRI> allvalues) {
        OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
        OWLEntityRenamer reNamer = new OWLEntityRenamer(manager, Collections.singleton(queries));

        for(OWLAxiom axiom: queries.getAxioms()) {
            Pattern p = Pattern.compile("\\<(.*?)\\>");
            String query = axiom.toString();
            Matcher m = p.matcher(query);
            while (m.find()) {
                try {
                    for (Map.Entry<String, IRI> entry : allvalues.entrySet()) {
                        if (entry.getKey().equals(m.group().replace("<", "").replace(">", ""))) {
                            queries.getOWLOntologyManager().applyChanges(reNamer.changeIRI(IRI.create(entry.getKey()), entry.getValue()));
                        }
                    }
                    queries.getOWLOntologyManager().applyChanges(reNamer.changeIRI(IRI.create("string"), IRI.create("http://www.w3.org/2001/XMLSchema#string")));
                } catch (Exception e) {
                    System.out.println("ERROR WHILE PARSING IMPLEMENTATION TERMS: "+ e.getMessage());
                }
            }
        }
        return queries.getAxioms();
    }
}
