package PersonalProjects.TelegramBotAAC;

import java.time.LocalDate;

public class TaskEntry {
    public int taskId;
    public String description;
    public LocalDate dueDate;
    public String addedBy;
    public long userId;
    public boolean submitted;

    public TaskEntry(int taskId, String description, LocalDate dueDate, String addedBy, long userId, boolean submitted) {
        this.taskId = taskId;
        this.description = description;
        this.dueDate = dueDate;
        this.addedBy = addedBy;
        this.userId = userId;
        this.submitted = submitted;
    }

    public String[] toCsvRow() {
        return new String[]{
                String.valueOf(taskId),
                description,
                dueDate.toString(), // נשמר כ yyyy-MM-dd
                addedBy,
                String.valueOf(userId),
                String.valueOf(submitted)
        };
    }
}
