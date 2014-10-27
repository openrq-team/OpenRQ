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
package net.fec.openrq.util.text;

/**
 * 
 */
public final class Words {

    /**
     * Returns {@link #quantify(int, String) quantify(amount, "byte")}.
     * 
     * @param amount
     *            How many bytes
     * @return {@code quantify(amount, "byte")}
     */
    public static String bytes(int amount) {

        return quantify(amount, "byte");
    }

    /**
     * Returns {@code amount + " " + } {@link #pluralize(int, String) pluralize(amount, baseWord)}.
     * 
     * @param amount
     *            How many instances of the base word
     * @param baseWord
     *            The word to be quantified
     * @return {@code amount + " " + pluralize(amount, baseWord)}
     */
    public static String quantify(int amount, String baseWord) {

        return amount + " " + pluralize(amount, baseWord);
    }

    /**
     * Returns {@code amount + " " + } {@link #pluralize(int, String, String) pluralize(amount, baseWord, plural)}.
     * 
     * @param amount
     *            How many instances of the base word
     * @param baseWord
     *            The word to be quantified
     * @param plural
     *            The plural form of the base word
     * @return {@code amount + " " + pluralize(amount, baseWord, plural)}
     */
    public static String quantify(int amount, String baseWord, String plural) {

        return amount + " " + pluralize(amount, baseWord, plural);
    }

    /**
     * Returns a provided word in its plural or singular form, depending on the provided amount value (it is singular if
     * the amount value equals to 1, or -1; it is plural otherwise).
     * 
     * @param amount
     *            How many instances of the base word
     * @param baseWord
     *            The word to be pluralized
     * @return {@code (baseWord  + "s")} if {@code amount} is 1 or -1, or {@code baseWord} otherwise
     */
    public static String pluralize(int amount, String baseWord) {

        return (amount == 1 || amount == -1) ? baseWord : baseWord + "s";
    }

    /**
     * Returns a provided word in its plural or singular form, depending on the provided amount value (it is singular if
     * the amount value equals to 1, or -1; it is plural otherwise).
     * <p>
     * The plural form is provided as a separate argument.
     * 
     * @param amount
     *            How many instances of the base word
     * @param baseWord
     *            The word to be pluralized
     * @param plural
     *            The plural form of the base word
     * @return {@code plural} if {@code amount} is 1 or -1, or {@code baseWord} otherwise
     */
    public static String pluralize(int amount, String baseWord, String plural) {

        return (amount == 1 || amount == -1) ? baseWord : plural;
    }

    private Words() {

        // not instantiable
    }
}
