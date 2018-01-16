package org.vxmlriot.jvoicexml.listener;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.Priority;
import org.jvoicexml.client.text.TextListener;
import org.jvoicexml.xml.ssml.SsmlDocument;

import java.net.InetSocketAddress;

/**
 * Logs all events on TextListener
 */
public class LoggingTextListener implements TextListener{

    private static final Logger LOGGER = Logger.getLogger(LoggingTextListener.class);
    private Priority level = Level.DEBUG;

    public void setLevel(Priority level) {
        this.level = level;
    }

    @Override
    public void started() {
        LOGGER.log(level, "started");
    }

    @Override
    public void connected(InetSocketAddress remote) {
        LOGGER.log(level, "connected: " + remote);
    }

    @Override
    public void outputSsml(SsmlDocument document) {
        LOGGER.log(level, "outputSsml: " + document);
    }

    @Override
    public void expectingInput() {
        LOGGER.log(level, "expectingInput");
    }

    @Override
    public void inputClosed() {
        LOGGER.log(level, "inputClosed");
    }

    @Override
    public void disconnected() {
        LOGGER.log(level, "disconnected");
    }
}
