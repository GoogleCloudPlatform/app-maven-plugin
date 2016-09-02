![beta](https://img.shields.io/badge/release%20status-beta-yellow.svg)
[![Build Status](http://travis-ci.org/GoogleCloudPlatform/app-maven-plugin.svg)](http://travis-ci.org/GoogleCloudPlatform/app-maven-plugin)
# Google App Engine Maven plugin (BETA)

This Maven plugin provides goals to build and deploy Google App Engine applications.

# Reference Documentation

App Engine Standard Environment:
* [Using Apache Maven and the App Engine Plugin (standard environment)](https://cloud.google.com/appengine/docs/java/tools/using-maven)
* [App Engine Maven Plugin Goals and Parameters (standard environment)](https://cloud.google.com/appengine/docs/java/tools/maven-reference)

App Engine Flexible Environment:
* [Using Apache Maven and the App Engine Plugin (flexible environment)](https://cloud.google.com/appengine/docs/flexible/java/using-maven)
* [App Engine Maven Plugin Goals and Parameters (flexible environment)](https://cloud.google.com/appengine/docs/flexible/java/maven-reference)

# Requirements

[Maven](http://maven.apache.org/) is required to build and run the plugin.

You must have [Google Cloud SDK](https://cloud.google.com/sdk/) installed.

Cloud SDK app-engine-java component is also required. Install it by running:

    gcloud components install app-engine-java

Login and configure Cloud SDK:

    gcloud init

# How to use

In your Maven App Engine Java app, add the following plugin to your pom.xml:

```XML
<plugin>
    <groupId>com.google.cloud.tools</groupId>
    <artifactId>appengine-maven-plugin</artifactId>
    <version>0.1.2</version>
</plugin>
```

You can now run commands like `mvn appengine:deploy` in the root folder of your Java application.

# Supported goals
- appengine:help
- appengine:stage
- appengine:deploy

Dev App Server goals for standard environment apps:
- appengine:run
- appengine:start 
- appengine:stop

Goal documentation is available by running:

    mvn appengine:help -Ddetail=true -Dgoal=[goal]
