package org.vxmlriot.driver;

import org.vxmlriot.jvoicexml.JVoiceXmlDriverBuilder;


/**
 * Factory to create a VxmlDriver instance
 */
public class VxmlDriverFactory {

    private static final VxmlDriverBuilder DEFAULT_DRIVER_BUILDER = new JVoiceXmlDriverBuilder();

    public static VxmlDriverBuilder driverBuilder = DEFAULT_DRIVER_BUILDER;

    public static VxmlDriver getDriver() {
        return driverBuilder.build();
    }

}
