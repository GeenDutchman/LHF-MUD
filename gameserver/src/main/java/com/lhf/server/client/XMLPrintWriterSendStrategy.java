package com.lhf.server.client;

import java.io.OutputStream;
import java.util.StringJoiner;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import com.lhf.messages.events.GameEvent;

public class XMLPrintWriterSendStrategy extends PrintWriterSendStrategy {

    public XMLPrintWriterSendStrategy(OutputStream outputStream) {
        super(outputStream);
    }

    @Override
    public synchronized void send(GameEvent toSend) {
        if (toSend != null) {
            try {
                toSend.writeXML(writer);
                writer.println("");
            } catch (ParserConfigurationException | TransformerException e) {
                Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, e, () -> {
                    StringJoiner sj = new StringJoiner("\r\n");
                    sj.add("Error creating XML for the event:").add(toSend.toString());
                    return sj.toString();
                });
                this.writer.println("XML ERROR!  XML ERROR!  Ask an Administrator to view the log!");
                this.writer.println(toSend.printString());
                this.writer.println("XML ERROR!  XML ERROR!  Ask an Administrator to view the log!");
            } finally {
                this.writer.flush();
            }
        }
    }

}
