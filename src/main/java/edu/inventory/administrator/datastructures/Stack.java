package edu.inventory.administrator.datastructures;

public class Stack<T> {
    private Node<T> top;
    private int size;

    public void push(T value) {
        Node<T> node = new Node<>(value);
        node.setNext(top);
        top = node;
        size++;
    }

    public T pop() {
        if (isEmpty()) {
            return null;
        }
        T value = top.getValue();
        top = top.getNext();
        size--;
        return value;
    }

    public T peek() {
        return isEmpty() ? null : top.getValue();
    }

    public boolean isEmpty() {
        return size == 0;
    }

    public int size() {
        return size;
    }
}
