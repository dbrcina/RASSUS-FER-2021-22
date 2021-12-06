@echo off

set COMMAND=%KAFKA_HOME%\bin\windows\kafka-topics.bat

call %COMMAND% --create --topic Command --partitions 1 --replication-factor 1 --zookeeper localhost:2181
call %COMMAND% --create --topic Register --partitions 1 --replication-factor 1 --zookeeper localhost:2181
