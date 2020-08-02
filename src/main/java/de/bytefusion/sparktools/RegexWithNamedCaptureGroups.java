package de.bytefusion.sparktools;

import org.apache.log4j.Logger;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import scala.collection.immutable.Stream;

import java.util.*;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class RegexWithNamedCaptureGroups {

    private static Logger log = Logger.getLogger(RegexWithNamedCaptureGroups.class);

    private String[] fields;
    private String[] values;
    private Map<String,Integer> groups = new HashMap<>();
    private Map<String,Integer> fieldIndices = new HashMap<>();
    private Pattern p;
    private boolean match;

    public RegexWithNamedCaptureGroups(Pattern p) {
        this.p = p;
        // extract string value
        String pattern = p.pattern();
        log.info("pattern=" + pattern );
        // regex match all capturing groups
        Pattern p1 = Pattern.compile("\\((\\?(=|<=|!|<!|<((?![=!])[^>]+)>))?");
        // find all captering groups
        Matcher m = p1.matcher(pattern);
        int count=0;
        ArrayList<String> allFields = new ArrayList<>();
        while ( m.find() ) {
            String nonCaptureGroup = m.group(2);
            String groupName = m.group(3);
            if ( nonCaptureGroup != null && groupName==null) {
                String group = pattern.substring(m.start(),m.end());
                // exclude non-capturing groups
                if ( group.startsWith("(?=") ) {
                    log.info( "skip positive lookahead " + pattern.substring(m.start(),m.end()) );
                } else if ( group.startsWith("(?<=") ) {
                    log.info( "skip positive lookbehind " + pattern.substring(m.start(),m.end()) );
                } else if ( group.startsWith("(?!") ) {
                    log.info( "skip negative lookahead " + pattern.substring(m.start(),m.end()) );
                } else if ( group.startsWith("(?<!") ) {
                    log.info( "skip negative lookbehind " + pattern.substring(m.start(),m.end()) );
                } else {
                    log.info( "skip other non-capturing group " + pattern.substring(m.start(),m.end()) );
                }
            } else if ( groupName != null ) {
                count++;
                log.info( "capturing group #" + count + " is named " + groupName + " " + pattern.substring(m.start(),m.end()) );
                // remember groupName and capturing group number
                groups.put(groupName, Integer.valueOf(count));
                // remember list off all fields
                allFields.add(groupName);
            } else {
                count++;
                log.info("skipping unnamed capturing group #" + count);
            }
        }

        for( int index=0; index<allFields.size(); index++ ) {
            this.fieldIndices.put( allFields.get(index), Integer.valueOf(index));
        }

        this.fields = new String[allFields.size()];
        this.fields = allFields.toArray(this.fields);
        this.values = new String[allFields.size()];
        log.info( "fields: " + Arrays.stream( this.fields ).collect(Collectors.joining(",")) );
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

    public Map<String,String> asKeyValuePair() {
        Map<String,String> result = null;
        if ( this.match ) {
            result = new HashMap<String,String>();
            int index=0;
            for( String field : this.fields ) {
                String value = this.values[index];
                result.put(field,value);
                index++;
            }
        }
        return result;
    }

    public Collection<String> fields() {
        return Arrays.asList(this.fields);
    }

    public String group(String name) {
        int index = fieldIndices.get(name).intValue();
        return values[index];
    }
}
