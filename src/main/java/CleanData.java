import java.io.InputStream;
import java.util.Properties;

public class CleanData{
    private static boolean remove_whitespace = false;
    private static boolean remove_not_ch_en_num = true;
    private static int repeat2one = 6;
    private static boolean remove_repeat_if_whitespace = false;
    private static boolean remove_html_tag = true;
    private static int length_limit = 5;
    public CleanData(){

        //读取cleanData的配置文件
        Properties properties = new Properties();
        try (InputStream input = Producer.class.getResourceAsStream("config_cleanData.properties")) {
            properties.load(input);
            remove_whitespace = Boolean.valueOf(properties.getProperty("remove_whitespace"));
            remove_not_ch_en_num = Boolean.valueOf(properties.getProperty("remove_not_ch_en_num"));
            repeat2one = Integer.valueOf(properties.getProperty("repeat2one"));
            remove_repeat_if_whitespace = Boolean.valueOf(properties.getProperty("remove_repeat_if_whitespace"));
            remove_html_tag = Boolean.valueOf(properties.getProperty("remove_html_tag"));
            length_limit = Integer.valueOf(properties.getProperty("length_limit"));
        } catch (Exception e) {
            System.out.println("error appeared when read the config file");
            e.printStackTrace();
        }

    }
    public static String cleanData(String content){
        //去除空格
        if(remove_whitespace){
            content = content.replaceAll("\\s","");
        }
        //去除重复字符
        if(remove_repeat_if_whitespace){
            content = content.replaceAll("(.)\\1{"+repeat2one+",}","$1");
        }else{
            content = content.replaceAll("([^\\s])\\1{"+repeat2one+",}","$1");
        }
        //去除乱码
        if(remove_not_ch_en_num){
            content = content.replaceAll("[ [^\u4E00-\u9FA5] && [^a-zA-Z0-9 \n] && [^~`!@#$%^&*()\\-_+={}\\[\\]|\\:;\"\'<>,.?/] && [^~?！@#￥%……&*（）??+={}【】|、：；“”‘      ’《》，。？/] ]", "");
        }
        //去除html标签
        if(remove_html_tag){
            content = content.replaceAll("<script.*?>[\\s\\S]*?<.*?/script>","");
            content = content.replaceAll("&nbsp","");
            content = content.replaceAll("<[^>\\d]+.*?>","");
        }
        //去除长度小于100
        if( content.replaceAll("\\s","").length()<length_limit){
            return null;
        }
        return content;
    }
}
