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

package th.co.edge.jseq.argouml.pgml;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Text;

import th.co.edge.jseq.util.XMLUtil;

public class FigText extends Fig {
    String text;
    int x;
    int y;
    String font;
    int textSize;

    public FigText(String name, String text, int x, int y, String font,
            int textSize, Stroke stroke, StrokeColor strokeColor) {
        super(name, Fill.OFF, FillColor.WHITE, stroke, strokeColor);
        this.text = text;
        this.x = x;
        this.y = y;
        this.font = font;
        this.textSize = textSize;
    }

    public Element getXML(Document doc) {
        Element textElement = createElement(doc, "text");
        textElement.setAttribute("x", Integer.toString(getX()));
        textElement.setAttribute("y", Integer.toString(getY()));
        textElement.setAttribute("font", getFont());
        textElement.setAttribute("textsize", Integer.toString(getTextSize()));
        Text textNode = doc.createTextNode(XMLUtil.makeXMLSafe(getText()));
        textElement.appendChild(textNode);
        return textElement;
    }

    public String getText() {
        return text;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public String getFont() {
        return font;
    }

    public int getTextSize() {
        return textSize;
    }
}
