package ontologies;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.*;
import tests.TestCaseDesign;

import java.net.URL;
import java.util.*;

import static ontologies.SkeletonGenerator.createSkeletonFromTestCases;

/*Class for managing ontologies*/
public class Ontology {
    private OWLOntology owlOntology;
    private OWLOntologyManager manager;
    private IRI prov;

    public OWLOntology getOwlOntology() {
        return owlOntology;
    }

    public void setOwlOntology(OWLOntology owlOntology) {
        this.owlOntology = owlOntology;
    }

    public OWLOntologyManager getManager() {
        return manager;
    }

    public void setManager(OWLOntologyManager manager) {
        this.manager = manager;
    }

    public String loadOntologyURL(String ontologyURL) {
        String response = " ";
        this.manager = OWLManager.createOWLOntologyManager();
        IRI path = IRI.create(ontologyURL);
        try {
            OWLOntology ontologyLoaded = this.manager.loadOntology(path);
            // remove all imports
            for(OWLOntology imported: manager.getDirectImports(ontologyLoaded)) {
                manager.removeOntology(imported);
            }
            this.setOwlOntology(ontologyLoaded);
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

    public Map<String, IRI> getClasses(){
        HashMap<String, IRI> hashMapClasses = new HashMap<>();
        Iterator<OWLClass> iter = owlOntology.getClassesInSignature(true).iterator();
        while(iter.hasNext()){
            OWLClass nextClass=iter.next();

            if(!hashMapClasses.containsKey(nextClass.getIRI().getFragment()))
                hashMapClasses.put(nextClass.getIRI().getFragment(),nextClass.getIRI());
            else{
                String[] uri = nextClass.getIRI().getNamespace().split("/");
                hashMapClasses.put(uri[uri.length-1]+nextClass.getIRI().getFragment(),nextClass.getIRI());
            }
        }
        return  hashMapClasses;

    }

    public Map<String, IRI> getIndividuals(){
        HashMap<String, IRI> hashMapIndividuals = new HashMap<>();
        Iterator<OWLNamedIndividual> iter = owlOntology.getIndividualsInSignature(true).iterator();
        while(iter.hasNext()){
            OWLNamedIndividual nextIndividual=iter.next();
            if(!nextIndividual.getIRI().toString().endsWith("/") && !nextIndividual.getIRI().toString().endsWith("#")) { //si es solo una uri
                if(!hashMapIndividuals.containsKey(nextIndividual.getIRI().getFragment())) {

                    hashMapIndividuals.put(nextIndividual.getIRI().getFragment(), nextIndividual.getIRI());
                }
                else{
                    String[] uri = nextIndividual.getIRI().getNamespace().split("/");
                    hashMapIndividuals.put(uri[uri.length-1]+nextIndividual.getIRI().getFragment(),nextIndividual.getIRI());
                }
            }

        }
        return  hashMapIndividuals;

    }

    public Map<String, IRI> getObjectProperties(){
        HashMap<String, IRI> hashMapProp = new HashMap<>();
        Iterator<OWLObjectProperty> iter = owlOntology.getObjectPropertiesInSignature(true).iterator();

        while(iter.hasNext()){
            OWLObjectProperty nextProp=iter.next();
            hashMapProp.put(nextProp.getIRI().getFragment(),nextProp.getIRI());
        }
        return  hashMapProp;
    }

    public Map<String, IRI> getDatatypeProperties(){
        HashMap<String, IRI> hashMapdataProp = new HashMap<>();
        Iterator<OWLDataProperty> iter = owlOntology.getDataPropertiesInSignature(true).iterator();

        while(iter.hasNext()){
            OWLDataProperty nextProp=iter.next();
            hashMapdataProp.put(nextProp.getIRI().getFragment(),nextProp.getIRI());
        }
        return  hashMapdataProp;
    }

    public Map<String, IRI> getProperties(){
        HashMap<String, IRI> properties = new HashMap<>();
        for (Map.Entry<String, IRI> entry : this.getDatatypeProperties().entrySet()) {
           properties.put(entry.getKey(), entry.getValue());
        }
        for (Map.Entry<String, IRI> entry : this.getObjectProperties().entrySet()) {
            properties.put(entry.getKey(), entry.getValue());
        }
        return properties;
    }

    public static Ontology loadOntology(String url, List<TestCaseDesign> testCaseDesigns, String testSuiteProv) {

        //Load ontologies
        Ontology ontology = new Ontology();

        if(isValid(url)) {
            ontology.loadOntologyURL(url);
        }else{
            try {
                ontology = createSkeletonFromTestCases(testSuiteProv,(ArrayList<TestCaseDesign>) testCaseDesigns);
            } catch (OWLOntologyCreationException | OWLOntologyStorageException  e) {
                e.printStackTrace();
            }
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
