cd github/app-maven-plugin

wget https://dl.google.com/dl/cloudsdk/channels/rapid/GoogleCloudSDKInstaller.exe
start /WAIT GoogleCloudSDKInstaller.exe /S /noreporting /nostartmenu /nodesktop /logtofile /D=T:\google
call t:\google\google-cloud-sdk\bin\gcloud.cmd components copy-bundled-python>>python_path.txt && SET /p CLOUDSDK_PYTHON=<python_path.txt && DEL python_path.txt
call t:\google\google-cloud-sdk\bin\gcloud.cmd components update --quiet
call t:\google\google-cloud-sdk\bin\gcloud.cmd components install app-engine-java --quiet
set GOOGLE_CLOUD_SDK_HOME=t:\google\google-cloud-sdk

wget http://www-us.apache.org/dist/maven/maven-3/3.3.9/binaries/apache-maven-3.3.9-bin.zip
unzip apache-maven-3.3.9-bin.zip

apache-maven-3.3.9/bin/mvn clean install cobertura:cobertura -B -U
REM curl -s https://codecov.io/bash | bash

exit /b %ERRORLEVEL%
