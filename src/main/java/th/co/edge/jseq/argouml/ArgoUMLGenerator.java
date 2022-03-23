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

import java.util.LinkedList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentType;
import org.w3c.dom.Element;

import ru.novosoft.uml.model_management.MModel;

import th.co.edge.jseq.Activation;
import th.co.edge.jseq.ActivationList;
import th.co.edge.jseq.Diagram;
import th.co.edge.jseq.argouml.util.MModelUtil;

/**
 * An <code>ArgoUMLGenerator</code> is used to generate
 * <code>ArgoUMLDiagram</code>s from an <code>ActivationList</code>
 * representing a number of root activations.
 */
public class ArgoUMLGenerator {
    private static final String ARGO_PUBLIC_ID = null;
    private static final String ARGO_SYSTEM_ID = "argo.dtd";
    private static final String ARGO_NAMESPACE = null;
    private static final String ARGOUML_VERSION = "0.12";

    private DocumentBuilder builder;
    private List<SequenceDiagram> sequenceDiagrams =
            new LinkedList<SequenceDiagram>();

    /**
     * Creates a new <code>ArgoUMLGenerator</code>.
     *
     * @throws ParserConfigurationException
     *             if there is some serious error in the XML configuration
     *             (should normally not occur)
     */
    public ArgoUMLGenerator() throws ParserConfigurationException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        this.builder = factory.newDocumentBuilder();
    }

    /**
     * Returns a new <code>ArgoUMLDiagram</code> representing the method calls
     * in the given <code>AcivationList</code>.
     *
     * @param activationList
     *            the root activations to use to create the sequence diagrams
     *
     * @return a new <code>ArgoUMLDiagram</code> representing the root
     *         activations in <code>activationList</code>
     *
     * @throws ParserConfigurationException
     *             if there is some serious error in the XML configuration
     *             (should normally not occur)
     */
    public Diagram generate(ActivationList activationList)
            throws ParserConfigurationException {
        MModel model = MModelUtil.createMModel("untitledModel");
        for (Activation activation : activationList) {
            SequenceDiagram sequenceDiagram =
                    MModelUtil.addSequenceDiagram(model, activation);
            sequenceDiagrams.add(sequenceDiagram);
        }
        Document argo = createArgoDocument(sequenceDiagrams);
        return new ArgoUMLDiagram(argo, model, sequenceDiagrams);
    }

    private Document createArgoDocument(List<SequenceDiagram> diagrams) {
        DOMImplementation impl = builder.getDOMImplementation();
        DocumentType docType =
                impl.createDocumentType("argo", ARGO_PUBLIC_ID, ARGO_SYSTEM_ID);
        Document doc = impl.createDocument(ARGO_NAMESPACE, "argo", docType);
        Element root = doc.getDocumentElement();
        root.appendChild(createDocumentationNode(doc, ARGOUML_VERSION));
        root.appendChild(createMemberNode(doc, "Untitled.xmi", "xmi"));
        for (int i = 0; i < diagrams.size(); i++) {
            String name = "SequenceDiagram" + (i++) + ".pgml";
            root.appendChild(createMemberNode(doc, name, "pgml"));
        }
        return doc;
    }

    private Element createDocumentationNode(Document doc, String version) {
        Element documentationNode = doc.createElement("documentation");
        Element versionNode = doc.createElement("version");
        documentationNode.appendChild(versionNode);
        versionNode.appendChild(doc.createTextNode(version));
        return documentationNode;
    }

    private Element createMemberNode(Document doc, String name, String type) {
        Element member = doc.createElement("member");
        member.setAttribute("type", type);
        member.setAttribute("name", name);
        return member;
    }
}
