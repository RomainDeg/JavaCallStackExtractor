## Here is the default config.json

```json
{
  "vm": {
    "host": "localhost",
    "port": "5006"
  },
  "breakpoint": {
    "className": "java.lang.Runtime",
    "methodName": "exec",
    "methodArguments": [
      "java.lang.String"
    ],
    "repBefore": 0,
    "repetition": 1
  },
  "sourceMethod": "main",
  "maxDepth" : 20,
  "logging": {
    "format"  : "json",
    "outputName" : "JDIOutput",
    "extension": "cs"

  }
}
```

## Do you want to have multiple configs?
By default, JDIAttach will search for a file named "config.json",  but you can give the name of your config file in the program arguments of JDIAttach (for instance, in the 'Run configurations' of the Eclipse IDE).

## Breakdown of every variable

### vm
This variable holds informations about the vm you will attach to
- host : Name of the host (if done locally it will stay at "localhost")
- port : address of the VM (if using the given argument in the part 1, will stay on "5006")

### breakpoint
This variable describes the method you want your callstack to stop on.
- className :  the name of the class where the method is situated
- methodName : the name of the method
- methodArguments : name of all arguments type of the method in the declaration order
- repBefore : the number of time you want to ignore the breakpoint before actually stopping 
- repetition <b>!!not implemented yet!!</b>: the number of time you want to stop on the breakpoint 

### sourceMethod
This variable correspond to the name of the method starting the thread (generally it will be the main)

### maxDepth
This variable define the maximum depth recursion of the instance logging, set 0 for max depth


### logging
This variable holds information for the logging format and wanted output.
- format : the logging format, can be either txt or json (Other can be easily added see [addLoggerFormat.md](addLoggerFormat.md))
- outputName : name of the output file
- extension : extension of the output file, default at "cs", this extension is important for an easier importation of the callstack in Moose (See [FamixCallStack](https://github.com/LeoDefossez/FamixCallStack#))
