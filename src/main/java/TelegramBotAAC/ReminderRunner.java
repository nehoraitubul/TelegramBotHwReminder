package TelegramBotAAC;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class ReminderRunner {
    public static void main(String[] args) {
        String botToken = System.getenv("TELEGRAM_BOT_TOKEN");

        if (botToken == null || botToken.isEmpty()) {
            System.out.println("❌ ה־TOKEN לא נטען מהסביבה!");
            return;
        }

        UserManager userManager = new UserManager();
        CsvTaskManager csvTaskManager = new CsvTaskManager();

        for (Long chatId : userManager.getAllUsers()) {
            try {
                String messageText = ReminderSender.buildReminderMessage(chatId, csvTaskManager);
                sendReminder(botToken, chatId, messageText);
            } catch (Exception e) {
                System.out.println("⚠ שגיאה בשליחה למשתמש " + chatId);
                e.printStackTrace();
            }
        }

        System.out.println("📬 כל התזכורות נשלחו בהצלחה!");
    }

    private static void sendReminder(String token, Long chatId, String text) throws Exception {
        String urlString = "https://api.telegram.org/bot" + token + "/sendMessage";

        URL url = new URL(urlString);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setDoOutput(true);
        conn.setRequestProperty("Content-Type", "application/json");

        String jsonBody = """
            {
              "chat_id": %d,
              "text": "%s",
              "parse_mode": "Markdown"
            }
            """.formatted(chatId, escapeForJson(text));

        try (OutputStream os = conn.getOutputStream()) {
            os.write(jsonBody.getBytes());
        }

        int responseCode = conn.getResponseCode();
        if (responseCode != 200) {
            System.out.println("❌ שגיאה בשליחה ל־" + chatId + ": קוד " + responseCode);
        }
    }

    private static String escapeForJson(String text) {
        return text.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n");
    }
}
