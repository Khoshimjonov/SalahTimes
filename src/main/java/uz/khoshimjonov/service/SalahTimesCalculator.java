package uz.khoshimjonov.service;

import lombok.Data;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * Salah Times Calculator
 */
public class SalahTimesCalculator {

    // ==================== CALCULATION METHODS ====================

    public enum CalculationMethod {
        MWL("Muslim World League", 18.0, 17.0, 0),
        ISNA("Islamic Society of North America", 15.0, 15.0, 0),
        EGYPT("Egyptian General Authority of Survey", 19.5, 17.5, 0),
        MAKKAH("Umm Al-Qura University, Makkah", 18.5, 0, 90),
        KARACHI("University of Islamic Sciences, Karachi", 18.0, 18.0, 0),
        TEHRAN("Institute of Geophysics, University of Tehran", 17.7, 14.0, 0),
        JAFARI("Shia Ithna-Ashari, Leva Institute, Qum", 16.0, 14.0, 0),
        SINGAPORE("Singapore Islamic Religious Council", 20.0, 18.0, 0),
        TURKEY("Diyanet İşleri Başkanlığı, Turkey", 18.0, 17.0, 0),
        DUBAI("Gulf Region", 18.2, 18.2, 0),
        KUWAIT("Kuwait", 18.0, 17.5, 0),
        QATAR("Qatar", 18.0, 0, 90),
        RUSSIA("Spiritual Administration of Muslims of Russia", 16.0, 15.0, 0),
        FRANCE("Union of Islamic Organizations of France", 12.0, 12.0, 0);

        private final String displayName;
        private final double fajrAngle;
        private final double ishaAngle;
        private final int ishaMinutes;

        CalculationMethod(String displayName, double fajrAngle, double ishaAngle, int ishaMinutes) {
            this.displayName = displayName;
            this.fajrAngle = fajrAngle;
            this.ishaAngle = ishaAngle;
            this.ishaMinutes = ishaMinutes;
        }

        public String getDisplayName() { return displayName; }
        public double getFajrAngle() { return fajrAngle; }
        public double getIshaAngle() { return ishaAngle; }
        public int getIshaMinutes() { return ishaMinutes; }

        public static CalculationMethod getByCode(int code) {
            return switch (code) {
                case 1 -> KARACHI;
                case 2 -> ISNA;
                case 3 -> MWL;
                case 4 -> MAKKAH;
                case 5 -> EGYPT;
                case 7 -> TEHRAN;
                case 8, 16 -> DUBAI;
                case 9 -> KUWAIT;
                case 10 -> QATAR;
                case 11 -> SINGAPORE;
                case 12 -> FRANCE;
                case 13 -> TURKEY;
                case 14 -> RUSSIA;
                default -> MWL;
            };
        }
    }

    public enum AsrMethod {
        SHAFII(1),   // Shafi'i, Maliki, Hanbali
        HANAFI(2);   // Hanafi

        private final int shadowRatio;

        AsrMethod(int shadowRatio) {
            this.shadowRatio = shadowRatio;
        }

        public int getShadowRatio() { return shadowRatio; }

        public static AsrMethod getByCode(int code) {
            return switch (code) {
                case 0 -> SHAFII;
                case 1 -> HANAFI;
                default -> SHAFII;
            };
        }
    }

    public enum HighLatMethod {
        NONE,
        NIGHT_MIDDLE,
        ONE_SEVENTH,
        ANGLE_BASED
    }

    // ==================== COORDINATES ====================

    public static class Coordinates {
        public final double latitude;
        public final double longitude;
        public final double elevation;
        public final ZoneId timezone;

        public Coordinates(double latitude, double longitude) {
            this(latitude, longitude, 0, ZoneId.systemDefault());
        }

        public Coordinates(double latitude, double longitude, double elevation, ZoneId timezone) {
            if (latitude < -90 || latitude > 90) {
                throw new IllegalArgumentException("Latitude must be between -90 and 90");
            }
            if (longitude < -180 || longitude > 180) {
                throw new IllegalArgumentException("Longitude must be between -180 and 180");
            }
            this.latitude = latitude;
            this.longitude = longitude;
            this.elevation = Math.max(0, elevation);
            this.timezone = timezone != null ? timezone : ZoneId.systemDefault();
        }

        @Override
        public String toString() {
            return String.format("(%.4f, %.4f, %.0fm)", latitude, longitude, elevation);
        }
    }

    // ==================== PRAYER TIMES RESULT ====================

    @Data
    public static class PrayerTimes {
        public final LocalDate date;
        public final LocalTime imsak;
        public final LocalTime fajr;
        public final LocalTime sunrise;
        public final LocalTime dhuhr;
        public final LocalTime asr;
        public final LocalTime maghrib;
        public final LocalTime isha;
        public final LocalTime midnight;
        public final LocalTime lastThird;

        public PrayerTimes(LocalDate date, double[] times) {
            this.date = date;
            this.imsak = toTime(times[0]);
            this.fajr = toTime(times[1]);
            this.sunrise = toTime(times[2]);
            this.dhuhr = toTime(times[3]);
            this.asr = toTime(times[4]);
            this.maghrib = toTime(times[5]);
            this.isha = toTime(times[6]);
            this.midnight = toTime(times[7]);
            this.lastThird = toTime(times[8]);
        }

        private static LocalTime toTime(double hours) {
            if (Double.isNaN(hours) || Double.isInfinite(hours)) {
                return null;
            }

            // Normalize to 0-24 range
            hours = hours % 24;
            if (hours < 0) {
                hours += 24;
            }

            // Convert to total seconds and round
            int totalSeconds = (int) Math.round(hours * 3600);

            // Handle edge case of rounding to 24:00:00
            if (totalSeconds >= 86400) {
                totalSeconds = totalSeconds % 86400;
            }

            int h = totalSeconds / 3600;
            int m = (totalSeconds % 3600) / 60;
            int s = totalSeconds % 60;

            return LocalTime.of(h, m, s);
        }

        @Override
        public String toString() {
            DateTimeFormatter fmt = DateTimeFormatter.ofPattern("HH:mm");
            return String.format(
                    "Prayer Times for %s%n" +
                            "─────────────────────────%n" +
                            "Imsak:   %s%n" +
                            "Fajr:    %s%n" +
                            "Sunrise: %s%n" +
                            "Dhuhr:   %s%n" +
                            "Asr:     %s%n" +
                            "Maghrib: %s%n" +
                            "Isha:    %s%n" +
                            "─────────────────────────%n" +
                            "Midnight:   %s%n" +
                            "Last Third: %s%n",
                    date,
                    formatTime(imsak, fmt),
                    formatTime(fajr, fmt),
                    formatTime(sunrise, fmt),
                    formatTime(dhuhr, fmt),
                    formatTime(asr, fmt),
                    formatTime(maghrib, fmt),
                    formatTime(isha, fmt),
                    formatTime(midnight, fmt),
                    formatTime(lastThird, fmt)
            );
        }

        private static String formatTime(LocalTime time, DateTimeFormatter fmt) {
            return time != null ? time.format(fmt) : "N/A";
        }

        public String toJson() {
            DateTimeFormatter fmt = DateTimeFormatter.ofPattern("HH:mm:ss");
            return String.format(
                    "{\"date\":\"%s\",\"imsak\":\"%s\",\"fajr\":\"%s\",\"sunrise\":\"%s\"," +
                            "\"dhuhr\":\"%s\",\"asr\":\"%s\",\"maghrib\":\"%s\",\"isha\":\"%s\"," +
                            "\"midnight\":\"%s\",\"lastThird\":\"%s\"}",
                    date,
                    formatTimeJson(imsak, fmt),
                    formatTimeJson(fajr, fmt),
                    formatTimeJson(sunrise, fmt),
                    formatTimeJson(dhuhr, fmt),
                    formatTimeJson(asr, fmt),
                    formatTimeJson(maghrib, fmt),
                    formatTimeJson(isha, fmt),
                    formatTimeJson(midnight, fmt),
                    formatTimeJson(lastThird, fmt)
            );
        }

        private static String formatTimeJson(LocalTime time, DateTimeFormatter fmt) {
            return time != null ? time.format(fmt) : "null";
        }
    }

    // ==================== CONSTANTS ====================

    private static final double DEG_TO_RAD = Math.PI / 180.0;
    private static final double RAD_TO_DEG = 180.0 / Math.PI;

    // Indices for times array
    private static final int IMSAK = 0;
    private static final int FAJR = 1;
    private static final int SUNRISE = 2;
    private static final int DHUHR = 3;
    private static final int ASR = 4;
    private static final int MAGHRIB = 5;
    private static final int ISHA = 6;
    private static final int MIDNIGHT = 7;
    private static final int LAST_THIRD = 8;

    // ==================== INSTANCE VARIABLES ====================

    private final Coordinates coords;
    private final CalculationMethod method;
    private final AsrMethod asrMethod;
    private final HighLatMethod highLatMethod;
    private final int[] adjustments; // minutes adjustment for each prayer
    private final double imsakMinutes; // minutes before Fajr for Imsak

    // ==================== CONSTRUCTORS ====================

    public SalahTimesCalculator(Coordinates coords) {
        this(coords, CalculationMethod.MWL, AsrMethod.SHAFII, HighLatMethod.ANGLE_BASED);
    }

    public SalahTimesCalculator(Coordinates coords, CalculationMethod method) {
        this(coords, method, AsrMethod.SHAFII, HighLatMethod.ANGLE_BASED);
    }

    public SalahTimesCalculator(Coordinates coords, CalculationMethod method, AsrMethod asrMethod) {
        this(coords, method, asrMethod, HighLatMethod.ANGLE_BASED);
    }

    public SalahTimesCalculator(Coordinates coords, CalculationMethod method,
                                AsrMethod asrMethod, HighLatMethod highLatMethod) {
        this(coords, method, asrMethod, highLatMethod, 10.0);
    }

    public SalahTimesCalculator(Coordinates coords, CalculationMethod method,
                                AsrMethod asrMethod, HighLatMethod highLatMethod,
                                double imsakMinutes) {
        this.coords = coords;
        this.method = method;
        this.asrMethod = asrMethod;
        this.highLatMethod = highLatMethod;
        this.imsakMinutes = imsakMinutes;
        this.adjustments = new int[9];
    }

    // ==================== ADJUSTMENT SETTERS ====================

    public SalahTimesCalculator adjustImsak(int minutes) { adjustments[IMSAK] = minutes; return this; }
    public SalahTimesCalculator adjustFajr(int minutes) { adjustments[FAJR] = minutes; return this; }
    public SalahTimesCalculator adjustSunrise(int minutes) { adjustments[SUNRISE] = minutes; return this; }
    public SalahTimesCalculator adjustDhuhr(int minutes) { adjustments[DHUHR] = minutes; return this; }
    public SalahTimesCalculator adjustAsr(int minutes) { adjustments[ASR] = minutes; return this; }
    public SalahTimesCalculator adjustMaghrib(int minutes) { adjustments[MAGHRIB] = minutes; return this; }
    public SalahTimesCalculator adjustIsha(int minutes) { adjustments[ISHA] = minutes; return this; }

    // ==================== MAIN CALCULATION ====================

    public PrayerTimes calculate(LocalDate date) {
        // Get timezone offset for the specific date (handles DST correctly)
        ZonedDateTime zdt = date.atStartOfDay(coords.timezone);
        double timezoneOffset = zdt.getOffset().getTotalSeconds() / 3600.0;

        // Calculate Julian date at noon
        double jd = julianDate(date.getYear(), date.getMonthValue(), date.getDayOfMonth());

        // Compute prayer times
        double[] times = computePrayerTimes(jd, timezoneOffset);

        // Apply manual adjustments
        for (int i = 0; i < Math.min(adjustments.length, 7); i++) {
            times[i] += adjustments[i] / 60.0;
        }

        // Normalize all times to 0-24 range
        for (int i = 0; i < times.length; i++) {
            times[i] = normalizeHour(times[i]);
        }

        return new PrayerTimes(date, times);
    }

    public PrayerTimes calculate() {
        return calculate(LocalDate.now(coords.timezone));
    }

    public List<PrayerTimes> calculateMonth(int year, int month) {
        List<PrayerTimes> result = new ArrayList<>();
        LocalDate date = LocalDate.of(year, month, 1);
        while (date.getMonthValue() == month) {
            result.add(calculate(date));
            date = date.plusDays(1);
        }
        return result;
    }

    public List<PrayerTimes> calculateYear(int year) {
        List<PrayerTimes> result = new ArrayList<>();
        LocalDate date = LocalDate.of(year, 1, 1);
        while (date.getYear() == year) {
            result.add(calculate(date));
            date = date.plusDays(1);
        }
        return result;
    }

    // ==================== CORE ALGORITHMS ====================

    private double[] computePrayerTimes(double jd, double timezoneOffset) {
        double[] times = new double[9];

        // Calculate sun position parameters
        double decl = sunDeclination(jd);
        double eqt = equationOfTime(jd);

        // Calculate Dhuhr (solar noon)
        double dhuhr = 12.0 + timezoneOffset - coords.longitude / 15.0 - eqt;

        // Calculate sunrise and sunset angles (including elevation adjustment)
        double riseSetAngle = 0.833 + 0.0347 * Math.sqrt(coords.elevation);

        // Calculate prayer times
        double sunriseHA = hourAngle(riseSetAngle, decl);
        double fajrHA = hourAngle(method.getFajrAngle(), decl);
        double asrHA = asrHourAngle(decl);
        double ishaHA = hourAngle(method.getIshaAngle(), decl);

        times[DHUHR] = dhuhr;
        times[SUNRISE] = dhuhr - sunriseHA;
        times[MAGHRIB] = dhuhr + sunriseHA;
        times[FAJR] = dhuhr - fajrHA;
        times[ASR] = dhuhr + asrHA;

        // Isha calculation
        if (method.getIshaMinutes() > 0) {
            times[ISHA] = times[MAGHRIB] + method.getIshaMinutes() / 60.0;
        } else {
            times[ISHA] = dhuhr + ishaHA;
        }

        // Imsak (10 minutes before Fajr by default)
        times[IMSAK] = times[FAJR] - imsakMinutes / 60.0;

        // Apply high latitude adjustments if needed
        times = adjustHighLatitude(times, dhuhr);

        // Calculate midnight and last third of night
        // Using next day's Fajr for accurate calculation
        double nextDayDecl = sunDeclination(jd + 1);
        double nextDayEqt = equationOfTime(jd + 1);
        double nextDayDhuhr = 12.0 + timezoneOffset - coords.longitude / 15.0 - nextDayEqt;
        double nextDayFajrHA = hourAngle(method.getFajrAngle(), nextDayDecl);
        double nextDayFajr = nextDayDhuhr - nextDayFajrHA;

        // Night duration from Maghrib to next Fajr
        double nightDuration = (nextDayFajr + 24.0) - times[MAGHRIB];
        if (nightDuration > 24.0) {
            nightDuration -= 24.0;
        }

        times[MIDNIGHT] = times[MAGHRIB] + nightDuration / 2.0;
        times[LAST_THIRD] = times[MAGHRIB] + nightDuration * 2.0 / 3.0;

        return times;
    }

    /**
     * Calculate hour angle for a given sun angle below horizon
     */
    private double hourAngle(double angle, double declination) {
        double latRad = coords.latitude * DEG_TO_RAD;
        double declRad = declination * DEG_TO_RAD;
        double angleRad = angle * DEG_TO_RAD;

        double cosHA = (-Math.sin(angleRad) - Math.sin(latRad) * Math.sin(declRad))
                / (Math.cos(latRad) * Math.cos(declRad));

        // Check if sun doesn't reach this angle (polar day/night)
        if (cosHA < -1.0 || cosHA > 1.0) {
            return Double.NaN;
        }

        return Math.acos(cosHA) * RAD_TO_DEG / 15.0; // Convert to hours
    }

    /**
     * Calculate Asr hour angle based on shadow ratio
     */
    private double asrHourAngle(double declination) {
        double latRad = coords.latitude * DEG_TO_RAD;
        double declRad = declination * DEG_TO_RAD;

        double shadowAngle = Math.atan(1.0 / (asrMethod.getShadowRatio()
                + Math.tan(Math.abs(latRad - declRad))));

        double cosHA = (Math.sin(shadowAngle) - Math.sin(latRad) * Math.sin(declRad))
                / (Math.cos(latRad) * Math.cos(declRad));

        if (cosHA < -1.0 || cosHA > 1.0) {
            return Double.NaN;
        }

        return Math.acos(cosHA) * RAD_TO_DEG / 15.0;
    }

    // ==================== SUN POSITION ====================

    /**
     * Calculate sun declination for a given Julian date
     * Using simplified formula accurate to about 0.01 degrees
     */
    private double sunDeclination(double jd) {
        double D = jd - 2451545.0;
        double g = normalize360(357.529 + 0.98560028 * D);
        double q = normalize360(280.459 + 0.98564736 * D);
        double L = normalize360(q + 1.915 * dsin(g) + 0.020 * dsin(2 * g));
        double e = 23.439 - 0.00000036 * D;

        return darcsin(dsin(e) * dsin(L));
    }

    /**
     * Calculate equation of time in hours
     * Using direct right ascension method (more accurate than approximation)
     */
    private double equationOfTime(double jd) {
        double D = jd - 2451545.0;
        double g = normalize360(357.529 + 0.98560028 * D);
        double q = normalize360(280.459 + 0.98564736 * D);
        double L = normalize360(q + 1.915 * dsin(g) + 0.020 * dsin(2 * g));
        double e = 23.439 - 0.00000036 * D;

        double RA = darctan2(dcos(e) * dsin(L), dcos(L)) / 15.0;

        return q / 15.0 - normalizeHour(RA);
    }

    // ==================== HIGH LATITUDE ADJUSTMENT ====================

    private double[] adjustHighLatitude(double[] times, double dhuhr) {
        if (highLatMethod == HighLatMethod.NONE) {
            return times;
        }

        double sunrise = times[SUNRISE];
        double sunset = times[MAGHRIB];

        // Night duration
        double nightTime = 24.0 - (sunset - sunrise);

        // Adjust Fajr
        double fajrDiff = nightPortion(method.getFajrAngle()) * nightTime;
        if (Double.isNaN(times[FAJR]) || (sunrise - times[FAJR]) > fajrDiff) {
            times[FAJR] = sunrise - fajrDiff;
            times[IMSAK] = times[FAJR] - imsakMinutes / 60.0;
        }

        // Adjust Isha
        double ishaAngle = method.getIshaMinutes() > 0 ? 18.0 : method.getIshaAngle();
        double ishaDiff = nightPortion(ishaAngle) * nightTime;
        if (Double.isNaN(times[ISHA]) || (times[ISHA] - sunset) > ishaDiff) {
            times[ISHA] = sunset + ishaDiff;
        }

        return times;
    }

    private double nightPortion(double angle) {
        switch (highLatMethod) {
            case NIGHT_MIDDLE:
                return 0.5;
            case ONE_SEVENTH:
                return 1.0 / 7.0;
            case ANGLE_BASED:
                return angle / 60.0;
            default:
                return 0;
        }
    }

    // ==================== JULIAN DATE ====================

    /**
     * Calculate Julian date for a given Gregorian date at noon UT
     */
    private static double julianDate(int year, int month, int day) {
        if (month <= 2) {
            year -= 1;
            month += 12;
        }

        int A = year / 100;
        int B = 2 - A + A / 4;

        return Math.floor(365.25 * (year + 4716))
                + Math.floor(30.6001 * (month + 1))
                + day + B - 1524.5;
    }

    // ==================== MATH HELPERS ====================

    private static double dsin(double degrees) {
        return Math.sin(degrees * DEG_TO_RAD);
    }

    private static double dcos(double degrees) {
        return Math.cos(degrees * DEG_TO_RAD);
    }

    private static double dtan(double degrees) {
        return Math.tan(degrees * DEG_TO_RAD);
    }

    private static double darcsin(double x) {
        return Math.asin(x) * RAD_TO_DEG;
    }

    private static double darccos(double x) {
        return Math.acos(x) * RAD_TO_DEG;
    }

    private static double darctan(double x) {
        return Math.atan(x) * RAD_TO_DEG;
    }

    private static double darctan2(double y, double x) {
        return Math.atan2(y, x) * RAD_TO_DEG;
    }

    /**
     * Normalize hours to 0-24 range
     */
    private static double normalizeHour(double hour) {
        hour = hour % 24.0;
        return hour < 0 ? hour + 24.0 : hour;
    }

    /**
     * Normalize degrees to 0-360 range
     */
    private static double normalize360(double degrees) {
        degrees = degrees % 360.0;
        return degrees < 0 ? degrees + 360.0 : degrees;
    }

    // ==================== QIBLA DIRECTION ====================

    /**
     * Calculate Qibla direction from the location
     * Returns bearing in degrees from North (0-360)
     */
    public double getQiblaDirection() {
        // Ka'aba coordinates
        final double kaabaLat = 21.4225;
        final double kaabaLon = 39.8262;

        double lat1 = coords.latitude * DEG_TO_RAD;
        double lat2 = kaabaLat * DEG_TO_RAD;
        double dLon = (kaabaLon - coords.longitude) * DEG_TO_RAD;

        double y = Math.sin(dLon) * Math.cos(lat2);
        double x = Math.cos(lat1) * Math.sin(lat2)
                - Math.sin(lat1) * Math.cos(lat2) * Math.cos(dLon);

        double bearing = Math.atan2(y, x) * RAD_TO_DEG;
        return normalize360(bearing);
    }

    /**
     * Calculate distance to Ka'aba in kilometers
     */
    public double getDistanceToKaaba() {
        final double kaabaLat = 21.4225;
        final double kaabaLon = 39.8262;
        final double earthRadius = 6371.0; // km

        double lat1 = coords.latitude * DEG_TO_RAD;
        double lat2 = kaabaLat * DEG_TO_RAD;
        double dLat = (kaabaLat - coords.latitude) * DEG_TO_RAD;
        double dLon = (kaabaLon - coords.longitude) * DEG_TO_RAD;

        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(lat1) * Math.cos(lat2)
                * Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return earthRadius * c;
    }

    // ==================== GETTERS ====================

    public Coordinates getCoordinates() { return coords; }
    public CalculationMethod getMethod() { return method; }
    public AsrMethod getAsrMethod() { return asrMethod; }
    public HighLatMethod getHighLatMethod() { return highLatMethod; }

    // ==================== MAIN ====================

    public static void main(String[] args) {
        System.out.println("╔═══════════════════════════════════════════════════════════════╗");
        System.out.println("║        SALAH TIMES CALCULATOR - CORRECTED VERSION            ║");
        System.out.println("╚═══════════════════════════════════════════════════════════════╝\n");

        LocalDate testDate = LocalDate.of(2025, 12, 8);

        // ===== TASHKENT =====
        System.out.println("=== TASHKENT, UZBEKISTAN ===");
        System.out.println("Method: Russia (Fajr 16°, Isha 15°), Asr: Hanafi\n");

        Coordinates tashkent = new Coordinates(41.37, 69.26, 460, ZoneId.of("Asia/Tashkent"));
        SalahTimesCalculator calcTashkent = new SalahTimesCalculator(
                tashkent, CalculationMethod.RUSSIA, AsrMethod.HANAFI
        );

        PrayerTimes timesTashkent = calcTashkent.calculate(testDate);
        System.out.println(timesTashkent);
        System.out.printf("Qibla Direction: %.2f°%n", calcTashkent.getQiblaDirection());
        System.out.printf("Distance to Ka'aba: %.0f km%n%n", calcTashkent.getDistanceToKaaba());

        // ===== Monthly Calendar =====
        System.out.println("=== TASHKENT - DECEMBER 2025 ===");
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("HH:mm");
        System.out.println("Date       | Imsak | Fajr  | Sunrise | Dhuhr | Asr   | Maghrib | Isha");
        System.out.println("-".repeat(80));

        for (PrayerTimes pt : calcTashkent.calculateMonth(2025, 12)) {
            System.out.printf("%s | %s | %s |  %s  | %s | %s |  %s  | %s%n",
                    pt.date,
                    pt.imsak.format(fmt),
                    pt.fajr.format(fmt),
                    pt.sunrise.format(fmt),
                    pt.dhuhr.format(fmt),
                    pt.asr.format(fmt),
                    pt.maghrib.format(fmt),
                    pt.isha.format(fmt)
            );
        }

        // ===== Verification Note =====
        System.out.println("\n=== VERIFICATION ===");
        System.out.println("Compare results with:");
        System.out.println("- https://www.islamicfinder.org/prayer-times/");
        System.out.println("- https://praytimes.org/");
        System.out.println("- Local mosque timings");
        System.out.println("\nNote: Minor variations (1-2 minutes) are normal due to:");
        System.out.println("- Different calculation parameters");
        System.out.println("- Rounding methods");
        System.out.println("- Local adjustments applied by some sources");
    }
}