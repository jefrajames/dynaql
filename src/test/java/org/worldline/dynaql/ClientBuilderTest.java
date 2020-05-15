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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.Test;

/**
 *
 * @author jefrajames
 */
public class ClientBuilderTest {

    @Test
    public void testTimeout() {
        ClientBuilder builder = ClientBuilder.newBuilder();
        builder.connectTimeout(500);
        builder.readTimeout(2000);
        builder.property(Configuration.HTTP_CONNECTION_MANAGER_TIMEOUT, 1500L);

        assertEquals(builder.getConfiguration(Configuration.HTTP_CONNECT_TIMEOUT), 500L);
        assertEquals(builder.getConfiguration(Configuration.HTTP_READ_TIMEOUT), 2000L);
        assertEquals(builder.getConfiguration(Configuration.HTTP_CONNECTION_MANAGER_TIMEOUT), 1500L);
    }

    @Test
    public void testProxy() {
        ClientBuilder builder = ClientBuilder.newBuilder();

        builder.property(Configuration.HTTP_PROXY_NAME, "localhost");
        builder.property(Configuration.HTTP_PROXY_PORT, 8080);

        assertEquals(builder.getConfiguration(Configuration.HTTP_PROXY_NAME), "localhost");
        assertEquals(builder.getConfiguration(Configuration.HTTP_PROXY_PORT), 8080);
    }

    @Test
    public void testIllegalTimeout() {
        assertThrows(IllegalArgumentException.class, () -> {
            ClientBuilder.newBuilder().connectTimeout(-1);
        });
    }

}
