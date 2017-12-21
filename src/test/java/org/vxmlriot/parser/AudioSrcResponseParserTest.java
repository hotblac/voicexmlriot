package org.vxmlriot.parser;

import org.junit.Test;
import org.jvoicexml.xml.ssml.SsmlDocument;

import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.endsWith;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.vxmlriot.stubs.SsmlDocumentBuilder.ssmlDocument;

public class AudioSrcResponseParserTest {

    private AudioSrcResponseParser parser = new AudioSrcResponseParser();

    @Test
    public void parse_returnsAudioSrc() throws Exception {
        SsmlDocument ssml = ssmlDocument().withFilename("ssmlAudioResponse_welcomeMessage.xml").build();
        Optional<String> speech = parser.parse(ssml);
        assertTrue(speech.isPresent());
        assertThat(speech.get(), endsWith("welcomeMessage.wav"));
    }

    @Test
    public void parseWhenNoAudioInDocument_returnsEmpty() throws Exception {
        // SSML with speech text only - no audio
        SsmlDocument ssml = ssmlDocument().withFilename("ssmlTextResponse_helloWorld.xml").build();
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