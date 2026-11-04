
package com.oropouche.tasks.infrastructure.adapters;

import com.oropouche.tasks.domain.models.AddicionalTaskInfo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class ExternalServiceTaskAdapterTest {

    private RestTemplate restTemplate;
    private MockRestServiceServer mockServer;
    private ExternalServiceTaskAdapter adapter;

    private static final String BASE_URL =
            "https://jsonplaceholder.typicode.com";

    @BeforeEach
    void setUp() {
        restTemplate = new RestTemplate();
        mockServer = MockRestServiceServer.createServer(restTemplate);
        adapter = new ExternalServiceTaskAdapter(restTemplate, BASE_URL);
    }

    @Test
    void shouldReturnAdditionalUserInfoWhenTaskAndUserExist() {

        mockServer.expect(requestTo(BASE_URL + "/todos/1"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(
                        "{\"id\":1,\"userId\":10}",
                        MediaType.APPLICATION_JSON
                ));

        mockServer.expect(requestTo(BASE_URL + "/users/10"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(
                        "{\"id\":10,\"name\":\"Leanne Graham\",\"email\":\"Sincere@april.biz\"}",
                        MediaType.APPLICATION_JSON
                ));

        AddicionalTaskInfo result = adapter.getAddicionalTaskInfo(1L);

        assertNotNull(result);
        assertEquals(10L, result.getUserId());
        assertEquals("Leanne Graham", result.getUserName());
        assertEquals("Sincere@april.biz", result.getUserEmail());

        mockServer.verify();
    }

    @Test
    void shouldReturnNullWhenTodoDoesNotExist() {

        mockServer.expect(requestTo(BASE_URL + "/todos/999"))
                .andRespond(withStatus(HttpStatus.NOT_FOUND));

        AddicionalTaskInfo result = adapter.getAddicionalTaskInfo(999L);

        assertNull(result);

        mockServer.verify();
    }

    @Test
    void shouldReturnNullWhenUserDoesNotExist() {

        mockServer.expect(requestTo(BASE_URL + "/todos/1"))
                .andRespond(withSuccess(
                        "{\"id\":1,\"userId\":999}",
                        MediaType.APPLICATION_JSON
                ));

        mockServer.expect(requestTo(BASE_URL + "/users/999"))
                .andRespond(withStatus(HttpStatus.NOT_FOUND));

        AddicionalTaskInfo result = adapter.getAddicionalTaskInfo(1L);

        assertNull(result);

        mockServer.verify();
    }

    @Test
    void shouldThrowIllegalArgumentExceptionWhenTaskIdIsNull() {

        assertThrows(
                IllegalArgumentException.class,
                () -> adapter.getAddicionalTaskInfo(null)
        );

        mockServer.verify();
    }

    @Test
    void shouldThrowIllegalArgumentExceptionWhenTaskIdIsZero() {

        assertThrows(
                IllegalArgumentException.class,
                () -> adapter.getAddicionalTaskInfo(0L)
        );

        mockServer.verify();
    }

    @Test
    void shouldWrapServerErrorInExternalTaskServiceException() {

        mockServer.expect(requestTo(BASE_URL + "/todos/1"))
                .andRespond(withServerError());

        assertThrows(
                ExternalTaskServiceException.class,
                () -> adapter.getAddicionalTaskInfo(1L)
        );

        mockServer.verify();
    }
}
