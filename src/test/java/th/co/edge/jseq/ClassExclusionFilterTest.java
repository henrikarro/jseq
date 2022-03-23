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

public class ClassExclusionFilterTest extends TestCase {
    private Activation foobarBaz = new Activation(null, "foo.bar",
            new TestMethodImpl("Baz"), -1);
    private Activation foobarFrotz = new Activation(null, "foo.bar",
            new TestMethodImpl("Frotz"), -1);
    private Activation foobazBaz = new Activation(null, "foo.baz",
            new TestMethodImpl("Baz"), -1);

    public ClassExclusionFilterTest(String name) {
        super(name);
    }

    public static Test suite() {
        TestSuite suite = new TestSuite(ClassExclusionFilterTest.class);
        return suite;
    }

    //
    // Test methods
    //

    public void testExactMatch() {
        ClassExclusionFilter filter = new ClassExclusionFilter("foo.bar.Baz");
        assertTrue("foo.bar.Baz should not be accepted", !filter
                .accept(foobarBaz));
        assertTrue("foo.bar.Frotz should be accepted", filter
                .accept(foobarFrotz));
    }

    public void testPrefixMatch() {
        ClassExclusionFilter filter = new ClassExclusionFilter("foo.bar.*");
        assertTrue("foo.bar.Baz should not be accepted", !filter
                .accept(foobarBaz));
        assertTrue("foo.baz.Baz should be accepted", filter.accept(foobazBaz));
    }

    public void testSuffixMatch() {
        ClassExclusionFilter filter = new ClassExclusionFilter("*.Baz");
        assertTrue("foo.bar.Baz should not be accepted", !filter
                .accept(foobarBaz));
        assertTrue("foo.bar.Frotz should be accepted", filter
                .accept(foobarFrotz));
    }

    public void testEverythingMatch() {
        ClassExclusionFilter filter = new ClassExclusionFilter("*");
        assertTrue("foo.bar.Baz should not be accepted", !filter
                .accept(foobarBaz));
        assertTrue("foo.baz.Baz should not be accepted", !filter
                .accept(foobazBaz));
        assertTrue("foo.bar.Frotz should not be accepted", !filter
                .accept(foobarFrotz));
    }
}
