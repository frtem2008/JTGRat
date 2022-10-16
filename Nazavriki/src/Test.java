//Test class, nothing special here
//TODO REMOVE

import java.util.Arrays;

public class Test {
    private static final Config config = new Config();

    public static void main(String[] args) {
        String s = getSchedule(config);
        System.out.println("\n\n\n________________________");
        System.out.println(s);
    }

    private static String getSchedule(Config config) {
        String schedule = config.parse(config.getConfigFile(), "schedule");
        String[] days = config.parse(config.getConfigFile(), "schedule days").split("\n");
        System.out.println("days = " + Arrays.toString(days));
        System.out.println("schedule = " + schedule);
        for (int i = 0; i < days.length; i++) {
            schedule = schedule.replaceAll("<" + days[i] + ">", days[i]);
            schedule = schedule.replaceAll("</" + days[i] + ">", "");
        }
        return schedule;
    }
}
