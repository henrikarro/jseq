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
 * A <code>Filter</code> that only accepts <code>Activation</code>s with
 * method names that do not match a given exclude pattern.
 */
public class ClassExclusionFilter implements ActivationList.Filter {
    private String excludePattern;

    /**
     * Creates a new <code>ClassExclusionFilter</code> with the given method
     * name exclude pattern. If the pattern starts or ends with "*", all method
     * names with the correct suffix or prefix, respectively, are accepted.
     *
     * <p>
     * For example, the pattern "foo.Bar.baz" would only match a method named
     * "baz" in the class foo.bar, "*.baz" would match any method named "baz" in
     * any class, and "foo.Bar.*" would match all method in the class foo.Bar.
     *
     * @param excludePattern
     *            the exclude pattern to use for this filter instance, possibly
     *            starting or ending with the wildcard "*"
     */
    public ClassExclusionFilter(String excludePattern) {
        this.excludePattern = excludePattern;
    }

    /**
     * Returns <code>true</code> if and only if the given
     * <code>Activation</code> represents a method call where the class name
     * plus method name matches the exclude pattern used by this filter.
     *
     * @param activation
     *            the <code>Activation</code> to check for matching method
     *            name
     *
     * @return <code>true</code> if <code>activation</code> represents a
     *         method call that matches the exclude pattern used by this filter
     */
    public boolean accept(Activation activation) {
        String fullMethodName =
                activation.getClassName() + "." + activation.getMethod().name();
        boolean accepted = true;
        if (excludePattern.endsWith("*")) {
            String prefix =
                    excludePattern.substring(0, excludePattern.length() - 1);
            accepted = !fullMethodName.startsWith(prefix);
        } else if (excludePattern.startsWith("*")) {
            String suffix = excludePattern.substring(1);
            accepted = !fullMethodName.endsWith(suffix);
        } else {
            accepted = !fullMethodName.equals(excludePattern);
        }
        return accepted;
    }
}
