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

import com.sun.jdi.Method;

/**
 * An <code>Activation</code> represents one method call, or in other words,
 * one stack frame.
 */
public class Activation implements java.io.Serializable {
    private static final long serialVersionUID = -774147570250028530L;
    private static final int INDENT_SIZE = 4;

    private Activation parent;
    private String className;
    private Method method;
    private int frameCount;
    private int numRepetitions = 1;
    private ActivationList nestedActivations = new ActivationList();

    /**
     * Creates a new <code>Activation</code> instance, representing a certain
     * method call.
     *
     * @param parent
     *            the <code>Activation</code> representing the method that
     *            called this method, or <code>null</code> if this is a root
     *            activation. The newly created <code>Activation</code> will
     *            be added as a nested activation of <code>parent</code>
     * @param className
     *            the name of the class that this method call belongs to
     * @param method
     *            the <code>Method</code> that is being called
     * @param frameCount
     *            the index number of the stack frame associated with this
     *            <code>Association</code>
     */
    public Activation(Activation parent, String className, Method method,
            int frameCount) {
        this.parent = parent;
        this.className = className;
        this.method = method;
        this.frameCount = frameCount;
        if (parent != null) {
            parent.add(this);
        }
    }

    /**
     * Performs a deep copy of this <code>Activation</code> instance.
     *
     * @param parentOfCopy
     *            the <code>Activation</code> representing the method that
     *            called this method, potentially different from the original,
     *            or <code>null</code> if the copy is a root activation. The
     *            newly created <code>Activation</code> will be added as a
     *            nested activation of <code>parentOfCopy</code>
     *
     * @return a copy of this <code>Activation</code> instance
     */
    public Activation copy(Activation parentOfCopy) {
        Activation copy =
                new Activation(parentOfCopy, getClassName(), getMethod(),
                        getFrameCount());
        for (Activation child : nestedActivations) {
            child.copy(copy);
        }
        return copy;
    }

    /**
     * Returns the <code>Activation</code> that called this
     * <code>Activation</code>, or <code>null</code> if this is a root
     * activation.
     *
     * @return the caller of this <code>Activation</code>
     */
    public Activation getParent() {
        return parent;
    }

    /**
     * Sets the <code>Activation</code> that called this
     * <code>Activation</code>.
     *
     * @param parent
     *            the caller of this <code>Activation</code>, or
     *            <code>null</code> if this is a root activation
     */
    public void setParent(Activation parent) {
        this.parent = parent;
    }

    /**
     * Returns the name of the class that this <code>Activation</code> belongs
     * to.
     *
     * @return the class name of this <code>Activation</code>
     */
    public String getClassName() {
        return className;
    }

    /**
     * Returns the <code>Method</code> that is being called by this
     * <code>Activation</code>.
     *
     * @return the <code>Method</code> represented by this
     *         <code>Activation</code>
     */
    public Method getMethod() {
        return method;
    }

    /**
     * Returns the index number of the stack frame associated with this
     * <code>Association</code>.
     *
     * @return the index number of this stack frame
     */
    public int getFrameCount() {
        return frameCount;
    }

    /**
     * Returns the number of times the method represented by this
     * <code>Activation</code> is being called consecutively, with no other
     * calls in between. If this number is greater than one, it may be
     * represented in a sequence diagram in some special way, e.g., "*[3]" for
     * three repetitions.
     *
     * @return the number of consecutive calls to this method
     */
    public int getNumRepetitions() {
        return numRepetitions;
    }

    /**
     * Increases the number of consecutive calls of this method by one.
     */
    public void increaseNumRepetitions() {
        numRepetitions++;
    }

    /**
     * Adds another nested <code>Activation</code> to this
     * <code>Activation</code>. This represents a method call made by this
     * method.
     *
     * @param nestedActivation
     *            the nested <code>Activation</code> to add to this
     *            <code>Activation</code>
     */
    public void add(Activation nestedActivation) {
        nestedActivations.add(nestedActivation);
    }

    /**
     * Returns an <code>ActivationList</code> with all nested
     * <code>Activation</code>s, that is, all methods called by this method.
     *
     * @return an <code>ActivationList</code> with all nested activations.
     */
    public ActivationList getNestedActivations() {
        return nestedActivations;
    }

    /**
     * Sets the <code>ActivationList</code> with all nested
     * <code>Activation</code>s called by this method.
     *
     * @param nestedActivations
     *            the new <code>ActivationList</code> with nested activations.
     */
    public void setNestedActivations(ActivationList nestedActivations) {
        this.nestedActivations = nestedActivations;
    }

    /**
     * Returns the number of nested activations.
     *
     * @return the number of method calls made by this activation.
     */
    public int getNumCalls() {
        return nestedActivations.size();
    }

    /**
     * Returns a string representation of this <code>Activation</code> and all
     * its nested <code>Activation</code>s, with each activation on a
     * separate line, and nested activations indented.
     *
     * @return a string representation of this <code>Activation</code>
     */
    @Override
    public String toString() {
        return toString(0);
    }

    /**
     * As <code>toString()</code>, but indents the first activation a given
     * number of spaces.
     *
     * @param indent
     *            the number of spaces to indent the first activation
     *
     * @return a string representation of this <code>Activation</code>, with
     *         the first indented <code>indent</code> number of spaces, and
     *         nested activations further indented
     */
    public String toString(int indent) {
        StringBuffer s = new StringBuffer();
        indent(s, indent);
        s.append(getClassName() + ".");
        s.append(getMethod().name());
        if (getNumRepetitions() > 1) {
            s.append(" (x " + getNumRepetitions() + ")");
        }
        s.append("\n");
        for (Activation nestedActivation : nestedActivations) {
            s.append(nestedActivation.toString(indent + INDENT_SIZE));
        }
        return s.toString();
    }

    private void indent(StringBuffer s, int indent) {
        for (int i = 0; i < indent; i++) {
            s.append(" ");
        }
    }

    /**
     * Compares this <code>Activation</code> to another object, and returns
     * <code>true</code> if and only if the other object is an
     * <code>Activation</code> with the same class and method names, and equal
     * nested activations.
     *
     * @param o
     *            the object to compare this <code>Activation</code> to
     *
     * @return <code>true</code> if <code>o</code> is an
     *         <code>Activation</code> with the same class and method names
     *         and the equal nested activations, <code>false</code> otherwise
     */
    @Override
    public boolean equals(Object o) {
        boolean equal = false;
        if (o instanceof Activation) {
            Activation otherActivation = (Activation) o;
            equal =
                    className.equals(otherActivation.className) &&
                            method.name().equals(otherActivation.method.name()) &&
                            nestedActivations
                                    .equals(otherActivation.nestedActivations);
        }
        return equal;
    }

    /**
     * Returns a hash code for this <code>Activation</code>.
     *
     * @return a hash code for this <code>Activation</code>
     */
    @Override
    public int hashCode() {
        int result = 17;
        result = 31 * result + className.hashCode();
        result = 31 * result + method.name().hashCode();
        result = 31 * result + nestedActivations.hashCode();
        return result;
    }

}
