#!/bin/sh
cd /home/ec2-user
rm -rf webserver.jar
wget https://s3-eu-west-1.amazonaws.com/cloudprime.irenical.org/webserver.jar
IP=$(curl -s http://169.254.169.254/latest/meta-data/public-ipv4)
chown ec2-user:ec2-user webserver.jar
java -jar webserver.jar IP
