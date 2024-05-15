package com.example.routeweb.services;

import com.example.routeweb.models.Task;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.type.TypeReference;
import org.springframework.stereotype.Service;


import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Optional;

@Service
public class CamundaService {

    private CloseableHttpClient httpClient = HttpClients.createDefault();


    public List<Task> getTasksByCriteria() throws IOException {
        String url = "http://localhost:8080/engine-rest/task?processDefinitionKey=DocumentRouting";
        HttpGet httpGet = new HttpGet(url);
        HttpResponse response = httpClient.execute(httpGet);
        String responseBody = EntityUtils.toString(response.getEntity());
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.setDateFormat(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ"));
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        System.out.println(objectMapper.readValue(responseBody, new TypeReference<List<Task>>() {
        }));
        return objectMapper.readValue(responseBody, new TypeReference<List<Task>>() {
        });
    }


    public void completeTaskWithVariables(String taskId, String adminResponse) throws IOException {
        String url = "http://localhost:8080/engine-rest/task/" + taskId + "/complete";
        System.out.println(url);
            // Создание JSON тела запроса
            String json = String.format("{\"variables\": {\"admin_key\": {\"value\": \"%s\", \"type\": \"String\"}}}", adminResponse);
            System.out.println("Переменные:  " + json);
            sendHttpPostRequest(httpClient, url, json);
    }

    public static void sendHttpPostRequest(CloseableHttpClient httpClient, String url, String requestBody) throws IOException {
        // Создаем HTTP POST запрос
        HttpPost httpPost = new HttpPost(url);

        // Устанавливаем тело запроса
        if (!requestBody.isEmpty()) {
            StringEntity entity = new StringEntity(requestBody);
            httpPost.setEntity(entity);
        }

        // Устанавливаем заголовок Content-Type
        httpPost.setHeader("Content-Type", "application/json");

        // Выполняем запрос и получаем ответ
        try (CloseableHttpResponse response = httpClient.execute(httpPost)) {
            // Обрабатываем ответ, например, проверяем статус код
            int statusCode = response.getStatusLine().getStatusCode();
            if (statusCode >= 200 && statusCode < 300) {
                // Успешный ответ
                System.out.println("HTTP POST request completed successfully");
            } else {
                if (response.getEntity() != null) {
                    String responseBody = EntityUtils.toString(response.getEntity());
                    System.err.println("Response body: {} " + responseBody);
                }

                // Ошибка в ответе
                System.err.println("Ответ от сервера " + response);
                System.err.println("HTTP POST request failed with status code: " + statusCode);
            }
        } catch (IOException e) {
            // Обрабатываем исключение, если оно произошло при выполнении запроса
            System.err.println("An error occurred while executing the HTTP POST request: " + e.getMessage());
            throw e;
        }
    }
}
