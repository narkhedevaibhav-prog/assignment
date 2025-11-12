package org.application.steps;

import org.framework.base.BasePage;
import org.framework.lib.BrowserManager;
import org.framework.lib.Util;

import com.microsoft.playwright.Page;

public class LoginPage extends BasePage {
	private Page page;

	public LoginPage(Page pageRef) {
		this.page = pageRef;
		
	}

	//-----------------LOCATOR SECTION START-----------------
	String userNamefld = "//*[text()='Username']";
	String pwdfld = "#password";
	String submitBtn = "#submit";
	String successLoginLbl = "//*[text()='Logged In Successfully']";
	//-----------------LOCATOR SECTION END-----------------
	
	
	
	//-----------------STEP SECTION START-----------------
	public void loginPracticePortal(String userName, String password) {

		try {

			fill(page.locator(userNamefld), userName);
			info("Entered Username :" + userName);
			
			fill(page.locator(pwdfld), Util.decrypt(password));
			info("Entered Password: *********");
			
			click(page.locator(submitBtn));
			info("Loging in");
			waitForPageLoad();

		} catch (Exception e) {
			exception(e);

		}

	}

	public void validateLoginFunctionality() {

		try {

			if (page.locator(successLoginLbl).count() > 0) {
				pass("Login Succussful", true);
			} else {
				fail("Login Unsuccesful");
				exception(new Exception("Login Failed"));
			}

		} catch (Exception e) {
			exception(e);
		}

	}
	
	//-----------------STEP SECTION ENDS-----------------

}
