package edu.inventory.administrator.datastructures;

public class Queue<T> {
    private Node<T> front;
    private Node<T> rear;
    private int size;

    public void enqueue(T value) {
        Node<T> node = new Node<>(value);
        if (rear == null) {
            front = node;
            rear = node;
        } else {
            rear.setNext(node);
            rear = node;
        }
        size++;
    }

    public T dequeue() {
        if (isEmpty()) {
            return null;
        }
        T value = front.getValue();
        front = front.getNext();
        if (front == null) {
            rear = null;
        }
        size--;
        return value;
    }

    public T peek() {
        return isEmpty() ? null : front.getValue();
    }

    public boolean isEmpty() {
        return size == 0;
    }

    public int size() {
        return size;
    }

    public LinkedList<T> toLinkedList() {
        LinkedList<T> values = new LinkedList<>();
        Node<T> current = front;
        while (current != null) {
            values.add(current.getValue());
            current = current.getNext();
        }
        return values;
    }
}
