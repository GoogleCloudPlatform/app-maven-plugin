cd github/app-maven-plugin

call gcloud.cmd components update --quiet
call gcloud.cmd components install app-engine-java --quiet

mvn clean install cobertura:cobertura -B -U
REM curl -s https://codecov.io/bash | bash

exit /b %ERRORLEVEL%
