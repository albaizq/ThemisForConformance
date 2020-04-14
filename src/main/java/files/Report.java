package files;

import org.json.JSONException;
import org.json.JSONObject;
import tests.TestCaseDesign;
import tests.TestCaseResult;
import ontologies.Ontology;

import java.util.ArrayList;

import static testingsteps.Mapping.processTestExpressionToExtractTerms;

public class Report {

    /*Store the result of each test design*/
    public  static JSONObject printReportPerOntology(Ontology onto, TestCaseDesign requirement, ArrayList<TestCaseResult> results) throws JSONException {
        JSONObject objresults = new JSONObject();
        String finalResult = "";
        for (TestCaseResult result : results) {
            if (result.getTestResult().equals("not passed")){
                finalResult = "not passed";
                break;
            }else if(result.getTestResult().equals("undefined")){
                finalResult = "undefined";
                break;
            }else if(result.getTestResult().equals("absent")){
                finalResult = "absent";
                break;
            }
        }
        if(finalResult==""){
            finalResult="passed";
        }
        objresults.put("Requirement description", requirement.getDescription());
        objresults.put("Result", finalResult);
        objresults.put("Ontology", onto.getProv());
        objresults.put("Provenance", requirement.getProvenance());
        ArrayList<String> terms = new ArrayList<>();
        for(String purpose: requirement.getPurpose()) {
            for (String term: processTestExpressionToExtractTerms(purpose)) {
                if(!terms.contains(term))
                    terms.add(term);
            }
        }
        objresults.put("Terms involved", terms);
        return objresults;
    }
}
