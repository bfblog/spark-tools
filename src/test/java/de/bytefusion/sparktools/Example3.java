package de.bytefusion.sparktools;

import org.apache.spark.SparkConf;
import org.apache.spark.SparkContext;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;
import org.apache.spark.sql.functions;

public class Example3 {
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

        Dataset<Row> df = spark.read().text("samples/access.log");
        df = df.withColumn("filename", functions.input_file_name());
        df.show(10,1000);

        Dataset<Row> df2 = df.withColumn("details", BFTools.regex_find(df.col("value"),"(?<ip>[0-9]+(\\.[0-9]+){3})|(?<method>GET|POST|HEAD)|(?<index>index\\.php)"));

        df2.show();
        df2.printSchema();
        df2.select("details.*").show();


    }
}
