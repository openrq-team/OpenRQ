package net.fec.openrq.util.io.printing.appendable;


import java.io.IOException;
import java.nio.CharBuffer;


/**
 * @author Ricardo Fonseca &lt;rfonseca&#064;lasige.di.fc.ul.pt&gt;
 * @param <C>
 */
final class CharBufferWrapper<C extends CharBuffer> extends AppendableWrapper<C> {

    public CharBufferWrapper(C cb) {

        super(cb);
    }

    @Override
    public void print(CharSequence csq) throws IOException {

        if (csq instanceof CharBuffer) appendable.put((CharBuffer)csq);
        else super.print(csq);
    }

    @Override
    public void print(char[] c, int off, int len) {

        appendable.put(c, off, len);
    }
}
