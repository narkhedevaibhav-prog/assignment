package org.framework.base;

import java.time.Duration;
import java.util.Properties;

import org.framework.lib.BrowserManager;
import org.framework.lib.ExtentReportMGR;
import org.framework.logger.FrameworkServiceManager;
import org.testng.SkipException;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.LoadState;
import com.microsoft.playwright.options.WaitForSelectorState;

public class BasePage extends FrameworkServiceManager {
	BaseTest baseTest;

	Properties prop = BaseTest.prop;

	// ******************************************

	// ----------ELEMENT STATE---------------------

	public static boolean isElementEnable(String selector) {
		try {
			Page page = BrowserManager.getInstance().getPage();
			Locator locator = page.locator(selector);
			return locator.isEnabled();
		} catch (Exception e) {
			return false;
		}
	}

	public static boolean isElementPresent(Page page, String selector) {
		try {
			// Optional wait
			page.waitForTimeout(1000);

			Locator locator = page.locator(selector);
			int count = locator.count();

			if (count == 0) {
				return false;
			}

			return locator.first().isVisible();
		} catch (Exception e) {
			return false;
		}
	}

	public static boolean isElementPresent(String selector) {
		try {
			Page page = BrowserManager.getInstance().getPage();
			Locator locator = page.locator(selector);
			return locator.count() > 0 && locator.isVisible();
		} catch (Exception e) {
			ExtentReportMGR.getInstance().getExtentTest().error("Error checking element presence: " + e.getMessage());
			return false;
		}
	}

	// -------------------PAGE ACTION

	public void click(Object element) {

		try {
			Locator locator;
			if (element instanceof Locator) {
				locator = (Locator) element;
				waitForVisible(locator);
			} else if (element instanceof String) {
				locator = BrowserManager.getInstance().getPage().locator((String) element);
			} else {
				throw new IllegalArgumentException("Parameter must be either Locator or String");
			}

			
		//	locator.waitFor(
					//new Locator.WaitForOptions().setTimeout(DEFAULT_TIMEOUT).setState(WaitForSelectorState.VISIBLE));
			
			if (!locator.isEnabled()) {
				Exception e = new Exception("Element is not enabled.");
				exception(e);
				// throw new RuntimeException("Element is not enabled.");
			}
			
			locator.evaluate("element => element.style.border = '2px solid red'");
			locator.click(new Locator.ClickOptions().setTimeout(DEFAULT_TIMEOUT));

		} catch (Exception e) {

			ExtentReportMGR.getInstance().getExtentTest().error("Failed to click element " + e.getMessage());

			exception(e);
		}
	}


	public static void clear(String selector) {
		Page page = BrowserManager.getInstance().getPage();
		Locator locator = page.locator(selector);
		locator.fill("");
	}

	public static String getText(String selector) {
		Page page = BrowserManager.getInstance().getPage();
		Locator locator = page.locator(selector);
		return locator.textContent().trim();
	}

	public void fill(Object element, String text) {

		try {
			Locator locator;

			if (element instanceof Locator) {
				locator = (Locator) element;

			} else if (element instanceof String) {
				// You'll need access to page instance here
				locator = BrowserManager.getInstance().getPage().locator((String) element);
			} else {
				throw new IllegalArgumentException("First parameter must be either Locator or String");
			}

			locator.waitFor(
					new Locator.WaitForOptions().setTimeout(DEFAULT_TIMEOUT).setState(WaitForSelectorState.VISIBLE));

			if (!locator.isEnabled()) {
				throw new RuntimeException("Element is not enabled.");
			}

			locator.fill(text);
			
		} catch (Exception e) {
			String elementInfo = element instanceof String ? "selector '" + element + "'" : text;
			ExtentReportMGR.getInstance().getExtentTest()
					.error("Failed to fill element " + elementInfo + ": " + e.getMessage());
			throw new RuntimeException("Failed to fill element " + elementInfo + ": " + e.getMessage(), e);
		}
	}

	public static void dblClick(Locator locator) {
		try {
			/*
			 * locator.waitFor(new Locator.WaitForOptions().setTimeout(timeout.toMillis())
			 * .setState(WaitForSelectorState.VISIBLE));
			 */
			waitForVisible(locator);

			if (!locator.isEnabled()) {
				throw new RuntimeException("locator is not enabled.");
			}

			locator.dblclick(new Locator.DblclickOptions().setTimeout(LONG_TIMEOUT));

		} catch (Exception e) {
			throw new RuntimeException("Failed to click locator: " + locator, e);
		}
	}

	/*----------------------------------------------------------Others----------------------------------------------------------------------*/

	public static void highLight(String selector) {
		try {
			Page page = BrowserManager.getInstance().getPage();
			Locator locator = page.locator(selector);
			locator.evaluate("element => element.style.border='3px solid red'");
		} catch (Exception e) {
			ExtentReportMGR.getInstance().getExtentTest().error(e);
		}
	}

	public static void highlightBg(String selector) {
		try {
			Page page = BrowserManager.getInstance().getPage();
			Locator locator = page.locator(selector);
			locator.evaluate(
					"element => element.setAttribute('style', 'background: #00FF00; border: 4px dotted blue;')");
		} catch (Exception e) {
			ExtentReportMGR.getInstance().getExtentTest().error(e);
		}
	}

	public static void openURL(String url) {
		try {
			Page page = BrowserManager.getInstance().getPage();
			page.navigate(url);
			ExtentReportMGR.getInstance().getExtentTest().info("Navigated to URL: " + url);
		} catch (Exception e) {
			ExtentReportMGR.getInstance().getExtentTest().error("Failed to open URL '" + url + "': " + e.getMessage());
			throw new RuntimeException("Failed to open URL '" + url + "': " + e.getMessage(), e);
		}
	}

	public String getProperty(String envValue) {
		try {
			return BaseTest.prop.getProperty(envValue);

		} catch (Exception e) {
			exception(e);
		}
		return null;

	}

	public static boolean waitForElementVisible(Page page, String selector, int timeoutInSeconds) {
		try {
			Locator locator = page.locator(selector);
			locator.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE)
					.setTimeout(timeoutInSeconds * 1000)); // timeout in milliseconds
			return true;
		} catch (Exception e) {
			return false;
		}
	}

	public static Locator waitForLocator(Locator locator, Duration timeout) {
		locator.waitFor(new Locator.WaitForOptions().setTimeout(timeout.toMillis()));
		return locator;
	}

	public static void waitForVisible(Locator locator, Duration timeout) {
		locator.waitFor(
				new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE).setTimeout(timeout.toMillis()));
	}

	public static void waitForVisible(Locator locator) {
		locator.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE).setTimeout(LONG_TIMEOUT));
	}

	public void waitForPageLoad() {
		waitForPageLoad(LONG_TIMEOUT);
	}

	public void waitForPageLoad(long timeoutMillis) {
		try {
			info("Waiting for page to load completely");
			BrowserManager.getInstance().getPage().waitForLoadState(LoadState.LOAD,
					new Page.WaitForLoadStateOptions().setTimeout(timeoutMillis));
			info("Page loaded successfully");
		} catch (Exception e) {
			error("Page load timeout: " + e.getMessage());
			throw new RuntimeException("Page load timeout: " + e.getMessage(), e);
		}
	}

	public void waitForEnable(Locator locator) {
		try {

			// Wait until the element is visible first
			locator.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE));

			// Poll until it's enabled (with timeout)
			int timeoutMs = MEDIUM_TIMEOUT;
			int intervalMs = 250;
			int waited = 0;

			while (!locator.isEnabled() && waited < timeoutMs) {
				BrowserManager.getInstance().getPage().waitForTimeout(intervalMs);
				waited += intervalMs;
			}

		} catch (Exception e) {
			exception(e);
		}
	}

	public static void waitForMSeconds(int timeoutInMilliSeconds) {
		try {
			BrowserManager.getInstance().getPage().waitForTimeout(timeoutInMilliSeconds);
		} catch (Exception e) {
			ExtentReportMGR.getInstance().getExtentTest().error(e.getMessage());
		}
	}

	public static boolean waitForElementVisible(String selector, int timeoutInSeconds) {
		try {
			waitForMSeconds(1000);
			Page page = BrowserManager.getInstance().getPage();
			Locator locator = page.locator(selector);
			locator.waitFor(new Locator.WaitForOptions().setTimeout(timeoutInSeconds * 1000)
					.setState(WaitForSelectorState.VISIBLE));
			return true;
		} catch (Exception e) {
			ExtentReportMGR.getInstance().getExtentTest().error(e.getMessage());
		}
		return false;
	}

}
