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

/**
 * A <code>FormatException</code> signals that a <code>Formatter</code>
 * failed to create a diagram.
 *
 * @see Formatter#format(ActivationList)
 */
public class FormatException extends Exception {

    /**
     * Creates a new <code>FormatException</code> with the given detail
     * message.
     *
     * @param message
     *            the detail message
     */
    public FormatException(String message) {
        super(message);
    }

    /**
     * Creates a new <code>FormatException</code> with the given detail
     * message and root cause.
     *
     * @param message
     *            the detail message
     * @param cause
     *            the root cause of this exception
     */
    public FormatException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Creates a new <code>FormatException</code> with the given root cause.
     *
     * @param cause
     *            the root cause
     */
    public FormatException(Throwable cause) {
        super(cause);
    }
}
