package com.oropouche.tasks.infrastructure.adapters;

import com.oropouche.tasks.domain.models.AddicionalTaskInfo;
import com.oropouche.tasks.domain.ports.out.ExternalServiceTaskPort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

public class ExternalServiceTaskAdapter implements ExternalServiceTaskPort {

    private final RestTemplate restTemplate;
    private final String baseUrl;

    public ExternalServiceTaskAdapter(RestTemplate restTemplate, String baseUrl) {
        this.restTemplate = restTemplate;
        this.baseUrl = baseUrl.replaceAll("/+$", "");
    }

    @Override
    public AddicionalTaskInfo getAddicionalTaskInfo(Long taskId) {

        if (taskId == null || taskId <= 0) {
            throw new IllegalArgumentException("Task ID must be greater than zero");
        }

        try {
            String todoUrl = baseUrl + "/todos/" + taskId;

            ResponseEntity<JsonPlaceHolderTodo> todoResponse =
                    restTemplate.getForEntity(todoUrl, JsonPlaceHolderTodo.class);

            JsonPlaceHolderTodo todo = todoResponse.getBody();

            if (todo == null || todo.getUserId() == null) {
                return null;
            }

            String userUrl = baseUrl + "/users/" + todo.getUserId();

            ResponseEntity<JsonPlaceHolderUser> userResponse =
                    restTemplate.getForEntity(userUrl, JsonPlaceHolderUser.class);

            JsonPlaceHolderUser user = userResponse.getBody();

            if (user == null || user.getId() == null) {
                return null;
            }

            return new AddicionalTaskInfo(
                    user.getId(),
                    user.getName(),
                    user.getEmail()
            );

        } catch (HttpClientErrorException.NotFound exception) {
            return null;
        } catch (RestClientException exception) {
            throw new ExternalTaskServiceException(
                    "Error communicating with JSONPlaceholder",
                    exception
            );
        }
    }

    private static class JsonPlaceHolderTodo {

        private Long id;
        private Long userId;

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public Long getUserId() {
            return userId;
        }

        public void setUserId(Long userId) {
            this.userId = userId;
        }
    }

    private static class JsonPlaceHolderUser {

        private Long id;
        private String name;
        private String email;

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }
    }
}