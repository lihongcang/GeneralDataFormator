import java.io.InputStream;
import java.util.Properties;

public class YourConsumer {
    public YourConsumer(String your_consumer_config_path){
        Properties properties = new Properties();
        try (InputStream input = Producer.class.getResourceAsStream(your_consumer_config_path)) {
            properties.load(input);
        } catch (Exception e) {
            System.out.println("error appeared when read the config file");
            e.printStackTrace();
        }
    }
    public static String yourConsumer(String content){
        return content;
    }
}
