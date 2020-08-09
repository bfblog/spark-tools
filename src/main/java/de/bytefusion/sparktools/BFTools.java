package de.bytefusion.sparktools;

import de.bytefusion.sparktools.udf.SplitByRegex;
import org.apache.spark.sql.Column;
import org.apache.spark.sql.expressions.UserDefinedFunction;
import org.apache.spark.sql.types.DataTypes;
import org.apache.spark.sql.types.StructField;
import org.apache.spark.sql.types.StructType;

import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static org.apache.spark.sql.functions.udf;

/**
 * Collection of spark user defined functions.
 */
public class BFTools {

    /**
     * Using regex capturing groups to create new data frame columns.
     * Apply regex to target column. Target column is of type string. Any named capturing group
     * (&lt; ? name &gt; ...) creates a new column of type string. If regex matches target, than capture
     * group matches are copied to the new columns.
     *
     * Example: (?&lt;group1&gt;[a-z]*)\s(?&lt;group2&gt;[a-z]+)
     *
     * source: "hello world"
     * target: group1=hello group2=world
     *
     * @param c      target column
     * @param regex  regular expression with naming capturing groups
     *
     * @return new data frame column
    */
    public static Column regex( Column c, String regex ) {

        Pattern p = Pattern.compile(regex);
        RegexWithNamedCaptureGroups p1 = new RegexWithNamedCaptureGroups(p);
        List<StructField> fields = p1.fields()
                .stream()
                .map(field -> DataTypes.createStructField(field, DataTypes.StringType, true))
                .collect(Collectors.toList());
        StructField[] sf = new StructField[fields.size()];
        sf = fields.toArray(sf);
        StructType struct = DataTypes.createStructType(sf);

        UserDefinedFunction myUDF = udf(new SplitByRegex(p), struct);

        return myUDF.apply(c);
    }

    private static UserDefinedFunction regex(String regex) {
        Pattern p = Pattern.compile(regex);
        RegexWithNamedCaptureGroups p1 = new RegexWithNamedCaptureGroups(p);
        List<StructField> fields = p1.fields()
                .stream()
                .map(field -> DataTypes.createStructField(field, DataTypes.StringType, true))
                .collect(Collectors.toList());
        StructField[] sf = new StructField[fields.size()];
        sf = fields.toArray(sf);
        StructType struct = DataTypes.createStructType(sf);
        return udf( new SplitByRegex( p ), struct);
    }

}
