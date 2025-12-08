package uz.khoshimjonov.service;

import java.time.LocalDate;
import java.util.*;

/**
 * Islamic Special Days Calendar
 * Returns all significant Islamic dates for a given Gregorian year
 */
public class IslamicCalendar {

    /**
     * Represents a special Islamic day
     */
    public static class IslamicEvent {
        private final String name;
        private final String nameArabic;
        private final String description;
        private final HijriDate hijriDate;
        private final LocalDate gregorianDate;
        private final EventType type;
        private final boolean isFastingDay;
        private final boolean isFastingProhibited;
        private final boolean isPublicHoliday;

        public enum EventType {
            EID,
            HOLY_NIGHT,
            BLESSED_DAY,
            FASTING_DAY,
            MONTH_START,
            HISTORICAL
        }

        public IslamicEvent(String name, String nameArabic, String description,
                            HijriDate hijriDate, EventType type,
                            boolean isFastingDay, boolean isFastingProhibited,
                            boolean isPublicHoliday) {
            this.name = name;
            this.nameArabic = nameArabic;
            this.description = description;
            this.hijriDate = hijriDate;
            this.gregorianDate = hijriDate.toGregorian();
            this.type = type;
            this.isFastingDay = isFastingDay;
            this.isFastingProhibited = isFastingProhibited;
            this.isPublicHoliday = isPublicHoliday;
        }

        // Getters
        public String getName() { return name; }
        public String getNameArabic() { return nameArabic; }
        public String getDescription() { return description; }
        public HijriDate getHijriDate() { return hijriDate; }
        public LocalDate getGregorianDate() { return gregorianDate; }
        public EventType getType() { return type; }
        public boolean isFastingDay() { return isFastingDay; }
        public boolean isFastingProhibited() { return isFastingProhibited; }
        public boolean isPublicHoliday() { return isPublicHoliday; }

        @Override
        public String toString() {
            return String.format("%s - %s (%s)",
                    gregorianDate, name, hijriDate);
        }

        public String toDetailedString() {
            StringBuilder sb = new StringBuilder();
            sb.append(String.format("%-25s %s%n", "Event:", name));
            sb.append(String.format("%-25s %s%n", "Arabic:", nameArabic));
            sb.append(String.format("%-25s %s%n", "Gregorian Date:", gregorianDate));
            sb.append(String.format("%-25s %s%n", "Hijri Date:", hijriDate));
            sb.append(String.format("%-25s %s%n", "Type:", type));
            if (description != null && !description.isEmpty()) {
                sb.append(String.format("%-25s %s%n", "Description:", description));
            }
            if (isFastingDay) {
                sb.append(String.format("%-25s %s%n", "Fasting:", "Recommended"));
            }
            if (isFastingProhibited) {
                sb.append(String.format("%-25s %s%n", "Fasting:", "Prohibited"));
            }
            if (isPublicHoliday) {
                sb.append(String.format("%-25s %s%n", "Public Holiday:", "Yes (in most Muslim countries)"));
            }
            return sb.toString();
        }
    }

    /**
     * Get all special Islamic days that fall within a Gregorian year
     */
    public static List<IslamicEvent> getEventsForYear(int gregorianYear) {
        List<IslamicEvent> events = new ArrayList<>();

        // Find which Hijri years overlap with this Gregorian year
        HijriDate startOfYear = HijriDate.fromGregorian(LocalDate.of(gregorianYear, 1, 1));
        HijriDate endOfYear = HijriDate.fromGregorian(LocalDate.of(gregorianYear, 12, 31));

        int startHijriYear = startOfYear.getYear();
        int endHijriYear = endOfYear.getYear();

        // Get events for each Hijri year that overlaps
        for (int hijriYear = startHijriYear; hijriYear <= endHijriYear; hijriYear++) {
            List<IslamicEvent> yearEvents = getEventsForHijriYear(hijriYear);

            for (IslamicEvent event : yearEvents) {
                // Only include events that fall within the Gregorian year
                if (event.getGregorianDate().getYear() == gregorianYear) {
                    events.add(event);
                }
            }
        }

        // Sort by Gregorian date
        events.sort(Comparator.comparing(IslamicEvent::getGregorianDate));

        return events;
    }

    /**
     * Get all special Islamic days for a Hijri year
     */
    public static List<IslamicEvent> getEventsForHijriYear(int hijriYear) {
        List<IslamicEvent> events = new ArrayList<>();

        // ===== MUHARRAM (Month 1) =====
        events.add(new IslamicEvent(
                "Islamic New Year",
                "رأس السنة الهجرية",
                "First day of the Islamic calendar year",
                new HijriDate(hijriYear, 1, 1),
                IslamicEvent.EventType.MONTH_START,
                false, false, true
        ));

        events.add(new IslamicEvent(
                "Tasu'a",
                "تاسوعاء",
                "9th of Muharram, recommended to fast with Ashura",
                new HijriDate(hijriYear, 1, 9),
                IslamicEvent.EventType.FASTING_DAY,
                true, false, false
        ));

        events.add(new IslamicEvent(
                "Day of Ashura",
                "يوم عاشوراء",
                "10th of Muharram, commemorates Moses and the Exodus; martyrdom of Husayn ibn Ali",
                new HijriDate(hijriYear, 1, 10),
                IslamicEvent.EventType.BLESSED_DAY,
                true, false, false
        ));

        // ===== SAFAR (Month 2) =====
        events.add(new IslamicEvent(
                "Start of Safar",
                "بداية شهر صفر",
                "Beginning of the month of Safar",
                new HijriDate(hijriYear, 2, 1),
                IslamicEvent.EventType.MONTH_START,
                false, false, false
        ));

        // ===== RABI' AL-AWWAL (Month 3) =====
        events.add(new IslamicEvent(
                "Mawlid al-Nabi",
                "المولد النبوي الشريف",
                "Birth of Prophet Muhammad ﷺ (12th Rabi' al-Awwal according to majority)",
                new HijriDate(hijriYear, 3, 12),
                IslamicEvent.EventType.BLESSED_DAY,
                false, false, true
        ));

        // ===== RAJAB (Month 7) =====
        events.add(new IslamicEvent(
                "Start of Rajab",
                "بداية شهر رجب",
                "Beginning of Rajab, one of the four sacred months",
                new HijriDate(hijriYear, 7, 1),
                IslamicEvent.EventType.MONTH_START,
                false, false, false
        ));

        events.add(new IslamicEvent(
                "Isra and Mi'raj",
                "الإسراء والمعراج",
                "Night Journey and Ascension of Prophet Muhammad ﷺ",
                new HijriDate(hijriYear, 7, 27),
                IslamicEvent.EventType.HOLY_NIGHT,
                false, false, false
        ));

        // ===== SHA'BAN (Month 8) =====
        events.add(new IslamicEvent(
                "Start of Sha'ban",
                "بداية شهر شعبان",
                "Beginning of Sha'ban",
                new HijriDate(hijriYear, 8, 1),
                IslamicEvent.EventType.MONTH_START,
                false, false, false
        ));

        events.add(new IslamicEvent(
                "Laylat al-Bara'at",
                "ليلة البراءة",
                "Night of Forgiveness (15th Sha'ban)",
                new HijriDate(hijriYear, 8, 15),
                IslamicEvent.EventType.HOLY_NIGHT,
                true, false, false
        ));

        // ===== RAMADAN (Month 9) =====
        events.add(new IslamicEvent(
                "First day of Ramadan",
                "أول يوم رمضان",
                "Beginning of the month of fasting",
                new HijriDate(hijriYear, 9, 1),
                IslamicEvent.EventType.MONTH_START,
                true, false, true
        ));

        // Laylat al-Qadr - traditionally on odd nights of last 10 days
        events.add(new IslamicEvent(
                "Laylat al-Qadr (21st night)",
                "ليلة القدر",
                "Night of Power - possible date",
                new HijriDate(hijriYear, 9, 21),
                IslamicEvent.EventType.HOLY_NIGHT,
                true, false, false
        ));

        events.add(new IslamicEvent(
                "Laylat al-Qadr (23rd night)",
                "ليلة القدر",
                "Night of Power - possible date",
                new HijriDate(hijriYear, 9, 23),
                IslamicEvent.EventType.HOLY_NIGHT,
                true, false, false
        ));

        events.add(new IslamicEvent(
                "Laylat al-Qadr (25th night)",
                "ليلة القدر",
                "Night of Power - possible date",
                new HijriDate(hijriYear, 9, 25),
                IslamicEvent.EventType.HOLY_NIGHT,
                true, false, false
        ));

        events.add(new IslamicEvent(
                "Laylat al-Qadr (27th night)",
                "ليلة القدر",
                "Night of Power - most commonly observed date",
                new HijriDate(hijriYear, 9, 27),
                IslamicEvent.EventType.HOLY_NIGHT,
                true, false, false
        ));

        events.add(new IslamicEvent(
                "Laylat al-Qadr (29th night)",
                "ليلة القدر",
                "Night of Power - possible date",
                new HijriDate(hijriYear, 9, 29),
                IslamicEvent.EventType.HOLY_NIGHT,
                true, false, false
        ));

        // ===== SHAWWAL (Month 10) =====
        events.add(new IslamicEvent(
                "Eid al-Fitr",
                "عيد الفطر",
                "Festival of Breaking the Fast",
                new HijriDate(hijriYear, 10, 1),
                IslamicEvent.EventType.EID,
                false, true, true
        ));

        events.add(new IslamicEvent(
                "Eid al-Fitr (Day 2)",
                "عيد الفطر - اليوم الثاني",
                "Second day of Eid al-Fitr",
                new HijriDate(hijriYear, 10, 2),
                IslamicEvent.EventType.EID,
                false, false, true
        ));

        events.add(new IslamicEvent(
                "Eid al-Fitr (Day 3)",
                "عيد الفطر - اليوم الثالث",
                "Third day of Eid al-Fitr",
                new HijriDate(hijriYear, 10, 3),
                IslamicEvent.EventType.EID,
                false, false, true
        ));

        // Six days of Shawwal fasting
        events.add(new IslamicEvent(
                "Six Days of Shawwal Begin",
                "صيام ستة أيام من شوال",
                "Recommended to fast 6 days in Shawwal after Eid",
                new HijriDate(hijriYear, 10, 2),
                IslamicEvent.EventType.FASTING_DAY,
                true, false, false
        ));

        // ===== DHU AL-QI'DAH (Month 11) =====
        events.add(new IslamicEvent(
                "Start of Dhu al-Qi'dah",
                "بداية شهر ذو القعدة",
                "Beginning of Dhu al-Qi'dah, one of the sacred months",
                new HijriDate(hijriYear, 11, 1),
                IslamicEvent.EventType.MONTH_START,
                false, false, false
        ));

        // ===== DHU AL-HIJJAH (Month 12) =====
        events.add(new IslamicEvent(
                "Start of Dhu al-Hijjah",
                "بداية شهر ذو الحجة",
                "Beginning of the month of Hajj, one of the sacred months",
                new HijriDate(hijriYear, 12, 1),
                IslamicEvent.EventType.MONTH_START,
                true, false, false
        ));

        // First 10 days - blessed days
        events.add(new IslamicEvent(
                "First 10 Days of Dhu al-Hijjah",
                "العشر الأوائل من ذي الحجة",
                "Most blessed days of the year - recommended to fast (except Eid)",
                new HijriDate(hijriYear, 12, 1),
                IslamicEvent.EventType.BLESSED_DAY,
                true, false, false
        ));

        events.add(new IslamicEvent(
                "Day of Tarwiyah",
                "يوم التروية",
                "8th of Dhu al-Hijjah - pilgrims go to Mina",
                new HijriDate(hijriYear, 12, 8),
                IslamicEvent.EventType.BLESSED_DAY,
                true, false, false
        ));

        events.add(new IslamicEvent(
                "Day of Arafah",
                "يوم عرفة",
                "9th of Dhu al-Hijjah - most important day of Hajj",
                new HijriDate(hijriYear, 12, 9),
                IslamicEvent.EventType.BLESSED_DAY,
                true, false, false
        ));

        events.add(new IslamicEvent(
                "Eid al-Adha",
                "عيد الأضحى",
                "Festival of Sacrifice",
                new HijriDate(hijriYear, 12, 10),
                IslamicEvent.EventType.EID,
                false, true, true
        ));

        events.add(new IslamicEvent(
                "Days of Tashreeq (Day 1)",
                "أيام التشريق - اليوم الأول",
                "11th of Dhu al-Hijjah - fasting prohibited",
                new HijriDate(hijriYear, 12, 11),
                IslamicEvent.EventType.EID,
                false, true, true
        ));

        events.add(new IslamicEvent(
                "Days of Tashreeq (Day 2)",
                "أيام التشريق - اليوم الثاني",
                "12th of Dhu al-Hijjah - fasting prohibited",
                new HijriDate(hijriYear, 12, 12),
                IslamicEvent.EventType.EID,
                false, true, true
        ));

        events.add(new IslamicEvent(
                "Days of Tashreeq (Day 3)",
                "أيام التشريق - اليوم الثالث",
                "13th of Dhu al-Hijjah - fasting prohibited",
                new HijriDate(hijriYear, 12, 13),
                IslamicEvent.EventType.EID,
                false, true, true
        ));

        return events;
    }

    /**
     * Get only major events (Eids, major holy nights, etc.)
     */
    public static List<IslamicEvent> getMajorEventsForYear(int gregorianYear) {
        List<IslamicEvent> allEvents = getEventsForYear(gregorianYear);
        List<IslamicEvent> majorEvents = new ArrayList<>();

        for (IslamicEvent event : allEvents) {
            if (event.isPublicHoliday() ||
                    event.getName().contains("Ashura") ||
                    event.getName().contains("Mawlid") ||
                    event.getName().contains("Isra") ||
                    event.getName().contains("Arafah") ||
                    (event.getName().contains("Laylat al-Qadr") && event.getName().contains("27th"))) {
                majorEvents.add(event);
            }
        }

        return majorEvents;
    }

    /**
     * Get events for a specific month
     */
    public static List<IslamicEvent> getEventsForMonth(int gregorianYear, int gregorianMonth) {
        List<IslamicEvent> allEvents = getEventsForYear(gregorianYear);
        List<IslamicEvent> monthEvents = new ArrayList<>();

        for (IslamicEvent event : allEvents) {
            if (event.getGregorianDate().getMonthValue() == gregorianMonth) {
                monthEvents.add(event);
            }
        }

        return monthEvents;
    }

    /**
     * Get Ramadan dates for a Gregorian year
     */
    public static Map<String, LocalDate> getRamadanDates(int gregorianYear) {
        Map<String, LocalDate> dates = new LinkedHashMap<>();

        List<IslamicEvent> events = getEventsForYear(gregorianYear);

        for (IslamicEvent event : events) {
            if (event.getName().equals("First day of Ramadan")) {
                dates.put("ramadanStart", event.getGregorianDate());
                dates.put("ramadanEnd", event.getGregorianDate().plusDays(29)); // Could be 29 or 30 days
            }
            if (event.getName().equals("Eid al-Fitr")) {
                dates.put("eidAlFitr", event.getGregorianDate());
            }
        }

        return dates;
    }

    /**
     * Get Hajj dates for a Gregorian year
     */
    public static Map<String, LocalDate> getHajjDates(int gregorianYear) {
        Map<String, LocalDate> dates = new LinkedHashMap<>();

        List<IslamicEvent> events = getEventsForYear(gregorianYear);

        for (IslamicEvent event : events) {
            if (event.getName().equals("Day of Tarwiyah")) {
                dates.put("dayOfTarwiyah", event.getGregorianDate());
            }
            if (event.getName().equals("Day of Arafah")) {
                dates.put("dayOfArafah", event.getGregorianDate());
            }
            if (event.getName().equals("Eid al-Adha")) {
                dates.put("eidAlAdha", event.getGregorianDate());
            }
        }

        return dates;
    }

    /**
     * Check if a specific date has any Islamic events
     */
    public static List<IslamicEvent> getEventsForDate(LocalDate date) {
        List<IslamicEvent> events = getEventsForYear(date.getYear());
        List<IslamicEvent> dateEvents = new ArrayList<>();

        for (IslamicEvent event : events) {
            if (event.getGregorianDate().equals(date)) {
                dateEvents.add(event);
            }
        }

        return dateEvents;
    }

    /**
     * Get upcoming events from today
     */
    public static List<IslamicEvent> getUpcomingEvents(int count) {
        return getUpcomingEvents(LocalDate.now(), count);
    }

    /**
     * Get upcoming events from a specific date
     */
    public static List<IslamicEvent> getUpcomingEvents(LocalDate fromDate, int count) {
        List<IslamicEvent> upcoming = new ArrayList<>();

        // Get events for current and next year
        List<IslamicEvent> events = new ArrayList<>();
        events.addAll(getEventsForYear(fromDate.getYear()));
        events.addAll(getEventsForYear(fromDate.getYear() + 1));

        // Filter and sort
        for (IslamicEvent event : events) {
            if (!event.getGregorianDate().isBefore(fromDate)) {
                upcoming.add(event);
            }
        }

        upcoming.sort(Comparator.comparing(IslamicEvent::getGregorianDate));

        // Return only requested count
        if (upcoming.size() > count) {
            return upcoming.subList(0, count);
        }

        return upcoming;
    }

    /**
     * Format events as a simple calendar string
     */
    public static String formatEventsAsCalendar(List<IslamicEvent> events) {
        StringBuilder sb = new StringBuilder();

        String currentMonth = "";

        for (IslamicEvent event : events) {
            String month = event.getGregorianDate().getMonth().toString();

            if (!month.equals(currentMonth)) {
                if (!currentMonth.isEmpty()) {
                    sb.append("\n");
                }
                sb.append("═══ ").append(month).append(" ═══\n");
                currentMonth = month;
            }

            sb.append(String.format("  %2d - %-30s (%s)\n",
                    event.getGregorianDate().getDayOfMonth(),
                    event.getName(),
                    event.getHijriDate().toNumericString()
            ));
        }

        return sb.toString();
    }

    // ==================== MAIN (DEMO) ====================

    public static void main(String[] args) {
        System.out.println("╔═══════════════════════════════════════════════════════════════╗");
        System.out.println("║              ISLAMIC CALENDAR - SPECIAL DAYS                 ║");
        System.out.println("╚═══════════════════════════════════════════════════════════════╝\n");

        int year = 2026;

        // Major events for 2025
        System.out.println("=== MAJOR ISLAMIC EVENTS " + year + " ===\n");
        List<IslamicEvent> majorEvents = getMajorEventsForYear(year);

        System.out.println(String.format("%-12s | %-30s | %-25s | %s",
                "Date", "Event", "Hijri Date", "Type"));
        System.out.println("-".repeat(95));

        for (IslamicEvent event : majorEvents) {
            System.out.println(String.format("%-12s | %-30s | %-25s | %s",
                    event.getGregorianDate(),
                    event.getName(),
                    event.getHijriDate(),
                    event.getType()
            ));
        }
        System.out.println();

        // All events for 2025
        System.out.println("=== ALL ISLAMIC EVENTS " + year + " ===\n");
        List<IslamicEvent> allEvents = getEventsForYear(year);
        System.out.println(formatEventsAsCalendar(allEvents));

        // Ramadan dates
        System.out.println("=== RAMADAN " + year + " ===");
        Map<String, LocalDate> ramadan = getRamadanDates(year);
        for (Map.Entry<String, LocalDate> entry : ramadan.entrySet()) {
            System.out.println(entry.getKey() + ": " + entry.getValue());
        }
        System.out.println();

        // Hajj dates
        System.out.println("=== HAJJ " + year + " ===");
        Map<String, LocalDate> hajj = getHajjDates(year);
        for (Map.Entry<String, LocalDate> entry : hajj.entrySet()) {
            System.out.println(entry.getKey() + ": " + entry.getValue());
        }
        System.out.println();

        // Upcoming events
        System.out.println("=== NEXT 10 UPCOMING EVENTS ===\n");
        LocalDate today = LocalDate.of(2025, 12, 8);
        List<IslamicEvent> upcoming = getUpcomingEvents(today, 10);

        for (IslamicEvent event : upcoming) {
            long daysUntil = java.time.temporal.ChronoUnit.DAYS.between(today, event.getGregorianDate());
            System.out.println(String.format("%s - %s (in %d days)",
                    event.getGregorianDate(), event.getName(), daysUntil));
        }
        System.out.println();

        // Detailed view of Eid al-Adha
        System.out.println("=== DETAILED VIEW: EID AL-ADHA ===\n");
        for (IslamicEvent event : allEvents) {
            if (event.getName().equals("Eid al-Adha")) {
                System.out.println(event.toDetailedString());
                break;
            }
        }

        // Events for a specific date
        System.out.println("=== EVENTS ON SPECIFIC DATE ===");
        LocalDate checkDate = LocalDate.of(2025, 3, 30);
        List<IslamicEvent> dateEvents = getEventsForDate(checkDate);
        System.out.println("Events on " + checkDate + ":");
        for (IslamicEvent event : dateEvents) {
            System.out.println("  - " + event.getName());
        }
        System.out.println();

        // 2026 Preview
        System.out.println("=== MAJOR EVENTS 2026 (Preview) ===\n");
        List<IslamicEvent> events2026 = getMajorEventsForYear(2026);
        for (IslamicEvent event : events2026) {
            System.out.println(String.format("%s - %s",
                    event.getGregorianDate(), event.getName()));
        }
    }
}