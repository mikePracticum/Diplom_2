package com.stellarburrgers.api_tests;

import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import io.qameta.allure.Step;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.UUID;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertTrue;

public class UserCreationTests {
    private static final String USER_URL = "https://stellarburgers.nomoreparties.site/api/auth/user";
    private static final String REGISTER_URL = "https://stellarburgers.nomoreparties.site/api/auth/register";
    private String email;
    private String authToken;
    private String name = "New Username21";
    private static final Logger logger = LoggerFactory.getLogger(UserCreationTests.class);

    @Before
    @Step("Создание тестового пользователя для заказа")
    public void setUp() {
        email = "test-" + UUID.randomUUID().toString() + "@yandex.ru";

        String requestBody = String.format("{\"email\":\"%s\", \"password\":\"testPassword\", \"name\":\"%s\"}", email, name );

        Response createUserResponse = given()
                .contentType(ContentType.JSON)
                .body(requestBody)
                .when()
                .post(REGISTER_URL)
                .then()
                .statusCode(200) // Проверка, что пользователь создан
                .log().all() // Логируем весь ответ от сервера
                .extract()
                .response();

        authToken = createUserResponse.jsonPath().getString("accessToken");

        // Логируем токен и ID пользователя
        logger.info("Токен авторизации получен: {}", authToken);
    }

    @After
    @Step("Удаление тестового пользователя")
    public void tearDown() {
        if (authToken != null && !authToken.isEmpty()) {
            Response response = given()
                    .header("Authorization", authToken)
                    .when()
                    .delete(USER_URL);

            int statusCode = response.statusCode();
            System.out.println("Ответ на запрос удаления: " + response.asString());
            assertTrue("Сервер вернул неожиданный код состояния: " + statusCode, statusCode == 200 || statusCode == 202);
        }
    }

    @Test
    @Step("Создание уникального пользователя")
    public void createUniqueUser() {
        String uniqueEmail = "test-" + UUID.randomUUID().toString() + "@yandex.ru";

        Response response = createUser(uniqueEmail, "password123", "UniqueUser");
        response.then()
                .statusCode(200)
                .body("success", equalTo(true))
                .log().all(); // Логируем успешный ответ
    }

    @Test
    @Step("Создание пользователя, который уже зарегистрирован")
    public void createUserAlreadyExists() {
        createUser(email, "password123", "ExistingUser");

        Response response = createUser(email, "password123", "ExistingUser");
        response.then()
                .statusCode(403) // Код ответа для конфликта
                .body("success", equalTo(false))
                .body("message", equalTo("User already exists"))
                .log().all(); // Логируем сообщение о конфликте
    }

    @Test
    @Step("Создание пользователя с отсутствующим обязательным полем")
    public void createUserWithoutRequiredField() {
        Response response = createUser(null, "password123", "MissingEmailUser");
        response.then()
                .statusCode(403) // Код ответа для некорректного запроса
                .body("success", equalTo(false))
                .log().all(); // Логируем сообщение об ошибке
    }

    private Response createUser(String email, String password, String name) {
        String requestBody = String.format("{\"email\":\"%s\", \"password\":\"%s\", \"name\":\"%s\"}",
                email != null ? email : "", password, name);
        return given()
                .contentType(ContentType.JSON)
                .body(requestBody)
                .when()
                .post(REGISTER_URL);
    }
}