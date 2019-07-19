package com.dawell.java.demo1;

import org.springframework.core.ResolvableType;

import java.io.Serializable;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Field;
import java.lang.reflect.Proxy;
import java.lang.reflect.Type;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveAction;
import java.util.concurrent.RecursiveTask;
import java.util.function.Consumer;
import java.util.stream.Stream;

@Demo1.DemoAnnotation(value = "test")
//@Slf4j
public class Demo1 {

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.TYPE)
    public @interface DemoAnnotation {
        String value() default "";
    }

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

        testGeneric();

        testReturn();

        testAnnotation();

        Module module = Demo1.class.getModule();
        // auto module 返回null
        System.out.println(module.getName());
        System.out.println(module.getDescriptor());

    }

    private static void testAnnotation() {
        LinkedList<Integer> list = Stream.of(1, 2, 3, 4, 5)
                .collect(LinkedList::new, List::add, List::addAll);

        DemoAnnotation annotation = Demo1.class.getDeclaredAnnotation(DemoAnnotation.class);
        System.out.println(annotation.getClass().toString());
        System.out.println((Proxy.getInvocationHandler(annotation).toString()));
    }

    @FunctionalInterface
    public interface Function {

        void execute();

    }

    private static void addElements(Collection<String> collection, String... elements) {

    }

    private static void addElements2(Collection<String> collection, String elemetn, String... others) {

    }

    private static Collection<Integer> testReturn() {
        // 线程安全
        Collections.unmodifiableCollection(Arrays.asList(1, 2, 3, 4, 5));

        List<Integer> numbers = Arrays.asList(1, 2, 3, 4, 5);
        return new ArrayList<>(numbers);

    }

    private static void testGeneric() {
        Type intType = int.class;
        Class intClass = int.class;

        Container<StringBuilder> container2 = new Container<>(new StringBuilder());
        Container<StringBuilder> container = new Container("123");
//        Container<StringBuilder> container2 = new Container<>("");
//        StringBuilder element = container.getElement(); // ClassCastException
        container.setElement(new StringBuilder());

        List<String> a = new ArrayList<>();
        add(a, "123");
        add2(a, "456");
        foreach(a, System.out::println);
        Set<String> b = new HashSet<>();
        add(b, "123");
        add2(b, "456");
        foreach(b, Demo1::print2);
        foreach(b, Demo1::print3);

        String aStr = a.get(0);
        System.out.println(aStr);
    }


    static <E extends CharSequence> Collection<E> add(Collection<E> target, E e) {
        target.add(e);
        return target;
    }

    static <C extends Collection<E>, E extends CharSequence> C add2(C target, E e) {
        target.add(e);
        return target;
    }

//    static <C extends Collection<E>, E extends CharSequence> C add3(C target, CharSequence e) {
//        target.add(e);
//        return target;
//    }

    static <C extends Iterable<E>, E extends CharSequence> void foreach(C target, Consumer<E> c) {
        for (E e : target) {
            c.accept(e);
        }
    }

    static <T extends CharSequence> void print2(T o) {
        System.out.println(o);
    }

    private static void print3(CharSequence o) {
        System.out.println(o);
    }

    static class Container<T extends CharSequence> {
        private T element;

        public Container(T element) {
            this.element = element;
        }

        public T getElement() {
            return element;
        }

        public void setElement(T element) {
            this.element = element;
        }
    }

    static final class Counting {

        public static Counting ONE = new Counting();
        public static Counting TWO = new Counting();
        public static Counting THREE = new Counting();

        private Counting() {

        }

    }

    enum Counting2 {
        ONE, TWO, THREE
    }

    private static void testClone() throws CloneNotSupportedException {
        Data data = new Data();
        data.setA("sss");
        Data data2 = data.clone();
        System.out.println(data2.toString());
        System.out.println(data2 == data);
        System.out.println(data2.a == data.a);
        Data data3 = data.clone2();
        System.out.println(data3.toString());
        System.out.println(data3 == data);
        System.out.println(data3.a == data.a);
    }

    //    @lombok.Data
    static class Data implements Cloneable {

        private String a;

        public String getA() {
            return a;
        }

        public void setA(String a) {
            this.a = a;
        }

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
        System.out.println("abc: " + abc);

        System.out.println("abc");
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
        System.out.println("目前方法栈深度：" + sts.length);
        Arrays.stream(sts).forEach(st -> {
            System.out.println(st.getMethodName());
        });
    }

    private static void testResolvableType() throws NoSuchFieldException {
        ResolvableType t = ResolvableType.forField(Demo1.class.getDeclaredField("myMap"));
        System.out.println(Objects.toString(t.getSuperType()));
        System.out.println(Objects.toString(t.asMap()));
        System.out.println(Objects.toString(t.getGeneric(0).resolve()));
        System.out.println(Objects.toString(t.getGeneric(1).resolve()));
        System.out.println(Objects.toString(t.getGeneric(1)));
        System.out.println(Objects.toString(t.resolveGeneric(1, 0)));
    }

    public static void execute(Callback callback) {
        try {
            callback.callback("123");
        } catch (Exception e) {
//            log.error("callback error!", e);
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
        System.out.println(Boolean.toString(b));
        System.out.println(Boolean.toString(b2));
        System.out.println(Boolean.toString(b3));
        System.out.println(Boolean.toString(b4));
        System.out.println(temp);
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
