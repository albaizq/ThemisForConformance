package reports;

import utils.Ontology;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;

public class TextConstants {

    public static final String reportName = "/report.html";
    public static final String siteName = "/index.html";
    public static final String getsiteFolderName(){
        return "site";
    }
    public static final String siteFolderTest = "sitetests";
    public static final String oopsResources= "/oops.rar";
    public static final String vocabResources = "/HTMLGenerator.zip";

    public static final String reportTestName = "/report.txt";
    public static final String labelName = "coverage test";
    public static final String requirementsName = "/requirements.html";


    public static final String header = "<!DOCTYPE html>\n" +
            "<html lang=\"en\">\n" +
            "  <head>\n" +
            "    <meta charset=\"UTF-8\">\n" +
            "    <title>Requirements</title>\n" +
            "    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n" +
            "    <meta name=\"description\" content=\"This describes the requirements identified for the ontology\">\n" +
            "    <meta name=\"languaje\" content=\"English\">\n" +
            "    \n" +
            "    <link rel=\"stylesheet\" href=\"HTMLGenerator/themes/blue/style.css\" type=\"text/css\" media=\"print, projection, screen\" />\n" +
            "    <script src=\"HTMLGenerator/js/jquery-1.11.0.js\"></script>\n" +
            "    <script type=\"text/javascript\" src=\"HTMLGenerator/js/jquery.tablesorter.min.js\"></script>\n" +
            "    <script type=\"text/javascript\" src=\"HTMLGenerator/js/jquery.stickytableheaders.js\"></script>\n" +
            "    <script type=\"text/javascript\" src=\"HTMLGenerator/js/jquery-ui.js\"></script>\n" +
            "    <script type=\"text/javascript\" src=\"HTMLGenerator/js/bootstrap.js\"></script>\n" +
            "    <link rel=\"stylesheet\" href=\"HTMLGenerator/css/jquery-ui.css\"></link>\n" +
            "    <script type=\"text/javascript\" id=\"js\">\n" +
            "	    $(document).ready(function() \n" +
            "		    { \n" +
            "		    	$(\"#tablesorter-demo\").tablesorter(); \n" +
            "		    	$(\"#tablesorter-demo\").stickyTableHeaders(); \n" +
            "		    	$('[data-toggle=\"tooltip\"]').tooltip(); \n" +

            "		    } \n" +
            "	    ); \n" +
            "    </script>\n" +

            "\n" +
            "    <!-- Le styles -->\n" +
            "    <link href=\"HTMLGenerator/css/bootstrap.css\" rel=\"stylesheet\">\n" +
            "    <style type=\"text/css\">\n" +
            "      body {\n" +
            "        padding-top: 60px;\n" +
            "        padding-bottom: 40px;\n" +
            "      }\n" +
            "    </style>\n" +
            //"    <link href=\"HTMLGenerator/css/bootstrap-responsive.css\" rel=\"stylesheet\">\n" +
            "    \n" +
            "    <!-- HTML5 shim, for IE6-8 support of HTML5 elements -->\n" +
            "    <!--[if lt IE 9]>\n" +
            "      <script src=\"HTMLGenerator/js/html5shiv.js\"></script>\n" +
            "    <![endif]-->\n" +
            "\n" +
            "    <!-- Fav and touch icons -->\n" +
            "  </head>\n" +
            "\n" +
            "  <body>\n" +
            "\n";

    public static final String navBarVocab = "<div class=\"navbar navbar-inverse navbar-fixed-top\" role=\"navigation\">\n" +
            "  <div class=\"container\">\n" +
            "    <div class=\"navbar-header\">\n" +
            "      <button type=\"button\" class=\"navbar-toggle\" data-toggle=\"collapse\" data-target=\".navbar-collapse\">\n" +
            "        <span class=\"sr-only\">Toggle navigation</span>\n" +
            "        <span class=\"icon-bar\"></span>\n" +
            "        <span class=\"icon-bar\"></span>\n" +
            "        <span class=\"icon-bar\"></span>\n" +
            "      </button>\n" +
            "    </div>\n" +
            "    <div class=\"collapse navbar-collapse\">\n" +
            "      <ul class=\"nav navbar-nav\">\n" +
            "        <li class=\"active\"><a href=\"#\">utils.Ontology requirements</a></li>\n" +
            "      </ul>\n" +
            "    </div><!--/.nav-collapse -->\n" +
            "  </div>\n" +
            " </div>\n" ;



    public static String getMetadata(Ontology onto){
        String html ="    <div class=\"container\">\n" +
                "\n" +
                "      <!-- Jumbotron -->\n" +
                "      <div class=\"jumbotron\">\n" +
                "        <h1><img src=\"HTMLGenerator/vicinity.png\" alt=\"VICINITY logo\" class=\"img-rounded\" class=\"img-responsive\" /></h1>\n" +
                "        <p class=\"lead\">Here you can find the list of the tested requirements"
                +
                "      </div>\n" +
                "      <hr>\n";

        return html;
    }



    public static final String end = "<hr>\n" +
            "" +
            "      <footer class=\"footer\">\n" +
            "      " +
            "      <div class=\"row\">\n" +
            "    	<div class=\"col-md-10\">\n" +
            "    		Developed by " +
            "	        <a href = \"http://oeg-upm.net\" target=\"_blank\">utils.Ontology Engineering Group</a>\n" +
            "	        <br>\n" +
            "    	Built with <a target=\"_blank\" href=\"http://getbootstrap.com/\">Bootstrap</a>\n" +
            "    	Icons from <a target=\"_blank\" href=\"http://glyphicons.com/\">Glyphicons</a>\n " +
            "	        <br>\n" +
            "	        Latest revision "+(new SimpleDateFormat("MMMM", Locale.UK).format(new Date(Calendar.getInstance().getTimeInMillis())))+", "+new GregorianCalendar().get(Calendar.YEAR)+"\n" +
            "           <br><p>&copy; utils.Ontology Engineering Group</p>\n"+
            "        </div>\n" +
            "    	<div class=\"col-md-2\">\n" +
            "    		<a href=\"http://www.oeg-upm.net/\" target=\"_blank\"><img src=\"HTMLGenerator/logo.gif\" alt=\"OEG logo\" class=\"img-rounded\" class=\"img-responsive\" /></a>\n" +
            "    	</div>\n" +
            "      </div>\n" +
            "" +
            "      </footer>\n" +
            "" +
            "    </div> <!-- /container -->\n";


    public static final String tableHeadVocab=
            "<div class=\"row\"> \n"+
                    "<div class=\"ui-widget\">\n"+
                    "<div class=\"col-md-8\" > \n"+
                    "</div>\n"+
                    "</div>\n"+
                    "</div>\n"+
                    "<br>\n"+
                    "<table id=\"tablesorter-demo\" class=\"tablesorter table table-hover table-responsive\">\n"+
                    "<thead>\n";
                   /* " <tr>"+
                    "<th class=\"col-md-2\">Identifier </th>"+
                    "<th class=\"col-md-4\">Competency Question</th>"+
                    "<th class=\"col-md-4\">Test</th>"+
                    //"<th class=\"col-md-2\">Comments</th>"+
                    "<th class=\"col-md-2\">Result</th>"+
                    //"<th class=\"col-md-4\">Relation with other requirements</th>"+
                    "</tr>" +
                    "</thead>\n"+
                    "<tbody>\n";*/





    public static final String tableEnd = "</tbody></table>\n";




    public static String getErrorReportHTML(){
        String s ="";
        return s;
    }

}
