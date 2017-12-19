package org.vxmlriot.driver;

import org.vxmlriot.jvoicexml.CallBuilder;
import org.vxmlriot.url.ClasspathFileUriBuilder;

/**
 * Factory to create a VxmlDriver instance
 */
public class VxmlDriverFactory {

    public static VxmlDriver getDriver() {
        // For now, only the JVoiceXML implementation exists. Build it with default config.
        JvoiceXmlDriver driver = new JvoiceXmlDriver();
        driver.callBuilder = new CallBuilder();
        driver.uriBuilder = new ClasspathFileUriBuilder();
        return driver;
    }

}
