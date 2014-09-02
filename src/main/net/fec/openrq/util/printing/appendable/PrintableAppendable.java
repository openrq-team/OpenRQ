package net.fec.openrq.util.printing.appendable;


import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.Writer;
import java.nio.CharBuffer;


/**
 * A class that wraps an Appendable object and provides methods for printing characters.
 * 
 * @author Ricardo Fonseca &lt;ricardof&#064;lasige.di.fc.ul.pt&gt;
 */
public class PrintableAppendable implements Appendable {

    public static PrintableAppendable of(Appendable appendable) {

        if (appendable instanceof PrintStream) {
            return new PrintableAppendable(new PrintStreamWrapper<>((PrintStream)appendable));
        }
        else if (appendable instanceof PrintWriter) {
            return new PrintableAppendable(new PrintWriterWrapper<>((PrintWriter)appendable));
        }
        else if (appendable instanceof Writer) {
            return new PrintableAppendable(new WriterWrapper<>((Writer)appendable));
        }
        else if (appendable instanceof StringBuilder) {
            return new PrintableAppendable(new StringBuilderWrapper((StringBuilder)appendable));
        }
        else if (appendable instanceof StringBuffer) {
            return new PrintableAppendable(new StringBufferWrapper((StringBuffer)appendable));
        }
        else if (appendable instanceof CharBuffer) {
            return new PrintableAppendable(new CharBufferWrapper<>((CharBuffer)appendable));
        }
        else {
            return new PrintableAppendable(new AppendableWrapper<>(appendable));
        }
    }


    private final AppendableWrapper<?> wrapper;


    PrintableAppendable(AppendableWrapper<?> wrapper) {

        this.wrapper = wrapper;
    }

    public PrintableAppendable print(char c) throws IOException {

        wrapper.print(c);
        return this;
    }

    public PrintableAppendable print(CharSequence csq) throws IOException {

        wrapper.print(csq);
        return this;
    }

    public PrintableAppendable print(CharSequence csq, int start, int end) throws IOException {

        wrapper.print(csq, start, end);
        return this;
    }

    public PrintableAppendable print(char[] c) throws IOException {

        wrapper.print(c);
        return this;
    }

    public PrintableAppendable print(char[] c, int off, int len) throws IOException {

        wrapper.print(c, off, len);
        return this;
    }

    public PrintableAppendable println() throws IOException {

        wrapper.println();
        return this;
    }

    public PrintableAppendable println(char c) throws IOException {

        wrapper.println(c);
        return this;
    }

    public PrintableAppendable println(CharSequence csq) throws IOException {

        wrapper.println(csq);
        return this;
    }

    public PrintableAppendable println(CharSequence csq, int start, int end) throws IOException {

        wrapper.println(csq, start, end);
        return this;
    }

    public PrintableAppendable println(char[] c) throws IOException {

        wrapper.println(c);
        return this;
    }

    public PrintableAppendable println(char[] c, int off, int len) throws IOException {

        wrapper.println(c, off, len);
        return println();
    }

    public PrintableAppendable printf(String format, Object... args) throws IOException {

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
