import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.BlockingQueue;

public class Consumer implements Runnable{

    private BlockingQueue<Map<String,String>> blockingQueueIn;
    private BlockingQueue<Map<String,String>> blockingQueueOut;

    //需要保留到blockingQueueOut的字段、需要处理的字段、选择是否覆盖要处理的字段、需要使用的功能
    private String[] fields;
    private String fieldPrimary;
    private boolean override_fieldPrimary;
    private String new_field_name;
    private int function_num;
    private boolean if_custom_config;
    private String function_config_file_path;

    public Consumer(BlockingQueue<Map<String, String>> blockingQueueIn,BlockingQueue<Map<String, String>> blockingQueueOut,String consumer_config_path) {

        Properties properties = new Properties();

        try (InputStream input = Producer.class.getResourceAsStream(consumer_config_path)) {
            properties.load(input);

            this.blockingQueueIn = blockingQueueIn;
            this.blockingQueueOut = blockingQueueOut;

            String field_string = properties.getProperty("fields");
            fields = field_string.split(":");
            fieldPrimary = properties.getProperty("fieldPrimary");
            override_fieldPrimary = Boolean.valueOf(properties.getProperty("override_fieldPrimary"));
            if(!override_fieldPrimary){
                new_field_name = properties.getProperty("new_field_name");
            }
            function_num = Integer.valueOf(properties.getProperty("function_num"));
            if_custom_config = Boolean.valueOf(properties.getProperty("if_custom_config"));
            if(if_custom_config){
                function_config_file_path = properties.getProperty("function_config_file_path");
            }
        } catch (Exception e) {
            System.out.println("error appeared when read the cousumer config file");
            e.printStackTrace();
        }

    }

    public void run() {

        while (true) {
            //从队列拿出数据
            Map<String, String> row = null;
            try {
                row = blockingQueueIn.take();
            } catch (Exception e) {
                System.out.println("error appeared when take data");
                e.printStackTrace();
            }

            //判断结束
            String content = row.get(fieldPrimary);
            if (content.equals("end-end-end")) {
                System.out.println("All data has been consumed");
                Map<String, String> row_result = new HashMap<>();
                for (String field : fields) {
                    row_result.put(field, "end-end-end");
                }
                if (!override_fieldPrimary) {
                    row_result.put(new_field_name, "end-end-end");
                    row_result.put(fieldPrimary, content);
                } else {
                    row_result.put(fieldPrimary, "end-end-end");
                }
                try {
                    blockingQueueOut.put(row_result);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
            }

            //处理数据
            String processed_content=null;
            switch (function_num){
                case 1:
                    if(if_custom_config){
                        CleanData cleandata = new CleanData(function_config_file_path);
                        processed_content = cleandata.cleanData(content);
                        break;
                    }else{
                        processed_content = CleanData.cleanData(content);
                        break;
                    }
                case 2:
                    if(if_custom_config){
                        GetHtmlContent gethtmlcontent = new GetHtmlContent(function_config_file_path);
                        Article article = gethtmlcontent.getArticle(content);
                        String title = article.Title.trim();
                        processed_content = title+"\n"+article.Content;
                        break;
                    }else{
                        Article article = GetHtmlContent.getArticle(content);
                        String title = article.Title;
                        processed_content = title+"\n"+article.Content;
                        break;
                    }
                case 3:
                    if(if_custom_config){
                        SplitArticle splitArticle = new SplitArticle(function_config_file_path);
                        System.out.println("ord ..."+content);
                        processed_content = splitArticle.splitArticle(content);
                        break;
                    }else{
                        processed_content = SplitArticle.splitArticle(content);
                        break;
                    }
                case 4:
                    if(if_custom_config){
                        TokenArticle tokenArticle = new TokenArticle(function_config_file_path);
                        processed_content = tokenArticle.tokenArticle(content);
                        break;
                    }else{
                        processed_content = TokenArticle.tokenArticle(content);
                        break;
                    }
                case 5:
                    //TO-DO : 测试
                    if(if_custom_config){
                        RemoveRepeat removeRepeat = new RemoveRepeat(function_config_file_path);
                        processed_content = String.valueOf(removeRepeat.removeRepeat(content));
                        break;
                    }else{
                        processed_content = String.valueOf(RemoveRepeat.removeRepeat(content));
                        break;
                    }
                case 100:
                    if(if_custom_config){
                        YourConsumer yourConsumer = new YourConsumer(function_config_file_path);
                        processed_content = yourConsumer.yourConsumer(content);
                        break;
                    }else{
                        processed_content = YourConsumer.yourConsumer(content);
                        break;
                    }
                default:
                    break;
            }

            //将处理后的数据放入队列
            if(processed_content!=null){
                Map<String,String> row_result = new HashMap<>();
                for(String field : fields){
                    row_result.put(field, row.get(field));
                }
                if(override_fieldPrimary){
                    row_result.put(fieldPrimary,processed_content);
                }else {
                    row_result.put(new_field_name,processed_content);
                    row_result.put(fieldPrimary,content);
                }
                try {
                    blockingQueueOut.put(row_result);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

        }
    }

}