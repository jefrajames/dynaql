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
import java.util.List;
import java.util.Properties;
import javax.json.JsonObject;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;

/**
 *
 * @author jefrajames
 */
public class InvocationTest {

    private static Properties CONFIG = new Properties();
    private static String endpoint;
    
    private static org.slf4j.Logger log = LoggerFactory.getLogger(InvocationTest.class);

    @BeforeAll
    public static void beforeClass() throws MalformedURLException, IOException {
        CONFIG.load(InvocationTest.class.getClassLoader().getResourceAsStream("graphql-config.properties"));
        endpoint = CONFIG.getProperty("endpoint");
        log.warn("endpoint=" + endpoint);
    }

    @Test
    public void testAllPeople() throws IOException {
        Client client = ClientBuilder.newBuilder().connectTimeout(500).readTimeout(1000).build();

        String request = CONFIG.getProperty("allPeople");
        Response response = client.target(endpoint).request(request).invoke();

        JsonObject data = response.getData();
        
        assertTrue(response.hasData());
        assertFalse(response.hasError());
    }

    @Test
    public void testAllPeopleWithError() throws IOException {
        Client client = ClientBuilder.newBuilder().build();

        String request = CONFIG.getProperty("allPeopleWithError");
        Response response = client.target(endpoint).request(request).invoke();

        assertFalse(response.hasData());
        assertTrue(response.hasError());
        response.getErrors().forEach(e -> log.warn("error=" + e));
        assertEquals(response.getErrors().size(), 1);
    }

    @Test
    public void testAllPeopleWithErrors() throws IOException {
        Client client = ClientBuilder.newBuilder().build();

        String request = CONFIG.getProperty("allPeopleWithErrors");
        Response response = client.target(endpoint).request(request).invoke();

        assertFalse(response.hasData());
        assertTrue(response.hasError());
        response.getErrors().forEach(e -> log.warn("error=" + e));
        assertEquals(response.getErrors().size(), 3);
    }

    @Test
    public void testBadQueryName() throws IOException {
        Client client = ClientBuilder.newBuilder().build();

        String request = CONFIG.getProperty("badQuery");
        Response response = client.target(endpoint).request(request).invoke();

        assertFalse(response.hasData());
        assertTrue(response.hasError());
        response.getErrors().forEach(e -> log.warn("error=" + e));
        assertEquals(response.getErrors().size(), 1);
    }

    @Test
    public void testFindById() throws IOException {
        Client client = ClientBuilder.newBuilder().build();

        String request = CONFIG.getProperty("personById");
        Response response = client.target(endpoint).request(request).invoke();

        assertTrue(response.hasData());
        assertFalse(response.hasError());
    }

    @Test
    public void testHeader() throws IOException {
        Client client = ClientBuilder.newBuilder().build();

        String request = CONFIG.getProperty("personById");
        Response response = client.target(endpoint).request(request).header("Authorization", "Bearer: JWT").invoke();

        assertTrue(response.hasData());
        assertFalse(response.hasError());
    }

    @Test
    public void testAsDataObject() throws IOException {
        Client client = ClientBuilder.newBuilder().build();

        String request = CONFIG.getProperty("personById");
        
        GraphQLTarget target =client.target(endpoint);
        
        Invocation invocation = target.request(request).build();

        Response response = invocation.invoke();

        assertTrue(response.hasData());
        assertFalse(response.hasError());

        PersonDTO person = response.asDataObject(PersonDTO.class, "person");
        assertEquals(person.getId(), 50);
    }
    
    @Test
    public void testUncompletePersonDTO() throws IOException {
        Client client = ClientBuilder.newBuilder().build();

        String request = CONFIG.getProperty("personById");
        
        GraphQLTarget target = client.target(endpoint);
        
        Invocation invocation = target.request(request).build();

        Response response = invocation.invoke();

        assertTrue(response.hasData());
        assertFalse(response.hasError());

        UncompletePersonDTO person = response.asDataObject(UncompletePersonDTO.class, "person");
        assertEquals(person.getId(), 50);
    }
    
    @Test
    public void testPersonWithAdditionalAttributesDTO() throws IOException {
        Client client = ClientBuilder.newBuilder().build();

        String request = CONFIG.getProperty("personById");
        
        GraphQLTarget target =client.target(endpoint);
        
        Invocation invocation = target.request(request).build();

        Response response = invocation.invoke();

        assertTrue(response.hasData());
        assertFalse(response.hasError());

        PersonWithAdditionalFieldsDTO person = response.asDataObject(PersonWithAdditionalFieldsDTO.class, "person");
        assertEquals(person.getId(), 50);
        assertNull(person.getStrength());
        assertEquals(person.getAddresses().get(0).getArea(),0);
    }

    @Test
    public void testAsDataList() throws IOException {
        Client client = ClientBuilder.newBuilder().build();

        String request = CONFIG.getProperty("allPeople");
        Response response = client.target(endpoint).request(request).invoke();

        assertTrue(response.hasData());
        assertFalse(response.hasError());
        
        List<PersonDTO> people = response.asDataList(PersonDTO.class, "people");
        assertTrue(people.size()>=100);

    }
    
     @Test
    public void testStringVariable() {

        Client client = ClientBuilder.newBuilder().build();

        String request = CONFIG.getProperty("queryWithStringVariable");
        
        GraphQLTarget target = client.target(endpoint);

        Response response = target.request(request).variable("surname", "Grant").invoke();
        assertFalse(response.hasError());
        assertTrue(response.hasData());
        
        assertThrows(RuntimeException.class, () -> {
            // Variable required here!
            target.request(request).invoke();
        });
    }

    @Test
    public void testIntVariable() {

        Client client = ClientBuilder.newBuilder().build();

        String request = CONFIG.getProperty("queryWithIntVariable");
        
        GraphQLTarget target = client.target(endpoint);
        
        Response response = target.request(request).variable("personId", 1).invoke();
        assertFalse(response.hasError());
        assertTrue(response.hasData());
        
        assertThrows(RuntimeException.class, () -> {
            // Variable required here!
            target.request(request).invoke();
        });

    }

}
