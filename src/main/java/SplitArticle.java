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

    
}
