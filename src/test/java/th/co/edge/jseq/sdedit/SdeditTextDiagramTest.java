/*
 * Copyright (c) 2003-2008 Henrik Arro
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

package th.co.edge.jseq.sdedit;

import java.io.StringWriter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import th.co.edge.jseq.ActivationList;
import th.co.edge.jseq.ActivationListTest;
import junit.framework.TestCase;

public class SdeditTextDiagramTest extends TestCase {
    public void testWriteDiagram() throws Exception {
        ActivationList rootActivations = ActivationListTest.buildActivationList();
        SdeditTextDiagram diagram = new SdeditTextDiagram(rootActivations);
        StringWriter writer = new StringWriter();
        diagram.writeDiagram(writer);
        String sdedit = writer.toString();
        assertEquals(1, numOccurrences("Scenarios", sdedit));
        assertEquals(1, numOccurrences("Foo", sdedit));
        assertEquals(1, numOccurrences("Bar", sdedit));
        assertEquals(1, numOccurrences("\\.testWithdrawal", sdedit));
        assertEquals(2, numOccurrences("\\.<init>", sdedit));
        assertEquals(5, numOccurrences("\\.frotz", sdedit));
        assertEquals(1, numOccurrences("\\.bar", sdedit));
        assertEquals(1, numOccurrences("\\.baz", sdedit));
    }
    
    private int numOccurrences(String regex, String s) {
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(s);
        int numOccurrences = 0;
        int start = 0;
        while (matcher.find(start)) {
            numOccurrences++;
            start = matcher.start() + 1;
        }
        return numOccurrences;
    }
}
