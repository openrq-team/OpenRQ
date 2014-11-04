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
package net.fec.openrq;


import java.util.Objects;


/**
 * A container object which may or may not contain a non-null parsed value.
 * 
 * @param <V>
 *            The type of the parsed value object
 */
public abstract class Parsed<V> {

    /**
     * Returns a new parsing container object, containing the provided value.
     * 
     * @param value
     *            A valid parsed object
     * @return a new parsing container object, containing the provided value
     * @exception NullPointerException
     *                If {@code value} is {@code null}
     */
    public static <T> Parsed<T> of(T value) {

        return new ParsedValue<>(value);
    }

    /**
     * Returns a new parsing container object. The value within will be invalid, and a {@code ParsingFailureException}
     * with the provided reason message for the parsing failure will always be thrown if attempting to retrieve the
     * non-existent value.
     * 
     * @param failureReason
     *            A message detailing the reason for the parsing failure
     * @return a new parsing container object with an invalid value
     */
    public static <T> Parsed<T> invalid(String failureReason) {

        return new InvalidValue<>(failureReason);
    }

    /**
     * Returns {@code true} if, and only if, the parsed value is valid.
     * 
     * @return {@code true} if, and only if, the parsed value is valid
     */
    public abstract boolean isValid();

    /**
     * Returns the parsed value, if it is valid, otherwise the method throws a {@code ParsingFailureException} with a
     * message containing the {@linkplain #failureReason() failure reason}.
     * <p>
     * The method {@link #isValid()} can be used to test the validity of the parsed value.
     * 
     * @return the parsed value, if it is valid
     * @exception ParsingFailureException
     *                If the parsed value is invalid
     */
    public abstract V value();

    /**
     * If the parsed value is invalid, this method returns a string indicating the reason for the parsing failure,
     * otherwise an empty string is returned.
     * <p>
     * The method {@link #isValid()} can be used to test the validity of the parsed value.
     * 
     * @return a string indicating the reason for the parsing failure, or an empty string if the parsing succeeded
     */
    public abstract String failureReason();

    private Parsed() {

        // private constructor to prevent external sub-classing
    }


    private static final class ParsedValue<T> extends Parsed<T> {

        private final T value;


        private ParsedValue(T value) {

            this.value = Objects.requireNonNull(value);
        }

        @Override
        public boolean isValid() {

            return true;
        }

        @Override
        public T value() {

            return value;
        }

        @Override
        public String failureReason() {

            return "";
        }
    }

    private static final class InvalidValue<T> extends Parsed<T> {

        private final String failureReason;


        private InvalidValue(String failureReason) {

            this.failureReason = Objects.requireNonNull(failureReason);
        }

        @Override
        public boolean isValid() {

            return false;
        }

        @Override
        public T value() {

            throw new ParsingFailureException(failureReason);
        }

        @Override
        public String failureReason() {

            return failureReason;
        }
    }
}
