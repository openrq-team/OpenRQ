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

/**
 * 
 */
final class NullWrapper extends AppendableWrapper<NullWrapper.NullAppendable> {

    static final class NullAppendable implements Appendable {

        @Override
        @SuppressWarnings("unused")
        public Appendable append(CharSequence csq) {

            return this;
        }

        @Override
        @SuppressWarnings("unused")
        public Appendable append(CharSequence csq, int start, int end) {

            return this;
        }

        @Override
        @SuppressWarnings("unused")
        public Appendable append(char c) {

            return this;
        }
    }


    private static NullAppendable NA_INSTANCE = new NullAppendable();


    public NullWrapper() {

        super(NA_INSTANCE);
    }

    @Override
    @SuppressWarnings("unused")
    public void printf(String format, Object... args) {

        // do nothing
    }
}
