/*
 * Copyright 2014 OpenRQ Team
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
 * The methods from this class do <b>not</b> throw an {@code IOException} if they detect an I/O error from the
 * underlying {@code Appendable} object. Instead, they ignore the error, but may be configured to print an exception
 * stack trace to
 * the standard error stream.
 * 
 * @author Ricardo Fonseca &lt;rfonseca&#064;lasige.di.fc.ul.pt&gt;
 */
public final class PrintableAppendable extends NoisyPrintableAppendable {

    public static PrintableAppendable of(Appendable appendable, boolean printStackTrace) {

        Objects.requireNonNull(appendable);

        if (appendable instanceof PrintStream) {
            return new PrintableAppendable(new PrintStreamWrapper<>((PrintStream)appendable), printStackTrace);
        }
        else if (appendable instanceof PrintWriter) {
            return new PrintableAppendable(new PrintWriterWrapper<>((PrintWriter)appendable), printStackTrace);
        }
        else if (appendable instanceof Writer) {
            return new PrintableAppendable(new WriterWrapper<>((Writer)appendable), printStackTrace);
        }
        else if (appendable instanceof StringBuilder) {
            return new PrintableAppendable(new StringBuilderWrapper((StringBuilder)appendable), printStackTrace);
        }
        else if (appendable instanceof StringBuffer) {
            return new PrintableAppendable(new StringBufferWrapper((StringBuffer)appendable), printStackTrace);
        }
        else if (appendable instanceof CharBuffer) {
            return new PrintableAppendable(new CharBufferWrapper<>((CharBuffer)appendable), printStackTrace);
        }
        else {
            return new PrintableAppendable(new AppendableWrapper<>(appendable), printStackTrace);
        }
    }

    public static PrintableAppendable ofNull() {

        return new PrintableAppendable(new NullWrapper(), false);
    }


    private boolean printStackTrace;


    PrintableAppendable(AppendableWrapper<?> wrapper, boolean printStackTrace) {

        super(wrapper);
        this.printStackTrace = printStackTrace;
    }

    @Override
    public PrintableAppendable print(char c) {

        try {
            super.print(c);
        }
        catch (IOException e) {
            handleIOException(e);
        }

        return this;
    }

    @Override
    public PrintableAppendable print(char[] c) {

        try {
            super.print(c);
        }
        catch (IOException e) {
            handleIOException(e);
        }

        return this;
    }

    @Override
    public PrintableAppendable print(char[] c, int off, int len) {

        try {
            super.print(c, off, len);
        }
        catch (IOException e) {
            handleIOException(e);
        }

        return this;
    }

    @Override
    public PrintableAppendable print(CharSequence csq) {

        try {
            super.print(csq);
        }
        catch (IOException e) {
            handleIOException(e);
        }

        return this;
    }

    @Override
    public PrintableAppendable print(CharSequence csq, int start, int end) {

        try {
            super.print(csq, start, end);
        }
        catch (IOException e) {
            handleIOException(e);
        }

        return this;
    }

    @Override
    public PrintableAppendable printf(String format, Object... args) {

        try {
            super.printf(format, args);
        }
        catch (IOException e) {
            handleIOException(e);
        }

        return this;
    }

    @Override
    public PrintableAppendable println() {

        try {
            super.println();
        }
        catch (IOException e) {
            handleIOException(e);
        }

        return this;
    }

    @Override
    public PrintableAppendable println(char c) {

        try {
            super.println(c);
        }
        catch (IOException e) {
            handleIOException(e);
        }

        return this;
    }

    @Override
    public PrintableAppendable println(char[] c) {

        try {
            super.println(c);
        }
        catch (IOException e) {
            handleIOException(e);
        }

        return this;
    }

    @Override
    public PrintableAppendable println(char[] c, int off, int len) {

        try {
            super.println(c, off, len);
        }
        catch (IOException e) {
            handleIOException(e);
        }

        return this;
    }

    @Override
    public PrintableAppendable println(CharSequence csq) {

        try {
            super.println(csq);
        }
        catch (IOException e) {
            handleIOException(e);
        }

        return this;
    }

    @Override
    public PrintableAppendable println(CharSequence csq, int start, int end) {

        try {
            super.println(csq, start, end);
        }
        catch (IOException e) {
            handleIOException(e);
        }

        return this;
    }

    private void handleIOException(IOException e) {

        if (printStackTrace) {
            e.printStackTrace();
        }
    }
}
