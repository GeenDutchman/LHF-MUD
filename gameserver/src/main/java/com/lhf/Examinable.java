package com.lhf;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.lhf.messages.events.SeeEvent;

public interface Examinable extends Taggable {
    String getName();

    String getDescription();

    @Override
    public default String getSimpleContent() {
        return this.getName();
    }

    public static final String XML_DESCRIPTION = "description";

    public static Element buildXMLElementFromExaminable(Document nodeGenerator, Examinable examinable) {
        if (examinable == null || nodeGenerator == null) {
            return null;
        }
        Element myElement = nodeGenerator.createElement(examinable.getTagName());
        myElement.setAttribute("colored", "true");
        final String name = examinable.getName();
        final String simpleContent = examinable.getSimpleContent();
        if (name != null && !name.equals(simpleContent)) {
            Element nameElement = nodeGenerator.createElement("name");
            nameElement.setAttribute("colored", "false");
            nameElement.appendChild(nodeGenerator.createTextNode(name));
            myElement.appendChild(nameElement);
        }
        if (simpleContent != null && !simpleContent.isEmpty() && !simpleContent.isBlank()) {
            myElement.appendChild(nodeGenerator.createTextNode(simpleContent));
        }
        if (myElement.getChildNodes().getLength() > 1) {
            myElement.setAttribute("complex", "true");
        }
        return myElement;
    }

    @Override
    public default Element buildXMLElement(Document nodeGenerator) {
        if (nodeGenerator == null) {
            return null;
        }
        return Examinable.buildXMLElementFromExaminable(nodeGenerator, this);
    }

    public static Element buildDetailedXMLElementFromExaminable(Document nodeGenerator, Examinable examinable) {
        if (nodeGenerator == null || examinable == null) {
            return null;
        }
        Element myElement = examinable.buildXMLElement(nodeGenerator);
        if (myElement == null) {
            return null;
        }
        final String description = examinable.getDescription();
        if (description == null || description.isEmpty() || description.isBlank()) {
            return myElement;
        }
        myElement.setAttribute("complex", "true");
        Element descriptionElement = nodeGenerator.createElement(XML_DESCRIPTION);
        descriptionElement.setAttribute("colored", "true");
        descriptionElement.appendChild(nodeGenerator.createTextNode(description.trim()));
        myElement.appendChild(descriptionElement);
        return myElement;
    }

    public default Element buildDetailedXMLElement(Document nodeGenerator) {
        if (nodeGenerator == null) {
            return null;
        }
        return Examinable.buildDetailedXMLElementFromExaminable(nodeGenerator, this);
    }

    default SeeEvent produceMessage() {
        return this.produceMessage(SeeEvent.getBuilder().setExaminable(this));
    }

    default SeeEvent produceMessage(SeeEvent.ABuilder<?> seeOutMessage) {
        if (seeOutMessage == null) {
            seeOutMessage = SeeEvent.getBuilder().setExaminable(this);
        }
        return seeOutMessage.Build();
    }
}
