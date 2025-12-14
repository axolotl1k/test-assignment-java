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
 * Variant: 3518.
 * <p>
 * Configuration based on variant:
 * <ul>
 * <li><b>C3 (List Type) = 2:</b> Circular Doubly Linked List.</li>
 * <li><b>C5 (Base System) = 3:</b> Decimal system (base 10).</li>
 * <li><b>Additional Base System (C5+1):</b> Hexadecimal (base 16).</li>
 * <li><b>C7 (Operation) = 4:</b> Remainder of division (Modulo %).</li>
 * </ul>
 *
 * @author Oleksandr Sliusar IO-35
 */
public class NumberListImpl implements NumberList {

    /**
     * Inner class representing a node in the circular doubly linked list.
     * Stores a single digit of the number.
     */
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

    /**
     * The radix (base) of the current number list representation.
     * Default is 10 (Decimal). Becomes 16 (Hexadecimal) after changeScale().
     */
    private int radix = 10;

    /**
     * Default constructor.
     * Returns an empty <tt>NumberListImpl</tt>.
     */
    public NumberListImpl() {
        this.head = null;
        this.size = 0;
    }

    /**
     * Constructs new <tt>NumberListImpl</tt> by reading a <b>decimal</b> number
     * from a file. The number is expected to be in string format.
     *
     * @param file - file where the number is stored.
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
     * Constructs new <tt>NumberListImpl</tt> by parsing a number
     * in string notation.
     *
     * @param value - number in string notation.
     */
    public NumberListImpl(String value) {
        this();
        parseAndAdd(value);
    }

    /**
     * Private constructor for creating a list with a specific radix.
     * Used internally for scale changes.
     *
     * @param value - string representation of the number.
     * @param radix - the base of the number system (e.g., 16 for Hex).
     */
    private NumberListImpl(String value, int radix) {
        this();
        this.radix = radix;
        parseAndAdd(value);
    }

    /**
     * Parses the string value and adds digits to the list.
     * Handles Hex digits (0-9, A-F).
     *
     * @param value - string to parse.
     */
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
     * Saves the number, stored in the list, into the specified file
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
     * Returns student's record book number.
     * Used by tests to determine the variant configuration.
     *
     * @return 3518 (Student's record book number).
     */
    public static int getRecordBookNumber() {
        return 3518;
    }

    /**
     * Returns a new <tt>NumberListImpl</tt> which represents the same number
     * converted to another scale of notation.
     * <p>
     * <b>Variant Specifics:</b>
     * Converts from Decimal (base 10) to <b>Hexadecimal (base 16)</b>.
     *
     * @return <tt>NumberListImpl</tt> in Hexadecimal scale.
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
     * Returns a new <tt>NumberListImpl</tt> which represents the result of
     * an additional operation.
     * <p>
     * <b>Variant Specifics (C7=4):</b>
     * Performs the <b>Remainder (Modulo %)</b> operation: {@code this % arg}.
     *
     * @param arg - second argument (divisor) of the operation.
     * @return result of {@code this % arg}. Returns "0" if divisor is zero.
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

    /**
     * Helper method to convert the current list into a BigInteger.
     * Uses the current {@code radix} to correctly interpret the digits.
     *
     * @return BigInteger representation of the list content.
     */
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
     * If the current list is Hexadecimal, it is converted back to Decimal string.
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
        return indexOf(o) >= 0;
    }

    @Override
    public Iterator<Byte> iterator() {
        return listIterator();
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
        if (index < 0 || index > size) throw new IndexOutOfBoundsException();
        if (c.isEmpty()) return false;
        // Insert collection elements one by one starting from index
        for (Byte e : c) {
            add(index++, e);
        }
        return true;
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
        return getNode(index).value;
    }

    private Node getNode(int index) {
        if (index < 0 || index >= size) throw new IndexOutOfBoundsException();
        // Optimization: search from the nearest end (start or end)
        Node current = head;
        if (index <= size / 2) {
            for (int i = 0; i < index; i++) current = current.next;
        } else {
            current = head.prev;
            for (int i = size - 1; i > index; i--) current = current.prev;
        }
        return current;
    }

    @Override
    public Byte set(int index, Byte element) {
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

            // prev <-> newNode <-> current
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
        if (index < 0 || index > size) throw new IndexOutOfBoundsException();
        return new ListIterator<Byte>() {
            private Node lastReturned = null;
            private Node nextNode = (index == size) ? head : (size == 0 ? null : getNode(index));
            private int nextIndex = index;

            @Override
            public boolean hasNext() {
                return nextIndex < size;
            }

            @Override
            public Byte next() {
                if (!hasNext()) throw new NoSuchElementException();
                lastReturned = nextNode;
                nextNode = nextNode.next;
                nextIndex++;
                return lastReturned.value;
            }

            @Override
            public boolean hasPrevious() {
                return nextIndex > 0;
            }

            @Override
            public Byte previous() {
                if (!hasPrevious()) throw new NoSuchElementException();
                nextNode = (nextNode == null) ? head.prev : nextNode.prev;
                lastReturned = nextNode;
                nextIndex--;
                return lastReturned.value;
            }

            @Override
            public int nextIndex() {
                return nextIndex;
            }

            @Override
            public int previousIndex() {
                return nextIndex - 1;
            }

            @Override
            public void remove() {
                if (lastReturned == null) throw new IllegalStateException();
                Node nodeToRemove = lastReturned;
                if (lastReturned == nextNode) {
                    nextNode = nextNode.next;
                } else {
                    nextIndex--;
                }
                removeNode(nodeToRemove);
                lastReturned = null;
            }

            @Override
            public void set(Byte e) {
                if (lastReturned == null) throw new IllegalStateException();
                lastReturned.value = e;
            }

            @Override
            public void add(Byte e) {
                NumberListImpl.this.add(nextIndex, e);
                nextIndex++;
                lastReturned = null;
            }
        };
    }

    @Override
    public List<Byte> subList(int fromIndex, int toIndex) {
        if (fromIndex < 0 || toIndex > size || fromIndex > toIndex) throw new IndexOutOfBoundsException();
        // Return a new list containing the elements (Deep Copy)
        // Since implementing a "View" inner class for Circular List is complex for this scope
        NumberListImpl sub = new NumberListImpl();
        if (size == 0 || fromIndex == toIndex) return sub;

        Node curr = getNode(fromIndex);
        for (int i = 0; i < toIndex - fromIndex; i++) {
            sub.add(curr.value);
            curr = curr.next;
        }
        return sub;
    }

    // --- Implementation of specific methods ---

    /**
     * Exchanges two list elements by their indices.
     * Swaps the values of the nodes, not the nodes themselves.
     *
     * @param index1 - index of first element.
     * @param index2 - index of second element.
     * @return true if indices are valid and swap was successful.
     */
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

    /**
     * Sorts elements of the list in ascending order.
     * Implements Bubble Sort algorithm by swapping node values.
     */
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

    /**
     * Sorts elements of the list in descending order.
     * Implements Bubble Sort algorithm by swapping node values.
     */
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

    /**
     * Performs left cyclic shift in the current list.
     * Moves the head pointer to the next node.
     */
    @Override
    public void shiftLeft() {
        if (size > 1 && head != null) {
            head = head.next;
        }
    }

    /**
     * Performs right cyclic shift in the current list.
     * Moves the head pointer to the previous node.
     */
    @Override
    public void shiftRight() {
        if (size > 1 && head != null) {
            head = head.prev;
        }
    }
}
