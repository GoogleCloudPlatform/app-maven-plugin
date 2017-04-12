![project status image](https://img.shields.io/badge/stability-stable-brightgreen.svg)
[![Build Status](http://travis-ci.org/GoogleCloudPlatform/app-maven-plugin.svg)](http://travis-ci.org/GoogleCloudPlatform/app-maven-plugin)
# Google App Engine Maven plugin

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
    <version>1.2.1</version>
</plugin>
```

You can now run commands like `mvn appengine:deploy` in the root folder of your Java application.

# Supported goals

Goal | Description
--- | ---
appengine:stage|Generates an application directory for deployment.
appengine:deploy|Stages and deploys an application to App Engine.
appengine:deployCron|Stage and deploy cron.yaml to Google App Engine standard or flexible environment.
appengine:deployDispatch|Stage and deploy dispatch.yaml to Google App Engine standard or flexible environment.
appengine:deployDos|Stage and deploy dos.yaml to Google App Engine standard or flexible environment.
appengine:deployIndex|Stage and deploy index.yaml to Google App Engine standard or flexible environment.
appengine:deployQueue|Stage and deploy queue.yaml to Google App Engine standard or flexible environment.
appengine:run|Runs the App Engine local development server. *(App Engine Standard Only)*
appengine:start|Starts running the App Engine devserver asynchronously and then returns to the command line. When this goal runs, the behavior is the same as the run goal except that Maven continues processing goals and exits after the server is up and running. *(App Engine Standard Only)*
appengine:stop|Stops a running App Engine web development server. *(App Engine Standard Only)*
appengine:genRepoInfoFile|Generates source context files for use by Stackdriver Debugger.
appengine:help|Displays help information on the plugin. Use `mvn appengine:help -Ddetail=true -Dgoal=[goal]` for detailed goal documentation.

To automatically run the `appengine:genRepoInfoFile` goal during the Maven build workflow, add the following to your plugin executions section:

```XML
<plugin>
  ...
  <executions>
    <execution>
      <phase>prepare-package</phase>
      <goals>
        <goal>genRepoInfoFile</goal>
        </goals>
    </execution>
  </executions>
  ...
</plugin>
```

# App Engine configuration file deployment

You can now deploy the cron/doc/etc. configuration files separately using the new goals:

* `deployCron`
* `deployDispatch`
* `deployDos`
* `deployIndex`
* `deployQueue`

## Source directory
_For GAE Flexible projects_ The deployment source directory can be overridden by setting the `appEngineDirectory` parameter in the deploy configuration.

### Default value
For GAE Standard projects it defaults to `${buildDir}/staged-app/WEB-INF/appengine-generated`.

For GAE Flexible projects it defaults to `src/main/appengine`.

# Dev App Server v1

Dev App Server v1 is the default configured local run server from version 1.3.0 onwards.

## Parameters

Dev App Server v1 parameters are a subset of Dev App Server 2 parameters that have been available as part of the
run configuration.

* ~~`appYamls`~~ - deprecated in favor of `services`.
* `services` - a list of services to run [default is the current module].
* `host` - host address to run on [default is localhost].
* `port` - port to run on [default is 8080].
* `jvmFlags` - jvm flags to send the to the process that started the dev server.

Any other configuration parameter is Dev App Server v2 ONLY, and will print a warning and be ignored.

## Debugger

You can debug the Dev App Server v1 using the jvmFlags
```XML
<configuration>
  <jvmFlags>
    <jvmFlag>
        -agentlib:jdwp=transport=dt_socket,address=5005,server=y,suspend=n
    </jvmFlag>
  </jvmFlags>
<configuration>
```

## Putting the Datastore somewhere else (so it's not deleted across rebuilds)
```XML
<configuration>
  <jvmFlags>
    <jvmFlag>
        -Ddatastore.backing_store=/path/to/my/local_db.bin
    </jvmFlag>
  </jvmFlags>
<configuration>
```

## Running Multiple Modules

Multimodule support can be done by adding all the runnable modules to a single module's configuration (which currently must be an appengine-standard application).

```XML
<configuration>
  <services>
    <service>${project.build.directory}/${project.name}-${project.version}</service>
    <service>${project.parent.basedir}/&lt;other_module&gt;/target/&lt;other_module_finalName&gt;-${project.version}</service>
  </services>
</configuration>
```

## Switch to Dev App Server v2-alpha

To switch back to the Dev App Server v2-alpha (that was default in version < 1.3.0) use the `devserverVersion` parameter

```XML
<configuration>
   <devserverVersion>2-alpha</devserverVersion>
</configuration>
```
