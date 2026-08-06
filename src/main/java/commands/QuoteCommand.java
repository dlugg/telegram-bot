package commands;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

public class QuoteCommand implements Command {
    private final OkHttpClient client;
    public QuoteCommand(OkHttpClient client){
        this.client = client;
    }
    @Override
    public String execute(long chatId, String args) {
        String url = "https://api.animechan.io/v1/quotes/random";
        Request request = new Request.Builder().url(url).build();
        try (Response response = client.newCall(request).execute()){
            String jsonResponse = response.body().string();
            if (response.code() == 200) {
                JSONObject obj = new JSONObject(jsonResponse);

                JSONObject data = obj.getJSONObject("data");
                JSONObject anime = data.getJSONObject("anime");
                JSONObject character = data.getJSONObject("character");

                String name = anime.getString("name");
                String charName = character.getString("name");

                String quote = data.getString("content");
                return "Цитата: " + quote + "\nНазвание аниме: " + name + "\nИмя персонажа: " + charName;
            } else {
                return "Что-то пошло не так... Попробуй позже.";
            }
        } catch (IOException | JSONException e) {
            e.printStackTrace();
            return "Что-то пошло не так... Попробуй позже.";
        }
    }

    @Override
    public String description(){
        return "аниме цитата";
    }
}
