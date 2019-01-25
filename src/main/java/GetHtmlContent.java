import java.io.InputStream;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.lang3.StringEscapeUtils;

public class GetHtmlContent{
    private static boolean appendMode=true;
    private static int depth=6;
    private static int limitCount=132;
    private static int headEmptyLines=2;
    private static int endLimitCharCount=20;

    //读取GetHtmlContent的配置文件
    public GetHtmlContent(){
        Properties properties = new Properties();
        try (InputStream input = Producer.class.getResourceAsStream("config_GetHtmlContent.properties")) {
            properties.load(input);
            appendMode = Boolean.valueOf(properties.getProperty("appendMode"));
            depth = Integer.valueOf(properties.getProperty("depth"));
            limitCount = Integer.valueOf(properties.getProperty("limitCount"));
            headEmptyLines = Integer.valueOf(properties.getProperty("headEmptyLines"));
            endLimitCharCount = Integer.valueOf(properties.getProperty("endLimitCharCount"));

        } catch (Exception e) {
            System.out.println("error appeared when read the config file");
            e.printStackTrace();
        }
    }
    public  static String getContent(String body){
        String[] orgLines = null;
        String[] lines = null;

        orgLines = body.split("\n");
        lines = new String[orgLines.length];

        for(int i=0;i<orgLines.length;i++){
            String lineInfo = orgLines[i];
            lineInfo = lineInfo.replaceAll("</p>|<br.*?>","[crlf]");
            lines[i] = lineInfo.replaceAll("<.*?>","").trim();
        }

        StringBuilder sb = new StringBuilder();
        StringBuilder orgsb = new StringBuilder();

        int preTextLen = 0;
        int startPos = -1;
        for(int i=0;i<lines.length-depth;i++){
            int len=0;
            for(int j=0;j<depth;j++){
                len+=lines[i+j].length();
            }

            if(startPos == -1){
                if(preTextLen > limitCount && len>0){
                    int emptyCount = 0;
                    for(int j=i-1;j>0;j--){
                        if(lines[j]==""||lines[j]==null){
                            emptyCount++;
                        }
                        else{
                            emptyCount=0;
                        }
                        if(emptyCount == headEmptyLines){
                            startPos = j+headEmptyLines;
                            break;
                        }
                    }

                    if(startPos == -1){
                        startPos = i;
                    }
                    for(int j=startPos;j<i;j++){
                        sb.append(lines[j]);
                        orgsb.append(orgLines[j]);
                    }
                }
            }
            else{
                if(len<=endLimitCharCount&&preTextLen<endLimitCharCount){
                    if(!appendMode){
                        break;
                    }
                    startPos = -1;
                }

                sb.append(lines[i]);
                orgsb.append(orgLines[i]);
            }
            preTextLen = len;
        }

        String result = sb.toString();
        result = result.replace("[crlf]","\n");
        result = StringEscapeUtils.escapeHtml4(result);
        return result;
    }
    public static String getTitle(String html){
        String title = "";
        Pattern p = Pattern.compile("<title>([\\s\\S]*?)</title>");
        Matcher m = p.matcher(html);
        if(m.find()){
            title = m.group(1);
        }
        p = Pattern.compile("<h1.*?>([\\s\\S]*?)</h1>");
        m = p.matcher(html);
        if(m.find()){
            String h1 = m.group(1);
            if((h1!=null&&h1!="")&&title.startsWith(h1)){
                title = h1;
            }
        }
        return title;
    }
    public static Article getArticle(String html){

        if(html.split("\n").length<10){
            html = html.replace(">",">\n");
        }

        String body = "";
        Pattern p = Pattern.compile("<body.*?>([\\s\\S]*?)</body>");
        Matcher m = p.matcher(html);
        if(m.find()){
            body = m.group(1);
            body = body.replaceAll("<script.*?>[\\s\\S]*?</script>","");
            body = body.replaceAll("<style.*?>[\\s\\S]*?</style>","");
            body = body.replaceAll("<!--.*?-->","");
            body = body.replaceAll("</a>","</a>\n");
            body = formatTag(body);
        }

        String content;
        String title;
        content = getContent(body);
        title = getTitle(html);
        Article article = new Article(title,content);

        return article;
    }
    public static String formatTag(String body){
        Pattern p = Pattern.compile("(<.*?>)",Pattern.DOTALL);
        Matcher m = p.matcher(body);
        while(m.find()){
            String temp = m.group(1).replaceFirst("\n","");
            //System.out.println(temp);
            body = body.replaceAll(m.group(1),temp);
            //这个错误也很难发现
            //body = m.replaceFirst(temp);
        }
        //System.out.println(body);
        return body;
    }
    public static class Article{
        public String Title;
        public String Content;

        public Article(String Title,String Content){
            this.Title=Title;
            this.Content=Content;
        }
    }
}