![project status image](https://img.shields.io/badge/stability-stable-brightgreen.svg)
[![Unit Tests](https://github.com/GoogleCloudPlatform/app-maven-plugin/actions/workflows/unit-tests.yaml/badge.svg)](https://github.com/GoogleCloudPlatform/app-maven-plugin/actions/workflows/unit-tests.yaml)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.google.cloud.tools/appengine-maven-plugin/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.google.cloud.tools/appengine-maven-plugin)
# Google App Engine Maven plugin

This Maven plugin provides goals to build and deploy Google App Engine applications.

# Requirements

[Maven](http://maven.apache.org/) is required to build and run the plugin.

[Google Cloud SDK](https://cloud.google.com/sdk/) is required but will be
automatically installed by the plugin.

# How to use

In your Maven App Engine Java app, add the following plugin to your pom.xml:

```XML
<plugin>
    <groupId>com.google.cloud.tools</groupId>
    <artifactId>appengine-maven-plugin</artifactId>
    <version>2.4.4</version>
</plugin>
```

You can now run commands like `mvn package appengine:deploy` in the root folder of your Java application.

## Goals and Configuration

Please see the [USER GUIDE](USER_GUIDE.md) for a full list of supported goals and configuration
options.
* [USER\_GUIDE for `app.yaml` based projects](USER_GUIDE.md#app-engine-appyaml-based-projects)
* [USER\_GUIDE for `appengine-web.xml` based projects](USER_GUIDE.md#app-engine-appengine-webxml-based-projects)

# Reference Documentation

App Engine Standard Environment:
* [(Java 8) Using Apache Maven and the App Engine Plugin (standard environment)](https://cloud.google.com/appengine/docs/legacy/standard/java/using-maven)
* [(Java 8) App Engine Maven Plugin Goals and Parameters (standard environment)](https://cloud.google.com/appengine/docs/legacy/standard/java/maven-reference)
* [Using Apache Maven and the App Engine Plugin (standard environment)](https://cloud.google.com/appengine/docs/standard/java-gen2/using-maven)
* [App Engine Maven Plugin Goals and Parameters (standard environment)](https://cloud.google.com/appengine/docs/standard/java-gen2/maven-reference)

App Engine Flexible Environment:
* [Using Apache Maven and the App Engine Plugin (flexible environment)](https://cloud.google.com/appengine/docs/flexible/java/using-maven)
* [App Engine Maven Plugin Goals and Parameters (flexible environment)](https://cloud.google.com/appengine/docs/flexible/java/maven-reference)
