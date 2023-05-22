package pro.sky.telegrambot.service;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.request.SendMessage;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import pro.sky.telegrambot.model.NotificationTask;
import pro.sky.telegrambot.repository.TaskRepository;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class TaskService {

    private static final Pattern PATTERN= Pattern.compile("([0-9\\.\\:\\s]{16})(\\s)([\\W+]+)");
    private final TelegramBot telegramBot;
    private final TaskRepository taskRepository;

    public TaskService(TelegramBot telegramBot, TaskRepository taskRepository) {
        this.telegramBot = telegramBot;
        this.taskRepository = taskRepository;
    }

    public boolean checkTask(String message, Long chatId) {
        Matcher matcher = PATTERN.matcher(message);
        if (matcher.matches()) {
            try {
                String stringTaskDate = matcher.group(1);
                String taskMessage = matcher.group(3);
                LocalDateTime taskDate = LocalDateTime.parse(stringTaskDate,
                        DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm")).truncatedTo(ChronoUnit.MINUTES);
                NotificationTask task = new NotificationTask();
                task.setChatId(chatId);
                task.setTaskMessage(taskMessage);
                task.setTaskDate(taskDate);
                taskRepository.save(task);
                return true;
            } catch (DateTimeParseException e) {
                return false;
            }
        } else return false;
    }

    @Scheduled(cron = "0 0/1 * * * *")
    public void sendTasks() {
        LocalDateTime date = LocalDateTime.now().truncatedTo(ChronoUnit.MINUTES);
        List<NotificationTask> taskList = taskRepository.findByTaskDateEquals(date);
        if (!taskList.isEmpty()) {
            taskList
                    .stream()
                    .filter(Objects::nonNull)
                    .filter(e -> e.getChatId() != null)
                    .filter(e -> e.getTaskMessage() != null)
                    .forEach(e -> {
                        telegramBot.execute(new SendMessage(e.getChatId(), e.getTaskMessage()));
                    });
        }
    }
}
