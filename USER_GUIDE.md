# User guide

Primitive guide for users 1.3.0 and above (need to expand)

## Applying the Plugin
For both _standard_ and _flexible_ applications, include the plugin in your pom.xml:

```XML
...
<build>
  <plugins>
    <plugin>
      <groupId>com.google.cloud.tools</groupId>
      <artifactId>appengine-maven-plugin</artifactId>
      <version>VERSION</version>
    </plugin>
  </plugins>
</build>
```

The [Cloud SDK](https://cloud.google.com/sdk) is required for this plugin to
function. Download and install it before running any tasks.

## App Engine Standard
The plugin will target the App Engine standard environment if you include an `appengine-web.xml`
in `src/main/webapp/WEB-INF/`, otherwise it will assume it is an [App Engine flexible](#app-engine-flexible)
application.

### Goals
For App Engine standard, the plugin exposes the following goals :

#### Local Run

| Goal    | Description |
| ------- | ----------- |
| `run`   | Run the application locally. |
| `start` | Start the application in the background. |
| `stop`  | Stop a running application. |

#### Deployment

| Goal             | Description |
| ---------------- | ----------- |
| `stage`          | Stage an application for deployment. |
| `deploy`         | Deploy an application. |
| `deployCron`     | Deploy cron configuration. |
| `deployDispatch` | Deploy dispatch configuration. |
| `deployDos`      | Deploy dos configuration. |
| `deployIndex`    | Deploy datastore index configuration. |
| `deployQueue`    | Deploy queue configuration. |

### Configuration
Once you've [initialized](https://cloud.google.com/sdk/docs/initializing) `gcloud` you can run and deploy
your application using the defaults provided by the plugin.

To see the generated documentation for goals and parameters including default values, execute the
following:

```bash
$  mvn appengine:help -Ddetail
```

If you wish to customize your configuration, the plugin can be configured using the usual
`<configuration>` element.

##### Cloud SDK configuration

| Parameter      | Description |
| -------------- | ----------- |
| `cloudSdkPath` | Location of the Cloud SDK, the plugin will try to find it if none is specified here. |

##### Run configuration
Note that only a subset are valid for Dev App Server version "1" and all are valid for Dev App Server
version "2-alpha".

Valid for versions "1" and "2-alpha":

| Parameter             | Description |
| --------------------- | ----------- |
| ~~`appYamls`~~        | Deprecated in favor of `services` |
| `devserverVersion`    | Server versions to use, options are "1" or "2-alpha" |
| `environment`        | Environment variables to pass to the Dev App Server process |
| `host`                | Application host address. |
| `jvmFlags`            | JVM flags to pass to the App Server Java process. |
| `port`                | Application host port. |
| `services`            | List of services to run |
| `startSuccessTimeout` | Amount of time in seconds to wait for the Dev App Server to start in the background. |

Only valid for version "2-alpha":

| Parameter (2-alpha only) |
| ------------------------ |
| `adminHost`              |
| `adminPort`              |
| `allowSkippedFiles`      |
| `apiPort`                |
| `authDomain`             |
| `automaticRestart`       |
| `clearDatastore`         |
| `customEntrypoint`       |
| `datastorePath`          |
| `defaultGcsBucketName`   |
| `devAppserverLogLevel`   |
| `logLevel`               |
| `maxModuleInstances`     |
| `pythonStartupArgs`      |
| `pythonStartupScript`    |
| `runtime`                |
| `skipSdkUpdateCheck`     |
| `storagePath`            |
| `threadsafeOverride`     |
| `useMtimeFileWatcher`    |

##### Stage
The `stage` configuration has some Flexible environment only parameters that
are not listed here and will just be ignored.
The `stage` configuration has the following parameters:

| Parameter               | Description |
| ----------------------- | ----------- |
| `compileEncoding`       | The character encoding to use when compiling JSPs. |
| `deleteJsps`            | Delete the JSP source files after compilation. |
| `disableJarJsps`        | Disable adding the classes generated from JSPs. |
| `disableUpdateCheck`    | Disable checking for App Engine SDK updates. |
| `enableJarClasses`      | Jar the WEB-INF/classes content. |
| `enableJarSplitting`    | Split JAR files larger than 10 MB into smaller fragments. |
| `enableQuickstart`      | Use Jetty quickstart to process servlet annotations. |
| `jarSplittingExcludes`  | Exclude files that match the list of comma separated SUFFIXES from all JAR files. |
| `sourceDirectory`       | The location of the compiled web application files, or the exploded WAR. This is used as the source for staging. |
| `stagingDirectory`      | The directory to which to stage the application. |

##### Deploy
The `deploy` configuration has some Flexible environment only parameters that
are not listed here and will just be ignored.
The `deploy` configuration has the following parameters:

| Parameter             | Description |
| --------------------- | ----------- |
| `bucket`              | The Google Cloud Storage bucket used to stage files associated with the deployment. |
| `deployables`         | The YAML files for the services or configurations you want to deploy. |
| `project`             | The Google Cloud Project target for this deployment. |
| `promote`             | Promote the deployed version to receive all traffic. |
| `server`              | The App Engine server to connect to. Typically, you do not need to change this value. |
| `stopPreviousVersion` | Stop the previously running version when deploying a new version that receives all traffic. |
| `version`             | The version of the app that will be created or replaced by this deployment. If you do not specify a version, one will be generated for you by the Cloud SDK. |

---

### How do I deploy my project Configuration Files?

You can now deploy the cron/dos/etc. configuration files separately using the new goals:

* `deployCron`
* `deployDispatch`
* `deployDos`
* `deployIndex`
* `deployQueue`

_For GAE Flexible projects_ The deployment source directory can be overridden by setting the `appEngineDirectory` parameter in the deploy configuration.

For standard it defaults to `<stagingDirectory>/WEB-INF/appengine-generated` (and `stagingDirectory`
defaults to `${project.build.directory}/appengine-staging`). You should probably
not change this configuration, for standard configured projects, this is the location that your
xml configs are converted into yaml for deployment.

### How do I debug Dev Appserver v1?

You can debug the Dev App Server v1 using the jvmFlags:

```XML
<configuration>
  <jvmFlags>
    <jvmFlag>
        -agentlib:jdwp=transport=dt_socket,address=5005,server=y,suspend=n
    </jvmFlag>
  </jvmFlags>
<configuration>
```

### How do I put datastore somewhere else (so it's not deleted across rebuilds)?

```XML
<configuration>
  <jvmFlags>
    <jvmFlag>
        -Ddatastore.backing_store=/path/to/my/local_db.bin
    </jvmFlag>
  </jvmFlags>
<configuration>
```

### How do I run multiple modules on the Dev App Server v1?

Multimodule support can be done by adding all the runnable modules to a single module's configuration (which currently must be an appengine-standard application).

```XML
<configuration>
  <services>
    <service>${project.build.directory}/${project.name}-${project.version}</service>
    <service>${project.parent.basedir}/other_module/target/other_module_finalName-${project.version}</service>
  </services>
</configuration>
```

### I want to use Dev Appserver 2 (alpha), how do I switch to it?

To switch back to the Dev App Server v2-alpha (that was default in version < 1.3.0) use the `devserverVersion` parameter

```XML
<configuration>
   <devserverVersion>2-alpha</devserverVersion>
</configuration>
```

### How can I pass environment variables to the Dev Appserver (both v1 and v2-alpha)?

You can pass environment variables directly to the Dev App Server:

```XML
<configuration>
  <environment>
    <VARIABLE_NAME>value</VARIABLE_NAME>
  <environment>
<configuration>
```

---

## App Engine Flexible
The plugin will target the App Engine flexible environment if you do **NOT** include an `appengine-web.xml`
in `src/main/webapp/WEB-INF/`.

### Goals
For App Engine flexible, the plugin exposes the following goals:

#### Deployment

| Goal             | Description |
| ---------------- | ----------- |
| `stage`          | Stage an application for deployment. |
| `deploy`         | Deploy an application. |
| `deployCron`     | Deploy cron configuration. |
| `deployDispatch` | Deploy dispatch configuration. |
| `deployDos`      | Deploy dos configuration. |
| `deployIndex`    | Deploy datastore index configuration. |
| `deployQueue`    | Deploy queue configuration. |

Once you've [initialized](https://cloud.google.com/sdk/docs/initializing) `gcloud` you can run and deploy
your application using the defaults provided by the plugin.

To see the generated documentation for goals and parameters including default values, execute the
following:

```bash
$  mvn appengine:help -Ddetail
```

If you wish to customize your configuration, the plugin can be configured using the usual
`<configuration>` element.

##### Cloud SDK configuration

| Parameter      | Description |
| -------------- | ----------- |
| `cloudSdkPath` | Location of the Cloud SDK, the plugin will try to find it if none is specified here. |


##### Stage
The `stage` configuration has the following parameters:

| Parameter            | Description |
| -------------------- | ----------- |
| `appEngineDirectory` | The directory that contains app.yaml. |
| `dockerDirectory`    | The directory that contains Dockerfile and other docker context. |
| `artifact`           | The artifact to deploy (a file, like a .jar or a .war). |
| `stagingDirectory`   | The directory to which to stage the application |

##### Deploy
The `deploy` configuration has the following parameters:

| Parameter             | Description |
| --------------------- | ----------- |
| `appEngineDirectory`  | Location of configuration files (cron.yaml, dos.yaml, etc) for configuration specific deployments. |
| `bucket`              | The Google Cloud Storage bucket used to stage files associated with the deployment. |
| `deployables`         | The YAML files for the services or configurations you want to deploy. |
| `imageUrl`            | Deploy with a Docker URL from the Google container registry. |
| `project`             | The Google Cloud Project target for this deployment. |
| `promote`             | Promote the deployed version to receive all traffic. |
| `server`              | The App Engine server to connect to. Typically, you do not need to change this value. |
| `stopPreviousVersion` | Stop the previously running version of this service after deploying a new one that receives all traffic. |
| `version`             | The version of the app that will be created or replaced by this deployment. If you do not specify a version, one will be generated for you by the Cloud SDK. |
