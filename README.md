# VoiceXMLRiot
VoiceXML application test framework.

## Purpose
A simple Java based VoiceXML application test framework. VoiceXMLRiot can simulate a call, drive inputs and verify responses. Standard operations provided by the VoiceXMLRiot [driver](https://github.com/hotblac/voicexmlriot/blob/master/src/main/java/org/vxmlriot/driver/VxmlDriver.java) are:

* **get**: Request a VoiceXML resource
* **enterDtmf**: Simulate DTMF input
* **say**: Simulate Voice input
* **getTextResponse**: Get a list of text (TTS) responses on the current VoiceXML page
* **getAudioSrc**: Get a list of audio (wav file) responses on the current VoiceXML page

Using these operations, VoiceXML application call flows can be simulated and responses verified.

## Usage
See [JVoiceXmlDriverTest.java](https://github.com/hotblac/voicexmlriot/blob/master/src/test/java/org/vxmlriot/system/JVoiceXmlDriverTest.java) for an example of how to initialize the Driver, drive a simple VoiceXML call flow and make assertions on the responses. This test uses JUnit but VoiceXMLRiot is test framework agnostic.

## Design
VoiceXMLRiot's design is inspired by the [Selenium WebDriver](http://www.seleniumhq.org/projects/webdriver/). Selenium is not itself a test framework. Selenium provides simple commands for fetching HTML web pages and accessing the content of the page. When used with a test framework such as JUnit, it can drive interaction with a web application and make assertions on the correctness of responses.

VoiceXMLRiot is a headless browser for VoiceXML voice applications. VoiceXMLRiot is not in itself a test framework but does allow JUnit or similar to drive a VoiceXML application and make assertions on its correctness. 

VoiceXMLRiot is based on the [JVoiceXML](https://github.com/JVoiceXML/JVoiceXML) Open Source VoiceXML interpreter. VoiceXMLRiot uses JVoiceXML in embedded mode using their provided Text server. It is not necessary to run a standalone JVoiceXML Voice Browser to use VoiceXMLRiot - the interpreter / voice browser is run in-process.

## Comparison with JVoiceXML VoiceXMLUnit

VoiceXMLRiot is intended to be a simpler and more flexible alternative to the [VoiceXMLUnit](https://github.com/JVoiceXML/JVoiceXML/tree/master/org.jvoicexml.voicexmlunit) library included with JVoiceXML. See the [VoiceXMLUnit Demo](https://github.com/JVoiceXML/JVoiceXML/tree/master/org.jvoicexml.voicexmlunit.demo/src/test/java/org/jvoicexml/voicexmlunit/demo) for examples of how to use that.

VoiceXMLRiot differs from VoiceXMLUnit in the following ways:
* VoiceXMLRiot runs fully embedded within the test code. No need to run an external JVoiceXML server.
* VoiceXMLRiot parses the VoiceXML responses and provides simple access to the TTS text / audio response strings. No XML parsing required.
* VoiceXMLUnit provides simple assertions on TTS text / voice responses allowing exact match assertions only. VoiceXMLRiot provides direct access to the response strings allowing richer assertions, for example with [Hamcrest](https://github.com/hamcrest/JavaHamcrest) matchers.

