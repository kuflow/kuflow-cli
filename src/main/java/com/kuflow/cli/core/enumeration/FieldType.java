/*
 * Copyright (c) 2021-present KuFlow S.L.
 *
 * All rights reserved.
 */

package com.kuflow.cli.core.enumeration;

public enum FieldType {
  STRING(Key.STRING),
  NUMBER(Key.NUMBER),
  DATE(Key.DATE);

  private final String name;

  FieldType(String name) {
    this.name = name;
  }

  public String getName() {
    return this.name;
  }

  public static class Key {

    public static final String STRING = "STRING";
    public static final String NUMBER = "NUMBER";
    public static final String DATE = "DATE";
  }
}
