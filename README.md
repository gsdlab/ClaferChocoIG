ClaferChocoIG
===========

v0.3.6.15-04-2014

A backend for [ClaferIDE](https://github.com/gsdlab/ClaferIDE), [ClaferMooVisualizer](https://github.com/gsdlab/ClaferMooVisalizer) and [ClaferConfigurator](https://github.com/gsdlab/ClaferConfigurator), as well as a stand-alone command line tool. Uses [ChocoSolver](https://github.com/gsdlab/chocosolver) to generate arbitrary or optimal instances from a Javascript-based representation (`--mode=choco`) of Clafer models.

This project is a Maven Java project that uses [ChocoSolver](https://github.com/gsdlab/chocosolver) as a primary dependency and invokes its API. The project can be compiled into compiled standalone binary which can be either used in a command line, or configured to be used as a backend for other tools.

Contributors
------------

* [Alexandr Murashkin](http://gsd.uwaterloo.ca/amurashk), MMath Candidate. Main developer.
* [Jimmy Liang](http://gsd.uwaterloo.ca/jliang), MSc. Candidate. Ports to Java 1.7 and Choco3, main developer of the [ChocoSolver](https://github.com/gsdlab/chocosolver).

Getting Binaries
--------------------

Binary distributions of the release 0.3.6 of Clafer Tools for Windows, Mac, and Linux, 
can be downloaded from [Clafer Tools - Binary Distributions](http://http://gsd.uwaterloo.ca/clafer-tools-binary-distributions). 

The binary for this project is called `claferchocoig-0.3.6-jar-with-dependencies.jar`.

In case this binary does not work on your particular machine configuration, it can be built from source code, as described further.

Running
-------------

### Prerequisites

* [Java 7+](http://www.oracle.com/technetwork/java/javase/downloads/index.html).
* [Clafer Compiler](https://github.com/gsdlab/clafer). Required for compiling Clafer files (`.cfr`) into the Clafer Choco Javascript format (`.js`), so that they can be run using the tool.
 
### Running

First, use *Clafer Compiler*:

```sh
clafer --mode=choco <file-name.cfr>
```

This will convert the Clafer file (`.cfr`) into the Clafer Choco Javascript file (`.js`). 

Next, run the instance generator:

```sh
java -jar claferchocoig-0.3.5-jar-with-dependencies.jar --file=<file-name.js> <options>
```

This will run the solver and produce instances in a textual form. The full list of `<options>` is listed below (as printed by `--help` command):

```
Option                          Description                            
------                          -----------                            
--file <File: Javascript file>  input file in Javascript format        
--help                          show help                              
--maxint <Integer>              specify maximum integer value          
--moo                           run the tool in multi-objective        
                                  optimization mode                    
-n <Integer>                    specify maximum number of instances    
--output <File: Text file>      output instances to file               
--prettify                      use simple and pretty output format    
                                  (not formal)                         
--repl                          run the tool in REPL (interactive) mode
--scope <Integer>               override global scope value            
--testaadl                      test the AADL to Clafer model          
--version                       display the tool version               

```

### Configuring as a Backend

The configuration is done in the `Server/Backends/backends.json` file.

* An example configuration for [ClaferIDE](https://github.com/gsdlab/ClaferIDE):

```json


{
    "backends": [
.................
.................

        {
            "id": "chocoIG", 
            "label": "Choco-based (IG + MOO)",
            "tooltip": "A new instance generator and multi-objective optimizer based on Choco3 solver library",
            "accepted_format": "choco",             
            "tool": "java",
            "tool_args": ["-jar", "$dirname$/../../../ChocoIG/claferchocoig-0.3.6-jar-with-dependencies.jar", "--file=$filepath$", "--repl", "--prettify"],            
            "tool_version_args": ["-jar", "$dirname$/../../../ChocoIG/claferchocoig-0.3.6-jar-with-dependencies.jar", "--version"],
            "scope_options": {
                "set_default_scope" : {"command": "globalScope $value$\n"}, 
                "set_individual_scope": {"command": "scope $clafer$ $value$\n"}, 
                "inc_all_scopes" : {"command": "incGlobalScope $value$\n"},
                "inc_individual_scope": {"command": "incScope $clafer$ $value$\n"},
                "produce_scope_file" : {"command": "saveScopes\n"},
                "set_int_scope" : {"command": "maxInt $value$\n", "default_value": 127}
            },
            "control_buttons": [
                {"id": "next", "command": "n\n", "label" : "Next", "tooltip": "Next Instance"}, 
                {"id": "reload", "command": "r\n", "label" : "Reset", "tooltip": "Reset instance generation, applied scopes and other settings"}, 
                {"id": "quit", "command": "q\n", "label" : "Quit", "tooltip": "Exit the IG safely"}
            ],
            "presentation_specifics": {
                "prompt_title": ""
            }            
        }
    ]   
}

```

**Notes**:

`$dirname$` means the full path to the *Server/Backends* folder of the tool, `$filepath$` is the full path to the input JS file being processed.
* If you make any changes to the `backends.json`, restart the web-tool completely to make the changes take effect.
* If you done your configuration properly, the tool will restart successfully and the backend should be listed in the `Backends` dropdown list. If the tool does not start, the reason may be either a syntax error in the backends.json file, or the paths specified in it are not correct or lead to an inaccessible JAR file. Also, check the `Execute` permission on the JAR file.

Building
--------

### Prerequisites

* [Maven 2+](http://maven.apache.org/download.cgi). Required for building the projects and linking all dependencies
* [ChocoSolver](https://github.com/gsdlab/chocosolver). This is a Maven dependency for the project, so it should be installed (`mvn install`) as well.

### Procedures

* Using Maven, run: `mvn install` over the project.
* Two binaries will appear in the `target` subfolder: `claferchocoig-0.3.6-jar-with-dependencies.jar` that is standalone and contains all the required dependencies, and `claferchocoig-0.3.6.jar` that is not.

### Important: Branches must correspond

All related projects are following the *simultaneous release model*. 
The branch `master` contains releases, whereas the branch `develop` contains code under development. 
When building the tools, the branches should match.
Releases from branches `master` are guaranteed to work well together.
Development versions from branches `develop` should work well together but this might not always be the case.

Need help?
==========
* See [language's website](http://clafer.org) for news, technical reports and more
  * Check out a [Clafer tutorial](http://t3-necsis.cs.uwaterloo.ca:8091/Tutorial/Intro)
  * Try a live instance of [ClaferWiki](http://t3-necsis.cs.uwaterloo.ca:8091)
  * Try a live instance of [ClaferIDE](http://t3-necsis.cs.uwaterloo.ca:8094)
  * Try a live instance of [ClaferConfigurator](http://t3-necsis.cs.uwaterloo.ca:8093)
  * Try a live instance of [ClaferMooVisualizer](http://t3-necsis.cs.uwaterloo.ca:8092)
* Take a look at (incomplete) [Clafer wiki](https://github.com/gsdlab/clafer/wiki)
* Browse example models in the [test suite](https://github.com/gsdlab/clafer/tree/master/test/positive) and [MOO examples](https://github.com/gsdlab/clafer/tree/master/spl_configurator/dataset)
* Post questions, report bugs, suggest improvements [GSD Lab Bug Tracker](http://gsd.uwaterloo.ca:8888/questions/). Tag your entries with `claferchocoig` (so that we know what they are related to) and with `alexander-murashkin` or `michal` (so that Alex or Michał gets a notification).
