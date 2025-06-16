package TelegramBotAAC;

public class DailyReminderRunner {
    public static void main(String[] args) {
        MyBot bot = new MyBot();
        bot.sendDailyReminderToAll();
    }
}
