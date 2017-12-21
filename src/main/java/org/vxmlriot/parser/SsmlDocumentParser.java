package org.vxmlriot.parser;

import org.jvoicexml.xml.ssml.SsmlDocument;

import java.util.Optional;

/**
 * Parses SSML documents to extract relevant response information
 */
public interface SsmlDocumentParser {
    /**
     * Parse the given document for required String
     * @param document
     * @return String if found in the document, Empty otherwise.
     *          If the document cannot be parsed, we assument that
     *          it does not contain what we're trying to parse and
     *          return Empty rather than rethrowing exceptions.
     */
    Optional<String> parse(SsmlDocument document);
}
