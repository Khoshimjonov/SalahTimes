package uz.khoshimjonov.service;
import java.time.LocalDate;

/**
 * Hijri (Islamic) Calendar Converter - Corrected Version
 *
 * Uses the Umm al-Qura / Kuwaiti algorithm with proper epoch handling.
 * The Islamic calendar began on July 16, 622 CE (Julian) / July 19, 622 CE (Gregorian).
 */
public class HijriDate {

    private final int year;
    private final int month;
    private final int day;
    private final String monthName;
    private final String monthNameArabic;

    private static final String[] HIJRI_MONTHS = {
            "Muharram", "Safar", "Rabi' al-Awwal", "Rabi' al-Thani",
            "Jumada al-Awwal", "Jumada al-Thani", "Rajab", "Sha'ban",
            "Ramadan", "Shawwal", "Dhu al-Qi'dah", "Dhu al-Hijjah"
    };

    private static final String[] HIJRI_MONTHS_ARABIC = {
            "مُحَرَّم", "صَفَر", "رَبِيع الأَوَّل", "رَبِيع الثَّانِي",
            "جُمَادَى الأُولَى", "جُمَادَى الآخِرَة", "رَجَب", "شَعْبَان",
            "رَمَضَان", "شَوَّال", "ذُو القَعْدَة", "ذُو الحِجَّة"
    };

    private static final String[] WEEKDAY_NAMES = {
            "Sunday", "Monday", "Tuesday", "Wednesday",
            "Thursday", "Friday", "Saturday"
    };

    private static final String[] WEEKDAY_NAMES_ARABIC = {
            "الأحد", "الإثنين", "الثلاثاء", "الأربعاء",
            "الخميس", "الجمعة", "السبت"
    };

    // Hijri epoch: July 19, 622 CE (Gregorian) = JD 1948439.5
    private static final double HIJRI_EPOCH = 1948439.5;

    // ==================== CONSTRUCTORS ====================

    public HijriDate(int year, int month, int day) {
        if (month < 1 || month > 12) {
            throw new IllegalArgumentException("Month must be between 1 and 12");
        }
        if (day < 1 || day > 30) {
            throw new IllegalArgumentException("Day must be between 1 and 30");
        }
        this.year = year;
        this.month = month;
        this.day = day;
        this.monthName = HIJRI_MONTHS[month - 1];
        this.monthNameArabic = HIJRI_MONTHS_ARABIC[month - 1];
    }

    // ==================== STATIC FACTORY METHODS ====================

    /**
     * Convert a Gregorian date to Hijri date
     */
    public static HijriDate fromGregorian(LocalDate gregorian) {
        return fromGregorian(gregorian.getYear(), gregorian.getMonthValue(), gregorian.getDayOfMonth());
    }

    /**
     * Convert Gregorian date components to Hijri date
     */
    public static HijriDate fromGregorian(int year, int month, int day) {
        double jd = gregorianToJulian(year, month, day);
        return julianToHijri(jd);
    }

    /**
     * Get today's Hijri date
     */
    public static HijriDate today() {
        return fromGregorian(LocalDate.now());
    }

    // ==================== CONVERSION TO GREGORIAN ====================

    /**
     * Convert this Hijri date to Gregorian
     */
    public LocalDate toGregorian() {
        double jd = hijriToJulian(year, month, day);
        return julianToGregorian(jd);
    }

    // ==================== CORE ALGORITHMS ====================

    /**
     * Convert Gregorian date to Julian Day Number
     */
    private static double gregorianToJulian(int year, int month, int day) {
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

    /**
     * Convert Julian Day Number to Gregorian date
     */
    private static LocalDate julianToGregorian(double jd) {
        int Z = (int) Math.floor(jd + 0.5);
        double F = jd + 0.5 - Z;

        int A;
        if (Z < 2299161) {
            A = Z;
        } else {
            int alpha = (int) Math.floor((Z - 1867216.25) / 36524.25);
            A = Z + 1 + alpha - alpha / 4;
        }

        int B = A + 1524;
        int C = (int) Math.floor((B - 122.1) / 365.25);
        int D = (int) Math.floor(365.25 * C);
        int E = (int) Math.floor((B - D) / 30.6001);

        int day = B - D - (int) Math.floor(30.6001 * E);
        int month = (E < 14) ? E - 1 : E - 13;
        int year = (month > 2) ? C - 4716 : C - 4715;

        return LocalDate.of(year, month, day);
    }

    /**
     * Convert Julian Day Number to Hijri date
     * Using corrected arithmetic algorithm
     */
    private static HijriDate julianToHijri(double jd) {
        jd = Math.floor(jd) + 0.5;

        // Days since Hijri epoch
        double days = jd - HIJRI_EPOCH;

        // Calculate Hijri year
        // Average lunar year = 354.36667 days
        int cycles = (int) Math.floor(days / 10631.0); // 30-year cycles
        double remainingDays = days - cycles * 10631.0;

        int yearInCycle = 0;
        double dayCount = 0;

        for (int y = 1; y <= 30; y++) {
            int daysInYear = isLeapYearInCycle(y) ? 355 : 354;
            if (dayCount + daysInYear > remainingDays) {
                yearInCycle = y;
                remainingDays -= dayCount;
                break;
            }
            dayCount += daysInYear;
            if (y == 30) {
                yearInCycle = 30;
                remainingDays -= dayCount;
            }
        }

        int year = cycles * 30 + yearInCycle;

        // Calculate month and day
        int month = 1;
        int dayOfYear = (int) Math.floor(remainingDays) + 1;

        for (int m = 1; m <= 12; m++) {
            int daysInMonth = getDaysInMonthInternal(year, m);
            if (dayOfYear <= daysInMonth) {
                month = m;
                break;
            }
            dayOfYear -= daysInMonth;
            month = m + 1;
        }

        int day = dayOfYear;

        // Bounds checking
        if (month > 12) {
            month = 12;
            day = getDaysInMonthInternal(year, 12);
        }
        if (day < 1) day = 1;
        if (day > 30) day = 30;

        return new HijriDate(year, month, day);
    }

    /**
     * Convert Hijri date to Julian Day Number
     */
    private static double hijriToJulian(int year, int month, int day) {
        // Calculate days from epoch
        int cycles = (year - 1) / 30;
        int remainingYears = (year - 1) % 30;

        // Days in complete 30-year cycles
        double days = cycles * 10631.0;

        // Days in remaining complete years
        for (int y = 1; y <= remainingYears; y++) {
            days += isLeapYearInCycle(y) ? 355 : 354;
        }

        // Days in complete months of current year
        for (int m = 1; m < month; m++) {
            days += getDaysInMonthInternal(year, m);
        }

        // Add days of current month
        days += day;

        return HIJRI_EPOCH + days - 1;
    }

    /**
     * Check if a year position in the 30-year cycle is a leap year
     * Leap years: 2, 5, 7, 10, 13, 16, 18, 21, 24, 26, 29
     */
    private static boolean isLeapYearInCycle(int yearInCycle) {
        int[] leapYears = {2, 5, 7, 10, 13, 16, 18, 21, 24, 26, 29};
        for (int ly : leapYears) {
            if (yearInCycle == ly) return true;
        }
        return false;
    }

    // ==================== UTILITY METHODS ====================

    /**
     * Check if the Hijri year is a leap year
     */
    public static boolean isLeapYear(int hijriYear) {
        int positionInCycle = ((hijriYear - 1) % 30) + 1;
        return isLeapYearInCycle(positionInCycle);
    }

    /**
     * Internal method to get days in month
     */
    private static int getDaysInMonthInternal(int year, int month) {
        // Odd months (1,3,5,7,9,11) have 30 days
        // Even months (2,4,6,8,10,12) have 29 days
        // Exception: month 12 has 30 days in leap years
        if (month % 2 == 1) {
            return 30;
        } else if (month == 12 && isLeapYear(year)) {
            return 30;
        } else {
            return 29;
        }
    }

    /**
     * Get the number of days in a Hijri month
     */
    public static int getDaysInMonth(int year, int month) {
        if (month < 1 || month > 12) {
            throw new IllegalArgumentException("Month must be between 1 and 12");
        }
        return getDaysInMonthInternal(year, month);
    }

    /**
     * Get the number of days in the Hijri year
     */
    public static int getDaysInYear(int hijriYear) {
        return isLeapYear(hijriYear) ? 355 : 354;
    }

    /**
     * Add days to this Hijri date
     */
    public HijriDate plusDays(int days) {
        double jd = hijriToJulian(year, month, day);
        return julianToHijri(jd + days);
    }

    /**
     * Subtract days from this Hijri date
     */
    public HijriDate minusDays(int days) {
        return plusDays(-days);
    }

    /**
     * Add months to this Hijri date
     */
    public HijriDate plusMonths(int months) {
        int totalMonths = (this.year - 1) * 12 + (this.month - 1) + months;
        int newYear = totalMonths / 12 + 1;
        int newMonth = totalMonths % 12 + 1;

        if (newMonth < 1) {
            newMonth += 12;
            newYear -= 1;
        }

        int maxDay = getDaysInMonth(newYear, newMonth);
        int newDay = Math.min(this.day, maxDay);

        return new HijriDate(newYear, newMonth, newDay);
    }

    /**
     * Add years to this Hijri date
     */
    public HijriDate plusYears(int years) {
        int newYear = this.year + years;
        int maxDay = getDaysInMonth(newYear, this.month);
        int newDay = Math.min(this.day, maxDay);
        return new HijriDate(newYear, this.month, newDay);
    }

    /**
     * Get the day of week (0 = Sunday, 6 = Saturday)
     */
    public int getDayOfWeek() {
        double jd = hijriToJulian(year, month, day);
        return (int) ((Math.floor(jd + 1.5)) % 7);
    }

    /**
     * Get the day of week name
     */
    public String getDayOfWeekName() {
        return WEEKDAY_NAMES[getDayOfWeek()];
    }

    /**
     * Get the day of week name in Arabic
     */
    public String getDayOfWeekArabic() {
        return WEEKDAY_NAMES_ARABIC[getDayOfWeek()];
    }

    // ==================== GETTERS ====================

    public int getYear() { return year; }
    public int getMonth() { return month; }
    public int getDay() { return day; }
    public String getMonthName() { return monthName; }
    public String getMonthNameArabic() { return monthNameArabic; }

    public static String getMonthName(int month) {
        if (month < 1 || month > 12) {
            throw new IllegalArgumentException("Month must be between 1 and 12");
        }
        return HIJRI_MONTHS[month - 1];
    }

    public static String getMonthNameArabic(int month) {
        if (month < 1 || month > 12) {
            throw new IllegalArgumentException("Month must be between 1 and 12");
        }
        return HIJRI_MONTHS_ARABIC[month - 1];
    }

    // ==================== FORMATTING ====================

    @Override
    public String toString() {
        return String.format("%d %s %d AH", day, monthName, year);
    }

    public String toArabicString() {
        return String.format("%d %s %d هـ", day, monthNameArabic, year);
    }

    public String toNumericString() {
        return String.format("%02d/%02d/%04d", day, month, year);
    }

    public String toISOString() {
        return String.format("%04d-%02d-%02d", year, month, day);
    }

    /**
     * Format with full details
     */
    public String toFullString() {
        LocalDate gregorian = toGregorian();
        return String.format("%s, %d %s %d AH (%s)",
                getDayOfWeekName(),
                day,
                monthName,
                year,
                gregorian.toString()
        );
    }

    // ==================== COMPARISON ====================

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        HijriDate other = (HijriDate) obj;
        return year == other.year && month == other.month && day == other.day;
    }

    @Override
    public int hashCode() {
        return 31 * (31 * year + month) + day;
    }

    public boolean isBefore(HijriDate other) {
        if (this.year != other.year) return this.year < other.year;
        if (this.month != other.month) return this.month < other.month;
        return this.day < other.day;
    }

    public boolean isAfter(HijriDate other) {
        return other.isBefore(this);
    }

    // ==================== SPECIAL DATES ====================

    /**
     * Check if this date is a special Islamic day
     */
    public String getSpecialDay() {
        if (month == 1 && day == 1) return "Islamic New Year";
        if (month == 1 && day == 10) return "Day of Ashura";
        if (month == 3 && day == 12) return "Mawlid al-Nabi (Birth of the Prophet ﷺ)";
        if (month == 7 && day == 27) return "Isra and Mi'raj";
        if (month == 8 && day == 15) return "Laylat al-Bara'at";
        if (month == 9 && day == 1) return "First day of Ramadan";
        if (month == 9 && day == 27) return "Laylat al-Qadr (probable)";
        if (month == 10 && day == 1) return "Eid al-Fitr";
        if (month == 12 && day == 8) return "Day of Tarwiyah";
        if (month == 12 && day == 9) return "Day of Arafah";
        if (month == 12 && day == 10) return "Eid al-Adha";
        if (month == 12 && day >= 11 && day <= 13) return "Days of Tashreeq";
        return null;
    }

    /**
     * Check if this date falls within Ramadan
     */
    public boolean isRamadan() {
        return month == 9;
    }

    /**
     * Check if this date is in the first 10 days of Dhu al-Hijjah
     */
    public boolean isFirst10DhulHijjah() {
        return month == 12 && day <= 10;
    }

    /**
     * Check if fasting is prohibited on this day
     */
    public boolean isFastingProhibited() {
        if (month == 10 && day == 1) return true;
        if (month == 12 && day >= 10 && day <= 13) return true;
        return false;
    }

    // ==================== MAIN (DEMO) ====================

    public static void main(String[] args) {
        System.out.println("╔═══════════════════════════════════════════════════════════════╗");
        System.out.println("║             HIJRI DATE CONVERTER - CORRECTED                 ║");
        System.out.println("╚═══════════════════════════════════════════════════════════════╝\n");

        // Test with known reference dates
        System.out.println("=== VERIFICATION WITH KNOWN DATES ===");

        // Reference: January 1, 2000 = 24 Ramadan 1420
        LocalDate ref1 = LocalDate.of(2000, 1, 1);
        HijriDate h1 = HijriDate.fromGregorian(ref1);
        System.out.println(ref1 + " → " + h1 + " (expected: ~24 Ramadan 1420)");

        // Reference: March 1, 2025 ≈ Ramadan 1, 1446
        LocalDate ref2 = LocalDate.of(2025, 3, 1);
        HijriDate h2 = HijriDate.fromGregorian(ref2);
        System.out.println(ref2 + " → " + h2 + " (expected: ~1 Ramadan 1446)");

        // Reference: June 6, 2025 ≈ Eid al-Adha 1446
        LocalDate ref3 = LocalDate.of(2025, 6, 6);
        HijriDate h3 = HijriDate.fromGregorian(ref3);
        System.out.println(ref3 + " → " + h3 + " (expected: ~9-10 Dhu al-Hijjah 1446)");

        System.out.println();

        // Today's date
        System.out.println("=== TODAY ===");
        LocalDate today = LocalDate.of(2025, 12, 8);
        HijriDate hijriToday = HijriDate.fromGregorian(today);
        System.out.println("Gregorian: " + today);
        System.out.println("Hijri:     " + hijriToday);
        System.out.println("Arabic:    " + hijriToday.toArabicString());
        System.out.println("Full:      " + hijriToday.toFullString());

        String special = hijriToday.getSpecialDay();
        if (special != null) {
            System.out.println("Special:   " + special);
        }
        System.out.println();

        // Round-trip test
        System.out.println("=== ROUND-TRIP CONVERSION TEST ===");
        LocalDate original = LocalDate.of(2025, 12, 8);
        HijriDate hijri = HijriDate.fromGregorian(original);
        LocalDate backToGregorian = hijri.toGregorian();
        System.out.println("Original:    " + original);
        System.out.println("To Hijri:    " + hijri);
        System.out.println("Back:        " + backToGregorian);
        System.out.println("Match:       " + original.equals(backToGregorian));
        System.out.println();

        // Important dates 1446 AH
        System.out.println("=== IMPORTANT DATES 1446 AH ===");
        Object[][] importantDates = {
                {1, 1, "Islamic New Year"},
                {1, 10, "Ashura"},
                {3, 12, "Mawlid al-Nabi"},
                {7, 27, "Isra and Mi'raj"},
                {9, 1, "Ramadan begins"},
                {10, 1, "Eid al-Fitr"},
                {12, 9, "Day of Arafah"},
                {12, 10, "Eid al-Adha"}
        };

        for (Object[] date : importantDates) {
            int m = (int) date[0];
            int d = (int) date[1];
            String name = (String) date[2];
            HijriDate h = new HijriDate(1446, m, d);
            System.out.printf("%s %d, 1446 AH = %s (%s)%n",
                    h.getMonthName(), h.getDay(),
                    h.toGregorian(),
                    name
            );
        }
        System.out.println();

        // Important dates 1447 AH
        System.out.println("=== IMPORTANT DATES 1447 AH ===");
        for (Object[] date : importantDates) {
            int m = (int) date[0];
            int d = (int) date[1];
            String name = (String) date[2];
            HijriDate h = new HijriDate(1447, m, d);
            System.out.printf("%s %d, 1447 AH = %s (%s)%n",
                    h.getMonthName(), h.getDay(),
                    h.toGregorian(),
                    name
            );
        }
        System.out.println();

        // Leap years
        System.out.println("=== LEAP YEARS ===");
        System.out.print("Leap years 1440-1450: ");
        for (int y = 1440; y <= 1450; y++) {
            if (HijriDate.isLeapYear(y)) {
                System.out.print(y + " ");
            }
        }
        System.out.println();
        System.out.println();

        // Date arithmetic
        System.out.println("=== DATE ARITHMETIC ===");
        HijriDate base = hijriToday;
        System.out.println("Today:        " + base + " = " + base.toGregorian());
        System.out.println("+30 days:     " + base.plusDays(30) + " = " + base.plusDays(30).toGregorian());
        System.out.println("+1 month:     " + base.plusMonths(1) + " = " + base.plusMonths(1).toGregorian());
        System.out.println("+1 year:      " + base.plusYears(1) + " = " + base.plusYears(1).toGregorian());
    }
}