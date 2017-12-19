package org.vxmlriot.it;

import org.junit.After;
import org.junit.Ignore;
import org.junit.Test;
import org.vxmlriot.driver.JvoiceXmlDriver;
import org.vxmlriot.driver.VxmlDriver;
import org.vxmlriot.driver.VxmlDriverFactory;

import java.util.List;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.instanceOf;
import static org.junit.Assert.assertThat;

/**
 * Integration test of the JvoiceXml driver implementation
 */
public class JvoiceXmlDriverTest {

    private static VxmlDriver driver = VxmlDriverFactory.getDriver();

    @After
    public void closeCall() throws Exception {
        driver.hangup();
    }

    @Test
    public void factory_buildsJvoiceXmlDriver() {
        assertThat(driver, instanceOf(JvoiceXmlDriver.class));
    }

    @Test
    public void getLocalFile_works() throws Exception {
        driver.get("hello.vxml");
    }

    @Test(expected = IllegalArgumentException.class)
    public void getMissingFile_throwsException() throws Exception {
        driver.get("missing.vxml");
    }

    @Test
    public void getMultipleRequests_works() throws Exception {
        driver.get("hello.vxml");
        driver.hangup();
        driver.get("hello.vxml");
        driver.hangup();
        driver.get("hello.vxml");
        driver.hangup();
    }

    @Ignore("TODO")
    @Test
    public void getTextResponse_returnsAllResponses() throws Exception {
        driver.get("hello.vxml");
        List<String> textResponse = driver.getTextResponse();
        assertThat(textResponse, contains(
                "Hello World!",
                "Goodbye!"
        ));
    }
}
