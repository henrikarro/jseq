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

import com.sun.jdi.Method;

import th.co.edge.jseq.ActivationList.Filter;

/**
 * A <code>Filter</code> that accepts all methods, except constructors that
 * are either synthetic (automatically created by the compiler), or are calls to
 * <code>super</code>.
 *
 * <p>
 * The reason that this filter was created was to remove "unnecessary" calls to
 * <code>&lt;init&gt;</code> from the sequence diagrams. Since the sequence
 * diagrams generated by JSeq are class-based, not instance-based (i.e., each
 * lifeline belongs to a class, not to a specific instance), and since the class
 * of the instance being called is used (not the declaring class), calls to
 * synthetic constructors or to the super constructor would be represented as
 * one or several calls to <code>&lt;init&gt;</code> back to the same class.
 * This was deemed more confusing than helpful.
 *
 * <p>
 * In order to determine if a call is a call to a super-class constructor, this
 * filter needs access to a <code>ClassLoader</code>. When running JSeq
 * stand-alone, this is not a problem, but when attaching to a running process,
 * JSeq should have the same classpath as that process. If not, the only effect
 * is that the "unnecessary" constructor calls are included in the diagram, so
 * failure to set up the correct classpath when attaching to a process is far
 * from catastrophic.
 *
 * <p>
 * This filter was created after Jacek Ratzinger supplied his patch (<a
 * href="https://sourceforge.net/tracker/index.php?func=detail&aid=2027500&group_id=230519&atid=1080393"
 * target="new">SourceForge issue 2027500</a>) since that change made calls to
 * the super-class constructor look as calls back to the same class. In the
 * process it also turned out that private nested classes use a synthetic
 * constructor that need not be shown in the generated sequence diagrams.
 */
public class ConstructorFilter implements Filter {
    private ClassLoader classLoader;

    /**
     * Creates a new <code>ConstructorFilter</code> that uses the given
     * <code>ClassLoader</code> to determine if a call is to a super-class
     * constructor.
     *
     * @param classLoader
     *            a <code>ClassLoader</code> that should match that of the
     *            process being traced
     */
    public ConstructorFilter(ClassLoader classLoader) {
        this.classLoader = classLoader;
    }

    /**
     * Returns <code>true</code> for all <code>Activation</code>s, except
     * calls to synthetic constructors and calls to super-class constructors.
     *
     * @param activation
     *            the <code>Activation</code> to check
     *
     * @return <code>false</code> if <code>activation</code> represents a
     *         call to a synthetic constructor or a call to a super-class
     *         constructor, <code>true</code> otherwise
     */
    public boolean accept(Activation activation) {
        boolean accepted = true;
        Method method = activation.getMethod();
        if (method.isConstructor() && activation.getParent() != null) {
            Activation parentActivation = activation.getParent();
            Method parentMethod = parentActivation.getMethod();
            if (parentMethod != null && parentMethod.isConstructor()) {
                if (parentMethod.isSynthetic()) {
                    accepted = false;
                } else if (isDeclaredInSuperclass(method, parentMethod)) {
                    accepted = false;
                }
            }
        }
        return accepted;
    }

    /**
     * Returns <code>true</code> if the method <code>m1</code> is declared
     * in a superclass to the class that declares the method <code>m2</code>.
     *
     * @param m1
     *            method from superclass?
     * @param m2
     *            method from subclass?
     *
     * @return <code>true</code> if <code>m1</code> is declared in a
     *         superclass to the class where <code>m2</code> is declared
     */
    private boolean isDeclaredInSuperclass(Method m1, Method m2) {
        boolean fromSuperclass = false;
        try {
            Class<?> c1 = classLoader.loadClass(m1.declaringType().name());
            Class<?> c2 = classLoader.loadClass(m2.declaringType().name());
            if (c1.isAssignableFrom(c2) && !c1.equals(c2)) {
                fromSuperclass = true;
            }
        } catch (Exception e) {
            System.err.println(e);
        }
        return fromSuperclass;
    }
}