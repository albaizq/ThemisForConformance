package utils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.semanticweb.owlapi.model.IRI;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;

public class ProcessCSV {
    /*Process csv where the ontologies are stored*/
    public  static HashMap<String, String> processCSVVocabs(String path) throws FileNotFoundException {
        HashMap<String, String> ontoAndTest = new HashMap<>();
        BufferedReader in = null;
        in = new java.io.BufferedReader(new java.io.FileReader(path));

        String currentLine;
        int lineN = 0;
        String ontology="";
        String test="";
        String name="";
        try {
            while ((currentLine = in.readLine()) != null) {
                if (lineN == 0) {
                    lineN++; //get rid of the headers
                } else {
                    //process each vocab
                    String[] info = currentLine.split(";");
                    ontology = info[0];
                    test = info[1];
                    ontoAndTest.put(ontology,test);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            in.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return ontoAndTest;
    }

    public  static JSONArray processCSVGoT(String path) throws FileNotFoundException {
        BufferedReader in = null;
        in = new java.io.BufferedReader(new java.io.FileReader(path));
        String currentLine;
        int lineN = 0;
        String ontology="";
        String value="";
        String term="";
        JSONArray array = new JSONArray();
        try {
            while ((currentLine = in.readLine()) != null) {
                if (lineN == 0) {
                    lineN++; //get rid of the headers
                } else {
                    //process each vocab
                    JSONObject obj = new JSONObject();
                    String[] info = currentLine.split(";");
                    if(info.length==3) {
                        ontology = info[0];
                        value = info[1];
                        term = info[2];
                    }
                    obj.put("Ontology",ontology);
                    obj.put("Value",value);
                    obj.put("Term",term);
                    array.put(obj);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        try {
            in.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return array;
    }


    public  static HashMap<String, IRI>  processCSVGoTHash(String path) throws FileNotFoundException {
        BufferedReader in = null;
        in = new java.io.BufferedReader(new java.io.FileReader(path));
        String currentLine;
        int lineN = 0;
        String value="";
        String term="";
        HashMap<String, IRI> got = new HashMap<String, IRI>();
        try {
            while ((currentLine = in.readLine()) != null) {
                if (lineN == 0) {
                    lineN++; //get rid of the headers
                } else {
                    //process each vocab
                    String[] info = currentLine.split(";");
                    if(info.length==2) {
                        value = info[0];
                        term = info[1];
                    }
                    got.put(value, IRI.create(term));

                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            in.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return got ;
    }


}
