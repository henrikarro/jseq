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
 * A <code>Formatter</code> can create a <code>Diagram</code> from a program
 * trace, represented by an <code>ActivationList</code>.
 */
public interface Formatter {

    /**
     * Generates a <code>Diagram</code> from a program trace, represented by
     * an <code>ActivationList</code> with the root activations.
     *
     * @param activationList
     *            a list with the root activations for which to create a
     *            sequence diagram
     *
     * @return a <code>Diagram</code> representing the given
     *         <code>ActivationList</code>
     *
     * @throws FormatException
     *             if the <code>Diagram</code> could not be created
     */
    public Diagram format(ActivationList activationList) throws FormatException;
}
