package org.vxmlriot.parser;

import org.junit.Test;
import org.jvoicexml.xml.ssml.SsmlDocument;

import java.util.Optional;

import static org.junit.Assert.*;
import static org.vxmlriot.stubs.SsmlDocumentBuilder.ssmlDocument;

public class TextResponseParserTest {

    private TextResponseParser parser = new TextResponseParser();

    @Test
    public void parse_returnsSpeechText() throws Exception {
        SsmlDocument ssml = ssmlDocument().withFilename("ssmlTextResponse_helloWorld.xml").build();
        Optional<String> speech = parser.parse(ssml);
        assertTrue(speech.isPresent());
        assertEquals("Hello World!", speech.get());
    }

    @Test
    public void parseWhenNoSpeechTextInDocument_returnsEmpty() throws Exception {
        // SSML with audio only - no speech text
        SsmlDocument ssml = ssmlDocument().withFilename("ssmlAudioResponse_welcomeMessage.xml").build();
        Optional<String> speech = parser.parse(ssml);
        assertFalse(speech.isPresent());
    }

    @Test
    public void parseWhenSsmlInvalid_returnsEmpty() throws Exception {
        SsmlDocument ssml = ssmlDocument().withFilename("ssmlInvalidResponse.xml").build();
        Optional<String> speech = parser.parse(ssml);
        assertFalse(speech.isPresent());
    }
}