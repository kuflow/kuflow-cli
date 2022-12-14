/*
 * Copyright (c) 2021-present KuFlow S.L.
 *
 * All rights reserved.
 */

package com.kuflow.cli.core.model;

public class EnvironmentProperties {

  private String endpoint;

  private String clientId;

  private String clientSecret;

  public EnvironmentProperties(
    String endpoint,
    String clientId,
    String clientSecret
  ) {
    this.endpoint = endpoint;
    this.clientId = clientId;
    this.clientSecret = clientSecret;
  }

  public String getEndpoint() {
    return this.endpoint;
  }

  public String getClientId() {
    return this.clientId;
  }

  public String getClientSecret() {
    return this.clientSecret;
  }
}
