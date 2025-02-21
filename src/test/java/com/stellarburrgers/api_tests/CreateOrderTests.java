package com.stellarburrgers.api_tests;

import io.qameta.allure.Step;
import io.restassured.http.ContentType;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static io.restassured.RestAssured.given;
import static org.junit.Assert.assertTrue;

import java.util.UUID;

public class CreateOrderTests {
    private final String BASE_URL = "https://stellarburgers.nomoreparties.site/api/orders";
    private final String USER_URL = "https://stellarburgers.nomoreparties.site/api/auth/user";
    private final String REGISTER_URL = "https://stellarburgers.nomoreparties.site/api/auth/register";
    private String authToken;
    private String testEmail;
    private String testName = "New Username21";

    @Before
    @Step("Создание тестового пользователя для заказа")
    public void setUp() {
        // Генерация уникального email
        testEmail = "test-" + UUID.randomUUID().toString() + "@yandex.ru";

        // Создание тестового пользователя
        String requestBody = String.format("{\"email\":\"%s\", \"password\":\"testPassword\", \"name\":\"%s\"}", testEmail, testName);

        Response createUserResponse = given()
                .contentType(ContentType.JSON)
                .body(requestBody)
                .when()
                .post(REGISTER_URL)
                .then()
                .statusCode(200) // Проверка, что пользователь создан
                .extract()
                .response();

        // Получение токена
        authToken = createUserResponse.jsonPath().getString("accessToken"); // Предполагается, что токен возвращается в ответе

        // Логируем токен авторизации
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

    @Step("Создание заказа теста")
    @Test
    public void CreateOrder() {
        // Проверяем токен
        if (authToken == null || authToken.isEmpty()) {
            throw new RuntimeException("Токен авторизации не установлен, не удается создать заказ.");
        }

        // Создание заказа с использованием корректных ингредиентов
        String orderResponse = given()
                .header("Authorization", authToken)
                .contentType("application/json")
                .body("{\"ingredients\": [\"61c0c5a71d1f82001bdaaa6d\", \"61c0c5a71d1f82001bdaaa76\"]}") // Корректные идентификаторы
                .when()
                .post(BASE_URL)
                .then()
                .statusCode(200) // Ожидаем успешный ответ
                .extract()
                .body()
                .asString();

        // Преобразуем в JSON
        JsonPath jsonPath = new JsonPath(orderResponse);
        String orderId = jsonPath.getString("order._id"); // Пожалуйста, проверьте структуру ответа

        // Логируем ID заказа
        System.out.println("Заказ успешно создан, ID заказа: " + orderId);
        // Логируем ответ
        System.out.println("Детали заказа: " + orderResponse);
    }
    @Step("Создание заказа без авторизации с игридентами")
    @Test
    public void CreateOrderWithoutAuthWithIngredients() {
        // Проверяем токен авторизации
        if (authToken == null || authToken.isEmpty()) {
            throw new RuntimeException("Токен авторизации не установлен, не удается создать заказ.");
        }

        // Создание заказа без авторизации и с использованием корректных ингредиентов
        String orderResponse = given()
                //.header("Authorization", authToken)
                .contentType("application/json")
                .body("{\"ingredients\": [\"61c0c5a71d1f82001bdaaa6d\", \"61c0c5a71d1f82001bdaaa76\"]}") // Корректные идентификаторы
                .when()
                .post(BASE_URL)
                .then()
                .statusCode(200) // Ожидаем успешный ответ
                .extract()
                .body()
                .asString();

        // Преобразуем в JSON
        JsonPath jsonPath = new JsonPath(orderResponse);
        String orderId = jsonPath.getString("order._id"); // Пожалуйста, проверьте структуру ответа

        // Логируем ID заказа
        System.out.println("Заказ успешно создан, ID заказа: " + orderId);
        // Логируем ответ
        System.out.println("Детали заказа: " + orderResponse);
    }
    @Step("Создание заказа с авторизацией без ингридиентов")
    @Test
    public void CreateOrderWithAuthWithoutIngredients() {
        // Проверяем токен авторизации
        if (authToken == null || authToken.isEmpty()) {
            throw new RuntimeException("Токен авторизации не установлен, не удается создать заказ.");
        }

        // Создание заказа с авторизацией и без ингредиентов
        String orderResponse = given()
                .header("Authorization", authToken)
                .contentType("application/json")
                //.body("{\"ingredients\": [\"61c0c5a71d1f82001bdaaa6d\", \"61c0c5a71d1f82001bdaaa76\"]}") // Корректные идентификаторы
                .when()
                .post(BASE_URL)
                .then()
                .statusCode(400) // Ожидаем не успех
                .extract()
                .body()
                .asString();

        // Преобразуем в JSON
        JsonPath jsonPath = new JsonPath(orderResponse);
        String orderId = jsonPath.getString("order._id"); // Пожалуйста, проверьте структуру ответа

        // Логируем ID заказа
        System.out.println("Order ID: " + orderId);
        // Логируем ответ
        System.out.println("Детали заказа: " + orderResponse);
    }
    @Step("Создание заказа без авторизации и без ингридиентов")
    @Test
    public void CreateOrderWithoutAuthWithoutIngredients() {
        // Проверяем токен авторизации
        if (authToken == null || authToken.isEmpty()) {
            throw new RuntimeException("Токен авторизации не установлен, не удается создать заказ.");
        }

        // Создание заказа без ингредиентов и без авторизации
        String orderResponse = given()
                //.header("Authorization", authToken)
                .contentType("application/json")
                //.body("{\"ingredients\": [\"61c0c5a71d1f82001bdaaa6d\", \"61c0c5a71d1f82001bdaaa76\"]}") // Корректные идентификаторы
                .when()
                .post(BASE_URL)
                .then()
                .statusCode(400) // Ожидаем не успех
                .extract()
                .body()
                .asString();

        // Преобразуем в JSON
        JsonPath jsonPath = new JsonPath(orderResponse);
        String orderId = jsonPath.getString("order._id"); // Пожалуйста, проверьте структуру ответа

        // Логируем ID заказа
        System.out.println("Order ID: " + orderId);
        // Логируем ответ
        System.out.println("Детали заказа: " + orderResponse);
    }
    @Step("Создание заказа с авторизацией и с некорректным ингридиентом")
    @Test
    public void CreateOrderWithAuthWithIncorrectIngredients() {
        // Проверяем токен авторизации
        if (authToken == null || authToken.isEmpty()) {
            throw new RuntimeException("Токен авторизации не установлен, не удается создать заказ.");
        }

        // Создание заказа с использованием не корректных ингредиентов
        String orderResponse = given()
                .header("Authorization", authToken)
                .contentType("application/json")
                .body("{\"ingredients\": [\"21c0c5a71d1f82001bdaaa6d\", \"21c0c5a71d1f82001bdaaa76\"]}") // НЕ корректные идентификаторы
                .when()
                .post(BASE_URL)
                .then()
                .statusCode(400) // Ожидаем "One or more ids provided are incorrect"
                .extract()
                .body()
                .asString();

        // Преобразуем в JSON
        JsonPath jsonPath = new JsonPath(orderResponse);
        String orderId = jsonPath.getString("order._id"); // Пожалуйста, проверьте структуру ответа

        // Логируем ID заказа
        System.out.println("Order ID: " + orderId);
        // Логируем ответ
        System.out.println("Детали заказа: " + orderResponse);
    }
    @Step("Создание заказа с авторизацией и с неверным хешем ингредиентов")
    @Test
    public void CreateOrderWithAuthWithWrongIngredients() {
        // Проверяем токен авторизации
        if (authToken == null || authToken.isEmpty()) {
            throw new RuntimeException("Токен авторизации не установлен, не удается создать заказ.");
        }

        // Создание заказа с использованием некорректных ингредиентов
        String orderResponse = given()
                .header("Authorization", authToken)
                .contentType("application/json")
                .body("{\"ingredients\": [\"21c0c5a71d1f820bdaaa6d\", \"21c0c51d1f82001bdaaa76\"]}") // неправильные ингредиенты
                .when()
                .post(BASE_URL)
                .then()
                .statusCode(500) // ожидание ошибки сервера
                .extract()
                .body()
                .asString();

        // Логируем ответ
        System.out.println("Ответ от сервера: " + orderResponse);

        // Дополнительно можно проверить содержимое ответа
        if (orderResponse.contains("Internal Server Error")) {
            System.out.println("Получена ожидаемая ошибка: Internal Server Error");
        } else {
            System.out.println("Ответ от сервера не содержит ожидаемой ошибки.");
        }
    }
}