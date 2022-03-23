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

import java.util.List;

import com.sun.jdi.AbsentInformationException;
import com.sun.jdi.ClassNotLoadedException;
import com.sun.jdi.LocalVariable;
import com.sun.jdi.Location;
import com.sun.jdi.Method;
import com.sun.jdi.ReferenceType;
import com.sun.jdi.Type;
import com.sun.jdi.VirtualMachine;

public class TestMethodImpl implements Method {
    private String name;
    private boolean isConstructor;

    public TestMethodImpl(String name) {
        this(name, false);
    }

    public TestMethodImpl(String name, boolean isConstructor) {
        this.name = name;
        this.isConstructor = isConstructor;
    }

    public List<Location> allLineLocations() throws AbsentInformationException {
        return null;
    }

    public List<Location> allLineLocations(String arg0, String arg1)
            throws AbsentInformationException {
        return null;
    }

    public List<String> argumentTypeNames() {
        return null;
    }

    public List<Type> argumentTypes() throws ClassNotLoadedException {
        return null;
    }

    public List<LocalVariable> arguments() throws AbsentInformationException {
        return null;
    }

    public byte[] bytecodes() {
        return null;
    }

    public boolean isAbstract() {
        return false;
    }

    public boolean isBridge() {
        return false;
    }

    public boolean isConstructor() {
        return isConstructor;
    }

    public boolean isNative() {
        return false;
    }

    public boolean isObsolete() {
        return false;
    }

    public boolean isStaticInitializer() {
        return false;
    }

    public boolean isSynchronized() {
        return false;
    }

    public boolean isVarArgs() {
        return false;
    }

    public Location location() {
        return null;
    }

    public Location locationOfCodeIndex(long arg0) {
        return null;
    }

    public List<Location> locationsOfLine(int arg0)
            throws AbsentInformationException {
        return null;
    }

    public List<Location> locationsOfLine(String arg0, String arg1, int arg2)
            throws AbsentInformationException {
        return null;
    }

    public Type returnType() throws ClassNotLoadedException {
        return null;
    }

    public String returnTypeName() {
        return null;
    }

    public List<LocalVariable> variables() throws AbsentInformationException {
        return null;
    }

    public List<LocalVariable> variablesByName(String arg0)
            throws AbsentInformationException {
        return null;
    }

    public ReferenceType declaringType() {
        return null;
    }

    public String genericSignature() {
        return null;
    }

    public boolean isFinal() {
        return false;
    }

    public boolean isStatic() {
        return false;
    }

    public boolean isSynthetic() {
        return false;
    }

    public String name() {
        return name;
    }

    public String signature() {
        return null;
    }

    public VirtualMachine virtualMachine() {
        return null;
    }

    public boolean isPackagePrivate() {
        return false;
    }

    public boolean isPrivate() {
        return false;
    }

    public boolean isProtected() {
        return false;
    }

    public boolean isPublic() {
        return false;
    }

    public int modifiers() {
        return 0;
    }

    public int compareTo(Method o) {
        return 0;
    }

}
