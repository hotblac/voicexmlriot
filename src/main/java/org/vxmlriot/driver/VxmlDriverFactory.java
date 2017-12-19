package org.vxmlriot.driver;

import org.vxmlriot.jvoicexml.JVoiceXmlDriverBuilder;


/**
 * Factory to create a VxmlDriver instance
 */
public class VxmlDriverFactory {

    public static VxmlDriver getDriver() {
        // For now, only the JVoiceXML implementation exists. Build it with default config.
        return new JVoiceXmlDriverBuilder().build();
    }

}
