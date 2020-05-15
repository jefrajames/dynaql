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
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author jefrajames
 */
public class InvocationBuilder {

    private final Configuration configuration;
    private final URI uri;
    private final String request;
    private final Map<String, Object> variables; // TODO: manage non String variables
    private final Map<String, String> headers;

    public InvocationBuilder(Configuration configuration, URI uri, String request) {
        this.configuration = configuration;
        this.uri = uri;
        this.request = request;
        this.variables = new HashMap<>();
        this.headers = new HashMap<>();
    }

    
    public InvocationBuilder variable(String name, Object value) {
        variables.put(name, value);
        return this;
    }
    
     public InvocationBuilder header(String name, String value) {
        headers.put(name, value);
        return this;
    }
     
     public Invocation build() {
         return new Invocation(configuration, uri, request, variables, headers);
     }
     
     public Response invoke() {
         return new Invocation(configuration, uri, request, variables, headers).invoke();
     }
     
      public Object getConfiguration(String key) {
        return configuration.get(key);
    } 
     
}
