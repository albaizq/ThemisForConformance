package utils;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.*;

import java.io.File;
import java.util.HashMap;
import java.util.Iterator;

public class Ontology {
    public OWLOntology ontology;
    public OWLOntologyManager manager;
    public IRI prov;


    public Ontology() {
    }

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

    public String load_ontologyFile(String ontologyURL) throws  Exception{
        String response = " ";
        this.manager = OWLManager.createOWLOntologyManager();

        String ext = org.apache.commons.io.FilenameUtils.getExtension(ontologyURL);
        File initialFile = new File(ontologyURL);
        this.setOntology(this.manager.loadOntologyFromOntologyDocument(initialFile));
        this.setProv(IRI.create(initialFile.getAbsoluteFile()));

        return response;

    }

    public String load_ontologyURL(String ontologyURL) {
        String response = " ";
        this.manager = OWLManager.createOWLOntologyManager();
        IRI path = IRI.create(ontologyURL);
        try {
            OWLOntology ontology = this.manager.loadOntology(path);
            // remove all imports
            for(OWLOntology imported: manager.getDirectImports(ontology)) {
                manager.removeOntology(imported);
            }
            System.out.println(ontology.getImports());
            this.setOntology(ontology);
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



}
