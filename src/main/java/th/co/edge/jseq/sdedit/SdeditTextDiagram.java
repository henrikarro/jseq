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

package th.co.edge.jseq.sdedit;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

import th.co.edge.jseq.Activation;
import th.co.edge.jseq.ActivationList;
import th.co.edge.jseq.Diagram;

/**
 * An <code>SdeditTextDiagram</code> is a <code>Diagram</code> that can be
 * used to generate sequence diagrams as text files that can be read by the <a
 * href="http://sdedit.sourceforge.net/" target="new">Quick Sequence Diagram
 * Editor</a>.
 */
public class SdeditTextDiagram implements Diagram {
    private static final String NEW_LINE = "\n";
    private ActivationList activationList;

    /**
     * Creates a new <code>SdeditTextDiagram</code>, using the given
     * <code>ActivationList</code> as the basis for the sequence diagram.
     * 
     * @param activationList
     *            a list of root <code>Activation</code>s to use when
     *            generating the sequence diagram
     */
    public SdeditTextDiagram(ActivationList activationList) {
        this.activationList = activationList;
    }

    /**
     * Creates a textual description of a sequence diagram understandable by the
     * <a href="http://sdedit.sourceforge.net/" target="new">Quick Sequence
     * Diagram Editor</a>, and saves it to a text file.
     * 
     * @param file
     *            the <code>File</code> to write to
     * 
     * @throws IOException
     *             if something went wrong when creating or saving the diagram
     */
    public void save(File file) throws IOException {
        FileWriter writer = null;
        try {
            writer = new FileWriter(file);
            writeDiagram(writer);
        } finally {
            if (writer != null) {
                writer.close();
            }
        }
    }

    /**
     * Same as the <code>save</code> method, but writes to a
     * <code>Writer</code> instead of to a <code>File</code>. This is
     * useful when testing.
     * 
     * @param writer
     *            the <code>Writer</code> to write to
     * 
     * @throws IOException
     *             if something went wrong when creating or writing the diagram
     * 
     * @see #save(File)
     */
    void writeDiagram(Writer writer) throws IOException {
        writeActorsAndObjectNames(writer);
        writer.write(NEW_LINE);

        int index = 1;
        for (Activation activation : activationList) {
            writeActivation(activation, index, writer);
            writer.write(NEW_LINE);
            index++;
        }
    }

    private void writeActorsAndObjectNames(Writer writer) throws IOException {
        int index = 1;
        for (Activation activation : activationList) {
            writer.write("Actor" + index + ":Actor");
            writer.write(NEW_LINE);
            writeObjectNames(activation, writer, index, new ArrayList<String>());
            index++;
        }
    }

    private void writeObjectNames(Activation activation, Writer writer,
            int index, List<String> objectNames) throws IOException {
        String objectName = getObjectName(activation, index);
        if (!objectNames.contains(objectName)) {
            objectNames.add(objectName);
            writer.write(objectName);
            writer.write(":");
            writer.write(getClassName(activation));
            writer.write("[a]");
            writer.write(NEW_LINE);
        }
        for (Activation nestedActivation : activation.getNestedActivations()) {
            writeObjectNames(nestedActivation, writer, index, objectNames);
        }
    }

    private void writeActivation(Activation activation, int index, Writer writer)
            throws IOException {
        writer.write("Actor" + index);
        writer.write(":");
        writer.write(getObjectName(activation, index));
        writer.write(".");
        writer.write(getMethodName(activation));
        writer.write(NEW_LINE);
        for (Activation nestedActivation : activation.getNestedActivations()) {
            writeActivation(nestedActivation, index, 1, writer);
        }
        writer.write(getObjectName(activation, index) + ":stop");
    }

    private void writeActivation(Activation activation, int index,
            int nestingLevel, Writer writer) throws IOException {
        indent(writer, nestingLevel);
        writer.write(getObjectName(activation.getParent(), index));
        writer.write(":");
        writer.write(getObjectName(activation, index));
        writer.write(".");
        writer.write(getMethodName(activation));
        writer.write(NEW_LINE);

        for (Activation nestedActivation : activation.getNestedActivations()) {
            writeActivation(nestedActivation, index, nestingLevel + 1, writer);
        }
    }

    private void indent(Writer writer, int depth) throws IOException {
        for (int i = 0; i < depth; i++) {
            writer.write("  ");
        }
    }

    private String getObjectName(Activation activation, int index) {
        return getClassName(activation).toLowerCase() + index;
    }

    private String getClassName(Activation activation) {
        String qualifiedClassName = activation.getClassName();
        int lastDot = qualifiedClassName.lastIndexOf('.');
        String result = qualifiedClassName.substring(lastDot + 1);
        if (result.contains("$")) {
            int lastInnerClassSeperator = result.lastIndexOf('$');
            result = result.substring(lastInnerClassSeperator + 1);
        }
        return result;
    }

    private String getMethodName(Activation activation) {
        String methodName = activation.getMethod().name();
        return methodName;
    }

    /**
     * Returns a string representation of this <code>SdeditTextDiagram</code>,
     * currently in the form of comma-separated list of the root activations,
     * <b>not</b> in the same form written to a file using the
     * <code>save</code> method. This may change in the future.
     * 
     * @return a string representation of this <code>SdeditTextDiagram</code>
     */
    @Override
    public String toString() {
        return activationList.toString();
    }
}
