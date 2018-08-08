#!/usr/bin/env bash

exit_if_err(){
  if [ $? -ne 0 ]; then
    echo "exit"
    exit 1
  fi
}

image_name=registry.bst-1.cns.bstjpc.com:5000/cloudpi/spinnaker-clouddriver
image_tag=$(date +"%Y%m%d-%H%M")

GRADLE_USER_HOME=../.clouddriver-cache ./gradlew buildDeb -x test
exit_if_err
image=${image_name}:${image_tag}

docker build -t ${image} .
exit_if_err
docker push ${image}
exit_if_err