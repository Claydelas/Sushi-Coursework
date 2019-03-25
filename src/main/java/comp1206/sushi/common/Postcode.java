package comp1206.sushi.common;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import javafx.beans.property.SimpleFloatProperty;
import javafx.beans.property.SimpleStringProperty;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.Map;

public class Postcode extends Model {

    private SimpleStringProperty name;
    private Map<String, Double> latLong;
    private SimpleFloatProperty distance;

    public Postcode(String code) {
        this.name = new SimpleStringProperty(code);
        calculateLatLong();
        this.distance = new SimpleFloatProperty(0);
    }

    public Postcode(String code, Restaurant restaurant) {
        this.name = new SimpleStringProperty(code);
        calculateLatLong();
        calculateDistance(restaurant);
    }

    @Override
    public String getName() {
        return this.name.getValue();
    }

    public void setName(String name) {
        this.name.setValue(name);
    }

    public Number getDistance() {
        return this.distance.getValue();
    }

    public Map<String, Double> getLatLong() {
        return this.latLong;
    }

    //Haversine implementation based on https://gist.github.com/broox/2393548
    protected void calculateDistance(Restaurant restaurant) {
        Postcode destination = restaurant.getLocation();

        double dLat = Math.toRadians(getLatLong().get("lat") - destination.getLatLong().get("lat"));
        double dLon = Math.toRadians(getLatLong().get("long") - destination.getLatLong().get("long"));

        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(destination.getLatLong().get("lat"))) * Math.cos(Math.toRadians(getLatLong().get("lat")))
                        * Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        this.distance = new SimpleFloatProperty((float) (6371 * c));

        System.out.println("Distance between '" + getName() + "' and '" + destination + "' -> " + getDistance() + "km");
    }

    //based on idea by https://stackoverflow.com/a/21964051/11214781
    protected void calculateLatLong() {

        this.latLong = new HashMap<>();
        String sURL = "https://www.southampton.ac.uk/~ob1a12/postcode/postcode.php?postcode=" + getName().replaceAll(" ", "");

        try {
            URL url = new URL(sURL);
            URLConnection request = url.openConnection();
            request.connect();

            JsonParser parser = new JsonParser();
            JsonElement root = parser.parse(new InputStreamReader((InputStream) request.getContent()));
            JsonObject jsonObject = root.getAsJsonObject();

            System.out.println("Raw GET content: " + root);

            latLong.put("lat", jsonObject.get("lat").getAsDouble());
            latLong.put("long", jsonObject.get("long").getAsDouble());

            System.out.println("Mapped entries: " + latLong.entrySet());

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
