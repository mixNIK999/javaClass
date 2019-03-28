package me.nikolyukin;

import org.jetbrains.annotations.NotNull;

import java.util.*;

public class PriorityQueue<E> extends AbstractQueue<E> {
    private int size = 0;
    private TreeMap<E, Queue<E>> tree;

    public PriorityQueue() {
        tree = new TreeMap<>();
    }

    public PriorityQueue(Comparator<? super E> comparator) {
        tree = new TreeMap<>(comparator);
    }

    private class MyIterator implements Iterator {
        Iterator<Map.Entry<E, Queue<E>>> treeIterator;
        Iterator<E> queueIterator;

        public MyIterator(@NotNull Iterator<Map.Entry<E, Queue<E>>> treeIterator) {
            this.treeIterator = treeIterator;
        }

        @Override
        public boolean hasNext() {
            return treeIterator.hasNext() || (queueIterator != null && queueIterator.hasNext());
        }

        @Override
        public E next() {
            if(queueIterator == null || !queueIterator.hasNext()) {
                queueIterator = treeIterator.next().getValue().iterator();
            }
            return queueIterator.next();
        }
    }

    @Override
    public Iterator<E> iterator() {
        return new MyIterator(tree.entrySet().iterator());
    }

    @Override
    public int size() {
        return size;
    }

    @Override
    public boolean offer(E e) {
        if (e == null) {
            return false;
        }
        if (!tree.containsKey(e)) {
            tree.put(e, new LinkedList<>());
        }
        size++;
        return tree.get(e).offer(e);
    }

    @Override
    public E poll() {
        if (tree.isEmpty()) {
            return null;
        }
        var queue = tree.firstEntry().getValue();
        E tmp = queue.poll();
        if (queue.isEmpty()) {
            tree.pollFirstEntry();
        }
        size--;
        return tmp;
    }

    @Override
    public E peek() {
        if (tree.isEmpty()) {
            return null;
        }
        return tree.firstEntry().getValue().peek();
    }
}
