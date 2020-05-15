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

/**
 *
 * @author jefrajames
 */
public class Configuration {
    
    public static final String HTTP_PROXY_NAME="http.proxy.hostname";
    public static final String HTTP_PROXY_PORT="http.proxy.port";
    
    // The time to establish connection with the remote host
    public static final String HTTP_CONNECT_TIMEOUT="http.connect.timeout";
    
    // The time waiting for data
    public static final String HTTP_READ_TIMEOUT="http.read.timeout";
    
    // The time waiting a connection from the pool/manager (specific to HTTP client)
    public static final String HTTP_CONNECTION_MANAGER_TIMEOUT="http.connection.manager.timeout";
    
    private final Properties properties = new Properties();
    
    public void property(String key, Object value) {
        properties.put(key, value);
    }
    
    public Object get(String key) {
        return properties.get(key);
    }

    public Properties getProperties() {
        return properties;
    }
    
}
