package net.fec.openrq.util.collection;


import java.util.AbstractList;
import java.util.Arrays;
import java.util.Collection;
import java.util.ConcurrentModificationException;
import java.util.Objects;
import java.util.RandomAccess;


/**
 * This class is a simple implementation of an immutable list where all the "write" methods simply throw an
 * {@code UnsupportedOperationException}.
 * 
 * @author Ricardo Fonseca &lt;rfonseca&#064;lasige.di.fc.ul.pt&gt;
 * @param <E>
 *            The element type of the list
 */
public class ImmutableList<E> extends AbstractList<E> implements RandomAccess {

    /**
     * Returns an immutable list with the provided elements. The returned list keeps a reference to the provided array
     * of elements, so make sure the array is not modifiable.
     * 
     * @param elements
     *            The elements to be placed in an immutable list
     * @return an immutable list of the provided elements
     * @exception NullPointerException
     *                If {@code elements} is {@code null}
     */
    @SafeVarargs
    public static <T> ImmutableList<T> of(T... elements) {

        return new ImmutableList<>(elements);
    }

    /**
     * Returns an immutable list with the provided elements. The returned list keeps a reference to the provided array
     * of elements, so make sure the array is not modifiable. Additionally, this method does not permit {@code null}
     * elements and will throw a {@code NullPointerException} if any {@code null} element is found.
     * 
     * @param elements
     *            The elements to be placed in an immutable list
     * @return an immutable list of the provided elements
     * @exception NullPointerException
     *                If {@code elements} is {@code null}, or any specific element is {@code null}
     */
    @SafeVarargs
    public static <T> ImmutableList<T> ofNonNull(T... elements) {

        for (T el : elements) {
            Objects.requireNonNull(el);
        }
        return new ImmutableList<>(elements);
    }

    /**
     * Returns an immutable list with the provided elements. The returned list does not keep a reference to the provided
     * array of elements.
     * 
     * @param elements
     *            The elements to be placed in an immutable list
     * @return an immutable list of the provided elements
     * @exception NullPointerException
     *                If {@code elements} is {@code null}
     */
    @SafeVarargs
    public static <T> ImmutableList<T> copyOf(T... elements) {

        return new ImmutableList<>(Arrays.copyOf(elements, elements.length));
    }

    /**
     * Returns an immutable list with the provided elements. The returned list does not keep a reference to the provided
     * array of elements. Additionally, this method does not permit {@code null} elements and will throw a
     * {@code NullPointerException} if any {@code null} element is found.
     * 
     * @param elements
     *            The elements to be placed in an immutable list
     * @return an immutable list of the provided elements
     * @exception NullPointerException
     *                If {@code elements} is {@code null}, or any specific element is {@code null}
     */
    @SafeVarargs
    public static <T> ImmutableList<T> copyOfNonNull(T... elements) {

        final T[] copy = Arrays.copyOf(elements, elements.length);
        for (T el : copy) {
            Objects.requireNonNull(el);
        }
        return new ImmutableList<>(copy);
    }

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
    public static <T> ImmutableList<T> copyOf(Collection<T> collection) {

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
    public static <T> ImmutableList<T> copyOfNonNull(Collection<T> collection) {

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

    /**
     * {@inheritDoc}
     * 
     * @throws IndexOutOfBoundsException
     *             {@inheritDoc}
     */
    @Override
    public E get(int index) {

        if (index < 0 || index >= size()) {
            throw new IndexOutOfBoundsException();
        }

        @SuppressWarnings("unchecked")
        final E el = (E)elements[index];
        return el;
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
