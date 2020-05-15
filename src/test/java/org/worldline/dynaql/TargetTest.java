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
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

/**
 *
 * @author jefrajames
 */
public class TargetTest {

    private static Properties CONFIG = new Properties();
    private static String endpoint;

    @BeforeAll
    public static void beforeClass() throws MalformedURLException, IOException {
        CONFIG.load(ClientTest.class.getClassLoader().getResourceAsStream("graphql-config.properties"));
        endpoint = CONFIG.getProperty("endpoint");
    }

    @Test
    public void testURI() {
        Client client = ClientBuilder.newBuilder().build();
        assertNotNull(client);

        GraphQLTarget target = client.target(CONFIG.getProperty("endpoint"));
        assertNotNull(target);
    }

    @Test
    public void testBadURI() {
        assertThrows(IllegalArgumentException.class, () -> {
            ClientBuilder.newBuilder().build().target("123");
        });
    }

    @Test
    public void testTimeoutConfig() throws IOException {
        Client client = ClientBuilder.newBuilder().connectTimeout(500).build();
        assertEquals(client.getConfiguration(Configuration.HTTP_CONNECT_TIMEOUT), 500L);

        GraphQLTarget target = client.target(endpoint);
        assertEquals(target.getConfiguration(Configuration.HTTP_CONNECT_TIMEOUT), 500L);

        target.property(Configuration.HTTP_CONNECT_TIMEOUT, 1000);
        assertEquals(target.getConfiguration(Configuration.HTTP_CONNECT_TIMEOUT), 1000);
    }

    @Test
    public void testProxyConfig() throws IOException {
        Client client = ClientBuilder.newBuilder().build();
        client.property(Configuration.HTTP_PROXY_NAME, "localhost").property(Configuration.HTTP_PROXY_PORT, 8080);
        assertEquals(client.getConfiguration(Configuration.HTTP_PROXY_NAME), "localhost");
        assertEquals(client.getConfiguration(Configuration.HTTP_PROXY_PORT), 8080);

        GraphQLTarget target = client.target(endpoint);
        assertEquals(target.getConfiguration(Configuration.HTTP_PROXY_NAME), "localhost");
        assertEquals(target.getConfiguration(Configuration.HTTP_PROXY_PORT), 8080);
        
        GraphQLTarget badHostTarget = client.target(endpoint);
        badHostTarget.property(Configuration.HTTP_PROXY_NAME, "otherhost").property(Configuration.HTTP_PROXY_PORT, 8080);
        assertEquals(target.getConfiguration(Configuration.HTTP_PROXY_NAME), "otherhost");
        assertEquals(target.getConfiguration(Configuration.HTTP_PROXY_PORT), 8080);
        assertThrows(RuntimeException.class, () -> {
             badHostTarget.request("GraphQL request here").invoke();
        });
        
        GraphQLTarget badPortTarget = client.target(endpoint);
        badPortTarget.property(Configuration.HTTP_PROXY_NAME, "localhost").property(Configuration.HTTP_PROXY_PORT, 1234);
        assertEquals(target.getConfiguration(Configuration.HTTP_PROXY_NAME), "localhost");
        assertEquals(target.getConfiguration(Configuration.HTTP_PROXY_PORT), 1234);
        assertThrows(RuntimeException.class, () -> {
             badPortTarget.request("GraphQL request here").invoke();
        });
     
    }

}
