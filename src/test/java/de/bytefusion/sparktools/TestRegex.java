package de.bytefusion.sparktools;

import org.apache.log4j.Logger;
import org.junit.Assert;
import org.junit.Test;

import java.io.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TestRegex {

    private static Logger log = Logger.getLogger( TestRegex.class );

    private final static Pattern REGEX_ACCESSLOG1 =  Pattern.compile("(?<ip>(([0-9]+)(\\.[0-9]+){3}))\\s(?<d1>[^\\s]+)\\s(?<d2>[^\\s]+)\\s\\[(?<datetime>[^\\]]+)\\]\\s\"(?<request>([^\"]|(?<=\\\\)\")+)\"\\s(?<httpstatus>[0-9]+)\\s(?<size>[0-9]+)\\s\"(?<referrer>([^\"]|(?<=\\\\)\")+)\"\\s\"(?<agent>([^\"]|(?<=\\\\)\")+)\"\\s\"(?<xxxx>([^\"]|(?<=\\\\)\")+)\"");

    @Test
    public void test2() {
        Pattern p = REGEX_ACCESSLOG1;
        Matcher m = p.matcher("109.169.248.247 - - [12/Dec/2015:18:25:11 +0100] \"GET /administrator/ HTTP/1.1\" 200 4263 \"-\" \"Mozilla/5.0 (Windows NT 6.0; rv:34.0) Gecko/20100101 Firefox/34.0\" \"-\"");
//        Pattern p = Pattern.compile("(?<ip>(([0-9]+)(\\.[0-9]+){3}))\\s(?<d1>[^\\s]+)\\s(?<d2>[^\\s]+)\\s\\[(?<datetime>[^\\]]+)\\]\\s\"(?<request>([^\"]|(?<=\\\\)\")+)\"\\s(?<httpstatus>[0-9]+)\\s(?<size>[0-9]+)\\s\"(?<referrer>([^\"]|(?<=\\\\)\")+)\"\\s\"(?<agent>([^\"]|(?<=\\\\)\")+)\"\\s\"(?<xxxx>([^\"]|(?<=\\\\)\")+)");
//        Matcher m = p.matcher("109.169.248.247 - - [12/Dec/2015:18:25:11 +0100] \"GET /administrator/ HTTP/1.1\" 200 4263 \"-\" \"Mozilla/5.0 (Windows NT 6.0; rv:34.0) Gecko/20100101 Firefox/34.0\" \"-\"\n");
        Assert.assertTrue("no match", m.matches());
    }

    @Test
    public void test1() throws FileNotFoundException {
        File accesslog = new File("./samples/access.log");

        Pattern p = REGEX_ACCESSLOG1;
        RegexWithNamedCaptureGroups named = new RegexWithNamedCaptureGroups(p);
        try(
         BufferedReader  reader = new BufferedReader( new FileReader(accesslog) );
        ) {
            String line = reader.readLine();
            while( line != null ) {
                System.out.println( line );
                if ( named.match(line) ) {
                    log.info( named.asKeyValuePair() );
                }
                line = reader.readLine();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        Assert.assertTrue( "accesslog exists", accesslog.exists() );

        System.out.println("Hello world!");
    }
}
