package results;

import org.json.JSONException;
import org.json.JSONObject;
import tests.TestCaseDesign;
import tests.TestCaseResult;
import ontologies.Ontology;

import java.util.ArrayList;

import static results.TermsExtractor.processTestExpressionToExtractTerms;

public class Report {

    /*Process the results for printing them*/
    public  static JSONObject printReportPerOntology(Ontology onto, TestCaseDesign requirement, ArrayList<TestCaseResult> results) throws JSONException {
        JSONObject objResults = new JSONObject();
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
        objResults.put("Requirement description", requirement.getDescription());
        objResults.put("Result", finalResult);
        objResults.put("Ontology", onto.getProv());
        objResults.put("Provenance", requirement.getProvenance());
        ArrayList<String> terms = new ArrayList<>();
        for(String purpose: requirement.getPurpose()) {
            for (String term: processTestExpressionToExtractTerms(purpose)) {
                if(!terms.contains(term))
                    terms.add(term);
            }
        }
        objResults.put("Terms involved", terms);
        return objResults;
    }
}
