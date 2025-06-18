package TelegramBotAAC;

import com.opencsv.CSVWriter;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.commands.SetMyCommands;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageReplyMarkup;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.commands.BotCommand;
import org.telegram.telegrambots.meta.api.objects.commands.scope.BotCommandScopeDefault;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.util.*;

public class MyBot extends TelegramLongPollingBot {
    private final long adminId = 1276968974;

    private static final String DATA_DIR = "/data/";
    private static final String FILE_PATH = DATA_DIR + "tasks.csv";

    private Map<Long, UserState> userStates = new HashMap<>();
    private Map<Long, String> pendingTaskDescriptions = new HashMap<>();

    private CsvTaskManager csvTaskManager = new CsvTaskManager();
    private UserManager userManager = new UserManager();
    private BackupManager backupManager = new BackupManager();

    private Timer timer = new Timer();

    public MyBot(){
        registerCommands();
//        startDailyReminder();
    }

    public String getBotToken(){
        String token = System.getenv("TELEGRAM_BOT_TOKEN");

        if (token == null) {
            token = EnvLoader.getEnv("TELEGRAM_BOT_TOKEN");
        }

        return token;
    }

    @Override
    public String getBotUsername() {
        return "AacHomeWorkReminderBot";
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage()){
            Long chatId = update.getMessage().getChatId();
            String messageText = update.getMessage().getText();

            if (!userManager.isUserExists(chatId)) {
                userManager.addUser(chatId);
                assignOpenTasksToNewUser(chatId);
                sendWelcomeMessage(chatId);
            }

            if (messageText.equals("/add")) {
                handleAddCommand(chatId);
                return;
            }

            if (messageText.equals("/list")) {
                handleListCommand(chatId);
                return;
            }

            if (messageText.equals("/update")) {
                handleUpdateCommand(chatId);
                return;
            }

            if (messageText.equals("/help")) {
                handleHelpCommand(chatId);
                return;
            }

            if (messageText != null && messageText.trim().startsWith("/dailyReminderNow") && chatId.equals(adminId)){
                System.out.println("🚨 נשלחה פקודת /dailyReminderNow ע״י מנהל");
                for (Long userId : userManager.getAllUsers()) {
                    sendReminderForUser(userId);
                }
            }

            UserState state = userStates.getOrDefault(chatId, UserState.NONE);

            switch (state) {
                case ADDING_TASK_DESCRIPTION:
                    handleTaskDescription(chatId, messageText);
                    break;

                case ADDING_TASK_DUEDATE:
                    handleTaskDueDate(chatId, messageText);
                    break;

                case NONE:
                default:
                    sendMessage(chatId, "🤖 לא הבנתי את הבקשה... אם צריך עזרה אפשר לכתוב /help 📄");
                    break;
            }
        } else if (update.hasCallbackQuery()) {
            handleCallback(update.getCallbackQuery());
        }

    }

    private void handleAddCommand(Long chatId) {
        userStates.put(chatId, UserState.ADDING_TASK_DESCRIPTION);
        sendMessage(chatId, "אנא כתוב את תיאור המשימה:");
    }

    private void handleTaskDescription(Long chatId, String taskDescription) {
        pendingTaskDescriptions.put(chatId, taskDescription);
        userStates.put(chatId, UserState.ADDING_TASK_DUEDATE);
        sendMessage(chatId, "אנא כתוב את תאריך ההגשה (לדוגמה: 2025-07-20):");
    }

    private void handleTaskDueDate(Long chatId, String dueDateStr) {
        String description = pendingTaskDescriptions.get(chatId);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        LocalDate dueDate;
        try {
            dueDate = LocalDate.parse(dueDateStr, formatter);
        } catch (DateTimeParseException e) {
            sendMessage(chatId, "⚠ תאריך לא תקין! יש להזין בפורמט: yyyy-MM-dd");
            return;
        }
        try {
            if (chatId.equals(adminId)) {
                csvTaskManager.addTaskForAllUsers(description, dueDate, "Admin", userManager.getAllUsers());
                sendMessage(chatId, "המשימה נוספה לכולם בהצלחה!");
            } else {
                csvTaskManager.addTaskForSingleUser(description, dueDate, "User", chatId);
                sendMessage(chatId, "המשימה נוספה, בהצלחה!");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        userStates.put(chatId, UserState.NONE);
        pendingTaskDescriptions.remove(chatId);
    }


    private void sendMessage(Long chatId, String text) {
        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        message.setText(text);

        try {
            execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }


    public void handleListCommand(Long chatId){
        List<TaskEntry> tasks = csvTaskManager.loadUnsubmittedTasksByUserId(chatId);

        if (tasks.isEmpty()) {
            sendMessage(chatId, "אין לך משימות פתוחות ✅");
            return;
        }

        StringBuilder sb = new StringBuilder("📋 *המשימות שלך:*\n\n");
        for (TaskEntry task : tasks) {
            sb.append("📌 מטלה: ").append(task.description)
                    .append("\n📅 עד: ").append(task.dueDate)
                    .append("\n⏳ ").append(getDeadlineStatus(task.dueDate))
                    .append("\n\n");
        }

        sendMessage(chatId, sb.toString());
    }


    public void registerCommands() {
        List<BotCommand> commands = new ArrayList<>();
        commands.add(new BotCommand("/add", "הוספת משימה חדשה"));
        commands.add(new BotCommand("/update", "סימון משימה כהוגשה/ביטול הגשה"));
        commands.add(new BotCommand("/list", "הצגת משימות פתוחות"));
        commands.add(new BotCommand("/help", "עזרה"));

        try {
            this.execute(new SetMyCommands(commands, new BotCommandScopeDefault(), null));
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }


    private void handleCallback(CallbackQuery callbackQuery) {
        String data = callbackQuery.getData();
        Long chatId = callbackQuery.getMessage().getChatId();
        int messageId = callbackQuery.getMessage().getMessageId();

        if (data.startsWith("SUBMIT:")) {
            int taskId = Integer.parseInt(data.substring(7));
            csvTaskManager.markTaskAsSubmitted(taskId, chatId);
            sendMessage(chatId, "המשימה סומנה כהוגשה ✅");
            removeInlineKeyboard(chatId, messageId);
        }
        else if (data.equals("SHOW_UNDO")) {
            removeInlineKeyboard(chatId, messageId);
            showUndoMenu(chatId);
        }
        else if (data.startsWith("UNDO:")) {
            int taskId = Integer.parseInt(data.substring(5));
            csvTaskManager.markTaskAsUnsubmitted(taskId, chatId);
            sendMessage(chatId, "המשימה סומנה כלא הוגשה ⛔");
            removeInlineKeyboard(chatId, messageId);
        }
        else if (data.equals("CANCEL")) {
            removeInlineKeyboard(chatId, messageId);
            sendMessage(chatId, "הפעולה בוטלה ❌");
        }
    }

    private void removeInlineKeyboard(Long chatId, int messageId) {
        EditMessageReplyMarkup editMarkup = new EditMessageReplyMarkup();
        editMarkup.setChatId(chatId);
        editMarkup.setMessageId(Math.toIntExact(messageId));
        editMarkup.setReplyMarkup(null);

        try {
            execute(editMarkup);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    public void handleUpdateCommand(Long chatId) {
        List<TaskEntry> tasks = csvTaskManager.loadUnsubmittedTasksByUserId(chatId);

        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();

        for (TaskEntry task : tasks) {
            InlineKeyboardButton button = new InlineKeyboardButton();
            button.setText(task.description + " (עד: " + task.dueDate + ")");
            button.setCallbackData("SUBMIT:" + task.taskId);
            rows.add(Collections.singletonList(button));
        }

        InlineKeyboardButton undoButton = new InlineKeyboardButton();
        undoButton.setText("↩ בטל הגשה");
        undoButton.setCallbackData("SHOW_UNDO");
        rows.add(Collections.singletonList(undoButton));

        InlineKeyboardButton cancelButton = new InlineKeyboardButton();
        cancelButton.setText("❌ בטל פעולה");
        cancelButton.setCallbackData("CANCEL");
        rows.add(Collections.singletonList(cancelButton));

        markup.setKeyboard(rows);

        SendMessage message = new SendMessage();
        message.setChatId(chatId);

        if (tasks.isEmpty()) {
            message.setText("אין לך משימות פתוחות ✅");
        } else {
            message.setText("בחר את המשימה שברצונך לסמן כהוגשה:");
        }

        message.setReplyMarkup(markup);

        try {
            execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    private void showUndoMenu(Long chatId) {
        List<TaskEntry> submittedTasks = csvTaskManager.loadSubmittedTasksByUserId(chatId);

        if (submittedTasks.isEmpty()) {
            sendMessage(chatId, "אין משימות שסומנו כהוגשו.");
            return;
        }

        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();

        for (TaskEntry task : submittedTasks) {
            InlineKeyboardButton button = new InlineKeyboardButton();
            button.setText(task.description + " (עד: " + task.dueDate + ")");
            button.setCallbackData("UNDO:" + task.taskId);
            rows.add(Collections.singletonList(button));
        }

        InlineKeyboardButton cancelButton = new InlineKeyboardButton();
        cancelButton.setText("❌ בטל פעולה");
        cancelButton.setCallbackData("CANCEL");
        rows.add(Collections.singletonList(cancelButton));

        markup.setKeyboard(rows);

        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        message.setText("בחר משימה שברצונך לסמן כלא הוגשה:");
        message.setReplyMarkup(markup);

        try {
            execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }



    private Date getNextRunTime(int hour, int minute) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, hour);
        calendar.set(Calendar.MINUTE, minute);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        Date targetTime = calendar.getTime();

        if (targetTime.before(new Date())) {
            calendar.add(Calendar.DATE, 1);
            targetTime = calendar.getTime();
        }

        return targetTime;
    }

    public void sendReminderForUser(Long chatId) {
        List<TaskEntry> tasks = csvTaskManager.loadUnsubmittedTasksByUserId(chatId);

        if (tasks.isEmpty()) {
            sendMessage(chatId, "📅 אין לך משימות פתוחות להיום. הכל מוגש ✅");
            return;
        }

        StringBuilder sb = new StringBuilder();
        sb.append("📢 *תזכורת יומית!* 📅\n");
        sb.append("המשימות שעדיין פתוחות אצלך:\n\n");

        for (TaskEntry task : tasks) {
            sb.append("📌 *מטלה:* ").append(task.description)
                    .append("\n📅 עד: ").append(task.dueDate)
                    .append("\n⏳ ").append(getDeadlineStatus(task.dueDate))
                    .append("\n✅ סטטוס: עדיין לא הוגש")
                    .append("\n")
                    .append("──────────────\n");
        }

        sb.append("\n💡 בסיום המשימה — יש לסמן אותה כהוגשה ✅");

        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        message.setText(sb.toString());
        message.enableMarkdown(true);
        try {
            execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }


    public void assignOpenTasksToNewUser(Long newChatId) {
        List<TaskEntry> allTasks = csvTaskManager.loadTasks();
        LocalDate today = LocalDate.now();

        try (CSVWriter writer = new CSVWriter(new OutputStreamWriter(new FileOutputStream(FILE_PATH, true), StandardCharsets.UTF_8))) {
            for (TaskEntry task : allTasks) {
                if (task.addedBy.equals("Admin") && !task.submitted && !task.dueDate.isBefore(today)) {
                    TaskEntry newEntry = new TaskEntry(task.taskId, task.description, task.dueDate, task.addedBy, newChatId, false);
                    writer.writeNext(newEntry.toCsvRow());
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }



    private String getDeadlineStatus(LocalDate dueDate) {
        LocalDate today = LocalDate.now();
        long daysDiff = ChronoUnit.DAYS.between(today, dueDate);

        if (daysDiff > 3) {
            return "🟢 נותרו: " + daysDiff + " ימים";
        } else if (daysDiff >= 0) {
            return "🟠 נותרו: " + daysDiff + " ימים";
        } else {
            return "🔴 באיחור של: " + Math.abs(daysDiff) + " ימים";
        }
    }



    private void handleHelpCommand(Long chatId) {
        String helpText = """
    📚 *הוראות שימוש בבוט:*

    /add — הוספת משימה חדשה.
    
    /list — הצגת כל המשימות הפתוחות שלך, כולל ימים שנותרו או איחור.
    
    /update — סימון משימה כהוגשה או ביטול הגשה.
    
    /help — הצגת הוראות שימוש.

    📅 *הערות לגבי תאריכים:*
    יש להזין תאריך בפורמט: yyyy-MM-dd
    לדוגמה: 2025-07-20

    🔔 כל יום ב-20:00 תישלח לך תזכורת אוטומטית לכל המשימות שפתוחות.
    """;

        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        message.setText(helpText);
        message.enableMarkdown(true);
        try {
            execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }


    private void sendWelcomeMessage(Long chatId) {
        String welcomeText = """
🎓 *ברוך הבא למערכת ניהול המשימות של התואר שלנו!*

✅ כל המשימות שפתוחות נוספו אוטומטית לרשימה האישית שלך.
✅ אין צורך להוסיף בעצמך משימות שכבר קיימות — הכל מתעדכן לבד.
✅ בכל יום ב-20:00 תקבל תזכורת עם המשימות שלך.

📝 תוכל להשתמש בפקודות:
- /list — לראות את המשימות שלך
- /update — לסמן שהגשת משימות או לבטל הגשה
- /help — לראות שוב את כל ההוראות

🚀 שיהיה בהצלחה בלימודים! ואם יש בעיות — תדברו איתי 😉
""";

        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        message.setText(welcomeText);
        message.enableMarkdown(true);
        try {
            execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }


}
