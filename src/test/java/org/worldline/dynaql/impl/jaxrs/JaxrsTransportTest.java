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
package org.worldline.dynaql.impl.jaxrs;

import org.worldline.dynaql.impl.entity.Profile;
import org.worldline.dynaql.impl.entity.Person;
import io.quarkus.test.junit.QuarkusTest;
import java.io.IOException;
import java.net.MalformedURLException;
import java.time.LocalDate;
import java.util.List;
import java.util.Properties;
import java.util.ServiceLoader;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.TimeUnit;
import javax.inject.Inject;
import javax.json.JsonObject;
import javax.ws.rs.ProcessingException;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import static javax.ws.rs.client.Entity.json;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.worldline.dynaql.api.GraphQLRequest;
import org.worldline.dynaql.api.GraphQLResponse;
import org.worldline.dynaql.api.GraphQLClientBuilder;

/**
 *
 * @author jefrajames
 */
@QuarkusTest
public class JaxrsTransportTest {

    private static final Properties CONFIG = new Properties();
    private static String endpoint;
    private static ClientBuilder clientBuilder;
    
    @Inject
    GraphQLClientBuilder graphQLClientBuilder;

    @BeforeAll
    public static void beforeClass() throws MalformedURLException, IOException {

        CONFIG.load(JaxrsTransportTest.class.getClassLoader().getResourceAsStream("graphql-config.properties"));
        endpoint = CONFIG.getProperty("endpoint");

        clientBuilder = ClientBuilder
                .newBuilder()
                .register(GraphQLResponseReader.class)
                .register(GraphQLRequestWriter.class)
                .connectTimeout(5, TimeUnit.SECONDS)
                .readTimeout(2, TimeUnit.SECONDS);

    }

    @Test
    public void testQueryList() throws IOException {

        GraphQLRequest graphQLRequest = graphQLClientBuilder.newRequest(CONFIG.getProperty("allPeople"));

        Client client = clientBuilder.build();

        WebTarget target = client.target(endpoint);

        Response response = target.request(MediaType.APPLICATION_JSON).post(json(graphQLRequest));

        assertEquals(response.getStatus(), 200);

        GraphQLResponse graphQLResponse = response.readEntity(GraphQLResponse.class);
        assertTrue(graphQLResponse.hasData());
        assertFalse(graphQLResponse.hasError());

        List<Person> people = graphQLResponse.getList(Person.class, "people");
        assertTrue(people.size() >= 100);

        client.close();
    }

    @Test
    public void testGraphQLErrors() throws IOException {

        GraphQLRequest graphQLRequest = graphQLClientBuilder.newRequest(CONFIG.getProperty("allPeopleWithErrors"));

        Client client = clientBuilder.build();

        Response response = client
                .target(endpoint)
                .request(MediaType.APPLICATION_JSON)
                // Same as .post(json(graphQLRequest))
                .post(Entity.entity(graphQLRequest, MediaType.APPLICATION_JSON));

        assertEquals(response.getStatus(), 200);

        GraphQLResponse graphQLResponse = response.readEntity(GraphQLResponse.class);

        assertFalse(graphQLResponse.hasData());
        assertTrue(graphQLResponse.hasError());
        graphQLResponse.getErrors().forEach(System.out::println);
        assertEquals(graphQLResponse.getErrors().size(), 3);

        client.close();
    }

    @Test
    public void testHeader() throws IOException {

        GraphQLRequest graphQLRequest = graphQLClientBuilder.newRequest(CONFIG.getProperty("personById"));

        Client client = clientBuilder.build();

        Response response = client
                .target(endpoint)
                .request(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer: JWT")
                .post(json(graphQLRequest));

        assertEquals(response.getStatus(), 200);

        GraphQLResponse graphQLResponse = response.readEntity(GraphQLResponse.class);

        assertTrue(graphQLResponse.hasData());
        assertFalse(graphQLResponse.hasError());
    }

    @Test
    public void testStringVariable() {

        GraphQLRequest graphQLRequest = graphQLClientBuilder
                .newRequest(CONFIG.getProperty("queryWithStringVariable"))
                .addVariable("surname", "Senger");

        Client client = clientBuilder.build();

        // Here, we directly get a GraphQLResponse typed entity
        GraphQLResponse graphQLResponse = client
                .target(endpoint)
                .request(MediaType.APPLICATION_JSON)
                .post(json(graphQLRequest), GraphQLResponse.class);

        assertFalse(graphQLResponse.hasError());
        assertTrue(graphQLResponse.hasData());

        List<Person> people = graphQLResponse.getList(Person.class, "personsWithSurname");

        client.close();
    }

    @Test
    public void testIntVariable() {

        GraphQLRequest graphQLRequest = graphQLClientBuilder
                .newRequest(CONFIG.getProperty("queryWithIntVariable"))
                .addVariable("personId", 1);

        Client client = clientBuilder.build();

        Response response = client
                .target(endpoint)
                .request() // Here we don't specify that we expect application/json content
                .post(json(graphQLRequest));

        assertEquals(response.getStatus(), 200);

        GraphQLResponse graphQLResponse = response
                .readEntity(GraphQLResponse.class);

        assertFalse(graphQLResponse.hasError());
        assertTrue(graphQLResponse.hasData());

        JsonObject myData = graphQLResponse.getData();

        Profile profile = graphQLResponse.getObject(Profile.class, "profile");
        assertFalse(graphQLResponse.hasError());
        assertTrue(graphQLResponse.hasData());

        assertEquals(profile.getPerson().getId(), 1);

        List<Profile> resultAsList = graphQLResponse.getList(Profile.class, "profile");
        assertEquals(resultAsList.size(), 1);
        assertEquals(resultAsList.get(0).getPerson().getId(), 1);

        client.close();
    }

    @Test
    public void testCreatePerson() {

        GraphQLRequest graphQLRequest = graphQLClientBuilder.newRequest(CONFIG.getProperty("createPersonWithVariables"));
        graphQLRequest.addVariable("surname", "James");
        graphQLRequest.addVariable("names", "JF");
        graphQLRequest.addVariable("birthDate", "27/04/1962");

        Client client = clientBuilder.build();

        Response response = client
                .target(endpoint)
                .request(MediaType.APPLICATION_JSON) // Here we don't specify that we expect application/json content
                .post(json(graphQLRequest));

        assertEquals(response.getStatus(), 200);

        GraphQLResponse graphQLResponse = response
                .readEntity(GraphQLResponse.class);
        assertFalse(graphQLResponse.hasError());
        assertTrue(graphQLResponse.hasData());

        Person jfj = graphQLResponse.getObject(Person.class, "updatePerson");
        assertEquals(jfj.getSurname(), "James");
        assertEquals(jfj.getNames()[0], "JF");
        assertEquals(jfj.getBirthDate(), LocalDate.of(1962, 4, 27));

    }

    // No Proxy test here: not JAX-RS standard!
    @Test
    public void testTimeoutOK() {

        GraphQLRequest graphQLRequest = graphQLClientBuilder.newRequest(CONFIG.getProperty("allPeople"));

        Client client = clientBuilder
                .connectTimeout(200, TimeUnit.MILLISECONDS)
                .readTimeout(400, TimeUnit.MILLISECONDS)
                .build();

        Response response = client
                .target(endpoint)
                .request()
                .post(json(graphQLRequest));

        GraphQLResponse graphQLResponse = response.readEntity(GraphQLResponse.class);

        assertTrue(graphQLResponse.hasData());
        assertFalse(graphQLResponse.hasError());
    }

    @Test
    public void testTimeoutKO() {

        GraphQLRequest graphQLRequest = graphQLClientBuilder.newRequest(CONFIG.getProperty("allPeople"));

        Client client = clientBuilder
                .connectTimeout(1, TimeUnit.MILLISECONDS) // Unrealistic values here!
                .readTimeout(1, TimeUnit.MILLISECONDS)
                .build();

        assertThrows(ProcessingException.class, () -> {
            client
                    .target(endpoint)
                    .request()
                    .post(json(graphQLRequest));
        });

    }

    @Test
    public void testMissingVariable() {
        GraphQLRequest graphQLRequest = graphQLClientBuilder.newRequest(CONFIG.getProperty("queryWithStringVariable"));

        Client client = clientBuilder.build();

        Response response = client
                .target(endpoint)
                .request(MediaType.APPLICATION_JSON)
                .post(json(graphQLRequest.toJson()));

        assertEquals(response.getStatus(), 500);

        client.close();
    }

    @Test
    public void testReactiveCall() throws InterruptedException {

        GraphQLRequest graphQLRequest = graphQLClientBuilder.newRequest(CONFIG.getProperty("allPeople"));

        Client client = clientBuilder.build();

        CompletionStage<GraphQLResponse> csr = client
                .target(endpoint)
                .request()
                .rx()
                .post(json(graphQLRequest.toJson()), GraphQLResponse.class);

        Thread.sleep(2000);

        csr.thenAccept(r -> {
            assertTrue(r.hasData());
            assertFalse(r.hasError());
        });

    }

}
