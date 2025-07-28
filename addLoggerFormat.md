## Adding a new logging format :
The currents logging formats are :
- txt, which is just for visual analysis
- json, which is for importing useful for importing in other programs

1. You want know what information can the logger use ?  
See [ILoggerFormat](./src/main/java/logging/ILoggerFormat.java)

2. You want to write another logger ?
As an exemple here how [LoggerJson](./src/main/java/logging/LoggerJson.java) is added :
```java
public class LoggerJson extends AbstractLoggerFormat {

    public LoggerJson(String outputName, String Extension) {
        super(outputName, Extension);
    }
    
    ....
    
}
```

The only thing needed to be done is offering an implementation for every method of ILoggerFormat.

3. You have you new logger, and you want to make it work on the application ?
Go to [StackExtractor](./src/main/java/extractors/StackExtractor.java) and modify the method registerAllLoggers() to add :  
res.put("<b>your keyword</b>", (name, extension) -> new <b>YourLogger</b>(name, extension));