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

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Properties;
import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

/**
 *
 * @author jefrajames
 */
public class ConfigurationTest {

    private static Properties CONFIG = new Properties();
    private static String endpoint;

    @BeforeAll
    public static void beforeClass() throws MalformedURLException, IOException {
        CONFIG.load(ClientTest.class.getClassLoader().getResourceAsStream("graphql-config.properties"));
        endpoint = CONFIG.getProperty("endpoint");
    }

    
    
    @Test
    public void testTimeoutInvocation() throws IOException {
        Client client = ClientBuilder.newBuilder().connectTimeout(500).build();

        GraphQLTarget target = client.target(endpoint);
        
        Invocation invocation1 = target.request("GraphQL request here").build();
        assertEquals(invocation1.getConfiguration(Configuration.HTTP_CONNECT_TIMEOUT), 500L);
            
        Invocation invocation2 = target
                .property(Configuration.HTTP_CONNECT_TIMEOUT, 1000)
                .request("GraphQL request here")
                .build();
        assertEquals(invocation2.getConfiguration(Configuration.HTTP_CONNECT_TIMEOUT), 1000);
    }

}
