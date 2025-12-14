/*
 * Copyright (c) 2014, NTUU KPI, Computer systems department and/or its affiliates.
 * All rights reserved.
 * NTUU KPI PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package ua.kpi.comsys.test2.implementation;

import java.io.File;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.file.Files;
import java.util.*;

import ua.kpi.comsys.test2.NumberList;

/**
 * Custom implementation of INumberList interface.
 * Variant: 3518
 * C3 = 2 (Circular Doubly Linked List)
 * C5 = 3 (Decimal), Additional = Hexadecimal (Base 16)
 * C7 = 4 (Modulo %)
 *
 * @author Oleksandr Sliusar IO-35
 */
public class NumberListImpl implements NumberList {

    // Inner class for the node of a circular doubly linked list
    private static class Node {
        Byte value;
        Node next;
        Node prev;

        Node(Byte value) {
            this.value = value;
        }
    }

    private Node head;
    private int size;

    // Radix of the current list (default is 10)
    private int radix = 10;

    /**
     * Default constructor.
     * Returns empty <tt>NumberListImpl</tt>
     */
    public NumberListImpl() {
        this.head = null;
        this.size = 0;
    }

    /**
     * Constructs new <tt>NumberListImpl</tt> by <b>decimal</b> number
     * from file, defined in string format.
     *
     * @param file - file where number is stored.
     */
    public NumberListImpl(File file) {
        this();
        try {
            List<String> lines = Files.readAllLines(file.toPath());
            if (!lines.isEmpty()) {
                String content = lines.get(0).trim();
                parseAndAdd(content);
            }
        } catch (IOException e) {
            // Log error but don't crash, resulting in an empty list (as expected by tests)
            e.printStackTrace();
        }
    }

    /**
     * Constructs new <tt>NumberListImpl</tt> by <b>decimal</b> number
     * in string notation.
     *
     * @param value - number in string notation.
     */
    public NumberListImpl(String value) {
        this();
        parseAndAdd(value);
    }

    // Private constructor for creating a list with a different radix
    private NumberListImpl(String value, int radix) {
        this();
        this.radix = radix;
        parseAndAdd(value);
    }

    // Helper method for string parsing
    private void parseAndAdd(String value) {
        if (value == null || value.isEmpty()) return;

        // Ignore negative numbers according to test logic
        if (value.startsWith("-")) return;

        for (int i = 0; i < value.length(); i++) {
            char c = value.charAt(i);
            // Check for character validity for hexadecimal (maximum) base
            if (Character.digit(c, 16) == -1) {
                this.clear();
                return;
            }
            int digit = Character.digit(c, 16);
            this.add((byte) digit);
        }
    }

    /**
     * Saves the number, stored in the list, into specified file
     * in <b>decimal</b> scale of notation.
     *
     * @param file - file where number has to be stored.
     */
    public void saveList(File file) {
        try {
            Files.write(file.toPath(), this.toDecimalString().getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Returns student's record book number, which has 4 decimal digits.
     *
     * @return student's record book number.
     */
    public static int getRecordBookNumber() {
        return 3518;
    }

    /**
     * Returns new <tt>NumberListImpl</tt> which represents the same number
     * in other scale of notation, defined by personal test assignment.
     * Target: Hexadecimal (base 16) for Variant 3518 (C5=3 -> +1 -> 4).
     *
     * @return <tt>NumberListImpl</tt> in other scale of notation.
     */
    public NumberListImpl changeScale() {
        if (this.isEmpty()) return new NumberListImpl();

        // 1. Get the value of the current number (as BigInteger)
        BigInteger val = toBigInteger();

        // 2. Convert to hexadecimal system
        String hexString = val.toString(16).toUpperCase();

        // 3. Create a new list, specifying that it stores Hex (radix 16)
        return new NumberListImpl(hexString, 16);
    }

    /**
     * Returns new <tt>NumberListImpl</tt> which represents the result of
     * additional operation, defined by personal test assignment.
     * Operation: Remainder (%) for Variant 3518 (C7=4).
     *
     * @param arg - second argument of additional operation
     * @return result of additional operation.
     */
    public NumberListImpl additionalOperation(NumberList arg) {
        BigInteger num1 = this.toBigInteger();

        // Convert argument to BigInteger
        StringBuilder sb = new StringBuilder();
        for(Byte b : arg) {
            char c = Character.forDigit(b, 10);
            sb.append(c);
        }
        if (sb.length() == 0) return new NumberListImpl("0");

        try {
            BigInteger num2 = new BigInteger(sb.toString());
            if (num2.equals(BigInteger.ZERO)) return new NumberListImpl("0");
            // Remainder operation
            BigInteger result = num1.remainder(num2);
            return new NumberListImpl(result.toString());
        } catch (NumberFormatException e) {
            return new NumberListImpl("0");
        }
    }

    // Helper method: List -> BigInteger (using current radix)
    private BigInteger toBigInteger() {
        if (this.isEmpty()) return BigInteger.ZERO;
        StringBuilder sb = new StringBuilder();
        Node current = head;
        if (current != null) {
            do {
                char c = Character.forDigit(current.value, 16);
                sb.append(Character.toUpperCase(c));
                current = current.next;
            } while (current != head);
        }
        return new BigInteger(sb.toString(), this.radix);
    }

    /**
     * Returns string representation of number, stored in the list
     * in <b>decimal</b> scale of notation.
     *
     * @return string representation in <b>decimal</b> scale.
     */
    public String toDecimalString() {
        if (isEmpty()) return "";
        // If the system is already decimal - just print the digits
        if (this.radix == 10) {
            return this.toString();
        }
        // If the system is different (Hex), convert value to decimal string
        return toBigInteger().toString(10);
    }

    @Override
    public String toString() {
        if (isEmpty()) return "";
        StringBuilder sb = new StringBuilder();
        Node current = head;
        if (current != null) {
            do {
                char c = Character.forDigit(current.value, 16);
                sb.append(Character.toUpperCase(c));
                current = current.next;
            } while (current != head);
        }
        return sb.toString();
    }

    // --- Core List Methods (Equals & HashCode) ---

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        NumberListImpl that = (NumberListImpl) o;
        if (this.size != that.size) return false;

        // Compare elements sequentially
        Iterator<Byte> it1 = this.iterator();
        Iterator<Byte> it2 = that.iterator();
        while (it1.hasNext() && it2.hasNext()) {
            if (!Objects.equals(it1.next(), it2.next())) return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hashCode = 1;
        for (Byte e : this) {
            hashCode = 31 * hashCode + (e == null ? 0 : e.hashCode());
        }
        return hashCode;
    }

    // --- List and NumberList interface methods ---

    @Override
    public int size() {
        return size;
    }

    @Override
    public boolean isEmpty() {
        return size == 0;
    }

    @Override
    public boolean contains(Object o) {
        if (isEmpty()) return false;
        Node current = head;
        do {
            if (Objects.equals(current.value, o)) return true;
            current = current.next;
        } while (current != head);
        return false;
    }

    @Override
    public Iterator<Byte> iterator() {
        return new Iterator<Byte>() {
            private Node current = head;
            private int count = 0;

            @Override
            public boolean hasNext() {
                return count < size;
            }

            @Override
            public Byte next() {
                if (!hasNext()) throw new NoSuchElementException();
                Byte val = current.value;
                current = current.next;
                count++;
                return val;
            }
        };
    }

    @Override
    public Object[] toArray() {
        Object[] arr = new Object[size];
        int i = 0;
        for (Byte b : this) {
            arr[i++] = b;
        }
        return arr;
    }

    @Override
    public <T> T[] toArray(T[] a) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean add(Byte e) {
        Node newNode = new Node(e);
        if (head == null) {
            head = newNode;
            head.next = head;
            head.prev = head;
        } else {
            Node tail = head.prev;
            tail.next = newNode;
            newNode.prev = tail;
            newNode.next = head;
            head.prev = newNode;
        }
        size++;
        return true;
    }

    @Override
    public boolean remove(Object o) {
        if (isEmpty()) return false;
        Node current = head;
        do {
            if (Objects.equals(current.value, o)) {
                removeNode(current);
                return true;
            }
            current = current.next;
        } while (current != head);
        return false;
    }

    private void removeNode(Node node) {
        if (size == 1) {
            head = null;
        } else {
            node.prev.next = node.next;
            node.next.prev = node.prev;
            if (node == head) {
                head = node.next;
            }
        }
        size--;
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        for (Object e : c) {
            if (!contains(e)) return false;
        }
        return true;
    }

    @Override
    public boolean addAll(Collection<? extends Byte> c) {
        boolean modified = false;
        for (Byte e : c) {
            if (add(e)) modified = true;
        }
        return modified;
    }

    @Override
    public boolean addAll(int index, Collection<? extends Byte> c) {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        boolean modified = false;
        for (Object o : c) {
            while (contains(o)) {
                remove(o);
                modified = true;
            }
        }
        return modified;
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        if (isEmpty()) return false;
        boolean modified = false;
        Node current = head;
        // Iterate over a copy to safely remove elements
        List<Node> nodesToCheck = new ArrayList<>();
        do {
            nodesToCheck.add(current);
            current = current.next;
        } while (current != head);

        for (Node node : nodesToCheck) {
            if (!c.contains(node.value)) {
                removeNode(node);
                modified = true;
            }
        }
        return modified;
    }

    @Override
    public void clear() {
        head = null;
        size = 0;
        radix = 10; // Reset radix to default
    }

    @Override
    public Byte get(int index) {
        if (index < 0 || index >= size) throw new IndexOutOfBoundsException();
        return getNode(index).value;
    }

    private Node getNode(int index) {
        Node current = head;
        for (int i = 0; i < index; i++) {
            current = current.next;
        }
        return current;
    }

    @Override
    public Byte set(int index, Byte element) {
        if (index < 0 || index >= size) throw new IndexOutOfBoundsException();
        Node node = getNode(index);
        Byte oldVal = node.value;
        node.value = element;
        return oldVal;
    }

    @Override
    public void add(int index, Byte element) {
        if (index < 0 || index > size) throw new IndexOutOfBoundsException();
        if (index == size) {
            add(element);
        } else {
            Node newNode = new Node(element);
            Node current = getNode(index);
            Node prev = current.prev;

            prev.next = newNode;
            newNode.prev = prev;
            newNode.next = current;
            current.prev = newNode;

            if (index == 0) head = newNode;
            size++;
        }
    }

    @Override
    public Byte remove(int index) {
        if (index < 0 || index >= size) throw new IndexOutOfBoundsException();
        Node node = getNode(index);
        Byte val = node.value;
        removeNode(node);
        return val;
    }

    @Override
    public int indexOf(Object o) {
        if (isEmpty()) return -1;
        Node current = head;
        for (int i = 0; i < size; i++) {
            if (Objects.equals(current.value, o)) return i;
            current = current.next;
        }
        return -1;
    }

    @Override
    public int lastIndexOf(Object o) {
        if (isEmpty()) return -1;
        Node current = head.prev; // Start from the end
        for (int i = size - 1; i >= 0; i--) {
            if (Objects.equals(current.value, o)) return i;
            current = current.prev;
        }
        return -1;
    }

    @Override
    public ListIterator<Byte> listIterator() {
        return listIterator(0);
    }

    @Override
    public ListIterator<Byte> listIterator(int index) {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<Byte> subList(int fromIndex, int toIndex) {
        throw new UnsupportedOperationException();
    }

    // --- Implementation of specific methods ---

    @Override
    public boolean swap(int index1, int index2) {
        if (index1 < 0 || index1 >= size || index2 < 0 || index2 >= size) return false;
        if (index1 == index2) return true;

        Node node1 = getNode(index1);
        Node node2 = getNode(index2);

        Byte temp = node1.value;
        node1.value = node2.value;
        node2.value = temp;

        return true;
    }

    @Override
    public void sortAscending() {
        // Simple bubble sort of values (nodes are not moved)
        if (size < 2) return;
        boolean swapped;
        do {
            swapped = false;
            Node current = head;
            for (int i = 0; i < size - 1; i++) {
                if (current.value > current.next.value) {
                    Byte temp = current.value;
                    current.value = current.next.value;
                    current.next.value = temp;
                    swapped = true;
                }
                current = current.next;
            }
        } while (swapped);
    }

    @Override
    public void sortDescending() {
        if (size < 2) return;
        boolean swapped;
        do {
            swapped = false;
            Node current = head;
            for (int i = 0; i < size - 1; i++) {
                if (current.value < current.next.value) {
                    Byte temp = current.value;
                    current.value = current.next.value;
                    current.next.value = temp;
                    swapped = true;
                }
                current = current.next;
            }
        } while (swapped);
    }

    @Override
    public void shiftLeft() {
        if (size > 1 && head != null) {
            head = head.next;
        }
    }

    @Override
    public void shiftRight() {
        if (size > 1 && head != null) {
            head = head.prev;
        }
    }
}
