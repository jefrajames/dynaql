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

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Properties;

/**
 *
 * @author jefrajames
 */
public class Client {

    private final HttpTimeout connectTimeout;
    private final HttpTimeout readTimeout;
    private final Properties properties;
    
    protected Client(HttpTimeout connectTimeout, HttpTimeout readTimeout) {
        this.connectTimeout = connectTimeout;
        this.readTimeout = readTimeout;
        this.properties = new Properties();
    }

    // Should be URI and operation
    public GraphQLTarget target(String uri) {

        URI target;

        if (uri == null || !uri.startsWith("http")) {
            throw new IllegalArgumentException("Illegal URI target value: " + uri);
        }

        try {
            target = new URI(uri);
        } catch (URISyntaxException ex) {
            throw new IllegalArgumentException("Illegal URI target value: " + uri);
        }

        return new GraphQLTarget(connectTimeout, readTimeout, target);
    }

    public void property(String key, Object value) {
        properties.put(key, value);
    }


}
