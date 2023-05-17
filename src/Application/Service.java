/**
 *
 *  @author Siedlik Patryk S22811
 *
 */

package zad2;


import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Currency;
import java.util.Locale;


public class Service {
	
	
	private final String countryCode;
	private final String countryCurrency;
	private final String weatherApiKey = "d9d03125310c200e2125ab7f8c4abd5d";
	

	public Service(String country) {
		countryCode = getCountryCode(country);
		countryCurrency = Currency.getInstance(new Locale("", countryCode)).getCurrencyCode();
	}

	public String getWeather(String city) {
		
		String apiUrl = String.format(
				"https://api.openweathermap.org/data/2.5/weather?q=%s,%s&units=metric&appid=%s",
				city,
				countryCode,
				weatherApiKey
			);
		
		try {
			return getUrlContent(apiUrl);
		} catch (Exception e) {
			return "Error getting weather information.";
		}
	}

	public Double getRateFor(String currencyCode) {
		try {
			String url = "https://api.exchangerate.host/latest?base=" + URLEncoder.encode(countryCurrency, "UTF-8")
					+ "&symbols=" + URLEncoder.encode(currencyCode, "UTF-8");
			String jsonString = getUrlContent(url);
			JSONObject jsonObject = (JSONObject) new JSONParser().parse(jsonString);
			JSONObject ratesObject = (JSONObject) jsonObject.get("rates");
			Double rate = (Double) ratesObject.get(currencyCode);
			return rate;
		} catch (Exception e) {
			return null;
		}
	}

	public Double getNBPRate() {
		try {
			String url = "http://www.nbp.pl/kursy/kursya.html";
			String htmlString = getUrlContent(url);
			int startIndex = htmlString.indexOf(countryCurrency + "</td>");
			int middleIndex = htmlString.indexOf("<td>", startIndex + 1);
			int endIndex = htmlString.indexOf("</td>", middleIndex + 1);
			String rateString = htmlString.substring(middleIndex + 4, endIndex).replace(",", ".");
			Double rate = Double.parseDouble(rateString);
			return rate;
		} catch (Exception e) {
			return null;
		}
	}

	public String getCountryCurrency() {
		return countryCurrency;
	}

	public String getUrlContent(String url) throws Exception {
		URL website = new URL(url);
		try (BufferedReader reader = new BufferedReader(new InputStreamReader(website.openStream()))) {
			StringBuilder sb = new StringBuilder();
			String line;
			while ((line = reader.readLine()) != null) {
				sb.append(line);
			}
			
			
			
			return sb.toString();
		}
	}

	private String getCountryCode(String country) {
		Locale[] locales = Locale.getAvailableLocales();
		for (Locale locale : locales) {
			if (country.equals(locale.getDisplayCountry(Locale.ENGLISH))) {
				return locale.getCountry();
			}
		}
		return "";
	}
}