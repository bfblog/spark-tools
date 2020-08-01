package de.bytefusion.sparktools;

import org.junit.Assert;
import org.junit.Test;

import java.io.*;
import java.util.regex.Pattern;

public class TestRegex {

    @Test
    public void test1() throws FileNotFoundException {
        File accesslog = new File("./samples/access.log");

        String regex = "(?<ip>(([0-9]+)(\\.[0-9]+){3}))\\s(?<d1>[^\\s]+)\\s(?<d2>[^\\s]+)\\s\\[(?<datetime>[^\\]]+)\\]\\s\"(?<request>([^\"]|(?<=\\\\)\")+)\"\\s(?<httpstatus>[0-9]+)\\s(?<size>[0-9]+)\\s\"(?<referrer>([^\"]|(?<=\\\\)\")+)\"\\s\"(?<agent>([^\"]|(?<=\\\\)\")+)\"";
        Pattern p = Pattern.compile(regex);
        RegexWithNamedCaptureGroups named = new RegexWithNamedCaptureGroups(p);
        try(
         BufferedReader  reader = new BufferedReader( new FileReader(accesslog) );
        ) {
            String line = reader.readLine();
            while( line != null ) {
                System.out.println( line );
                if ( named.match(line) ) {
                    
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
