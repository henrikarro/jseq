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
import java.io.IOException;

import net.sf.sdedit.Main;
import th.co.edge.jseq.ActivationList;
import th.co.edge.jseq.Diagram;

/**
 * An <code>SdeditPngDiagram</code> is a <code>Diagram</code> that can be
 * used to generate sequence diagrams in PNG format, using the <a
 * href="http://sdedit.sourceforge.net/" target="new">Quick Sequence Diagram
 * Editor</a> to create the image.
 *
 * @author jacek.ratzinger
 */
public class SdeditPngDiagram implements Diagram {

    private static final String DEFAULT_PNG_FILE_EXTENSION = ".png";

    private final SdeditTextDiagram textDiagram;

    /**
     * Creates a new <code>SdeditPngDiagram</code>, using the given
     * <code>ActivationList</code> as the basis for the sequence diagram.
     *
     * @param activationList
     *            a list of root <code>Activation</code>s to use when
     *            generating the sequence diagram
     */
    public SdeditPngDiagram(ActivationList activationList) {
        this.textDiagram = new SdeditTextDiagram(activationList);
    }

    /**
     * Creates a sequence diagram and writes it to file as a PNG image.
     *
     * @param pngFile
     *            the <code>File</code> to write to
     *
     * @throws IOException
     *             if something went wrong when creating or saving the diagram
     */
    public void save(File pngFile) throws IOException {
        String outputFilePrefix =
                pngFile.getName().substring(
                        0,
                        pngFile.getName().length() -
                                DEFAULT_PNG_FILE_EXTENSION.length());
        File tempTextFile =
                new File(pngFile.getParentFile(), outputFilePrefix + ".txt");
        textDiagram.save(tempTextFile);
        String[] sdeditArgs =
                { "-t", "png", "-o", pngFile.getAbsolutePath(),
                        tempTextFile.getAbsolutePath() };
        try {
            Main.main(sdeditArgs);
        } catch (Exception e) {
            throw new IOException(e);
        }
    }

}
