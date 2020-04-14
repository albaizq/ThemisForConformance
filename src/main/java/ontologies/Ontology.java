package ontologies;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.*;
import tests.TestCaseDesign;

import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import static ontologies.SkeletonGenerator.createSkeletonFromTestCases;

public class Ontology {
    public OWLOntology ontology;
    public OWLOntologyManager manager;
    public IRI prov;

    public OWLOntology getOntology() {
        return ontology;
    }

    public void setOntology(OWLOntology ontology) {
        this.ontology = ontology;
    }


    public OWLOntologyManager getManager() {
        return manager;
    }

    public void setManager(OWLOntologyManager manager) {
        this.manager = manager;
    }

    public String load_ontologyURL(String ontologyURL) {
        String response = " ";
        this.manager = OWLManager.createOWLOntologyManager();
        IRI path = IRI.create(ontologyURL);
        try {
            OWLOntology ontologyLoaded = this.manager.loadOntology(path);
            // remove all imports
            for(OWLOntology imported: manager.getDirectImports(ontologyLoaded)) {
                manager.removeOntology(imported);
            }
            this.setOntology(ontologyLoaded);
        } catch (Exception e) {
            System.err.println("could not load vocabulary." + e.getMessage());
            response = null;
            System.exit(1);
        }
        this.setProv(path);
        return response;

    }

    public IRI getProv() {
        return prov;
    }

    public void setProv(IRI prov) {
        this.prov = prov;
    }

    public HashMap<String, IRI> getClasses(){
        HashMap<String, IRI> hashMapClasses = new HashMap<String, IRI>();
        Iterator<OWLClass> iter = ontology.getClassesInSignature(true).iterator();
        while(iter.hasNext()){
            OWLClass nextClass=iter.next();

            if(!hashMapClasses.containsKey(nextClass.getIRI().getFragment().toString()))
                hashMapClasses.put(nextClass.getIRI().getFragment().toString(),nextClass.getIRI());
            else{
                String[] uri = nextClass.getIRI().getNamespace().toString().split("/");
                hashMapClasses.put(uri[uri.length-1]+nextClass.getIRI().getFragment().toString(),nextClass.getIRI());
            }
        }
        return  hashMapClasses;

    }

    public HashMap<String, IRI> getIndividuals(){
        HashMap<String, IRI> hashMapIndividuals = new HashMap<String, IRI>();
        Iterator<OWLNamedIndividual> iter = ontology.getIndividualsInSignature(true).iterator();
        while(iter.hasNext()){
            OWLNamedIndividual nextIndividual=iter.next();
            if(!nextIndividual.getIRI().toString().endsWith("/") && !nextIndividual.getIRI().toString().endsWith("#")) { //si es solo una uri
                if(!hashMapIndividuals.containsKey(nextIndividual.getIRI().getFragment().toString())) {

                    hashMapIndividuals.put(nextIndividual.getIRI().getFragment().toString(), nextIndividual.getIRI());
                }
                else{
                    String[] uri = nextIndividual.getIRI().getNamespace().toString().split("/");
                    hashMapIndividuals.put(uri[uri.length-1]+nextIndividual.getIRI().getFragment().toString(),nextIndividual.getIRI());
                }
            }

        }
        return  hashMapIndividuals;

    }

    public HashMap<String, IRI> getObjectProperties(){
        HashMap<String, IRI> hashMapProp = new HashMap<String, IRI>();
        Iterator<OWLObjectProperty> iter = ontology.getObjectPropertiesInSignature(true).iterator();

        while(iter.hasNext()){
            OWLObjectProperty nextProp=iter.next();
            hashMapProp.put(nextProp.getIRI().getFragment().toString(),nextProp.getIRI());
        }
        return  hashMapProp;
    }

    public HashMap<String, IRI> getDatatypeProperties(){
        HashMap<String, IRI> hashMapdataProp = new HashMap<String, IRI>();
        Iterator<OWLDataProperty> iter = ontology.getDataPropertiesInSignature(true).iterator();

        while(iter.hasNext()){
            OWLDataProperty nextProp=iter.next();
            hashMapdataProp.put(nextProp.getIRI().getFragment().toString(),nextProp.getIRI());
        }
        return  hashMapdataProp;
    }

    public HashMap<String, IRI> getProperties(){
        HashMap<String, IRI> properties = new HashMap<>();
        for (Map.Entry<String, IRI> entry : this.getDatatypeProperties().entrySet()) {
           properties.put(entry.getKey(), entry.getValue());
        }
        for (Map.Entry<String, IRI> entry : this.getObjectProperties().entrySet()) {
            properties.put(entry.getKey(), entry.getValue());
        }
        return properties;
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


}
