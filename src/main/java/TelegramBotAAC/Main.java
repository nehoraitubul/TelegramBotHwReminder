package TelegramBotAAC;

import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

public class Main {
    public static void main(String[] args) {
        try {
            MyBot bot = new MyBot();
            TelegramBotsApi botsApi = new TelegramBotsApi(DefaultBotSession.class);
            botsApi.registerBot(bot);

            // מפעיל את ה-endpoint שיאפשר לשלוח תזכורת מבחוץ
            ReminderEndpoint.start(bot);

            System.out.println("✅ הבוט וה-HTTP endpoint פועלים");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}