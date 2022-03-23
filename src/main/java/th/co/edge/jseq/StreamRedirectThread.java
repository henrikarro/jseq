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

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;

/**
 * A <code>StreamRedirectThread</code> redirects everything from one
 * <code>InputStream</code> to another <code>OutputStream</code>.
 * 
 * <p>
 * This class is used when launching and tracing a program to make sure that
 * what is written to <code>System.out</code> and <code>System.err</code>
 * shows up in the JSeq console windows.
 */

// TODO Add a CheckStyle or PMD rule to catch package private classes, methods
// and fields without a package comment.
class StreamRedirectThread extends Thread {
    private static final int BUFFER_SIZE = 1024;

    private final Reader in;
    private final Writer out;

    /**
     * Creates a new <code>StreamRedirectThread</code> with a given name,
     * redirecting input from the given <code>InputStream</code> to the given
     * <code>OutputStream</code>.
     * 
     * @param name
     *            the name of this <code>StreamRedirectThread</code>
     * 
     * @param in
     *            the <code>InputStream</code> to read from
     * 
     * @param out
     *            the <code>OutputStream</code> to write to
     */
    StreamRedirectThread(String name, InputStream in, OutputStream out) {
        super(name);
        this.in = new InputStreamReader(in);
        this.out = new OutputStreamWriter(out);
        setPriority(Thread.MAX_PRIORITY - 1);
    }

    /**
     * Executes in a separate Java thread, redirecting from an
     * <code>InputStream</code> to an <code>OutputStream</code>.
     */
    @Override
    public void run() {
        try {
            char[] cbuf = new char[BUFFER_SIZE];
            int count;
            while ((count = in.read(cbuf, 0, BUFFER_SIZE)) >= 0) {
                out.write(cbuf, 0, count);
                out.flush();
            }
        } catch (IOException exc) {
            System.err.println("Child I/O Transfer - " + exc);
        }
    }
}
