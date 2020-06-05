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

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.json.bind.Jsonb;
import javax.json.bind.JsonbBuilder;
import javax.ws.rs.Consumes;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.Provider;
import org.slf4j.LoggerFactory;
import org.worldline.dynaql.api.GraphQLResponse;
import org.worldline.dynaql.impl.DynaQLResponse;
import org.worldline.dynaql.impl.DynaQLResponse.DynaQLError;
import org.worldline.dynaql.api.GraphQLResponse.GraphQLError;

/**
 * This is an implementation specific class and should not be in the
 * specification API.
 *
 * @author jefrajames
 */
@Provider
@Consumes("application/json")
public class GraphQLResponseReader implements MessageBodyReader<GraphQLResponse> {

    private static final int MAX_LOG_LENGTH = 128;

    private static final org.slf4j.Logger log = LoggerFactory.getLogger(GraphQLResponseReader.class);

    @Override
    public boolean isReadable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
        return type == GraphQLResponse.class;
    }

    @Override
    public DynaQLResponse readFrom(Class<GraphQLResponse> type, Type genericType, Annotation[] annotations,
            MediaType mediaType, MultivaluedMap<String, String> httpHeaders,
            InputStream entityStream) throws IOException, WebApplicationException {

        JsonReader jsonReader = Json.createReader(new InputStreamReader(entityStream));

        JsonObject jsonResponse = jsonReader.readObject();
        log.warn("Received GraphQL response: "
                + (jsonResponse.toString().length() <= MAX_LOG_LENGTH ? jsonResponse.toString() : jsonResponse.toString().substring(0, MAX_LOG_LENGTH) + " etc..."));

        DynaQLResponse graphQLResponse = new DynaQLResponse();

        if (jsonResponse.containsKey("errors")) {
            log.warn("GraphQL errors detected in the response");
            JsonArray rawErrors = jsonResponse.getJsonArray("errors");
            Jsonb jsonb = JsonbBuilder.create();
            List<GraphQLError> errors = jsonb.fromJson(rawErrors.toString(), new ArrayList<DynaQLError>() {
            }.getClass().getGenericSuperclass());
            graphQLResponse.setErrors(errors);
            try {
                jsonb.close();
            } catch (Exception ignore) {
            } // Ugly!!!
        }

        if (jsonResponse.containsKey("data")) {
            if (!jsonResponse.isNull("data")) {
                JsonObject data = jsonResponse.getJsonObject("data");
                graphQLResponse.setData(data);
            } else {
                log.warn("No data in GraphQLResponse");
            }
        }

        return graphQLResponse;
    }

}
