package org.vxmlriot.jvoicexml.listener;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jvoicexml.client.text.TextListener;
import org.jvoicexml.client.text.TextMessageEvent;
import org.jvoicexml.xml.ssml.SsmlDocument;

import java.net.InetSocketAddress;

/**
 * Logs all events on TextListener
 */
public class LoggingTextListener implements TextListener{

    private static final Logger LOGGER = LogManager.getLogger(LoggingTextListener.class);
    private Level level = Level.DEBUG;

    public void setLevel(Level level) {
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
    public void outputSsml(TextMessageEvent textMessageEvent, SsmlDocument document) {
        LOGGER.log(level, "outputSsml: " + document);
    }

    @Override
    public void expectingInput(TextMessageEvent textMessageEvent) {
        LOGGER.log(level, "expectingInput");
    }

    @Override
    public void inputClosed(TextMessageEvent textMessageEvent) {
        LOGGER.log(level, "inputClosed");
    }

    @Override
    public void disconnected(TextMessageEvent textMessageEvent) {
        LOGGER.log(level, "disconnected");
    }
}
