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

public class MethodFilterTest extends TestCase {
    public MethodFilterTest(String name) {
        super(name);
    }

    public static Test suite() {
        TestSuite suite = new TestSuite(MethodFilterTest.class);
        return suite;
    }

    //
    // Test methods
    //

    public void testConstructor() {
        MethodFilter filter = new MethodFilter("Foo.<init>");
        assertEquals("Foo", filter.getClassName());
        assertEquals("<init>", filter.getMethodName());
    }

    public void testAccept() {
        MethodFilter filter = new MethodFilter("Foo.<init>");
        Activation activation = new Activation(null, "Foo", new TestMethodImpl(
                "<init>", true), -1);
        assertTrue("Filter should accept Foo.<init>", filter.accept(activation));
        activation = new Activation(null, "Bar", new TestMethodImpl("<init>"), -1);
        assertTrue("Filter should not accept Bar.<init>", !filter
                .accept(activation));
        activation = new Activation(null, "Foo", new TestMethodImpl("bar"), -1);
        assertTrue("Filter should not accept Foo.bar", !filter
                .accept(activation));
    }
}
