@echo off

set BIN_DIR=%KAFKA_HOME%\bin\windows
set CONFIG_DIR=%KAFKA_HOME%\config

start "Zookeeper server" "%BIN_DIR%\zookeeper-server-start.bat" %CONFIG_DIR%\zookeeper.properties
timeout /t 3 /nobreak > nul
start "Kafka server" "%BIN_DIR%\kafka-server-start.bat" %CONFIG_DIR%\server.properties
