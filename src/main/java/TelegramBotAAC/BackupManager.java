package TelegramBotAAC;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;

public class BackupManager {
    private static final String BACKUP_FOLDER = "backups";

    public BackupManager() {
        File dir = new File(BACKUP_FOLDER);
        if (!dir.exists()) {
            dir.mkdirs();
        }
    }

    public void backupFiles() {
        String date = LocalDate.now().toString();

        backupFile("tasks.csv", "tasks_backup_" + date + ".csv");
        backupFile("users.csv", "users_backup_" + date + ".csv");
    }

    private void backupFile(String sourceFileName, String backupFileName) {
        Path sourcePath = Paths.get(sourceFileName);
        Path backupPath = Paths.get(BACKUP_FOLDER, backupFileName);

        try {
            Files.copy(sourcePath, backupPath, StandardCopyOption.REPLACE_EXISTING);
            System.out.println("בוצע גיבוי עבור: " + sourceFileName);
        } catch (IOException e) {
            System.out.println("שגיאה בעת גיבוי של: " + sourceFileName);
            e.printStackTrace();
        }
    }
}
