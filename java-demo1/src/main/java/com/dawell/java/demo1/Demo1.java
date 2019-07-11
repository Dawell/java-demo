package com.dawell.java.demo1;

import ch.qos.logback.core.net.SyslogOutputStream;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ResolvableType;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveAction;
import java.util.concurrent.RecursiveTask;
import java.util.function.Consumer;

@Slf4j
public class Demo1 {

    private Map<String, Integer> myMap;

    public static void main(String[] args) throws NoSuchFieldException, InterruptedException, IllegalAccessException, CloneNotSupportedException {

        testSort();

        testForkJoin();

        testString();

        execute(System.out::println);

        testResolvableType();

        testStackTrace();

        testList();

        testlambda();

        testInnerString();

        TestInterface.test("123");

        print((CharSequence) "123");

        testClone();
        // 线程安全
        Collections.unmodifiableCollection(Arrays.asList(1, 2, 3, 4, 5));



    }

    private static void testClone() throws CloneNotSupportedException {
        Data data = new Data();
        data.setA("sss");
        Data data2 = data.clone();
        log.info(data2.toString());
        log.info("{}", data2 == data);
        log.info("{}", data2.a == data.a);
        Data data3 = data.clone2();
        log.info(data3.toString());
        log.info("{}", data3 == data);
        log.info("{}", data3.a == data.a);
    }

    @lombok.Data
    static class Data implements Cloneable {

        private String a;

        @Override
        public Data clone() throws CloneNotSupportedException {
            return (Data) super.clone();
        }

        public Data clone2() throws CloneNotSupportedException {
            Data clone = (Data) super.clone();
            clone.setA(new String(this.a));
            return clone;
        }
    }

    static class SnapshotData {

        private List<String> a;

        public List<String> getA() {
            return new ArrayList<>(a);
        }
    }

    private static void print(CharSequence s) {

    }

    private static void print(Serializable s) {

    }

    interface TestInterface {

        static void test(String s) {
            System.out.println(s);
        }

        default void test2() {
            System.out.println();
        }

    }

    private static void testInnerString() throws NoSuchFieldException, IllegalAccessException {
        String abc = "abc";

        Field field = String.class.getDeclaredField("value");
        field.setAccessible(true);
//        field.set(abc, "def".toCharArray());
        field.set(abc, "def".getBytes());
        log.info("abc: " + abc);

        log.info("abc");
    }

    private static void testlambda() {
        new Runnable() {
            @Override
            public void run() {

            }
        };

        new Callable<String>() {
            @Override
            public String call() throws Exception {
                return null;
            }
        };

        Action action = () -> {
        };

        Action action2 = System.out::println;
        Consumer consumer = System.out::println;
    }

    public interface Action {
        void execute();

        default void execute2() {

        }

    }

    private static void testList() {
        List<String> a = Collections.emptyList();
        List b = null;
        a = b;
        b = a;
    }
//
//    public interface Converter {
//
//    }

    public interface Converter<S, T> {

        T convert(S source);

    }

    private static void testStackTrace() {
        StackTraceElement[] sts = new Throwable().getStackTrace();
        log.info("目前方法栈深度：{}", sts.length);
        Arrays.stream(sts).forEach(st -> {
            log.info(st.getMethodName());
        });
    }

    private static void testResolvableType() throws NoSuchFieldException {
        ResolvableType t = ResolvableType.forField(Demo1.class.getDeclaredField("myMap"));
        log.info(Objects.toString(t.getSuperType()));
        log.info(Objects.toString(t.asMap()));
        log.info(Objects.toString(t.getGeneric(0).resolve()));
        log.info(Objects.toString(t.getGeneric(1).resolve()));
        log.info(Objects.toString(t.getGeneric(1)));
        log.info(Objects.toString(t.resolveGeneric(1, 0)));
    }

    public static void execute(Callback callback) {
        try {
            callback.callback("123");
        } catch (Exception e) {
            log.error("callback error!", e);
        }
    }

    interface Callback {
        void callback(String name) throws Exception;
    }

    private static void testString() {
        boolean b = "123" == new String("123");
        String s = "12" + "3";
        boolean b2 = "12" + "3" == "123";
        String c = "3";
        String e = "444";
        boolean b3 = "12" + c == "123";
        final String d = "3";
        boolean b4 = "12" + d == "123";
        String temp = "ABC" + e + 'D';
        log.info(Boolean.toString(b));
        log.info(Boolean.toString(b2));
        log.info(Boolean.toString(b3));
        log.info(Boolean.toString(b4));
        log.info(temp);
    }

    private static void testForkJoin() {
        ForkJoinPool forkJoin = new ForkJoinPool(4);

        List<String> data = Arrays.asList("1", "2", "3", "4", "5", "6", "7", "8", "9");

        RecursiveAction action = new MyRecursiveAction(data);

        forkJoin.invoke(action);

        RecursiveTask task = new MyRecursiveTask(data);

        System.out.println(forkJoin.invoke(task));
    }

    static class MyRecursiveAction extends RecursiveAction {

        private final List<String> data;

        public MyRecursiveAction(List<String> data) {
            this.data = data;
        }

        @Override
        protected void compute() {

            int size = data.size();
            if (size > 1) {
                RecursiveAction action = new MyRecursiveAction(data.subList(0, size / 2));
                action.fork();
                RecursiveAction action2 = new MyRecursiveAction(data.subList(size / 2, size));
                action2.fork();
                return;
            }
            System.out.println("Action: " + Thread.currentThread().getName() + " " + data);

        }
    }

    static class MyRecursiveTask extends RecursiveTask<String> {

        private final List<String> data;

        public MyRecursiveTask(List<String> data) {
            this.data = data;
        }

        @Override
        protected String compute() {

            int size = data.size();
            if (size > 1) {
                RecursiveTask<String> action = new MyRecursiveTask(data.subList(0, size / 2));
                action.fork();
                RecursiveTask<String> action2 = new MyRecursiveTask(data.subList(size / 2, size));
                action2.fork();
                return action.join() + "," + action2.join();
            }
            System.out.println("Task: " + Thread.currentThread().getName() + " " + data);
            return String.join(",", data);
        }
    }


    /**
     * -Djava.util.Arrays.useLegacyMergeSort=true
     * 采用 legacyMergeSort
     * 默认采用 ComparableTimSort
     */
    private static void testSort() {
        Object[] a = {"3", "1", "2"};
        Arrays.sort(a);
        System.out.println(Arrays.toString(a));
    }

}
