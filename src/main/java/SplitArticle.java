import java.io.InputStream;
import java.util.Properties;

public class SplitArticle {
    private static int split_num = 1;
    private static boolean if_override_split_char;
    private static String split_chars;
    public SplitArticle(String split_article_config_path){
        Properties properties = new Properties();
        try (InputStream input = Producer.class.getResourceAsStream(split_article_config_path)) {
            properties.load(input);
            split_num = Integer.valueOf(properties.getProperty("split_num"));
            if_override_split_char = Boolean.valueOf(properties.getProperty("if_override_split_char"));
            if(if_override_split_char){
                split_chars = properties.getProperty("split_chars");
            }
        } catch (Exception e) {
            System.out.println("error appeared when read the config file");
            e.printStackTrace();
        }
    }

    public static String splitArticle(String content){

        //分句标志
        String bd = ";?!~。？！；\n";
        if(if_override_split_char){
            bd = split_chars;
        }

        int count = 1;
        int begin = 0;
        String new_content = "";
        int len = content.length();

        //分段
        for (int i = 0; i < len; i++) {
            String subStr = content.substring(i, i + 1);
            if (bd.contains(subStr) || i==len-1) {
                if (split_num == count || i==len-1) {
                    new_content = new_content + "\n"+ (content.substring(begin, i + 1)).trim();
                    begin = i+1;
                    count = 1;
                } else {
                    count = count + 1;
                }
            }
        }
        return new_content.trim();
    }

//    public static void main(String[] args){
//        String content = "1,2。4；6？4\n5";
//        content = splitArticle(content);
//        System.out.println(content);
//    }
}
