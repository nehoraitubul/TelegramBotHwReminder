package TelegramBotAAC;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

public class ReminderSender {

    public static String buildReminderMessage(Long chatId, CsvTaskManager csvTaskManager) {
        List<TaskEntry> tasks = csvTaskManager.loadUnsubmittedTasksByUserId(chatId);

        if (tasks.isEmpty()) {
            return "📅 אין לך משימות פתוחות להיום. הכל מוגש ✅";
        }

        StringBuilder sb = new StringBuilder();
        sb.append("📢 *תזכורת יומית!* 📅\\n");
        sb.append("המשימות שעדיין פתוחות אצלך:\\n\\n");

        for (TaskEntry task : tasks) {
            sb.append("📌 *מטלה:* ").append(escapeMarkdown(task.description))
                    .append("\\n📅 עד: ").append(task.dueDate)
                    .append("\\n⏳ ").append(getDeadlineStatus(task.dueDate))
                    .append("\\n✅ סטטוס: עדיין לא הוגש")
                    .append("\\n──────────────\\n");
        }

        sb.append("\\n💡 בסיום המשימה — יש לסמן אותה כהוגשה ✅");

        return sb.toString();
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

    private static String escapeMarkdown(String text) {
        return text.replace("*", "\\*").replace("_", "\\_").replace("[", "\\[").replace("`", "\\`");
    }
}

