package TelegramBotAAC;

import static spark.Spark.*;

public class ReminderEndpoint {
    public static void start(MyBot bot) {
        port(8080); // Railway אוטומטית מקצה PORT מתאים
        get("/run-reminder", (req, res) -> {
            if (bot != null) {
                bot.sendDailyReminderToAllUsers();
                return "✅ תזכורת נשלחה בהצלחה!";
            } else {
                return "❌ הבוט לא זמין כרגע.";
            }
        });
    }
}
