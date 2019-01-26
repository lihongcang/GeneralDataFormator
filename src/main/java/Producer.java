import java.io.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.BlockingQueue;
import java.sql.*;

public class Producer implements Runnable{

    private BlockingQueue<Map<String,String>> blockingQueue;
    private Properties properties = new Properties();

    //数据源类型
    private String type;
    //如果数据源是数据库，配置连接参数
    private String driverName, dbURL, userName, userPwd,psm;
    private Connection dbConn = null;
    private PreparedStatement sm = null;
    private ResultSet rs = null;
    private String[] fields;
    //如果数据源是文本，配置路径参数
    private String file_path;
    private String n2one;


    public Producer(BlockingQueue<Map<String, String>> blockingQueue,String producer_config_path) {
        try (InputStream input = Producer.class.getResourceAsStream(producer_config_path)) {
            properties.load(input);
            this.blockingQueue = blockingQueue;
            type = properties.getProperty("type");

            switch (type){
                case "database":
                    System.out.println("producer is reading your database config");
                    driverName = properties.getProperty("jdbc.driver");
                    dbURL = properties.getProperty("jdbc.url");
                    userName = properties.getProperty("jdbc.user");
                    userPwd = properties.getProperty("jdbc.pass");
                    psm = properties.getProperty("jdbc.statement");
                    String fields_string = properties.getProperty("jdbc.fields_string");
                    fields = fields_string.split(":");
                    break;
                case "text":
                    System.out.println("producer is reading your text config");
                    file_path = properties.getProperty("file_path");
                    n2one=properties.getProperty("n2one");
                    break;
                default:
                    System.out.println("please config your data source type in config_producer.properties");
                    break;
            }

        } catch (Exception e) {
            System.out.println("error appeared when read the config file");
            e.printStackTrace();
        }

    }

    public void run() {
        switch (type){
            case "database":
                getFromDB();
                break;
            case "text":
                getFromText();
        }
    }

    public void getFromDB(){
        DateFormat fmt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        int i = 0;

        //连接数据库
        try {
            Class.forName(driverName);
            dbConn = DriverManager.getConnection(dbURL, userName, userPwd);
            System.out.println("connect successfully!");
        } catch (Exception e) {
            System.out.println("connect failed!");
            e.printStackTrace();
        }

        //取数据放入队列
        try {
            sm = dbConn.prepareStatement(psm);
            rs = sm.executeQuery();
            while (rs.next()) {
                Map<String, String> row = new HashMap<>();
                for(String field : fields){
                    row.put(field,rs.getString(field));
                }
                blockingQueue.put(row);

                //打印时间
                i += 1;
                if (i % 1000 == 0) {
                    System.out.println("producer\t"+fmt.format(System.currentTimeMillis()) +"=> " + i);
                }
            }

            //查询结束标志
            Map<String, String> row = new HashMap<>();
            for(String field : fields){
                row.put(field,"end-end-end");
            }
            blockingQueue.put(row);
            System.out.println("query is over");
            dbConn.close();

        } catch (Exception e) {
            System.out.println("query database failed!");
            e.printStackTrace();
        }
    }

    public void getFromText(){
        DateFormat fmt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        int i = 0;

        InputStreamReader read;// 考虑到编码格式
        try {
            read = new InputStreamReader(new FileInputStream(file_path), "UTF-8");
            BufferedReader bufferedReader = new BufferedReader(read);
            String lineTxt;

            int count=1;
            String content="";
            while ((lineTxt = bufferedReader.readLine()) != null) {
                if(count < Integer.valueOf(n2one)){
                    content+=lineTxt+"\n";
                    count+=1;
                    continue;
                }

                Map<String, String> row = new HashMap<>();
                row.put("content",content);
                blockingQueue.put(row);
                count=1;
                content="";

                i = i+1;
                if(i%1000 == 0){
                    System.out.println("producer\t"+fmt.format(System.currentTimeMillis()) +"=> " + i);
                }
            }
            if(content!=""){
                Map<String, String> row = new HashMap<>();
                row.put("content",content);
                blockingQueue.put(row);
            }

            //查询结束标志
            Map<String, String> row = new HashMap<>();
            row.put("content","end-end-end");
            blockingQueue.put(row);

            bufferedReader.close();
            read.close();

        } catch (Exception e) {
            System.out.println("producer read from text failed");
            e.printStackTrace();
        }

    }
}