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

package th.co.edge.jseq;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

/**
 * A <code>TextDiagram</code> that can be fully defined by a string, for
 * example using XML.
 */
public class TextDiagram implements Diagram {
    private String text;

    /**
     * Creates a new <code>TextDiagram</code> defined by the given string.
     *
     * @param text
     *            the string defining this <code>TextDiagram</code>
     */
    public TextDiagram(String text) {
        this.text = text;
    }

    /**
     * Writes this <code>TextDiagram</code> to file using a
     * <code>Writer</code> so as to support different character encodings.
     *
     * @param file
     *            the <code>File</code> to write to
     *
     * @throws IOException
     *             if the diagram could not be saved
     */
    public void save(File file) throws IOException {
        FileWriter writer = null;
        try {
            writer = new FileWriter(file);
            writer.write(text);
        } finally {
            if (writer != null) {
                try {
                    writer.close();
                } catch (IOException e) {
                    System.err.println(e);
                }
            }
        }
    }

    /**
     * Returns the string defining this <code>TextDiagram</code>.
     *
     * @return the string defining this <code>TextDiagram</code>
     */
    @Override
    public String toString() {
        return text;
    }
}
