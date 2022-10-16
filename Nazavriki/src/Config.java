//Class for getting configs and properties from files

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

public class Config {
    private final File pathFile = new File("");
    //path to config folder
    private final String configFolder = pathFile.getAbsolutePath() + "/Config/";
    //path to logs folder
    private final String loggerfolderpath;
    //file config.dat
    private String configFile;
    //user list (for /info)
    private ArrayList<String> users;

    //initialization
    public Config() {
        configFile = readFile(new File(configFolder + "config.dat"));
        loggerfolderpath = getProperty(getConfigFile(), "loggerfolderpath");
        initUsers(new File(configFolder + "userinfo.dat"));
    }

    //reading a file
    private static String readFile(File file) {
        StringBuilder sb = new StringBuilder();

        try (BufferedReader read = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = read.readLine()) != null) {
                sb.append(line).append(System.lineSeparator());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return sb.toString();
    }

    //path for logger
    public String getLoggerFolderPath() {
        return loggerfolderpath;
    }

    //getting random string from parsing a label
    public String getRandomFromParse(String file, String parse) {
        String[] split = parse(file, parse).split("\n");
        if (Math.random() <= 0.5)
            Collections.shuffle(Arrays.asList(split));
        Collections.shuffle(Arrays.asList(split));
        if (Math.random() <= 0.5) //for better randomness
            Collections.shuffle(Arrays.asList(split));
        Collections.shuffle(Arrays.asList(split));
        return split[0];
    }

    //parsing from <label> to </label> in file
    public String parse(String file, String label) {
        StringBuilder res = new StringBuilder();
        label = label.trim();

        if (!file.contains("<" + label + ">") ||
                !file.contains("</" + label + ">") ||
                file.indexOf("<" + label + ">") > file.indexOf("</" + label + ">")
        ) {
            return null; //file doesn't have a propper format
        } else {
            res.append(file.substring(
                    file.indexOf("<" + label + ">") + ("<" + label + ">").length(),
                    file.indexOf("</" + label + ">")
            ).trim());
        }
        String[] split = res.toString().split("\n");
        res = new StringBuilder();
        for (int i = 0; i < split.length; i++) {
            res.append(split[i].trim()).append("\n"); //trimming each string in result
        }
        return res.toString().trim();
    }

    //getting config file updated every time
    public String getConfigFile() {
        configFile = readFile(new File(configFolder + "config.dat"));
        return configFile;
    }

    //getting property=propertyvalue in file
    public String getProperty(String file, String property) {
        String res = file.split(property + "=")[1];
        res = res.substring(0, res.indexOf("\n")).trim();
        return res;
    }


    //list of users
    public ArrayList<String> getUsers() {
        return users;
    }

    //getting info about a user
    public String getUserInfo(String user) {
        user = user.trim();

        if (!users.contains(user)) {
            return getProperty(getConfigFile(), "nouserfoundtext");
        }

        for (int i = 0; i < users.size(); i++) {
            if (users.get(i).contains(user)) {
                String toParse;
                if (i % 2 == 0)
                    toParse = users.get(i) + "|" + users.get(i + 1); //parsing by name
                else
                    toParse = users.get(i - 1) + "|" + users.get(i); //parsing by nick

                return parse(readFile(new File(getProperty(getConfigFile(), "userinfopath"))), toParse);
            }
        }
        return getProperty(getConfigFile(), "nouserfoundtext");
    }

    //getting a prefix for a weather command
    public String getWeatherMsgText() {
        return getProperty(getConfigFile(), "weathermsgtext");
    }

    //user list initialization
    private String initUsers(File file) {
        StringBuilder res = new StringBuilder();

        String text = readFile(file);
        String[] split = text.split("_______________________");
        users = new ArrayList<>();
        String[] splitUser = split[0].split("\\n");
        String[] splitUserUser = new String[splitUser.length * 2];
        for (int i = 0; i < splitUserUser.length; i++) {
            splitUserUser[i] = splitUser[i / 2].split("\\|")[i % 2].trim();
        }
        users.addAll(Arrays.asList(splitUserUser));

        for (int i = 0; i < text.split("\n").length; i++) {
            res.append(text.split("\n")[i].trim()).append("\n");
        }

        return res.toString();
    }
}
