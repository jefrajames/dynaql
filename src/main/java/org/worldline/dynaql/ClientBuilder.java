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

/**
 *
 * @author jefrajames
 */
public class ClientBuilder {

    private final Configuration configuration = new Configuration();
    
    public static ClientBuilder newBuilder() {
        return new ClientBuilder();
    }

    public ClientBuilder connectTimeout(long connectTimeout) {
        if ( connectTimeout<=0 )
            throw new IllegalArgumentException("Timeout value should be positive " + connectTimeout);
        
        configuration.property(Configuration.HTTP_CONNECT_TIMEOUT, connectTimeout);
        return this;
    }

    public ClientBuilder readTimeout(long readTimeout) {
        if ( readTimeout<=0 )
            throw new IllegalArgumentException("Timeout value should be positive " + readTimeout);
        
        configuration.property(Configuration.HTTP_READ_TIMEOUT, readTimeout);
        return this;
    }

    // Build a Client
    public Client build() {
        return new Client(configuration);
    }


    public ClientBuilder property(String key, Object value) {
        configuration.property(key, value);
        return this;
    }
    
    public Object getConfiguration(String key) {
        return configuration.get(key);
    } 

}
