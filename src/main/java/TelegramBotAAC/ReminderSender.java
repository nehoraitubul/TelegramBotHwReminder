package TelegramBotAAC;

import org.telegram.telegrambots.bots.DefaultAbsSender;
import org.telegram.telegrambots.bots.DefaultBotOptions;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

public class ReminderSender extends DefaultAbsSender {

    private static final String TOKEN = System.getenv("TELEGRAM_BOT_TOKEN");

    public ReminderSender() {
        super(new DefaultBotOptions());
    }

    public static void sendReminder(Long chatId, CsvTaskManager csvTaskManager) {
        ReminderSender sender = new ReminderSender();

        List<TaskEntry> tasks = csvTaskManager.loadUnsubmittedTasksByUserId(chatId);
        if (tasks.isEmpty()) {
            sender.sendText(chatId, "📅 אין לך משימות פתוחות להיום. הכל מוגש ✅");
            return;
        }

        StringBuilder sb = new StringBuilder();
        sb.append("📢 *תזכורת יומית!* 📅\n");
        sb.append("המשימות שעדיין פתוחות אצלך:\n\n");

        for (TaskEntry task : tasks) {
            sb.append("📌 *מטלה:* ").append(task.description)
                    .append("\n📅 עד: ").append(task.dueDate)
                    .append("\n⏳ ").append(getDeadlineStatus(task.dueDate))
                    .append("\n✅ סטטוס: עדיין לא הוגש")
                    .append("\n──────────────\n");
        }

        sb.append("\n💡 בסיום המשימה — יש לסמן אותה כהוגשה ✅");
        sender.sendText(chatId, sb.toString());
    }

    private void sendText(Long chatId, String text) {
        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        message.setText(text);
        message.enableMarkdown(true);

        try {
            execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    private static String getDeadlineStatus(LocalDate dueDate) {
        LocalDate today = LocalDate.now();
        long daysDiff = ChronoUnit.DAYS.between(today, dueDate);

        if (daysDiff > 3) {
            return "🟢 נותרו: " + daysDiff + " ימים";
        } else if (daysDiff >= 0) {
            return "🟠 נותרו: " + daysDiff + " ימים";
        } else {
            return "🔴 באיחור של: " + Math.abs(daysDiff) + " ימים";
        }
    }
}
