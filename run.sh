#!/bin/bash
set -a
source .env
set +a
mvn package && java -jar target/tgBot-1.0-SNAPSHOT.jar