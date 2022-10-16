// Class for getting weather data from open weather map service

import com.github.prominence.openweathermap.api.OpenWeatherMapClient;
import com.github.prominence.openweathermap.api.enums.Language;
import com.github.prominence.openweathermap.api.enums.UnitSystem;
import com.github.prominence.openweathermap.api.model.weather.Weather;

import java.text.DecimalFormat;
import java.util.ArrayList;

public class WeatherData {
    public static OpenWeatherMapClient openWeatherClient;

    //getting weather
    public static Weather getWeather(String city) {
        return openWeatherClient
                .currentWeather()
                .single()
                .byCityName(city)
                .language(Language.RUSSIAN)
                .unitSystem(UnitSystem.METRIC)
                .retrieve()
                .asJava();
    }

    //formatting
    public static String formatWeather(Weather weather, Config config) {
        String messageText = config.parse(config.getConfigFile(), "weather message");
        return replacePlaceHolders(messageText, weather, config);
    }

    //setting up an OWM client
    public static void setOWMClient(Config config) {
        openWeatherClient = new OpenWeatherMapClient(config.getProperty(config.getConfigFile(), "owmkey"));
        openWeatherClient.setReadTimeout(1000);
        openWeatherClient.setConnectionTimeout(1000);
    }

    //formatting pressure from hectopascals to mercury column millimeters
    private static String parsePressure(Weather weather) {
        return new DecimalFormat("#000.000").format(weather.getAtmosphericPressure().getValue() * 0.7501d);
    }

    //formatting weather message
    private static String replacePlaceHolders(String toFormat, Weather weather, Config config) {
        String noFallText = config.parse(config.getConfigFile(), "noFall");
        toFormat = toFormat.replaceAll("\\{cityName}", weather.getLocation().getName());
        toFormat = toFormat.replaceAll("\\{measureTime}", weather.getCalculationTime().getHour() + ":" +
                weather.getCalculationTime().getMinute() + ":" +
                weather.getCalculationTime().getSecond() + ":");
        toFormat = toFormat.replaceAll("\\{temperature}", String.valueOf(weather.getTemperature().getValue()));
        toFormat = toFormat.replaceAll("\\{feelTemperature}", String.valueOf(weather.getTemperature().getFeelsLike()));
        toFormat = toFormat.replaceAll("\\{windDegrees}", String.valueOf(weather.getWind().getDegrees()));
        toFormat = toFormat.replaceAll("\\{windSpeed}", String.valueOf(weather.getWind().getSpeed()));
        toFormat = toFormat.replaceAll("\\{humidity}", String.valueOf(weather.getHumidity().getValue()));
        toFormat = toFormat.replaceAll("\\{pressure}", String.valueOf(parsePressure(weather)));
        toFormat = toFormat.replaceAll("\\{clouds}", String.valueOf(weather.getClouds().getValue()));

        //special fall replacement
        boolean fall = false;
        String[] split = toFormat.split("\n");
        ArrayList<String> replaces = new ArrayList<>();

        toFormat = toFormat.replaceAll("\\{", "!");

        if (weather.getRain() == null) {
            for (int i = 0; i < split.length; i++) {
                if (split[i].contains("{rainHour}")) {
                    replaces.add(split[i].replaceAll("\\{", "!"));
                }
            }
            for (int i = 0; i < replaces.size(); i++) {
                toFormat = toFormat.replace(replaces.get(i), "");
            }

        } else {
            toFormat = toFormat.replaceAll("!rainHour}", String.valueOf(weather.getRain().getOneHourLevel()));
            fall = true;
        }

        if (weather.getSnow() == null) {
            for (int i = 0; i < split.length; i++) {
                if (split[i].contains("{snowHour}")) {
                    replaces.add(split[i].replaceAll("\\{", "!"));
                }
            }

            for (int i = 0; i < replaces.size(); i++) {
                toFormat = toFormat.replace(replaces.get(i), "");
            }
        } else {
            toFormat = toFormat.replaceAll("!snowHour}", String.valueOf(weather.getRain().getOneHourLevel()));
            fall = true;
        }

        if (!fall) {
            toFormat = toFormat.replaceAll("!nofall}", noFallText);
        } else {
            toFormat = toFormat.replaceAll("!nofall}", "");
        }

        return toFormat;
    }
}
