/*
 * Copyright 2014 Jose Lopes
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
package net.fec.openrq.util.printing.appendable;


import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.Writer;
import java.nio.CharBuffer;


/**
 * 
 */
public final class QuietPrintableAppendable extends PrintableAppendable {

    public static QuietPrintableAppendable of(Appendable appendable, boolean printStackTrace) {

        if (appendable instanceof PrintStream) {
            return new QuietPrintableAppendable(new PrintStreamWrapper<>((PrintStream)appendable), printStackTrace);
        }
        else if (appendable instanceof PrintWriter) {
            return new QuietPrintableAppendable(new PrintWriterWrapper<>((PrintWriter)appendable), printStackTrace);
        }
        else if (appendable instanceof Writer) {
            return new QuietPrintableAppendable(new WriterWrapper<>((Writer)appendable), printStackTrace);
        }
        else if (appendable instanceof StringBuilder) {
            return new QuietPrintableAppendable(new StringBuilderWrapper((StringBuilder)appendable), printStackTrace);
        }
        else if (appendable instanceof StringBuffer) {
            return new QuietPrintableAppendable(new StringBufferWrapper((StringBuffer)appendable), printStackTrace);
        }
        else if (appendable instanceof CharBuffer) {
            return new QuietPrintableAppendable(new CharBufferWrapper<>((CharBuffer)appendable), printStackTrace);
        }
        else {
            return new QuietPrintableAppendable(new AppendableWrapper<>(appendable), printStackTrace);
        }
    }

    public static QuietPrintableAppendable ofNull() {

        return new QuietPrintableAppendable(new NullWrapper(), false);
    }


    private boolean printStackTrace;


    QuietPrintableAppendable(AppendableWrapper<?> wrapper, boolean printStackTrace) {

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
