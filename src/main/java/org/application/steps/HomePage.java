package org.application.steps;

import com.microsoft.playwright.Page;

import java.time.Duration;

import org.framework.base.BasePage;
import org.framework.base.BaseTest;

public class HomePage extends BasePage {
	private Page page;

	public HomePage(Page pageRef) {
		this.page = pageRef;
	}
	
	//-----------------LOCATOR SECTION START-----------------
	String logOutBtn = "//a[contains(text(), 'Log out')]";
	String testLoginLbl = "//*[contains(text(), 'Test login')]";
	//-----------------LOCATOR SECTION END-----------------
	
	
	
	//-----------------STEP SECTION START-----------------
	public void logoutFunctionality() {
		
		try {
			
			info("Clicking on logout button");
			click(page.locator(logOutBtn));
			
			waitForLocator(page.locator(testLoginLbl), Duration.ofMillis(DEFAULT_TIMEOUT));
			if(page.locator(testLoginLbl).count()>0) {
				pass("Page is successfully logged out",true);
			}else {
				fail("Not able to logout");
			}
			
		} catch (Exception e) {
			exception(e);
		}

	}
	
	//-----------------STEP SECTION END-----------------

}
