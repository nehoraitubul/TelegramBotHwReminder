package TelegramBotAAC;

import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;
import com.opencsv.exceptions.CsvValidationException;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

public class UserManager {
    private static final String FILE_PATH = "users.csv";

    private Set<Long> users = new HashSet<>();

    public UserManager() {
        File file = new File(FILE_PATH);
        if (!file.exists()) {
            try (CSVWriter writer = new CSVWriter(new FileWriter(FILE_PATH))) {
                writer.writeNext(new String[]{"UserID"});
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        loadUsers();
    }

    private void loadUsers() {
        try (CSVReader reader = new CSVReader(new FileReader(FILE_PATH))) {
            reader.readNext(); // מדלג על כותרות
            String[] nextLine;
            while ((nextLine = reader.readNext()) != null) {
                users.add(Long.parseLong(nextLine[0]));
            }
        } catch (IOException | CsvValidationException e) {
            e.printStackTrace();
        }
    }

    public boolean isUserExists(Long chatId) {
        return users.contains(chatId);
    }

    public void addUser(Long chatId) {
        if (!users.contains(chatId)) {
            users.add(chatId);
            try (CSVWriter writer = new CSVWriter(new FileWriter(FILE_PATH, true))) {
                writer.writeNext(new String[]{String.valueOf(chatId)});
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public Set<Long> getAllUsers() {
        return users;
    }
}
