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

package simple;

public class Foo {
    public static void main(String[] args) {
        Foo foo = new Foo();
        foo.doIt();
    }

    public void doIt() {
        Bar bar = new Bar();
        Baz baz = new Baz(bar);
        baz.grunk();
        Sub sub1 = new Sub();
        sub1.f();
        Sub sub2 = new Sub();
        sub2.f();
        Interface i = new Implementation();
        i.g();
    }
}

class Super {
    public void f() {
    }
}

class Sub extends Super {
}

interface Interface {
    public void g();
}

class Implementation implements Interface {
    public void g() {
    }
}
