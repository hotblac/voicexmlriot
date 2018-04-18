package org.vxmlriot.jvoicexml.listener;

import org.junit.Test;
import org.jvoicexml.xml.ssml.SsmlDocument;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertFalse;
import static org.vxmlriot.stubs.SsmlDocumentBuilder.ssmlDocument;


public class ResponseListenerTest {

    private ResponseListener listener = new ResponseListener();

    @Test
    public void getCapturedResponses_returnsAllResponses() throws Exception {

        SsmlDocument response1 = ssmlDocument().withFilename("ssmlTextResponse_helloWorld.xml").build();
        SsmlDocument response2 = ssmlDocument().withFilename("ssmlTextResponse_goodbye.xml").build();

        listener.outputSsml(null, response1);
        listener.outputSsml(null, response2);
        List<SsmlDocument> responses = listener.getCapturedResponses();

        assertThat(responses, contains(response1, response2));
    }

    @Test
    public void getCapturedResponses_waitsForAllResponses() throws Exception {

        final SsmlDocument response1 = ssmlDocument().withFilename("ssmlTextResponse_helloWorld.xml").build();
        final SsmlDocument response2 = ssmlDocument().withFilename("ssmlTextResponse_goodbye.xml").build();
        final List<SsmlDocument> responses = new ArrayList<>();

        Thread mainThread = new Thread(() -> {
            // Expect this line to wait till all output is received
            List<SsmlDocument> receivedResponses = listener.getCapturedResponses();
            responses.addAll(receivedResponses);
        });

        Thread responseThread = new Thread(() -> {
            listener.outputSsml(null, response1);
            listener.outputSsml(null, response2);
        });

        mainThread.start();
        responseThread.start();
        mainThread.join(1000);

        // Expect the main thread to have completed after receiving responses
        assertFalse("Main thread hung without receiving responses", mainThread.isAlive());

        assertThat(responses, contains(response1, response2));
    }

    /**
     * We find that JVoiceXML can sometimes fire the outputSsml event multiple times
     * for the same response.
     * As a workaround, discard any messages that are identical to messages already captured.
     */
    @Test
    public void getCapturedResponses_discardsDuplicates() throws Exception {

        // Fire two instances of the same response document
        SsmlDocument response1 = ssmlDocument().withFilename("ssmlTextResponse_helloWorld.xml").build();
        SsmlDocument response2 = ssmlDocument().withFilename("ssmlTextResponse_helloWorld.xml").build();
        listener.outputSsml(null, response1);
        listener.outputSsml(null, response2);
        List<SsmlDocument> responses = listener.getCapturedResponses();
        assertThat(responses, hasSize(1));
    }

    @Test
    public void clear_resetsState() throws Exception {
        listener.outputSsml(null, ssmlDocument().withFilename("ssmlTextResponse_helloWorld.xml").build());
        assertThat(listener.getCapturedResponses(), not(empty()));

        listener.clear();
        assertThat(listener.getCapturedResponses(), empty());
    }
}