package net.codestory;

import static net.codestory.AngularServer.*;

import net.codestory.http.*;
import net.codestory.http.misc.*;

import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.*;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;

import java.net.URL;
import java.util.concurrent.TimeUnit;

public class AngularTest {
	private static final String ENV_USERNAME = "BROWSERSTACK_USER";
	private static final String ENV_ACCESSKEY = "BROWSERSTACK_ACCESSKEY";
	private static final String ENV_BROWSERS = "BROWSERSTACK_BROWSERS";

	private static final String HUB_ENDPOINT = "https://%s:%s@hub.browserstack.com/wd/hub";

	private WebServer webServer;
	private WebDriver driver;

	@Before
	public void setUp() throws Exception {
		webServer = new WebServer() {
			@Override
			protected Env createEnv() {
				return Env.prod();
			}
		}.configure(new WebConfiguration()).start(3000);

		String browserstackUsername = System.getenv(ENV_USERNAME);
		String browserstackAccessKey = System.getenv(ENV_ACCESSKEY);

		if (browserstackUsername == null || browserstackUsername.trim().isEmpty() ||
				browserstackAccessKey == null || browserstackAccessKey.trim().isEmpty()) {
			throw new IllegalArgumentException("Invalid environment variable value(s) for " + ENV_USERNAME + " or " + ENV_ACCESSKEY);
		}

		String browserstackBrowsers = System.getenv(ENV_BROWSERS);
		if (browserstackBrowsers == null || browserstackBrowsers.trim().isEmpty()) {
			throw new IllegalArgumentException("Invalid environment variable value for " + ENV_BROWSERS);
		}

		JSONArray jsonBrowsers = new JSONArray(browserstackBrowsers);

		DesiredCapabilities caps = new DesiredCapabilities();
		caps.setCapability("build", "Sample Jenkins Project");
		caps.setCapability("browserstack.local", "true");
		caps.setCapability("browserstack.debug", "true");

		if (jsonBrowsers.length() > 0) {
			JSONObject browser = jsonBrowsers.getJSONObject(0);
			caps.setCapability("browser", browser.getString("browser"));
			caps.setCapability("browser_version", browser.getString("browser_version"));
			caps.setCapability("os", browser.getString("os"));
			caps.setCapability("os_version", browser.getString("os_version"));
		} else {
			caps.setCapability("browser", "Firefox");
			caps.setCapability("browser_version", "43.0");
			caps.setCapability("os", "Windows");
			caps.setCapability("os_version", "8.1");
		}

		String endpoint = String.format(HUB_ENDPOINT, browserstackUsername, browserstackAccessKey);
		driver = new RemoteWebDriver(new URL(endpoint), caps);
	}

	@After
	public void tearDown() throws Exception {
		webServer.stop();
		driver.quit();
	}

	@Test
	public void testSimple() throws Exception {
		driver.get("http://localhost:" + webServer.port());
		System.out.println("Page title is: " + driver.getTitle());

		WebElement inputElement = driver.findElement(By.id("name"));
		inputElement.clear();
		inputElement.sendKeys("BrowserStack");
		inputElement.submit();

		driver.manage().timeouts().implicitlyWait(3, TimeUnit.SECONDS);

		WebElement textElement = driver.findElement(By.tagName("h1"));
		Assert.assertEquals("Hello, BROWSERSTACK!", textElement.getText());
	}
}
