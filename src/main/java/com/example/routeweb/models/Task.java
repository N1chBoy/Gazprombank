package com.example.routeweb.models;


import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.Date;

@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSZ")
@JsonIgnoreProperties(ignoreUnknown = true)
public class Task {

        private String id;
        private String name;
        private String taskDefinitionKey; // Существующее поле
        private Date created; // Добавленное поле для хранения времени создания задачи

        // Геттеры и сеттеры для нового поля
        public Date getCreated() {
            return created;
        }

        public void setCreated(Date created) {
            this.created = created;
        }

        // Геттер для activityId
        public String getActivityId() {
            return taskDefinitionKey;
        }

        // Сеттер для activityId
        public void setActivityId(String taskDefinitionKey) {
            this.taskDefinitionKey = taskDefinitionKey;
        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

}
