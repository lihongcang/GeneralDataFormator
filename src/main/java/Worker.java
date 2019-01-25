import java.io.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.BlockingQueue;
import java.sql.*;

public class Worker implements Runnable{

    private BlockingQueue<Map<String,String>> blockingQueue;
    private Properties properties = new Properties();

    //数据流类型
    private String type;
    //如果数据流是数据库，配置连接参数
    private String driverName, dbURL, userName, userPwd,psm;
    private Connection dbConn = null;
    private PreparedStatement sm = null;
    private String[] fields;
    //如果数据流是文本，配置路径参数
    private String field;
    private String split_string;
    private String file_path;


    public Worker(BlockingQueue<Map<String, String>> blockingQueue,String worker_config_path) {
        try (InputStream input = Producer.class.getResourceAsStream(worker_config_path)) {
            properties.load(input);
            this.blockingQueue = blockingQueue;
            type = properties.getProperty("type");

            switch (type){
                case "database":
                    System.out.println("worker is reading your database config");
                    driverName = properties.getProperty("jdbc.driver");
                    dbURL = properties.getProperty("jdbc.url");
                    userName = properties.getProperty("jdbc.user");
                    userPwd = properties.getProperty("jdbc.pass");
                    psm = properties.getProperty("jdbc.statement");
                    String field_string = properties.getProperty("jdbc.fieldsGet");
                    fields = field_string.split(":");
                    break;
                case "text":
                    System.out.println("worker is reading your text config");
                    file_path = properties.getProperty("file_path");
                    field = properties.getProperty("field");
                    split_string = properties.getProperty("split_string");
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
                putToDB();
                break;
            case "text":
                putToText();
        }
    }

    public void putToDB() {
        DateFormat fmt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        int i = 0;

        //连接数据库
        try {
            Class.forName(driverName);
            dbConn = DriverManager.getConnection(dbURL, userName, userPwd);
            sm = dbConn.prepareStatement(psm);
            System.out.println("connect successfully!");
        } catch (Exception e) {
            System.out.println("connect failed!");
            e.printStackTrace();
        }

        //写入数据
        while (true) {
            Map<String, String> row = null;
            try {
                row = blockingQueue.take();

                if (row.get(fields[0]).equals("end-end-end")) {
                    System.out.println("All processed data has been write to database");
                    dbConn.close();
                    break;
                }

                for (int j = 0; j < fields.length; j++) {
                    sm.setObject(j, row.get(fields[j]));
                }
                sm.execute();

                //打印时间
                i += 1;
                if (i % 1000 == 0) {
                    System.out.println("worker\t" + fmt.format(System.currentTimeMillis()) + "=> " + i);
                }

            } catch (Exception e) {
                System.out.println("worker write data failed");
                e.printStackTrace();
            }
        }
    }

    public void putToText(){

        DateFormat fmt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        int i = 0;

        //写入数据
        OutputStreamWriter write = null;
        try {
            write = new OutputStreamWriter(new FileOutputStream(file_path,true));
        } catch (FileNotFoundException e) {
            System.out.println("file not found");
            e.printStackTrace();
        }
        BufferedWriter bufferedWriter = new BufferedWriter(write);
        while (true) {
            Map<String, String> row = null;
            try {
                row = blockingQueue.take();

                String content = row.get(field);
                if (content.equals("end-end-end")) {
                    System.out.println("All processed data has been write to database");
                    bufferedWriter.close();
                    write.close();
                    break;
                }

                System.out.println(content);
                bufferedWriter.write(content+split_string);

                //打印时间
                i += 1;
                if (i % 1000 == 0) {
                    System.out.println("worker\t" + fmt.format(System.currentTimeMillis()) + "=> " + i);
                }

            } catch (Exception e) {
                System.out.println("worker write data failed");
                e.printStackTrace();
            }
        }

    }
}