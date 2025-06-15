package PersonalProjects.TelegramBotAAC;

import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class CsvTaskManager {
    private static final String FILE_PATH = "tasks.csv";

    public CsvTaskManager(){
        File file = new File(FILE_PATH);
        if (!file.exists()  || file.length() == 0){
            try (CSVWriter writer = new CSVWriter(new FileWriter(FILE_PATH))) {
                writer.writeNext(new String[]{"TaskID", "Description", "DueDate", "AddedBy", "UserID", "Submitted"});
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    public List<TaskEntry> loadTasks(){
        List<TaskEntry> tasks = new ArrayList<>();
        DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE;

        try{
            CSVReader reader = new CSVReader(new FileReader(FILE_PATH));
            reader.readNext();
            String[] nextLine;

            while ((nextLine = reader.readNext()) != null) {
                int taskId = Integer.parseInt(nextLine[0]);
                String description = nextLine[1];
                LocalDate dueDate = LocalDate.parse(nextLine[2], formatter);
                String addedBy = nextLine[3];
                long userId = Long.parseLong(nextLine[4]);
                boolean submitted = Boolean.parseBoolean(nextLine[5]);

                tasks.add(new TaskEntry(taskId, description, dueDate, addedBy, userId, submitted));
            }

        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return tasks;
    }

    public List<TaskEntry> loadUnsubmittedTasksByUserId(long providedUserId){
        List<TaskEntry> tasks = new ArrayList<>();
        DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE;

        try (CSVReader reader = new CSVReader(new FileReader(FILE_PATH))) {
            reader.readNext();
            String[] nextLine;

            while ((nextLine = reader.readNext()) != null) {
                if (providedUserId == Long.parseLong(nextLine[4]) && !Boolean.parseBoolean(nextLine[5])){
                    int taskId = Integer.parseInt(nextLine[0]);
                    String description = nextLine[1];
                    LocalDate dueDate = LocalDate.parse(nextLine[2], formatter);
                    String addedBy = nextLine[3];
                    long userId = Long.parseLong(nextLine[4]);
                    boolean submitted = Boolean.parseBoolean(nextLine[5]);

                    tasks.add(new TaskEntry(taskId, description, dueDate, addedBy, userId, submitted));
                }
            }

        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return tasks;
    }

    public void addTaskForAllUsers(String description, LocalDate dueDate, String addedBy, Set<Long> userIds){
        int newTaskId = getNextTaskId();

        try (CSVWriter writer = new CSVWriter(new FileWriter(FILE_PATH, true))) {
            for (long id : userIds){
                TaskEntry entry = new TaskEntry(newTaskId, description, dueDate, addedBy, id, false);
                writer.writeNext(entry.toCsvRow());
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void addTaskForSingleUser(String description, LocalDate dueDate, String addedBy, long userId) {
        int newTaskId = getNextTaskId();

        try (CSVWriter writer = new CSVWriter(new FileWriter(FILE_PATH, true))) {
            TaskEntry entry = new TaskEntry(newTaskId, description, dueDate, addedBy, userId, false);
            writer.writeNext(entry.toCsvRow());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public int getNextTaskId(){
        int id = 0;
        for (TaskEntry task : loadTasks()){
            if (task.taskId > id){
                id = task.taskId;
            }
        }
        return id + 1;
    }


    public void markTaskAsSubmitted(int taskId, long userId) {
        List<TaskEntry> tasks = loadTasks();

        for (TaskEntry task : tasks) {
            if (task.taskId == taskId && task.userId == userId) {
                task.submitted = true;
            }
        }

        saveAllTasks(tasks);
    }

    private void saveAllTasks(List<TaskEntry> tasks) {
        try (CSVWriter writer = new CSVWriter(new FileWriter(FILE_PATH))) {
            writer.writeNext(new String[]{"TaskID", "Description", "DueDate", "AddedBy", "UserID", "Submitted"});

            for (TaskEntry task : tasks) {
                writer.writeNext(task.toCsvRow());
            }

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    public List<TaskEntry> loadSubmittedTasksByUserId(long userId) {
        List<TaskEntry> tasks = new ArrayList<>();
        DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE;

        try (CSVReader reader = new CSVReader(new FileReader(FILE_PATH))) {
            reader.readNext();
            String[] nextLine;

            while ((nextLine = reader.readNext()) != null) {
                long taskUserId = Long.parseLong(nextLine[4]);
                boolean submitted = Boolean.parseBoolean(nextLine[5]);

                if (taskUserId == userId && submitted) {
                    int taskId = Integer.parseInt(nextLine[0]);
                    String description = nextLine[1];
                    LocalDate dueDate = LocalDate.parse(nextLine[2], formatter);
                    String addedBy = nextLine[3];

                    tasks.add(new TaskEntry(taskId, description, dueDate, addedBy, userId, submitted));
                }
            }

        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return tasks;
    }


    public void markTaskAsUnsubmitted(int taskId, long userId) {
        List<TaskEntry> tasks = loadTasks();

        for (TaskEntry task : tasks) {
            if (task.taskId == taskId && task.userId == userId) {
                task.submitted = false;
            }
        }

        saveAllTasks(tasks);
    }

}
