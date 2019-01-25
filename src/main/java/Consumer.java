import java.io.InputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.BlockingQueue;

public class Consumer implements Runnable{
    //数据输入和数据输出队列
    private BlockingQueue<Map<String,String>> blockingQueueIn;
    private BlockingQueue<Map<String,String>> blockingQueueOut;
    //字段组
    private String[] fields;
    private String fieldPrimary;
    //打印参数
    private String echonum;
    //处理模块
    private int function;

    public Consumer(BlockingQueue<Map<String, String>> blockingQueueIn,BlockingQueue<Map<String, String>> blockingQueueOut) {

        Properties properties = new Properties();

        try (InputStream input = Producer.class.getResourceAsStream("config.properties")) {
            properties.load(input);

            //初始化输入队列输出
            this.blockingQueueIn = blockingQueueIn;
            this.blockingQueueOut = blockingQueueOut;

            //使用的字段,请确保你的字段使用:分割
            String fieldstring = properties.getProperty("fieldsUse");
            fields = fieldstring.split(":");
            fieldPrimary = properties.getProperty("fieldPrimary");
            //选择的功能
            function = Integer.valueOf(properties.getProperty("function"));
            //初始化打印参数
            echonum = properties.getProperty("echonum");

        } catch (Exception e) {
            System.out.println("error appeared when read the config file");
            e.printStackTrace();
        }

    }

    public void run() {
        //打印时间的格式
        DateFormat fmt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        int i = 0;

        while(true){
            //从队列拿出数据
            Map<String,String> row = null;
            try {
                row = blockingQueueIn.take();
            } catch (Exception e) {
                System.out.println("error appeared when take data");
                e.printStackTrace();
            }

            //判断结束
            String content = row.get(fieldPrimary);
            if(content.equals("end-end-end")){
                System.out.println("All data has been consumed");
                break;
            }

            //处理函数
            switch (function){
                case 1:
                    content = CleanData.cleanData(content);

                    //将处理后的数据放入队列
                    Map<String,String> row_result = null;
                    for(String field : fields){
                        if(!field.equals(fieldPrimary)) {
                            row_result.put(field, row.get(field));
                        }else{
                            row_result.put(fieldPrimary,content);
                        }
                    }

                    break;
                case 2:
                    GetHtmlContent.Article article = GetHtmlContent.getArticle(content);
                    String title = article.Title;
                    content = title+"\n"+article.Content;

                    Map<String,String> row_result2 = null;
                    for(String field : fields){
                        if(!field.equals(fieldPrimary)) {
                            row_result2.put(field, row.get(field));
                        }else{
                            row_result2.put(fieldPrimary,content);
                        }
                    }

                    break;
                case 3:

                    break;
            }

        }

    }

}