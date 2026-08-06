package commands;
import service.*;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

public class BtcCommand implements Command{
    private final OkHttpClient client;
    public BtcCommand(OkHttpClient client){
        this.client = client;
    }
    @Override
    public String execute(long chatId, String args) {
        String url = "https://api.binance.com/api/v3/ticker/price?symbol=BTCUSDT";
        Request request = new Request.Builder().url(url).build();
        try (Response response = client.newCall(request).execute()) {
            String jsonResponse = response.body().string();
            JSONObject obj = new JSONObject(jsonResponse);
            double btcPrice = obj.getDouble("price");
            return "Текущая цена BTC/USD составляет - " + btcPrice;
        } catch (IOException | JSONException e) {
            e.printStackTrace();
            return "Мне не удалось достать курс BTC сейчас. Попробуй позже.";
        }
    }
    @Override
    public String description(){
        return "узнать цену биткоина";
    }
}
