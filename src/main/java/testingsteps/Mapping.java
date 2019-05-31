package testingsteps;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/*
 * Class to obtain the terms of each test
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


}
