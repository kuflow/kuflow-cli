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
package com.kuflow.cli.core.model;

import com.kuflow.cli.core.util.Constants;
import com.kuflow.cli.core.util.StringUtils;

public class EnvironmentProperties {

    private KuFlowProperties kuflow = new KuFlowProperties();

    public KuFlowProperties getKuflow() {
        return this.kuflow;
    }

    public void setKuflow(KuFlowProperties kuflow) {
        this.kuflow = kuflow;
    }

    public static final class KuFlowProperties {

        private String endpoint = Constants.KUFLOW_REST_API_ENDPOINT;

        private String clientId;

        private String clientSecret;

        public String getEndpoint() {
            return this.endpoint;
        }

        public void setEndpoint(String endpoint) {
            this.endpoint = endpoint;
        }

        public String getClientId() {
            return this.clientId;
        }

        public void setClientId(String clientId) {
            this.clientId = clientId;
        }

        public String getClientSecret() {
            return this.clientSecret;
        }

        public void setClientSecret(String clientSecret) {
            this.clientSecret = clientSecret;
        }

        public boolean isFilled() {
            return this.endpoint != null && this.clientId != null && this.clientSecret != null;
        }

        @Override
        public String toString() {
            String hideSecret = (StringUtils.isBlank(this.clientSecret)) ? "" : "****";
            return "[endpoint=" + this.endpoint + ", clientId=" + this.clientId + ", clientSecret=" + hideSecret + "]";
        }
    }

    public EnvironmentProperties merge(EnvironmentProperties other) {
        KuFlowProperties otherKuFlowProperties = other.getKuflow();

        if (StringUtils.isBlank(this.getKuflow().getEndpoint())) {
            this.getKuflow().setEndpoint(otherKuFlowProperties.getEndpoint());
        }

        if (StringUtils.isBlank(this.getKuflow().getClientId())) {
            this.getKuflow().setClientId(otherKuFlowProperties.getClientId());
        }

        if (StringUtils.isBlank(this.getKuflow().getClientSecret())) {
            this.getKuflow().setClientSecret(otherKuFlowProperties.getClientSecret());
        }

        return this;
    }
}
