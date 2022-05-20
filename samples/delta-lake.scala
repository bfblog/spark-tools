// Create data DataFrame
val data = spark.range(0, 50000)

// Write the data DataFrame to /delta location
data.write.format("delta").save("/data/samples/delta")

// Append new data to your Delta table
data.write.format("delta").mode("append").save("/data/samples/delta")

// Overwrite your Delta table
data.write.format("delta").mode("overwrite").save("/data/samples/delta")

// Append new data to your Delta table
data.write.format("delta").mode("append").save("/data/samples/delta")
data.write.format("delta").mode("append").save("/data/samples/delta")
data.write.format("delta").mode("append").save("/data/samples/delta")
data.write.format("delta").mode("append").save("/data/samples/delta")

import io.delta.tables._
val deltaTable = DeltaTable.forPath(spark, "/data/samples/delta")
// get the full history of the table
val fullHistoryDF = deltaTable.history()
// get the last 5 operations
val lastOperationDF = deltaTable.history(5)


val path = "/data/samples/delta"
val numFiles = 1

spark.read
 .format("delta")
 .load(path)
 .repartition(numFiles)
 .write
 .option("dataChange", "false")
 .format("delta")
 .mode("overwrite")
 .save(path)
 
// vacuum the table
spark.conf.set("spark.databricks.delta.retentionDurationCheck.enabled","false")

deltaTable.vacuum(0)

spark.conf.set("spark.databricks.delta.retentionDurationCheck.enabled","true")