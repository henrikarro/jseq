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

package th.co.edge.jseq.argouml;

import java.io.File;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import th.co.edge.jseq.Activation;
import th.co.edge.jseq.ActivationList;
import th.co.edge.jseq.Diagram;
import th.co.edge.jseq.TestMethodImpl;

public class ArgoUMLGeneratorTest extends TestCase {
    public ArgoUMLGeneratorTest(String name) {
        super(name);
    }

    public static Test suite() {
        TestSuite suite = new TestSuite(ArgoUMLGeneratorTest.class);
        return suite;
    }

    public static void main(String[] args) throws Exception {
        ArgoUMLGeneratorTest testcase = new ArgoUMLGeneratorTest("");
        testcase.test();
    }

    //
    // Test methods
    //

    public void test() throws Exception {
        ArgoUMLGenerator generator = new ArgoUMLGenerator();
        // ActivationList activationList = buildActivationList();
        ActivationList activationList = th.co.edge.jseq.ActivationListTest
                .buildActivationList();
        Diagram diagram = generator.generate(activationList);
        File file = new File("test.zargo");
        diagram.save(file);
    }

    //
    // Utility methods
    //

    public static ActivationList buildActivationList() {
        Activation root = new Activation(null, "Foo", new TestMethodImpl(
                "start"), -1);
        Activation barInit = new Activation(root, "Bar", new TestMethodImpl(
                "<init>", true), -1);
        new Activation(barInit, "Baz", new TestMethodImpl("hubba"), -1);
        new Activation(root, "Bar", new TestMethodImpl("frotz"), -1);

        ActivationList activationList = new ActivationList();
        activationList.add(root);
        return activationList;
    }
}
