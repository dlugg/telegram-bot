package commands;

import model.State;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.json.JSONException;
import org.json.JSONObject;
import service.StateService;

import java.io.IOException;

public class WeatherCommand implements Command {
    private final StateService stateService;
    private final OkHttpClient client;
    private final String weatherApiKey;

    public WeatherCommand(StateService stateService, OkHttpClient client, String weatherApiKey) {
        this.stateService = stateService;
        this.client = client;
        this.weatherApiKey = weatherApiKey;
    }

    @Override
    public String execute(long chatId, String args) {
        if (args.isBlank()) {
            stateService.setState(chatId, State.WAITING_FOR_WEATHER_CITY);
            return "Напиши название города на английском (или сразу: /weather London)";
        } else {

            String url = "https://api.openweathermap.org/data/2.5/weather?q=" + args + "&appid=" + weatherApiKey + "&units=metric";
            Request request = new Request.Builder().url(url).build();
            try (Response response = client.newCall(request).execute()) {
                String jsonResponse = response.body().string();
                if (response.code() == 200) {
                    JSONObject obj = new JSONObject(jsonResponse);
                    JSONObject main = obj.getJSONObject("main");
                    double temp = main.getDouble("temp");
                    double feels_like_temp = main.getDouble("feels_like");
                    String cityName = obj.getString("name");
                    return "В городе " + cityName + " сейчас " + temp + "°C" +
                            " Ощущается как " + feels_like_temp;

                } else if (response.code() == 401) {
                    return "Мой ключ погоды еще не активировался. Подожди немного!";
                } else if (response.code() == 404) {
                    return ("Город не найден. Проверь английское название!");
                } else {
                    return ("Что-то пошло не так при запросе погоды...");
                }

            } catch (IOException  | JSONException e) {
                e.printStackTrace();
                return ("Что-то пошло не так... Попробуй позже.");
            } finally {
                stateService.setState(chatId, State.IDLE);
            }
        }
    }

    @Override
    public String description(){
        return "прогноз погоды";
    }
}
