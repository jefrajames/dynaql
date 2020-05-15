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
import java.io.InputStream;
import java.io.StringReader;
import java.math.BigDecimal;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.json.JsonReader;
import javax.json.bind.Jsonb;
import javax.json.bind.JsonbBuilder;
import org.apache.commons.io.IOUtils;
import org.apache.http.Header;
import org.apache.http.HttpHost;
import org.apache.http.client.HttpResponseException;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.config.RequestConfig.Builder;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.slf4j.LoggerFactory;

/**
 *
 * @author jefrajames
 */
public class Invocation {

    public static final int DEFAULT_HTTP_PROXY_PORT = 3128;

    private final Configuration configuration;
    private final URI uri;
    private final String request;
    private final Map<String, String> headers;
    private final Map<String, Object> variables; // TODO: how to manage non String variables?

    private static final org.slf4j.Logger log = LoggerFactory.getLogger(Invocation.class);

    protected Invocation(Configuration configuration, URI uri, String request, Map<String, Object> variables, Map<String, String> headers) {
        this.configuration = configuration;
        this.uri = uri;
        this.request = request;
        this.variables = variables;
        this.headers = headers;
    }

    private void setProxy(Builder configBuilder) {
        String hostname = (String) configuration.get(Configuration.HTTP_PROXY_NAME);
        if (hostname == null) {
            return; // No proxy configured
        }
        log.debug("http.proxy.hostname=" + hostname);

        Integer port = (Integer) configuration.get(Configuration.HTTP_PROXY_PORT);
        if (port == null || port <= 0) {
            port = DEFAULT_HTTP_PROXY_PORT;
        }
        log.debug("http.proxy.port=" + port);

        configBuilder.setProxy(new HttpHost(hostname, port.intValue()));

        return;
    }

    private void setTimeout(Builder configBuilder) {

        // Socket connection timeout
        Long connectTimeout = (Long) configuration.get(Configuration.HTTP_CONNECT_TIMEOUT);

        if (connectTimeout != null) {
            log.debug("http.connect.timeout=" + connectTimeout);
            configBuilder.setConnectTimeout(connectTimeout.intValue());
        }

        // Socket read timeout
        Long readTimeout = (Long) configuration.get(Configuration.HTTP_READ_TIMEOUT);
        if (readTimeout != null) {
            log.debug("http.read.timeout=" + readTimeout);
            configBuilder.setSocketTimeout(readTimeout.intValue());
        }

        // Connection pool/manager timeout
        Long managerTimeout = (Long) configuration.get(Configuration.HTTP_CONNECTION_MANAGER_TIMEOUT);
        if (managerTimeout != null) {
            log.debug("http.connection.manager.timeout=" + managerTimeout);
            configBuilder.setConnectionRequestTimeout(managerTimeout.intValue());
        }

    }

    // Classe HttpResponse qui continet header + responseBody
    private String postHttp(StringEntity stringEntity, Response graphQLResponse) throws IOException {

        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {

            HttpPost httpPost = new HttpPost(uri);

            // Configure the client
            Builder configBuilder = RequestConfig.custom();
            setProxy(configBuilder);
            setTimeout(configBuilder);

            httpPost.setConfig(configBuilder.build());

            // Set the HTTP headers
            if (headers != null) {
                for (Map.Entry<String, String> header : headers.entrySet()) {
                    httpPost.addHeader(header.getKey(), header.getValue());
                }
            }

            httpPost.setEntity(stringEntity);

            try (CloseableHttpResponse httpResponse = httpClient.execute(httpPost)) {

                Header[] headers = httpResponse.getAllHeaders();
                for (int i = 0; i < headers.length; i++) {
                    graphQLResponse.addHeader(headers[i].getName(), headers[i].getValue());
                }

                InputStream contentStream = httpResponse.getEntity().getContent();

                String contentString = IOUtils.toString(contentStream, "UTF-8");
                log.info("GraphQL response=" + contentString);

                if (httpResponse.getStatusLine().getStatusCode() != 200) {
                    throw new HttpResponseException(httpResponse.getStatusLine().getStatusCode(), "The server responded with" + contentString);
                }

                return contentString;
            }

        }

    }

    private JsonObject formatJsonVariables() {
        JsonObjectBuilder varBuilder = Json.createObjectBuilder();
  
        variables.forEach((k, v) -> {
            if (v instanceof String)
                varBuilder.add(k, (String) v);
            else if ( v instanceof Integer )
                varBuilder.add(k, (Integer) v);
        });

        return varBuilder.build();
    }

    private String formatJsonQuery(String request) {
        JsonObjectBuilder queryBuilder = Json.createObjectBuilder().add("query", request);
        if ( !variables.isEmpty() )
            queryBuilder.add("variables", formatJsonVariables());
        return queryBuilder.build().toString();
    }

    public Response invoke() {

        String jsonRequest = formatJsonQuery(request);
        log.info("GraphQL request=" + jsonRequest);

        StringEntity stringEntity = new StringEntity(jsonRequest, ContentType.APPLICATION_JSON);

        Response graphQLResponse = new Response();

        String responseBody;
        try {
            responseBody = postHttp(stringEntity, graphQLResponse);
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }

        JsonReader jsonReader = Json.createReader(new StringReader(responseBody));

        JsonObject jsonResponse = jsonReader.readObject();

        if (jsonResponse.containsKey("errors")) {
            log.warn("GraphQL errors element detected");
            JsonArray rawErrors = jsonResponse.getJsonArray("errors");
            Jsonb jsonb = JsonbBuilder.create();
            List<GraphQLError> errors = jsonb.fromJson(rawErrors.toString(), new ArrayList<GraphQLError>() {
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
                log.warn("data element is null");
            }
        }

        return graphQLResponse;
    }

    public Object getConfiguration(String key) {
        return configuration.get(key);
    }

}
