[![experimental](http://badges.github.io/stability-badges/dist/experimental.svg)](http://github.com/badges/stability-badges)
# Google App Engine Maven plugin

This Maven plugin allows you to perform Java app management operations.

# Requirements

Maven is required to build the plugin. To download Maven, follow the [instructions](http://maven.apache.org/).

The remaining dependencies are specified in the pom.xml file and should be automatically downloaded when the plugin is built.

# How to use

In your Maven App Engine Java app, add the following plugin to your pom.xml:

```XML
<plugin>
    <groupId>com.google.cloud.tools</groupId>
    <artifactId>gcp-app-maven-plugin</artifactId>
    <version>0.1.0-SNAPSHOT</version>
</plugin>
```

You can now run commands like "mvn gcp-app:deploy" in the root folder of your Java application. (i.e., where the project's pom.xml file is)

# Supported goals

- gcp-app:help
- gcp-app:stage
- gcp-app:deploy
- gcp-app:run
- gcp-app:start
- gcp-app:stop
