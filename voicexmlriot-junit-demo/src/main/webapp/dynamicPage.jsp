<?xml version="1.0" encoding="UTF-8" standalone="no"?>
<vxml xmlns="http://www.w3.org/2001/vxml"
      version="2.1" xml:lang="en" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
      xsi:schemaLocation="http://www.w3.org/2001/vxml http://www.w3.org/TR/voicexml20/vxml.xsd">
    <form>
        <block>
            <prompt><%=request.getParameter("param1")%></prompt>
            <audio src="<%=request.getParameter("param2")%>.wav" />
        </block>
    </form>
</vxml>
