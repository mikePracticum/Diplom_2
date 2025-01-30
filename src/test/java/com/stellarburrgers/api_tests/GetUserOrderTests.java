package com.stellarburrgers.api_tests;

import io.qameta.allure.Step;
import io.restassured.http.ContentType;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static io.restassured.RestAssured.given;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.UUID;

public class GetUserOrderTests {
    private final String BASE_URL = "https://stellarburgers.nomoreparties.site/api/orders";
    private final String USER_URL = "https://stellarburgers.nomoreparties.site/api/auth/user";
    private final String REGISTER_URL = "https://stellarburgers.nomoreparties.site/api/auth/register";
    private String authToken;
    private String testEmail;
    private String testName = "Test User";

    @Before
    @Step("Создание тестового пользователя для получения заказов")
    public void setUp() {
        // Генерация уникального email
        testEmail = "test-" + UUID.randomUUID().toString() + "@example.com";

        // Регистрация тестового пользователя
        Response createUserResponse = given()
                .contentType(ContentType.JSON)
                .body("{\"email\":\"" + testEmail + "\", \"password\":\"test-password\", \"name\":\"" + testName + "\"}")
                .when()
                .post(REGISTER_URL)
                .then()
                .statusCode(200) // Проверка, что пользователь создан
                .extract()
                .response();

        // Получение токена авторизации
        authToken = createUserResponse.jsonPath().getString("accessToken");

        // Логируем токен
        System.out.println("Токен авторизации получен: " + authToken);
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

    @Step("Получение заказов пользователя")
    @Test
    public void getUserOrders() {
        // Проверяем токен
        if (authToken == null || authToken.isEmpty()) {
            throw new RuntimeException("Токен авторизации не установлен, не удается получить заказы.");
        }

        // Получение заказов с использованием токена
        String orderResponse = given()
                .header("Authorization", authToken)
                .when()
                .get(BASE_URL)
                .then()
                .statusCode(200) // Ожидаем успешный ответ
                .extract()
                .body()
                .asString();

        // Преобразуем в JSON
        JsonPath jsonPath = new JsonPath(orderResponse);
        boolean success = jsonPath.getBoolean("success");

        // Логируем информацию о получении заказов
        System.out.println("Получение заказов: " + orderResponse);
        assertEquals(true, success);
    }

    @Step("Получение заказов пользователя без авторизации")
    @Test
    public void getUserOrdersWithoutAuth() {
        // Получение заказов без токена
        String orderResponse = given()
                .when()
                .get(BASE_URL)
                .then()
                .statusCode(401) // Ожидаем ответ не авторизованного пользователя
                .extract()
                .body()
                .asString();

        // Преобразуем в JSON
        JsonPath jsonPath = new JsonPath(orderResponse);
        boolean success = jsonPath.getBoolean("success");

        // Логируем информацию о получении заказов
        System.out.println("Получение заказов: " + orderResponse);
        assertEquals(false, success);
    }
}