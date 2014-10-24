package net.fec.openrq.util.io.printing;


import java.io.IOException;
import java.nio.CharBuffer;
import java.util.Formatter;
import java.util.Objects;

import net.fec.openrq.util.checking.Invariants;
import net.fec.openrq.util.io.printing.appendable.NoisyPrintableAppendable;


/**
 * This class provides support for writing text lines to an {@code Appendable} object, according to specific indentation
 * rules.
 * 
 * @author Ricardo Fonseca &lt;rfonseca&#064;lasige.di.fc.ul.pt&gt;
 */
public final class Indenter {

    public static Indenter create(Appendable app, int maxLineWidth, int maxIndentLevel) {

        return new Indenter(app, maxLineWidth, maxIndentLevel, DEFAULT_PREFIX, DEFAULT_INDENTATION_CHARACTER);
    }

    public static Indenter create(Appendable app, int maxLineWidth, int maxIndentLevel, String prefix) {

        return new Indenter(app, maxLineWidth, maxIndentLevel, prefix, DEFAULT_INDENTATION_CHARACTER);
    }

    public static Indenter create(Appendable app, int maxLineWidth, int maxIndentLevel, char indChar) {

        return new Indenter(app, maxLineWidth, maxIndentLevel, DEFAULT_PREFIX, indChar);
    }

    public static Indenter create(Appendable app, int maxLineWidth, int maxIndentLevel, String prefix, char indChar) {

        return new Indenter(app, maxLineWidth, maxIndentLevel, prefix, indChar);
    }


    public static final String DEFAULT_PREFIX = "";
    public static final char DEFAULT_INDENTATION_CHARACTER = ' ';

    private final Appendable originalAppendable;
    private final NoisyPrintableAppendable output;

    private final int maxLineWidth;
    private final int maxIndentLevel;
    private final String prefix;
    private final char indentChar;

    private int nextIndentLevel;


    private Indenter(Appendable app, int maxLineWidth, int maxIndentLevel, String prefix, char indentChar) {

        this.originalAppendable = Objects.requireNonNull(app);
        this.output = NoisyPrintableAppendable.of(app);

        validateMaxParams(maxLineWidth, maxIndentLevel);
        validatePrefix(prefix, maxLineWidth, maxIndentLevel);

        this.maxLineWidth = maxLineWidth;
        this.maxIndentLevel = maxIndentLevel;
        this.prefix = (prefix == null) ? "" : prefix;
        this.indentChar = Character.isWhitespace(indentChar) ? ' ' : indentChar;

        this.nextIndentLevel = 0;
    }

    private static void validateMaxParams(int maxLineWidth, int maxIndentLevel) {

        if (maxLineWidth < 0) {
            throw new IllegalArgumentException("maxLineWidth < 0");
        }
        if (maxIndentLevel < 0) {
            throw new IllegalArgumentException("maxIndentLevel < 0");
        }
        if (maxIndentLevel >= maxLineWidth) {
            throw new IllegalArgumentException("maxIndentLevel >= maxLineWidth");
        }
    }

    // requires valid maxParams
    private static void validatePrefix(String prefix, int maxLineWidth, int maxIndentLevel) {

        if (prefix != null) {
            if (prefix.length() >= maxLineWidth - maxIndentLevel) {
                throw new IllegalArgumentException("prefix is too large");
            }
        }
    }

    public Appendable getAppendable() {

        return originalAppendable;
    }

    public int getMaxLineWidth() {

        return maxLineWidth;
    }

    public int getMaxIndentationLevel() {

        return maxIndentLevel;
    }

    public String getPrefix() {

        return prefix;
    }

    public char getIndentationCharacter() {

        return indentChar;
    }

    public Indenter println(boolean incIndentLevel) throws IOException {

        printPrefix();
        printIndent();
        output.println();

        if (incIndentLevel) {
            advanceIndentLevel();
        }
        return this;
    }

    public Indenter println(boolean incIndentLevel, CharSequence csq) throws IOException {

        return println(incIndentLevel, new CharSequenceWrapper(csq));
    }

    public Indenter println(boolean incIndentLevel, CharSequence csq, int start, int end) throws IOException {

        return println(incIndentLevel, new CharSequenceWrapper(csq, start, end));
    }

    public Indenter println(boolean incIndentLevel, char[] c) throws IOException {

        return println(incIndentLevel, new CharArrayWrapper(c));
    }

    public Indenter println(boolean incIndentLevel, char[] c, int off, int len) throws IOException {

        return println(incIndentLevel, new CharArrayWrapper(c, off, len));
    }

    public Indenter printf(boolean incIndentLevel, String format, Object... args) throws IOException {

        final StringBuilder sb = new StringBuilder();
        try (final Formatter f = new Formatter(sb)) {
            f.format(format, args);
            return println(incIndentLevel, sb);
        }
    }

    public Indenter resetIndentationLevel() {

        this.nextIndentLevel = 0;
        return this;
    }

    private Indenter println(boolean incIndentLevel, CharSequenceOrArray input) throws IOException {

        if (!input.hasRemaining()) {
            return println(incIndentLevel);
        }

        final int startOff = getLineStartOffset();
        final int lineRemaining = maxLineWidth - startOff;
        Invariants.assertInvariants(lineRemaining >= 1);
        final LineBuffer lineBuf = new LineBuffer(output, lineRemaining);

        // read char by char into the line buffer and do a specific flush if any newline is found
        char lastChar = '\0';
        boolean requiresPrefixAndIndent = true;

        while (input.hasRemaining()) {

            if (requiresPrefixAndIndent) {
                printPrefix();
                printIndent();
                requiresPrefixAndIndent = false;
            }

            final char c = input.readNextChar();
            if (c == '\r') {
                lineBuf.flush();
                requiresPrefixAndIndent = true;
            }
            else if (c == '\n') {
                // do not print two newlines on CRLF
                if (lastChar != '\r') {
                    lineBuf.flush();
                    requiresPrefixAndIndent = true;
                }
            }
            else {
                requiresPrefixAndIndent = lineBuf.writeChar(c);
            }

            lastChar = c;
        }

        // take care of any leftover characters inside the buffer
        if (!lineBuf.isEmpty()) {

            if (requiresPrefixAndIndent) {
                printPrefix();
                printIndent();
            }
            lineBuf.flush();

            while (!lineBuf.isEmpty()) {
                printPrefix();
                printIndent();
                lineBuf.flush();
            }
        }

        if (incIndentLevel) {
            advanceIndentLevel();
        }
        return this;
    }

    private int getLineStartOffset() {

        return prefix.length() + nextIndentLevel;
    }

    private void printPrefix() throws IOException {

        if (!prefix.isEmpty()) {
            output.print(prefix);
        }
    }

    private void printIndent() throws IOException {

        for (int i = 0; i < nextIndentLevel; i++) {
            output.print(indentChar);
        }
    }

    private void advanceIndentLevel() {

        final int nextLevel = this.nextIndentLevel;
        if (nextLevel < maxIndentLevel) {
            this.nextIndentLevel = nextLevel + 1;
        }
    }


    private static final class LineBuffer {

        private final NoisyPrintableAppendable output;
        private final StringBuilder mainBuf;
        private final CharBuffer tempBuf;

        private int nextLinePos;
        private int lastWordEndPos;
        private char lastChar;


        LineBuffer(NoisyPrintableAppendable output, int lineWidth) {

            this.output = output;
            this.mainBuf = new StringBuilder(lineWidth);
            this.tempBuf = CharBuffer.allocate(lineWidth);

            this.nextLinePos = 0;
            this.lastWordEndPos = -1;
            this.lastChar = '\0';
        }

        int getLineWidth() {

            return mainBuf.capacity();
        }

        boolean isEmpty() {

            return mainBuf.length() == 0;
        }

        /**
         * @param c
         * @return {@code true} if a newline was flushed to the output
         * @throws IOException
         */
        boolean writeChar(char c) throws IOException {

            if (Character.isWhitespace(c)) {
                return writeSpaceCharacter(' '); // all whitespace characters are converted to ' '
            }
            else if (!Character.isISOControl(c)) { // ignore some non-printable characters
                return writeWordCharacter(c);
            }
            else {
                return false;
            }
        }

        private boolean writeSpaceCharacter(char s) throws IOException {

            // duplicate spaces or at the beginning of a line are ignored
            if (!(lastChar == s || nextLinePos == 0)) {
                if (nextLinePos < getLineWidth()) {
                    mainBuf.append(s);
                    tempBuf.clear(); // the last word is safe inside the main buffer
                    lastWordEndPos = nextLinePos - 1;
                    nextLinePos++;
                    lastChar = s;
                }
                else {
                    flush(); // also updates lastChar, nextLinePos and lastWordEndPos
                    return true;
                }
            }

            return false;
        }

        private boolean writeWordCharacter(char c) throws IOException {

            if (nextLinePos < getLineWidth()) {
                mainBuf.append(c);
                if (lastWordEndPos != -1) { // only write to temp buffer if at least one word is inside the main buffer
                    tempBuf.append(c);
                }
                nextLinePos++;
                lastChar = c;
            }
            else {
                tempBuf.append(c);
                flush(); // also updates lastChar, nextLinePos and lastWordEndPos
                return true;
            }

            return false;
        }

        void flush() throws IOException {

            if (lastWordEndPos == -1) {
                // One of two things can happen here:
                // 1) The temp buffer is empty and the only characters (if any) are in the main buffer
                // OR
                // 2) The temp buffer has one char and the main buffer is full with one word the size of the lineWidth
                output.println(mainBuf);
            }
            else {
                final int tempPos = tempBuf.position();
                if (0 < tempPos && tempPos < (getLineWidth() - lastWordEndPos - 1)) {
                    output.print(mainBuf, 0, lastWordEndPos + 1).print(' ');
                    tempBuf.flip();
                    output.print(tempBuf);
                    tempBuf.clear();
                    output.println();
                }
                else {
                    output.println(mainBuf, 0, lastWordEndPos + 1);
                }
            }

            mainBuf.setLength(0);
            tempBuf.flip();
            mainBuf.append(tempBuf.array(), tempBuf.position() + tempBuf.arrayOffset(), tempBuf.remaining());
            tempBuf.clear();

            lastWordEndPos = -1;
            final int nextPos = nextLinePos = mainBuf.length();
            lastChar = (nextPos > 0) ? mainBuf.charAt(nextPos - 1) : '\0';
        }
    }

    private static abstract class CharSequenceOrArray {

        private final int finalIndex;
        private int nextReadIndex;


        protected CharSequenceOrArray(int start, int end) {

            if (start < 0 || end < start) {
                throw new IndexOutOfBoundsException();
            }

            this.finalIndex = end;
            this.nextReadIndex = start;
        }

        final boolean hasRemaining() {

            return nextReadIndex < finalIndex;
        }

        final char readNextChar() {

            if (!hasRemaining()) {
                throw new IllegalStateException("no more characters remaining");
            }

            final int next = this.nextReadIndex;
            final char c = readCharAt(next);
            this.nextReadIndex = next + 1;
            return c;
        }

        abstract char readCharAt(int index);
    }

    private static final class CharSequenceWrapper extends CharSequenceOrArray {

        private final CharSequence csq;


        CharSequenceWrapper(CharSequence csq) {

            this(csq, 0, csq.length());
        }

        CharSequenceWrapper(CharSequence csq, int start, int end) {

            super(start, end); // if returns normally, then: 0 <= start AND start <= end
            if (csq.length() < end) {
                throw new IndexOutOfBoundsException();
            }

            this.csq = csq;
        }

        @Override
        char readCharAt(int index) {

            return csq.charAt(index);
        }
    }

    private static final class CharArrayWrapper extends CharSequenceOrArray {

        private final char[] array;


        CharArrayWrapper(char[] array) {

            this(array, 0, array.length);
        }

        CharArrayWrapper(char[] array, int off, int len) {

            super(off, off + len); // if returns normally, then: off >= 0 AND len >= 0 AND (off + len) does not overflow
            if (off + len > array.length) {
                throw new IndexOutOfBoundsException();
            }

            this.array = array;
        }

        @Override
        char readCharAt(int index) {

            return array[index];
        }
    }
}
