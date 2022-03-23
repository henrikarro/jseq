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
import java.util.Stack;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class FigObject extends FigGroup {
    private static final String FIG_SEQ_OBJECT_CLASS =
            "org.argouml.uml.diagram.sequence.ui.FigSeqObject";

    private String objectName;
    private int x;
    private int y;
    private List<String> dynObjects = new LinkedList<String>();
    private List<String> dynObjectsActivation = new LinkedList<String>();
    private Stack<Integer> startPortNumbers = new Stack<Integer>();

    public FigObject(String name, String objectName, String uuid, int x, int y) {
        super(name, FIG_SEQ_OBJECT_CLASS, uuid, Fill.ON, FillColor.WHITE,
                Stroke.ON, StrokeColor.BLACK);
        this.objectName = objectName;
        this.x = x;
        this.y = y;

        addPrivateAttribute("enclosingFig", name);

        addFig(new FigRectangle(getNextFigName(), x, y, getNameBoxWidth(),
                getNameBoxHeight(), FillColor.CYAN));
        addFig(new FigRectangle(getNextFigName(), x, y, getNameBoxWidth(),
                getNameBoxHeight(), FillColor.WHITE));
        addFig(new FigText(getNextFigName(), objectName, x, y, "dialog", 9,
                Stroke.ON, StrokeColor.BLACK));
        addFig(new FigRectangle(getNextFigName(), x + getNameBoxWidth() / 2 -
                getLifeLineWidth() / 2, y + getNameBoxHeight(),
                getLifeLineWidth(), getLifeLineHeight(), FillColor.WHITE));
        addFig(new FigLine(getNextFigName(), 10, 49, 19, 49));
        addFig(new FigLine(getNextFigName(), 19, 49, 10, 49));
    }

    public Element getXML(Document doc) {
        Element group = super.getXML(doc);
        group.setAttribute("dynobjects", getDynObjects());
        return group;
    }

    public String getObjectName() {
        return objectName;
    }

    public String getDynObjects() {
        return dynObjects.toString();
    }

    public boolean isActive() {
        return !startPortNumbers.empty();
    }

    public void activate(int portNumber) {
        startPortNumbers.push(portNumber);
    }

    public void deactivate(int portNumber) {
        if (isActive()) {
            int startPortNumber = startPortNumbers.pop();
            if (!isActive()) {
                String activationDynObject =
                        "a|" + startPortNumber + "|" + portNumber +
                                "|false|false";
                dynObjectsActivation.add(0, activationDynObject);
                dynObjects.addAll(dynObjectsActivation);
                dynObjectsActivation.clear();
            }
        }
    }

    public String addPort(int portNumber) {
        dynObjectsActivation.add("b|" + portNumber);
        if (!isActive()) {
            Fig activation =
                    new FigRectangle(getNextFigName(), x + getNameBoxWidth() /
                            2 - getActivationWidth() / 2, y +
                            getNameBoxHeight(), getActivationWidth(),
                            getActivationHeight(), FillColor.WHITE);
            addFig(activation);
        }
        String portName = getNextFigName();
        Fig figPort2 =
                new FigRectangle(portName, x + getNameBoxWidth() / 2 -
                        getActivationWidth() / 2, y + getNameBoxHeight(),
                        getActivationWidth(), 0, FillColor.WHITE);
        addFig(figPort2);
        return portName;
    }

    private int getNameBoxWidth() {
        return 75;
    }

    private int getNameBoxHeight() {
        return 26;
    }

    private int getLifeLineWidth() {
        return 10;
    }

    private int getLifeLineHeight() {
        return 120;
    }

    private int getActivationWidth() {
        return 20;
    }

    private int getActivationHeight() {
        return 20;
    }
}
