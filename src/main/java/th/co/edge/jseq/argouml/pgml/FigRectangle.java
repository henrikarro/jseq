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

public class FigRectangle extends Fig {
    int x;
    int y;
    int width;
    int height;

    public FigRectangle(String name, int x, int y, int width, int height,
            FillColor fillColor) {
        super(name, Fill.ON, fillColor, Stroke.ON, StrokeColor.BLACK);
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    public Element getXML(Document doc) {
        Element rectangle = createElement(doc, "rectangle");
        rectangle.setAttribute("x", Integer.toString(getX()));
        rectangle.setAttribute("y", Integer.toString(getY()));
        rectangle.setAttribute("width", Integer.toString(getWidth()));
        rectangle.setAttribute("height", Integer.toString(getHeight()));
        return rectangle;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }
}
