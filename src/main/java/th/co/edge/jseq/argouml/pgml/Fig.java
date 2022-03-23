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

import th.co.edge.jseq.util.XMLUtil;

/**
 * A <code>Fig</code> represents a PGML figure of some kind.
 */
public abstract class Fig {
    private String name;
    private Fill fill;
    private FillColor fillColor;
    private Stroke stroke;
    private StrokeColor strokeColor;

    /**
     * Creates a new <code>Fig</code>.
     * 
     * @param name
     *            the name of the new <code>Fig</object>
     * @param fill determines if the figure should be filled or not
     * 
     * @param fillColor if the figure should be filled, determines the color to use
     * @param stroke determines if the figure should be stroked or not
     * @param strokeColor if the figure should be stroked, determines the color to use
     */
    public Fig(String name, Fill fill, FillColor fillColor, Stroke stroke,
            StrokeColor strokeColor) {
        this.name = name;
        this.fill = fill;
        this.fillColor = fillColor;
        this.stroke = stroke;
        this.strokeColor = strokeColor;
    }

    public abstract Element getXML(Document doc);

    protected Element createElement(Document doc, String tag) {
        Element element = doc.createElement(tag);
        element.setAttribute("name", XMLUtil.makeXMLSafe(getName()));
        element.setAttribute("fill", getFill().getStringValue());
        element.setAttribute("fillcolor", getFillColor().getStringValue());
        element.setAttribute("stroke", getStroke().getStringValue());
        element.setAttribute("strokecolor", getStrokeColor().getStringValue());
        return element;
    }

    //
    // Getters
    //

    /**
     * Returns the name of this figure object.
     * 
     * @return the name of this figure object
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the stroke attribute of this figure object, that is, whether or
     * not this figure should be filled.
     * 
     * @return the stroke attribute of this figure object
     */
    public Fill getFill() {
        return fill;
    }

    /**
     * Returns the fill color of this figure object, that is, if this figure
     * should be filled, the color to use.
     * 
     * @return the fill color of this figure object
     */
    public FillColor getFillColor() {
        return fillColor;
    }

    /**
     * Returns the stroke attribute of this figure object, that is, whether or not 
     * 
     * @return
     */
    public Stroke getStroke() {
        return stroke;
    }

    public StrokeColor getStrokeColor() {
        return strokeColor;
    }
}
