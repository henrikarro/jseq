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

package th.co.edge.jseq.util;

import java.util.ArrayList;

import org.w3c.dom.DocumentType;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * A utility class to work with <code>org.w3c.dom.Node</code> objects.
 */
public class XMLUtil {
    /**
     * This class only contains static methods, so we hide the constructor.
     */
    private XMLUtil() {
    }

    /**
     * Traverses the given <code>Node</code> and its children depth-first,
     * calling the given <code>Visitor</code> for each node.
     *
     * <p>
     * If the traversal should be stopped at a certain node, the
     * <code>Visitor</code> should return <code>true</code> from the
     * <code>visit</code> method for that node. Obviously, if the entire tree
     * should be traversed, the <code>visit</code> method should always return
     * <code>false</code>.
     *
     * @param node
     *            the root <code>Node</code> at which to start the traversal
     * @param visitor
     *            the <code>Visitor</code>
     *
     * @see Visitor#visit(Node)
     */
    public static void traverse(Node node, Visitor visitor) {
        find(node, visitor);
    }

    /**
     * Searches the given <code>Node</code> and its children depth-first,
     * looking for the first <code>Node</code> that satisfies the given
     * <code>Visitor</code>.
     *
     * @param node
     *            the root <code>Node</code> at which to start the search
     * @param visitor
     *            the <code>Visitor</code> that is called for every node, and
     *            determines if the right node has been found by returning
     *            <code>true</code> from the <code>visit</code> method
     *
     * @return the first <code>Node</code> in a depth-first search that
     *         satisfies <code>visitor</code>, or <code>null</code> if
     *         there is none.
     *
     * @see Visitor#visit(Node)
     */
    public static Node find(Node node, Visitor visitor) {
        Node result = null;
        if (visitor.visit(node)) {
            result = node;
        } else {
            NodeList children = node.getChildNodes();
            for (int i = 0; i < children.getLength(); i++) {
                Node child = children.item(i);
                result = find(child, visitor);
                if (result != null) {
                    break;
                }
            }
        }
        visitor.afterVisit(node);
        return result;
    }

    /**
     * Searches a <code>Node</code> and its children depth-first for an
     * element node with a given name.
     *
     * @param node
     *            the root <code>Node</code> at which to start the search
     * @param tag
     *            the name of the tag to search for
     *
     * @return the <code>Node</code> found, or <code>null</code>
     */
    public static Node findTag(Node node, String tag) {
        return findTag(node, tag, null);
    }

    /**
     * Searches a <code>Node</code> and its children depth-first for an
     * element node with a given name, and also containing a text sub-node with
     * a given text.
     *
     * @param node
     *            the root <code>Node</code> at which to start the search
     * @param tag
     *            the name of the tag to search for
     * @param text
     *            a string that must be included in the first text sub-node of
     *            the found node, or <code>null</code> if only the tag name
     *            should be used to determine if a node matches
     *
     * @return the <code>Node</code> found, or <code>null</code>
     */
    public static Node findTag(Node node, String tag, String text) {
        Node result = find(node, new TagFinder(tag, text));
        return result;
    }

    /**
     * Searches a <code>Node</code> and its children depth-first, returning an
     * array with all element nodes with a given name.
     *
     * @param node
     *            the root <code>Node</code> at which to start the search
     * @param tag
     *            the name of the tags to search for
     *
     * @return a <code>Node</code> array with all element nodes with the given
     *         <code>name</code> starting from <code>node</code>. If no
     *         such nodes exist, an empty array is returned
     */
    public static Node[] findAllTags(Node node, String tag) {
        return findAllTags(node, tag, null);
    }

    /**
     * Searches a <code>Node</code> and its children depth-first, returning an
     * array with all element nodes with a given name, and also containing a
     * text sub-node with a given text.
     *
     * @param node
     *            the root <code>Node</code> at which to start the search
     * @param tag
     *            the name of the tags to search for
     * @param text
     *            a string that must be included in the first text sub-node of
     *            the found node, or <code>null</code> if only the tag name
     *            should be used to determine if a node matches
     *
     * @return a <code>Node</code> array with all element nodes with the given
     *         <code>name</code> and containing a text node with
     *         <code>text</code>, starting from <code>node</code>. If no
     *         such nodes exist, an empty array is returned
     *
     */
    public static Node[] findAllTags(Node node, String tag, String text) {
        TagCollector tagCollector = new TagCollector(tag, text);
        traverse(node, tagCollector);
        return tagCollector.getNodes();
    }

    /**
     * Returns the first text sub-node of the given <code>Node</code>.
     *
     * @param node
     *            the <code>Node</code> whose children to search for the first
     *            text node
     *
     * @return the first text sub-node of <code>node</code>, or
     *         <code>null</code> if there is none
     */
    public static String getText(Node node) {
        String text = getText(node, 0);
        return text;
    }

    /**
     * Returns the nth text sub-node of the given <code>Node</code>.
     *
     * @param node
     *            the <code>Node</code> whose children to search for the
     *            <code>nth</code> text node
     * @param nth
     *            the index of the text childe node to return
     *
     * @return the <code>nth</code> text sub-node of <code>node</code>, or
     *         <code>null</code> if <code>node</code> has fewer than
     *         <code>nth</code> text nodes as children
     */
    public static String getText(Node node, int nth) {
        String text = null;
        int numFound = -1;
        NodeList children = node.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            Node child = children.item(i);
            if (child.getNodeType() == Node.TEXT_NODE) {
                numFound++;
                if (numFound == nth) {
                    text = child.getNodeValue().trim();
                    break;
                }
            }
        }
        return text;
    }

    /**
     * Returns the named attribute in a <code>Node</code>.
     *
     * @param node
     *            the <code>Node</code> in which to look for attributes
     * @param attributeName
     *            the name of the attribute to look up
     *
     * @return the attribute node for the attribute named
     *         <code>attributeName</code> in <code>Node</code>, or
     *         <code>null</code> if there is no attribute with that name
     */
    public static Node getAttribute(Node node, String attributeName) {
        NamedNodeMap attributes = node.getAttributes();
        Node attributeValue = attributes.getNamedItem(attributeName);
        return attributeValue;
    }

    /**
     * Returns the string value of a named attribute in a <code>Node</code>.
     *
     * @param node
     *            the <code>Node</code> in which to look for attributes
     * @param attributeName
     *            the name of the attribute to look up
     *
     * @return the string value of the attribute named
     *         <code>attributeName</code> in <code>Node</code>, or
     *         <code>null</code> if there is no attribute with that name
     */
    public static String getAttributeText(Node node, String attributeName) {
        String attributeText = null;
        Node attribute = getAttribute(node, attributeName);
        if (attribute != null) {
            attributeText = getText(attribute);
        }
        return attributeText;
    }

    /**
     * Returns a string representation of a <code>Node</code> and its
     * children, pretty-printed so as to include new-lines and with each
     * sub-node indented.
     *
     * @param node
     *            the <code>Node</code> to represent as a string
     *
     * @return a pretty-printed string representation of <code>Node</code>
     */
    public static String toString(Node node) {
        return toString(node, true);
    }

    /**
     * Returns a string representation of a <code>Node</code> and its
     * children.
     *
     * @param node
     *            the <code>Node</code> to represent as a string
     * @param prettyPrint
     *            if <code>true</code>, the string representation of
     *            <code>Node</code> will be pretty-printed, so as to include
     *            new-lines and with each sub-node indented
     *
     * @return a string representation of <code>Node</code>
     */
    public static String toString(Node node, boolean prettyPrint) {
        NodePrinter nodePrinter = new XMLUtil.NodePrinter(prettyPrint);
        traverse(node, nodePrinter);
        return nodePrinter.toString();
    }

    /**
     * Returns a copy of a string where all special XML characters have been
     * replaced by the corresponding character entity reference, for example, "<"
     * is replaced by "&lt;".
     *
     * @param original
     *            the string to make safe to use in an XML document
     *
     * @return a copy of <code>original</code> with all special XML characters
     *         replaced by the corresponding character entity reference
     */
    public static String makeXMLSafe(String original) {
        StringBuffer s = new StringBuffer();
        for (int i = 0; i < original.length(); i++) {
            if (original.charAt(i) == '<') {
                s.append("&lt;");
            } else if (original.charAt(i) == '>') {
                s.append("&gt;");
            } else if (original.charAt(i) == '&') {
                s.append("&amp;");
            } else {
                s.append(original.charAt(i));
            }
        }
        return s.toString();
    }

    //
    // Nested top-level classes
    //

    /**
     * The <code>Visitor</code> interface is used when traversing XML
     * documents. To use it, create a class that implements the interface and
     * give it to the <code>traverse</code> and <code>find</code> methods.
     *
     * @see XMLUtil#traverse(Node, th.co.edge.jseq.util.XMLUtil.Visitor)
     * @see XMLUtil#find(Node, th.co.edge.jseq.util.XMLUtil.Visitor)
     */
    public interface Visitor {
        /**
         * Called for every <code>Node</code> that is traversed, before the
         * children are visited.
         *
         * @param node
         *            the <code>Node</code> currently being visited.
         *
         * @return <code>true</code> if you want to stop the traversal, or if
         *         you have found the <code>Node</code> you are looking for,
         *         <code>false</code> otherwise
         */
        public boolean visit(Node node);

        /**
         * Called after the children of the Node have been traversed.
         *
         * @param node
         *            the <code>Node</code> whose children have just been
         *            traversed
         */
        public void afterVisit(Node node);
    }

    /**
     * An implementation of <code>Visitor</code> that creates a string
     * representation of a <code>Node</code> and its children, or in other
     * words, to a (part of) an XML document.
     *
     * <p>
     * To use this class, create a new <code>NodePrinter</code> instance, call
     * <code>XMLUtil.traverse</code> using it, and then call the
     * <code>toString</code> method on the <code>NodePrinter</code>.
     */
    public static class NodePrinter implements XMLUtil.Visitor {
        private static final String NEW_LINE =
                System.getProperty("line.separator");

        private final StringBuffer stringBuffer = new StringBuffer();
        private final boolean prettyPrint;
        private int indent = 0;

        /**
         * Creates a new <code>NodePrinter</code>.
         *
         * @param prettyPrint
         *            if <code>true</code> string representation of an XML
         *            document will contain new-lines, with each sub-node
         *            indented
         */
        public NodePrinter(boolean prettyPrint) {
            this.prettyPrint = prettyPrint;
        }

        /**
         * Adds the string representation appropriate at the beginning of the
         * given <code>Node</code>. For example, for an element node, this
         * would add the start tag with its attributes; for a document node,
         * this would add the DOCTYPE declaration, and so on.
         *
         * @param node
         *            the <code>Node</code> for which to generate a string
         *            representation
         *
         * @return <code>false</code>, so as never to stop the traversal
         */
        public boolean visit(Node node) {
            switch (node.getNodeType()) {
            case Node.DOCUMENT_NODE:
                stringBuffer.append("<?xml version=\"1.0\"?>");
                newline();
                break;
            case Node.DOCUMENT_TYPE_NODE:
                DocumentType docType = (DocumentType) node;
                if (docType.getPublicId() != null ||
                        docType.getSystemId() != null) {
                    stringBuffer.append("<!DOCTYPE " + docType.getName());
                    if (docType.getPublicId() != null) {
                        stringBuffer.append(" PUBLIC \"" +
                                docType.getPublicId() + "\" ");
                        stringBuffer.append("\"" + docType.getSystemId() +
                                "\">");
                    } else {
                        stringBuffer.append(" SYSTEM \"" +
                                docType.getSystemId() + "\">");
                    }
                    newline();
                }
                break;
            case Node.ELEMENT_NODE:
                indent();
                stringBuffer.append("<" + node.getNodeName());
                stringBuffer.append(getAttributes(node));
                // If this node is empty, add the trailing slash.
                // For example, "<br/>".
                if (node.getChildNodes().getLength() == 0) {
                    stringBuffer.append("/");
                    indent -= 2;
                }
                stringBuffer.append(">");
                // Print a newline unless the only child is text, to be printed
                // inline.
                // For example "<foo>This is text</foo>".
                if (node.getChildNodes().getLength() != 1 ||
                        node.getFirstChild().getNodeType() != Node.TEXT_NODE) {
                    newline();
                    indent += 2;
                }
                break;
            case Node.TEXT_NODE:
                stringBuffer.append(node.getNodeValue());
                break;
            default:
                // Do nothing
                break;
            }
            return false;
        }

        /**
         * For an element node, adds the end tag for the given <code>Node</code>
         * to the string representation of the XML document, if necessary.
         *
         * @param node
         *            the <code>Node</code> being visited
         */
        public void afterVisit(Node node) {
            if (node.getNodeType() == Node.ELEMENT_NODE &&
                    node.getChildNodes().getLength() != 0) {
                // Indent unless the only child is text, printed inline.
                // For example "<foo>This is text</foo>".
                if (node.getChildNodes().getLength() != 1 ||
                        node.getFirstChild().getNodeType() != Node.TEXT_NODE) {
                    indent -= 2;
                    indent();
                }
                stringBuffer.append("</" + node.getNodeName() + ">");
                newline();
            }
        }

        private void indent() {
            if (prettyPrint) {
                for (int i = 0; i < indent; i++) {
                    stringBuffer.append(" ");
                }
            }
        }

        private void newline() {
            if (prettyPrint) {
                stringBuffer.append(NEW_LINE);
            }
        }

        private String getAttributes(Node node) {
            StringBuffer sb = new StringBuffer();
            NamedNodeMap attributes = node.getAttributes();
            if (attributes != null) {
                for (int i = 0; i < attributes.getLength(); i++) {
                    Node attribute = attributes.item(i);
                    sb.append(" " + attribute.getNodeName() + "=\"" +
                            attribute.getNodeValue() + "\"");
                }
            }
            return sb.toString();
        }

        /**
         * Returns a string representation of the <code>Node</code> that has
         * been traversed using this <code>NodePrinter</code>, or the empty
         * string if the <code>Node</code> has not yet been traversed.
         *
         * @return a string representation of the <code>Node</code> that has
         *         been traversed
         */
        @Override
        public String toString() {
            return stringBuffer.toString();
        }
    }

    private static class TagFinder implements XMLUtil.Visitor {
        private String tag;
        private String text;

        public TagFinder(String tag, String text) {
            this.tag = tag;
            this.text = text;
        }

        public boolean visit(Node node) {
            boolean found = false;
            if (node.getNodeType() == Node.ELEMENT_NODE &&
                    node.getNodeName().equals(tag)) {
                if (text == null) {
                    found = true;
                } else {
                    String nodeText = XMLUtil.getText(node);
                    if (nodeText != null) {
                        found = nodeText.equals(text);
                    }
                }
            }
            return found;
        }

        public void afterVisit(Node node) {
        }
    }

    private static class TagCollector extends TagFinder {
        private ArrayList<Node> nodes = new ArrayList<Node>();

        public TagCollector(String tag, String text) {
            super(tag, text);
        }

        @Override
        public boolean visit(Node node) {
            if (super.visit(node)) {
                nodes.add(node);
            }
            return false;
        }

        public Node[] getNodes() {
            Node[] nodeArray = new Node[nodes.size()];
            nodes.toArray(nodeArray);
            return nodeArray;
        }
    }
}
