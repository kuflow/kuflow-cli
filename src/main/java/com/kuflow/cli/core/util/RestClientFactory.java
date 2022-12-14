/*
 * Copyright (c) 2021-present KuFlow S.L.
 *
 * All rights reserved.
 */

package com.kuflow.cli.core.util;

import com.kuflow.cli.core.model.EnvironmentProperties;
import com.kuflow.rest.KuFlowRestClient;
import com.kuflow.rest.KuFlowRestClientBuilder;

public class RestClientFactory {

    public static KuFlowRestClient kuFlowRestClient(EnvironmentProperties properties) {
        KuFlowRestClientBuilder builder = new KuFlowRestClientBuilder();
        builder.clientId(properties.getClientId());
        builder.clientSecret(properties.getClientSecret());
        String endpoint = properties.getEndpoint();
        if (endpoint != null) {
            builder.endpoint(endpoint);
            builder.allowInsecureConnection(endpoint.startsWith("http://"));
        }

        return builder.buildClient();
    }
}
