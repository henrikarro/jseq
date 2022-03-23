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

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

public class ActivationListTest extends TestCase {
    public ActivationListTest(String name) {
        super(name);
    }

    public static Test suite() {
        TestSuite suite = new TestSuite(ActivationListTest.class);
        return suite;
    }

    //
    // Test methods
    //

    public void testFilter() {
        ActivationList list = buildActivationList();
        ActivationList filteredList = list.filter(new ClassExclusionFilter(
                "Bar.*"));
        assertEquals(1, filteredList.size());
        Activation root = filteredList.get(0);
        assertEquals(3, root.getNumCalls());
        Activation fooInit = root.getNestedActivations().get(0);
        assertEquals(0, fooInit.getNumCalls());
    }

    public void testFind() {
        ActivationList list = buildActivationList();
        ActivationList filteredList = list.find(new MethodFilter("Foo.<init>"));
        assertEquals(1, filteredList.size());
        Activation root = filteredList.get(0);
        assertEquals(6, root.getNumCalls());
        Activation fooInit = root.getNestedActivations().get(0);
        assertEquals(0, fooInit.getNumCalls());
    }

    public void testCopy() {
        ActivationList list = buildActivationList();
        ActivationList copy = list.copy();
        assertEquals(list, copy);
    }

    public void testCollapseRepetitions() {
        ActivationList list = buildActivationList();
        // Bar.<init> + 5 calls to Bar.frotz = 6 calls
        int numCallsByFooInit = list.get(0).getNestedActivations().get(0)
                .getNumCalls();
        assertEquals(6, numCallsByFooInit);
        ActivationList noRepetitions = list.collapseRepetitions();
        // Bar.<init> + 1 call to Bar.frotz (x5) = 2 calls
        numCallsByFooInit = noRepetitions.get(0).getNestedActivations().get(0)
                .getNumCalls();
        assertEquals(2, numCallsByFooInit);
    }

    //
    // Utiltity methods
    //

    /**
     * Creates a test activation list.
     * 
     * The activation list looks as follows:
     * 
     * <pre><code>
     * [Scenarios.testWithdrawal
     *     Foo.&lt;init&gt;
     *         Bar.&lt;init&gt;
     *         Bar.frotz
     *         Bar.frotz
     *         Bar.frotz
     *         Bar.frotz
     *         Bar.frotz
     *     Foo.bar
     *     Foo.baz
     * ]
     * </code></pre>
     */
    public static ActivationList buildActivationList() {
        Activation root = new Activation(null, "Scenarios", new TestMethodImpl(
                "testWithdrawal"), -1);
        Activation fooInit = new Activation(root, "Foo", new TestMethodImpl(
                "<init>", true), -1);
        new Activation(fooInit, "Bar", new TestMethodImpl("<init>", true), -1);
        new Activation(fooInit, "Bar", new TestMethodImpl("frotz"), -1);
        new Activation(fooInit, "Bar", new TestMethodImpl("frotz"), -1);
        new Activation(fooInit, "Bar", new TestMethodImpl("frotz"), -1);
        new Activation(fooInit, "Bar", new TestMethodImpl("frotz"), -1);
        new Activation(fooInit, "Bar", new TestMethodImpl("frotz"), -1);

        new Activation(root, "Foo", new TestMethodImpl("bar"), -1);
        new Activation(root, "Foo", new TestMethodImpl("baz"), -1);
        ActivationList list = new ActivationList();
        list.add(root);
        return list;
    }
}
