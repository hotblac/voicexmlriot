package org.vxmlriot.driver;

import org.junit.Test;
import org.vxmlriot.jvoicexml.JVoiceXmlDriver;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.instanceOf;
import static org.junit.Assert.assertNotNull;

public class VxmlDriverFactoryTest {

    @Test
    public void getDriver_returnsJvoiceXmlDriver() {
        VxmlDriver driver = VxmlDriverFactory.getDriver();
        assertNotNull(driver);
        assertThat(driver, instanceOf(JVoiceXmlDriver.class));
    }
}