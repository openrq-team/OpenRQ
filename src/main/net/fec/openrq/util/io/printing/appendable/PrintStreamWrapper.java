package net.fec.openrq.util.io.printing.appendable;


import java.io.PrintStream;


/**
 * @author Ricardo Fonseca &lt;rfonseca&#064;lasige.di.fc.ul.pt&gt;
 * @param <P>
 */
final class PrintStreamWrapper<P extends PrintStream> extends AppendableWrapper<P> {

    public PrintStreamWrapper(P ps) {

        super(ps);
    }

    @Override
    public void print(char[] c) {

        appendable.print(c);
    }

    @Override
    public void println() {

        appendable.println();
    }

    @Override
    public void println(char c) {

        appendable.println(c);
    }

    @Override
    public void println(char[] c) {

        appendable.println(c);
    }

    @Override
    public void printf(String format, Object... args) {

        appendable.printf(format, args);
    }
}
