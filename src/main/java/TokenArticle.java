import org.ansj.domain.Result;
import org.ansj.domain.Term;
import org.ansj.recognition.impl.StopRecognition;
import org.ansj.splitWord.analysis.ToAnalysis;

import java.io.*;
import java.util.List;

public class TokenArticle {

    public static String tokenArticle(String content){

        StopRecognition filter = new StopRecognition();
        filter.insertStopNatures("u"); //过滤词性
        filter.insertStopNatures("r");
        try{
            BufferedReader stopdic = new BufferedReader(new InputStreamReader(new FileInputStream(new File("./library/stop.dic"))));
            String lineTxt;
            while((lineTxt=stopdic.readLine())!=null){
                filter.insertStopWords(lineTxt);
            }
        }catch(Exception e){
            e.printStackTrace();
        }

        Result result = ToAnalysis.parse(content).recognition(filter);
        //Result result =  ToAnalysis.parse(content);
        List<Term> terms = result.getTerms();
        String result_str = "";
        for(int i=0;i<terms.size();i++)
        {
            String word = terms.get(i).toString();
            word = word.split("/")[0];
            result_str =result_str + word+" ";
        }
        result_str = result_str.replaceAll("[[^\u4E00-\u9FA5]&&[^a-zA-Z0-9 ]]", "");
        result_str = result_str.replaceAll("\\s{2,}"," ");
        result_str = result_str.replaceAll("^\\s+|\n","");
        return result_str;
    }
}