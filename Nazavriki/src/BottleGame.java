//Class for bottle game handling
//TODO delete game, set of UID's

import org.telegram.telegrambots.meta.api.objects.User;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class BottleGame {
    //bottle game id
    private final long id;
    //player list
    private final List<User> players;
    //important users
    private User lastUser, owner;
    //config for properties
    private Config config;

    //initialization
    public BottleGame(Config config) {
        this.players = new ArrayList<>();
        id = generateId();
        this.config = config;
    }

    //formatting output
    @Override
    public String toString() {
        String[] playerNicks = new String[players.size()];
        for (int i = 0; i < players.size(); i++) {
            playerNicks[i] = '@' + players.get(i).getUserName();
        }
        return "BottleGame{" +
                (players.size() == 0 ? "No players" : "players: " + Arrays.toString(playerNicks)) +
                ", id=" + id +
                '}';
    }

    //removing player by username
    public void removePlayer(String userName) {
        for (int i = 0; i < players.size(); i++) {
            if (players.get(i).getUserName().equals(userName)) {
                players.remove(i);
                break;
            }
        }
    }

    //getting owner nickname
    public String getOwnerNick() {
        return owner.getUserName();
    }

    //setting owner
    public void setOwner(User owner) {
        this.owner = owner;
    }

    //generating random id from 1 to maxId from config
    private long generateId() {
        int maxId = Integer.parseInt(config.getProperty(config.getConfigFile(), "maxbottlegameid"));
        return (long) (Math.random() * (maxId - 1) + 1);
    }

    //adding new player
    public void addPlayer(User user) {
        players.add(user);
    }

    //is a player in the game by username
    public boolean containsPlayer(String userName) {
        for (int i = 0; i < players.size(); i++) {
            if (players.get(i).getUserName().equals(userName))
                return true;
        }
        return false;
    }

    //is a player in the game
    public boolean containsPlayer(User user) {
        return players.contains(user);
    }

    //spinning
    public User spin() {
        Collections.shuffle(players);
        lastUser = players.get(0);
        return players.get(0);
    }

    //getting bottle game id
    public long getId() {
        return id;
    }
}
