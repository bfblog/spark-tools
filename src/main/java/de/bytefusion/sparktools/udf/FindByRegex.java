package de.bytefusion.sparktools.udf;

import de.bytefusion.sparktools.RegexWithNamedCaptureGroups;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.RowFactory;
import org.apache.spark.sql.api.java.UDF1;

import java.io.Serializable;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class FindByRegex implements UDF1<String, Row>, Serializable {

    private Pattern p;
    transient private RegexWithNamedCaptureGroups ncg;

    public FindByRegex( Pattern p ) {
        this.p = p;
    }

    private void initialize() {
        if ( this.ncg == null ) {
            this.ncg = new RegexWithNamedCaptureGroups(this.p);
        }
    }

    public Row call(String text) {
        List<String> values = null;
        initialize();
        if ( ncg.find(text) ) {
            values = ncg.fields()
                    .stream()
                    .map( name -> ncg.group(name) )
                    .collect(Collectors.toList());
        } else {
            values = ncg.fields()
                    .stream()
                    .map( name -> (String)null )
                    .collect(Collectors.toList());
        }
        Object[] data = values.toArray( new Object[ values.size() ] );
        return RowFactory.create(data);
    }

}
