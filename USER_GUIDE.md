# User guide

---
Primitive guide for users 1.3.0 and above (need to expand)

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
    <service>${project.parent.basedir}/other_module/target/other_module_finalName-${project.version}</service>
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
