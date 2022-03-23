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
 * A <code>Filter</code> that accepts only methods with a given fully
 * qualified name.
 */
public class MethodFilter implements ActivationList.Filter {
    private final String className;
    private final String methodName;

    /**
     * Creates a new <code>MethodFilter</code> that only accepts methods with
     * the given fully qualified name. Only exact matches are accepted, there is
     * no wild-card expansion.
     *
     * @param method
     *            the fully qualified method name to accept
     */
    public MethodFilter(String method) {
        this.className = method.substring(0, method.lastIndexOf("."));
        this.methodName = method.substring(method.lastIndexOf(".") + 1);
    }

    /**
     * Returns <code>true</code> if and only if the given activation is a call
     * to a method with the fully qualified name given when creating this
     * <code>MethodFilter</code>.
     *
     * @param activation
     *            the <code>Activation</code> to check
     *
     * @return <code>true</code> if <code>activation</code> represents a
     *         call to a method with the name associated with this
     *         <code>MethodFilter</code>, <code>false</code> otherwise
     */
    public boolean accept(Activation activation) {
        return activation.getClassName().equals(className)
                && activation.getMethod().name().equals(methodName);
    }

    /**
     * Returns the class name associated with this <code>MethodFilter</code>.
     *
     * @return the class name associated with this <code>MethodFilter</code>
     */
    public String getClassName() {
        return className;
    }

    /**
     * Returns the unqualified method name associated with this
     * <code>MethodFilter</code>.
     *
     * @return the unqualified method name associated with this
     *         <code>MethodFilter</code>
     */
    public String getMethodName() {
        return methodName;
    }
}
