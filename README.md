ClaferChocoIG
=============

v0.3.10.1

A backend for [ClaferIDE](https://github.com/gsdlab/ClaferIDE), [ClaferMooVisualizer](https://github.com/gsdlab/ClaferMooVisalizer), and [ClaferConfigurator](https://github.com/gsdlab/ClaferConfigurator), as well as a stand-alone command-line tool.
It uses [ChocoSolver](https://github.com/gsdlab/chocosolver) to generate arbitrary or optimal instances from a Javascript-based representation of Clafer models generated by the Clafer compiler using the option `--mode=choco`.

This project is a Maven Java project that uses [ChocoSolver](https://github.com/gsdlab/chocosolver) as a primary dependency and invokes its API.
The project can be compiled into compiled standalone binary which can be either used in a command line, or configured to be used as a backend for other tools.

Contributors
------------

* [Alexandr Murashkin](http://gsd.uwaterloo.ca/amurashk), MMath. Original developer.
* [Jimmy Liang](http://gsd.uwaterloo.ca/jliang), MMath, Ph.D. Candidate. Ports to Java 1.7 and Choco3, main developer of the [ChocoSolver](https://github.com/gsdlab/chocosolver).
* [Michał Antkiewicz](http://gsd.uwaterloo.ca/mantkiew). Research Engineer. Usability extensions, technology transfer.

Getting Binaries
----------------

Binary distributions of the release 0.3.10 of Clafer Tools for Windows, Mac, and Linux, can be downloaded from [Clafer Tools - Binary Distributions](http://http://gsd.uwaterloo.ca/clafer-tools-binary-distributions).

The binary for this project is called `claferchocoig.jar`.

In case this binary does not work on your particular machine configuration, it can be built from source code, as described further.

Integration with Sublime Text 2/3
-------------------------------

See [ClaferToolsST](https://github.com/gsdlab/ClaferToolsST)

Usage
-----

### Prerequisites

* [Java 8+](http://www.oracle.com/technetwork/java/javase/downloads/index.html).
* [Clafer Compiler](https://github.com/gsdlab/clafer) v0.3.10.
  * Required for compiling Clafer files (`.cfr`) into the Clafer Choco Javascript format (`.js`), so that they can be run using the tool.

### Running Stand-alone and Arguments

Clafer Choco IG can work with both the Clafer model file (`.cfr`) or the Clafer Choco Javascript file (`.js`).
When given a Clafer model file, as follows

```sh
java -jar claferchocoig.jar --file=<file-name.cfr> <options>
```

Clafer Choco IG compiles it by calling the compiler as follows:

```
clafer -m choco
```

that is, the default options are used, including simple scope computation.

When different compilation options are needed, the Clafer model should be compiled manually and the resulting Clafer Choco Javascript file (`.js`) can be directly processed.
For example, one can compile a model as follows:

```sh
clafer -m choco -m html --self-contained --ss=full --skip-goals --meta-data <file-name.cfr>
```

that is, to produce both `.js` and self-contained `.html` outputs, perform full scope computation, ignore optimization objectives (goals), and generate `.cfr-map` and `.cfr-scope` metadata files.

After that, the resulting `.js` file can be edited to adjust certain parameters, such as, scopes or integer range.

Next, run the instance generator:

```sh
java -jar claferchocoig.jar --file=<file-name.js> <options>
```

This will run the solver and produce instances in a textual form.
The full list of `<options>` is listed below (as printed by `--help` command):

```
Option                                Description
------                                -----------
--file <File: Clafer model (.cfr) or  input file in .cfr or .js format
  Clafer Javascript file (.js)>
--help                                show help
--maxint <Integer>                    specify maximum integer value
--minint <Integer>                    specify minimum integer value
--moo                                 run the tool in multi-objective
                                        optimization mode
-n <Integer>                          specify maximum number of instances
--output <File: Text file>            output instances to file
--prettify                            use simple and pretty output format
                                        (not formal)
--repl                                run the tool in REPL (interactive) mode
--scope <Integer>                     override global scope value
--testaadl                            test the AADL to Clafer model
--version                             display the tool version

```

#### REPL commands

When running in the REPL mode (`--repl`), the following commands are available:

```
help                           print the REPL commands
n                              generate the next instance
<enter>                        generate the next instance
r                              reload the model from the same <file-name.js> file
unsatCore                      compute the set of contradicting constraints if any
minUnsat                       compute the minimal UnSAT core and a near-miss example
globalScope <value>            set the global scope to the <value>
scope <clafer-UID> <value>     set the scope of the given clafer to the <value>
incGlobalScope <value>         increase the global scope by <value>
incScope <clafer-UID> <value>  increase the scope of the given clafer by the <value>
saveScopes                     save the currect scopes to a `.cfr-scope` file
maxInt <value>                 set the largest allowed integer to <value>
q                              exit the REPL sesssion
```

### Configuring as a Backend

The configuration is done in the `<host-tool-path>/Server/Backends/backends.json` file, where `<host-tool-path>` is a path to the web-based tool (`ClaferIDE`, etc.) you want to configure to use `ClaferChocoIG` as a backend.

* An example configuration for [ClaferIDE](https://github.com/gsdlab/ClaferIDE):

```json
{
    "backends": [
        {
            "id": "chocoIG",
            "label": "Choco-based (IG + MOO)",
            "tooltip": "A new instance generator and multi-objective optimizer based on Choco3 solver library",
            "accepted_format": "choco",
            "tool": "java",
            "tool_args": ["-jar", "~/bin/claferchocoig.jar", "--file=$filepath$", "--repl", "--prettify"],
            "tool_version_args": ["-jar", "~/bin/claferchocoig.jar", "--version"],
            "scope_options": {
                "set_default_scope" : {"command": "globalScope $value$\n", "label": "Default:", "argument": "--scope=$value$", "default_value": 1},
                "set_individual_scope": {"command": "scope $clafer$ $value$\n"},
                "inc_all_scopes" : {"command": "incGlobalScope $value$\n", "label": "All:", "default_value": 1},
                "inc_individual_scope": {"command": "incScope $clafer$ $value$\n"},
                "produce_scope_file" : {"command": "saveScopes\n"},
                "set_int_scope" : {"command": "maxInt $value$\n", "label": "Max. integer:", "argument": "--maxint=$value$", "default_value": 127}
            },
            "control_buttons": [
                {"id": "next_instance", "command": "n\n", "label" : "Next", "tooltip": "Next Instance"},
                {"id": "reload", "command": "r\n", "label" : "Reload", "tooltip": "Reload the model preserving scopes and other settings"},
                {"id": "quit", "command": "q\n", "label" : "Quit", "tooltip": "Exit the IG safely"}
            ],
            "presentation_specifics": {
                "prompt_title": "ChocoIG> "
            }
        },
    ]
}

```

* An example configuration for [ClaferMooVisualizer](https://github.com/gsdlab/ClaferMooVisualizer):

```json
{
    "backends": [
        {
            "id": "choco_moo",
            "label": "Choco-based (MOO with magnifier)",
            "tooltip": "A new Choco-based solver, for multi-objective optimization",
            "accepted_format": "choco",
            "tool": "java",
            "tool_args": ["-jar", "~/bin/claferchocoig.jar", "--file=$filepath$", "--moo"],
            "tool_version_args": ["-jar", "~/bin/claferchocoig.jar", "--version"],
            "optimization_options": {
                "set_int_scope" : {"label": "Max. integer:", "argument": "--maxint=$value$", "default_value": 127},
                "set_default_scope" : {"label": "Default scopes:", "argument": "--scope=$value$", "default_value": 25}
            }
        },
    ]
}
```

* An example configuration for [ClaferConfigurator](https://github.com/gsdlab/ClaferConfigurator):

```json

{
    "backends": [
        {
            "id": "chocoIG",
            "label": "Choco-based (IG + MOO)",
            "tooltip": "The new instance generator based on Choco3 solver library",
            "accepted_format": "choco",
            "tool": "java",
            "tool_args": ["-jar", "~/bin/claferchocoig.jar", "--file=$filepath$", "--repl"],
            "tool_version_args": ["-jar", "~/bin/claferchocoig.jar", "--version"],
            "scope_options": {
                "set_default_scope" : {"command": "globalScope $value$\n"},
                "set_individual_scope": {"command": "scope $clafer$ $value$\n"},
                "inc_all_scopes" : {"command": "incGlobalScope $value$\n"},
                "inc_individual_scope": {"command": "incScope $clafer$ $value$\n"},
                "set_int_scope" : {"command": "maxInt $value$\n", "default_value": 127}
            },
            "control_buttons": [
                {"id": "next_instance", "command": "n\n", "label" : "Next", "tooltip": "Next Instance"},
                {"id": "reload", "command": "r\n", "label" : "Reset", "tooltip": "Reset instance generation, applied scopes and other settings"},
                {"id": "quit", "command": "q\n", "label" : "Quit", "tooltip": "Exit the IG safely"}
            ],
            "presentation_specifics": {
                "prompt_title": "",
                "no_more_instances": "No more instances found. Please consider increasing scopes"
            }
        },
    ]
}

```

**Notes**:

* If you make any changes to the `backends.json`, restart the web-tool completely to make the changes take effect.
* If you done your configuration properly, the tool will restart successfully and the backend should be listed in the `Backends` dropdown list. If the tool does not start, the reason may be either a syntax error in the backends.json file, or the paths specified in it are not correct or lead to an inaccessible JAR file. Also, check the `Execute` permission on the JAR file.

Installation from source code
-----------------------------

### Prerequisites

* [Maven 2+](http://maven.apache.org/download.cgi). Required for building the projects and linking all dependencies
* [ChocoSolver](https://github.com/gsdlab/chocosolver). This is a Maven dependency for the project, so it should be installed (`mvn install`) as well.

#### Important: Branches must correspond

All related projects are following the *simultaneous release model*.
The branch `master` contains releases, whereas the branch `develop` contains code under development.
When building the tools, the branches should match.
Releases from branches 'master` are guaranteed to work well together.
Development versions from branches `develop` should work well together but this might not always be the case.

#### Building

1. install the dependencies
2. open the command line terminal.
3. in some `<source directory>` of your choice, execute
  * `git clone git://github.com/gsdlab/ClaferChocoIG.git`
4. in `<source directory>/ClaferChocoIG`, execute
  * `mvn install`

Two binaries will appear in the `target` subfolder:
`claferchocoig-0.3.10.1-jar-with-dependencies.jar` that is standalone and contains all the required dependencies,
and `claferchocoig-0.3.10.1.jar` that is not.

### Installation

1. In `<source directory>/ClaferChocoIG`, execute
  * `make install to=<target directory>`

Need help?
==========
* Visit [language's website](http://clafer.org).
* Report issues to [issue tracker](https://github.com/gsdlab/ClaferChocoIG/issues)
