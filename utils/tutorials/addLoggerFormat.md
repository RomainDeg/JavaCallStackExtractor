# Adding a new logging format 
JavaCallStackExtractor currently supports the following logging formats:
- json – A structured format suitable for integration with other tools.

## Want to know what data is available to the logger?
Check the interface [ILoggerFormat](../../src/main/java/app/logging/ILoggerFormat.java) to see which methods need to be implemented and what information the logger has access to.

## Want to create your own logger ?
You can add a new logger by extending AbstractLoggerFormat.
Here's a simplified example based on [LoggerJson](../../src/main/java/logging/LoggerJson.java) :
```java
public class LoggerJson extends AbstractLoggerFormat {

    public LoggerJson(String outputName, String Extension) {
        super(outputName, Extension);
    }
    
    // Implement required methods from ILoggerFormat here
}
```

To create your custom logger, simply provide concrete implementations for all methods defined in the ILoggerFormat interface.

## How to enable your logger in the application?
To make your custom logger available at runtime: 
1. Open [StackExtractor](../../src/main/java/extractors/StackExtractor.java)
2. Locate the method registerAllLoggers().
3. Add a new entry to the res map using the following format:
```declarative
    res.put("yourKeyword", (name, extension) -> new YourLogger(name, extension));
```
Replace yourKeyword with the identifier you want to use (e.g., "xml", "csv"), and YourLogger with the name of your custom logger class.