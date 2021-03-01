package com.nb.newstbot.domain;

import lombok.extern.slf4j.Slf4j;

/**
 * @author Nick Barban.
 */
@Slf4j
public class NonCommand {
    public String nonCommandExecute(Long chatId, String userName, String text) {
        log.debug(String.format("Пользователь %s. Начата обработка сообщения \"%s\", не являющегося командой",
                userName, text));

        String answer;
        try {
            log.debug(String.format("Пользователь %s. Пробуем создать объект настроек из сообщения \"%s\"",
                    userName, text));
            log.debug(String.format("Пользователь %s. Объект настроек из сообщения \"%s\" создан и сохранён",
                    userName, text));
            createSettings(text);
            answer = "Настройки обновлены. Вы всегда можете их посмотреть с помощью /settings";
        } catch (IllegalArgumentException e) {
            log.debug(String.format("Пользователь %s. Не удалось создать объект настроек из сообщения \"%s\". " +
                    "%s", userName, text, e.getMessage()));
            answer = e.getMessage() +
                    "\n\n❗ Настройки не были изменены. Вы всегда можете их посмотреть с помощью /settings";
        } catch (Exception e) {
            log.debug(String.format("Пользователь %s. Не удалось создать объект настроек из сообщения \"%s\". " +
                    "%s. %s", userName, text, e.getClass().getSimpleName(), e.getMessage()));
            answer = "Простите, я не понимаю Вас. Похоже, что Вы ввели сообщение, не соответствующее формату, или " +
                    "использовали слишком большие числа\n\n" +
                    "Возможно, Вам поможет /help";
        }

        log.debug(String.format("Пользователь %s. Завершена обработка сообщения \"%s\", не являющегося командой",
                userName, text));
        return answer;
    }

    private void createSettings(String text) throws IllegalArgumentException {
        //отсекаем файлы, стикеры, гифки и прочий мусор
        if (text == null) {
            throw new IllegalArgumentException("Сообщение не является текстом");
        }
        /*text = text.replaceAll("-", "")//избавляемся от отрицательных чисел (умники найдутся)
                .replaceAll(", ", ",")//меняем ошибочный разделитель "запятая+пробел" на запятую
                .replaceAll(" ", ",");//меняем разделитель-пробел на запятую
        String[] parameters = text.split(",");
        if (parameters.length != 3) {
            throw new IllegalArgumentException(String.format("Не удалось разбить сообщение \"%s\" на 3 составляющих",
                    text));
        }
        int min = Integer.parseInt(parameters[0]);
        int max = Integer.parseInt(parameters[1]);
        int listCount = Integer.parseInt(parameters[2]);

        validateSettings(min, max, listCount);*/
    }

    /*private void validateSettings(int min, int max, int listCount) {
        if (min == 0 || max == 0 || listCount == 0) {
            throw new IllegalArgumentException("\uD83D\uDCA9 Ни один из параметров не может равняться 0");
        }
    }*/
}
