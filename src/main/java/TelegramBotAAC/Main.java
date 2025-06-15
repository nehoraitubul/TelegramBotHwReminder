package PersonalProjects.TelegramBotAAC;

import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

public class Main {
    // USERNAME: AacHomeWorkReminder
    // TOKEN: 7691437918:AAHnGdRgRN55TP6bV1ZMD29hsGdDnLWK3dk
    // LINK: t.me/AacHomeWorkReminderBot

    public static void main(String[] args) {

        try {
            TelegramBotsApi telegramBotsApi = new TelegramBotsApi(DefaultBotSession.class);
            telegramBotsApi.registerBot(new MyBot());
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }

    }
}
