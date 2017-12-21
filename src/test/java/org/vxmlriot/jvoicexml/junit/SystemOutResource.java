package org.vxmlriot.jvoicexml.junit;

import org.junit.rules.ExternalResource;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

/**
 * JUnit rule for testing output to System.out.
 * Handy for verifying logging.
 */
public class SystemOutResource extends ExternalResource {

    private PrintStream sysOut;
    private final ByteArrayOutputStream outContent = new ByteArrayOutputStream();

    @Override
    protected void before() {
        sysOut = System.out;
        System.setOut(new PrintStream(outContent));
    }

    @Override
    protected void after() {
        System.setOut(sysOut);
        // All output to System.out has redirected to local OutputStream instead of console.
        // On completion of the test, spew it all back to the real System.out so we can see it.
        System.out.print(outContent.toString());
    }

    public String asString() {
        return outContent.toString();
    }
}