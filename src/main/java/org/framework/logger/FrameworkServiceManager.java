package org.framework.logger;

import org.testng.Assert;

import org.framework.interfaces.IExceptionHandler;
import org.framework.interfaces.ILogger;

public class FrameworkServiceManager implements ILogger, IExceptionHandler {
	public static int QUICK_TIMEOUT, DEFAULT_TIMEOUT, MEDIUM_TIMEOUT, LONG_TIMEOUT, EXTRA_LONG_TIMEOUT;

	@Override
	//hard - Break the execution
	public void exception(Exception e) {
	    try {
	    	
	        StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
	        String callerClass = "Unknown";
	        String callerMethod = "Unknown";
	        int lineNumber = -1;
	        for (int i = 2; i < stackTrace.length; i++) { // skip getStackTrace and this method
	            StackTraceElement element = stackTrace[i];
	            if (!element.getClassName().equals(this.getClass().getName())) {
	                callerClass = element.getClassName();
	                callerMethod = element.getMethodName();
	                lineNumber = element.getLineNumber();
	                break;
	            }
	        }
	        
	        String message = String.format(
	            "<b>Exception :</b>%s ,%s ,%d #%s",
	            callerClass, callerMethod, lineNumber, e != null ? e.getMessage() : "null"
	        );
	        
	        fail(message);
	        System.err.printf(
	            "\u001B[1mAN EXCEPTION OCCURRED\u001B[0m in class '%s', method '%s', at line %d. Message: %s%n",
	            callerClass, callerMethod, lineNumber, e != null ? e.getMessage() : "null"
	        );
	        
	        Assert.fail("Test terminated due to exception: " + (e != null ? e.getMessage() : "null"));
	    } catch (Exception ex) {
	        System.err.println("Error in exception handler: " + ex.getMessage());
	        Assert.fail("Test terminated due to exception handler error: " + ex.getMessage());
	    }
	}

	@Override
	//soft - Will continue the execution
	public void exception(Exception e, boolean terminate) {
	    if (terminate) {
	        exception(e);
	    } else {
	        fail("<b>Exception :</b>" + (e != null ? e.getMessage() : "No Exception Message"));
	    }
	}
}