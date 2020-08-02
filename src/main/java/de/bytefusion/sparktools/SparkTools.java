package de.bytefusion.sparktools;

import de.bytefusion.sparktools.udf.SplitByRegex;
import org.apache.spark.sql.Column;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.api.java.UDF1;
import org.apache.spark.sql.expressions.UserDefinedFunction;
import org.apache.spark.sql.types.DataTypes;
import org.apache.spark.sql.types.StructField;
import org.apache.spark.sql.types.StructType;

import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static org.apache.spark.sql.functions.udf;

public class SparkTools {

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

    public static UserDefinedFunction regex(String regex) {
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
