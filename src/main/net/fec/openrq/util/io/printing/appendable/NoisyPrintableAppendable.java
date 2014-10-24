package net.fec.openrq.util.io.printing.appendable;


import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.Writer;
import java.nio.CharBuffer;
import java.util.Objects;


/**
 * A class that wraps an {@code Appendable} object and provides methods for printing characters.
 * <p>
 * The methods from this class throw an {@code IOException} if they detect an I/O error from the underlying
 * {@code Appendable} object.
 * 
 * @author Ricardo Fonseca &lt;rfonseca&#064;lasige.di.fc.ul.pt&gt;
 */
public class NoisyPrintableAppendable implements Appendable {

    public static NoisyPrintableAppendable of(Appendable appendable) {

        Objects.requireNonNull(appendable);

        if (appendable instanceof PrintStream) {
            return new NoisyPrintableAppendable(new PrintStreamWrapper<>((PrintStream)appendable));
        }
        else if (appendable instanceof PrintWriter) {
            return new NoisyPrintableAppendable(new PrintWriterWrapper<>((PrintWriter)appendable));
        }
        else if (appendable instanceof Writer) {
            return new NoisyPrintableAppendable(new WriterWrapper<>((Writer)appendable));
        }
        else if (appendable instanceof StringBuilder) {
            return new NoisyPrintableAppendable(new StringBuilderWrapper((StringBuilder)appendable));
        }
        else if (appendable instanceof StringBuffer) {
            return new NoisyPrintableAppendable(new StringBufferWrapper((StringBuffer)appendable));
        }
        else if (appendable instanceof CharBuffer) {
            return new NoisyPrintableAppendable(new CharBufferWrapper<>((CharBuffer)appendable));
        }
        else {
            return new NoisyPrintableAppendable(new AppendableWrapper<>(appendable));
        }
    }


    private final AppendableWrapper<?> wrapper;


    NoisyPrintableAppendable(AppendableWrapper<?> wrapper) {

        this.wrapper = wrapper;
    }

    public NoisyPrintableAppendable print(char c) throws IOException {

        wrapper.print(c);
        return this;
    }

    public NoisyPrintableAppendable print(CharSequence csq) throws IOException {

        wrapper.print(csq);
        return this;
    }

    public NoisyPrintableAppendable print(CharSequence csq, int start, int end) throws IOException {

        wrapper.print(csq, start, end);
        return this;
    }

    public NoisyPrintableAppendable print(char[] c) throws IOException {

        wrapper.print(c);
        return this;
    }

    public NoisyPrintableAppendable print(char[] c, int off, int len) throws IOException {

        wrapper.print(c, off, len);
        return this;
    }

    public NoisyPrintableAppendable println() throws IOException {

        wrapper.println();
        return this;
    }

    public NoisyPrintableAppendable println(char c) throws IOException {

        wrapper.println(c);
        return this;
    }

    public NoisyPrintableAppendable println(CharSequence csq) throws IOException {

        wrapper.println(csq);
        return this;
    }

    public NoisyPrintableAppendable println(CharSequence csq, int start, int end) throws IOException {

        wrapper.println(csq, start, end);
        return this;
    }

    public NoisyPrintableAppendable println(char[] c) throws IOException {

        wrapper.println(c);
        return this;
    }

    public NoisyPrintableAppendable println(char[] c, int off, int len) throws IOException {

        wrapper.println(c, off, len);
        return println();
    }

    public NoisyPrintableAppendable printf(String format, Object... args) throws IOException {

        wrapper.printf(format, args);
        return this;
    }

    @Override
    public final Appendable append(char c) throws IOException {

        wrapper.append(c);
        return this;
    }

    @Override
    public final Appendable append(CharSequence csq) throws IOException {

        wrapper.append(csq);
        return this;
    }

    @Override
    public final Appendable append(CharSequence csq, int start, int end) throws IOException {

        wrapper.append(csq, start, end);
        return this;
    }
}
