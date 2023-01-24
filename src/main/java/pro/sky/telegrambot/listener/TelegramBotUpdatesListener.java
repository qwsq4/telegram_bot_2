package pro.sky.telegrambot.listener;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.UpdatesListener;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.SendMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import pro.sky.telegrambot.service.TaskService;

import javax.annotation.PostConstruct;
import java.util.List;

@Service
public class TelegramBotUpdatesListener implements UpdatesListener {

    private Logger logger = LoggerFactory.getLogger(TelegramBotUpdatesListener.class);
    private final TaskService taskService;
    private final TelegramBot telegramBot;

    public TelegramBotUpdatesListener(TaskService taskService, TelegramBot telegramBot) {
        this.taskService = taskService;
        this.telegramBot = telegramBot;
    }

    @PostConstruct
    public void init() {
        telegramBot.setUpdatesListener(this);
    }

    @Override
    public int process(List<Update> updates) {
        updates
                .stream()
                .filter(e -> e.message() != null)
                .filter(e -> e.message().text() != null)
                .forEach(this::checkUpdate);
        return UpdatesListener.CONFIRMED_UPDATES_ALL;
    }
    public void checkUpdate(Update update) {
        String message = update.message().text();
        Long chatId = update.message().chat().id();
        if (message.equals("/start")) {
            telegramBot.execute(new SendMessage(chatId, "Привет! Введи что и когда тебе нужно напомнить " +
                    "в формате: 01.01.2022 20:00 Сделать домашнюю работу"));
        } else if (taskService.checkTask(message, chatId)) {
            telegramBot.execute(new SendMessage(chatId, "Напоминание создано!"));
        } else telegramBot.execute(new SendMessage(chatId, "Не удалось понять твой запрос(. Введи что и когда " +
                "тебе нужно напомнить в формате: 01.01.2022 20:00 Сделать домашнюю работу"));
    }
}