/**
 *
 *  @author Siedlik Patryk S22811
 *
 */

package zad2;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.text.DecimalFormat;


public class Main extends Application {

    private WebView webView;
    private WebEngine webEngine;
    private TextField cityField;
    private TextField countryField;
    private TextField currencyField;
    private TextArea weatherArea;
    private TextArea currencyArea;
    private Label titleLabel;
    private Label weatherLabel;
    private Label currencyLabel;
    private Button getDataButton;
    private Service service;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {

    	webView = new WebView();
        webEngine = webView.getEngine();
        webView.setPrefSize(800, 400);

        titleLabel = new Label("Weather, exchange rates and city description.");
        titleLabel.setFont(new Font(24));

        cityField = new TextField();
        cityField.setPromptText("City");

        countryField = new TextField();
        countryField.setPromptText("Country");

        currencyField = new TextField();
        currencyField.setPromptText("Currency");

        weatherLabel = new Label("Weather:");

        weatherArea = new TextArea();
        weatherArea.setEditable(false);
        weatherArea.setWrapText(true);
        weatherArea.setFont(new Font(12));
        weatherArea.setPrefSize(350, 75);

        currencyLabel = new Label("Currency:");
        currencyArea = new TextArea();
        currencyArea.setEditable(false);
        currencyArea.setWrapText(true);
        currencyArea.setFont(new Font(12));
        currencyArea.setPrefSize(350, 75);
        

        getDataButton = new Button("Get Data");
        getDataButton.setOnAction(e -> getData());

        VBox inputBox = new VBox(10, cityField, countryField, currencyField, getDataButton);
        inputBox.setAlignment(Pos.CENTER);
        inputBox.setPadding(new Insets(10));

        BorderPane mainLayout = new BorderPane();
        mainLayout.setTop(titleLabel);
        mainLayout.setCenter(webView);
        mainLayout.setBottom(new VBox(10, inputBox, weatherLabel, weatherArea, currencyLabel, currencyArea));
        mainLayout.setPadding(new Insets(10));

        Scene scene = new Scene(mainLayout);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private void getData() {

        String city = cityField.getText().trim();
        String country = countryField.getText().trim();
        String currencyCode = currencyField.getText().trim().toUpperCase();

        new Thread(() -> {
            service = new Service(country);
            String weatherJsonString = service.getWeather(city);

            Platform.runLater(() -> {
                try {
                    JSONObject weatherJson = (JSONObject) new JSONParser().parse(weatherJsonString);
                    int cod = ((Long) weatherJson.get("cod")).intValue();
                    if (cod == 200) {
                        JSONArray weatherArray = (JSONArray) weatherJson.get("weather");
                        JSONObject weatherObject = (JSONObject) weatherArray.get(0);
                        String description = (String) weatherObject.get("description");
                        JSONObject mainObject = (JSONObject) weatherJson.get("main");
                        Number temperature = (Number) mainObject.get("temp");
                        Number feelsLike = (Number) mainObject.get("feels_like");
                        Number humidity = (Number) mainObject.get("humidity");

                        String weatherInfo = "Description: " + description + "\n" + "Temperature: " + Math.round(temperature.doubleValue()) + "°C\n"
                                + "Feels like:"
                                + ""
                                + " " + Math.round(feelsLike.doubleValue()) + "°C\n" + "Humidity: " + humidity.doubleValue() + "%";
                        weatherArea.setText(weatherInfo);
                    } else {
                        weatherArea.setText("Error getting weather information: " + weatherJson.get("message"));
                    }
                } catch (Exception ex) {
                    weatherArea.setText("Error getting weather information.");
                }
            });
        }).start();

        new Thread(() -> {
            service = new Service(country);
            String rateInfo = buildRateInfo(currencyCode, country, service);
            Platform.runLater(() -> currencyArea.setText(rateInfo));
        }).start();


        try {
            String url = "https://en.wikipedia.org/wiki/" + city;
            webEngine.load(url);
        } catch (Exception ex) {
            currencyArea.setText("Error getting Wikipedia information.");
        }
    }
    
    
    private String buildRateInfo(String currencyCode, String country, Service service) {
        DecimalFormat decimalFormat = new DecimalFormat("#.####");
        
        try {
            
            Double rate = service.getRateFor(currencyCode);
            Double ratePLN = service.getRateFor("PLN");

            StringBuilder rateInfo = new StringBuilder();
            rateInfo.append("1 ").append(service.getCountryCurrency()).append(" = ").append(decimalFormat.format(rate)).append(" ").append(currencyCode).append("\n")
                    .append("1 ").append(currencyCode).append(" = ").append(decimalFormat.format(1 / rate)).append(" ").append(service.getCountryCurrency());

            if (!country.equalsIgnoreCase("Poland")) {
                rateInfo.append("\n1 ").append(service.getCountryCurrency()).append(" = ").append(decimalFormat.format(ratePLN)).append(" PLN\n")
                        .append("1 ").append("PLN").append(" = ").append(decimalFormat.format(1 / ratePLN)).append(" ").append(service.getCountryCurrency());
            }

            return rateInfo.toString();
        } catch (Exception ex) {
            return "Error getting exchange rate information.";
        }
    }

    
    
}