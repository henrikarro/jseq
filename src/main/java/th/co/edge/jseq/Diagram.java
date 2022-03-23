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
import java.io.IOException;

/**
 * A <code>Diagram</code> represents some form of sequence diagram that can be
 * written to a file.
 */
public interface Diagram {

    /**
     * Writes this diagram to file, using whatever native format is used by that
     * kind of diagram: text, XML, binary, ...
     *
     * @param file
     *            the <code>File</code> to write to
     *
     * @throws IOException
     *             if writing to the file failed
     */
    public void save(File file) throws IOException;

    /**
     * Returns a string representation of this diagram.
     *
     * @return a string representation of this diagram
     */
    public String toString();
}
