package com.example.clienthomecloud;

import java.util.HashMap;
import java.util.Map;

public class StatusCode {
    Map<Integer, String> listWithCodes = new HashMap<Integer, String>();
    public StatusCode(){
        listWithCodes.put(1, "Соединение установлено");
        listWithCodes.put(2, "Соединение закрыто");
        listWithCodes.put(3, "Соединение не установлено, Сервер не найден");
        listWithCodes.put(4, "Соединение не существует");
        listWithCodes.put(5, "Идет отправление на сервер");
        listWithCodes.put(6, "Отправка произошла");
        listWithCodes.put(7, "Ошибка отправки");
        listWithCodes.put(8, "Пользователь отклонил в доступе");
    }
}
