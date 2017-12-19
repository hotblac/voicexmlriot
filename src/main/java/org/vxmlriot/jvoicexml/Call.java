package org.vxmlriot.jvoicexml;

import org.jvoicexml.JVoiceXmlMain;
import org.jvoicexml.Session;
import org.jvoicexml.client.text.TextServer;

import java.net.URI;

/**
 * A call session
 */
public class Call {

    /**
     * JVoiceXml interpreter
     */
    private JVoiceXmlMain jvxml;

    /**
     * The active JVoiceXml session.
     * */
    protected Session session;

    /**
     * Socket server to stream text from and to the JVoiceXML interpreter
     */
    protected TextServer textServer;


    public Call(Session session, TextServer textServer) {
        this.session = session;
        this.textServer = textServer;
    }

    /**
     * Call the VXML application at given URI
     * @param uri of the VXML application
     */
    public void call(URI uri) {

    }
}
