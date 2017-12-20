package org.vxmlriot.jvoicexml.listener;

import org.junit.Test;
import org.jvoicexml.xml.ssml.SsmlDocument;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.junit.Assert.assertFalse;


public class ResponseListenerTest {

    @Test
    public void getCapturedResponses_returnsAllResponses() throws Exception {

        SsmlDocument response1 = getSsmlDocument("ssmlTextResponse_helloWorld.xml");
        SsmlDocument response2 = getSsmlDocument("ssmlTextResponse_goodbye.xml");

        ResponseListener listener = new ResponseListener();
        listener.outputSsml(response1);
        listener.outputSsml(response2);
        List<SsmlDocument> responses = listener.getCapturedResponses();

        assertThat(responses, contains(response1, response2));
    }

    @Test
    public void getSsmlResponse_waitsForAllResponses() throws Exception {

        final ResponseListener listener = new ResponseListener();

        final SsmlDocument response1 = getSsmlDocument("ssmlTextResponse_helloWorld.xml");
        final SsmlDocument response2 = getSsmlDocument("ssmlTextResponse_goodbye.xml");
        final List<SsmlDocument> responses = new ArrayList<>();

        Thread mainThread = new Thread(() -> {
            // Expect this line to wait till all output is received
            List<SsmlDocument> receivedResponses = listener.getCapturedResponses();
            responses.addAll(receivedResponses);
        });

        Thread responseThread = new Thread(() -> {
            listener.outputSsml(response1);
            listener.outputSsml(response2);
        });

        mainThread.start();
        responseThread.start();
        mainThread.join(1000);

        // Expect the main thread to have completed after receiving responses
        assertFalse("Main thread hung without receiving responses", mainThread.isAlive());

        assertThat(responses, contains(response1, response2));
    }

    private SsmlDocument getSsmlDocument(String filename) throws ParserConfigurationException, SAXException, IOException {
        final InputStream vxmlInput = getClass().getClassLoader().getResourceAsStream(filename);
        final InputSource vxmlSource = new InputSource(vxmlInput);
        return new SsmlDocument(vxmlSource);
    }

}