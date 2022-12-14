/*
 * The MIT License
 * Copyright © 2021-present KuFlow S.L.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package com.kuflow.cli.core.enumeration;

public enum CommandType {
    SAVE_ELEMENT_FIELD(Key.SAVE_ELEMENT_FIELD),
    SAVE_ELEMENT_DOCUMENT(Key.SAVE_ELEMENT_DOCUMENT),
    SAVE_ELEMENT_DOCUMENT_BY_REFERENCE(Key.SAVE_ELEMENT_DOCUMENT_BY_REFERENCE),
    APPEND_LOG(Key.APPEND_LOG),
    SAVE_ELEMENT_PRINCIPAL(Key.SAVE_ELEMENT_PRINCIPAL);

    private final String name;

    CommandType(String name) {
        this.name = name;
    }

    public String getName() {
        return this.name;
    }

    public static class Key {

        public static final String SAVE_ELEMENT_DOCUMENT = "save-element-document";
        public static final String SAVE_ELEMENT_DOCUMENT_BY_REFERENCE = "save-element-document-by-reference";
        public static final String SAVE_ELEMENT_FIELD = "save-element-field";
        public static final String SAVE_ELEMENT_PRINCIPAL = "save-element-principal";
        public static final String APPEND_LOG = "append-log";
    }
}
