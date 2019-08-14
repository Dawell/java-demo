package com.dawell.java.demo1;

import org.springframework.core.ResolvableType;
import org.springframework.objenesis.ObjenesisException;
import org.springframework.objenesis.instantiator.util.UnsafeUtils;
import org.springframework.util.StopWatch;
import sun.misc.Unsafe;

import java.io.*;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.management.*;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;
import java.lang.reflect.Field;
import java.lang.reflect.Proxy;
import java.lang.reflect.Type;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.security.PrivilegedExceptionAction;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Consumer;
import java.util.stream.Stream;

@JDKDemo.DemoAnnotation(value = "test")
//@Slf4j
public class JDKDemo {

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.TYPE)
    public @interface DemoAnnotation {
        String value() default "";
    }

    private Map<String, Integer> myMap;

    public static void main(String[] args) throws Exception {

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

        testModule();

        testMapGet();

        testValueOf();

        testDeepCloneInSerialization();

        testSystemCopy();

        testCheckedList();

        testRefQueue();

        testListRemove();

        testThread();

        testJMX();

        testFastjson();

        testSynchronized();

        testDeadLock();

        testProduceAndConsume();

        testLock();

        testUnsafe();

        testSynchronousQueue();

        testFlow();

    }

    private static void testFlow() throws InterruptedException {
        CompletableFuture.supplyAsync(() -> 1)
                .thenApply(String::valueOf)
                .completeExceptionally(new RuntimeException());

        SubmissionPublisher<String> sp = new SubmissionPublisher<>();
        sp.subscribe(new Flow.Subscriber<>() {
            private Flow.Subscription subscription;

            @Override
            public void onSubscribe(Flow.Subscription subscription) {
                System.out.println("订阅");
                subscription.request(3);
                this.subscription = subscription;
            }

            @Override
            public void onNext(String item) {
                if ("exit".equals(item)) {
                    subscription.cancel();
                    return;
                }
                System.out.println("处理" + item);
            }

            @Override
            public void onError(Throwable throwable) {
                System.out.println("异常");
            }

            @Override
            public void onComplete() {
                System.out.println("完成");
            }
        });
        sp.submit("Hello");
        sp.submit("Hello2");
        sp.submit("exit");
        sp.submit("hello3");

        Thread.sleep(100);
    }


    private static void testSynchronousQueue() throws InterruptedException {
        SynchronousQueue<Integer> queue = new SynchronousQueue<>();
        new Thread(() -> {
            try {
                queue.put(1);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();
        System.out.println(queue.size());
        System.out.println(queue.take());
    }

    private static void testLock() {
        ReentrantLock lock = new ReentrantLock();

        try {
            lock.lock();
            // ...
        } finally {
            lock.unlock();
        }

        ReentrantLock lock2 = new ReentrantLock();

        try {
            lock2.tryLock(3, TimeUnit.SECONDS);
            // ...
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } finally {
            lock2.unlock();
        }

        ReentrantLock lock3 = new ReentrantLock();
        System.out.println("重进入次数： " + lock3.getHoldCount());
        lockCount(lock3, 10);
    }

    private static void testUnsafe() throws NoSuchFieldException, IllegalAccessException, java.security.PrivilegedActionException {
        AtomicBoolean ab = new AtomicBoolean();
        ab.set(true);
        ab.get();

        UnsafeUtils.getUnsafe();
        try {
            Unsafe unsafe = Unsafe.getUnsafe();
        } catch (Exception e) {
            System.out.println(e.getClass().getName());
        }

        PrivilegedExceptionAction<Unsafe> action = new PrivilegedExceptionAction<Unsafe>() {
            @Override
            public Unsafe run() throws Exception {
                Field f = Unsafe.class.getDeclaredField("theUnsafe");
                f.setAccessible(true);
                return (Unsafe) f.get((Object) null);
            }
        };
        Unsafe unsafe = AccessController.doPrivileged(action);

        AtomicLong al = new AtomicLong();
        al.set(1);
        al.get();
    }

    private static void lockCount(ReentrantLock lock, int times) {
        if (times < 1) {
            return;
        }
        try {
            lock.lock();
            System.out.println("重进入次数： " + lock.getHoldCount());
            lockCount(lock, times - 1);
        } finally {
            lock.unlock();
        }

    }

    private static void testProduceAndConsume() throws InterruptedException {
        ExecutorService executorService = Executors.newFixedThreadPool(2);

        Runner runner = new Runner();

        Future<?> producerFuture = executorService.submit(() -> {
            runner.produce();
        });
        Future<?> consumerFuture = executorService.submit(() -> {
            runner.consume();
        });

//        producerFuture.get();
//        consumerFuture.get();

        Thread.sleep(1000);
        runner.isRun = false;

        executorService.shutdown();
    }

    public static class Runner {

        private List<Integer> data = new LinkedList<>();

        private static final int MAX_SIZE = 5;

        public volatile boolean isRun = true;

        public void produce() {
            while (isRun) {

                synchronized (this) {
                    while (data.size() >= MAX_SIZE) {
                        try {
                            wait();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                    int i = new Random().nextInt(100);
                    data.add(i);
                    System.out.println("生产中：" + i);
                    notify();
                }

            }
        }

        public void consume() {
            while (isRun) {

                synchronized (this) {
                    while (data.isEmpty()) {
                        try {
                            wait();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                    Integer i = data.remove(0);
                    System.out.println("消费中：" + i);
                    notify();
                }

            }
        }


    }


    private static void testDeadLock() {
        Object m1 = new Object();
        Object m2 = new Object();

        new Thread(() -> {

            synchronized (m1) {
                System.out.println(Thread.currentThread().getId() + " hold m1");
                synchronized (m2) {
                    System.out.println(Thread.currentThread().getId() + " hold m2");
                }
            }

        }).start();

        new Thread(() -> {

//            synchronized (m2){
//                System.out.println(Thread.currentThread().getId()+" hold m2");
//                synchronized (m1){
//                    System.out.println(Thread.currentThread().getId()+" hold m1");
//                }
//            }

        }).start();

//        Thread.onSpinWait();
    }

    @Override
    protected void finalize() throws Throwable {
        System.out.println("Thread execute finalize!");
    }

    private static void testSynchronized() {
        Object obj = new Object();
        synchronized (obj) {
            echo("Hello");
            System.out.println("holdsLock: " + Thread.holdsLock(obj));
        }
        System.out.println("holdsLock: " + Thread.holdsLock(obj));

        int count = 1000000;
        List list = new ArrayList();
        List vector = Collections.synchronizedList(new ArrayList());

        testSpeed(count, list);
        testSpeed(count, vector);

        System.gc();

        testSpeed(count, list);
        testSpeed(count, vector);
    }

    private static void testSpeed(int count, List list) {
        StopWatch sw = new StopWatch();
        sw.start();
        for (int i = 0; i < count; i++) {
            list.add(i);
        }
        sw.stop();
        System.out.println(list.getClass().getName() + " cost: " + sw.getTotalTimeMillis());
    }

    private synchronized static void echo(String msg) {
        System.out.println(msg);
    }

    private static void testJMX() {
        long pid = ProcessHandle.current().pid();
        System.out.println(pid);

        RuntimeMXBean runtimeMXBean = ManagementFactory.getRuntimeMXBean();
        System.out.println(runtimeMXBean.getName());
        System.out.println(runtimeMXBean.getName().substring(0, runtimeMXBean.getName().indexOf("@")));

        System.out.println(runtimeMXBean.getPid());

        LocalDateTime localDateTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(runtimeMXBean.getStartTime()), ZoneId.systemDefault());
        System.out.println(runtimeMXBean.getStartTime());
        System.out.println(localDateTime);
        System.out.println(runtimeMXBean.getUptime());


        ThreadMXBean threadMXBean = ManagementFactory.getThreadMXBean();
        System.out.println(threadMXBean.getThreadCount());
        MemoryMXBean memoryMXBean = ManagementFactory.getMemoryMXBean();
        System.out.println(memoryMXBean.getHeapMemoryUsage());
        OperatingSystemMXBean osMXBean = ManagementFactory.getOperatingSystemMXBean();
        System.out.println(osMXBean.getName());

//        System.exit(9);
    }

    private static void testFastjson() {
    /* String payload = "{\"@type\":\"com.sun.rowset.JdbcRowSetImpl\",\"dataSourceName\":\"rmi://localhost:1099/Exploit\"," +
             " \"autoCommit\":true}";
     JSON.parse(payload);*/
    }

    private static void testThread() throws InterruptedException {
        StackWalker stackWalker = StackWalker.getInstance();
        stackWalker.forEach(System.out::println);

        Thread thread = new Thread(() -> {
            if (Thread.currentThread().isInterrupted()) {
                System.out.println("被中止");
                return;
            }
            System.out.println("执行中");
        });
        thread.start();
        thread.interrupt();
        thread.join();
    }

    private static void testListRemove() {
        List<Integer> list = new ArrayList<>(List.of(1, 2, 3));

        Iterator<Integer> iterator = list.iterator();
        while (iterator.hasNext()) {
            iterator.next();
            iterator.remove();
        }
        System.out.println(list);

        List<Integer> list2 = new ArrayList<>(List.of(1, 2, 3));

        // 注意size不要写到for里面，因为删除的时候会变化
        int size = list2.size();
        for (int i = 0; i < size; i++) {
            list2.remove(0);
        }
        System.out.println("final: " + list2);

        List<Integer> list3 = new ArrayList<>(List.of(1, 2, 3));
        try {
            for (Integer integer : list3) {
                list3.remove(integer);
            }
        } catch (ConcurrentModificationException e) {
            System.out.println("remove exception: " + e.getClass().getSimpleName());
        }


        List<Integer> list4 = new ArrayList<>(List.of(1, 2, 3));

        Iterator<Integer> iterator2 = list4.iterator();
        while (iterator2.hasNext()) {
//            list4.add(4);// ConcurrentModificationException
//            list4.remove(0);// ConcurrentModificationException
//            iterator2.remove();// IllegalStateException
            iterator2.next();
//            list4.add(4);// 第二次循环时ConcurrentModificationException
            iterator2.remove();
        }
    }

    private static void testRefQueue() {
        ReferenceQueue queue = new ReferenceQueue();
        WeakReference<String> ref = new WeakReference<>("123", queue);
        System.out.println(ref.get());
        ref.enqueue();
        System.out.println(queue.poll());
    }

    private static void testCheckedList() {
        List<Integer> list = new ArrayList<>(List.of(1, 2, 3));
        List list2 = list;
        list2.add("A");
        System.out.println(list2);

        list = Collections.checkedList(list, Integer.class);
        list2 = list;
        try {
            list2.add("A");
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    private static void testSystemCopy() {
        InnerObj[] array1 = {new InnerObj(1), new InnerObj(2), new InnerObj(3), new InnerObj(4), new InnerObj(5)};
        InnerObj[] array2 = Arrays.copyOf(array1, 5);
        for (int i = 0; i < array1.length; i++) {
            System.out.println(array1[i] == array2[i]);
        }
    }

    static class InnerObj {
        Integer a;

        public InnerObj(Integer a) {
            this.a = a;
        }
    }

    private static void testDeepCloneInSerialization() throws IOException, ClassNotFoundException {
        List<String> list = new ArrayList<>();
        list.add("123");
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(list);
        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        ObjectInputStream ois = new ObjectInputStream(bais);
        List<String> clone = (List<String>) ois.readObject();
        System.out.println(clone);
    }

    private static void testMapGet() {
        System.out.println("---------------");
        Map<Integer, String> map = Map.of(1000, "123");
        System.out.println(map.get(1000));
        System.out.println(map.get(new Key(1000)));
    }

    private static void testValueOf() {
        Integer a = (Integer) 1000;
        Integer b = (Integer) 1000;
        System.out.println(a == b);
        Integer a1 = (Integer) 1;
        Integer b2 = (Integer) 1;
        System.out.println(a1 == b2);
    }

    private static class Key {
        private final int value;

        private Key(int value) {
            this.value = value;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o instanceof Integer) {
                return this.value == ((Integer) o).intValue();
            }
            return false;
        }

        @Override
        public int hashCode() {
            return value;
        }
    }

    private static void testModule() {
        Module module = JDKDemo.class.getModule();
        // auto module 返回null
        System.out.println(module.getName());
        System.out.println(module.getDescriptor());
    }

    private static void testAnnotation() {
        LinkedList<Integer> list = Stream.of(1, 2, 3, 4, 5)
                .collect(LinkedList::new, List::add, List::addAll);

        DemoAnnotation annotation = JDKDemo.class.getDeclaredAnnotation(DemoAnnotation.class);
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
        foreach(b, JDKDemo::print2);
        foreach(b, JDKDemo::print3);

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
        ResolvableType t = ResolvableType.forField(JDKDemo.class.getDeclaredField("myMap"));
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
