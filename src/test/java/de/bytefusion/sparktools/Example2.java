package de.bytefusion.sparktools;

import org.apache.spark.SparkConf;
import org.apache.spark.SparkContext;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;
import org.apache.spark.sql.functions;

/**
 * Example splitting an application log file to data frame with columns loglevel, datetime, thread, class and message.
 */
public class Example2 {
    public static void main(String[] args) {

        SparkConf sparkConf =
                new SparkConf()
                        .setMaster("local")
                        .setAppName("regex");

        SparkSession spark =
                SparkSession
                        .builder()
                        .config(sparkConf)
                        .getOrCreate();

        SparkContext sc = spark.sparkContext();

        // load sample application log
        Dataset<Row> df = spark.read().text("samples/application.log");

        // add filename to dataframe
        df = df.withColumn("filename", functions.input_file_name());

        // show contents before regex
        df.show(10,1000);

        // apply regular expression with named capture groups
        Dataset<Row> df2 = df.withColumn("x", BFTools.regex_match(df.col("value"),"(?<loglevel>[A-Z]+)\\s+(?<datetime>[^\\s]+\\s[^\\s]+)\\s+\\[(?<thread>[^\\]]+)\\]\\s(?<class>[^\\s]+)\\s+(?<message>.*)"));

        // show contents with additional columns from regex
        df2.show();
        df2.printSchema();

        // filter out non matching lines, group by class and count number of entries
        df2.filter( df2.col("x.datetime").isNotNull())
                .select("x.*")
                .groupBy("class")
                .agg(functions.count("*").as("count"))
                .orderBy(functions.col("count").desc())
                .show();
    }
}
