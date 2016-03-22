#!/bin/sh
cd /home/ec2-user
rm -rf webserver.jar
wget https://s3-eu-west-1.amazonaws.com/cloudprime.irenical.org/webserver.jar
chown ec2-user:ec2-user webserver.jar
java -jar webserver.jar
