
package org.framework.lib;

import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.ExtentTest;

public final class ExtentReportMGR {
    private static final ExtentReportMGR INSTANCE = new ExtentReportMGR();
    private final ThreadLocal<ExtentTest> extentTest = new ThreadLocal<>();
    
    private final ThreadLocal<Integer> stepNum = ThreadLocal.withInitial(() -> 1);
    public ThreadLocal<Integer> getStepNum() {
		return stepNum;
	}

	private ExtentReports extentReports;

    private ExtentReportMGR() {}

    public static ExtentReportMGR getInstance() {
        return INSTANCE;
    }

	
	public ExtentTest getExtentTest() {
		return extentTest.get();
	}

	
	public void setExtentTest(ExtentTest extent) {
		extentTest.set(extent);
		getStepNum().set(1);
	}


	public ExtentTest setExtentTestNode(ExtentTest extent, String nodeName) {
		int currentStepNum = getStepNum().get();
		extentTest.set(extent.createNode("Step " + currentStepNum+":" + nodeName));
		getStepNum().set(currentStepNum + 1);
		return getExtentTest();
	}


	public void removeExtentTest() {
		if (extentTest.get() != null) {
			extentTest.remove();
			extentTest.set(null);
		}
		if (stepNum.get() != null) {
			stepNum.remove();			
		}
	}

	public ExtentReports getExtentReports() {
		return extentReports;
	}

	
	public void setExtentReports(ExtentReports extentReports) {
		this.extentReports = extentReports;
	}
	
	/*
	 * public void removeCurrentTest(final ExtentReports reports, final ExtentTest
	 * extentTest) { reports.removeTest(extentTest); }
	 */

}