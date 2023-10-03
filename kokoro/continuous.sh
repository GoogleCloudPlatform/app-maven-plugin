#!/bin/bash

# Fail on any error.
set -e
# Display commands to stderr.
set -x

sudo /opt/google-cloud-sdk/bin/gcloud components update --quiet
sudo /opt/google-cloud-sdk/bin/gcloud components install app-engine-java --quiet

cd github/app-maven-plugin
./mvnw clean install -B -U
# bash <(curl -s https://codecov.io/bash)
