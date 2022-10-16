//Main bot class for messaging and actions

import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class Bot extends TelegramLongPollingBot {
    //list of all bottle games, handled by this bot
    private final List<BottleGame> bottleGames;

    //config
    private final Config config;
    //logger (message saving)
    private final Logger logger;
    //messaging answers
    private final MessageAnswers messageAnswers;
    //commands actions
    private final Commands commandHandler;

    //music utils
    private boolean musicAddMode = false; //is a user now adding music
    private String musicSavingPath; //path to save user's music
    private User lastMusicUser = null; //last user, who tried to add the music

    public Bot() {
        //creating all needed objects
        bottleGames = new ArrayList<>();
        config = new Config();
        logger = new Logger(config);
        messageAnswers = new MessageAnswers();
        commandHandler = new Commands(messageAnswers, config);
        //weather configuration
        WeatherData.setOWMClient(config);
    }

    @Override
    public String getBotUsername() {
        //getting bot configs, depending on testing mode
        if (config.getProperty(config.getConfigFile(), "TEST").equals("TRUE"))
            return config.getProperty(config.getConfigFile(), "TEST_BOT_NAME");
        else if (config.getProperty(config.getConfigFile(), "TEST").equals("FALSE"))
            return config.getProperty(config.getConfigFile(), "RELEASE_BOT_NAME");
        else
            return null;
    }

    @Override
    public String getBotToken() {
        //getting bot configs, depending on testing mode
        if (config.getProperty(config.getConfigFile(), "TEST").equals("TRUE"))
            return config.getProperty(config.getConfigFile(), "TEST_BOT_TOKEN");
        else if (config.getProperty(config.getConfigFile(), "TEST").equals("FALSE"))
            return config.getProperty(config.getConfigFile(), "RELEASE_BOT_TOKEN");
        else
            return null;
    }


    //main message handling method
    @Override
    public void onUpdateReceived(Update update) {
        try {
            if (update.hasMessage()) {
                //received message
                Message inMess = update.getMessage();
                //Chat id to send the message
                String chatId = inMess.getChatId().toString();
                //getting an answer for the message
                SendMessage response = parseMessage(inMess);

                //responding only if we have an answer to the message
                if (response != null && !response.getText().isBlank()) {
                    response.setChatId(chatId);
                    execute(response);
                }
            }
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    //parsing message method
    public SendMessage parseMessage(Message inMess) throws TelegramApiException {
        //a message the bot will send as answer to inMess
        SendMessage message = new SendMessage();
        //text version of a received message for more clear code
        String textMsg = inMess.getText();
        //a response string to be set as message answer text
        String response;


        //command number
        int command;
        //array for all commands
        String[] commands = {
                "/bottle",
                "/бутылочка",
                "/newbottlegame",
                "/новая игра",
                "/listbottlegames",
                "бутылочки",
                "/joinbottlegame",
                "/войти в бутылочку",
                "/bottlespin",
                "/вращать",
                "/spinbottle",
                "/leavegame",
                "/ливнуть",
                "/kickuser",

                "/dushno",
                "/active",
                config.getWeatherMsgText(),
                "/rickroll",
                "/рикролл",
                "/future",
                "/гадалка",
                "/help",
                "/start",
                "/creator",
                "/schedule",
                "/расписание",
                "/music",
                "/addmusic",
                "/endmusic",
                "/ball",
                "/info",

        };
        System.out.println("config.getWeatherMsgText() = " + config.getWeatherMsgText());

        //array for all words
        String[] words = {
                "пон",
                "иди нахер",
                "иди нахуй",
                "иди нах",
                "пошел нафиг",
                "пошел нахер",
                "пошел нахуй",
                "нахер иди",
                "fucking",
                "спасибо бот",
                "спасибо, бот",
                "спасибо тебе бот",
                "спасибо тебе, бот",
                "спасибо)",
                "спасибо )",
                "работа",
                "поработал",
                "work",
                "бб",
                "ночи",
                "спать",
                "спокойной",
                "споки ноки",
                "поесть",
                "да",
                "кто лох",
                "f"
        };

        System.out.println("__________________________________\nStarting parsing a message");
        //logging received message
        logger.log(inMess);

        //top secret property (sending an answer from configs with a fixed small chance)
        response = messageAnswers.getTopSecret(config);
        message.setReplyToMessageId(inMess.getMessageId());

        //music handling
        if (musicAddMode && inMess.getFrom().equals(lastMusicUser)) {
            response = saveMusic(inMess);
            message.setText(response);
        } else {
            //parsing a command
            command = startsWithCommands(textMsg, commands);
            switch (command) {
                case -1 -> System.out.println("No commands in this message");
                case 0, 1 -> response = commandHandler.getBottleCommand();
                case 2, 3 -> {
                    bottleGames.add(new BottleGame(config));
                    bottleGames.get(bottleGames.size() - 1).setOwner(inMess.getFrom());
                    response = commandHandler.getNewBottleGameCommand(bottleGames);
                }
                case 4, 5 -> response = commandHandler.getListBottleGamesCommand(bottleGames);
                case 6, 7 -> response = commandHandler.getJoinBottleGameCommand(bottleGames, inMess);
                case 8, 9, 10 -> response = commandHandler.getBottleSpinCommand(bottleGames, inMess);
                case 11, 12, 13 -> response = commandHandler.getLeaveBottleGameCommand(bottleGames, inMess);
                case 14 -> commandHandler.sendPhoto(inMess.getChatId().toString(), property("dushnopath"));
                case 15 -> response = commandHandler.getActiveCommand(inMess);
                case 16 -> response = commandHandler.getWeatherCommand(inMess);
                case 17, 18 -> response = commandHandler.getRickrollCommand(inMess);
                case 19, 20 -> response = commandHandler.getFutureCommand(inMess);
                case 21, 22 -> response = commandHandler.getHelpCommand();
                case 23 -> response = commandHandler.getCreatorCommand();
                case 24, 25 -> response = commandHandler.getScheduleCommand(inMess);
                case 26 -> response = commandHandler.getMusicCommand(inMess);
                case 27 -> response = addMusic(inMess); //not a command handler method because of the local variables
                case 28 -> response = endMusic(); //not a command handler method because of the local variables
                case 29 -> response = commandHandler.getBallCommand(inMess);
                case 30 -> response = commandHandler.getInfoCommand(inMess);
            }
            //if no commands had been executed
            if (command == -1) {
                //parsing a word
                command = containsWords(textMsg, words);
                switch (command) {
                    case -1 -> System.out.println("No words in this message");
                    case 0 -> commandHandler.sendPon(inMess.getChatId().toString(), property("ponpath"));
                    case 1, 2, 3, 4, 5, 6, 7 -> response = messageAnswers.getSadText(config);
                    case 8 -> response = messageAnswers.getFuckingSlavesText(config);
                    case 9, 10, 11, 12, 13, 14 -> response = messageAnswers.getThanks(config);
                    case 15, 16, 17 -> response = messageAnswers.getGreatJob(config, inMess);
                    case 18, 19, 20, 21, 22 -> response = messageAnswers.getBye(config);
                    case 23 -> response = messageAnswers.getWereToEat(config);
                    case 24 -> response = messageAnswers.getYes(config);
                    case 25 -> response = messageAnswers.getLoh(config);
                    case 26 -> response = messageAnswers.getF(config, inMess);
                }
            }
            //symbol count based answers are appended to  response
            response += "\n" + messageAnswers.getSymbols(config, textMsg);
            //sending a non-blank message
            if (!response.isBlank())
                message.setText(response);

        }
        System.out.println("Message parsed\n__________________________________\n");

        return message;
    }

    //method for switch-case
    //returns an index of a command from commands in a message
    private int startsWithCommands(String msg, String... commands) {
        for (int i = 0; i < commands.length; i++) {
            if (msg.toLowerCase(Locale.ROOT).startsWith(commands[i].toLowerCase(Locale.ROOT)))
                return i;
        }
        return -1;
    }

    //method for switch-case
    //returns an index of a word from words in a message
    private int containsWords(String msg, String... words) {
        for (int i = 0; i < words.length; i++) {
            if (msg.toLowerCase(Locale.ROOT).contains(words[i].toLowerCase(Locale.ROOT)))
                return i;
        }
        return -1;
    }

    //music message method
    private String saveMusic(Message inMess) {
        if (inMess.hasAudio() && inMess.getFrom().equals(lastMusicUser))
            return messageAnswers.addMusic(config, musicSavingPath, inMess);
        if (inMess.hasText() && inMess.getText().startsWith("/endmusic")) {
            lastMusicUser = null;
            musicAddMode = false;
            musicSavingPath = null;
            return config.getProperty(config.getConfigFile(), "notmusicuser");
        }

        return config.getProperty(config.getConfigFile(), "musicaddhelp");
    }

    //addmusic response method
    private String addMusic(Message inMess) {
        if (inMess.getText().trim().split(" ").length != 1) {
            System.out.println("Going into add music mode");
            musicAddMode = true;
            musicSavingPath = inMess.getText().trim().split(" ")[1];
            lastMusicUser = inMess.getFrom();
            return property("musicsavetext");
        } else
            return property("musicaddhelp");
    }

    //endmusic response method
    private String endMusic() {
        lastMusicUser = null;
        musicAddMode = false;
        musicSavingPath = null;
        return config.getProperty(config.getConfigFile(), "notmusicuser");
    }

    //a more simple way to get properties
    private String property(String property) {
        return config.getProperty(config.getConfigFile(), property);
    }
}