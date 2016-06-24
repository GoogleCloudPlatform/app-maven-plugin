[![experimental](http://badges.github.io/stability-badges/dist/experimental.svg)](http://github.com/badges/stability-badges)
[![build status image](https://travis-ci.org/GoogleCloudPlatform/app-maven-plugin.svg?branch=master)](https://travis-ci.org/GoogleCloudPlatform/app-maven-plugin)
# Google App Engine Maven plugin

This Maven plugin allows you to manage Java applications running on Google App Engine.

# Requirements

Maven is required to build the plugin. To download Maven, follow the [instructions](http://maven.apache.org/).

The remaining dependencies are specified in the pom.xml file and should be automatically downloaded when the plugin is built.

# How to use

In your Maven App Engine Java app, add the following plugin to your pom.xml:

```XML
<plugin>
    <groupId>com.google.cloud.tools</groupId>
    <artifactId>appengine-maven-plugin</artifactId>
    <version>0.1.0-SNAPSHOT</version>
</plugin>
```

You can now run commands like "mvn appengine:deploy" in the root folder of your Java application. (i.e., where the project's pom.xml file is)

# Supported goals

- appengine:help
- appengine:stage
- appengine:deploy
- appengine:run
- appengine:start
- appengine:stop
