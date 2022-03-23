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

import java.awt.Point;
import java.util.LinkedList;
import java.util.List;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class FigPath extends Fig {
    private String description;
    private List<Point> points = new LinkedList<Point>();

    public FigPath(String name, String description, int x1, int y1, int x2,
            int y2, Fill fill) {
        super(name, fill, FillColor.WHITE, Stroke.ON, StrokeColor.BLACK);
        this.description = description;
        this.points.add(new Point(x1, y1));
        this.points.add(new Point(x2, y2));
    }

    public FigPath(String name, String description, List<Point> points,
            Fill fill) {
        super(name, fill, FillColor.WHITE, Stroke.ON, StrokeColor.BLACK);
        this.description = description;
        if (points.size() < 2) {
            String message = "Number of points must be at least 2: " + points;
            throw new IllegalArgumentException(message);
        }
        this.points.addAll(points);
    }

    public Element getXML(Document doc) {
        Element lineElement = createElement(doc, "path");
        lineElement.setAttribute("description", getDescription());

        Point point = (Point) points.get(0);

        Element moveTo = doc.createElement("moveto");
        moveTo.setAttribute("x", Integer.toString(point.x));
        moveTo.setAttribute("y", Integer.toString(point.y));
        lineElement.appendChild(moveTo);

        for (int i = 1; i < points.size(); i++) {
            point = (Point) points.get(i);
            Element lineTo = doc.createElement("lineto");
            lineTo.setAttribute("x", Integer.toString(point.x));
            lineTo.setAttribute("y", Integer.toString(point.y));
            lineElement.appendChild(lineTo);
        }

        return lineElement;
    }

    public String getDescription() {
        return description;
    }

    public List<Point> getPoints() {
        return points;
    }
}
