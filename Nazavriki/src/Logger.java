//Class for message logging

import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.Message;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class Logger {
    private static String logsFolderPath; //path to logs folder
    private static String chatsFolderPath; //path to chats folder
    private final List<MessageTime> messages; //message list to be saved to disk
    private LocalDateTime lastSaveTime; //last time logs were saved from memory to disk

    //initialization
    public Logger(Config config) {
        messages = new ArrayList<>();
        logsFolderPath = config.getLoggerFolderPath();
        chatsFolderPath = logsFolderPath + "/Chats";
        createFolders();

        System.out.println("[LOGGING]Full path to logs is " + new File(logsFolderPath).getAbsolutePath());
        //log saving thread
        new Thread(() -> {
            //logs will be saved to disk each logdelay milliseconds
            long logDelay = Long.parseLong(config.getProperty(config.getConfigFile(), "logdelay"));
            System.out.println("[LOGGING]Logging for all chats enabled with interval: " + logDelay / 1000 + " seconds");

            while (true) {
                System.out.println("____________________________________________");
                System.out.println("[LOGGING]Saving messages started");
                saveMessages(config);
                System.out.println("[LOGGING]Saving messages finished");
                System.out.println("____________________________________________");

                try {
                    Thread.sleep(logDelay); //waiting for the next log cycle
                } catch (InterruptedException e) {
                    System.err.println("[LOGGING]Thread sleep for saving chat thread created an exception:");
                    e.printStackTrace();
                }
            }
        }).start();
    }

    private String formMessage(MessageTime messageTime) {
        Message message = messageTime.message();
        String res = "";
        res += "{\n";
        res += "Time: " + messageTime.time() + "\n";
        res += "User data:" + message.getFrom().getFirstName() + " " + message.getFrom().getLastName() + " (@" + message.getFrom().getUserName() + ")\n";
        res += "Text:" + message.getText() + "\n";
        res += "Chat info:" + message.getChat().getTitle() + " chat type: " + message.getChat().getType() + "\n";
        res += "}\n";
        return res;
    }

    private void saveMessages(Config config) {
        LocalDateTime now = LocalDateTime.now();
        System.out.println("[LOGGING]now = " + now);
        System.out.println("[LOGGING]lastSaveTime = " + lastSaveTime);

        System.out.println("[LOGGING]Saving " + messages.size() + " messages");

        FileOutputStream fos;
        ObjectOutputStream oos;

        for (int i = 0; i < messages.size(); i++) {
            MessageTime message = messages.get(i);
            try {
                File chatFolder = getChatFolder(getChatFolderName(message.message.getChat()));
                File curFile;

                //saving to a new log file
                if (Boolean.parseBoolean(config.getProperty(config.getConfigFile(), "enablesmalllogfiles"))) {
                    curFile = new File(chatFolder + "/" + now.getHour() + "-" + now.getMinute() + "-" + now.getSecond() + " save.log");
                    System.out.println("[LOGGING]curFile = " + curFile);
                    System.out.println("[LOGGING]Creating a file for a new save: " + (curFile.createNewFile() ? "Success" : "Failure"));
                    fos = new FileOutputStream(curFile, true);
                    oos = new ObjectOutputStream(fos);
                    oos.writeObject(formMessage(message) + "\n");
                    oos.flush();
                }
                //appending to an existing big log file
                curFile = new File(chatFolder + "/!bigsave.log");
                System.out.println("[LOGGING]curFile = " + curFile);
                System.out.println("[LOGGING]Creating a file for a new save: " + (curFile.createNewFile() ? "Success" : "Failure"));
                fos = new FileOutputStream(curFile, true);
                oos = new ObjectOutputStream(fos);
                oos.writeObject(formMessage(message) + "\n");
                oos.flush();
            } catch (IOException e) {
                System.err.println("[LOGGING]Unable to save messages: folder for chat: " + getChatFolderName(message.message.getChat()) + " not found");
                e.printStackTrace();
            }
        }
        lastSaveTime = now;

        messages.clear();
    }

    //getting a folder for chat logs
    private File getChatFolder(String chatName) {
        if (!containsChat(chatName))
            return null;
        return new File(chatsFolderPath + "/" + chatName);
    }

    //checking if chat folder already exists
    public boolean containsChat(String chatName) {
        File file = new File(chatsFolderPath);
        List<String> directories = Arrays.asList(Objects.requireNonNull(file.list((dir, name) -> new File(dir, name).isDirectory())));
        return directories.contains(chatName);
    }

    //logging
    public void log(Message msg) {
        addChat(msg.getChat());
        messages.add(new MessageTime(msg, LocalDateTime.now()));
    }

    //getting a name for a chat folder
    private String getChatFolderName(Chat chat) {
        return (chat.getTitle() == null ? chat.getLastName() + " " + chat.getFirstName() : chat.getTitle())
                .replaceAll("\"", "")
                .replaceAll("/", "");
    }

    //adding chat folder
    public void addChat(Chat chat) {
        String chatName = getChatFolderName(chat);
        if (!containsChat(chatName)) {
            boolean a = new File(chatsFolderPath + "/" + chatName).mkdir();
            System.out.println("chatsFolderPath + \"/\" + chatName = " + chatsFolderPath + "/" + chatName);
            System.out.println("Creating new folder fot chat " + chatName + (a ? " successful" : " failure"));
        } else
            System.out.println("Chat folder for " + chatName + " already exists");
    }

    //creating all folders
    public void createFolders() {
        System.out.println("logsFolderPath = " + logsFolderPath);
        System.out.println("chatsFolderPath = " + chatsFolderPath);
        boolean a = new File(logsFolderPath).mkdir();
        boolean b = new File(chatsFolderPath).mkdir();
        System.out.println("Creating logger folder: " + (a ? "Successful" : "Failure"));
        System.out.println("Creating chats folder: " + (b ? "Successful" : "Failure"));
    }

    //a record for simplicity of saving messages with their time
    record MessageTime(Message message, LocalDateTime time) {
    }
}
