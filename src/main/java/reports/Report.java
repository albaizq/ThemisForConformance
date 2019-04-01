package reports;

import tests.TestCaseDesign;
import tests.TestCaseImplementation;
import tests.TestCaseResult;
import utils.Ontology;
import utils.Utils;

import java.util.ArrayList;

public class Report {
    public String generateHTMLTable(ArrayList<Ontology> ontologies, ArrayList<TestCaseResult> trs, ArrayList<TestCaseImplementation> tis, ArrayList<TestCaseDesign> tds, String fileName){
        String eval ="";

        for (TestCaseDesign td: tds){
            eval += "<tr>\n";
            eval+=  "   <th>Identifier </th>\n"+
                    "   <th>Test</th>\n";

            for(Ontology ontology: ontologies){
                eval +="    <th>\n";
                eval+= "    "+ontology.getProv().toString()+"\n";
                eval+= "   </th>\n";
            }
            eval +="</tr>\n";

            if((td.getSource()!=null ) ) {
                eval += "<tr>\n";
                eval +="    <td>\n";
                String purposeTest="";
                for(String purpose: td.getPurpose()){
                    purposeTest+=purpose.replaceAll("<","").replaceAll(">","")+",";
                }
                purposeTest = purposeTest.substring(0,purposeTest.length() - 1);

                eval +="    "+purposeTest+"\n  </td>\n";
                for (TestCaseImplementation ti : tis) {
                    if (ti.getRelatedTestDesign().equals(td.getUri()) ) {
                        for(Ontology onto: ontologies) {
                            for (TestCaseResult tr : trs) {
                                if (tr.getRelatedTestImpl().equals(ti.getUri()) &&  tr.getOntologyURI().equals(onto.getProv())) {
                                    eval += "   <td class=\"tg-031e\">\n";
                                    if (tr.getTestResult().equals("passed")) {
                                        eval += "   <span class=\"glyphicon glyphicon-ok\" style =\"color:rgb(0, 255, 0)\"></span>\n";
                                    } else if (tr.getTestResult().equals("undefined")) {
                                        eval += "   <span class=\"label label-default\">Undefined</span>\n";
                                    }
                                    eval += "   </td>\n";
                                }
                            }
                        }
                    }
                }
                eval += "</tr>\n";
            }
        }
        return eval;
    }



    public  void saveDocument(String path, String textToWrite){
        java.io.File f = new java.io.File(path);
        java.io.Writer out = null;
        try{
            f.createNewFile();
            out = new java.io.BufferedWriter(new java.io.OutputStreamWriter(new java.io.FileOutputStream(f), "UTF-8"));
            out.write(textToWrite);
            out.close();
        }catch(java.io.IOException e){
            System.err.println("Error while creating the file "+e.getMessage()+"\n"+f.getAbsolutePath());
        }

    }
    public  java.io.File HTMLgenerator(ArrayList<Ontology> ontos, Report report, ArrayList<TestCaseResult> trs, ArrayList<TestCaseImplementation> tis, ArrayList<TestCaseDesign> tds, String resources, String urlOut ){

        java.io.File auxF = new java.io.File(urlOut);
        auxF = new java.io.File(auxF.getAbsolutePath() + java.io.File.separator + TextConstants.getsiteFolderName()); //modify URL
        auxF.mkdirs();
        createFolderStructure(auxF.getAbsolutePath(), resources);
        String urlReportOut = auxF.getAbsolutePath() + java.io.File.separator + TextConstants.reportName;

        String html = TextConstants.header + TextConstants.navBarVocab;
        html += TextConstants.tableHeadVocab;
        html += report.generateHTMLTable(ontos, trs, tis, tds,"requirements.html");
        html += TextConstants.tableEnd;
        html += TextConstants.end;
        report.saveDocument(urlReportOut, html);
        return auxF;
    }




    public static void createFolderStructure(String savePath, String resources) {
        System.out.println("UnZip");
        System.out.println(resources);
        System.out.println(savePath);
        Utils.unZipIt(resources, savePath);
    }
}
