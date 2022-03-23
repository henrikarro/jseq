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

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.xml.parsers.ParserConfigurationException;

import th.co.edge.jseq.argouml.ArgoUMLGenerator;
import th.co.edge.jseq.sdedit.SdeditPngDiagram;
import th.co.edge.jseq.sdedit.SdeditTextDiagram;
import th.co.edge.jseq.svg.SVGGenerator;

/**
 * A singleton that holds all <code>Formatter</code>s known to the system.
 */
public class FormatterRegistry {
    private static FormatterRegistry instance = new FormatterRegistry();

    private Map<String, Formatter> formatters = new HashMap<String, Formatter>();

    private FormatterRegistry() {
        formatters.put("text", new TextFormatter());
        formatters.put("png", new SdeditPngFormatter());
        formatters.put("sdedit", new SdeditTextFormatter());
        formatters.put("svg", new SVGFormatter());
        formatters.put("argouml", new ArgoUMLFormatter());
    }

    /**
     * Returns the only instance of this singleton. By default, it contains
     * <code>Formatter</code>s for some commonly used formats, e.g., text and
     * SVG, but more can be added using the <code>register</code> method.
     *
     * @return the only instance of this singleton
     *
     * @see #register(String, Formatter)
     */
    public static FormatterRegistry getInstance() {
        return instance;
    }

    /**
     * Returns the <code>Formatter</code> associated with the given name.
     *
     * @param type
     *            the name of the <code>Formatter</code> to look up
     *
     * @return the <code>Formatter</code> associated with <code>type</code>
     */
    public Formatter get(String type) {
        Formatter formatter = formatters.get(type);
        if (formatter == null) {
            throw new IllegalArgumentException("Illegal type: " + type
                    + ". Should be one of " + getFormatterTypes());
        }
        return formatter;
    }

    /**
     * Returns a comma-separated list with the names of the formatters that have
     * been registered.
     *
     * @return a comma-separated list with names of all formatters known to the
     *         system
     */
    public String getFormatterTypes() {
        StringBuffer s = new StringBuffer();
        for (Iterator<String> i = formatters.keySet().iterator(); i.hasNext();) {
            s.append(i.next());
            if (i.hasNext()) {
                s.append(", ");
            }
        }
        return s.toString();
    }

    /**
     * Registers a new <code>Formatter</code> so that it can be used
     *
     * @param type
     *            the name of the <code>Formatter</code>
     *
     * @param formatter
     *            the <code>Formatter</code> to associate with
     *            <code>type</code>
     */
    public void register(String type, Formatter formatter) {
        formatters.put(type, formatter);
    }

    //
    // Nested top-level classes
    //

    private static class TextFormatter implements Formatter {
        public Diagram format(ActivationList activationList) {
            return new TextDiagram(activationList.toString());
        }
    }

    private static class SdeditPngFormatter implements Formatter {
        public Diagram format(ActivationList activationList)
                throws FormatException {
            return new SdeditPngDiagram(activationList);
        }
    }

    private static class SdeditTextFormatter implements Formatter {
        public Diagram format(ActivationList activationlist)
                throws FormatException {
            return new SdeditTextDiagram(activationlist);
        }
    }

    private static class SVGFormatter implements Formatter {
        public Diagram format(ActivationList activationList)
                throws FormatException {
            Diagram diagram;
            try {
                SVGGenerator svgGenerator = new SVGGenerator();
                diagram = svgGenerator.generate(activationList);
            } catch (ParserConfigurationException e) {
                throw new FormatException("Failed to create diagram", e);
            }
            return diagram;
        }
    }

    private static class ArgoUMLFormatter implements Formatter {
        public Diagram format(ActivationList activationList)
                throws FormatException {
            Diagram diagram;
            try {
                ArgoUMLGenerator generator = new ArgoUMLGenerator();
                diagram = generator.generate(activationList);
            } catch (ParserConfigurationException e) {
                throw new FormatException("Failed to create diagram", e);
            }
            return diagram;
        }
    }
}
