import java.io.InputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.BlockingQueue;
import java.sql.*;

public class Producer implements Runnable{
    //保存数据的阻塞队列,保存字段的字符串数组
    private BlockingQueue<Map<String,String>> blockingQueue;
    private String[] fields;
    //数据库连接参数
    private String driverName, dbURL, userName, userPwd,psm;
    private Connection dbConn = null;
    private PreparedStatement sm = null;
    private ResultSet rs = null;
    //打印参数
    private String echonum;

    public Producer(BlockingQueue<Map<String, String>> blockingQueue) {

        Properties properties = new Properties();

        try (InputStream input = Producer.class.getResourceAsStream("config.properties")) {
            properties.load(input);

            //初始化数据库配置
            driverName = properties.getProperty("jdbc.driver");
            dbURL = properties.getProperty("jdbc.url");
            userName = properties.getProperty("jdbc.user");
            userPwd = properties.getProperty("jdbc.pass");
            psm = properties.getProperty("jdbc.statement");

            //初始化阻塞队列
            this.blockingQueue = blockingQueue;
            //初始化字段,请确保你的字段使用:分割
            String fieldstring = properties.getProperty("jdbc.fieldsGet");
            fields = fieldstring.split(":");
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
                if (i % Integer.valueOf(echonum) == 0) {
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
            System.out.println("query failed!");
            e.printStackTrace();
        }

    }
}