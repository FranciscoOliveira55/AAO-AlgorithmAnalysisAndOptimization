package Classes;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Logger {

    private static boolean printMessagesInConsole = false;

    public static void WriteMessage(String message) {
        // Function to write a log message to a file
        // Define the log file path
        String logFilePath = "logs.txt";

        // Get the current date and time
        LocalDateTime now = LocalDateTime.now();

        // Format the current date and time
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        String formattedDateTime = now.format(formatter);

        // Prepare the log message
        String logMessage = formattedDateTime + " # " + message;

        // Write the log message to the file
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(logFilePath, true))) {
            writer.write(logMessage);
            writer.newLine();
            //Print in console
            if (printMessagesInConsole){
                System.out.println(message);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
