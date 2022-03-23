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

import java.util.LinkedList;
import java.util.List;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Text;

class FigGroup extends Fig {
    private String description;
    private String href;

    private List<Fig> figs = new LinkedList<Fig>();
    private List<String> privateAttributes = new LinkedList<String>();
    private int nextFigNumber = 0;

    public FigGroup(String name, String description, String href, Fill fill,
            FillColor fillColor, Stroke stroke, StrokeColor strokeColor) {
        super(name, fill, fillColor, stroke, strokeColor);
        this.description = description;
        this.href = href;
    }

    public Element getXML(Document doc) {
        Element group = createElement(doc, "group");
        group.setAttribute("description", getDescription());
        group.setAttribute("href", getHREF());

        group.appendChild(getPrivateAttributesElement(doc));

        for (Fig fig : figs) {
            group.appendChild(fig.getXML(doc));
        }
        return group;
    }

    private Element getPrivateAttributesElement(Document doc) {
        Element priv = doc.createElement("private");
        for (String privateAttribute : privateAttributes) {
            Text textNode = doc.createTextNode(privateAttribute);
            priv.appendChild(textNode);
        }
        return priv;
    }

    public void addPrivateAttribute(String attribute, String value) {
        privateAttributes.add(attribute + "=\"" + value + "\"");
    }

    public void addFig(Fig fig) {
        figs.add(fig);
    }

    //
    // Getters
    //

    public String getDescription() {
        return description;
    }

    public String getHREF() {
        return href;
    }

    protected String getNextFigName() {
        return getName() + "." + nextFigNumber++;
    }

}
