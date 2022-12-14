/*
 * Copyright (c) 2021-present KuFlow S.L.
 *
 * All rights reserved.
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
