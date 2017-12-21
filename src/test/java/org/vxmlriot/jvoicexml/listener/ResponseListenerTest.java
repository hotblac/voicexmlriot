package org.vxmlriot.jvoicexml.listener;

import org.junit.Test;
import org.jvoicexml.xml.ssml.SsmlDocument;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertFalse;
import static org.vxmlriot.stubs.SsmlDocumentBuilder.ssmlDocument;


public class ResponseListenerTest {

    @Test
    public void getCapturedResponses_returnsAllResponses() throws Exception {

        SsmlDocument response1 = ssmlDocument().withFilename("ssmlTextResponse_helloWorld.xml").build();
        SsmlDocument response2 = ssmlDocument().withFilename("ssmlTextResponse_goodbye.xml").build();

        ResponseListener listener = new ResponseListener();
        listener.outputSsml(response1);
        listener.outputSsml(response2);
        List<SsmlDocument> responses = listener.getCapturedResponses();

        assertThat(responses, contains(response1, response2));
    }

    @Test
    public void getSsmlResponse_waitsForAllResponses() throws Exception {

        final ResponseListener listener = new ResponseListener();

        final SsmlDocument response1 = ssmlDocument().withFilename("ssmlTextResponse_helloWorld.xml").build();
        final SsmlDocument response2 = ssmlDocument().withFilename("ssmlTextResponse_goodbye.xml").build();
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

    @Test
    public void clear_resetsState() throws Exception {
        ResponseListener listener = new ResponseListener();
        listener.outputSsml(ssmlDocument().withFilename("ssmlTextResponse_helloWorld.xml").build());
        assertThat(listener.getCapturedResponses(), not(empty()));

        listener.clear();
        assertThat(listener.getCapturedResponses(), empty());
    }
}