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

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * A <code>MockObjectMap</code> is a mapping from names to
 * <code>MockObject</code>s, with utility methods to make it easy to
 * translate from <code>Activation</code>s to <code>MockObject</code>s,
 * with automatic handling of column numbers.
 */
public class MockObjectMap {
    private Map<String, MockObject> objectMap = new HashMap<String, MockObject>();
    private int nextColumn = 0;

    /**
     * Ensures that all <code>MockObject</code>s in another
     * <code>MockObjectMap</code> is also in this map.
     *
     * @param otherObjectMap
     *            the <code>MockObjectMap</code> whose <code>MockObject</code>s
     *            to add to this map if necessary
     */
    public void add(MockObjectMap otherObjectMap) {
        for (MockObject object : otherObjectMap.listView()) {
            getInstance(object.getName());
        }
    }

    /**
     * Returns a new <code>MockObjectMap</code> where the method calls
     * represented by the given <code>Activation</code> and its nested
     * activations have been added as <code>MockObject</code>s to this map,
     * with the activation method call first, and the nested method calls in the
     * order they were added to the activation.
     *
     * @param activation
     *            the <code>Activation</code> whose method call and nested
     *            method calls to add to this map
     *
     * @return a new <code>MockObjectMap</code> containing all method calls
     *         from <code>activation</code>
     */
    public static MockObjectMap addAll(Activation activation) {
        MockObjectMap objectMap = new MockObjectMap();
        addAll(activation, objectMap);
        return objectMap;
    }

    private static void addAll(Activation activation, MockObjectMap objectMap) {
        objectMap.getInstance(activation.getClassName());
        for (Activation nestedActivation : activation.getNestedActivations()) {
            addAll(nestedActivation, objectMap);
        }
    }

    /**
     * Returns the <code>MockObject</code> associated with the given name. If
     * there is none, a new <code>MockObject</code> is created, using the
     * given name and the next available column number, and this object is added
     * to the map.
     *
     * @param name
     *            the name of the <code>MockObject</code> to look up
     *
     * @return the <code>MockObject</code> associated with <code>name</code>,
     *         or a newly created <code>MockObject</code> (this method never
     *         returns <code>null</code>
     */
    private MockObject getInstance(String name) {
        MockObject object = objectMap.get(name);
        if (object == null) {
            object = new MockObject(name, nextColumn++);
            objectMap.put(name, object);
        }
        return object;
    }

    /**
     * Returns the <code>MockObject</code> associated with the given name, or
     * <code>null</code> if there is none.
     *
     * @param name
     *            the name of the <code>MockObject</code> to look up
     *
     * @return the <code>MockObject</code> associated with <code>name</code>,
     *         or <code>null</code> if there is none
     */
    public MockObject get(String name) {
        return objectMap.get(name);
    }

    /**
     * Returns a list of all <code>MockObject</code>s in this map, ordered by
     * column number (and name if the same column number occurs several times,
     * which it normally should not do).
     *
     * @return a list of all <code>MockObject</code>s in this map
     */
    public List<MockObject> listView() {
        List<MockObject> values = new LinkedList<MockObject>(objectMap.values());
        Collections.sort(values);
        return values;
    }

    /**
     * Returns a string representation of this <code>MockObjectMap</code>,
     * mainly useful for logging.
     *
     * @return a string representation of this <code>MockObjectMap</code>
     */
    @Override
    public String toString() {
        return listView().toString();
    }
}
