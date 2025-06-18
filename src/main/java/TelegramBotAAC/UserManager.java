package TelegramBotAAC;

import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;
import com.opencsv.exceptions.CsvValidationException;

import java.io.*;
import java.util.*;

public class UserManager {
    private static final String DATA_DIR = "/data/";
    private static final String FILE_PATH = DATA_DIR + "users.csv";

    private Set<Long> users = new HashSet<>();
    private Map<Long, Boolean> receiveAdminTasksMap = new HashMap<>();

    public UserManager() {
        File dir = new File(DATA_DIR);
        if (!dir.exists()) {
            dir.mkdirs();
        }



        File file = new File(FILE_PATH);
        boolean fileExists = file.exists();

        if (!fileExists) {
            try (CSVWriter writer = new CSVWriter(new FileWriter(FILE_PATH))) {
                writer.writeNext(new String[]{"UserID", "ReceiveAdminTasks"});
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        ensureHeaderExists();
        loadUsers();
    }

    private void ensureHeaderExists() {
        try (BufferedReader reader = new BufferedReader(new FileReader(FILE_PATH))) {
            String headerLine = reader.readLine();
            if (headerLine == null || !headerLine.contains("ReceiveAdminTasks")) {
                List<String[]> allLines = new ArrayList<>();
                try (CSVReader csvReader = new CSVReader(new FileReader(FILE_PATH))) {
                    String[] nextLine;
                    while ((nextLine = csvReader.readNext()) != null) {
                        if (nextLine.length == 1) {
                            allLines.add(new String[]{nextLine[0], "true"});
                        } else {
                            allLines.add(nextLine);
                        }
                    }
                } catch (CsvValidationException e) {
                    throw new RuntimeException(e);
                }
                try (CSVWriter writer = new CSVWriter(new FileWriter(FILE_PATH))) {
                    writer.writeNext(new String[]{"UserID", "ReceiveAdminTasks"});
                    for (String[] line : allLines) {
                        writer.writeNext(line);
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void loadUsers() {
        try (CSVReader reader = new CSVReader(new FileReader(FILE_PATH))) {
            reader.readNext();
            String[] nextLine;
            while ((nextLine = reader.readNext()) != null) {
                try {
                    long id = Long.parseLong(nextLine[0]);
                    boolean receive = nextLine.length > 1 ? Boolean.parseBoolean(nextLine[1]) : true;
                    users.add(id);
                    receiveAdminTasksMap.put(id, receive);
                } catch (NumberFormatException e) {
                    System.out.println("⚠ דילוג על שורה לא חוקית בקובץ users.csv: " + Arrays.toString(nextLine));
                }
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
            receiveAdminTasksMap.put(chatId, true);
            try (CSVWriter writer = new CSVWriter(new FileWriter(FILE_PATH, true))) {
                writer.writeNext(new String[]{String.valueOf(chatId), "true"});
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public Set<Long> getAllUsers() {
        return users;
    }

    public boolean isReceivingAdminTasks(Long chatId) {
        return receiveAdminTasksMap.getOrDefault(chatId, true);
    }

    public void setReceivingAdminTasks(Long chatId, boolean value) {
        receiveAdminTasksMap.put(chatId, value);
        saveAllUsers();
    }

    private void saveAllUsers() {
        try (CSVWriter writer = new CSVWriter(new FileWriter(FILE_PATH))) {
            writer.writeNext(new String[]{"UserID", "ReceiveAdminTasks"});
            for (Long id : users) {
                boolean receive = receiveAdminTasksMap.getOrDefault(id, true);
                writer.writeNext(new String[]{String.valueOf(id), String.valueOf(receive)});
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}


//package TelegramBotAAC;
//
//import com.opencsv.CSVReader;
//import com.opencsv.CSVWriter;
//import com.opencsv.exceptions.CsvValidationException;
//
//import java.io.File;
//import java.io.FileReader;
//import java.io.FileWriter;
//import java.io.IOException;
//import java.util.HashSet;
//import java.util.Set;
//
//public class UserManager {
//    private static final String DATA_DIR = "/data/";
//    private static final String FILE_PATH = DATA_DIR + "users.csv";
//
//    private Set<Long> users = new HashSet<>();
//
//
//    public UserManager() {
//        File dir = new File(DATA_DIR);
//        if (!dir.exists()) {
//            dir.mkdirs();
//        }
//
//        File file = new File(FILE_PATH);
//        if (!file.exists()) {
//            try (CSVWriter writer = new CSVWriter(new FileWriter(FILE_PATH))) {
//                writer.writeNext(new String[]{"UserID"});
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//        }
//        loadUsers();
//    }
//
//
//    private void loadUsers() {
//        try (CSVReader reader = new CSVReader(new FileReader(FILE_PATH))) {
//            reader.readNext();
//            String[] nextLine;
//            while ((nextLine = reader.readNext()) != null) {
//                users.add(Long.parseLong(nextLine[0]));
//            }
//        } catch (IOException | CsvValidationException e) {
//            e.printStackTrace();
//        }
//    }
//
//    public boolean isUserExists(Long chatId) {
//        return users.contains(chatId);
//    }
//
//    public void addUser(Long chatId) {
//        if (!users.contains(chatId)) {
//            users.add(chatId);
//            try (CSVWriter writer = new CSVWriter(new FileWriter(FILE_PATH, true))) {
//                writer.writeNext(new String[]{String.valueOf(chatId)});
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//        }
//    }
//
//    public Set<Long> getAllUsers() {
//        return users;
//    }
//}
