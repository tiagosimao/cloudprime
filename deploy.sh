#!/bin/sh
mvn clean package
s3cmd --acl-public --guess-mime-type put webserver/target/webserver.jar s3://cloudprime.irenical.org/webserver.jar
s3cmd --acl-public --guess-mime-type put loadbalancer/target/loadbalancer.jar s3://cloudprime.irenical.org/loadbalancer.jar
s3cmd --acl-public --guess-mime-type put ec2/boot.sh s3://cloudprime.irenical.org/boot.sh
