package org.vxmlriot.jvoicexml.listener;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.junit.Rule;
import org.junit.Test;
import org.jvoicexml.xml.ssml.SsmlDocument;
import org.vxmlriot.jvoicexml.junit.LogAppenderResource;

import java.net.InetSocketAddress;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.vxmlriot.stubs.SsmlDocumentBuilder.ssmlDocument;

public class LoggingTextListenerTest {

    /**
     * Attach a new appender to the logger for the class under test
     */
    @Rule public LogAppenderResource appender = new LogAppenderResource(Logger.getLogger(LoggingTextListener.class)); 
    private LoggingTextListener listener = new LoggingTextListener();

    @Test
    public void startedEvent_isLogged() {
        listener.started();
        assertThat(appender.getOutput(), containsString("started"));
    }

    @Test
    public void connectedEvent_isLogged() {
        final InetSocketAddress socketAddress = new InetSocketAddress("localhost", 8080);
        listener.connected(socketAddress);
        assertThat(appender.getOutput(), containsString("connected"));
        assertThat(appender.getOutput(), containsString(socketAddress.toString()));
    }

    @Test
    public void outputSsmlEvent_isLogged() throws Exception {
        final SsmlDocument ssml = ssmlDocument().withFilename("hello.vxml").build();
        listener.outputSsml(null, ssml);
        assertThat(appender.getOutput(), containsString("outputSsml"));
        assertThat(appender.getOutput(), containsString(ssml.toString()));
    }

    @Test
    public void expectingInputEvent_isLogged() {
        listener.expectingInput(null);
        assertThat(appender.getOutput(), containsString("expectingInput"));
    }

    @Test
    public void inputClosedEvent_isLogged() {
        listener.inputClosed(null);
        assertThat(appender.getOutput(), containsString("inputClosed"));
    }

    @Test
    public void disconnectedEvent_isLogged() {
        listener.disconnected(null);
        assertThat(appender.getOutput(), containsString("disconnected"));
    }

    @Test
    public void setLevel_changesLogLevel() {
        listener.setLevel(Level.INFO);
        listener.started();
        assertThat(appender.getOutput(), containsString("INFO"));
    }
}