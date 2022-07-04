# Amethyst Core

### Library mod that powers the Amethyst Imbuement family of mods; can be used to create your very own Magic-themed Mod!
see the wiki for more detailed information.

### Including AC in your project
Amethyst Core is stored on Modrinth, so you can easily grab it from there with the following maven and dependencies notation:

**Current Latest Version: 0.1.0+1.18.2**

repositories:
```
//(build.gradle.kts)
maven {
        name = "Modrinth"
        url = uri("https://api.modrinth.com/maven")
        content {
            includeGroup("maven.modrinth")
        }
    }
```
```
//(build.gradle)
maven {
        name = "Modrinth"
        url = "https://api.modrinth.com/maven"
        content {
            includeGroup "maven.modrinth"
        }
    }
```

dependencies:
```
//(build.gradle.kts)
modImplementation("maven.modrinth:UbOpZw7q:[VERSION]")
```
```
//(build.gradle)
modImplementation "maven.modrinth:UbOpZw7q:[VERSION]"
```

optional:
```
include("maven.modrinth:UbOpZw7q:[VERSION])
//or
include "maven.modrinth:UbOpZw7q:[VERSION]
```
