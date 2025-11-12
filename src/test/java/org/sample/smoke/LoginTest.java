package org.sample.smoke;

import java.nio.file.Paths;

import org.application.steps.HomePage;
import org.application.steps.LoginPage;

import org.framework.base.BasePage;
import org.framework.base.BaseTest;
import org.framework.lib.BrowserManager;
import org.framework.logger.ListenersTestNG;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import com.microsoft.playwright.Page;
import com.microsoft.playwright.Tracing;

@Listeners(ListenersTestNG.class)

public class LoginTest extends BaseTest {

	Page page;
	HomePage homePage;
	LoginPage loginPage;


	@BeforeClass
	public void Setup() {
		
		//Trace
		BrowserManager.getInstance().getContext().tracing().start(new Tracing.StartOptions()
				  .setScreenshots(true)
				  .setSnapshots(true)
				  .setSources(true));
		
		page = BrowserManager.getInstance().getPage();
		homePage = new HomePage(page);
		loginPage = new LoginPage(page);
		
	
	}

	@Test(description = "Login and Logout functionality", dataProvider = "portalCredentials")
	public void LoginAndLogoutFuncitonality(String testCase, String userName, String password, String scenario) {
		assignTestTitleToExtentReport(testCase + "- Practice Portal Login and Logout functionality : " + scenario);
		createStep("Login to Practice Portal");
		BasePage.openURL(prop.getProperty("env.url"));
		loginPage.loginPracticePortal(userName, password);
		loginPage.validateLoginFunctionality();
		createStep("Logout to Practice Portal");
		homePage.logoutFunctionality();

	}

	@AfterClass
	public void tearDown() {
		//Trace End
		BrowserManager.getInstance().getContext().tracing().stop(new Tracing.StopOptions()
				  .setPath(Paths.get("trace.zip")));
	}

	@DataProvider(name = "portalCredentials")
	public Object[][] portalCredentials() {
		return new Object[][] 
				{ 
			    { "Test Case3","student", "V2fcGf69cXD6chVeudHRLafV7X5Pn9p5", "with correct Password" },
				{ "Test Case4", "student", "V2fcGf69cXD6chVeudHRLafV7X5Pn9p5", "with correct credentials" },
				{ "Test Case5", "studen", "r9gdQ5BEzrFN6LImNzZHSATL3PYg97Bl", "with incorrect Username" } 
				};
	}

}
