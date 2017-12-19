package org.vxmlriot.driver;

import org.junit.Test;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertNotNull;

public class VxmlDriverFactoryTest {

    @Test
    public void getDriver_createsJvoiceXmlDriver() {
        VxmlDriver driver = VxmlDriverFactory.getDriver();
        assertNotNull(driver);
        assertThat(driver, instanceOf(JvoiceXmlDriver.class));
    }

    @Test
    public void getJvoiceXmlDriver_hasRequiredDependencies() {
        VxmlDriver driver = VxmlDriverFactory.getDriver();
        JvoiceXmlDriver jvxmlDriver = (JvoiceXmlDriver)driver;
        assertNotNull(jvxmlDriver.callBuilder);
        assertNotNull(jvxmlDriver.uriBuilder);
    }
}