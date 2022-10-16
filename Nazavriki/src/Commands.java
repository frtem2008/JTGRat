//Class for command handling

import com.github.prominence.openweathermap.api.model.weather.Weather;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.methods.send.SendSticker;
import org.telegram.telegrambots.meta.api.methods.send.SendVideo;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public class Commands {
    //message answers handler
    private final MessageAnswers messageAnswers;
    //config for parsing
    private final Config config;

    //initialization
    public Commands(MessageAnswers messageAnswers, Config config) {
        this.messageAnswers = messageAnswers;
        this.config = config;
    }

    //bottle response
    public String getBottleCommand() {
        return messageAnswers.getBottleGameUsageText(config);
    }

    //newbottlegame response
    public String getNewBottleGameCommand(List<BottleGame> bottleGames) {
        return messageAnswers.getStartBottleGameText(bottleGames.get(bottleGames.size() - 1), config);
    }

    //listbottlegames response
    public String getListBottleGamesCommand(List<BottleGame> bottleGames) {
        String[] split = Arrays.toString(bottleGames.toArray()).split("BottleGame");
        String response;

        for (int i = 0; i < split.length; i++)
            split[i] += "\n";

        StringBuilder res = new StringBuilder();
        for (String s : split)
            res.append(s);

        response = res.toString().replaceAll("\\[", "").replaceAll("]", "");

        if (response.isBlank())
            response = "No bottle games yet";
        return response;
    }

    //leavegame response
    public String getLeaveBottleGameCommand(List<BottleGame> bottleGames, Message inMess) {
        int i;
        String toLeave = null, textMsg = inMess.getText(), response = "";
        if (textMsg.toLowerCase(Locale.ROOT).contains("/leavegame"))
            toLeave = inMess.getFrom().getUserName();
        else if (textMsg.toLowerCase(Locale.ROOT).matches("/kickuser \\w+"))
            toLeave = textMsg.toLowerCase(Locale.ROOT).split("/kickuser")[1];
        for (i = 0; i < bottleGames.size(); i++) {
            if (bottleGames.get(i).containsPlayer(toLeave)) {
                bottleGames.get(i).removePlayer(toLeave);
                if (bottleGames.get(i).getOwnerNick().equals(toLeave)) {
                    response = messageAnswers.getBottleGameDeletedText(bottleGames.get(i), config);
                    bottleGames.remove(i);
                } else
                    response = messageAnswers.getLeaveGameText(bottleGames.get(i), toLeave, config);
                i = -1;
                break;
            }
        }
        if (i != -1)
            response = messageAnswers.getNotInABottleGameText(config);
        return response;
    }

    //spinbottle response
    public String getBottleSpinCommand(List<BottleGame> bottleGames, Message inMess) {
        return messageAnswers.getBottleGameRound(bottleGames, inMess.getFrom(), config);
    }

    //joinbottlegame response
    public String getJoinBottleGameCommand(List<BottleGame> bottleGames, Message inMess) {
        String textMsg = inMess.getText(), response = "";
        long id;
        if (textMsg.toLowerCase(Locale.ROOT).matches("/joinbottlegame \\d+") ||
                textMsg.toLowerCase(Locale.ROOT).matches("/войти в бутылочку \\d+")) {
            if (textMsg.toLowerCase(Locale.ROOT).contains("/joinbottlegame "))
                id = Long.parseLong(textMsg.toLowerCase(Locale.ROOT).split("/joinbottlegame")[1].trim());
            else
                id = Long.parseLong(textMsg.toLowerCase(Locale.ROOT).split("/войти в бутылочку")[1].trim());

            for (int i = 0; i < bottleGames.size(); i++) {
                if (bottleGames.get(i).getId() == id) {
                    bottleGames.get(i).addPlayer(inMess.getFrom());
                    response = messageAnswers.getBottleGameJoinText(bottleGames.get(i), config);
                    id = -1;
                }
            }
            if (id != -1)
                response = messageAnswers.getNoBottleGameText(config);
        } else {
            response = messageAnswers.getInvalidBottleCommand(config);
        }
        return response;
    }

    //send pon message
    public void sendPon(String chatId, String ponPath) throws TelegramApiException {
        if (Math.random() < Double.parseDouble(config.getProperty(config.getConfigFile(), "ponchance")))
            sendVideo(chatId, ponPath);
        else
            sendSticker(chatId, config.getProperty(config.getConfigFile(), "ponstickerid"));
    }

    //schedule response
    public String getScheduleCommand(Message inMess) {
        return messageAnswers.getSchedule(config, inMess.getText());
    }

    //music response
    public String getMusicCommand(Message inMess) {
        return messageAnswers.sendMusic(config, inMess.getChatId().toString(), inMess.getText().replaceAll("/music", ""));
    }

    //ball response
    public String getBallCommand(Message inMess) {
        if (inMess.getText().endsWith("?"))
            return messageAnswers.getBallAnswer(config);
        return "";
    }

    //weather response
    public String getWeatherCommand(Message inMess) {
        String textMsg = inMess.getText();
        textMsg = textMsg.toLowerCase(Locale.ROOT);
        String city = textMsg.replaceAll(config.getWeatherMsgText(), "").trim();
        System.out.println("Getting weather for city: " + city);
        Weather weather = WeatherData.getWeather(city);

        return WeatherData.formatWeather(weather, config);
    }

    //info response
    public String getInfoCommand(Message inMess) {
        System.out.println("Getting info for");
        String textMsg = inMess.getText();
        if (textMsg.equals("/info")) {
            return config.getUsers().toString();
        } else {
            System.out.println("Getting info for " + textMsg);
            textMsg = textMsg.replace("/info @", "");
            textMsg = textMsg.replace("/info ", "");
            return config.getUserInfo(textMsg);
        }
    }

    //help response
    public String getHelpCommand() {
        return config.parse(config.getConfigFile(), "help");
    }

    //creator response
    public String getCreatorCommand() {
        return config.parse(config.getConfigFile(), "creator");
    }

    //future response
    public String getFutureCommand(Message inMess) {
        return messageAnswers.getFuture(config, inMess);
    }

    //rickroll response
    public String getRickrollCommand(Message inMess) throws TelegramApiException {
        if (Math.random() < Double.parseDouble(config.getProperty(config.getConfigFile(), "matthewchance")))
            sendSticker(inMess.getChatId().toString(), config.getProperty(config.getConfigFile(), "matthewsticker"));
        return config.getProperty(config.getConfigFile(), "rickrolllink");
    }

    //active response
    public String getActiveCommand(Message inMess) {
        String textMsg = inMess.getText();
        if (textMsg.replace("/active", "").trim().equals(""))
            return messageAnswers.getActiveText(config, inMess.getFrom().getUserName());
        else
            return messageAnswers.getActiveText(config, textMsg.replaceAll("/active", ""));
    }

    //method to send a video with path "path" to chat "chatId"
    public void sendVideo(String chatId, String path) throws TelegramApiException {
        SendVideo video = new SendVideo(chatId, new InputFile(new File(path)));
        System.out.println("video = " + video);
        System.out.println("Sending video: " + (Main.bot.execute(video) != null ? "Success" : "failure"));
    }

    //method to send a sticker with stickerId to chat "chatId"
    public void sendSticker(String chatId, String stickerId) throws TelegramApiException {
        SendSticker sticker = new SendSticker(chatId, new InputFile(stickerId));
        System.out.println("Sending sticker: " + (Main.bot.execute(sticker) != null ? "Success" : "failure"));
    }

    //method to send a photo with path "path" to chat "chatId"
    public void sendPhoto(String chatId, String path) throws TelegramApiException {
        SendPhoto photo = new SendPhoto(chatId, new InputFile(new File(path)));
        System.out.println("Sending photo: " + (Main.bot.execute(photo) != null ? "Success" : "failure"));
    }
}
