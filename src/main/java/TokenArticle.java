import org.ansj.app.extracting.domain.Token;
import org.ansj.domain.Result;
import org.ansj.domain.Term;
import org.ansj.recognition.impl.StopRecognition;
import org.ansj.splitWord.analysis.ToAnalysis;

import java.io.*;
import java.util.List;
import java.util.Properties;

public class TokenArticle {
    private static boolean if_custom_stop=false;
    private static String[] stop_natures={"u","r"};
    private static String stop_dic_file_path;

    public TokenArticle(String token_article_config_path){
        Properties properties = new Properties();
        try (InputStream input = Producer.class.getResourceAsStream(token_article_config_path)) {
            properties.load(input);
            String stop_natures_string = properties.getProperty("stop_natures_string");
            if_custom_stop = Boolean.valueOf(properties.getProperty("if_custom_stop"));
            if(if_custom_stop){
                stop_natures = stop_natures_string.split(":");
                stop_dic_file_path = properties.getProperty("stop_dic_file_path");
            }
        } catch (Exception e) {
            System.out.println("error appeared when read the config file");
            e.printStackTrace();
        }
    }
    public static String tokenArticle(String content){

        Result result = ToAnalysis.parse(content);
        if(if_custom_stop){
            StopRecognition filter = new StopRecognition();
            for(String nature:stop_natures){
                filter.insertStopWords(nature);
            }
            try{
                BufferedReader stopdic = new BufferedReader(new InputStreamReader(new FileInputStream(new File(stop_dic_file_path))));
                String lineTxt;
                while((lineTxt=stopdic.readLine())!=null){
                    filter.insertStopWords(lineTxt);
                }
            }catch(Exception e){
                e.printStackTrace();
            }
            result = result.recognition(filter);
        }

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