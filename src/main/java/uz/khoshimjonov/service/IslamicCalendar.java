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
        private final String nameEn;
        private final String nameRu;
        private final String nameUz;
        private final String nameArabic;
        private final String descriptionEn;
        private final String descriptionRu;
        private final String descriptionUz;
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

        public IslamicEvent(String nameEn, String nameRu, String nameUz, String nameArabic,
                            String descriptionEn, String descriptionRu, String descriptionUz,
                            HijriDate hijriDate, EventType type,
                            boolean isFastingDay, boolean isFastingProhibited,
                            boolean isPublicHoliday) {
            this.nameEn = nameEn;
            this.nameRu = nameRu;
            this.nameUz = nameUz;
            this.nameArabic = nameArabic;
            this.descriptionEn = descriptionEn;
            this.descriptionRu = descriptionRu;
            this.descriptionUz = descriptionUz;
            this.hijriDate = hijriDate;
            this.gregorianDate = hijriDate.toGregorian();
            this.type = type;
            this.isFastingDay = isFastingDay;
            this.isFastingProhibited = isFastingProhibited;
            this.isPublicHoliday = isPublicHoliday;
        }

        /**
         * Get event name in specified language
         * @param lang Language code: "en", "ru", "uz", or "ar"
         * @return Event name in specified language, defaults to English if not found
         */
        public String getName(String lang) {
            if (lang == null) {
                return nameEn;
            }
            return switch (lang.toLowerCase()) {
                case "ru" -> nameRu;
                case "uz" -> nameUz;
                case "ar", "arabic" -> nameArabic;
                default -> nameEn;
            };
        }

        /**
         * Get event description in specified language
         * @param lang Language code: "en", "ru", or "uz"
         * @return Event description in specified language, defaults to English if not found
         */
        public String getDescription(String lang) {
            if (lang == null) {
                return descriptionEn;
            }
            return switch (lang.toLowerCase()) {
                case "ru" -> descriptionRu;
                case "uz" -> descriptionUz;
                default -> descriptionEn;
            };
        }

        // Getters
        public String getName() { return nameEn; }
        public String getNameEn() { return nameEn; }
        public String getNameRu() { return nameRu; }
        public String getNameUz() { return nameUz; }
        public String getNameArabic() { return nameArabic; }
        public String getDescription() { return descriptionEn; }
        public String getDescriptionEn() { return descriptionEn; }
        public String getDescriptionRu() { return descriptionRu; }
        public String getDescriptionUz() { return descriptionUz; }
        public HijriDate getHijriDate() { return hijriDate; }
        public LocalDate getGregorianDate() { return gregorianDate; }
        public EventType getType() { return type; }
        public boolean isFastingDay() { return isFastingDay; }
        public boolean isFastingProhibited() { return isFastingProhibited; }
        public boolean isPublicHoliday() { return isPublicHoliday; }

        @Override
        public String toString() {
            return String.format("%s - %s (%s)",
                    gregorianDate, nameEn, hijriDate);
        }

        public String toDetailedString() {
            return toDetailedString("en");
        }

        public String toDetailedString(String lang) {
            StringBuilder sb = new StringBuilder();
            sb.append(String.format("%-25s %s%n", "Event:", getName(lang)));
            sb.append(String.format("%-25s %s%n", "Arabic:", nameArabic));
            sb.append(String.format("%-25s %s%n", "Gregorian Date:", gregorianDate));
            sb.append(String.format("%-25s %s%n", "Hijri Date:", hijriDate));
            sb.append(String.format("%-25s %s%n", "Type:", type));
            String desc = getDescription(lang);
            if (desc != null && !desc.isEmpty()) {
                sb.append(String.format("%-25s %s%n", "Description:", desc));
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

        HijriDate startOfYear = HijriDate.fromGregorian(LocalDate.of(gregorianYear, 1, 1));
        HijriDate endOfYear = HijriDate.fromGregorian(LocalDate.of(gregorianYear, 12, 31));

        int startHijriYear = startOfYear.getYear();
        int endHijriYear = endOfYear.getYear();

        for (int hijriYear = startHijriYear; hijriYear <= endHijriYear; hijriYear++) {
            List<IslamicEvent> yearEvents = getEventsForHijriYear(hijriYear);

            for (IslamicEvent event : yearEvents) {
                if (event.getGregorianDate().getYear() == gregorianYear) {
                    events.add(event);
                }
            }
        }

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
                "Исламский Новый год",
                "Islomiy Yangi yil",
                "رأس السنة الهجرية",
                "First day of the Islamic calendar year",
                "Первый день исламского календарного года",
                "Islomiy taqvim yilining birinchi kuni",
                new HijriDate(hijriYear, 1, 1),
                IslamicEvent.EventType.MONTH_START,
                false, false, true
        ));

        events.add(new IslamicEvent(
                "Day of Ashura",
                "День Ашура",
                "Ashuro kuni",
                "يوم عاشوراء",
                "10th of Muharram",
                "10-е Мухаррама",
                "Muharramning 10-kuni",
                new HijriDate(hijriYear, 1, 10),
                IslamicEvent.EventType.BLESSED_DAY,
                true, false, false
        ));

        // ===== SAFAR (Month 2) =====
        events.add(new IslamicEvent(
                "Start of Safar",
                "Начало месяца Сафар",
                "Safar oyining boshlanishi",
                "بداية شهر صفر",
                "Beginning of the month of Safar",
                "Начало месяца Сафар",
                "Safar oyining boshlanishi",
                new HijriDate(hijriYear, 2, 1),
                IslamicEvent.EventType.MONTH_START,
                false, false, false
        ));

        // ===== RABI' AL-AWWAL (Month 3) =====
        events.add(new IslamicEvent(
                "Mawlid al-Nabi",
                "Мавлид ан-Наби",
                "Mavlud an-Nabiy",
                "المولد النبوي الشريف",
                "Birth of Prophet Muhammad ﷺ (12th Rabi' al-Awwal according to majority)",
                "Рождение Пророка Мухаммада ﷺ (12-е Раби аль-Авваль по мнению большинства)",
                "Payg'ambarimiz Muhammad ﷺ ning tug'ilgan kuni (ko'pchilik bo'yicha Rabiul-avvalning 12-kuni)",
                new HijriDate(hijriYear, 3, 12),
                IslamicEvent.EventType.BLESSED_DAY,
                false, false, true
        ));

        // ===== RAJAB (Month 7) =====
        events.add(new IslamicEvent(
                "Start of Rajab",
                "Начало месяца Раджаб",
                "Rajab oyining boshlanishi",
                "بداية شهر رجب",
                "Beginning of Rajab, one of the four sacred months",
                "Начало Раджаба, одного из четырёх священных месяцев",
                "Rajab oyining boshlanishi, to'rtta muqaddas oylardan biri",
                new HijriDate(hijriYear, 7, 1),
                IslamicEvent.EventType.MONTH_START,
                false, false, false
        ));

        events.add(new IslamicEvent(
                "Isra and Mi'raj",
                "Исра и Мирадж",
                "Isro va Me'roj",
                "الإسراء والمعراج",
                "Night Journey and Ascension of Prophet Muhammad ﷺ",
                "Ночное путешествие и Вознесение Пророка Мухаммада ﷺ",
                "Payg'ambarimiz Muhammad ﷺ ning tungi sayohati va Me'rojga ko'tarilishi",
                new HijriDate(hijriYear, 7, 27),
                IslamicEvent.EventType.HOLY_NIGHT,
                false, false, false
        ));

        // ===== SHA'BAN (Month 8) =====
        events.add(new IslamicEvent(
                "Start of Sha'ban",
                "Начало месяца Шаабан",
                "Sha'bon oyining boshlanishi",
                "بداية شهر شعبان",
                "Beginning of Sha'ban",
                "Начало месяца Шаабан",
                "Sha'bon oyining boshlanishi",
                new HijriDate(hijriYear, 8, 1),
                IslamicEvent.EventType.MONTH_START,
                false, false, false
        ));

        events.add(new IslamicEvent(
                "Laylat al-Bara'at",
                "Ночь Бараат",
                "Baro'at kechasi",
                "ليلة البراءة",
                "Laylat al-Bara'at",
                "Ночь Бараат",
                "Baro'at kechasi",
                new HijriDate(hijriYear, 8, 15),
                IslamicEvent.EventType.HOLY_NIGHT,
                true, false, false
        ));

        // ===== RAMADAN (Month 9) =====
        events.add(new IslamicEvent(
                "First day of Ramadan",
                "Первый день Рамадана",
                "Ramazonning birinchi kuni",
                "أول يوم رمضان",
                "Beginning of the month of fasting",
                "Начало месяца поста",
                "Ro'za oyi boshlanishi",
                new HijriDate(hijriYear, 9, 1),
                IslamicEvent.EventType.MONTH_START,
                true, false, true
        ));

        events.add(new IslamicEvent(
                "Laylat al-Qadr (21st night)",
                "Ляйлят аль-Кадр (21-я ночь)",
                "Qadr kechasi (21-kecha)",
                "ليلة القدر",
                "Night of Power - possible date",
                "Ночь Предопределения - возможная дата",
                "Qadr kechasi - ehtimoliy sana",
                new HijriDate(hijriYear, 9, 21),
                IslamicEvent.EventType.HOLY_NIGHT,
                true, false, false
        ));

        events.add(new IslamicEvent(
                "Laylat al-Qadr (23rd night)",
                "Ляйлят аль-Кадр (23-я ночь)",
                "Qadr kechasi (23-kecha)",
                "ليلة القدر",
                "Night of Power - possible date",
                "Ночь Предопределения - возможная дата",
                "Qadr kechasi - ehtimoliy sana",
                new HijriDate(hijriYear, 9, 23),
                IslamicEvent.EventType.HOLY_NIGHT,
                true, false, false
        ));

        events.add(new IslamicEvent(
                "Laylat al-Qadr (25th night)",
                "Ляйлят аль-Кадр (25-я ночь)",
                "Qadr kechasi (25-kecha)",
                "ليلة القدر",
                "Night of Power - possible date",
                "Ночь Предопределения - возможная дата",
                "Qadr kechasi - ehtimoliy sana",
                new HijriDate(hijriYear, 9, 25),
                IslamicEvent.EventType.HOLY_NIGHT,
                true, false, false
        ));

        events.add(new IslamicEvent(
                "Laylat al-Qadr (27th night)",
                "Ляйлят аль-Кадр (27-я ночь)",
                "Qadr kechasi (27-kecha)",
                "ليلة القدر",
                "Night of Power - most commonly observed date",
                "Ночь Предопределения - наиболее распространённая дата",
                "Qadr kechasi - eng ko'p nishonlanadigan sana",
                new HijriDate(hijriYear, 9, 27),
                IslamicEvent.EventType.HOLY_NIGHT,
                true, false, false
        ));

        events.add(new IslamicEvent(
                "Laylat al-Qadr (29th night)",
                "Ляйлят аль-Кадр (29-я ночь)",
                "Qadr kechasi (29-kecha)",
                "ليلة القدر",
                "Night of Power - possible date",
                "Ночь Предопределения - возможная дата",
                "Qadr kechasi - ehtimoliy sana",
                new HijriDate(hijriYear, 9, 29),
                IslamicEvent.EventType.HOLY_NIGHT,
                true, false, false
        ));

        // ===== SHAWWAL (Month 10) =====
        events.add(new IslamicEvent(
                "Eid al-Fitr",
                "Ид аль-Фитр (Ураза-байрам)",
                "Ramazon hayiti",
                "عيد الفطر",
                "Eid al-Fitr",
                "Ид аль-Фитр",
                "Ramazon hayiti",
                new HijriDate(hijriYear, 10, 1),
                IslamicEvent.EventType.EID,
                false, true, true
        ));

        events.add(new IslamicEvent(
                "Eid al-Fitr (Day 2)",
                "Ид аль-Фитр (День 2)",
                "Ramazon hayiti (2-kun)",
                "عيد الفطر - اليوم الثاني",
                "Second day of Eid al-Fitr",
                "Второй день Ид аль-Фитр",
                "Ramazon hayitining ikkinchi kuni",
                new HijriDate(hijriYear, 10, 2),
                IslamicEvent.EventType.EID,
                false, false, true
        ));

        events.add(new IslamicEvent(
                "Eid al-Fitr (Day 3)",
                "Ид аль-Фитр (День 3)",
                "Ramazon hayiti (3-kun)",
                "عيد الفطر - اليوم الثالث",
                "Third day of Eid al-Fitr",
                "Третий день Ид аль-Фитр",
                "Ramazon hayitining uchinchi kuni",
                new HijriDate(hijriYear, 10, 3),
                IslamicEvent.EventType.EID,
                false, false, true
        ));

        events.add(new IslamicEvent(
                "Six Days of Shawwal Begin",
                "Начало шести дней Шавваля",
                "Shavvolning olti kunlik ro'zasi boshlanishi",
                "صيام ستة أيام من شوال",
                "Recommended to fast 6 days in Shawwal after Eid",
                "Рекомендуется поститься 6 дней в Шаввале после Ида",
                "Hayitdan keyin Shavvolda 6 kun ro'za tutish tavsiya etiladi",
                new HijriDate(hijriYear, 10, 2),
                IslamicEvent.EventType.FASTING_DAY,
                true, false, false
        ));

        // ===== DHU AL-QI'DAH (Month 11) =====
        events.add(new IslamicEvent(
                "Start of Dhu al-Qi'dah",
                "Начало месяца Зуль-Каада",
                "Zulqa'da oyining boshlanishi",
                "بداية شهر ذو القعدة",
                "Beginning of Dhu al-Qi'dah, one of the sacred months",
                "Начало Зуль-Каада, одного из священных месяцев",
                "Zulqa'da oyining boshlanishi, muqaddas oylardan biri",
                new HijriDate(hijriYear, 11, 1),
                IslamicEvent.EventType.MONTH_START,
                false, false, false
        ));

        // ===== DHU AL-HIJJAH (Month 12) =====
        events.add(new IslamicEvent(
                "Start of Dhu al-Hijjah",
                "Начало месяца Зуль-Хиджа",
                "Zulhijja oyining boshlanishi",
                "بداية شهر ذو الحجة",
                "Beginning of the month of Hajj, one of the sacred months",
                "Начало месяца Хаджа, одного из священных месяцев",
                "Haj oyi boshlanishi, muqaddas oylardan biri",
                new HijriDate(hijriYear, 12, 1),
                IslamicEvent.EventType.MONTH_START,
                true, false, false
        ));

        events.add(new IslamicEvent(
                "First 10 Days of Dhu al-Hijjah",
                "Первые 10 дней Зуль-Хиджа",
                "Zulhijjaning dastlabki 10 kuni",
                "العشر الأوائل من ذي الحجة",
                "First 10 Days of Dhu al-Hijjah",
                "Первые 10 дней Зуль-Хиджа",
                "Zulhijjaning dastlabki 10 kuni",
                new HijriDate(hijriYear, 12, 1),
                IslamicEvent.EventType.BLESSED_DAY,
                true, false, false
        ));

        events.add(new IslamicEvent(
                "Day of Arafah",
                "День Арафат",
                "Arafa kuni",
                "يوم عرفة",
                "9th of Dhu al-Hijjah - most important day of Hajj",
                "9-е Зуль-Хиджа - самый важный день Хаджа",
                "Zulhijjaning 9-kuni - Hajning eng muhim kuni",
                new HijriDate(hijriYear, 12, 9),
                IslamicEvent.EventType.BLESSED_DAY,
                true, false, false
        ));

        events.add(new IslamicEvent(
                "Eid al-Adha",
                "Ид аль-Адха (Курбан-байрам)",
                "Qurbon hayiti",
                "عيد الأضحى",
                "Festival of Sacrifice",
                "Праздник жертвоприношения",
                "Qurbonlik bayrami",
                new HijriDate(hijriYear, 12, 10),
                IslamicEvent.EventType.EID,
                false, true, true
        ));

        events.add(new IslamicEvent(
                "Days of Tashreeq (Day 1)",
                "Дни Ташрик (День 1)",
                "Tashriq kunlari (1-kun)",
                "أيام التشريق - اليوم الأول",
                "11th of Dhu al-Hijjah - fasting prohibited",
                "11-е Зуль-Хиджа - пост запрещён",
                "Zulhijjaning 11-kuni - ro'za tutish taqiqlangan",
                new HijriDate(hijriYear, 12, 11),
                IslamicEvent.EventType.EID,
                false, true, true
        ));

        events.add(new IslamicEvent(
                "Days of Tashreeq (Day 2)",
                "Дни Ташрик (День 2)",
                "Tashriq kunlari (2-kun)",
                "أيام التشريق - اليوم الثاني",
                "12th of Dhu al-Hijjah - fasting prohibited",
                "12-е Зуль-Хиджа - пост запрещён",
                "Zulhijjaning 12-kuni - ro'za tutish taqiqlangan",
                new HijriDate(hijriYear, 12, 12),
                IslamicEvent.EventType.EID,
                false, true, true
        ));

        events.add(new IslamicEvent(
                "Days of Tashreeq (Day 3)",
                "Дни Ташрик (День 3)",
                "Tashriq kunlari (3-kun)",
                "أيام التشريق - اليوم الثالث",
                "13th of Dhu al-Hijjah - fasting prohibited",
                "13-е Зуль-Хиджа - пост запрещён",
                "Zulhijjaning 13-kuni - ro'za tutish taqiqlangan",
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
                    event.getNameEn().contains("Ashura") ||
                    event.getNameEn().contains("Mawlid") ||
                    event.getNameEn().contains("Isra") ||
                    event.getNameEn().contains("Arafah") ||
                    (event.getNameEn().contains("Laylat al-Qadr") && event.getNameEn().contains("27th"))) {
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
            if (event.getNameEn().equals("First day of Ramadan")) {
                dates.put("ramadanStart", event.getGregorianDate());
                dates.put("ramadanEnd", event.getGregorianDate().plusDays(29));
            }
            if (event.getNameEn().equals("Eid al-Fitr")) {
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
            if (event.getNameEn().equals("Day of Tarwiyah")) {
                dates.put("dayOfTarwiyah", event.getGregorianDate());
            }
            if (event.getNameEn().equals("Day of Arafah")) {
                dates.put("dayOfArafah", event.getGregorianDate());
            }
            if (event.getNameEn().equals("Eid al-Adha")) {
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

        List<IslamicEvent> events = new ArrayList<>();
        events.addAll(getEventsForYear(fromDate.getYear()));
        events.addAll(getEventsForYear(fromDate.getYear() + 1));

        for (IslamicEvent event : events) {
            if (!event.getGregorianDate().isBefore(fromDate)) {
                upcoming.add(event);
            }
        }

        upcoming.sort(Comparator.comparing(IslamicEvent::getGregorianDate));

        if (upcoming.size() > count) {
            return upcoming.subList(0, count);
        }

        return upcoming;
    }

    /**
     * Format events as a simple calendar string
     */
    public static String formatEventsAsCalendar(List<IslamicEvent> events) {
        return formatEventsAsCalendar(events, "en");
    }

    /**
     * Format events as a simple calendar string in specified language
     */
    public static String formatEventsAsCalendar(List<IslamicEvent> events, String lang) {
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
                    event.getName(lang),
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

        // Demo with different languages
        System.out.println("=== MAJOR ISLAMIC EVENTS " + year + " (English) ===\n");
        List<IslamicEvent> majorEvents = getMajorEventsForYear(year);

        System.out.printf("%-12s | %-35s | %-25s | %s%n", "Date", "Event", "Hijri Date", "Type");
        System.out.println("-".repeat(100));

        for (IslamicEvent event : majorEvents) {
            System.out.printf("%-12s | %-35s | %-25s | %s%n",
                    event.getGregorianDate(),
                    event.getName("en"),
                    event.getHijriDate(),
                    event.getType()
            );
        }
        System.out.println();

        System.out.println("=== MAJOR ISLAMIC EVENTS " + year + " (Russian) ===\n");
        System.out.printf("%-12s | %-40s | %-25s%n", "Дата", "Событие", "Хиджра");
        System.out.println("-".repeat(85));

        for (IslamicEvent event : majorEvents) {
            System.out.printf("%-12s | %-40s | %-25s%n",
                    event.getGregorianDate(),
                    event.getName("ru"),
                    event.getHijriDate()
            );
        }
        System.out.println();

        System.out.println("=== MAJOR ISLAMIC EVENTS " + year + " (Uzbek) ===\n");
        System.out.printf("%-12s | %-40s | %-25s%n", "Sana", "Voqea", "Hijriy");
        System.out.println("-".repeat(85));

        for (IslamicEvent event : majorEvents) {
            System.out.printf("%-12s | %-40s | %-25s%n",
                    event.getGregorianDate(),
                    event.getName("uz"),
                    event.getHijriDate()
            );
        }
        System.out.println();

        // All events calendar in different languages
        System.out.println("=== ALL EVENTS CALENDAR (English) ===\n");
        List<IslamicEvent> allEvents = getEventsForYear(year);
        System.out.println(formatEventsAsCalendar(allEvents, "en"));

        System.out.println("=== ALL EVENTS CALENDAR (Russian) ===\n");
        System.out.println(formatEventsAsCalendar(allEvents, "ru"));

        System.out.println("=== ALL EVENTS CALENDAR (Uzbek) ===\n");
        System.out.println(formatEventsAsCalendar(allEvents, "uz"));

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
        LocalDate today = LocalDate.of(2025, 12, 9);
        List<IslamicEvent> upcoming = getUpcomingEvents(today, 10);

        System.out.println("English | Russian | Uzbek");
        System.out.println("-".repeat(100));
        for (IslamicEvent event : upcoming) {
            long daysUntil = java.time.temporal.ChronoUnit.DAYS.between(today, event.getGregorianDate());
            System.out.printf("%s - %s | %s | %s (in %d days)%n",
                    event.getGregorianDate(),
                    event.getName("en"),
                    event.getName("ru"),
                    event.getName("uz"),
                    daysUntil
            );
        }
        System.out.println();

        // Detailed view of Eid al-Adha in different languages
        System.out.println("=== DETAILED VIEW: EID AL-ADHA (English) ===\n");
        for (IslamicEvent event : allEvents) {
            if (event.getNameEn().equals("Eid al-Adha")) {
                System.out.println(event.toDetailedString("en"));
                break;
            }
        }

        System.out.println("=== DETAILED VIEW: EID AL-ADHA (Russian) ===\n");
        for (IslamicEvent event : allEvents) {
            if (event.getNameEn().equals("Eid al-Adha")) {
                System.out.println(event.toDetailedString("ru"));
                break;
            }
        }

        System.out.println("=== DETAILED VIEW: EID AL-ADHA (Uzbek) ===\n");
        for (IslamicEvent event : allEvents) {
            if (event.getNameEn().equals("Eid al-Adha")) {
                System.out.println(event.toDetailedString("uz"));
                break;
            }
        }

        // Events for a specific date
        System.out.println("=== EVENTS ON SPECIFIC DATE ===");
        LocalDate checkDate = LocalDate.of(2025, 3, 30);
        List<IslamicEvent> dateEvents = getEventsForDate(checkDate);
        System.out.println("Events on " + checkDate + ":");
        for (IslamicEvent event : dateEvents) {
            System.out.println("  - EN: " + event.getName("en"));
            System.out.println("    RU: " + event.getName("ru"));
            System.out.println("    UZ: " + event.getName("uz"));
        }
        System.out.println();

        // 2026 Preview
        System.out.println("=== MAJOR EVENTS 2026 (Preview - All Languages) ===\n");
        List<IslamicEvent> events2026 = getMajorEventsForYear(2026);
        for (IslamicEvent event : events2026) {
            System.out.printf("%s:%n  EN: %s%n  RU: %s%n  UZ: %s%n%n",
                    event.getGregorianDate(),
                    event.getName("en"),
                    event.getName("ru"),
                    event.getName("uz")
            );
        }
    }
}