package edu.inventory.administrator.datastructures;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.function.Predicate;

public class LinkedList<T> implements Iterable<T> {
    private Node<T> head;
    private Node<T> tail;
    private int size;

    public void add(T value) {
        Node<T> node = new Node<>(value);
        if (head == null) {
            head = node;
            tail = node;
        } else {
            tail.setNext(node);
            tail = node;
        }
        size++;
    }

    public T get(int index) {
        validateIndex(index);
        Node<T> current = head;
        int currentIndex = 0;
        while (current != null) {
            if (currentIndex == index) {
                return current.getValue();
            }
            current = current.getNext();
            currentIndex++;
        }
        return null;
    }

    public T first() {
        return head == null ? null : head.getValue();
    }

    public T last() {
        return tail == null ? null : tail.getValue();
    }

    public boolean update(Predicate<T> matcher, T replacement) {
        Node<T> current = head;
        while (current != null) {
            if (matcher.test(current.getValue())) {
                current.setValue(replacement);
                return true;
            }
            current = current.getNext();
        }
        return false;
    }

    public T find(Predicate<T> matcher) {
        Node<T> current = head;
        while (current != null) {
            if (matcher.test(current.getValue())) {
                return current.getValue();
            }
            current = current.getNext();
        }
        return null;
    }

    public LinkedList<T> findAll(Predicate<T> matcher) {
        LinkedList<T> results = new LinkedList<>();
        Node<T> current = head;
        while (current != null) {
            if (matcher.test(current.getValue())) {
                results.add(current.getValue());
            }
            current = current.getNext();
        }
        return results;
    }

    public boolean remove(Predicate<T> matcher) {
        if (head == null) {
            return false;
        }

        if (matcher.test(head.getValue())) {
            head = head.getNext();
            if (head == null) {
                tail = null;
            }
            size--;
            return true;
        }

        Node<T> previous = head;
        Node<T> current = head.getNext();
        while (current != null) {
            if (matcher.test(current.getValue())) {
                previous.setNext(current.getNext());
                if (current == tail) {
                    tail = previous;
                }
                size--;
                return true;
            }
            previous = current;
            current = current.getNext();
        }
        return false;
    }

    public int size() {
        return size;
    }

    public boolean isEmpty() {
        return size == 0;
    }

    public LinkedList<T> copy() {
        LinkedList<T> copy = new LinkedList<>();
        for (T value : this) {
            copy.add(value);
        }
        return copy;
    }

    @Override
    public Iterator<T> iterator() {
        return new Iterator<>() {
            private Node<T> current = head;

            @Override
            public boolean hasNext() {
                return current != null;
            }

            @Override
            public T next() {
                if (!hasNext()) {
                    throw new NoSuchElementException();
                }
                T value = current.getValue();
                current = current.getNext();
                return value;
            }
        };
    }

    private void validateIndex(int index) {
        if (index < 0 || index >= size) {
            throw new IndexOutOfBoundsException("Invalid index: " + index);
        }
    }
}
