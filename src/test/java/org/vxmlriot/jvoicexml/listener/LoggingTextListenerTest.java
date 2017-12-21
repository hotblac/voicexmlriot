package org.vxmlriot.jvoicexml.listener;

import org.apache.log4j.Level;
import org.junit.ClassRule;
import org.junit.Test;
import org.jvoicexml.xml.ssml.SsmlDocument;
import org.vxmlriot.jvoicexml.junit.SystemOutResource;

import java.net.InetSocketAddress;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.vxmlriot.stubs.SsmlDocumentBuilder.ssmlDocument;

public class LoggingTextListenerTest {

    @ClassRule public static SystemOutResource sysOut = new SystemOutResource();
    private LoggingTextListener listener = new LoggingTextListener();

    @Test
    public void startedEvent_isLogged() {
        listener.started();
        assertThat(sysOut.asString(), containsString("started"));
    }

    @Test
    public void connectedEvent_isLogged() {
        final InetSocketAddress socketAddress = new InetSocketAddress("example.com", 8080);
        listener.connected(socketAddress);
        assertThat(sysOut.asString(), containsString("connected"));
        assertThat(sysOut.asString(), containsString(socketAddress.toString()));
    }

    @Test
    public void outputSsmlEvent_isLogged() throws Exception {
        final SsmlDocument ssml = ssmlDocument().withFilename("hello.vxml").build();
        listener.outputSsml(ssml);
        assertThat(sysOut.asString(), containsString("outputSsml"));
        assertThat(sysOut.asString(), containsString(ssml.toString()));
    }

    @Test
    public void expectingInputEvent_isLogged() {
        listener.expectingInput();
        assertThat(sysOut.asString(), containsString("expectingInput"));
    }

    @Test
    public void inputClosedEvent_isLogged() {
        listener.inputClosed();
        assertThat(sysOut.asString(), containsString("inputClosed"));
    }

    @Test
    public void disconnectedEvent_isLogged() {
        listener.disconnected();
        assertThat(sysOut.asString(), containsString("disconnected"));
    }

    @Test
    public void setLevel_changesLogLevel() {
        listener.setLevel(Level.INFO);
        listener.started();
        assertThat(sysOut.asString(), containsString("INFO"));
    }
}