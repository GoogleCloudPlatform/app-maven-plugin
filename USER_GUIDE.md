# User guide 2.+

| NOTE             |
| :---------------- |
| The behavior of the appengine-maven-plugin has changed since v1.+; please see the [CHANGELOG](CHANGELOG.md) for a full list of changes. If you are having trouble using or updating your plugin, please file a [new issue](https://github.com/GoogleCloudPlatform/app-maven-plugin/issues).|

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

As of version 2.0.0, App Engine goals no longer fork. In order to run any App Engine goals, you must
make sure to package your application first (i.e. run `mvn package appengine:<goal>`). You may also
bind the goals to a lifecycle phase in your pom.xml.

## App Engine Standard
The plugin will target the App Engine standard environment if you include an `appengine-web.xml`
in `src/main/webapp/WEB-INF/`, otherwise it will assume it is an [App Engine flexible](#app-engine-flexible)
application.

### Goals
For App Engine standard, the plugin exposes the following goals :

#### Local Run

| Goal              | Description |
| ----------------- | ----------- |
| `run`             | Run the application locally. |
| `start`           | Start the application in the background. |
| `stop`            | Stop a running application. |

#### Deployment

| Goal             | Description |
| ---------------- | ----------- |
| `cloudSdkLogin`  | Login and set the Cloud SDK common configuration user. |
| `stage`          | Stage an application for deployment. |
| `deploy`         | Deploy an application. |
| `deployCron`     | Deploy cron configuration. |
| `deployDispatch` | Deploy dispatch configuration. |
| `deployDos`      | Deploy dos configuration. |
| `deployIndex`    | Deploy datastore index configuration. |
| `deployQueue`    | Deploy queue configuration. |
| `deployAll`      | Deploy the application and all of its configuration files at once. |

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

| Parameter               | Description |
| ----------------------- | ----------- |
| `serviceAccountKeyFile` | A Google project service account key file to run Cloud SDK operations requiring an authenticated user. |
| `cloudSdkHome`          | Location of the Cloud SDK. |
| `cloudSdkVersion`       | Desired version of the Cloud SDK. (e.g. "192.0.0") |

The Cloud SDK will be installed/updated/verified depending on which parameters are configured:

| Parameters Specified   | Action |
| ---------------------- | ------ |
| None                   | Latest version of the Cloud SDK is downloaded and installed. |
| Both parameters        | Cloud SDK installation specified at `cloudSdkHome` is verified. |
| `cloudSdkHome` only    | No verification. |
| `cloudSdkVersion` only | Cloud SDK at specified version is downloaded and installed. |

The Cloud SDK is installed in `$USER_HOME/.cache/google-cloud-tools-java/managed-cloud-sdk/<version>/google-cloud-sdk`
on Linux, `$USER_HOME/Library/Application Support/google-cloud-tools-java/managed-cloud-sdk/<version>/google-cloud-sdk`
on OSX, and `%LOCALAPPDATA%/google/ct4j-cloud-sdk/<version>/google-cloud-sdk` on Windows.
The Cloud SDK installation/verification occurs automatically before running any appengine goals.

##### Run configuration
Note that only a subset are valid for Dev App Server version "1" and all are valid for Dev App Server
version "2-alpha".

Valid for versions "1" and "2-alpha":

| Parameter             | Description |
| --------------------- | ----------- |
| `devserverVersion`    | Server versions to use, options are "1" or "2-alpha" |
| `environment`         | Environment variables to pass to the Dev App Server process |
| `host`                | Application host address. |
| `jvmFlags`            | JVM flags to pass to the App Server Java process. |
| `port`                | Application host port. |
| `services`            | List of services to run |
| `startSuccessTimeout` | Amount of time in seconds to wait for the Dev App Server to start in the background. |
| `additionalArguments` | Any additional arguments to be passed to the Dev App Server |
| `automaticRestart`    | Automatically restart the server when explode-war directory has changed |
| `projectId`           | Set a Google Cloud Project Id on the running development server |

Only valid for version "2-alpha":

| Parameter (2-alpha only) |
| ------------------------ |
| `adminHost`              |
| `adminPort`              |
| `allowSkippedFiles`      |
| `apiPort`                |
| `authDomain`             |
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
| `projectId`           | The Google Cloud Project target for this deployment. This can also be set to `GCLOUD_CONFIG` or `APPENGINE_CONFIG`.\* |
| `promote`             | Promote the deployed version to receive all traffic. |
| `server`              | The App Engine server to connect to. Typically, you do not need to change this value. |
| `stopPreviousVersion` | Stop the previously running version when deploying a new version that receives all traffic. |
| `version`             | The version of the app that will be created or replaced by this deployment. This also can be set to `GCLOUD_CONFIG` or `APPENGINE_CONFIG`.\* |

\* setting a property to `GCLOUD_CONFIG` will deploy using the gcloud settings for the property.
\* setting a property to `APPENGINE_CONFIG` will deploy using the value read from `appengine-web.xml`.

## App Engine Flexible
The plugin will target the App Engine flexible environment if you do **NOT** include an `appengine-web.xml`
in `src/main/webapp/WEB-INF/`.

### Goals
For App Engine flexible, the plugin exposes the following goals:

#### Deployment

| Goal             | Description |
| ---------------- | ----------- |
| `cloudSdkLogin`  | Login and set the Cloud SDK common configuration user. |
| `stage`          | Stage an application for deployment. |
| `deploy`         | Deploy an application. |
| `deployCron`     | Deploy cron configuration. |
| `deployDispatch` | Deploy dispatch configuration. |
| `deployDos`      | Deploy dos configuration. |
| `deployIndex`    | Deploy datastore index configuration. |
| `deployQueue`    | Deploy queue configuration. |
| `deployAll`      | Deploy the application and all of its configuration files at once. |

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

| Parameter          | Description |
| ------------------ | ----------- |
| `cloudSdkHome`     | Location of the Cloud SDK. |
| `cloudSdkVersion`  | Desired version of the Cloud SDK. (e.g. "192.0.0") |

The Cloud SDK will be installed/updated/verified depending on which parameters are configured:

| Parameters Specified   | Action |
| ---------------------- | ------ |
| None                   | Latest version of the Cloud SDK is downloaded and installed. |
| Both parameters        | Cloud SDK installation specified at `cloudSdkHome` is verified. |
| `cloudSdkHome` only    | No verification. |
| `cloudSdkVersion` only | Cloud SDK at specified version is downloaded and installed. |

The Cloud SDK is installed in `$USER_HOME/.cache/google-cloud-tools-java/managed-cloud-sdk/<version>/google-cloud-sdk`
on Linux, `$USER_HOME/Library/Application Support/google-cloud-tools-java/managed-cloud-sdk/<version>/google-cloud-sdk`
on OSX, and `%LOCALAPPDATA%/google/ct4j-cloud-sdk/<version>/google-cloud-sdk` on Windows.
The Cloud SDK installation/verification occurs automatically before running any appengine goals.


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
| `imageUrl`            | Deploy with a Docker URL from the Google container registry. |
| `projectId`             | The Google Cloud Project target for this deployment. This can also be set to `GCLOUD_CONFIG`.\* |
| `promote`             | Promote the deployed version to receive all traffic. |
| `server`              | The App Engine server to connect to. Typically, you do not need to change this value. |
| `stopPreviousVersion` | Stop the previously running version of this service after deploying a new one that receives all traffic. |
| `version`             | The version of the app that will be created or replaced by this deployment. This can also be set to `GCLOUD_CONFIG`.\* |

\* setting a property to `GCLOUD_CONFIG` will deploy using the gcloud settings for the property.

---

## FAQ

### How do I deploy my project Configuration Files?

You can now deploy the cron/dos/etc. configuration files separately using the new goals:

* `deployCron`
* `deployDispatch`
* `deployDos`
* `deployIndex`
* `deployQueue`

You may also use the `deployAll` goal to deploy the application and all valid configuration files at once.

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
</configuration>
```

### How do I enable automatic reload of my application?

To enable automatic reload of an application, you must tell the Dev App Server to scan for changes :
```XML
<configuration>
  <automaticRestart>true</automaticRestart>
</configuration>
```
While your app is running, just run `mvn war:exploded` to reflect your changes into the running application.


### How do I put datastore somewhere else (so it's not deleted across rebuilds)?

```XML
<configuration>
  <jvmFlags>
    <jvmFlag>
        -Ddatastore.backing_store=/path/to/my/local_db.bin
    </jvmFlag>
  </jvmFlags>
</configuration>
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

Caution: The v2-alpha version of the development web server is not fully
supported, and you may find errors when using this version.

To switch to Dev App Server v2-alpha use the `devserverVersion` parameter.

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
  </environment>
</configuration>
```

### How can I pass additional arguments to the Dev Appserver (both v1 and v2-alpha)?

You can pass additional arguments directly to the Dev App Server:

```XML
<configuration>
  <additionalArguments>
    <additionalArgument>--ARG1</additionalArgument>
    <additionalArgument>--ARG2</additionalArgument>
  </additionalArguments>
</configuration>
```

### How can I bind App Engine goals to lifecycle phases in my build file?

You can add something like the following to your pom.xml:

```XML
<plugin>
  ...
  <executions>
    <execution>
      <phase>deploy</phase>
      <goals>
        <goal>deploy</goal>
      </goals>
    </execution>
  </executions>
  ...
</plugin>
```

In this case, running `mvn deploy` will automatically build and deploy the application to appengine.

### I have a project that supports both flex and standard. How do I control which deployment to use?

The plugin defaults to standard deployment if your project contains a webapp/WEB-INF/appengine-web.xml file. If
your project also has an appengine/app.yaml and you wish to use flexible deployment, you may temporarily move the
appengine-web.xml file to a different location before deploying.

---
