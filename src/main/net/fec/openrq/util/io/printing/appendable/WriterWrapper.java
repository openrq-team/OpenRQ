package net.fec.openrq.util.io.printing.appendable;


import java.io.IOException;
import java.io.Writer;
import java.nio.CharBuffer;


/**
 * @author Ricardo Fonseca &lt;rfonseca&#064;lasige.di.fc.ul.pt&gt;
 * @param <W>
 */
class WriterWrapper<W extends Writer> extends AppendableWrapper<W> {

    private static final int CACHED_ARRAY_SIZE = 1024;

    private char[] cachedArray = null;


    public WriterWrapper(W writer) {

        super(writer);
    }

    @Override
    public void print(CharSequence csq) throws IOException {

        if (csq instanceof String) {
            appendable.write((String)csq);
        }
        else if (csq instanceof CharBuffer) {
            final int srcLen = csq.length();
            final char[] array = allocateArray(srcLen);
            ((CharBuffer)csq).get(array, 0, srcLen);
            print(array, 0, srcLen);
        }
        else {
            super.print(csq);
        }
    }

    @Override
    public void print(CharSequence csq, int start, int end) throws IOException {

        if (start < 0 || end < start || csq.length() < end) {
            throw new IndexOutOfBoundsException();
        }

        if (csq instanceof String) {
            appendable.write((String)csq, start, end - start);
        }
        else if (csq instanceof StringBuilder) {
            final int srcLen = end - start;
            final char[] array = allocateArray(srcLen);
            ((StringBuilder)csq).getChars(start, end, array, 0);
            print(array, 0, srcLen);
        }
        else if (csq instanceof StringBuffer) {
            final int srcLen = end - start;
            final char[] array = allocateArray(srcLen);
            ((StringBuffer)csq).getChars(start, end, array, 0);
            print(array, 0, srcLen);
        }
        else if (csq instanceof CharBuffer) {
            final int srcLen = end - start;
            final char[] array = allocateArray(srcLen);
            ((CharBuffer)csq).subSequence(start, end).get(array, 0, srcLen);
            print(array, 0, srcLen);
        }
        else {
            super.print(csq, start, end);
        }
    }

    @Override
    public void print(char[] c, int off, int len) throws IOException {

        appendable.write(c, off, len);
    }

    // requires length >= 0
    private char[] allocateArray(int length) {

        if (length > CACHED_ARRAY_SIZE) {
            return new char[length]; // do not keep very big arrays in memory
        }

        char[] cached = this.cachedArray;
        if (cached == null) {
            cached = new char[CACHED_ARRAY_SIZE];
            this.cachedArray = cached;
        }

        return cached;
    }
}
