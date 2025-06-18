package TelegramBotAAC;

public class ReminderRunner {
    public static void main(String[] args) {
        try {
            CsvTaskManager csvTaskManager = new CsvTaskManager();
            UserManager userManager = new UserManager();
            MyBot bot = new MyBot();

            for (Long userId : userManager.getAllUsers()) {
                bot.sendReminderForUser(userId);
            }

            System.out.println("📬 כל התזכורות נשלחו בהצלחה!");

        } catch (Exception e) {
            System.out.println("❌ שגיאה בעת שליחת התזכורות:");
            e.printStackTrace();
        }
    }
}
