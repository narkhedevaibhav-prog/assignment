package org.framework.interfaces;

import java.io.IOException;

import org.testng.ITestResult;
import org.testng.Reporter;

import com.aventstack.extentreports.MediaEntityBuilder;
import com.aventstack.extentreports.MediaEntityModelProvider;
import com.microsoft.playwright.Page;

import org.framework.base.BaseTest;
import org.framework.lib.BrowserManager;
import org.framework.lib.ExtentReportMGR;

public interface ILogger {
	

	
	// Pass ---------------------------------------
	default void pass(String description) {
		pass(description, null);
		
	}

	default void pass(String description, MediaEntityModelProvider provider) {
		ExtentReportMGR.getInstance().getExtentTest().pass(description, provider);
		ExtentReportMGR.getInstance().getExtentReports().flush();
	}

	default void pass(String description, boolean captureScreenShot) {
		MediaEntityModelProvider provider = captureScreenShot ? getScreenShot() : null;
		pass(description, provider);
	}

	
	default void pass(String description, boolean captureScreenShot, Page page) {
		MediaEntityModelProvider provider = captureScreenShot ? getScreenShot(page) : null;
		pass(description, provider);
	}

	// Info ---------------------------------------
	default void info(String description) {
		info(description, null);
	}

	default void info(String description, MediaEntityModelProvider provider) {
		ExtentReportMGR.getInstance().getExtentTest().info(description, provider);
		ExtentReportMGR.getInstance().getExtentReports().flush();
	}

	default void info(String description, boolean captureScreenShot) {
		MediaEntityModelProvider provider = captureScreenShot ? getScreenShot() : null;
		info(description, provider);
	}

	// Newly Added - 09/24
	default void info(String description, boolean captureScreenShot, Page page) {
		MediaEntityModelProvider provider = captureScreenShot ? getScreenShot(page) : null;
		info(description, provider);
	}

	// Error ---------------------------------------
	default void error(String description) {
		error(description, getScreenShot());
	}

	default void error(String description, MediaEntityModelProvider provider) {
		ExtentReportMGR.getInstance().getExtentTest().error(description, provider);
		ExtentReportMGR.getInstance().getExtentReports().flush();
	}

	default void error(String description, boolean captureScreenShot) {
		MediaEntityModelProvider provider = captureScreenShot ? getScreenShot() : null;
		error(description, provider);
	}


	default void error(String description, boolean captureScreenShot, Page page) {
		MediaEntityModelProvider provider = captureScreenShot ? getScreenShot(page) : null;
		error(description, provider);
	}

	// Warning ---------------------------------------
	default void warning(String description) {
		warning(description, getScreenShot());
	}

	default void warning(String description, MediaEntityModelProvider provider) {
		ExtentReportMGR.getInstance().getExtentTest().warning(description, provider);
		ExtentReportMGR.getInstance().getExtentReports().flush();
	}

	default void warning(String description, boolean captureScreenShot) {
		MediaEntityModelProvider provider = captureScreenShot ? getScreenShot() : null;
		warning(description, provider);
	}

	
	default void warning(String description, boolean captureScreenShot, Page page) {
		MediaEntityModelProvider provider = captureScreenShot ? getScreenShot(page) : null;
		warning(description, provider);
	}

	// Fail---------------------------------------
	default void fail(String description) {
		fail(description, getScreenShot());
		 ITestResult iTestResult = Reporter.getCurrentTestResult();
		 iTestResult.setStatus(ITestResult.FAILURE);
	}

	default void fail(String description, MediaEntityModelProvider provider) {
		ExtentReportMGR.getInstance().getExtentTest().fail(description, provider);
		 ITestResult iTestResult = Reporter.getCurrentTestResult();
		 iTestResult.setStatus(ITestResult.FAILURE);
		ExtentReportMGR.getInstance().getExtentReports().flush();
	}

	default void fail(String description, boolean captureScreenShot) {
		MediaEntityModelProvider provider = captureScreenShot ? getScreenShot() : null;
		fail(description, provider);
		ITestResult iTestResult = Reporter.getCurrentTestResult();
		iTestResult.setStatus(ITestResult.FAILURE);
	}


	default void fail(String description, boolean captureScreenShot, Page page) {
		MediaEntityModelProvider provider = captureScreenShot ? getScreenShot(page) : null;
		fail(description, provider);
		ITestResult iTestResult = Reporter.getCurrentTestResult();
		iTestResult.setStatus(ITestResult.FAILURE);
	}

	default String getTestName() {
		try {
			return BrowserManager.getInstance().getTestName();
		} catch (Exception e) {
			return "";
		}
	}

	default MediaEntityModelProvider getScreenShot() {
		try {
			Page page = BrowserManager.getInstance().getPage();
			if (page != null) {

				Page.ScreenshotOptions options = new Page.ScreenshotOptions()
					    .setTimeout(12000); // disables timeout
				
					byte[] screenshotBytes = page.screenshot(options);


				// byte[] screenshotBytes = page.screenshot(new Page.ScreenshotOptions());
				String base64Screenshot = java.util.Base64.getEncoder().encodeToString(screenshotBytes);
				return MediaEntityBuilder.createScreenCaptureFromBase64String(base64Screenshot).build();
			}
		} catch (IOException e) {
			return null;
		}
		return null;
	}

	default MediaEntityModelProvider getScreenShot(Page page) {
		try {
			if (page != null) {

				byte[] screenshotBytes = page.screenshot(new Page.ScreenshotOptions());

				String base64Screenshot = java.util.Base64.getEncoder().encodeToString(screenshotBytes);
				return MediaEntityBuilder.createScreenCaptureFromBase64String(base64Screenshot).build();
			}
		} catch (IOException e) {
			return null;
		}
		return null;
	}
}
