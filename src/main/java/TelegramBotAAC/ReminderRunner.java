package TelegramBotAAC;

import java.net.HttpURLConnection;
import java.net.URL;

public class ReminderRunner {
    public static void main(String[] args) {
        String endpoint = "https://telegrambothwreminder-production.up.railway.app/run-reminder";

        try {
            URL url = new URL(endpoint);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");

            int responseCode = conn.getResponseCode();
            if (responseCode == 200) {
                System.out.println("✅ תזכורת הופעלה בהצלחה דרך ה־Endpoint!");
            } else {
                System.out.println("❌ שגיאה בשליחה. קוד תגובה: " + responseCode);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
