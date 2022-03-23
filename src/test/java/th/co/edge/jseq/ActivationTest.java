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

public class ActivationTest extends TestCase {
    public ActivationTest(String name) {
        super(name);
    }

    public static Test suite() {
        TestSuite suite = new TestSuite(ActivationTest.class);
        return suite;
    }

    //
    // Test methods
    //

    public void testConstructor() {
        Activation parent = new Activation(null, "class1", new TestMethodImpl(
                "method1"), -1);
        assertEquals("class1", parent.getClassName());
        assertEquals("method1", parent.getMethod().name());
        assertEquals(null, parent.getParent());

        Activation child = new Activation(parent, "class2", new TestMethodImpl(
                "method2"), -1);
        assertEquals("class2", child.getClassName());
        assertEquals("method2", child.getMethod().name());
        assertEquals(parent, child.getParent());

        assertEquals(1, parent.getNumCalls());
        assertEquals(0, child.getNumCalls());

        assertEquals(child, parent.getNestedActivations().get(0));
    }
}
