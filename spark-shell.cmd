docker run ^
     -ti ^
     --rm ^
     --name=spark-shell ^
     -p 4040:4040 ^
     --volume %~dp0/samples:/data/samples ^
     --volume %~dp0/scripts:/scripts ^
     -e SPARK_DAEMON_MEMORY=10g ^
     bfblog/spark:latest