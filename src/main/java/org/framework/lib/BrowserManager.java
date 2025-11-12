
package org.framework.lib;

import java.util.ArrayList;
import java.util.Collections;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;

import org.framework.base.BasePage;
import org.framework.base.BaseTest;

public final class BrowserManager extends BasePage {
	private static final ThreadLocal<String> downloadFolder = new ThreadLocal<>();
	private static final ThreadLocal<Playwright> playwright = new ThreadLocal<>();
	private static final ThreadLocal<Browser> browser = new ThreadLocal<>();
	private static final ThreadLocal<BrowserContext> context = new ThreadLocal<>();
	private static final ThreadLocal<Page> page = new ThreadLocal<>();
	private static final BrowserManager INSTANCE = new BrowserManager();
	private static final ThreadLocal<String> testName = new ThreadLocal<>();
	
    private BrowserManager() {}

    
    public static BrowserManager getInstance() {
        return INSTANCE;
    }


	public void launchBrowser(String browserType) {
		boolean headless = Boolean.parseBoolean(BaseTest.prop.getProperty("env.headless", "false"));
		int slowmo=Integer.parseInt(BaseTest.prop.getProperty("SLOWMOVE", "100"));
		int width = Integer.parseInt(BaseTest.prop.getProperty("browser.width", "1920"));
	    int height = Integer.parseInt(BaseTest.prop.getProperty("browser.height", "1080"));
		ArrayList<String> arguments = new ArrayList<>();
		Collections.addAll(arguments,
                "--no-sandbox",
                "--disable-dev-shm-usage"
               /* "--disable-background-networking",
                "--disable-background-timer-throttling",
                "--disable-backgrounding-occluded-windows",
                "--disable-default-apps",
                "--disable-extensions",
                "--disable-sync",
                "--disable-gpu",
                "--no-first-run",
                "--no-default-browser-check",
                "--disable-features=Translate,InterestFeedContentSuggestions",
                "--mute-audio",
                "--disable-client-side-phishing-detection",
                "--disable-component-update",
                "--disable-domain-reliability",
                "--disable-print-preview",
                "--disable-prompt-on-repost",
                "--disable-renderer-backgrounding",
                "--disable-ipc-flooding-protection"*/
        );
		arguments.add(headless ? "--window-size=" + width + "," + height : "--start-maximized");
		Playwright pw = Playwright.create();
		playwright.set(pw);
		Browser b;
		switch (browserType.toLowerCase()) {
		case "firefox":
			b = pw.firefox().launch(new BrowserType.LaunchOptions().setHeadless(headless).setSlowMo(slowmo));
			break;
		case "webkit":
			b = pw.webkit().launch(new BrowserType.LaunchOptions().setHeadless(headless).setSlowMo(slowmo));
			break;
		case "chromium":
			b = pw.chromium().launch(new BrowserType.LaunchOptions().setHeadless(headless).setArgs(arguments).setSlowMo(slowmo));
			break;
		default:
			b = pw.chromium().launch(
					new BrowserType.LaunchOptions().setHeadless(headless).setArgs(arguments).setChannel("chrome").setSlowMo(slowmo));
			break;
		}
		browser.set(b);
		Browser.NewContextOptions options = new Browser.NewContextOptions()
		        .setViewportSize(headless ? new com.microsoft.playwright.options.ViewportSize(width, height) : null);
        BrowserContext ctx = b.newContext(options);
		ctx.setDefaultTimeout(DEFAULT_TIMEOUT);
		context.set(ctx);
		Page p = ctx.newPage();
		page.set(p);
	}


	public Page getPage() {
		return page.get();
	}


	public BrowserContext getContext() {
		return context.get();
	}

	
	public Browser getBrowser() {
		return browser.get();
	}


	public void quit() {
	    try {
	        
	        // Close browser context and browser
	        if (context != null) {
	            context.get().close();
	        }
	        if (browser != null) {
	            browser.get().close();
	        }
	        if (playwright != null) {
	            playwright.get().close();
	        }
	        
	    } catch (Exception e) {
	        System.err.println("Error during browser quit: " + e.getMessage());
	    }
	}


	public String getDownloadfolder() {
		return downloadFolder.get();
	}


	public void setDownloadfolder(String folderRef) {
		downloadFolder.set(folderRef);
	}

	
	public void unload() {
		// driver.remove();
	}

	public String getTestName() {
		return testName.get();
	}

	public void setTestName(String testNameRef) {
		testName.set(testNameRef);
	}

	public void closePage() {
		try {
			if (context != null)
				context.get().close();
			// if (page.get() != null) page.get().close();
		} catch (Exception e) {
			fail("Failed to close the current page");
			exception(e);
		}

	}


	public static BrowserContext getNewContext() {
		return browser.get().newContext(new Browser.NewContextOptions().setViewportSize(null));

	}

	
	public static void setContext(BrowserContext context) {
		if (context != null) {
			System.out.println("DEBUG: BrowserManager.setContext() called - handling trace transfer");
			
			// Get the current context before switching
			BrowserContext oldContext = BrowserManager.context.get();
			
			context.setDefaultTimeout(LONG_TIMEOUT);
			BrowserManager.context.set(context);
			Page newPage = context.newPage();
			setPage(newPage);
			
			System.out.println("DEBUG: New context set successfully");
		}
	}
	public static void setPage(Page newPage) {
		if (newPage != null) {
			page.set(newPage);
		}
	}
}