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

package hello;

public class HelloJSeq {
    private Foo foo;

    public HelloJSeq() {
        this.foo = new Foo(this, 2);
    }

    public static void main(String[] args) {
        HelloJSeq hello = new HelloJSeq();
        hello.hello();
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
        ExceptionTest exceptionTest = new ExceptionTest();
        exceptionTest.test();
    }

    public void print(String s) {
        try {
            int x = 1 + 2;
            for (int i = 0; i < x; i++) {
                foo.foo();
            }
            System.out.println(s);
        } catch (Exception e) {
            System.out.println(e);
        }
        bar();
    }

    public void bar() {
        System.out.println("So, there");
    }

    public class Foo {
        HelloJSeq hello;
        int n;

        public Foo(HelloJSeq hello, int n) {
            this.hello = hello;
            this.n = n;
            hello.bar();
        }

        public void foo() {
            for (int i = 0; i < n; i++) {
                hello.bar();
            }
        }
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

    private static class ExceptionTest {
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
