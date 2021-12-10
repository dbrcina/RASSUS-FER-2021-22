@echo off

set COMMAND=%KAFKA_HOME%\bin\windows\kafka-consumer-groups.bat

call %COMMAND% --all-groups --all-topics --reset-offsets --to-latest --bootstrap-server localhost:9092