package com.lhf.server.client;

import java.io.OutputStream;
import java.util.StringJoiner;
import java.util.logging.Logger;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import com.lhf.messages.events.GameEvent;
import com.lhf.messages.events.GameEvent.XMLOutputBuilder;

public class XMLPrintWriterSendStrategy extends PrintWriterSendStrategy {

    public XMLPrintWriterSendStrategy(OutputStream outputStream) {
        super(outputStream);
    }

    @Override
    public void send(GameEvent toSend) {
        if (toSend != null) {
            XMLOutputBuilder xml = toSend.buildXML();
            try {
                xml.getXMLString(writer);
            } catch (ParserConfigurationException | TransformerException e) {
                Logger.getLogger(this.getClass().getName()).severe(() -> {
                    StringJoiner sj = new StringJoiner("\r\n");
                    sj.add("For the XML:").add(xml.getStringified());
                    sj.add("The following error occurred:").add(e.toString());
                    return sj.toString();
                });
                this.writer.println("XML ERROR!  XML ERROR!  Ask an Administrator to view the log!");
                this.writer.println(xml.getStringified());
                this.writer.println("XML ERROR!  XML ERROR!  Ask an Administrator to view the log!");
                this.writer.flush();
            }
        }
    }

}
