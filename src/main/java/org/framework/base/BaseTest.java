package org.framework.base;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.sql.Connection;
import java.util.Optional;
import java.util.Properties;

import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeSuite;

import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.reporter.ExtentHtmlReporter;

import org.framework.lib.BrowserManager;
import org.framework.lib.ExtentReportMGR;
import org.framework.lib.Util;
import org.framework.logger.FrameworkServiceManager;


public class BaseTest extends FrameworkServiceManager {
	
    public static Properties prop;
    public static Connection con;
    public ExtentHtmlReporter htmlReporter;
    protected static ExtentReports extent;
    
    protected static ThreadLocal<ExtentTest> logger = new ThreadLocal<>();
    protected static ThreadLocal<ExtentTest> step = new ThreadLocal<>();
    
    public static String projectPath = Paths.get("").toAbsolutePath().toString();
    File reportBackupDir;
    File rootDir;


	@BeforeSuite()
	public void initiatePropertyFile() {
		Util.getInstance().CreatePropertyfile();
		prop = Util.getInstance().prop;
		Util.deleteFlag = true;
		int baseTimeout;
		
		 baseTimeout = Integer.parseInt(prop.getProperty("DEFAULT_TIMEOUT"));
		
		 //defining timout
		DEFAULT_TIMEOUT = baseTimeout;
		QUICK_TIMEOUT = Math.max(1, (baseTimeout + 9) / 10);
		MEDIUM_TIMEOUT = (baseTimeout + 1) / 2;
		LONG_TIMEOUT = (baseTimeout <= Integer.MAX_VALUE / 2) ? baseTimeout * 2 : Integer.MAX_VALUE;
		EXTRA_LONG_TIMEOUT = (baseTimeout <= Integer.MAX_VALUE / 4) ? baseTimeout * 4 : Integer.MAX_VALUE;
	}


	@BeforeSuite(dependsOnMethods = { "initiatePropertyFile" })
	public void reportSetup() {
		ExtentReportMGR.getInstance().setExtentReports(Util.getInstance().initExtentReport(prop));
		extent = ExtentReportMGR.getInstance().getExtentReports();

	}

	@BeforeClass
	public void setUp() {
	    try {
	  
	        String testName = this.getClass().getSimpleName();
	        BrowserManager.getInstance().setDownloadfolder(projectPath + "\\downloads\\" + testName);
	        BrowserManager.getInstance().setTestName(testName);
	        // Launch Playwright browser (default to Chromium, or use property)
	        String browserType = Optional.ofNullable(prop.getProperty("env.browser")).orElse("chromium");
	        BrowserManager.getInstance().launchBrowser(browserType);
	        
	    } catch (Exception e) {
	    }
	}

	
	@AfterMethod
	public void flushReport() throws IOException {
	    try {
	        ExtentReportMGR.getInstance().getExtentReports().flush();
	    } catch (Exception e) {
	    }
	}
	@AfterClass
	public void clear() {
		ExtentReportMGR.getInstance().removeExtentTest();
	}


	@AfterSuite
	public void closeBrowser() throws IOException {
		try {
			//ExtentReportMGR.getInstance().getExtentReports().flush();
			// Open the test report in the default desktop browser.
			openReport();
		} catch (IOException e) {
		}
	}

	public void openReport() throws IOException {
		Optional.ofNullable(prop.getProperty("env.openReport")).map(Boolean::parseBoolean).ifPresent(isOpen -> {
			if (isOpen) {
				try {
					Desktop.getDesktop().open(new File(projectPath + "\\test-output\\extent-report\\"
							+ Paths.get(projectPath).getFileName() + "_TestResult.html"));
					// Desktop.getDesktop().open(new File(projectPath +
					// "\\test-output\\extent-report\\ApplicationReport.html"));

				} catch (IOException e) {
					exception(e);
				}
			}
		});

	}



	public void assignTestTitleToExtentReport(String reportTitle, String...ticketID) {
		String jiraID = (ticketID.length > 0 && ticketID[0] != null && !ticketID[0].isEmpty())
			    ? String.format(" [<a href='https://americanbureauofshipping.atlassian.net/browse/%s' target='_blank'>%s</a>]", ticketID[0], ticketID[0])
			    : "";
		String methodName = Thread.currentThread().getStackTrace()[2].getMethodName();
		ExtentTest test = ExtentReportMGR.getInstance().getExtentReports().createTest(reportTitle, methodName+":"+jiraID);
		logger.set(test);
		step.set(test);
		ExtentReportMGR.getInstance().setExtentTest(test);
		
		// Add category assignment if feature is available
		try {
			String feature = Optional.ofNullable(System.getProperty("testingType")).map(String::toUpperCase)
					.orElse(prop.getProperty("env.feature"));
			if (feature != null && !feature.isEmpty()) {
				test.assignCategory(feature);
			}
		} catch (Exception e) {
			System.out.println("Failed to assign category: " + e.getMessage());
		}
	}	


	public void createStep(String stepName) {
		ExtentTest node = ExtentReportMGR.getInstance().setExtentTestNode(step.get(), stepName);
		logger.set(node);
	}
}
