/*
 * Copyright (c) 2021-present KuFlow S.L.
 *
 * All rights reserved.
 */

package com.kuflow.cli.core.enumeration;

public enum CommandType {
  SAVE_ELEMENT(Key.SAVE_ELEMENT),
  SAVE_ELEMENT__FIELD(Key.SAVE_ELEMENT__FIELD),
  SAVE_ELEMENT__DOCUMENT(Key.SAVE_ELEMENT__DOCUMENT);

  private final String name;

  CommandType(String name) {
    this.name = name;
  }

  public String getName() {
    return this.name;
  }

  public static class Key {

    public static final String SAVE_ELEMENT = "save-element";
    public static final String SAVE_ELEMENT__DOCUMENT = "document";
    public static final String SAVE_ELEMENT__FIELD = "field";
  }
}
