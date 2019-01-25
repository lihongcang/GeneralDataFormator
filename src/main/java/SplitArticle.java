import java.io.InputStream;
import java.util.Properties;

public class SplitArticle {
    private static int split_num = 1;
    public void SplitArticle(){
        Properties properties = new Properties();
        try (InputStream input = Producer.class.getResourceAsStream("config_split_article.properties")) {
            properties.load(input);
            split_num = Integer.valueOf(properties.getProperty("split_num"));
        } catch (Exception e) {
            System.out.println("error appeared when read the config file");
            e.printStackTrace();
        }
    }

    public static String splitArticle(String content){

        int count = 0;

        //分句标志
        String bd = ";?!~。？！；\n";

        String new_content = content;
        int len = new_content.length();

        //分段
        for (int i = 0; i < len; i++) {
            String subStr = content.substring(i, i + 1);
            if (bd.contains(subStr)) {
                if (split_num == count) {
                    new_content = (content.substring(0, i + 1)).trim() +"\n"+ (content.substring(i + 1, len)).trim();
                    count = 0;
                } else {
                    count = count + 1;
                }
            }
        }
        return new_content;
    }

}
