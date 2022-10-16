//Class for message answers handling

import org.telegram.telegrambots.meta.api.methods.GetFile;
import org.telegram.telegrambots.meta.api.methods.send.SendAudio;
import org.telegram.telegrambots.meta.api.objects.Audio;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.File;
import java.util.*;

public class MessageAnswers {
    public MessageAnswers() {
    }

    //for /active
    //for /active
    public String getActiveText(Config config, String userName) {
        return Math.random() <= 0.5 ? getRandomActiveText(config, userName) : getRandomPassiveText(config, userName);
    }

    //getting a random text from <active>
    public String getRandomActiveText(Config config, String userName) {
        String res = config.getRandomFromParse(config.getConfigFile(), "active");
        return res.replaceAll("\\{nick}", "@" + userName.replaceAll("@", ""));
    }

    //getting a random text from <passive>
    public String getRandomPassiveText(Config config, String userName) {
        String res = config.getRandomFromParse(config.getConfigFile(), "passive");
        return res.replaceAll("\\{nick}", "@" + userName.replaceAll("@", ""));
    }

    //for where to eat
    public String getWereToEat(Config config) {
        return config.getRandomFromParse(config.getConfigFile(), "eat place");
    }

    //for bye words
    public String getBye(Config config) {
        return config.getRandomFromParse(config.getConfigFile(), "бб");
    }

    //for who is loh
    public String getLoh(Config config) {
        String loh = config.parse(config.getConfigFile(), "loh");
        if (Math.random() < Double.parseDouble(loh.split("\n")[0].split("\\)")[0]))
            return loh.split("\n")[0].split("\\)")[1];
        return loh.split("\n")[1].split("\\)")[1];
    }

    //getting a string with all music names
    private String getAllMusic(Config config) {
        String musicConfig = config.parse(config.getConfigFile(), "music");
        File musicFolder = new File(config.getProperty(config.getConfigFile(), "musicfolderpath"));
        String music = formMusic(config, musicFolder);
        return musicConfig.replaceAll("\\{music}", music);
    }

    //forming a music string from the music folder
    private String formMusic(Config config, File musicFolder) {
        System.out.println("[MUSIC]Forming music...");
        StringBuilder res = new StringBuilder(config.getProperty(config.getConfigFile(), "musicprefix") + "\n");
        if (!musicFolder.isDirectory())
            return "No music today)))";
        System.out.println("[MUSIC]Music folder is folder: " + musicFolder.isDirectory());
        File[] groups = musicFolder.listFiles();
        if (groups == null)
            return config.getProperty(config.getConfigFile(), "nomusicgroups");
        for (int i = 0; i < groups.length; i++) {
            res.append(i + 1).append(")").append(groups[i].getName()).append(": ").append(Objects.requireNonNull(groups[i].listFiles()).length).append(" песен\n");
        }
        System.out.println("[MUSIC]Music formed!");
        return res.toString();
    }

    //for /schedule
    private String getSchedule(Config config) {
        StringBuilder schedule = new StringBuilder();
        //getting valid days from config
        String[] days = config.parse(config.getConfigFile(), "schedule days").split("\n");

        for (int i = 0; i < days.length; i++) {
            schedule.append(getSchedule(config, "/расписание " + days[i]));
        }

        return schedule.toString();
    }

    // for /schedule <day>
    public String getSchedule(Config config, String msg) {
        System.out.println("Getting schedule for " + msg);
        //incorrect usage
        if (msg.trim().split(" ").length > 2)
            return config.getProperty(config.getConfigFile(), "scheduleusage");

        //empty schedule day
        if (msg.toLowerCase(Locale.ROOT).trim().equals("/расписание") || msg.toLowerCase(Locale.ROOT).trim().equals("/schedule"))
            return config.getProperty(config.getConfigFile(), "scheduletext").replaceAll("\\{day}", "неделю") + "\n" + getSchedule(config);

        String scheduleDay = msg.split(" ")[1].toLowerCase(Locale.ROOT);
        scheduleDay = scheduleDay
                .replaceAll("пн", "понедельник")
                .replaceAll("вт", "вторник")
                .replaceAll("ср", "среда")
                .replaceAll("чт", "четверг")
                .replaceAll("пт", "пятница")
                .replaceAll("сб", "суббота");

        String schedule = config.parse(config.getConfigFile(), "schedule");
        String[] configDays = config.parse(config.getConfigFile(), "schedule days").split("\n");
        StringBuilder res = Optional.ofNullable(config.getProperty(config.getConfigFile(), "schedulewrongday")).map(StringBuilder::new).orElse(null);
        //forming schedule
        for (int i = 0; i < configDays.length; i++) {
            if (configDays[i].contains(scheduleDay)) {
                System.out.println("Day is: " + configDays[i] + ", schedule day is: " + scheduleDay);
                System.out.println("schedule = " + schedule);
                res = Optional.ofNullable(config.parse(schedule, configDays[i])).map(StringBuilder::new).orElse(null);
                System.out.println("res = " + res);
            }
        }
        //final schedule formatting
        String[] resSplit = Objects.requireNonNull(res).toString().split("\n");
        res = new StringBuilder();
        for (int i = 0; i < resSplit.length; i++) {
            if (!resSplit[i].isBlank() && !resSplit[i].equals(config.getProperty(config.getConfigFile(), "schedulewrongday")))
                res.append(i + 1).append(")").append(resSplit[i]).append("\n");

            if (resSplit[i].equals(config.getProperty(config.getConfigFile(), "schedulewrongday"))) {
                res = Optional.ofNullable(resSplit[i]).map(StringBuilder::new).orElse(null);
                break;
            }
        }

        return scheduleDay + ":\n" + res;
    }

    //for yes words
    public String getYes(Config config) {
        if (Math.random() <= 0.2)
            return config.getProperty(config.getConfigFile(), "datext");
        return "";
    }

    //for great work words
    public String getGreatJob(Config config, Message msg) {
        String nick = msg.getFrom().getUserName();
        return config.getProperty(config.getConfigFile(), "greatjob").replaceAll("\\{nick}", "@" + nick);
    }

    //for symbol-count based answers
    public String getSymbols(Config config, String msg) {
        System.out.println(msg.trim().length());
        if (msg.trim().length() == 239)
            return config.getProperty(config.getConfigFile(), "239symbols");
        if (msg.trim().length() == 30 && Math.random() <= 0.3)
            return config.getProperty(config.getConfigFile(), "30symbols");
        return "";
    }

    //for thank bot based words
    public String getThanks(Config config) {
        return config.getRandomFromParse(config.getConfigFile(), "thanks");
    }

    //sending music
    public String sendMusic(Config config, String chatId, String group) {
        //music group folder name
        group = group.toLowerCase(Locale.ROOT).trim();
        if (group.isEmpty())
            return getAllMusic(config);

        int number = 1;
        if (group.split(" ").length != 1)
            number = Integer.parseInt(group.split(" ")[1]);

        //if group is blank or contains more than one word
        if (number < 2)
            number = 1;
        System.out.println("[MUSIC] " + number + " tracks to send from " + group.split(" ")[0]);
        group = group.split(" ")[0];
        String musicFolder = config.getProperty(config.getConfigFile(), "musicfolderpath");
        //music group folder
        File musicGroup = new File(musicFolder + "/" + group);

        if (!musicGroup.isDirectory())
            return config.getProperty(config.getConfigFile(), "nothavemusicgroup");

        File[] files = musicGroup.listFiles();
        if (files == null)
            return config.getProperty(config.getConfigFile(), "nomusicgroups");

        //getting a random track
        Collections.shuffle(Arrays.asList(files));
        for (int i = 0; i < number && i < files.length; i++) {
            File toSend = files[i];
            System.out.println("[MUSIC]Sending: " + toSend.getAbsolutePath());
            SendAudio audio = new SendAudio(chatId, new InputFile(toSend));
            try {
                System.out.println("[MUSIC]Sending " + group + " music: " + (Main.bot.execute(audio) != null ? "Success" : "failure"));
            } catch (TelegramApiException e) {
                e.printStackTrace();
                return config.getProperty(config.getConfigFile(), "nothavemusicgroup");
            }
        }
        return "";
    }

    //for F
    public String getF(Config config, Message message) {
        double a = Math.random();
        System.out.println("a = " + a);
        //Special for Gleb Babushkin
        if (message.getFrom().getId() == Integer.parseInt(config.getProperty(config.getConfigFile(), "glebid")) &&
                a <= Double.parseDouble(config.getProperty(config.getConfigFile(), "glebfchance")))
            return "";

        String textMsg = message.getText();
        //getting multiple f's
        if (textMsg.contains("x")) {
            int num = Integer.parseInt(textMsg.split("x")[1].trim());
            if (num <= 5) {
                StringBuilder res = new StringBuilder();
                for (int i = 0; i < num; i++) {
                    res.append("\n\n").append(config.parse(config.getConfigFile(), "F"));
                }
                return res.toString();
            }
        }
        return config.parse(config.getConfigFile(), "F");
    }

    //adding music
    public String addMusic(Config config, String messageAdd, Message audioMessage) {
        System.out.println("[MUSIC]Adding music...");
        System.out.println("[MUSIC]Folder to save: " + messageAdd);
        //generating save file
        File toSave = new File(config.getProperty(config.getConfigFile(), "musicfolderpath") + "/" + messageAdd);
        //creating new folder for the group
        System.out.println("[MUSIC]Creating new folder for this track: " + toSave.mkdir());
        File tmp = new File(toSave.getAbsolutePath() + "/savetemp.mp3");
        //downloading
        File savedMusic = downloadMusic(audioMessage, tmp);
        System.out.println("[MUSIC]Saved: " + savedMusic.exists());
        //reformatting music storage
        FormatFolder.formatFolder(toSave);
        //second call to rename savetemp.mp3
        FormatFolder.formatFolder(toSave);
        return config.getProperty(config.getConfigFile(), "musicsaved").replaceAll("\\{path}", toSave.getName());
    }

    //downloading music from inMess to path
    public File downloadMusic(Message inMess, File path) {
        if (inMess.hasAudio()) {
            System.out.println("[MUSIC]Downloading music: " + inMess.getAudio().getFileName());
            //generating properties
            String doc_id = inMess.getAudio().getFileId();
            String doc_name = inMess.getAudio().getFileName();
            String doc_mine = inMess.getAudio().getMimeType();
            int doc_size = inMess.getAudio().getFileSize();

            //setting audio properties
            Audio audio = new Audio();
            audio.setMimeType(doc_mine);
            audio.setFileName(doc_name);
            audio.setFileSize(doc_size);
            audio.setFileId(doc_id);

            //downloading
            GetFile getFile = new GetFile();
            getFile.setFileId(audio.getFileId());
            try {
                org.telegram.telegrambots.meta.api.objects.File file = Main.bot.execute(getFile);
                System.out.println("[MUSIC]Starting new downloading thread: ");
                new Thread(() -> {
                    try {
                        Main.bot.downloadFile(file, path);
                        System.out.println("[MUSIC]Downloaded track: " + inMess.getAudio().getFileName() + " in " + path.getAbsolutePath());
                    } catch (TelegramApiException e) {
                        e.printStackTrace();
                    }
                }).start();
                return path;
            } catch (TelegramApiException e) {
                e.printStackTrace();
                return null;
            }
        }
        return null;
    }

    //for /ball
    public String getBallAnswer(Config config) {
        System.out.println("[BALL]Getting ball...");
        double yesChance, probablyChance, noChance;
        String ballAnswer;
        yesChance = Double.parseDouble(config.getProperty(config.getConfigFile(), "ballyeschance"));
        probablyChance = Double.parseDouble(config.getProperty(config.getConfigFile(), "ballprobablychance"));
        noChance = Double.parseDouble(config.getProperty(config.getConfigFile(), "ballnochance"));
        //wrong chances
        if (yesChance + probablyChance + noChance != 1.0) {
            System.out.println(config.getProperty(config.getConfigFile(), "ballwrongchance"));
            return "";
        }

        if (Math.random() <= yesChance)
            ballAnswer = config.getRandomFromParse(config.getConfigFile(), "ball probably");
        else if (Math.random() <= yesChance + probablyChance)
            ballAnswer = config.getRandomFromParse(config.getConfigFile(), "ball no");
        else
            ballAnswer = config.getRandomFromParse(config.getConfigFile(), "ball yes");

        return ballAnswer;
    }

    //for fucking word
    public String getFuckingSlavesText(Config config) {
        String fucking = config.parse(config.getConfigFile(), "fucking");
        if (Math.random() < Double.parseDouble(fucking.split("\n")[0].split("\\)")[0]))
            return fucking.split("\n")[0].split("\\)")[1];
        return fucking.split("\n")[1].split("\\)")[1];
    }

    //for /future
    public String getFuture(Config config, Message inMess) {
        String msg = inMess.getText();
        String future;
        msg = msg.replaceAll("/future", "");

        //nick is more than 1 word long
        if (msg.split(" ").length > 2)
            return config.getProperty(config.getConfigFile(), "futureusage");

        String nick;
        //getting nick with @
        nick = msg.split(" ").length == 1 ? "@" + inMess.getFrom().getUserName() : msg.split(" ")[1];
        System.out.println("Getting future for " + nick);
        //getting 'future'
        future = config.getRandomFromParse(config.getConfigFile(), "future");

        return config.getProperty(config.getConfigFile(), "futuretext")
                .replaceAll("\\{nick}", nick) + " - " + future;
    }


    //for bad bot words
    public String getSadText(Config config) {
        return config.getRandomFromParse(config.getConfigFile(), "sad text");
    }

    //BOTTLE GAME FUNCTIONS_____________________________________________
    //for /newbottlegame
    public String getStartBottleGameText(BottleGame game, Config config) {
        return config.getProperty(config.getConfigFile(), "bottlecreationtext").replaceAll("\\{id}", String.valueOf(game.getId()));
    }

    //bottle game round
    public String getBottleGameRound(List<BottleGame> games, User spinner, Config config) {
        User res = null;
        BottleGame cur;
        int count = 0;
        //spinner must be a different player
        for (int i = 0; i < games.size(); i++) {
            cur = games.get(i);
            if (cur.containsPlayer(spinner)) {
                do {
                    res = cur.spin();
                    count++;
                } while (res.equals(spinner) && count < 10);
                break;
            }
        }
        //spinner is one in a bottle game
        if (count >= 10)
            return config.getProperty(config.getConfigFile(), "bottleoneplayer");
        //spinner isn't in a bottle game
        if (res == null)
            return config.getProperty(config.getConfigFile(), "notinabottlegametext");

        String activity = config.getRandomFromParse(config.getConfigFile(), "bottle");
        //formatting
        activity = activity.replaceAll("\\{nick1}", "@" + spinner.getUserName());
        activity = activity.replaceAll("\\{nick2}", "@" + res.getUserName());

        return activity;
    }

    //for top secret message
    public String getTopSecret(Config config) {
        if (Math.random() <= Double.parseDouble(config.parse(config.getConfigFile(), "top secret").split("\\)")[0])) {
            return config.parse(config.getConfigFile(), "top secret").split("\\)")[1];
        }
        return "";
    }

    //for user not in a bottle game case
    public String getNotInABottleGameText(Config config) {
        return config.getProperty(config.getConfigFile(), "notinabottlegametext");
    }

    //for bottle game deleted case
    public String getBottleGameDeletedText(BottleGame game, Config config) {
        return config.getProperty(config.getConfigFile(), "bottlegamedeleted").replaceAll("\\{id}", String.valueOf(game.getId()));
    }

    //bottle game usage text
    public String getBottleGameUsageText(Config config) {
        return config.getProperty(config.getConfigFile(), "bottleusagetext");
    }

    //for user has left a bottle game case
    public String getLeaveGameText(BottleGame game, String userName, Config config) {
        return config.getProperty(config.getConfigFile(), "bottlegameuserleft")
                .replaceAll("\\{nick}", userName)
                .replaceAll("\\{id}", String.valueOf(game.getId()));
    }

    //for user has joined a bottle game case
    public String getBottleGameJoinText(BottleGame game, Config config) {
        return config.getProperty(config.getConfigFile(), "bottlegamejoinedtext").replaceAll("\\{id}", String.valueOf(game.getId()));
    }

    //for not existing bottle game case
    public String getNoBottleGameText(Config config) {
        return config.getProperty(config.getConfigFile(), "nosuchbottlegametext");
    }

    //for wrong bottle game usage
    public String getInvalidBottleCommand(Config config) {
        return config.getProperty(config.getConfigFile(), "bottleusagetext");
    }
    //BOTTLE GAME FUNCTIONS_____________________________________________
}
