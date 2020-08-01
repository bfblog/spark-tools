package de.bytefusion.sparktools;

import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class RegexWithNamedCaptureGroups {

    private static Logger log = Logger.getLogger(RegexWithNamedCaptureGroups.class);

    private String[] fields;
    private String[] values;
    private Map<String,Integer> groups = new HashMap<>();
    private Pattern p;
    private boolean match;

    public RegexWithNamedCaptureGroups(Pattern p) {
        this.p = p;
        // extract string value
        String pattern = p.pattern();
        log.info("pattern=" + pattern );
        // regex match all capturing groups
        Pattern p1 = Pattern.compile("\\((\\?<([^(>=!)]+)>)?");
        // find all captering groups
        Matcher m = p1.matcher(pattern);
        int count=0;
        ArrayList<String> allFields = new ArrayList<>();
        while ( m.find() ) {
            count++;
            if ( m.groupCount() == 2 ) {
                String groupName = m.group(2);
                log.info( "capturing group #" + count + " is named " + groupName );
                // remember groupName and capturing group number
                groups.put(groupName, Integer.valueOf(count));
                // remember list off all field
                allFields.add(groupName);
            } else {
                log.info("skipping unnamed capturing group #" + count);
            }
        }
        this.fields = new String[allFields.size()];
        this.values = new String[allFields.size()];
        log.info( "fields: " + Arrays.stream( this.values ).collect(Collectors.joining(",")) );
    }

    public boolean match( String text ) {
        // clear buffer
        for( int idx=0; idx<this.values.length; idx++ ) {
            values[idx] = null;
        }
        // regex match
        Matcher m = this.p.matcher(text);
        if ( this.match = m.matches() ) {
            // text matches regular expression
            for( int idx=0; idx<this.fields.length; idx++) {
                String fieldName = fields[idx];
                int group = this.groups.get(fieldName).intValue();
                String value = m.group(group);
                values[idx] = value;
            }
        }
        return this.match;
    }
}
