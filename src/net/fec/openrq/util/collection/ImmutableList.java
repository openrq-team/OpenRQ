package net.fec.openrq.util.collection;


import java.util.AbstractList;
import java.util.Collection;
import java.util.ConcurrentModificationException;
import java.util.RandomAccess;


/**
 * This class is a simple implementation of an immutable list where all the "write" methods simply throw an
 * {@code UnsupportedOperationException}.
 * 
 * @author Jos&#233; Lopes &lt;jlopes&#064;lasige.di.fc.ul.pt&gt;
 * @author Ricardo Fonseca &lt;ricardof&#064;lasige.di.fc.ul.pt&gt;
 * @param <E>
 *            The element type of the list
 */
public class ImmutableList<E> extends AbstractList<E> implements RandomAccess {

    /**
     * Returns an immutable copy of the provided collection, as a list. This method iterates over the elements of the
     * collection while copying them to the returned list.
     * 
     * @param collection
     *            A collection to be copied
     * @return an immutable copy of the provided collection, as a list
     * @exception NullPointerException
     *                If {@code collection} is {@code null}
     * @exception ConcurrentModificationException
     *                If the method detects a modification in the provided collection while copying its elements
     */
    public static <T> ImmutableList<T> getImmutableCopy(Collection<T> collection) {

        final int size = collection.size();
        final Object[] elements = new Object[size];

        int i = 0;
        for (T el : collection) {
            if (i >= size) {
                throw new ConcurrentModificationException();
            }
            elements[i] = el;
            i++;
        }
        if (i != size) {
            throw new ConcurrentModificationException();
        }

        return new ImmutableList<>(elements);
    }

    /**
     * Returns an immutable copy of the provided collection, as a list. This method iterates over the elements of the
     * collection while copying them to the returned list. Additionally, this method does not permit {@code null}
     * elements inside the collection and will throw a {@code NullPointerException} if any {@code null} element is
     * found.
     * 
     * @param collection
     *            A collection to be copied
     * @return an immutable copy of the provided collection, as a list
     * @exception NullPointerException
     *                If {@code collection} or any of its elements is {@code null}
     * @exception ConcurrentModificationException
     *                If the method detects a modification in the provided collection while copying its elements
     */
    public static <T> ImmutableList<T> getNullFreeImmutableCopy(Collection<T> collection) {

        final int size = collection.size();
        final Object[] elements = new Object[size];

        int i = 0;
        for (T el : collection) {
            if (el == null) {
                throw new NullPointerException("null element inside the collection");
            }
            if (i >= size) {
                throw new ConcurrentModificationException();
            }
            elements[i] = el;
            i++;
        }
        if (i != size) {
            throw new ConcurrentModificationException();
        }

        return new ImmutableList<>(elements);
    }


    private final Object[] elements;


    private ImmutableList(Object[] elements) {

        this.elements = elements;
    }

    @SuppressWarnings("unchecked")
    @Override
    public E get(int index) {

        if (index < 0 || index >= size()) {
            throw new IndexOutOfBoundsException();
        }

        return (E)elements[index];
    }

    @Override
    public int size() {

        return elements.length;
    }

    @Override
    public int indexOf(Object o) {

        final int size = size();
        if (o == null) {
            for (int i = 0; i < size; i++) {
                if (elements[i] == null) {
                    return i;
                }
            }
        }
        else {
            for (int i = 0; i < size; i++) {
                if (o.equals(elements[i])) {
                    return i;
                }
            }
        }
        return -1;
    }

    @Override
    public int lastIndexOf(Object o) {

        final int size = size();
        if (o == null) {
            for (int i = (size - 1); i >= 0; i--) {
                if (elements[i] == null) {
                    return i;
                }
            }
        }
        else {
            for (int i = (size - 1); i >= 0; i--) {
                if (o.equals(elements[i])) {
                    return i;
                }
            }
        }
        return -1;
    }
}
