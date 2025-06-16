package TelegramBotAAC;

public class ReminderRunner {
    public static void main(String[] args) {
        // טוען את כל המנהלים בדיוק כמו בבוט שלך
        CsvTaskManager csvTaskManager = new CsvTaskManager();
        UserManager userManager = new UserManager();

        // עובר על כל היוזרים
        for (Long chatId : userManager.getAllUsers()) {
            ReminderSender.sendReminder(chatId, csvTaskManager);
        }

        System.out.println("התזכורת נשלחה בהצלחה לכל המשתמשים ✅");
    }
}
