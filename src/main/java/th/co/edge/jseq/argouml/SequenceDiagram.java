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

package th.co.edge.jseq.argouml;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentType;
import org.w3c.dom.Element;

import ru.novosoft.uml.behavior.common_behavior.MAction;
import ru.novosoft.uml.behavior.common_behavior.MLink;
import ru.novosoft.uml.behavior.common_behavior.MObject;
import ru.novosoft.uml.behavior.common_behavior.MStimulus;
import ru.novosoft.uml.foundation.core.MClass;
import ru.novosoft.uml.foundation.core.MClassifier;
import ru.novosoft.uml.foundation.core.MNamespace;

import th.co.edge.jseq.Activation;
import th.co.edge.jseq.MockObject;
import th.co.edge.jseq.MockObjectMap;
import th.co.edge.jseq.argouml.pgml.Fig;
import th.co.edge.jseq.argouml.pgml.FigLink;
import th.co.edge.jseq.argouml.pgml.FigObject;
import th.co.edge.jseq.argouml.pgml.FigStimulus;

/**
 * A <code>SequenceDiagram</code> represents a sequence diagram as a <a
 * href="http://en.wikipedia.org/wiki/PGML" target="new">PGML</a> document.
 */
public class SequenceDiagram {
    private static final String PGML_PUBLIC_ID = null;
    private static final String PGML_SYSTEM_ID = "pgml.dtd";
    private static final String PGML_NAMESPACE = null;

    private static final String UML_SEQUENCE_DIAGRAM_CLASS =
            "org.argouml.uml.diagram.sequence.ui.UMLSequenceDiagram";

    private static final int TOP_MARGIN = 10;
    private static final int LEFT_MARGIN = 10;
    private static final int COLUMN_WIDTH = 135;

    private MNamespace namespace;
    private MockObjectMap mockObjectMap;
    private DocumentBuilder builder;

    private Map<MObject, FigObject> figObjectMap =
            new HashMap<MObject, FigObject>();
    private List<Fig> figObjects = new LinkedList<Fig>();
    private List<Fig> figLinks = new LinkedList<Fig>();
    private List<Fig> figStimuli = new LinkedList<Fig>();
    private int nextFigNumber = 0;
    private int nextPortNumber = 0;

    /**
     * Creates a new <code>SequenceDiagram</code> depicting a given root
     * activation.
     *
     * @param namespace
     *            only used to generate the description element in the PGML file
     * @param activation
     *            the root activation to depict as a sequence diagram
     *
     * @throws ParserConfigurationException
     *             if there is some serious error in the XML configuration
     *             (should normally not occur)
     */
    public SequenceDiagram(MNamespace namespace, Activation activation)
            throws ParserConfigurationException {
        this.namespace = namespace;
        this.mockObjectMap = MockObjectMap.addAll(activation);

        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        this.builder = factory.newDocumentBuilder();
    }

    /**
     * Returns this sequence diagram as a PGML XML <code>Document</code>.
     *
     * @return this sequence diagram as a PGML XML <code>Document</code>
     */
    public Document getDocument() {
        DOMImplementation impl = builder.getDOMImplementation();
        DocumentType docType =
                impl.createDocumentType("pgml", PGML_PUBLIC_ID, PGML_SYSTEM_ID);
        Document doc = impl.createDocument(PGML_NAMESPACE, "pgml", docType);
        Element root = doc.getDocumentElement();
        root.setAttribute("description", getRootDescription());
        addFigs(doc, figObjects);
        addFigs(doc, figStimuli);
        addFigs(doc, figLinks);
        return doc;
    }

    private void addFigs(Document doc, List<Fig> figs) {
        for (Fig fig : figs) {
            doc.getDocumentElement().appendChild(fig.getXML(doc));
        }
    }

    private String getRootDescription() {
        return UML_SEQUENCE_DIAGRAM_CLASS + "|" + namespace.getUUID();
    }

    /**
     * Adds a life-line to this diagram, represented by an <code>MObject</code>.
     *
     * @param object
     *            the <code>MObject</code> to add
     */
    public void addObject(MObject object) {
        FigObject figObject = createEmptyFigObject(object);
        figObjects.add(figObject);
        figObjectMap.put(object, figObject);
    }

    /**
     * Adds an arrow representing a method call to this diagram, from a given
     * sender to a given receiver represented by activation boxes in the
     * resulting diagram.
     *
     * @param sender
     *            the <code>MObject</code> representing the caller
     *
     * @param receiver
     *            the <code>MObject</code> representing the callee
     *
     * @param link
     */
    @SuppressWarnings("unchecked")
    public void addCall(MObject sender, MObject receiver, MLink link) {
        FigObject figSender = figObjectMap.get(sender);
        FigObject figReceiver = figObjectMap.get(receiver);

        String sourcePortFig = figSender.addPort(nextPortNumber);
        if (!figSender.isActive()) {
            activate(sender);
        }
        String destPortFig = figReceiver.addPort(nextPortNumber);
        activate(receiver);
        nextPortNumber++;
        String sourceFigNode = figSender.getName();
        String destFigNode = figReceiver.getName();

        for (Iterator i = link.getStimuli().iterator(); i.hasNext();) {
            MStimulus stimulus = (MStimulus) i.next();
            MAction action = stimulus.getDispatchAction();
            FigStimulus figStimulus =
                    createFigStimulus(stimulus, action.getName());
            figStimuli.add(figStimulus);
        }
        FigLink figLink =
                createFigLink(link, sourcePortFig, destPortFig, sourceFigNode,
                        destFigNode);
        figLinks.add(figLink);
    }

    /**
     * Makes the given <code>MObject</code>, represented as a life-line in
     * the sequence diagram, active, that is, executing a method. This will be
     * represented as an activation box for the given <code>MObject</code>.
     *
     * @param object
     *            the <code>MObject</code> to activate
     */
    private void activate(MObject object) {
        FigObject figObject = figObjectMap.get(object);
        figObject.activate(nextPortNumber);
    }

    /**
     * Deactivates the given <code>MObject</code>, that is, returns from a
     * method call. The activation box currently associated with this object
     * will be ended.
     *
     * @param object
     *            the <code>MObject</code> to deactivate
     */
    public void deactivate(MObject object) {
        FigObject figObject = figObjectMap.get(object);
        figObject.deactivate(nextPortNumber - 1);
    }

    private FigObject createEmptyFigObject(MObject object) {
        int column = getColumn(object);
        FigObject fig =
                new FigObject(getNextFigName(), getName(object), object
                        .getUUID(), LEFT_MARGIN, TOP_MARGIN + column *
                        COLUMN_WIDTH);
        return fig;
    }

    private FigStimulus createFigStimulus(MStimulus stimulus, String name) {
        FigStimulus fig =
                new FigStimulus(getNextFigName(), name, stimulus.getUUID());
        return fig;
    }

    private FigLink createFigLink(MLink link, String sourcePortFig,
            String destPortFig, String sourceFigNode, String destFigNode) {
        FigLink fig =
                new FigLink(getNextFigName(), link.getUUID(), LEFT_MARGIN,
                        TOP_MARGIN, sourcePortFig, destPortFig, sourceFigNode,
                        destFigNode);
        return fig;
    }

    private int getColumn(MObject object) {
        MClass cls = getClass(object);
        MockObject mockObject = mockObjectMap.get(cls.getName());
        if (mockObject == null) {
            throw new IllegalArgumentException("Object not found " + object);
        }
        return mockObject.getColumn();
    }

    private String getName(MObject object) {
        MClass cls = getClass(object);
        return object.getName() + " : " + cls.getName();
    }

    // TODO: Move SuppressWarnings annotations to inner-most scope possible.
    @SuppressWarnings("unchecked")
    private MClass getClass(MObject object) {
        MClass cls = null;
        for (Iterator i = object.getClassifiers().iterator(); i.hasNext();) {
            MClassifier classifier = (MClassifier) i.next();
            if (classifier instanceof MClass) {
                cls = (MClass) classifier;
                break;
            }
        }
        if (cls == null) {
            throw new IllegalArgumentException("No class found for " + object);
        }
        return cls;
    }

    private String getNextFigName() {
        return "Fig" + nextFigNumber++;
    }
}
