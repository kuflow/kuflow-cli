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

public enum ElementDefinitionType {
    FORM(Key.FORM),
    DOCUMENT(Key.DOCUMENT),
    DECISION(Key.DECISION),
    FIELD(Key.FIELD),
    PRINCIPAL(Key.PRINCIPAL);

    private final String name;

    ElementDefinitionType(String name) {
        this.name = name;
    }

    public String getName() {
        return this.name;
    }

    public static class Key {

        public static final String FORM = "FORM";
        public static final String DOCUMENT = "DOCUMENT";
        public static final String DECISION = "DECISION";
        public static final String FIELD = "FIELD";
        public static final String PRINCIPAL = "PRINCIPAL";
    }
}
