package uz.khoshimjonov.api;

import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;
import uz.khoshimjonov.dto.PrayerTimesResponse;

import javax.net.ssl.*;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

public class AlAdhanApi {
    private static final String URL = "https://api.aladhan.com/v1/timings/%s?school=%s&method=%s&latitude=%s&longitude=%s";
    private final ExecutorService EXECUTOR = Executors.newSingleThreadExecutor();

    private final Gson GSON = new Gson();

    public void sendRequestAsync(String timings, int school, int method, String latitude, String longitude, Consumer<PrayerTimesResponse> callback) {
        executeRequestAsync(
                String.format(URL, timings, school, method, latitude, longitude),
                PrayerTimesResponse.class,
                r -> {
                    if (r != null) {
                        callback.accept((PrayerTimesResponse) r);
                    }
                }
        );
    }

    private <T> void executeRequestAsync(String url, Class<?> type, Consumer<T> callback) {
        EXECUTOR.execute(() -> {
            try {
                callback.accept(sendRequest(url, type));
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    public PrayerTimesResponse getSalahTimes(String timings, int school, int method, String latitude, String longitude) throws Exception {
        return sendRequest( String.format(URL, timings, school, method, latitude, longitude), PrayerTimesResponse.class);
    }

    private <T> T sendRequest(String url, Class<?> type) throws Exception {

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
