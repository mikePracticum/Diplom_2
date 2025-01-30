package com.stellarburrgers.api_tests;

import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.junit.Test;
import io.qameta.allure.Step; // Импортируем аннотацию Step

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;

public class UserLoginTests {

    static {
        // Установка базового URI вашего API
        RestAssured.baseURI = "https://stellarburgers.nomoreparties.site/api/auth/";
    }

    @Test
    @Step("Тест успешного логина")
    public void testSuccessfulLogin() {
        // Задаём параметры для аутентификации
        String email = "test-221data1@yandex.ru";
        String password = "password";

        // Выполняем аутентификацию и получаем ответ
        Response response = performLogin(email, password);

        // Выводим ответ от сервера
        System.out.println("Ответ от сервера (успешный логин): " + response.asString());

        // Проверяем успешный ответ
        validateSuccessfulLogin(response);
    }

    @Test
    @Step("Тест неуспешного логина")
    public void testUnsuccessfulLogin() {
        // Задаём неверные параметры для аутентификации
        String email = "wrong-email@yandex.ru";
        String password = "wrong-password";

        // Выполняем аутентификацию и получаем ответ
        Response response = performLogin(email, password);

        // Выводим ответ от сервера
        System.out.println("Ответ от сервера (неуспешный логин): " + response.asString());

        // Проверяем, что ответ содержит ошибку
        validateUnsuccessfulLogin(response);
    }

    @Step("Выполнение логина")
    private Response performLogin(String email, String password) {
        return given()
                .contentType("application/json")
                .body("{ \"email\": \"" + email + "\", \"password\": \"" + password + "\" }")
                .when()
                .post("login")
                .then()
                .extract().response();
    }

    @Step("Проверка успешного логина")
    private void validateSuccessfulLogin(Response response) {
        response.then()
                .statusCode(200)
                .body("success", equalTo(true));
    }

    @Step("Проверка неуспешного логина")
    private void validateUnsuccessfulLogin(Response response) {
        response.then()
                .statusCode(401) // Обычно для неверных данных возвращается 401 Unauthorized
                .body("success", equalTo(false))
                .body("message", equalTo("email or password are incorrect")); // Замените на актуальное сообщение вашей API
    }
}