package no.dv8.utils;

import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

public class Maps {
    public static Map<String, Object> mapOf(Object... pairs) {
        Map<String, Object> m = new TreeMap<>();
        for (int i = 0; i < pairs.length; i += 2) {
            m.put(pairs[i].toString(), pairs[i + 1]);
        }
        return m;
    }
}
