package uz.khoshimjonov.api;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import uz.khoshimjonov.dto.NominatimResponse;
import uz.khoshimjonov.dto.PrayerTimesResponse;

import javax.net.ssl.*;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.URI;
import java.util.List;

public class Api {
    private static final String AL_ADHAN_URL = "https://api.aladhan.com/v1/timings/%s?school=%s&method=%s&latitude=%s&longitude=%s";
    private static final String NOMINATIM_URL = "https://nominatim.openstreetmap.org/search?format=json&limit=1&q=%s";
    private final Gson GSON = new Gson();


    public PrayerTimesResponse getSalahTimes(String timings, int school, int method, String latitude, String longitude) throws Exception {
        return sendRequest( String.format(AL_ADHAN_URL, timings, school, method, latitude, longitude), PrayerTimesResponse.class);
    }

    public List<NominatimResponse> getPositionByAddress(String address) throws Exception {
        Type listType = TypeToken.getParameterized(List.class, NominatimResponse.class).getType();
        return sendRequest(String.format(NOMINATIM_URL, address), listType);
    }

    private <T> T sendRequest(String url, Type type) throws Exception {

        TrustManager[] trustAllCerts = new TrustManager[]{
                new X509TrustManager() {
                    public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                        return null;
                    }

                    public void checkClientTrusted(
                            java.security.cert.X509Certificate[] certs, String authType) {
                    }

                    public void checkServerTrusted(
                            java.security.cert.X509Certificate[] certs, String authType) {
                    }
                }
        };

        HostnameVerifier verifier = (string, sSLSession) -> true;
        try {
            SSLContext sc = SSLContext.getInstance("SSL");
            sc.init(null, trustAllCerts, new java.security.SecureRandom());
            HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
            HttpsURLConnection.setDefaultHostnameVerifier(verifier);
        } catch (Exception e) {
            e.printStackTrace();
        }

        HttpURLConnection connection = (HttpURLConnection) new URI(url).toURL().openConnection();
        connection.setRequestMethod("GET");
        connection.setConnectTimeout(5000);
        connection.setReadTimeout(5000);

        int responseCode = connection.getResponseCode();
        if (responseCode == HttpURLConnection.HTTP_OK) {
            try (JsonReader reader = new JsonReader(new InputStreamReader(connection.getInputStream()))) {
                return this.GSON.fromJson(reader, type);
            }

        } else {
            System.out.println("GET request failed. Response Code: " + responseCode + " message: " + connection.getResponseMessage());
        }
        connection.disconnect();
        return null;
    }
}
