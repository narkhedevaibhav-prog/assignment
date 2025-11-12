package org.framework.logger;

import java.util.Arrays;

import org.framework.base.BaseTest;
import org.framework.interfaces.ILogger;
import org.framework.lib.Util;
import org.testng.ISuiteListener;
import org.testng.ITestListener;
import org.testng.ITestResult;

import com.aventstack.extentreports.MediaEntityModelProvider;

public class ListenersTestNG implements ITestListener, ISuiteListener, ILogger {

	@Override
	public void onTestSuccess(ITestResult result) {
		String testName = result.getMethod().getMethodName();

		System.out.println("Test Finished: " + testName + " | Status: \u001B[32mSUCCESS\u001B[0m");

		ITestListener.super.onTestSuccess(result);
	}

	@Override
	public void onTestFailure(ITestResult result) {
		String issueSummary;
		String description;
		String testName = result.getMethod().getMethodName();
		MediaEntityModelProvider screenshot = getScreenShot();
		
		if (BaseTest.prop.getProperty("jiraConnection").equalsIgnoreCase("true")) {
		    issueSummary = result.getMethod().getDescription();
		    description  = result.getMethod().getDescription() + " || " + Arrays.toString(result.getParameters());
		    
		    Util.getInstance().createIssueWithAttachment(issueSummary, description);
		}
				
		

		ITestListener.super.onTestFailure(result);
	}

}