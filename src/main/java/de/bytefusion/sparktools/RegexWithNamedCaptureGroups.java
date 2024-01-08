package de.bytefusion.sparktools;

import org.apache.log4j.Logger;

import java.io.StringWriter;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class RegexWithNamedCaptureGroups {

    private static final Logger log = Logger.getLogger(RegexWithNamedCaptureGroups.class);

    private String[] fields;
    private final String[] values;
    private final Map<String,Integer> groups = new HashMap<>();
    private final Map<String,Integer> fieldIndices = new HashMap<>();
    private final Pattern p;
    private boolean match;

    
    public RegexWithNamedCaptureGroups(Pattern p) {
        this.p = p;
        // extract string value
        String pattern = p.pattern();
        log.info("pattern=" + pattern );
        // regex match all capturing groups
        Pattern p1 = Pattern.compile("(?<!\\\\)\\((\\?(=|<=|!|<!|<((?![=!])[^>]+)>))?");
        // find all capturing groups
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

        // remember field indices
        for( int index=0; index<allFields.size(); index++ ) {
            this.fieldIndices.put( allFields.get(index), Integer.valueOf(index));
        }

        // remember fields
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

    public boolean find( String text ) {
        boolean anyMatch = false;

        // clear buffer
        for( int idx=0; idx<this.values.length; idx++ ) {
            values[idx] = null;
        }

        Matcher m = this.p.matcher(text);
        while ( m.find() ) {
            anyMatch = true;

            // text matches regular expression
            for( int idx=0; idx<this.fields.length; idx++) {
                String fieldName = fields[idx];
                int group = this.groups.get(fieldName).intValue();
                String value = m.group(group);
                if ( value != null ) {
                    values[idx] = value;
                }
            }
        }
        return anyMatch;
    }

    public Map<String, String> asKeyValuePair() {
        Map<String, String> result = null;
        if (this.match) {
            result = new HashMap<>();
            int index = 0;
            for (String field : this.fields) {
                String value = this.values[index];
                result.put(field, value);
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

    /**
     * Format regex in a more human readable format
     *
     * @param regex
     *
     * @result pretty printed visualization of the regex
     */
    public static String prettyPrint( String regex ) {
        // find named and unnamed groups
        Pattern p2 = Pattern.compile("(?<open>(?<!\\\\)\\((\\?(=|<=|!|<!|(?<capture><((?![=!])[^>]+)>)))?)|(?<close>(?<!\\\\\\\\)\\))");
        // apply pattern to incoming regex
        Matcher m = p2.matcher(regex);
        // formatting indent level
        int indent = 0;
        // writer contains pretty printed string
        StringWriter sb = new StringWriter();

        // remember group details here
        Stack<Boolean> lastGroupWasNamedStack = new Stack<Boolean>();
        Stack<Integer> lastGroupEndedAtStack = new Stack<>();
        lastGroupWasNamedStack.push(Boolean.FALSE);
        lastGroupEndedAtStack.push(Integer.valueOf(0));

        // while opening/closing braces are found
        while( m.find() ) {
            int lastEnd = lastGroupEndedAtStack.peek().intValue();
            int curEnd = m.end();
            String fragment = regex.substring(lastEnd,m.start());
            lastGroupEndedAtStack.push( Integer.valueOf(curEnd));
            log.debug("m.start() = " + m.start() + " m.end() = " + m.end() );
            String open = m.group("open");
            String close = m.group("close");
            String capture = m.group("capture");
            // new capture group?
            if ( open != null ) {
                sb.append(fragment);
                // named capture group?
                if ( capture != null ) {
                    // yes, named capture group
                    indent++;
                    lastGroupWasNamedStack.push(Boolean.TRUE);
                    sb.append("\n");
                    for( int i=0; i<indent; i++) {
                        sb.append("...");
                    }
                } else {
                    // no, anonym capture group
                    lastGroupWasNamedStack.push(Boolean.FALSE);
                }
            }
            // extract text between end of last group and beginning of current group
            String str = regex.substring(m.start(), m.end());
            sb.append( str );

            // closing capture group?
            if ( close != null ) {
                sb.append(fragment);
                // closing a named capture group?
                if ( lastGroupWasNamedStack.peek().booleanValue() ) {
                    indent--;
                }
                lastGroupWasNamedStack.pop();
                sb.append("");
            }
        }  // while

        return sb.toString();
    }
}
