import org.ansj.domain.Result;
import org.ansj.domain.Term;
import org.ansj.recognition.impl.StopRecognition;
import org.ansj.splitWord.analysis.ToAnalysis;

import java.io.*;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.util.*;

public class RemoveRepeat {
    private static Map<String, LinkedList<String>> map = new HashMap<>();

    public RemoveRepeat(String remove_repeat_config_path){
        Properties properties = new Properties();
        try (InputStream input = Producer.class.getResourceAsStream(remove_repeat_config_path)) {
            properties.load(input);
        } catch (Exception e) {
            System.out.println("error appeared when read the config file");
            e.printStackTrace();
        }
    }
    @SuppressWarnings("unchecked")
    public static boolean removeRepeat(String content) {
        boolean flagn = true;
        int len_content = content.length();

        if(len_content>500){
            content = content.substring(0,500);
        }
        content = content.replaceAll("[^\u4E00-\u9FA5a-zA-Z]", "");


        SimHash hash = new SimHash(content,64);
        String complete_hash = hash.getStrSimHash()+hash.getStrSimHash2();
        LinkedList<String> nodes;
        LinkedList<String> nodes_temp;
        String temp;
        for(int i=0;i<8;i++){
            temp = complete_hash.substring( 16*i,16*(i+1) );
            nodes = map.get(temp);
            if(nodes == null){
                nodes = new LinkedList<>();
                nodes.add(complete_hash+"\t"+len_content);
                map.put(temp,nodes);
                return false;
            }else{
                nodes_temp = (LinkedList<String>)nodes.clone();

                for(String node: nodes_temp ){
                    if(hash.getDistance(complete_hash.substring(0,64),node.substring(0,64)) < 2
                            && hash.getDistance(complete_hash.substring(64,128),node.substring(64,128)) < 2
                            && (len_content-Integer.valueOf(node.split("\t")[1])) > -50
                            && (len_content-Integer.valueOf(node.split("\t")[1]))< 50 ){

                        return true;
                    }
                }
                nodes.add(complete_hash+"\t"+len_content);
                return false;
            }
        }
        return true;
    }
}

class SimHash {
    private String tokens;
    private BigInteger intSimHash;
    private String strSimHash;
    private int hashbits = 64;
    private String strSimHash2;
    private BigInteger intSimHash2;

    public SimHash(String tokens, int hashbits) {
        this.tokens = tokens;
        this.hashbits = hashbits;
        this.intSimHash = this.simHash();
        this.intSimHash2 = this.simHash2();
    }


    public BigInteger simHash() {
        final int[] v = new int[this.hashbits];

        StopRecognition filter = new StopRecognition();
        filter.insertStopNatures("u"); //过滤词性
        filter.insertStopNatures("r");
        try{
            BufferedReader stopdic = new BufferedReader(new InputStreamReader(new FileInputStream(new File("./library/stop.dic"))));
            String lineTxt;
            while((lineTxt=stopdic.readLine())!=null){
                filter.insertStopWords(lineTxt);
            }
        }catch(Exception e){
            e.printStackTrace();
        }

        Result result = ToAnalysis.parse(tokens).recognition(filter);
        List<Term> terms = result.getTerms();
        for (int j = 0; j < terms.size(); j++) {

            String word = terms.get(j).toString().split("/")[0];
            //final BigInteger t = this.hash(word);
            BigInteger t=null;
            try {
                MessageDigest md = MessageDigest.getInstance("MD5");
                byte[] wordData = word.getBytes();
                md.update(wordData);
                t = new BigInteger(md.digest());
            }catch (Exception e){
                System.out.println("md5 error");
                e.printStackTrace();
            }


            //向量加权都为1
            for (int i = 0; i < this.hashbits; i++) {
                final BigInteger bitmask = new BigInteger("1").shiftLeft(i);
                if (t.and(bitmask).signum() != 0) {
                    v[i] += 1;
                } else {
                    v[i] -= 1;
                }
            }

        }

        BigInteger fingerprint = new BigInteger("0");
        final StringBuffer simHashBuffer = new StringBuffer();
        for (int i = 0; i < this.hashbits; i++) {
            if (v[i] >= 0) {
                fingerprint = fingerprint.add(new BigInteger("1").shiftLeft(i));
                simHashBuffer.append("1");
            } else {
                simHashBuffer.append("0");
            }
        }

        this.strSimHash = simHashBuffer.toString();
        return fingerprint;
    }

    public BigInteger simHash2() {
        final int[] v = new int[this.hashbits];

        StopRecognition filter = new StopRecognition();
        filter.insertStopNatures("u"); //过滤词性
        filter.insertStopNatures("r");
        try{
            BufferedReader stopdic = new BufferedReader(new InputStreamReader(new FileInputStream(new File("./library/stop.dic"))));
            String lineTxt;
            while((lineTxt=stopdic.readLine())!=null){
                filter.insertStopWords(lineTxt);
            }
        }catch(Exception e){
            e.printStackTrace();
        }

        Result result = ToAnalysis.parse(tokens).recognition(filter);
        List<Term> terms = result.getTerms();
        for (int j = 0; j < terms.size(); j++) {

            String word = terms.get(j).toString().split("/")[0];
            //final BigInteger t =  BigInteger.valueOf(MurmurHash.hash64(word)) ;
            BigInteger t=null;
            try {
                MessageDigest md = MessageDigest.getInstance("SHA");
                byte[] wordData = word.getBytes();
                md.update(wordData);
                t = new BigInteger(md.digest());
            }catch (Exception e){
                System.out.println("SHA error");
                e.printStackTrace();
            }


            for (int i = 0; i < this.hashbits; i++) {
                final BigInteger bitmask = new BigInteger("1").shiftLeft(i);
                if (t.and(bitmask).signum() != 0) {
                    v[i] += 1;
                } else {
                    v[i] -= 1;
                }
            }

        }

        BigInteger fingerprint = new BigInteger("0");
        final StringBuffer simHashBuffer = new StringBuffer();
        for (int i = 0; i < this.hashbits; i++) {
            if (v[i] >= 0) {
                fingerprint = fingerprint.add(new BigInteger("1").shiftLeft(i));
                simHashBuffer.append("1");
            } else {
                simHashBuffer.append("0");
            }
        }

        this.strSimHash2 = simHashBuffer.toString();
        return fingerprint;
    }

    private BigInteger hash(String source) {
        if (source == null || source.length() == 0) {
            return new BigInteger("0");
        } else {
            char[] sourceArray = source.toCharArray();
            BigInteger x = BigInteger.valueOf(((long) sourceArray[0]) << 7);
            BigInteger m = new BigInteger("1000003");
            BigInteger mask = new BigInteger("2").pow(this.hashbits).subtract(
                    new BigInteger("1"));
            for (char item : sourceArray) {
                BigInteger temp = BigInteger.valueOf((long) item);
                x = x.multiply(m).xor(temp).and(mask);
            }
            x = x.xor(new BigInteger(String.valueOf(source.length())));
            if (x.equals(new BigInteger("-1"))) {
                x = new BigInteger("-2");
            }
            return x;
        }
    }

    public int getDistance(String str1, String str2) {
        int distance;
        if (str1.length() != str2.length()) {
            distance = -1;
        } else {
            distance = 0;
            for (int i = 0; i < str1.length(); i++) {
                if (str1.charAt(i) != str2.charAt(i)) {
                    distance++;
                }
            }
        }
        return distance;
    }

    public String getStrSimHash(){
        return strSimHash;
    }
    public String getStrSimHash2(){
        return strSimHash2;
    }

}
