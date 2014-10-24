package net.fec.openrq.util.io.printing.appendable;


import java.io.IOException;
import java.nio.CharBuffer;
import java.util.Formatter;
import java.util.Objects;


/**
 * @author Ricardo Fonseca &lt;rfonseca&#064;lasige.di.fc.ul.pt&gt;
 * @param <A>
 */
class AppendableWrapper<A extends Appendable> implements Appendable {

    protected final A appendable;
    private Formatter formatter;	// lazily initialized


    public AppendableWrapper(A appendable) {

        this.appendable = Objects.requireNonNull(appendable);
        this.formatter = null;
    }

    public void print(char c) throws IOException {

        appendable.append(c);
    }

    public void print(CharSequence csq) throws IOException {

        appendable.append(csq);
    }

    public void print(CharSequence csq, int start, int end) throws IOException {

        appendable.append(csq, start, end);
    }

    public void print(char[] c) throws IOException {

        print(c, 0, c.length);
    }

    public void print(char[] c, int off, int len) throws IOException {

        print(CharBuffer.wrap(c, off, len));
    }

    public void println() throws IOException {

        appendable.append(System.lineSeparator());
    }

    public void println(char c) throws IOException {

        print(c);
        println();
    }

    public void println(CharSequence csq) throws IOException {

        print(csq);
        println();
    }

    public void println(CharSequence csq, int start, int end) throws IOException {

        print(csq, start, end);
        println();
    }

    public void println(char[] c) throws IOException {

        println(c, 0, c.length);
    }

    public void println(char[] c, int off, int len) throws IOException {

        print(c, off, len);
        println();
    }

    public void printf(String format, Object... args) throws IOException {

        final IOException ex = getFormatter().format(format, args).ioException();
        if (ex != null) {
            throw ex;
        }
    }

    private Formatter getFormatter() {

        Formatter f = this.formatter;
        if (f == null) {
            f = new Formatter(appendable);
            this.formatter = f;
        }

        return f;
    }

    @Override
    public final Appendable append(char c) throws IOException {

        print(c);
        return this;
    }

    @Override
    public final Appendable append(CharSequence csq) throws IOException {

        print(csq);
        return this;
    }

    @Override
    public final Appendable append(CharSequence csq, int start, int end) throws IOException {

        print(csq, start, end);
        return this;
    }
}
