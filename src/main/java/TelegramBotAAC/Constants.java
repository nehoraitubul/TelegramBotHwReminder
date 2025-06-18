package TelegramBotAAC;

public class Constants {
    public static final String DATA_DIR = "/data/";
    public static final String TASKS_FILE = DATA_DIR + "tasks.csv";
    public static final String USERS_FILE = DATA_DIR + "users.csv";

    public static final String[] TASKS_HEADER = {
            "TaskID", "Description", "DueDate", "AddedBy", "UserID", "Submitted"
    };

    public static final String[] USERS_HEADER = {
            "UserID", "ReceiveAdminTasks"
    };
}
