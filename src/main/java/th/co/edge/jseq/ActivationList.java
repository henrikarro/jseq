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

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * An <code>ActivationList</code> holds a number of <code>Activation</code>
 * instances, representing a number of method calls. This can be used to hold
 * nested method call made by a method, or all root activation, i.e., methods
 * that are not called by one of the traced methods but "magically" from outside
 * the sequence diagram.
 */
public class ActivationList implements java.io.Serializable,
        Iterable<Activation> {
    private static final long serialVersionUID = 5359418912455509769L;

    private List<Activation> activations = new LinkedList<Activation>();

    /**
     * Adds an <code>Activation</code> to this list.
     *
     * @param activation
     *            the <code>Activation</code> to add
     */
    public void add(Activation activation) {
        activations.add(activation);
    }

    /**
     * Adds all <code>Activation</code>s from another
     * <code>ActivationList</code> to this list.
     *
     * @param activationList
     *            the <code>ActivationList</code> whose
     *            <code>Activation</code>s will be added to this list
     */
    public void addAll(ActivationList activationList) {
        activations.addAll(activationList.activations);
    }

    /**
     * Removes an <code>Activation</code> at a certain position in this list.
     *
     * @param index
     *            the position in this list to remove
     *
     * @return the removed <code>Activation</code>
     */
    public Activation remove(int index) {
        return activations.remove(index);
    }

    /**
     * Returns the <code>Activation</code> at a certain position in this list.
     *
     * @param index
     *            the position in this list to return
     *
     * @return the <code>Activation</code> at position <code>index</code> in
     *         this list
     */
    public Activation get(int index) {
        return activations.get(index);
    }

    /**
     * Returns the size of this <code>ActivationList</code>.
     *
     * @return the size of this list
     */
    public int size() {
        return activations.size();
    }

    /**
     * Returns an iterator that iterates over the <code>Activation</code>s in
     * this list, in the order they were added.
     *
     * @return an iterator that iterates over the <code>Activation</code>s in
     *         this list
     */
    public Iterator<Activation> iterator() {
        return activations.iterator();
    }

    /**
     * Returns a new <code>ActivationList</code> containing only the
     * <code>Activation</code>s for which the given filter returns
     * <code>true</code>. The filtering is also done recursively on nested
     * <code>Activation</code>s.
     *
     * @param filter
     *            the <code>Filter</code> instance used to determine which
     *            <code>Activation</code>s should be included
     *
     * @return a new <code>ActivationList</code> containing only the
     *         <code>Activation</code>s that <code>filter</code> accepts
     */
    public ActivationList filter(Filter filter) {
        ActivationList filteredList = new ActivationList();
        for (Activation activation : activations) {
            if (filter.accept(activation)) {
                Activation newActivation = new Activation(null, activation
                        .getClassName(), activation.getMethod(), -1);
                ActivationList nestedActivations = activation
                        .getNestedActivations().filter(filter);
                newActivation.setNestedActivations(nestedActivations);
                nestedActivations.setParent(newActivation);
                filteredList.add(newActivation);
            }
        }
        return filteredList;
    }

    /**
     * Returns a new <code>ActivationList</code> where the activations and
     * nested activations have been pruned, so as to start with an activation
     * that is accepted by the given filter. In other words, this method "lifts"
     * activations accepted by the filter to be root activations.
     *
     * <p>
     * For example, given the following activations:
     *
     * <pre>
     *   m
     *     m1
     *       q1
     *       q2
     *     m2
     *   p
     *     m1
     *       q3
     *     p2
     * </pre>
     *
     * and a filter that only accepts methods named "m1", the following list
     * would be returned:
     *
     * <pre>
     *   m1
     *     q1
     *     q2
     *   m1
     *     q3
     * </pre>
     *
     * @param filter
     *            the <code>Filter</code> instance used to determine which
     *            <code>Activation</code>s should be used as root activations
     *
     * @return a new <code>ActivationList</code> with <code>Activation</code>s
     *         accepted by <code>filter</code> as root activations
     */
    public ActivationList find(Filter filter) {
        ActivationList foundActivations = new ActivationList();
        for (Activation activation : activations) {
            if (filter.accept(activation)) {
                foundActivations.add(activation.copy(null));
            } else {
                foundActivations.addAll(activation.getNestedActivations().find(
                        filter));
            }
        }
        return foundActivations;
    }

    /**
     * Sets the parent <code>Activation</code> of all <code>Activation</code>s
     * in this list to the given value.
     *
     * @param parent
     *            the new parent activation
     */
    public void setParent(Activation parent) {
        for (Activation activation : activations) {
            activation.setParent(parent);
        }
    }

    /**
     * Returns an <code>ActivationList</code> where all consecutive identical
     * <code>Activation</code>s but the first have been removed, and its
     * <code>numRepetitions</code> property increased accordingly.
     *
     * @return an <code>ActivationList</code> with no repeated identical
     *         <code>Activation</code>s
     */
    public ActivationList collapseRepetitions() {
        ActivationList newList = copy();
        for (int i = 0; i < newList.size(); i++) {
            Activation activation = newList.get(i);
            while (newList.size() > i + 1) {
                Activation nextActivation = newList.get(i + 1);
                if (activation.equals(nextActivation)) {
                    newList.remove(i + 1);
                    activation.increaseNumRepetitions();
                } else {
                    break;
                }
            }
            ActivationList nestedActivations = activation
                    .getNestedActivations().collapseRepetitions();
            nestedActivations.setParent(activation);
            activation.setNestedActivations(nestedActivations);
        }
        return newList;
    }

    /**
     * Performs a deep copy of this <code>ActivationList</code>, copying all
     * <code>Activation</code>s and recursively the nested
     * <code>Activation</code>s.
     *
     * @return a new <code>ActivationList</code> containing a deep copy of all
     *         <code>Activation</code>s in this list
     */
    public ActivationList copy() {
        ActivationList newList = new ActivationList();
        for (Activation activation : activations) {
            newList.add(activation.copy(null));
        }
        return newList;
    }

    /**
     * Returns a string representation of this <code>ActivationList</code>.
     *
     * @return a string representation of this <code>ActivationList</code>
     */
    @Override
    public String toString() {
        return activations.toString();
    }

    /**
     * Compares this <code>ActivationList</code> to another object, and
     * returns <code>true</code> if and only if the other object is an
     * <code>ActivationList</code> with equal <code>Activation</code>s.
     *
     * @param o
     *            the object to compare this <code>ActivationList</code> to
     *
     * @return <code>true</code> if <code>o</code> is an
     *         <code>ActivationList</code> with equal activations,
     *         <code>false</code> otherwise
     */
    @Override
    public boolean equals(Object o) {
        boolean equal = false;
        if (o instanceof ActivationList) {
            ActivationList otherActivationList = (ActivationList) o;
            return activations.equals(otherActivationList.activations);
        }
        return equal;
    }

    /**
     * Returns a hash code for this <code>ActivationList</code>.
     *
     * @return a hash code for this <code>ActivationList</code>
     */
    @Override
    public int hashCode() {
        return activations.hashCode();
    }

    //
    // Nested top-level classes
    //

    /**
     * An interface used by the <code>filter</code> and <code>find</code>
     * methods to determine if an <code>Activation</code> is acceptable or
     * not.
     *
     * @see #filter(Filter)
     * @see #find(Filter)
     */
    public interface Filter {

        /**
         * This method determines if an <code>Activation</code> is accepted by
         * a <code>Filter</code> instance or not. It should return
         * <code>true</code> if the given <code>Activation</code> should be
         * accepted, <code>false</code> otherwise.
         *
         * @param activation
         *            the <code>Activation</code> to check for acceptance
         *
         * @return <code>true</code> if <code>activation</code> is accepted,
         *         <code>false</code> otherwise
         */
        boolean accept(Activation activation);
    }
}
