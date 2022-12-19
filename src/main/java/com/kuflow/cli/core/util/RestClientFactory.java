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
package com.kuflow.cli.core.util;

import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.core.http.policy.HttpLogOptions;
import com.kuflow.cli.core.model.EnvironmentProperties;
import com.kuflow.cli.core.model.EnvironmentProperties.KuFlowProperties;
import com.kuflow.rest.KuFlowRestClient;
import com.kuflow.rest.KuFlowRestClientBuilder;

public class RestClientFactory {

    public static KuFlowRestClient kuFlowRestClient(EnvironmentProperties properties) {
        KuFlowRestClientBuilder builder = new KuFlowRestClientBuilder();
        KuFlowProperties kuflow = properties.getKuflow();
        builder.clientId(kuflow.getClientId());
        builder.clientSecret(kuflow.getClientSecret());
        String endpoint = kuflow.getEndpoint();
        if (endpoint != null) {
            builder.endpoint(endpoint);
            builder.allowInsecureConnection(endpoint.startsWith("http://"));
        }

        HttpLogOptions logOptions = new HttpLogOptions();
        logOptions.setLogLevel(HttpLogDetailLevel.BODY_AND_HEADERS);
        builder.httpLogOptions(logOptions);

        return builder.buildClient();
    }
}
