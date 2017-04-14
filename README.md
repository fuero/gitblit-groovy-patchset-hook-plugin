# Gitblit Groovy Patchset Hook plugin

This plugin provides a patchset hook that calls groovy scripts similar
to the existing functionality provided by [GitblitReceivePack](https://github.com/gitblit/gitblit/blob/master/src/main/java/com/gitblit/git/GitblitReceivePack.java).

Beware that due to the nature of extension points in gitblit, the groovy scripts
will be run for all repositories and they have to take care of deciding if they
should act on a particular patchset.

## Configuration settings

This plugin queries 3 new settings from gitblit.properties:

    groovyPatchsetHook.onNewPatchset
    groovyPatchsetHook.onUpdatePatchset
    groovyPatchsetHook.onMergePatchset

The values should be comma separated lists of groovy script names, e.g.:

    groovyPatchsetHook.onNewPatchset = script1, script2
    groovyPatchsetHook.onUpdatePatchset = script3, script4
    groovyPatchsetHook.onMergePatchset = script1, script4

The plugin passes these values to the called script:

 - event: PatchSetEvent, enum { New, Update, Merge }
 - gitblit: IGitblit
 - ticket: TicketModel
 - url: String
 - logger: slf4j Logger 

## Building, Deploying

To build, apply the changes and execute:

```
mvn clean package
```

To deploy, copy the generated groovy-patchset-hook zip file to your Gitblit `${baseFolder}/plugins` directory.

## Acknowledgements

This plugin is heavily based on the [cookbook](https://github.com/gitblit/gitblit-cookbook-plugin) plugin and
the [GitblitReceivePack](https://github.com/gitblit/gitblit/blob/master/src/main/java/com/gitblit/git/GitblitReceivePack.java) class


