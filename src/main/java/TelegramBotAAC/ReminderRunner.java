package TelegramBotAAC;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class ReminderRunner {
    public static void main(String[] args) {
        String botToken = System.getenv("TELEGRAM_BOT_TOKEN");
        String adminChatId = "1276968974";
        String urlString = "https://api.telegram.org/bot" + botToken + "/sendMessage";

        if (botToken == null || botToken.isEmpty()) {
            System.out.println("❌ ה־TOKEN לא נטען מהסביבה!");
            return;
        }

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
                os.write(jsonBody.getBytes(StandardCharsets.UTF_8));
            }

            int responseCode = conn.getResponseCode();
            if (responseCode == 200) {
                System.out.println("✅ נשלחה פקודה /dailyReminderNow");
            } else {
                System.out.println("⚠️ שגיאה בשליחת הפקודה: קוד " + responseCode);
                InputStream errorStream = conn.getErrorStream();
                if (errorStream != null) {
                    String response = new String(errorStream.readAllBytes(), StandardCharsets.UTF_8);
                    System.out.println("💬 תגובת שגיאה: " + response);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
