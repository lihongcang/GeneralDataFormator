import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.lang3.StringEscapeUtils;

class Article{
    public String Title;
    public String Content;

    public Article(String Title,String Content){
        this.Title=Title;
        this.Content=Content;
    }
}
public class GetHtmlContent{
    private static boolean appendMode=true;
    private static int depth=6;
    private static int limitCount=132;
    private static int headEmptyLines=2;
    private static int endLimitCharCount=20;

    //读取GetHtmlContent的配置文件
    public GetHtmlContent(String get_html_content_config_path){
        Properties properties = new Properties();
        try (InputStream input = Producer.class.getResourceAsStream(get_html_content_config_path)) {
            properties.load(input);
            appendMode = Boolean.valueOf(properties.getProperty("appendMode"));
            depth = Integer.valueOf(properties.getProperty("depth"));
            limitCount = Integer.valueOf(properties.getProperty("limitCount"));
            headEmptyLines = Integer.valueOf(properties.getProperty("headEmptyLines"));
            endLimitCharCount = Integer.valueOf(properties.getProperty("endLimitCharCount"));

        } catch (Exception e) {
            System.out.println("error appeared when read the gethtmlcontent config file");
            e.printStackTrace();
        }
    }

    public static Article getArticle(String html){

        if(html.split("\n").length<10){
            html = html.replace(">",">\n");
        }
        String body = "";
        Pattern p = Pattern.compile("(<body.*?</body>)",Pattern.DOTALL);
        Matcher m = p.matcher(html);
        if(m.find()){
            body = m.group(1);
            body = Pattern.compile("<script.*?>.*?</script>",Pattern.DOTALL).matcher(body).replaceAll("");
            //    <div class="recommend-item-box recommend-ad-box"><div id="kp_box_67" data-pid="67" data-track-view='{"mod":"kp_popu_67-809","con":",,"}' data-track-click='{"mod":"kp_popu_67-809","con":",,"}'><script    async="async"    charset="utf-8"    src="https://shared.ydstatic.com/js/yatdk/3.0.1/stream.js"    data-id="6cb24153a03289ff3597c7aab4b69fe9"    data-div-Style="width:100%;"data-tit-Style="margin-bottom: 6px; font-size: 18px; line-height: 24px; color: #3d3d3d;display: inline-block;font-weight:bold;"data-des-Style="font-size: 13px; line-height: 22px; white-space: normal; color: #999;"data-img-Style="float:left;margin-right:15px;width:90px;height:60px;">
            //    </script></div></div> 出了错误
            //body = body.replaceAll("<script.*?>[\\s\\S]*?</script>","");
            body = Pattern.compile("<style.*?>.*?</style>",Pattern.DOTALL).matcher(body).replaceAll("");
            body = Pattern.compile("<!--.*?-->",Pattern.DOTALL).matcher(body).replaceAll("");
            body = body.replaceAll("</a>","</a>\n");
            body = formatTag(body);
        }

        String content = getContent(body);
        String title = getTitle(html);
        Article article = new Article(title,content);

        return article;
    }
    public static String formatTag(String body){
        Pattern p = Pattern.compile("(<.*?>)",Pattern.DOTALL);
        Matcher m = p.matcher(body);
        while(m.find()){
            String temp = m.group(1).replace("\n","");
            //如果使用replaceAll的话，可能因为m.group(1)中包含异常报错
            body = body.replace(m.group(1),temp);
            //这个错误也很难发现
            //body = m.replaceFirst(temp);
        }
        //System.out.println(body);
        return body;
    }
    public  static String getContent(String body){
        String[] orgLines = null;
        String[] lines = null;

        orgLines = body.split("\n");
        lines = new String[orgLines.length];

        for(int i=0;i<orgLines.length;i++){
            String lineInfo = orgLines[i];
            lineInfo = lineInfo.replaceAll("</p>|<br.*?/>","[crlf]");
            lines[i] = lineInfo.replaceAll("<.*?>","").trim();
        }

        StringBuffer sb = new StringBuffer();

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
                    for(int j=startPos;j<=i;j++){
                        sb.append(lines[j]);
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

    //测试
//    public static void main(String[] args){
//        String Url = "https://blog.csdn.net/u014595019/article/details/44035981";
//        StringBuffer sb = new StringBuffer();
//        try{
//            URL url = new URL(Url);
//            BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()));
//            String line;
//            while((line=in.readLine()) != null){
//                sb.append(line);
//                sb.append("\n");
//            }
//        }catch (Exception e){
//            e.printStackTrace();
//        }
//
//        Article article = getArticle(String.valueOf(sb));
//        System.out.println(article.Title);
//    }
}