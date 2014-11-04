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

/**
 */
final class SingularMatrixException extends Exception {

    private static final long serialVersionUID = 1L;
    private static final String msg = "Matrix is singular, therefore not invertible.";


    public SingularMatrixException() {

        // no cause, suppression disabled and non-writable stack trace (for a lighter exception and higher performance)
        super(msg, null, false, false);
    }

    public SingularMatrixException(String message) {

        // no cause, suppression disabled and non-writable stack trace (for a lighter exception and higher performance)
        super(message, null, false, false);
    }
}
