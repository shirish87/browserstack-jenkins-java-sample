package net.codestory;

import net.codestory.http.WebServer;
import net.codestory.http.misc.Env;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;

import java.net.URL;
import java.util.Iterator;

import static net.codestory.AngularServer.WebConfiguration;

public class AngularTest {
	private static final String ENV_USERNAME = "BROWSERSTACK_USER";
	private static final String ENV_ACCESSKEY = "BROWSERSTACK_ACCESSKEY";
	private static final String ENV_BROWSERS = "BROWSERSTACK_BROWSERS";
	private static final String ENV_LOCAL = "BROWSERSTACK_LOCAL";

	private static final String HUB_ENDPOINT = "https://%s:%s@hub.browserstack.com/wd/hub";

	private WebServer webServer;

	private JSONArray jsonBrowsers;
	private String browserstackUsername, browserstackAccessKey, isLocalEnabled;

	@Before
	public void setUp() throws Exception {
		webServer = new WebServer() {
			@Override
			protected Env createEnv() {
				return Env.prod();
			}
		}.configure(new WebConfiguration()).start(3000);

		browserstackUsername = System.getenv(ENV_USERNAME);
		browserstackAccessKey = System.getenv(ENV_ACCESSKEY);

		if (browserstackUsername == null || browserstackUsername.trim().isEmpty() ||
				browserstackAccessKey == null || browserstackAccessKey.trim().isEmpty()) {
			throw new IllegalArgumentException("Invalid environment variable value(s) for " + ENV_USERNAME + " or " + ENV_ACCESSKEY);
		}

		String browserstackBrowsers = System.getenv(ENV_BROWSERS);
		if (browserstackBrowsers == null || browserstackBrowsers.trim().isEmpty()) {
			throw new IllegalArgumentException("Invalid environment variable value for " + ENV_BROWSERS);
		}

		isLocalEnabled = System.getenv(ENV_LOCAL);
		isLocalEnabled = (isLocalEnabled != null && isLocalEnabled.trim().equals("1")) ? "true" : "false";
		jsonBrowsers = new JSONArray(browserstackBrowsers);
	}

	@After
	public void tearDown() throws Exception {
		webServer.stop();
	}

	@Test
	public void testSimple() throws Exception {
		DesiredCapabilities caps = new DesiredCapabilities();
		caps.setCapability("build", "Sample Jenkins Project");
		caps.setCapability("browserstack.local", isLocalEnabled);
		caps.setCapability("browserstack.debug", "true");

		for (int i = 0; i < jsonBrowsers.length(); i++) {
			JSONObject browser = jsonBrowsers.getJSONObject(0);
			Iterator<?> keys = browser.keys();

			while (keys.hasNext()) {
				String key = (String) keys.next();
				if (browser.get(key) instanceof String) {
					caps.setCapability(key, (String) browser.get(key));
				}
			}

			String endpoint = String.format(HUB_ENDPOINT, browserstackUsername, browserstackAccessKey);
			System.out.println("Running on " + browser.toString());
			WebDriver driver = new RemoteWebDriver(new URL(endpoint), caps);

			driver.get("http://localhost:" + webServer.port());
			System.out.println("Page title is: " + driver.getTitle());

			WebElement inputElement = driver.findElement(By.id("name"));
			inputElement.clear();
			inputElement.sendKeys("browsers");
			inputElement.submit();

			Thread.sleep(5000);

			WebElement textElement = driver.findElement(By.tagName("h1"));
			Assert.assertTrue(textElement.getText().contains("Hello"));
			driver.quit();
		}
	}
}
