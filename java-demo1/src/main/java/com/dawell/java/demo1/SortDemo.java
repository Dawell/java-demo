package com.dawell.java.demo1;

import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

public class SortDemo {

    public static void main(String[] args) {
//        Integer[] source = List.of(5,6).toArray(new Integer[]{});
//        Integer[] source = List.of(6, 5).toArray(new Integer[]{});
        Integer[] source = List.of(4, 3, 1, 6, 5, 2).toArray(new Integer[]{});
//        Integer[] source = List.of(10, 7, 2, 4, 7, 62, 3, 4, 2, 1, 8, 9, 19).toArray(new Integer[]{});
        getConsumer().accept(source);
        System.out.println("---sort---");
        new BubbleSort<Integer>().sort4Action(source).doAction(getConsumer());
        new InsertionSort().sort4Action(source).doAction(getConsumer());
        System.out.println("---quicksort---");
        new QuickSort().sort4Action(source).doAction(getConsumer());
        new QuickSort2().sort4Action(source).doAction(getConsumer());

    }

    private static <T> Consumer<T[]> getConsumer() {
        return (T[] t) -> System.out.println(Arrays.toString(t));
    }

    static class QuickSort<T extends Comparable<T>> implements Sort<T> {

        @Override
        public void sort(T[] source) {
            sort(source, 0, source.length - 1);
        }

        private void sort(T[] source, int low, int high) {
            if (low < high) {
                int index = partition(source, low, high);
                sort(source, low, index - 1);
                sort(source, index + 1, high);
            }

        }

        /**
         * 将末尾数据大小分开2组
         * [！4, 3, 1, 6, ！5, 2]，2作为end标识
         *
         * @param source
         * @param low
         * @param high
         * @return
         */
        private int partition(T[] source, int low, int high) {
            T end = source[high];
            int i = high, j = low;
            // 注意如果是i=high-1，则下面while里<0与>0，会导致2个数据时不走while循环
            System.out.println(Arrays.toString(Arrays.copyOfRange(source, low, high + 1)));
            while (i > j) {
                // 从左往右找到比标准大的，只要小的都忽略
                while (source[j].compareTo(end) < 1 && i > j) {
                    j++;
                }
                System.out.println("finalleft:" + source[j]);
                // 从右往左找到比标准小的，只要大的都忽略
                while (source[i].compareTo(end) > -1 && i > j) {
                    i--;
                }
                System.out.println("finalright:" + source[i]);

                if (i > j) {
                    swap(source, i, j);
                    getConsumer().accept(source);
                }
            }
            // 否则2个会交换

            swap(source, high, j);
            getConsumer().accept(source);

            return j;
        }
    }

    static class QuickSort2<T extends Comparable<T>> implements Sort<T> {

        @Override
        public void sort(T[] source) {
            sort(source, 0, source.length - 1);
        }

        private void sort(T[] source, int low, int high) {
            if (low < high) {
                int index = partition(source, low, high);
                sort(source, low, index - 1);
                sort(source, index + 1, high);
            }

        }

        /**
         * 将末尾数据大小分开2组
         * [！4, 3, 1, 6, ！5, 2]，2作为end标识
         *
         * @param source
         * @param low
         * @param high
         * @return
         */
        private int partition(T[] source, int low, int high) {
            T end = source[high];
            int i = high - 1, j = low;
            System.out.println(Arrays.toString(Arrays.copyOfRange(source, low, high + 1)));
            // 注意相等的时候相当于是只有2个的情况也要进行对比
            while (i >= j) {
                // 从左往右找到比标准大的，只要小的都忽略
                while (source[j].compareTo(end) < 0 && i >= j) {
                    j++;
                }
                System.out.println("finalleft:" + source[j]);

                // 注意相等的时候只执行一遍退出！
                if (i == j) {
                    break;
                }

                // 从右往左找到比标准小的，只要大的都忽略
                while (source[i].compareTo(end) > 0 && i > j) {
                    i--;
                }
                System.out.println("finalright:" + source[i]);

                if (i > j) {
                    swap(source, i, j);
                    getConsumer().accept(source);
                }

            }
            // 否则2个会交换
            swap(source, high, j);
            getConsumer().accept(source);

            return j;
        }


    }

    static class InsertionSort<T extends Comparable<T>> implements Sort<T> {

        @Override
        public void sort(T[] source) {
            for (int i = 1; i < source.length; i++) {
                T t = source[i];
                for (int j = i - 1; j >= 0; j--) {
//                    System.out.println(source[j] + " " + t);
                    if (source[j].compareTo(t) > 0) {
                        swap(source, j + 1, j);
//                        getConsumer().accept(source);
                    } else {
                        break;
                    }

                }
            }
        }

    }

    static class BubbleSort<T extends Comparable<T>> implements Sort<T> {

        @Override
        public void sort(T[] source) {
            for (int i = 0; i < source.length; i++) {
                for (int j = i + 1; j < source.length; j++) {
                    if (source[i].compareTo(source[j]) > 0) {
                        swap(source, i, j);
                    }
                }
            }
        }

    }

    interface Sort<T extends Comparable<T>> {

        void sort(T[] source);

        default Action<T> sort4Action(T[] source) {
            T[] copy = Arrays.copyOf(source, source.length);
            sort(copy);
            return o -> o.accept(copy);
        }

        default void swap(T[] source, int i, int j) {
            T temp = source[i];
            source[i] = source[j];
            source[j] = temp;
        }

        interface Action<T> {
            void doAction(Consumer<T[]> o);
        }

    }


}

