/*
 * Copyright (c) 2003-2008, by Henrik Arro and Contributors
 *
 * This file is part of JSeq, a tool to automatically create
 * sequence diagrams by tracing program execution.
 *
 * See <http://jseq.sourceforge.net> for more information.
 *
 * JSeq is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 3 of
 * the License, or (at your option) any later version.
 *
 * JSeq is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with JSeq. If not, see <http://www.gnu.org/licenses/>.
 */

package th.co.edge.jseq.svg;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentType;
import org.w3c.dom.Element;

import th.co.edge.jseq.Activation;
import th.co.edge.jseq.ActivationList;
import th.co.edge.jseq.Diagram;
import th.co.edge.jseq.MockObject;
import th.co.edge.jseq.MockObjectMap;
import th.co.edge.jseq.TextDiagram;
import th.co.edge.jseq.util.XMLUtil;

/**
 * A <code>SVGGenerator</code> is used to create a sequence diagram in <a
 * href="http://www.w3.org/Graphics/SVG/">SVG</a> format.
 */
public class SVGGenerator {
    private static final String SVG_1_1_PUBLIC_ID = "-//W3C//DTD SVG 1.1//EN";
    private static final String SVG_1_1_SYSTEM_ID =
            "http://www.w3.org/Graphics/SVG/1.1/DTD/svg11.dtd";
    private static final String SVG_NAMESPACE = "http://www.w3.org/2000/svg";

    private static final int COLUMN_WIDTH = 100;
    private static final int ROW_HEIGHT = 30;
    private static final int LIFE_LINE_LEFT_MARGIN = 150;
    private static final int LIFE_LINE_TOP_MARGIN = 75;
    private static final int LIFE_LINE_EXTRA_HEIGHT = 100;
    private static final int NUM_ROWS_BETWEEN_DIAGRAMS = 5;
    private static final int EXTRA_DIAGRAM_WIDTH = 200;
    private static final int EXTRA_DIAGRAM_HEIGHT = 50;
    private static final int HEADER_LEFT_MARGIN = 25;
    private static final int HEADER_TOP_MARGIN = 50;
    private static final int HEADER_VERTICAL_SHIFT = 20;
    private static final int INDENT_FIRST_ARROW = 55;
    private static final int ARROW_HEAD_WIDTH = 5;
    private static final int ARROW_HEAD_HEIGHT = 5;
    private static final int ARROW_VERTICAL_MARGIN = 100;
    private static final int SELF_ARROW_WIDTH = 45;
    private static final int SELF_ARROW_HEIGHT = 30;
    private static final int ACTIVATION_BOX_WIDTH = 10;
    private static final int ACTIVATION_BOX_TOP_MARGIN = 15;
    private static final int ACTIVATION_BOX_BOTTOM_MARGIN = 10;
    private static final int METHOD_NAME_BOTTOM_MARGIN = 5;

    private DocumentBuilder builder;

    private MockObjectMap objectMap;
    private int startRow;
    private int row;
    private int maxX;
    private int maxY;
    private Element groupHeaders;
    private Element groupCalls;
    private Element groupActivationBoxes;
    private Element groupLifeLines;

    /**
     * Creates a new <code>SVGGenerator</code>.
     *
     * @throws ParserConfigurationException
     *             if there is an error in the XML parser configuration (should
     *             normally not occur)
     */
    public SVGGenerator() throws ParserConfigurationException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        this.builder = factory.newDocumentBuilder();
    }

    /**
     * Returns a new SVG sequence diagram based on the given
     * <code>ActivationList</code>, representing the root activations.
     *
     * @param activationList
     *            the root activations to use to generate the diagram
     *
     * @return an SVG <code>TextDiagram</code> that can be written to file as
     *         XML
     */
    public Diagram generate(ActivationList activationList) {
        Document doc = createSVGDocument(activationList);
        return new TextDiagram(XMLUtil.toString(doc));
    }

    /**
     * Returns a new XML <code>Document</code> representing an
     * <code>ActivationList</code> as an SVG document.
     *
     * @param activationList
     *            the root activations to use to generate this diagram
     *
     * @return an XML <code>Document</code> representing
     *         <code>activationList</code> as an SVG document
     */
    public Document createSVGDocument(ActivationList activationList) {
        DOMImplementation impl = builder.getDOMImplementation();
        DocumentType docType =
                impl.createDocumentType("svg", SVG_1_1_PUBLIC_ID,
                        SVG_1_1_SYSTEM_ID);
        Document doc = impl.createDocument(SVG_NAMESPACE, "svg", docType);
        Element root = doc.getDocumentElement();
        root.setAttribute("xmlns", SVG_NAMESPACE);
        this.startRow = 0;
        for (Activation activation : activationList) {
            fillSVGDocument(doc, activation);
            this.startRow += row + NUM_ROWS_BETWEEN_DIAGRAMS;
        }
        root.setAttributeNS(null, "width", Integer.toString(maxX +
                EXTRA_DIAGRAM_WIDTH));
        root.setAttributeNS(null, "height", Integer.toString(maxY +
                EXTRA_DIAGRAM_HEIGHT));
        return doc;
    }

    private void fillSVGDocument(Document doc, Activation activation) {
        this.objectMap = MockObjectMap.addAll(activation);
        this.row = 0;
        createGroups(doc);
        addHeaders(doc);
        addCalls(doc, activation);
        addLifelines(doc);
    }

    private void createGroups(Document doc) {
        groupHeaders = doc.createElementNS(SVG_NAMESPACE, "g");
        groupHeaders.setAttributeNS(null, "id", "Headers");
        groupCalls = doc.createElementNS(SVG_NAMESPACE, "g");
        groupCalls.setAttributeNS(null, "id", "Calls");
        groupLifeLines = doc.createElementNS(SVG_NAMESPACE, "g");
        groupLifeLines.setAttributeNS(null, "id", "Lifelines");
        groupActivationBoxes = doc.createElementNS(SVG_NAMESPACE, "g");
        groupActivationBoxes.setAttributeNS(null, "id", "ActivationBoxes");
        Element root = doc.getDocumentElement();
        root.appendChild(groupHeaders);
        root.appendChild(groupLifeLines);
        root.appendChild(groupActivationBoxes);
        root.appendChild(groupCalls);
    }

    private void addHeaders(Document doc) {
        int x = HEADER_LEFT_MARGIN;
        int y = HEADER_TOP_MARGIN + startRow * ROW_HEIGHT;
        int column = 0;
        for (MockObject object : objectMap.listView()) {
            String name = object.getName();
            if (name.lastIndexOf(".") >= 0) {
                name = name.substring(name.lastIndexOf(".") + 1);
            }
            x += COLUMN_WIDTH;
            Element text = doc.createElementNS(SVG_NAMESPACE, "text");
            text.setAttributeNS(null, "x", Integer.toString(x));
            text.setAttributeNS(null, "y", Integer.toString(getHeaderY(y,
                    column++)));
            text.appendChild(doc.createTextNode(XMLUtil.makeXMLSafe(name)));
            groupHeaders.appendChild(text);
        }
        if (x > maxX) {
            maxX = x;
        }
    }

    private int getHeaderY(int y, int column) {
        int headerY;
        if (column % 2 == 0) {
            headerY = y;
        } else {
            headerY = y - HEADER_VERTICAL_SHIFT;
        }
        return headerY;
    }

    private void addCalls(Document doc, Activation activation) {
        int firstRow = row;
        addCall(doc, activation);
        for (Activation nestedActivation : activation.getNestedActivations()) {
            addCalls(doc, nestedActivation);
        }
        int lastRow = row;
        addActivationBox(doc, activation, firstRow, lastRow);
    }

    private void addCall(Document doc, Activation activation) {
        MockObject sender = null;
        if (activation.getParent() != null) {
            sender = objectMap.get(activation.getParent().getClassName());
        }
        MockObject receiver = objectMap.get(activation.getClassName());
        String methodName = activation.getMethod().name();
        if (activation.getNumRepetitions() > 1) {
            methodName =
                    "*[" + activation.getNumRepetitions() + "] " + methodName;
        }
        if (sender == receiver) {
            addSelfArrow(doc, sender, methodName);
        } else {
            addArrow(doc, sender, receiver, methodName);
        }
    }

    private void addSelfArrow(Document doc, MockObject sender, String methodName) {
        int x1 =
                LIFE_LINE_LEFT_MARGIN + ACTIVATION_BOX_WIDTH / 2 +
                        sender.getColumn() * COLUMN_WIDTH;
        int y1 = ARROW_VERTICAL_MARGIN + (row + startRow) * ROW_HEIGHT;
        int x2 = x1 + SELF_ARROW_WIDTH;
        int y2 = y1;
        int x3 = x2;
        int y3 = y2 + SELF_ARROW_HEIGHT;
        int x4 = x1;
        int y4 = y3;
        String points =
                x1 + "," + y1 + "," + x2 + "," + y2 + "," + x3 + "," + y3 +
                        "," + x4 + "," + y4;
        Element line = doc.createElementNS(SVG_NAMESPACE, "polyline");
        line.setAttributeNS(null, "fill", "none");
        line.setAttributeNS(null, "stroke", "black");
        line.setAttributeNS(null, "points", points);
        groupCalls.appendChild(line);

        addMethodName(doc, methodName, x1, y1, x2, y2);
        addArrowHead(doc, x3, y3, x4, y4);

        row += 2;
    }

    private void addArrow(Document doc, MockObject sender, MockObject receiver,
            String methodName) {
        int x1 =
                (sender == null ? INDENT_FIRST_ARROW : LIFE_LINE_LEFT_MARGIN +
                        ACTIVATION_BOX_WIDTH / 2 + sender.getColumn() *
                        COLUMN_WIDTH);
        int y1 = ARROW_VERTICAL_MARGIN + (row + startRow) * ROW_HEIGHT;
        int x2 =
                LIFE_LINE_LEFT_MARGIN - ACTIVATION_BOX_WIDTH / 2 +
                        receiver.getColumn() * COLUMN_WIDTH;
        int y2 = y1;
        if (x1 > x2) {
            x1 -= ACTIVATION_BOX_WIDTH;
            x2 += ACTIVATION_BOX_WIDTH;
        }
        Element line = doc.createElementNS(SVG_NAMESPACE, "line");
        line.setAttributeNS(null, "stroke", "black");
        line.setAttributeNS(null, "x1", Integer.toString(x1));
        line.setAttributeNS(null, "y1", Integer.toString(y1));
        line.setAttributeNS(null, "x2", Integer.toString(x2));
        line.setAttributeNS(null, "y2", Integer.toString(y2));
        groupCalls.appendChild(line);

        addMethodName(doc, methodName, x1, y1, x2, y2);
        addArrowHead(doc, x1, y1, x2, y2);

        row++;
    }

    private void addMethodName(Document doc, String methodName, int x1, int y1,
            int x2, int y2) {
        Element text = doc.createElementNS(SVG_NAMESPACE, "text");
        int x;
        if (x1 < x2) {
            x = x1 + ACTIVATION_BOX_WIDTH;
        } else {
            x = x1 - COLUMN_WIDTH + ACTIVATION_BOX_WIDTH * 2;
        }
        int y = y1 - METHOD_NAME_BOTTOM_MARGIN;
        text.setAttributeNS(null, "x", Integer.toString(x));
        text.setAttributeNS(null, "y", Integer.toString(y));
        text.appendChild(doc.createTextNode(XMLUtil.makeXMLSafe(methodName)));
        groupCalls.appendChild(text);
    }

    private void addArrowHead(Document doc, int x1, int y1, int x2, int y2) {
        Element line = doc.createElementNS(SVG_NAMESPACE, "polyline");
        line.setAttributeNS(null, "fill", "none");
        line.setAttributeNS(null, "stroke", "black");
        String points;
        if (x1 < x2) {
            points =
                    (x2 - ARROW_HEAD_WIDTH) + "," + (y2 - ARROW_HEAD_HEIGHT) +
                            "," + x2 + "," + y2 + "," +
                            (x2 - ARROW_HEAD_WIDTH) + "," +
                            (y2 + ARROW_HEAD_HEIGHT);
        } else {
            points =
                    (x2 + ARROW_HEAD_WIDTH) + "," + (y2 - ARROW_HEAD_HEIGHT) +
                            "," + x2 + "," + y2 + "," +
                            (x2 + ARROW_HEAD_WIDTH) + "," +
                            (y2 + ARROW_HEAD_HEIGHT);
        }
        line.setAttributeNS(null, "points", points);
        groupCalls.appendChild(line);
    }

    private void addActivationBox(Document doc, Activation activation,
            int firstRow, int lastRow) {
        MockObject sender = null;
        if (activation.getParent() != null) {
            sender = objectMap.get(activation.getParent().getClassName());
        }
        MockObject receiver = objectMap.get(activation.getClassName());
        if (sender != receiver) {
            int x =
                    LIFE_LINE_LEFT_MARGIN - ACTIVATION_BOX_WIDTH / 2 +
                            receiver.getColumn() * COLUMN_WIDTH;
            int y =
                    LIFE_LINE_TOP_MARGIN + ACTIVATION_BOX_TOP_MARGIN +
                            (startRow + firstRow) * ROW_HEIGHT;
            int width = ACTIVATION_BOX_WIDTH;
            int height =
                    (lastRow - firstRow) * ROW_HEIGHT -
                            ACTIVATION_BOX_BOTTOM_MARGIN;
            Element rect = doc.createElementNS(SVG_NAMESPACE, "rect");
            rect.setAttributeNS(null, "fill", "white");
            rect.setAttributeNS(null, "stroke", "gray");
            rect.setAttributeNS(null, "x", Integer.toString(x));
            rect.setAttributeNS(null, "y", Integer.toString(y));
            rect.setAttributeNS(null, "width", Integer.toString(width));
            rect.setAttributeNS(null, "height", Integer.toString(height));
            groupActivationBoxes.appendChild(rect);
        }
    }

    private void addLifelines(Document doc) {
        for (int col = 0; col < objectMap.listView().size(); col++) {
            int x1 = LIFE_LINE_LEFT_MARGIN + col * COLUMN_WIDTH;
            int y1 = LIFE_LINE_TOP_MARGIN + startRow * ROW_HEIGHT;
            int x2 = x1;
            int y2 = LIFE_LINE_EXTRA_HEIGHT + (row + startRow) * ROW_HEIGHT;
            Element line = doc.createElementNS(SVG_NAMESPACE, "line");
            line.setAttributeNS(null, "stroke", "gray");
            line.setAttributeNS(null, "stroke-dasharray", "10,5");
            line.setAttributeNS(null, "x1", Integer.toString(x1));
            line.setAttributeNS(null, "y1", Integer.toString(y1));
            line.setAttributeNS(null, "x2", Integer.toString(x2));
            line.setAttributeNS(null, "y2", Integer.toString(y2));
            groupLifeLines.appendChild(line);
            maxY = y2;
        }
    }
}
