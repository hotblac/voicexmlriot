package org.vxmlriot.it;

import org.junit.Test;
import org.vxmlriot.driver.JvoiceXmlDriver;
import org.vxmlriot.driver.VxmlDriver;
import org.vxmlriot.driver.VxmlDriverFactory;

import static org.hamcrest.core.IsInstanceOf.instanceOf;
import static org.junit.Assert.assertThat;

/**
 * Integration test of the JvoiceXml driver implementation
 */
public class JvoiceXmlDriverTest {

    private VxmlDriver driver = VxmlDriverFactory.getDriver();

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
}
