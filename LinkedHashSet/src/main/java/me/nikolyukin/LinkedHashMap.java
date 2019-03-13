package me.nikolyukin;

import org.jetbrains.annotations.NotNull;

import java.util.*;

import static java.lang.Math.abs;

public class LinkedHashMap<K, V> extends AbstractMap<K, V> {

    MyList<MyEntry<K, V>> linkList = new MyList<>();
    Object[] arr;


    public LinkedHashMap(int cp) {
        arr = new Object[cp];
    }

    @Override
    public Set<Map.Entry<K, V>> entrySet() {
        return new MySet(linkList);
    }

    @Override
    public V put(@NotNull K key, @NotNull V value) {
        MyList<Entry<K, V>> list = getList(key);
        for (var p : list) {
            if (p.getKey().equals(key)) {
                var prev = p.getValue();
                p.setValue(value);
                return prev;
            }
        }
        var newEntry = new MyEntry<>(key, value);
        list.add(newEntry);
        linkList.add(newEntry);
        list.root.prev.link = linkList.root.prev;
        return null;
    }

    private MyList<Entry<K, V>> getList(@NotNull Object key) {
        int index = abs(key.hashCode()) % arr.length;
        if (arr[index] == null) {
            arr[index] = new MyList<>();
        }
        return (MyList<Entry<K, V>>) arr[index];
    }

//    @Override
//    public V remove(Object key) {
//        var list = getList(key);
//        int index = 0;
//        for (var p : list) {
//            if (p.getKey().equals(key)) {
//                var nd = list.findIndex(index);
//                var link = list.link;
//                return list.remove(index).getValue();
//            }
//            index++;
//        }
//        return null;
//    }

    static private class MySet<E> extends AbstractSet<E> {
        private List<E> list;

        public MySet(@NotNull List<E> list) {
            this.list = list;
        }

        @Override
        public Iterator<E> iterator() {
            return list.iterator();
        }

        @Override
        public int size() {
            return list.size();
        }
    }
    static private class MyEntry<K, V> implements Map.Entry<K,V>{
        private K key;
        private V value;

        private MyEntry(@NotNull K key, @NotNull V value) {
            this.key = key;
            this.value = value;
        }

        @Override
        public K getKey() {
            return key;
        }

        @Override
        public V getValue() {
            return value;
        }

        @Override
        public V setValue(@NotNull V value) {
            var prev =this.value;
            this.value = value;
            return prev;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj instanceof LinkedHashMap.MyEntry) {
                return key.equals(((MyEntry) obj).key);
            }
            return false;
        }
    }
    static private class MyList<T> extends AbstractList<T> {
        private int size = 0;
        private Node root;

        private MyList() {
            root = new Node();
            root.next = root;
            root.prev = root;
        }

        @Override
        public boolean add(@NotNull T t) {
            size++;
            var prev = root.prev;
            var next = root;
            var newNode = new Node<T>(t);
            prev.next = newNode;
            newNode.prev = prev;
            next.prev = newNode;
            newNode.next = next;
            return true;

        }

        @Override
        public T remove(int index) {
            var curr = findIndex(index);
            var next = curr.next;
            var prev = curr.prev;
            next.prev = prev;
            prev.next = next;
            size--;
            return curr.value;
        }

        private Node<T> findIndex(int index) throws IndexOutOfBoundsException{
            if (index < 0 || index >= size) {
                throw new IndexOutOfBoundsException();
            }
            Node<T> current = root.next;
            for (int i = 0; i < index; i++) {
                current = current.next;
            }
            return current;
        }

        @Override
        public T get(int index) {
            return findIndex(index).value;
        }

        @Override
        public Iterator<T> iterator() {
            return new SetListIterator<T>(root.next);
        }

        private class SetListIterator<T> implements Iterator<T> {
            Node<T> next;
            private SetListIterator(Node<T> node) {
                next = node;
            }

            @Override
            public boolean hasNext() {
                return next.value != null;
            }

            @Override
            public T next() {
                var res = next;
                next = next.next;
                return res.value;
            }
        }

        @Override
        public int size() {
            return size;
        }

        private static class Node<T> {
            T value;
            Node<T> next;
            Node<T> prev;
            Node<T> link;
            public Node() {
                this(null);
            }

            public Node(T t) {
                value = t;
            }
        }
    }
}
