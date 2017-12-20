package org.vxmlriot.driver;

import org.junit.After;
import org.junit.Test;
import org.vxmlriot.jvoicexml.JVoiceXmlDriver;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.instanceOf;
import static org.junit.Assert.assertNotNull;

public class VxmlDriverFactoryTest {

    private VxmlDriver driver;

    @After
    public void tearDown() throws Exception {
        if (driver != null) {
            driver.shutdown();
        }
    }

    @Test
    public void getDriver_returnsJvoiceXmlDriver() {
        driver = VxmlDriverFactory.getDriver();
        assertNotNull(driver);
        assertThat(driver, instanceOf(JVoiceXmlDriver.class));
    }
}