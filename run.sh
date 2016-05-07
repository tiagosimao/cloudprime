#!/bin/sh
mvn clean package
java -jar -Daws.accessKeyId="$1" -Daws.secretKey="$2" loadbalancer/target/loadbalancer.jar


