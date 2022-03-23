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

package th.co.edge.jseq.argouml.pgml;

public class Fill {
    public static final Fill ON = new Fill(1, "ON");
    public static final Fill OFF = new Fill(0, "OFF");

    private int value;
    private String description;

    private Fill(int value, String description) {
        this.value = value;
        this.description = description;
    }

    public int getIntValue() {
        return value;
    }

    public String getStringValue() {
        return Integer.toString(value);
    }

    public String getDescription() {
        return description;
    }

    public String toString() {
        return "Fill." + getDescription();
    }
}
