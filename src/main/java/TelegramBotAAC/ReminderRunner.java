package TelegramBotAAC;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class ReminderRunner {
    public static void main(String[] args) {
        String botToken = System.getenv("TELEGRAM_BOT_TOKEN");
        String adminChatId = "1276968974";
        String urlString = "https://api.telegram.org/bot" + botToken + "/sendMessage";

        try {
            URL url = new URL(urlString);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setDoOutput(true);
            conn.setRequestProperty("Content-Type", "application/json");

            String jsonBody = """
                {
                  "chat_id": %s,
                  "text": "/dailyReminderNow"
                }
                """.formatted(adminChatId);

            try (OutputStream os = conn.getOutputStream()) {
                os.write(jsonBody.getBytes());
            }

            int responseCode = conn.getResponseCode();
            if (responseCode == 200) {
                System.out.println("נשלחה פקודה /dailyReminderNow ✅");
            } else {
                System.out.println("שגיאה בשליחת הפקודה: קוד " + responseCode);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
