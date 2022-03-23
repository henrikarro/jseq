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

package th.co.edge.jseq.scenarios;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

public class HelloWorldScenario extends TestCase {
    public HelloWorldScenario(String name) {
        super(name);
    }

    public static Test suite() {
        TestSuite suite = new TestSuite(HelloWorldScenario.class);
        return suite;
    }

    public static void main(String[] args) {
        HelloWorldScenario scenario = new HelloWorldScenario("HelloWorld");
        scenario.test();
    }

    public void test() {
        HelloWorld helloWorld = new HelloWorld();
        helloWorld.hello();
    }
}

class HelloWorld {
    Foo foo;

    public HelloWorld() {
        this.foo = new Foo(this, 2);
    }

    public void hello() {
        print("Hello, World!");
        Thread thread1 = new Thread(new ThreadOne());
        thread1.start();
        Thread thread2 = new Thread(new ThreadTwo());
        thread2.start();
        try {
            thread1.join();
            thread2.join();
        } catch (InterruptedException e) {
        }
        ExceptionTest1 exceptionTest1 = new ExceptionTest1();
        exceptionTest1.test();
    }

    public void print(String s) {
        try {
            int x = 1 + 2;
            for (int i = 0; i < 3; i++) {
                foo.foo();
            }
            System.out.println(s + x);
        } catch (Exception e) {
            System.out.println(e);
        }
        bar();
    }

    public void bar() {
        System.out.println("So, there");
    }

    private static class ThreadOne implements Runnable {
        public void run() {
            for (int i = 0; i < 10; i++) {
                t1();
            }
        }

        private void t1() {
            System.out.println("t1");
            Thread.yield();
        }
    }

    private static class ThreadTwo implements Runnable {
        public void run() {
            for (int i = 0; i < 10; i++) {
                t2();
            }
        }

        private void t2() {
            System.out.println("t2");
            Thread.yield();
        }
    }

    private static class ExceptionTest1 {
        public void test() {
            ExceptionTest2 exceptionTest2 = new ExceptionTest2();
            try {
                exceptionTest2.test();
            } catch (Exception e) {
            }
            print();
        }

        public void print() {
            System.out.println("Exception test ready");
        }
    }

    private static class ExceptionTest2 {
        public void test() {
            ExceptionTest3 exceptionTest3 = new ExceptionTest3();
            exceptionTest3.test();
        }
    }

    private static class ExceptionTest3 {
        public void test() {
            throw new RuntimeException("foo");
        }
    }
}
