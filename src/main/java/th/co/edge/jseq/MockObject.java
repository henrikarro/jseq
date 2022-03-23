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
 * A <code>MockObject</code> is an immutable class representing one life-line
 * in a sequence diagram.
 */
public class MockObject implements Comparable<MockObject> {
    private final String name;
    private final int column;

    /**
     * Creates a new <code>MockObject</code> with a given name and column in
     * the diagram.
     *
     * @param name
     *            the name that will be displayed at the top of the life-line
     *
     * @param column
     *            the column number of this life-line
     */
    public MockObject(String name, int column) {
        this.name = name;
        this.column = column;
    }

    /**
     * Returns the name that will be displayed at the top of the life-line.
     *
     * @return the name of this <code>MockObject</code>
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the column number of this <code>MockObject</code>, which
     * determines the order of the life-lines in the sequence diagram.
     *
     * @return the column number of this <code>MockObject</code>
     */
    public int getColumn() {
        return column;
    }

    /**
     * Returns a string representation of this <code>MockObject</code>,
     * mainly useful for logging.
     *
     * @return a string representation of this <code>MockObject</code>
     */
    @Override
    public String toString() {
        return "{" + getName() + "," + getColumn() + "}";
    }

    /**
     * Compares this <code>MockObject</code> to another object and returns
     * <code>true</code> if and only if the other object is a
     * <code>MockObject</code> with the same name and column number.
     *
     * @param o
     *            the object to compare this <code>MockObject</code> to
     *
     * @return <code>true</code> if <code>o</code> is a
     *         <code>MockObject</code> with the same name and column number,
     *         <code>false</code> otherwise
     *
     */
    @Override
    public boolean equals(Object o) {
        boolean equal = false;
        if (o instanceof MockObject) {
            MockObject otherMockObject = (MockObject) o;
            equal =
                    name.equals(otherMockObject.name) &&
                            column == otherMockObject.column;
        }
        return equal;
    }

    /**
     * Returns a hash code for this <code>MockObject</code>.
     *
     * @return a hash code for this <code>MockObject</code>
     */
    @Override
    public int hashCode() {
        int result = 17;
        result = 31 * result + name.hashCode();
        result = 31 * result + column;
        return result;
    }

    /**
     * Compares this <code>MockObject</code> to another
     * <code>MockObject</code>, returning a negative integer, zero or a
     * positive integer if this object is less than, equal to, or greater than
     * the other object, respectively.
     *
     * <p>
     * Comparison is done as follows:
     * <ul>
     * <li>First the column numbers are compared</li>
     * <li>If the column numbers are equal, the names are compared
     * lexicographically</li>
     * </ul>
     *
     * @param otherObject
     *            the <code>MockObject</code> to compare this object to
     *
     * @return <code>0</code> if this object is equal to
     *         <code>otherObject</code>; a negative integer if this object is
     *         smaller than <code>otherObject</code>; and a positive integer
     *         if this object is greater than <code>otherObject</code>
     */
    public int compareTo(MockObject otherObject) {
        int result = getColumn() - otherObject.getColumn();
        if (result == 0) {
            result = name.compareTo(otherObject.name);
        }
        return result;
    }
}
