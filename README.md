# spark-tools
 
## Regular Expression with named capturing groups

This example uses regular expressions with named capturing groups. Most programming languages support regular expressions and unnamed capturing groups. But in fact, only a few libraries support the named capturing groups. By default, the capturing groups are numbered from 0 ascending. Instead of numbering, named capturing groups assign a name to the group. In this way, names identify a group and the matching content.
As a result, the feature of named capturing groups allows us to split a text into a list of key-value pairs.

    91.227.29.79 - - [12/Dec/2015:18:33:51 +0100] "GET /administrator/ HTTP/1.1" 200 4263 "-" "Mozilla/5.0 (Windows NT 6.0; rv:34.0) Gecko/20100101 Firefox/34.0" "-"

The above example is an extract from a web server log file. Most readers will identify many entities, e.g., the IP address or the timestamp. The regular expression will easily split it into fragments.

    (?<ip>(([0-9]+)(\.[0-9]+){3}))\s(?<identd>[^\s]+)\s(?<user>[^\s]+)\s\[(?<datetime>[^\]]+)\]\s"(?<request>((?<type>GET|POST|HEAD|DELETE|OPTIONS|TRACE|PUT|OPTIONS|TRACE) (?<uri>[^\s]+) (HTTP/(?<httpversion>[^\s]+))|[^"]|(?<=\\)")+)"\s(?<httpstatus>[0-9]+)\s(?<size>[0-9]+)\s"(?<referrer>([^"]|(?<=\\)")+)"\s"(?<agent>([^"]|(?<=\\)")+)"\s"(?<xxxx>([^"]|(?<=\\)")+)"
    
As you can see, human-readable identifiers markup each capturing group.  When our regular expression library can offer both information, we see a list of key-value pairs.

    ip = 91.227.29.79
    identd = - 
    user = .- 
    timestamp = 12/Dec/2015:18:33:51 +0100
    request = GET /administrator/ HTTP/1.1
    type = GET
    uri = /administrator/
    httpversion = 1.1
    httpstatus = 200 
    size = 4263
    referrer = - 
    useragent = Mozilla/5.0 (Windows NT 6.0; rv:34.0) Gecko/20100101 Firefox/34.0
    xxxx = "-"

Did you note the nested capturing groups? The request and its components type, URI, and HTTP version. Do you know a more comfortable way to extract such content?

    Dataset<Row> df = spark.read().text("samples/access.log");
    df
        .withColumn("details", SparkTools.regex(df.col("value"),"(?<ip>(([0-9]+)(\\.[0-9]+){3}))\\s(?<identd>[^\\s]+)\\s(?<user>[^\\s]+)\\s\\[(?<datetime>[^\\]]+)\\]\\s\"(?<request>((?<type>GET|POST|HEAD|DELETE|OPTIONS|TRACE|PUT|OPTIONS|TRACE) (?<uri>[^\\s]+) (HTTP/(?<httpversion>[^\\s]+))|[^\"]|(?<=\\\\)\")+)\"\\s(?<httpstatus>[0-9]+)\\s(?<size>[0-9]+)\\s\"(?<referrer>([^\"]|(?<=\\\\)\")+)\"\\s\"(?<agent>([^\"]|(?<=\\\\)\")+)\"\\s\"(?<xxxx>([^\"]|(?<=\\\\)\")+)\""));
        show();

# Build Java Sources

The development container includes java and maven tools. When using docker, just execute the **developer-shell.cmd** script to enter linux environment on this workspace:

```
   windows> developer-shell.cmd
```

As a result, you will see the shell prompt on current workspace and can start building the sources.

```
   C:\Users\Markus\Documents\GitHub\spark-tools>docker run -ti --network=host --rm --name=kubectl-host --volume C:\Users\Markus\Documents\GitHub\spark-tools\:/workspace --volume C:\Users\Markus\.kube:/home/developer/.kube --volume //var/run/docker.sock:/var/run/docker.sock bfblog/maven-jdk8:latest
/workspace # mvn clean install
[INFO] Scanning for projects...
[INFO]
[INFO] ---------------------< de.bytefusion:spark-tools >----------------------
[INFO] Building spark-tools 1.0-SNAPSHOT
[INFO] --------------------------------[ jar ]---------------------------------
...
[INFO] ------------------------------------------------------------------------
[INFO] BUILD SUCCESS
[INFO] ------------------------------------------------------------------------
[INFO] Total time:  45.186 s
[INFO] Finished at: 2022-06-11T07:20:38Z
[INFO] ------------------------------------------------------------------------
/workspace #
...
```

# References
* https://www.bytefusion.de/2020/08/05/make-it-easy-apache-spark-data-frames-and-regex-power/
* https://www.bytefusion.de/2017/06/10/analyze-access-log-with-apache-spark/
* https://www.bytefusion.de/2022/06/11/apache-spark-delta-lake-examples/
  

