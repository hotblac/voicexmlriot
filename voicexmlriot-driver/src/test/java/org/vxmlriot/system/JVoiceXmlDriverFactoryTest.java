package org.vxmlriot.system;

import org.junit.After;
import org.junit.Test;
import org.vxmlriot.driver.VxmlDriver;
import org.vxmlriot.driver.VxmlDriverFactory;
import org.vxmlriot.jvoicexml.JVoiceXmlDriver;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.instanceOf;

public class JVoiceXmlDriverFactoryTest {

    private VxmlDriver driver = null;

    @After
    public void stopDriver() {
        driver.shutdown();
    }

    @Test
    public void factory_buildsJvoiceXmlDriver() {
        driver = VxmlDriverFactory.getDriver();
        assertThat(driver, instanceOf(JVoiceXmlDriver.class));
    }

    @Test
    public void restartDriver_works() throws Exception {

        driver = VxmlDriverFactory.getDriver();
        driver.shutdown();

        driver = VxmlDriverFactory.getDriver();
        driver.get("hello.vxml");
    }
}
