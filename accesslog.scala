
var df = spark.read.text("/data/samples/access.log");

df = df.withColumn("details", BFTools.regex_match(df.col("value"),"(?<ip>(([0-9]+)(.[0-9]+){3}))\\s(?<identd>[^s]+)\\s(?<user>[^\\s]+)\\s\\[(?<datetime>[^]]+)]\\s\\\"(?<request>((?<type>GET|POST|HEAD|DELETE|OPTIONS|TRACE|PUT|OPTIONS|TRACE) (?<uri>[^\\s]+) (HTTP/(?<httpversion>[^\\s]+))|[^\"]|(?<=)\")+)\"\\s(?<httpstatus>[0-9]+)\\s(?<size>[0-9]+)\\s\\\"(?<referrer>([^\\\"]|(?<=)\")+)\\\"\\s\\\"(?<agent>([^\"]|(?<=)\\\")+)\\\"\\s\\\"(?<xxxx>([^\\\"]|(?<=)\\\")+)\\\""));

df = df.select("details.*")
df = df.withColumn("httpstatus", $"httpstatus".cast(org.apache.spark.sql.types.IntegerType))
df = df.withColumn("size", $"size".cast(org.apache.spark.sql.types.IntegerType))
df = df.withColumn("datetime", to_date($"datetime", "dd/MMM/yyyy:HH:mm:SS Z"))

df.printSchema

df.show