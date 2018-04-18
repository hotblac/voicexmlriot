package org.vxmlriot.jvoicexml.listener;

import org.jvoicexml.client.text.TextListener;
import org.jvoicexml.client.text.TextMessageEvent;
import org.jvoicexml.xml.ssml.SsmlDocument;

import java.net.InetSocketAddress;

/**
 * Adapter for teh TextListener interface.
 * This provides default implementations for all methods allowing implementor
 * to implement only required methods.
 */
public class TextListenerAdapter implements TextListener {
    @Override
    public void started() {

    }

    @Override
    public void connected(InetSocketAddress remote) {

    }

    @Override
    public void outputSsml(TextMessageEvent textMessageEvent, SsmlDocument ssmlDocument) {

    }

    @Override
    public void expectingInput(TextMessageEvent textMessageEvent) {

    }

    @Override
    public void inputClosed(TextMessageEvent textMessageEvent) {

    }

    @Override
    public void disconnected(TextMessageEvent textMessageEvent) {

    }
}
