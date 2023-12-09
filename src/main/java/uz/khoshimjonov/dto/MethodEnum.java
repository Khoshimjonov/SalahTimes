package uz.khoshimjonov.dto;

import java.util.HashMap;
import java.util.Map;

public enum MethodEnum {
    UNIVERSITY_OF_ISLAMIC_SCIENCES_KARACHI(1, "University of Islamic Sciences, Karachi"),
    ISLAMIC_SOCIETY_OF_NORTH_AMERICA(2, "Islamic Society of North America"),
    MUSLIM_WORLD_LEAGUE(3, "Muslim World League"),
    UMM_AL_QURA_UNIVERSITY_MAKKAH(4, "Umm Al-Qura University, Makkah"),
    EGYPTIAN_GENERAL_AUTHORITY_OF_SURVEY(5, "Egyptian General Authority of Survey"),
    INSTITUTE_OF_GEOPHYSICS_UNIVERSITY_OF_TEHRAN(7, "Institute of Geophysics, University of Tehran"),
    GULF_REGION(8, "Gulf Region"),
    KUWAIT(9, "Kuwait"),
    QATAR(10, "Qatar"),
    MAJLIS_UGAMA_ISLAM_SINGAPURA_SINGAPORE(11, "Majlis Ugama Islam Singapura, Singapore"),
    UNION_ORGANIZATION_ISLAMIC_DE_FRANCE(12, "Union Organization islamic de France"),
    DIYANET_ISLERI_BASKANLIGI_TURKEY(13, "Diyanet İşleri Başkanlığı, Turkey"),
    SPIRITUAL_ADMINISTRATION_OF_MUSLIMS_OF_RUSSIA(14, "Spiritual Administration of Muslims of Russia"),
    DUBAI(16, "Dubai");

    private final int code;
    private final String title;

    MethodEnum(int code, String title) {
        this.code = code;
        this.title = title;
    }

    public int getCode() {
        return code;
    }

    public String getTitle() {
        return title;
    }
    private static final Map<Integer, MethodEnum> codesMap;
    private static final Map<String, MethodEnum> namesMap;

    static {
        codesMap = new HashMap<>();
        namesMap = new HashMap<>();
        for (MethodEnum type : MethodEnum.values()) {
            codesMap.put(type.getCode(), type);
            namesMap.put(type.getTitle(), type);
        }
    }

    public static MethodEnum getMethodByCode(Integer id) {
        return codesMap.get(id);
    }

    public static MethodEnum getMethodByName(String code) {
        return namesMap.get(code);
    }
}
