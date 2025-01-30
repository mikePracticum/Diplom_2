package com.stellarburrgers.api_tests;

import io.qameta.allure.Step;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static io.restassured.RestAssured.given;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.UUID;

public class UserUpdateTests {
    private final String BASE_URL = "https://stellarburgers.nomoreparties.site/api/auth/user";
    private String authToken;
    private String testEmail;
    private String testName = "New Username21";

    @Before
    @Step("Создание тестового пользователя для обновления")
    public void setUp() {
        // Генерация уникального email
        testEmail = "test-" + UUID.randomUUID().toString() + "@yandex.ru";

        // Создание тестового пользователя
        String requestBody = String.format("{\"email\":\"%s\", \"password\":\"testPassword\", \"name\":\"Old Username\"}", testEmail);

        Response createUserResponse = given()
                .contentType(ContentType.JSON)
                .body(requestBody)
                .when()
                .post("https://stellarburgers.nomoreparties.site/api/auth/register")
                .then()
                .statusCode(200) // Проверка, что пользователь создан
                .extract()
                .response();

        // Получение токена для дальнейших операций
        authToken = createUserResponse.jsonPath().getString("accessToken"); // Предполагается, что токен возвращается в ответе
    }
    @After
    @Step("Удаление тестового пользователя")
    public void tearDown() {
        if (authToken != null && !authToken.isEmpty()) {
            Response response = given()
                    .header("Authorization", authToken)
                    .when()
                    .delete(BASE_URL);

            int statusCode = response.statusCode();
            System.out.println("Ответ на запрос удаления: " + response.asString());
            assertTrue("Сервер вернул неожиданный код состояния: " + statusCode, statusCode == 200 || statusCode == 202);
        }
    }

    @Test
    @Step("Тест обновления email пользователя с авторизацией")
    public void testUpdateEmailWithAuthorization() {
        String newEmail = "updated-" + UUID.randomUUID().toString() + "@yandex.ru";
        String requestBody = String.format("{\"email\":\"%s\"}", newEmail);

        Response response = given()
                .contentType(ContentType.JSON)
                .header("Authorization", authToken)
                .body(requestBody)
                .when()
                .patch(BASE_URL)
                .then()
                .log().all() // Логирование ответа
                .statusCode(200)
                .extract()
                .response();

        assertEquals(true, response.jsonPath().getBoolean("success"));
        assertEquals(newEmail, response.jsonPath().getString("user.email"));
    }

    @Test
    @Step("Тест обновления имени пользователя с авторизацией")
    public void testUpdateNameWithAuthorization() {
        String newName = "Updated Username";
        String requestBody = String.format("{\"name\":\"%s\"}", newName);

        Response response = given()
                .contentType(ContentType.JSON)
                .header("Authorization", authToken)
                .body(requestBody)
                .when()
                .patch(BASE_URL)
                .then()
                .log().all() // Логирование ответа
                .statusCode(200)
                .extract()
                .response();

        assertEquals(true, response.jsonPath().getBoolean("success"));
        assertEquals(newName, response.jsonPath().getString("user.name"));
    }

    @Test
    @Step("Тест обновления email пользователя без авторизации")
    public void testUpdateEmailWithoutAuthorization() {
        String newEmail = "unauthorized-update-" + UUID.randomUUID().toString() + "@yandex.ru";
        String requestBody = String.format("{\"email\":\"%s\"}", newEmail);

        Response response = given()
                .contentType(ContentType.JSON)
                .body(requestBody)
                .when()
                .patch(BASE_URL)
                .then()
                .log().all() // Логирование ответа
                .statusCode(401) // Проверка, что возвращается код 401
                .extract()
                .response();

        assertEquals(false, response.jsonPath().getBoolean("success")); // Проверка успешности
        assertEquals("You should be authorised", response.jsonPath().getString("message")); // Проверка сообщения об ошибке
    }

    @Test
    @Step("Тест обновления имени пользователя без авторизации")
    public void testUpdateNameWithoutAuthorization() {
        String newName = "Unauthorized Name Update";
        String requestBody = String.format("{\"name\":\"%s\"}", newName);

        Response response = given()
                .contentType(ContentType.JSON)
                .body(requestBody)
                .when()
                .patch(BASE_URL)
                .then()
                .log().all() // Логирование ответа
                .statusCode(401) // Проверка, что возвращается код 401
                .extract()
                .response();

        assertEquals(false, response.jsonPath().getBoolean("success")); // Проверка успешности
        assertEquals("You should be authorised", response.jsonPath().getString("message")); // Проверка сообщения об ошибке
    }
}