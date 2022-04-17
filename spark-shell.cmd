docker run ^
     -ti ^
     --network=host ^
     --rm ^
     --name=spark-shell ^
     --volume %~dp0/samples:/data/samples ^
     --volume %~dp0/scripts:/scripts ^
     bfblog/spark:latest