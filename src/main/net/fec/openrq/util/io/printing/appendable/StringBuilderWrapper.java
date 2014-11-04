package net.fec.openrq.util.io.printing.appendable;

/**
 * @author Ricardo Fonseca &lt;rfonseca&#064;lasige.di.fc.ul.pt&gt;
 */
final class StringBuilderWrapper extends AppendableWrapper<StringBuilder> {

    public StringBuilderWrapper(StringBuilder sb) {

        super(sb);
    }

    @Override
    public void print(char[] c, int off, int len) {

        appendable.append(c, off, len);
    }
}
