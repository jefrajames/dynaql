/*
 * Copyright 2020 jefrajames.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.worldline.dynaql;

import java.util.Properties;
import java.util.concurrent.TimeUnit;

/**
 *
 * @author jefrajames
 */
public class ClientBuilder {

    private HttpTimeout connectTimeout;
    private HttpTimeout readTimeout;
    private final Properties properties = new Properties();

    public static ClientBuilder newBuilder() {
        return new ClientBuilder();
    }


    public ClientBuilder connectTimeout(long value, TimeUnit timeUnit) {
        this.connectTimeout = new HttpTimeout(value, timeUnit);
        return this;
    }

    public ClientBuilder readTimeout(long value, TimeUnit timeUnit) {
        this.readTimeout = new HttpTimeout(value, timeUnit);
        return this;
    }

    // Build a Client
    public Client build() {
        return new Client(connectTimeout, readTimeout);
    }

    public HttpTimeout getConnectTimeout() {
        return connectTimeout;
    }

    public HttpTimeout getReadTimeout() {
        return readTimeout;
    }

    public ClientBuilder property(String key, Object value) {
        properties.put(key, value);
        return this;
    }

}
