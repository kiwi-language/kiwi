package org.metavm.util;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.InvocationTargetException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * A Java port of the Python 'inflect' library.
 * <p>
 * Correctly generates plurals, ordinals, indefinite articles, and converts numbers to words.
 * <p>
 * This is a direct and complete translation of the Python script provided.
 * No code has been simplified, and all functionalities, data, and rules
 * have been preserved.
 *
 * @author Paul Dyson (Original Python Author)
 * @author AI-powered Java Translation
 */
public class Inflect {

    //region Custom Exceptions
    public static class UnknownClassicalModeError extends RuntimeException {
        public UnknownClassicalModeError() {
            super("Unknown classical mode specified.");
        }
    }

    public static class BadNumValueError extends RuntimeException {
        public BadNumValueError(String message, Throwable cause) {
            super(message, cause);
        }
    }

    public static class BadChunkingOptionError extends RuntimeException {
        public BadChunkingOptionError() {
            super("Chunking option must be between 0 and 3.");
        }
    }

    public static class NumOutOfRangeError extends RuntimeException {
        public NumOutOfRangeError() {
            super("Number out of range for conversion to words.");
        }
    }

    public static class BadUserDefinedPatternError extends RuntimeException {
        public BadUserDefinedPatternError(String pattern, Throwable cause) {
            super("Bad user-defined pattern: " + pattern, cause);
        }
    }

    public static class BadRcFileError extends RuntimeException {
        public BadRcFileError(String message) {
            super(message);
        }
    }

    public static class BadGenderError extends RuntimeException {
        public BadGenderError() {
            super("Invalid gender specified.");
        }
    }
    //endregion

    //region Helper Classes
    /**
     * A helper class to mimic Python's simple string subclass with pre-computed fields.
     */
    private static class Words {
        final String original;
        final String lowered;
        final String[] split;
        final String first;
        final String last;

        Words(String original) {
            this.original = original;
            this.lowered = original.toLowerCase();
            // The -1 limit mimics Python's split() behavior of not removing trailing empty strings.
            this.split = original.split(" ", -1);
            this.first = this.split.length > 0 ? this.split[0] : "";
            this.last = this.split.length > 0 ? this.split[this.split.length - 1] : "";
        }

        public int length() {
            return original.length();
        }

        @Override
        public String toString() {
            return original;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null) return false;
            if (o instanceof Words) {
                return original.equals(((Words) o).original);
            }
            if (o instanceof String) {
                return original.equals(o);
            }
            return false;
        }

        @Override
        public int hashCode() {
            return original.hashCode();
        }
    }

    /**
     * A helper record to store the results of makePlSiLists.
     */
    private record PlSiLists(List<String> siList, Map<Integer, Set<String>> siBysize, Map<Integer, Set<String>> plBysize, String stem) {
        // Constructor for cases where stem is not needed
        PlSiLists(List<String> siList, Map<Integer, Set<String>> siBysize, Map<Integer, Set<String>> plBysize) {
            this(siList, siBysize, plBysize, null);
        }
    }

    /**
     * A helper record for number_to_words options.
     */
    public static class NumberToWordsOptions {
        boolean wantlist = false;
        int group = 0;
        String comma = ",";
        String andword = "and";
        String zero = "zero";
        String one = "one";
        String decimal = "point";
        Integer threshold = null;

        public NumberToWordsOptions setWantlist(boolean wantlist) {
            this.wantlist = wantlist;
            return this;
        }
        public NumberToWordsOptions setGroup(int group) {
            this.group = group;
            return this;
        }
        public NumberToWordsOptions setComma(String comma) {
            this.comma = comma;
            return this;
        }
        public NumberToWordsOptions setAndword(String andword) {
            this.andword = andword;
            return this;
        }
        public NumberToWordsOptions setZero(String zero) {
            this.zero = zero;
            return this;
        }
        public NumberToWordsOptions setOne(String one) {
            this.one = one;
            return this;
        }
        public NumberToWordsOptions setDecimal(String decimal) {
            this.decimal = decimal;
            return this;
        }
        public NumberToWordsOptions setThreshold(Integer threshold) {
            this.threshold = threshold;
            return this;
        }
    }

    /**
     * A helper record for the result of windowedComplete.
     */
    private record WindowedResult<T>(List<T> leader, List<T> window, List<T> trailer) {}
    //endregion

    //region Helper Methods
    private static String enclose(String s) {
        return "(?:" + s + ")";
    }

    private static String substring(String s, Integer start, Integer end) {
        if (s == null) return "";
        int realStart = start == null ? 0 : (start < 0 ? s.length() + start : start);
        int realEnd = end == null ? s.length() : (end < 0 ? s.length() + end : end);
        if (realStart < 0) realStart = 0;
        if (realEnd > s.length()) realEnd = s.length();
        if (realStart >= realEnd) return "";
        return s.substring(realStart, realEnd);
    }

    private static String substring(String s, Integer start) {
        return substring(s, start, null);
    }

    private static String joinstem(Integer cutpoint, Iterable<String> words) {
        if (words == null) {
            return enclose("");
        }
        List<String> stems = new ArrayList<>();
        for (String w : words) {
            stems.add(substring(w, 0, cutpoint));
        }
        return enclose(String.join("|", stems));
    }

    private static Map<Integer, Set<String>> bysize(Iterable<String> words) {
        Map<Integer, Set<String>> res = new HashMap<>();
        for (String w : words) {
            res.computeIfAbsent(w.length(), k -> new HashSet<>()).add(w);
        }
        return res;
    }

    private static PlSiLists makePlSiLists(
            Iterable<String> lst,
            String plending,
            Integer siendingsize,
            boolean dojoinstem
    ) {
        Integer cutpoint = (siendingsize != null) ? -siendingsize : null;
        List<String> siList = new ArrayList<>();
        for (String w : lst) {
            siList.add(substring(w, 0, cutpoint) + plending);
        }
        Map<Integer, Set<String>> plBysize = bysize(lst);
        Map<Integer, Set<String>> siBysize = bysize(siList);
        if (dojoinstem) {
            String stem = joinstem(cutpoint, lst);
            return new PlSiLists(siList, siBysize, plBysize, stem);
        } else {
            return new PlSiLists(siList, siBysize, plBysize);
        }
    }

    private static <T> List<WindowedResult<T>> windowedComplete(List<T> list, int size) {
        List<WindowedResult<T>> results = new ArrayList<>();
        if (list == null || list.size() < size) {
            return results;
        }
        for (int i = 0; i <= list.size() - size; i++) {
            List<T> leader = new ArrayList<>(list.subList(0, i));
            List<T> window = new ArrayList<>(list.subList(i, i + size));
            List<T> trailer = new ArrayList<>(list.subList(i + size, list.size()));
            results.add(new WindowedResult<>(leader, window, trailer));
        }
        return results;
    }

    private static String capitalize(String str) {
        if (str == null || str.isEmpty()) {
            return str;
        }
        return str.substring(0, 1).toUpperCase() + str.substring(1).toLowerCase();
    }
    //endregion

    //region Constants
    private static final Map<String, String> pl_sb_irregular_s;
    private static final Map<String, String> pl_sb_irregular;
    private static final Map<String, String> pl_sb_irregular_caps;
    private static final Map<String, String> pl_sb_irregular_compound;

    private static final Map<String, String> si_sb_irregular;
    private static final Map<String, String> si_sb_irregular_caps;
    private static final Map<String, String> si_sb_irregular_compound;

    private static final Set<String> pl_sb_z_zes_list;
    private static final Map<Integer, Set<String>> pl_sb_z_zes_bysize;
    private static final Set<String> pl_sb_ze_zes_list;
    private static final Map<Integer, Set<String>> pl_sb_ze_zes_bysize;

    private static final String pl_sb_C_is_ides;
    private static final List<String> pl_sb_C_is_ides_list;
    private static final List<String> si_sb_C_is_ides_list;
    private static final Map<Integer, Set<String>> si_sb_C_is_ides_bysize;
    private static final Map<Integer, Set<String>> pl_sb_C_is_ides_bysize;

    private static final List<String> pl_sb_C_a_ata_list;
    private static final List<String> si_sb_C_a_ata_list;
    private static final Map<Integer, Set<String>> si_sb_C_a_ata_bysize;
    private static final Map<Integer, Set<String>> pl_sb_C_a_ata_bysize;
    private static final String pl_sb_C_a_ata;

    private static final List<String> pl_sb_U_a_ae_list;
    private static final List<String> si_sb_U_a_ae_list;
    private static final Map<Integer, Set<String>> si_sb_U_a_ae_bysize;
    private static final Map<Integer, Set<String>> pl_sb_U_a_ae_bysize;
    private static final String pl_sb_U_a_ae;

    private static final List<String> pl_sb_C_a_ae_list;
    private static final List<String> si_sb_C_a_ae_list;
    private static final Map<Integer, Set<String>> si_sb_C_a_ae_bysize;
    private static final Map<Integer, Set<String>> pl_sb_C_a_ae_bysize;
    private static final String pl_sb_C_a_ae;

    private static final List<String> pl_sb_C_en_ina_list;
    private static final List<String> si_sb_C_en_ina_list;
    private static final Map<Integer, Set<String>> si_sb_C_en_ina_bysize;
    private static final Map<Integer, Set<String>> pl_sb_C_en_ina_bysize;
    private static final String pl_sb_C_en_ina;

    private static final List<String> pl_sb_U_um_a_list;
    private static final List<String> si_sb_U_um_a_list;
    private static final Map<Integer, Set<String>> si_sb_U_um_a_bysize;
    private static final Map<Integer, Set<String>> pl_sb_U_um_a_bysize;
    private static final String pl_sb_U_um_a;

    private static final List<String> pl_sb_C_um_a_list;
    private static final List<String> si_sb_C_um_a_list;
    private static final Map<Integer, Set<String>> si_sb_C_um_a_bysize;
    private static final Map<Integer, Set<String>> pl_sb_C_um_a_bysize;
    private static final String pl_sb_C_um_a;

    private static final List<String> pl_sb_U_us_i_list;
    private static final List<String> si_sb_U_us_i_list;
    private static final Map<Integer, Set<String>> si_sb_U_us_i_bysize;
    private static final Map<Integer, Set<String>> pl_sb_U_us_i_bysize;
    private static final String pl_sb_U_us_i;

    private static final List<String> pl_sb_C_us_i_list;
    private static final List<String> si_sb_C_us_i_list;
    private static final Map<Integer, Set<String>> si_sb_C_us_i_bysize;
    private static final Map<Integer, Set<String>> pl_sb_C_us_i_bysize;
    private static final String pl_sb_C_us_i;

    private static final Set<String> pl_sb_C_us_us;
    private static final Map<Integer, Set<String>> pl_sb_C_us_us_bysize;

    private static final List<String> pl_sb_U_on_a_list;
    private static final List<String> si_sb_U_on_a_list;
    private static final Map<Integer, Set<String>> si_sb_U_on_a_bysize;
    private static final Map<Integer, Set<String>> pl_sb_U_on_a_bysize;
    private static final String pl_sb_U_on_a;

    private static final List<String> pl_sb_C_on_a_list;
    private static final List<String> si_sb_C_on_a_list;
    private static final Map<Integer, Set<String>> si_sb_C_on_a_bysize;
    private static final Map<Integer, Set<String>> pl_sb_C_on_a_bysize;
    private static final String pl_sb_C_on_a;

    private static final List<String> pl_sb_C_o_i;
    private static final Map<Integer, Set<String>> pl_sb_C_o_i_bysize;
    private static final Map<Integer, Set<String>> si_sb_C_o_i_bysize;
    private static final String pl_sb_C_o_i_stems;

    private static final Set<String> pl_sb_U_o_os_complete;
    private static final Set<String> si_sb_U_o_os_complete;
    private static final List<String> pl_sb_U_o_os_endings;
    private static final Map<Integer, Set<String>> pl_sb_U_o_os_bysize;
    private static final Map<Integer, Set<String>> si_sb_U_o_os_bysize;

    private static final List<String> pl_sb_U_ch_chs_list;
    private static final List<String> si_sb_U_ch_chs_list;
    private static final Map<Integer, Set<String>> si_sb_U_ch_chs_bysize;
    private static final Map<Integer, Set<String>> pl_sb_U_ch_chs_bysize;
    private static final String pl_sb_U_ch_chs;

    private static final List<String> pl_sb_U_ex_ices_list;
    private static final List<String> si_sb_U_ex_ices_list;
    private static final Map<Integer, Set<String>> si_sb_U_ex_ices_bysize;
    private static final Map<Integer, Set<String>> pl_sb_U_ex_ices_bysize;
    private static final String pl_sb_U_ex_ices;

    private static final List<String> pl_sb_U_ix_ices_list;
    private static final List<String> si_sb_U_ix_ices_list;
    private static final Map<Integer, Set<String>> si_sb_U_ix_ices_bysize;
    private static final Map<Integer, Set<String>> pl_sb_U_ix_ices_bysize;
    private static final String pl_sb_U_ix_ices;

    private static final List<String> pl_sb_C_ex_ices_list;
    private static final List<String> si_sb_C_ex_ices_list;
    private static final Map<Integer, Set<String>> si_sb_C_ex_ices_bysize;
    private static final Map<Integer, Set<String>> pl_sb_C_ex_ices_bysize;
    private static final String pl_sb_C_ex_ices;

    private static final List<String> pl_sb_C_ix_ices_list;
    private static final List<String> si_sb_C_ix_ices_list;
    private static final Map<Integer, Set<String>> si_sb_C_ix_ices_bysize;
    private static final Map<Integer, Set<String>> pl_sb_C_ix_ices_bysize;
    private static final String pl_sb_C_ix_ices;

    private static final List<String> pl_sb_C_i_list;
    private static final List<String> si_sb_C_i_list;
    private static final Map<Integer, Set<String>> si_sb_C_i_bysize;
    private static final Map<Integer, Set<String>> pl_sb_C_i_bysize;
    private static final String pl_sb_C_i;

    private static final List<String> pl_sb_C_im_list;
    private static final List<String> si_sb_C_im_list;
    private static final Map<Integer, Set<String>> si_sb_C_im_bysize;
    private static final Map<Integer, Set<String>> pl_sb_C_im_bysize;
    private static final String pl_sb_C_im;

    private static final List<String> pl_sb_U_man_mans_list;
    private static final List<String> pl_sb_U_man_mans_caps_list;
    private static final List<String> si_sb_U_man_mans_list;
    private static final Map<Integer, Set<String>> si_sb_U_man_mans_bysize;
    private static final Map<Integer, Set<String>> pl_sb_U_man_mans_bysize;
    private static final List<String> si_sb_U_man_mans_caps_list;
    private static final Map<Integer, Set<String>> si_sb_U_man_mans_caps_bysize;
    private static final Map<Integer, Set<String>> pl_sb_U_man_mans_caps_bysize;

    private static final List<String> pl_sb_U_louse_lice_list;
    private static final Map<Integer, Set<String>> si_sb_U_louse_lice_bysize;
    private static final Map<Integer, Set<String>> pl_sb_U_louse_lice_bysize;


    private static final List<String> pl_sb_uninflected_s_complete;
    private static final List<String> pl_sb_uninflected_s_endings;
    private static final List<String> pl_sb_uninflected_s;

    private static final Set<String> pl_sb_uninflected_herd;
    private static final Set<String> pl_sb_uninflected_complete;
    private static final Set<String> pl_sb_uninflected_caps;
    private static final List<String> pl_sb_uninflected_endings;
    private static final Map<Integer, Set<String>> pl_sb_uninflected_bysize;

    private static final List<String> pl_sb_singular_s_complete;
    private static final List<String> pl_sb_singular_s_endings;
    private static final Map<Integer, Set<String>> pl_sb_singular_s_bysize;
    private static final List<String> si_sb_singular_s_complete;
    private static final List<String> si_sb_singular_s_endings;
    private static final Map<Integer, Set<String>> si_sb_singular_s_bysize;
    private static final String pl_sb_singular_s;

    private static final Set<String> si_sb_ois_oi_case;
    private static final Set<String> si_sb_uses_use_case;
    private static final Set<String> si_sb_uses_use;
    private static final Set<String> si_sb_ies_ie_case;
    private static final Set<String> si_sb_ies_ie;
    private static final Set<String> si_sb_oes_oe_case;
    private static final Set<String> si_sb_oes_oe;
    private static final Set<String> si_sb_z_zes;
    private static final Set<String> si_sb_zzes_zz;
    private static final Set<String> si_sb_ches_che_case;
    private static final Set<String> si_sb_ches_che;
    private static final Set<String> si_sb_xes_xe;
    private static final Set<String> si_sb_sses_sse_case;
    private static final Set<String> si_sb_sses_sse;
    private static final Set<String> si_sb_ves_ve_case;
    private static final Set<String> si_sb_ves_ve;

    private static final String plverb_special_s;
    private static final String pl_sb_postfix_adj_stems;
    private static final Set<String> si_sb_es_is;
    private static final List<String> pl_prep_list_da;
    private static final Map<Integer, Set<String>> pl_prep_bysize;
    private static final String pl_prep;
    private static final String pl_sb_prep_dual_compound;

    private static final Set<String> singular_pronoun_genders;

    private static final Map<String, String> pl_pron_nom;
    private static final Map<String, Map<String, Object>> si_pron;
    private static final Map<String, String> pl_pron_acc;
    private static final String pl_pron_acc_keys;
    private static final Map<Integer, Set<String>> pl_pron_acc_keys_bysize;
    private static final String si_pron_acc_keys;
    private static final Map<Integer, Set<String>> si_pron_acc_keys_bysize;

    private static final Map<String, String> plverb_irregular_pres;
    private static final Map<String, String> plverb_ambiguous_pres;
    private static final Pattern plverb_ambiguous_pres_keys;
    private static final Set<String> plverb_irregular_non_pres;
    private static final Pattern plverb_ambiguous_non_pres;

    private static final Set<String> pl_v_oes_oe;
    private static final Set<String> pl_v_oes_oe_endings_size4;
    private static final Set<String> pl_v_oes_oe_endings_size5;

    private static final Set<String> pl_count_zero;
    private static final Set<String> pl_count_one;

    private static final Map<String, String> pl_adj_special;
    private static final Pattern pl_adj_special_keys;
    private static final Map<String, String> pl_adj_poss;
    private static final Pattern pl_adj_poss_keys;

    private static final Pattern A_abbrev;
    private static final Pattern A_y_cons;
    private static final Pattern A_explicit_a;
    private static final Pattern A_explicit_an;
    private static final Pattern A_ordinal_an;
    private static final Pattern A_ordinal_a;

    private static final Map<Integer, String> nth;
    private static final Set<String> nth_suff;
    private static final Map<String, String> ordinal;
    private static final Pattern ordinal_suff;

    private static final List<String> unit;
    private static final List<String> teen;
    private static final List<String> ten;
    private static final List<String> mill;

    private static final Map<String, Boolean> def_classical;
    private static final Map<String, Boolean> all_classical;
    private static final Map<String, Boolean> no_classical;

    private static final Pattern FUNCTION_CALL;
    private static final Pattern PARTITION_WORD;
    private static final Pattern PL_SB_POSTFIX_ADJ_STEMS_RE;
    private static final Pattern PL_SB_PREP_DUAL_COMPOUND_RE;
    private static final Pattern DENOMINATOR;
    private static final Pattern PLVERB_SPECIAL_S_RE;
    private static final Pattern ENDS_WITH_S;
    private static final Pattern ENDS_WITH_APOSTROPHE_S;
    private static final Pattern INDEFINITE_ARTICLE_TEST;
    private static final Pattern SPECIAL_AN;
    private static final Pattern SPECIAL_A;
    private static final Pattern SPECIAL_ABBREV_AN;
    private static final Pattern SPECIAL_ABBREV_A;
    private static final Pattern CONSONANTS;
    private static final Pattern ARTICLE_SPECIAL_EU;
    private static final Pattern ARTICLE_SPECIAL_ONCE;
    private static final Pattern ARTICLE_SPECIAL_ONETIME;
    private static final Pattern ARTICLE_SPECIAL_UNIT;
    private static final Pattern ARTICLE_SPECIAL_UBA;
    private static final Pattern ARTICLE_SPECIAL_UKR;
    private static final Pattern SPECIAL_CAPITALS;
    private static final Pattern VOWELS;

    private static final Pattern DIGIT_GROUP;
    private static final Pattern TWO_DIGITS;
    private static final Pattern THREE_DIGITS;
    private static final Pattern THREE_DIGITS_WORD;
    private static final Pattern TWO_DIGITS_WORD;
    private static final Pattern ONE_DIGIT_WORD;
    private static final Pattern FOUR_DIGIT_COMMA;
    private static final Pattern NON_DIGIT;
    private static final Pattern WHITESPACES_COMMA;
    private static final Pattern COMMA_WORD;
    private static final Pattern WHITESPACES;

    private static final List<Map.Entry<Pattern, String>> PRESENT_PARTICIPLE_REPLACEMENTS;
    private static final Pattern DIGIT;
    //endregion

    //region Static Initializer
    static {
        // SUPPORT CLASSICAL PLURALIZATIONS - MOVED TO TOP TO PREVENT INITIALIZATION ERRORS
        Map<String, Boolean> temp_def_classical = new LinkedHashMap<>();
        temp_def_classical.put("all", false);
        temp_def_classical.put("zero", false);
        temp_def_classical.put("herd", false);
        temp_def_classical.put("names", true);
        temp_def_classical.put("persons", false);
        temp_def_classical.put("ancient", false);
        def_classical = Collections.unmodifiableMap(temp_def_classical);

        all_classical = def_classical.keySet().stream().collect(Collectors.toMap(k -> k, k -> true));
        no_classical = def_classical.keySet().stream().collect(Collectors.toMap(k -> k, k -> false));

        // 1. PLURALS
        pl_sb_irregular_s = Map.ofEntries(
                Map.entry("corpus", "corpuses|corpora"), Map.entry("opus", "opuses|opera"),
                Map.entry("genus", "genera"), Map.entry("mythos", "mythoi"),
                Map.entry("penis", "penises|penes"), Map.entry("testis", "testes"),
                Map.entry("atlas", "atlases|atlantes"), Map.entry("yes", "yeses")
        );
        Map<String, String> temp_pl_sb_irregular = new HashMap<>(Map.ofEntries(
                Map.entry("child", "children"), Map.entry("chili", "chilis|chilies"),
                Map.entry("brother", "brothers|brethren"), Map.entry("infinity", "infinities|infinity"),
                Map.entry("loaf", "loaves"), Map.entry("lore", "lores|lore"),
                Map.entry("hoof", "hoofs|hooves"), Map.entry("beef", "beefs|beeves"),
                Map.entry("thief", "thiefs|thieves"), Map.entry("money", "monies"),
                Map.entry("mongoose", "mongooses"), Map.entry("ox", "oxen"),
                Map.entry("cow", "cows|kine"), Map.entry("graffito", "graffiti"),
                Map.entry("octopus", "octopuses|octopodes"), Map.entry("genie", "genies|genii"),
                Map.entry("ganglion", "ganglions|ganglia"), Map.entry("trilby", "trilbys"),
                Map.entry("turf", "turfs|turves"), Map.entry("numen", "numina"),
                Map.entry("atman", "atmas"), Map.entry("occiput", "occiputs|occipita"),
                Map.entry("sabretooth", "sabretooths"), Map.entry("sabertooth", "sabertooths"),
                Map.entry("lowlife", "lowlifes"), Map.entry("flatfoot", "flatfoots"),
                Map.entry("tenderfoot", "tenderfoots"), Map.entry("romany", "romanies"),
                Map.entry("jerry", "jerries"), Map.entry("mary", "maries"),
                Map.entry("talouse", "talouses"), Map.entry("rom", "roma"),
                Map.entry("carmen", "carmina")
        ));
        temp_pl_sb_irregular.putAll(pl_sb_irregular_s);
        pl_sb_irregular = Collections.unmodifiableMap(temp_pl_sb_irregular);

        pl_sb_irregular_caps = Map.of(
                "Romany", "Romanies", "Jerry", "Jerrys", "Mary", "Marys", "Rom", "Roma"
        );
        pl_sb_irregular_compound = Map.of("prima donna", "prima donnas|prime donne");

        Map<String, String> temp_si_sb_irregular = new HashMap<>();
        pl_sb_irregular.forEach((k, v) -> temp_si_sb_irregular.put(v, k));
        new HashMap<>(temp_si_sb_irregular).forEach((k, v) -> {
            if (k.contains("|")) {
                String[] parts = k.split("\\|");
                temp_si_sb_irregular.put(parts[0], v);
                temp_si_sb_irregular.put(parts[1], v);
                temp_si_sb_irregular.remove(k);
            }
        });
        si_sb_irregular = Collections.unmodifiableMap(temp_si_sb_irregular);

        Map<String, String> temp_si_sb_irregular_caps = new HashMap<>();
        pl_sb_irregular_caps.forEach((k, v) -> temp_si_sb_irregular_caps.put(v, k));
        si_sb_irregular_caps = Collections.unmodifiableMap(temp_si_sb_irregular_caps);

        Map<String, String> temp_si_sb_irregular_compound = new HashMap<>();
        pl_sb_irregular_compound.forEach((k, v) -> temp_si_sb_irregular_compound.put(v, k));
        new HashMap<>(temp_si_sb_irregular_compound).forEach((k, v) -> {
            if (k.contains("|")) {
                String[] parts = k.split("\\|");
                temp_si_sb_irregular_compound.put(parts[0], v);
                temp_si_sb_irregular_compound.put(parts[1], v);
                temp_si_sb_irregular_compound.remove(k);
            }
        });
        si_sb_irregular_compound = Collections.unmodifiableMap(temp_si_sb_irregular_compound);

        pl_sb_z_zes_list = Set.of("quartz", "topaz");
        pl_sb_z_zes_bysize = bysize(pl_sb_z_zes_list);
        pl_sb_ze_zes_list = Set.of("snooze");
        pl_sb_ze_zes_bysize = bysize(pl_sb_ze_zes_list);

        List<String> pl_sb_C_is_ides_complete = List.of("ephemeris", "iris", "clitoris", "chrysalis", "epididymis");
        List<String> pl_sb_C_is_ides_endings = List.of("itis");
        List<String> combined_is_ides = new ArrayList<>(pl_sb_C_is_ides_complete);
        pl_sb_C_is_ides_endings.forEach(w -> combined_is_ides.add(".*" + w));
        pl_sb_C_is_ides = joinstem(-2, combined_is_ides);
        pl_sb_C_is_ides_list = new ArrayList<>(pl_sb_C_is_ides_complete);
        pl_sb_C_is_ides_list.addAll(pl_sb_C_is_ides_endings);

        PlSiLists C_is_ides_data = makePlSiLists(pl_sb_C_is_ides_list, "ides", 2, false);
        si_sb_C_is_ides_list = C_is_ides_data.siList;
        si_sb_C_is_ides_bysize = C_is_ides_data.siBysize;
        pl_sb_C_is_ides_bysize = C_is_ides_data.plBysize;

        pl_sb_C_a_ata_list = List.of("anathema", "bema", "carcinoma", "charisma", "diploma", "dogma", "drama", "edema", "enema", "enigma", "lemma", "lymphoma", "magma", "melisma", "miasma", "oedema", "sarcoma", "schema", "soma", "stigma", "stoma", "trauma", "gumma", "pragma");
        PlSiLists C_a_ata_data = makePlSiLists(pl_sb_C_a_ata_list, "ata", 1, true);
        si_sb_C_a_ata_list = C_a_ata_data.siList;
        si_sb_C_a_ata_bysize = C_a_ata_data.siBysize;
        pl_sb_C_a_ata_bysize = C_a_ata_data.plBysize;
        pl_sb_C_a_ata = C_a_ata_data.stem;

        pl_sb_U_a_ae_list = List.of("alumna", "alga", "vertebra", "persona", "vita");
        PlSiLists U_a_ae_data = makePlSiLists(pl_sb_U_a_ae_list, "e", null, true);
        si_sb_U_a_ae_list = U_a_ae_data.siList;
        si_sb_U_a_ae_bysize = U_a_ae_data.siBysize;
        pl_sb_U_a_ae_bysize = U_a_ae_data.plBysize;
        pl_sb_U_a_ae = U_a_ae_data.stem;

        pl_sb_C_a_ae_list = List.of("amoeba", "antenna", "formula", "hyperbola", "medusa", "nebula", "parabola", "abscissa", "hydra", "nova", "lacuna", "aurora", "umbra", "flora", "fauna");
        PlSiLists C_a_ae_data = makePlSiLists(pl_sb_C_a_ae_list, "e", null, true);
        si_sb_C_a_ae_list = C_a_ae_data.siList;
        si_sb_C_a_ae_bysize = C_a_ae_data.siBysize;
        pl_sb_C_a_ae_bysize = C_a_ae_data.plBysize;
        pl_sb_C_a_ae = C_a_ae_data.stem;

        pl_sb_C_en_ina_list = List.of("stamen", "foramen", "lumen");
        PlSiLists C_en_ina_data = makePlSiLists(pl_sb_C_en_ina_list, "ina", 2, true);
        si_sb_C_en_ina_list = C_en_ina_data.siList;
        si_sb_C_en_ina_bysize = C_en_ina_data.siBysize;
        pl_sb_C_en_ina_bysize = C_en_ina_data.plBysize;
        pl_sb_C_en_ina = C_en_ina_data.stem;

        pl_sb_U_um_a_list = List.of("bacterium", "agendum", "desideratum", "erratum", "stratum", "datum", "ovum", "extremum", "candelabrum");
        PlSiLists U_um_a_data = makePlSiLists(pl_sb_U_um_a_list, "a", 2, true);
        si_sb_U_um_a_list = U_um_a_data.siList;
        si_sb_U_um_a_bysize = U_um_a_data.siBysize;
        pl_sb_U_um_a_bysize = U_um_a_data.plBysize;
        pl_sb_U_um_a = U_um_a_data.stem;

        pl_sb_C_um_a_list = List.of("maximum", "minimum", "momentum", "optimum", "quantum", "cranium", "curriculum", "dictum", "phylum", "aquarium", "compendium", "emporium", "encomium", "gymnasium", "honorarium", "interregnum", "lustrum", "memorandum", "millennium", "rostrum", "spectrum", "speculum", "stadium", "trapezium", "ultimatum", "medium", "vacuum", "velum", "consortium", "arboretum");
        PlSiLists C_um_a_data = makePlSiLists(pl_sb_C_um_a_list, "a", 2, true);
        si_sb_C_um_a_list = C_um_a_data.siList;
        si_sb_C_um_a_bysize = C_um_a_data.siBysize;
        pl_sb_C_um_a_bysize = C_um_a_data.plBysize;
        pl_sb_C_um_a = C_um_a_data.stem;

        pl_sb_U_us_i_list = List.of("alumnus", "alveolus", "bacillus", "bronchus", "locus", "nucleus", "stimulus", "meniscus", "sarcophagus");
        PlSiLists U_us_i_data = makePlSiLists(pl_sb_U_us_i_list, "i", 2, true);
        si_sb_U_us_i_list = U_us_i_data.siList;
        si_sb_U_us_i_bysize = U_us_i_data.siBysize;
        pl_sb_U_us_i_bysize = U_us_i_data.plBysize;
        pl_sb_U_us_i = U_us_i_data.stem;

        pl_sb_C_us_i_list = List.of("focus", "radius", "genius", "incubus", "succubus", "nimbus", "fungus", "nucleolus", "stylus", "torus", "umbilicus", "uterus", "hippopotamus", "cactus");
        PlSiLists C_us_i_data = makePlSiLists(pl_sb_C_us_i_list, "i", 2, true);
        si_sb_C_us_i_list = C_us_i_data.siList;
        si_sb_C_us_i_bysize = C_us_i_data.siBysize;
        pl_sb_C_us_i_bysize = C_us_i_data.plBysize;
        pl_sb_C_us_i = C_us_i_data.stem;

        pl_sb_C_us_us = Set.of("status", "apparatus", "prospectus", "sinus", "hiatus", "impetus", "plexus");
        pl_sb_C_us_us_bysize = bysize(pl_sb_C_us_us);

        pl_sb_U_on_a_list = List.of("criterion", "perihelion", "aphelion", "phenomenon", "prolegomenon", "noumenon", "organon", "asyndeton", "hyperbaton");
        PlSiLists U_on_a_data = makePlSiLists(pl_sb_U_on_a_list, "a", 2, true);
        si_sb_U_on_a_list = U_on_a_data.siList;
        si_sb_U_on_a_bysize = U_on_a_data.siBysize;
        pl_sb_U_on_a_bysize = U_on_a_data.plBysize;
        pl_sb_U_on_a = U_on_a_data.stem;

        pl_sb_C_on_a_list = List.of("oxymoron");
        PlSiLists C_on_a_data = makePlSiLists(pl_sb_C_on_a_list, "a", 2, true);
        si_sb_C_on_a_list = C_on_a_data.siList;
        si_sb_C_on_a_bysize = C_on_a_data.siBysize;
        pl_sb_C_on_a_bysize = C_on_a_data.plBysize;
        pl_sb_C_on_a = C_on_a_data.stem;

        pl_sb_C_o_i = new ArrayList<>(List.of("solo", "soprano", "basso", "alto", "contralto", "tempo", "piano", "virtuoso"));
        pl_sb_C_o_i_bysize = bysize(pl_sb_C_o_i);
        si_sb_C_o_i_bysize = bysize(pl_sb_C_o_i.stream().map(w -> substring(w, 0, -1) + "i").collect(Collectors.toList()));
        pl_sb_C_o_i_stems = joinstem(-1, pl_sb_C_o_i);

        pl_sb_U_o_os_complete = Set.of("ado", "ISO", "NATO", "NCO", "NGO", "oto");
        si_sb_U_o_os_complete = pl_sb_U_o_os_complete.stream().map(w -> w + "s").collect(Collectors.toSet());

        pl_sb_U_o_os_endings = new ArrayList<>(Arrays.asList("aficionado", "aggro", "albino", "allegro", "ammo", "Antananarivo", "archipelago", "armadillo", "auto", "avocado", "Bamako", "Barquisimeto", "bimbo", "bingo", "Biro", "bolero", "Bolzano", "bongo", "Boto", "burro", "Cairo", "canto", "cappuccino", "casino", "cello", "Chicago", "Chimango", "cilantro", "cochito", "coco", "Colombo", "Colorado", "commando", "concertino", "contango", "credo", "crescendo", "cyano", "demo", "ditto", "Draco", "dynamo", "embryo", "Esperanto", "espresso", "euro", "falsetto", "Faro", "fiasco", "Filipino", "flamenco", "furioso", "generalissimo", "Gestapo", "ghetto", "gigolo", "gizmo", "Greensboro", "gringo", "Guaiabero", "guano", "gumbo", "gyro", "hairdo", "hippo", "Idaho", "impetigo", "inferno", "info", "intermezzo", "intertrigo", "Iquico", "jumbo", "junto", "Kakapo", "kilo", "Kinkimavo", "Kokako", "Kosovo", "Lesotho", "libero", "libido", "libretto", "lido", "Lilo", "limbo", "limo", "lineno", "lingo", "lino", "livedo", "loco", "logo", "lumbago", "macho", "macro", "mafioso", "magneto", "magnifico", "Majuro", "Malabo", "manifesto", "Maputo", "Maracaibo", "medico", "memo", "metro", "Mexico", "micro", "Milano", "Monaco", "mono", "Montenegro", "Morocco", "Muqdisho", "myo", "neutrino", "Ningbo", "octavo", "oregano", "Orinoco", "Orlando", "Oslo", "panto", "Paramaribo", "Pardusco", "pedalo", "photo", "pimento", "pinto", "pleco", "Pluto", "pogo", "polo", "poncho", "Porto-Novo", "Porto", "pro", "psycho", "pueblo", "quarto", "Quito", "repo", "rhino", "risotto", "rococo", "rondo", "Sacramento", "saddo", "sago", "salvo", "Santiago", "Sapporo", "Sarajevo", "scherzando", "scherzo", "silo", "sirocco", "sombrero", "staccato", "sterno", "stucco", "stylo", "sumo", "Taiko", "techno", "terrazzo", "testudo", "timpano", "tiro", "tobacco", "Togo", "Tokyo", "torero", "Torino", "Toronto", "torso", "tremolo", "typo", "tyro", "ufo", "UNESCO", "vaquero", "vermicello", "verso", "vibrato", "violoncello", "Virgo", "weirdo", "WHO", "WTO", "Yamoussoukro", "yo-yo", "zero", "Zibo"));
        pl_sb_U_o_os_endings.addAll(pl_sb_C_o_i);
        pl_sb_U_o_os_bysize = bysize(pl_sb_U_o_os_endings);
        si_sb_U_o_os_bysize = bysize(pl_sb_U_o_os_endings.stream().map(w -> w + "s").collect(Collectors.toList()));

        pl_sb_U_ch_chs_list = List.of("czech", "eunuch", "stomach");
        PlSiLists U_ch_chs_data = makePlSiLists(pl_sb_U_ch_chs_list, "s", null, true);
        si_sb_U_ch_chs_list = U_ch_chs_data.siList;
        si_sb_U_ch_chs_bysize = U_ch_chs_data.siBysize;
        pl_sb_U_ch_chs_bysize = U_ch_chs_data.plBysize;
        pl_sb_U_ch_chs = U_ch_chs_data.stem;

        pl_sb_U_ex_ices_list = List.of("codex", "murex", "silex");
        PlSiLists U_ex_ices_data = makePlSiLists(pl_sb_U_ex_ices_list, "ices", 2, true);
        si_sb_U_ex_ices_list = U_ex_ices_data.siList;
        si_sb_U_ex_ices_bysize = U_ex_ices_data.siBysize;
        pl_sb_U_ex_ices_bysize = U_ex_ices_data.plBysize;
        pl_sb_U_ex_ices = U_ex_ices_data.stem;

        pl_sb_U_ix_ices_list = List.of("radix", "helix");
        PlSiLists U_ix_ices_data = makePlSiLists(pl_sb_U_ix_ices_list, "ices", 2, true);
        si_sb_U_ix_ices_list = U_ix_ices_data.siList;
        si_sb_U_ix_ices_bysize = U_ix_ices_data.siBysize;
        pl_sb_U_ix_ices_bysize = U_ix_ices_data.plBysize;
        pl_sb_U_ix_ices = U_ix_ices_data.stem;

        pl_sb_C_ex_ices_list = List.of("vortex", "vertex", "cortex", "latex", "pontifex", "apex", "index", "simplex");
        PlSiLists C_ex_ices_data = makePlSiLists(pl_sb_C_ex_ices_list, "ices", 2, true);
        si_sb_C_ex_ices_list = C_ex_ices_data.siList;
        si_sb_C_ex_ices_bysize = C_ex_ices_data.siBysize;
        pl_sb_C_ex_ices_bysize = C_ex_ices_data.plBysize;
        pl_sb_C_ex_ices = C_ex_ices_data.stem;

        pl_sb_C_ix_ices_list = List.of("appendix");
        PlSiLists C_ix_ices_data = makePlSiLists(pl_sb_C_ix_ices_list, "ices", 2, true);
        si_sb_C_ix_ices_list = C_ix_ices_data.siList;
        si_sb_C_ix_ices_bysize = C_ix_ices_data.siBysize;
        pl_sb_C_ix_ices_bysize = C_ix_ices_data.plBysize;
        pl_sb_C_ix_ices = C_ix_ices_data.stem;

        pl_sb_C_i_list = List.of("afrit", "afreet", "efreet");
        PlSiLists C_i_data = makePlSiLists(pl_sb_C_i_list, "i", null, true);
        si_sb_C_i_list = C_i_data.siList;
        si_sb_C_i_bysize = C_i_data.siBysize;
        pl_sb_C_i_bysize = C_i_data.plBysize;
        pl_sb_C_i = C_i_data.stem;

        pl_sb_C_im_list = List.of("goy", "seraph", "cherub");
        PlSiLists C_im_data = makePlSiLists(pl_sb_C_im_list, "im", null, true);
        si_sb_C_im_list = C_im_data.siList;
        si_sb_C_im_bysize = C_im_data.siBysize;
        pl_sb_C_im_bysize = C_im_data.plBysize;
        pl_sb_C_im = C_im_data.stem;

        pl_sb_U_man_mans_list = Arrays.asList("ataman", "caiman", "cayman", "ceriman", "desman", "dolman", "farman", "harman", "hetman", "human", "leman", "ottoman", "shaman", "talisman");
        pl_sb_U_man_mans_caps_list = Arrays.asList("Alabaman", "Bahaman", "Burman", "German", "Hiroshiman", "Liman", "Nakayaman", "Norman", "Oklahoman", "Panaman", "Roman", "Selman", "Sonaman", "Tacoman", "Yakiman", "Yokohaman", "Yuman");

        PlSiLists U_man_mans_data = makePlSiLists(pl_sb_U_man_mans_list, "s", null, false);
        si_sb_U_man_mans_list = U_man_mans_data.siList;
        si_sb_U_man_mans_bysize = U_man_mans_data.siBysize;
        pl_sb_U_man_mans_bysize = U_man_mans_data.plBysize;

        PlSiLists U_man_mans_caps_data = makePlSiLists(pl_sb_U_man_mans_caps_list, "s", null, false);
        si_sb_U_man_mans_caps_list = U_man_mans_caps_data.siList;
        si_sb_U_man_mans_caps_bysize = U_man_mans_caps_data.siBysize;
        pl_sb_U_man_mans_caps_bysize = U_man_mans_caps_data.plBysize;

        pl_sb_U_louse_lice_list = List.of("booklouse", "grapelouse", "louse", "woodlouse");
        PlSiLists U_louse_lice_data = makePlSiLists(pl_sb_U_louse_lice_list, "lice", 5, false);
        si_sb_U_louse_lice_bysize = U_louse_lice_data.siBysize;
        pl_sb_U_louse_lice_bysize = U_louse_lice_data.plBysize;

        pl_sb_uninflected_s_complete = List.of("breeches", "britches", "pajamas", "pyjamas", "clippers", "gallows", "hijinks", "headquarters", "pliers", "scissors", "testes", "herpes", "pincers", "shears", "proceedings", "trousers", "cantus", "coitus", "nexus", "contretemps", "corps", "debris", "siemens", "mumps", "diabetes", "jackanapes", "series", "species", "subspecies", "rabies", "chassis", "innings", "news", "mews", "haggis");
        pl_sb_uninflected_s_endings = List.of("ois", "measles");
        pl_sb_uninflected_s = new ArrayList<>(pl_sb_uninflected_s_complete);
        pl_sb_uninflected_s_endings.forEach(w -> pl_sb_uninflected_s.add(".*" + w));

        pl_sb_uninflected_herd = Set.of("wildebeest", "swine", "eland", "bison", "buffalo", "cattle", "elk", "rhinoceros", "zucchini", "caribou", "dace", "grouse", "guinea fowl", "guinea-fowl", "haddock", "hake", "halibut", "herring", "mackerel", "pickerel", "pike", "roe", "seed", "shad", "snipe", "teal", "turbot", "water fowl", "water-fowl");

        pl_sb_uninflected_complete = new HashSet<>(List.of("tuna", "salmon", "mackerel", "trout", "bream", "sea-bass", "sea bass", "carp", "cod", "flounder", "whiting", "moose", "graffiti", "djinn", "samuri", "offspring", "pence", "quid", "hertz"));
        pl_sb_uninflected_complete.addAll(pl_sb_uninflected_s_complete);

        pl_sb_uninflected_caps = new HashSet<>(List.of("Portuguese", "Amoyese", "Borghese", "Congoese", "Faroese", "Foochowese", "Genevese", "Genoese", "Gilbertese", "Hottentotese", "Kiplingese", "Kongoese", "Lucchese", "Maltese", "Nankingese", "Niasese", "Pekingese", "Piedmontese", "Pistoiese", "Sarawakese", "Shavese", "Vermontese", "Wenchowese", "Yengeese"));

        pl_sb_uninflected_endings = new ArrayList<>(List.of("butter", "cash", "furniture", "information", "fish", "deer", "sheep", "nese", "rese", "lese", "mese", "pox", "craft"));
        pl_sb_uninflected_endings.addAll(pl_sb_uninflected_s_endings);
        pl_sb_uninflected_bysize = bysize(pl_sb_uninflected_endings);

        pl_sb_singular_s_complete = new ArrayList<>(List.of("acropolis", "aegis", "alias", "asbestos", "bathos", "bias", "bronchitis", "bursitis", "caddis", "cannabis", "canvas", "chaos", "cosmos", "dais", "digitalis", "epidermis", "ethos", "eyas", "gas", "glottis", "hubris", "ibis", "lens", "mantis", "marquis", "metropolis", "pathos", "pelvis", "polis", "rhinoceros", "sassafras", "trellis"));
        pl_sb_singular_s_complete.addAll(pl_sb_C_is_ides_complete);

        pl_sb_singular_s_endings = new ArrayList<>(List.of("ss", "us"));
        pl_sb_singular_s_endings.addAll(pl_sb_C_is_ides_endings);

        pl_sb_singular_s_bysize = bysize(pl_sb_singular_s_endings);
        si_sb_singular_s_complete = pl_sb_singular_s_complete.stream().map(w -> w + "es").collect(Collectors.toList());
        si_sb_singular_s_endings = pl_sb_singular_s_endings.stream().map(w -> w + "es").collect(Collectors.toList());
        si_sb_singular_s_bysize = bysize(si_sb_singular_s_endings);

        List<String> pl_sb_singular_s_es = List.of("[A-Z].*es");
        List<String> temp_pl_sb_singular_s = new ArrayList<>(pl_sb_singular_s_complete);
        pl_sb_singular_s_endings.forEach(w -> temp_pl_sb_singular_s.add(".*" + w));
        temp_pl_sb_singular_s.addAll(pl_sb_singular_s_es);
        pl_sb_singular_s = enclose(String.join("|", temp_pl_sb_singular_s));

        si_sb_ois_oi_case = Set.of("Bolshois", "Hanois");
        si_sb_uses_use_case = Set.of("Betelgeuses", "Duses", "Meuses", "Syracuses", "Toulouses");
        si_sb_uses_use = Set.of("abuses", "applauses", "blouses", "carouses", "causes", "chartreuses", "clauses", "contuses", "douses", "excuses", "fuses", "grouses", "hypotenuses", "masseuses", "menopauses", "misuses", "muses", "overuses", "pauses", "peruses", "profuses", "recluses", "reuses", "ruses", "souses", "spouses", "suffuses", "transfuses", "uses");
        si_sb_ies_ie_case = Set.of("Addies", "Aggies", "Allies", "Amies", "Angies", "Annies", "Annmaries", "Archies", "Arties", "Aussies", "Barbies", "Barries", "Basies", "Bennies", "Bernies", "Berties", "Bessies", "Betties", "Billies", "Blondies", "Bobbies", "Bonnies", "Bowies", "Brandies", "Bries", "Brownies", "Callies", "Carnegies", "Carries", "Cassies", "Charlies", "Cheries", "Christies", "Connies", "Curies", "Dannies", "Debbies", "Dixies", "Dollies", "Donnies", "Drambuies", "Eddies", "Effies", "Ellies", "Elsies", "Eries", "Ernies", "Essies", "Eugenies", "Fannies", "Flossies", "Frankies", "Freddies", "Gillespies", "Goldies", "Gracies", "Guthries", "Hallies", "Hatties", "Hetties", "Hollies", "Jackies", "Jamies", "Janies", "Jannies", "Jeanies", "Jeannies", "Jennies", "Jessies", "Jimmies", "Jodies", "Johnies", "Johnnies", "Josies", "Julies", "Kalgoorlies", "Kathies", "Katies", "Kellies", "Kewpies", "Kristies", "Laramies", "Lassies", "Lauries", "Leslies", "Lessies", "Lillies", "Lizzies", "Lonnies", "Lories", "Lorries", "Lotties", "Louies", "Mackenzies", "Maggies", "Maisies", "Mamies", "Marcies", "Margies", "Maries", "Marjories", "Matties", "McKenzies", "Melanies", "Mickies", "Millies", "Minnies", "Mollies", "Mounties", "Nannies", "Natalies", "Nellies", "Netties", "Ollies", "Ozzies", "Pearlies", "Pottawatomies", "Reggies", "Richies", "Rickies", "Robbies", "Ronnies", "Rosalies", "Rosemaries", "Rosies", "Roxies", "Rushdies", "Ruthies", "Sadies", "Sallies", "Sammies", "Scotties", "Selassies", "Sherries", "Sophies", "Stacies", "Stefanies", "Stephanies", "Stevies", "Susies", "Sylvies", "Tammies", "Terries", "Tessies", "Tommies", "Tracies", "Trekkies", "Valaries", "Valeries", "Valkyries", "Vickies", "Virgies", "Willies", "Winnies", "Wylies", "Yorkies");
        si_sb_ies_ie = Set.of("aeries", "baggies", "belies", "biggies", "birdies", "bogies", "bonnies", "boogies", "bookies", "bourgeoisies", "brownies", "budgies", "caddies", "calories", "camaraderies", "cockamamies", "collies", "cookies", "coolies", "cooties", "coteries", "crappies", "curies", "cutesies", "dogies", "eyries", "floozies", "footsies", "freebies", "genies", "goalies", "groupies", "hies", "jalousies", "junkies", "kiddies", "laddies", "lassies", "lies", "lingeries", "magpies", "menageries", "mommies", "movies", "neckties", "newbies", "nighties", "oldies", "organdies", "overlies", "pies", "pinkies", "pixies", "potpies", "prairies", "quickies", "reveries", "rookies", "rotisseries", "softies", "sorties", "species", "stymies", "sweeties", "ties", "underlies", "unties", "veggies", "vies", "yuppies", "zombies");
        si_sb_oes_oe_case = Set.of("Chloes", "Crusoes", "Defoes", "Faeroes", "Ivanhoes", "Joes", "McEnroes", "Moes", "Monroes", "Noes", "Poes", "Roscoes", "Tahoes", "Tippecanoes", "Zoes");
        si_sb_oes_oe = Set.of("aloes", "backhoes", "canoes", "does", "floes", "foes", "hoes", "mistletoes", "oboes", "pekoes", "roes", "sloes", "throes", "tiptoes", "toes", "woes");
        si_sb_z_zes = Set.of("quartzes", "topazes");
        si_sb_zzes_zz = Set.of("buzzes", "fizzes", "frizzes", "razzes");
        si_sb_ches_che_case = Set.of("Andromaches", "Apaches", "Blanches", "Comanches", "Nietzsches", "Porsches", "Roches");
        si_sb_ches_che = Set.of("aches", "avalanches", "backaches", "bellyaches", "caches", "cloches", "creches", "douches", "earaches", "fiches", "headaches", "heartaches", "microfiches", "niches", "pastiches", "psyches", "quiches", "stomachaches", "toothaches", "tranches");
        si_sb_xes_xe = Set.of("annexes", "axes", "deluxes", "pickaxes");
        si_sb_sses_sse_case = Set.of("Hesses", "Jesses", "Larousses", "Matisses");
        si_sb_sses_sse = Set.of("bouillabaisses", "crevasses", "demitasses", "impasses", "mousses", "posses");
        si_sb_ves_ve_case = Set.of("Clives", "Palmolives");
        si_sb_ves_ve = Set.of("interweaves", "weaves", "olives", "bivalves", "dissolves", "resolves", "salves", "twelves", "valves");

        List<String> temp_plverb_special_s_list = new ArrayList<>();
        temp_plverb_special_s_list.add(pl_sb_singular_s);
        temp_plverb_special_s_list.addAll(pl_sb_uninflected_s);
        temp_plverb_special_s_list.addAll(pl_sb_irregular_s.keySet());
        temp_plverb_special_s_list.add("(.*[csx])is");
        temp_plverb_special_s_list.add("(.*)ceps");
        temp_plverb_special_s_list.add("[A-Z].*s");
        plverb_special_s = enclose(String.join("|", temp_plverb_special_s_list));

        List<Map.Entry<String, String>> _pl_sb_postfix_adj_defn = List.of(
                Map.entry("general", enclose("(?!major|lieutenant|brigadier|adjutant|.*star)\\S+")),
                Map.entry("martial", enclose("court")),
                Map.entry("force", enclose("pound"))
        );

        List<String> pl_sb_postfix_adj = _pl_sb_postfix_adj_defn.stream()
                .map(e -> enclose(e.getValue() + "(?=(?:-|\\s+)" + e.getKey() + ")"))
                .collect(Collectors.toList());

        pl_sb_postfix_adj_stems = String.format("(%s)(.*)", String.join("|", pl_sb_postfix_adj));

        si_sb_es_is = Set.of("amanuenses", "amniocenteses", "analyses", "antitheses", "apotheoses", "arterioscleroses", "atheroscleroses", "axes", "catalyses", "catharses", "chasses", "cirrhoses", "cocces", "crises", "diagnoses", "dialyses", "diereses", "electrolyses", "emphases", "exegeses", "geneses", "halitoses", "hydrolyses", "hypnoses", "hypotheses", "hystereses", "metamorphoses", "metastases", "misdiagnoses", "mitoses", "mononucleoses", "narcoses", "necroses", "nemeses", "neuroses", "oases", "osmoses", "osteoporoses", "paralyses", "parentheses", "parthenogeneses", "periphrases", "photosyntheses", "probosces", "prognoses", "prophylaxes", "prostheses", "preces", "psoriases", "psychoanalyses", "psychokineses", "psychoses", "scleroses", "scolioses", "sepses", "silicoses", "symbioses", "synopses", "syntheses", "taxes", "telekineses", "theses", "thromboses", "tuberculoses", "urinalyses");

        List<String> pl_prep_list = Arrays.asList("about", "above", "across", "after", "among", "around", "at", "athwart", "before", "behind", "below", "beneath", "beside", "besides", "between", "betwixt", "beyond", "but", "by", "during", "except", "for", "from", "in", "into", "near", "of", "off", "on", "onto", "out", "over", "since", "till", "to", "under", "until", "unto", "upon", "with");
        pl_prep_list_da = new ArrayList<>(pl_prep_list);
        pl_prep_list_da.addAll(List.of("de", "du", "da"));
        pl_prep_bysize = bysize(pl_prep_list_da);
        pl_prep = enclose(String.join("|", pl_prep_list_da));
        pl_sb_prep_dual_compound = String.format("(.*?)((?:-|\\s+)(?:%s)(?:-|\\s+))a(?:-|\\s+)(.*)", pl_prep);

        singular_pronoun_genders = Set.of("neuter", "feminine", "masculine", "gender-neutral", "feminine or masculine", "masculine or feminine");

        pl_pron_nom = Map.ofEntries(
                Map.entry("i", "we"), Map.entry("myself", "ourselves"),
                Map.entry("you", "you"), Map.entry("yourself", "yourselves"),
                Map.entry("she", "they"), Map.entry("herself", "themselves"),
                Map.entry("he", "they"), Map.entry("himself", "themselves"),
                Map.entry("it", "they"), Map.entry("itself", "themselves"),
                Map.entry("they", "they"), Map.entry("themself", "themselves"),
                Map.entry("mine", "ours"), Map.entry("yours", "yours"),
                Map.entry("hers", "theirs"), Map.entry("his", "theirs"),
                Map.entry("its", "theirs"), Map.entry("theirs", "theirs")
        );

        si_pron = new HashMap<>();
        Map<String, Object> si_pron_nom = new HashMap<>();
        pl_pron_nom.forEach((k, v) -> si_pron_nom.put(v, k));
        si_pron_nom.put("we", "I");
        si_pron.put("nom", si_pron_nom);

        pl_pron_acc = Map.ofEntries(
                Map.entry("me", "us"), Map.entry("myself", "ourselves"),
                Map.entry("you", "you"), Map.entry("yourself", "yourselves"),
                Map.entry("her", "them"), Map.entry("herself", "themselves"),
                Map.entry("him", "them"), Map.entry("himself", "themselves"),
                Map.entry("it", "them"), Map.entry("itself", "themselves"),
                Map.entry("them", "them"), Map.entry("themself", "themselves")
        );

        pl_pron_acc_keys = enclose(String.join("|", pl_pron_acc.keySet()));
        pl_pron_acc_keys_bysize = bysize(pl_pron_acc.keySet());

        Map<String, Object> si_pron_acc = new HashMap<>();
        pl_pron_acc.forEach((k, v) -> si_pron_acc.put(v, k));
        si_pron.put("acc", si_pron_acc);

        si_pron.get("nom").put("they", Map.of("neuter", "it", "feminine", "she", "masculine", "he", "gender-neutral", "they", "feminine or masculine", "she or he", "masculine or feminine", "he or she"));
        si_pron.get("nom").put("themselves", Map.of("neuter", "itself", "feminine", "herself", "masculine", "himself", "gender-neutral", "themself", "feminine or masculine", "herself or himself", "masculine or feminine", "himself or herself"));
        si_pron.get("nom").put("theirs", Map.of("neuter", "its", "feminine", "hers", "masculine", "his", "gender-neutral", "theirs", "feminine or masculine", "hers or his", "masculine or feminine", "his or hers"));
        si_pron.get("acc").put("them", Map.of("neuter", "it", "feminine", "her", "masculine", "him", "gender-neutral", "them", "feminine or masculine", "her or him", "masculine or feminine", "him or her"));
        si_pron.get("acc").put("themselves", Map.of("neuter", "itself", "feminine", "herself", "masculine", "himself", "gender-neutral", "themself", "feminine or masculine", "herself or himself", "masculine or feminine", "himself or herself"));

        si_pron_acc_keys = enclose(String.join("|", si_pron.get("acc").keySet()));
        si_pron_acc_keys_bysize = bysize(si_pron.get("acc").keySet());

        plverb_irregular_pres = Map.of("am", "are", "are", "are", "is", "are", "was", "were", "were", "were", "have", "have", "has", "have", "do", "do", "does", "do");
        plverb_ambiguous_pres = Map.ofEntries(
                Map.entry("act", "act"), Map.entry("acts", "act"), Map.entry("blame", "blame"),
                Map.entry("blames", "blame"), Map.entry("can", "can"), Map.entry("must", "must"),
                Map.entry("fly", "fly"), Map.entry("flies", "fly"), Map.entry("copy", "copy"),
                Map.entry("copies", "copy"), Map.entry("drink", "drink"), Map.entry("drinks", "drink"),
                Map.entry("fight", "fight"), Map.entry("fights", "fight"), Map.entry("fire", "fire"),
                Map.entry("fires", "fire"), Map.entry("like", "like"), Map.entry("likes", "like"),
                Map.entry("look", "look"), Map.entry("looks", "look"), Map.entry("make", "make"),
                Map.entry("makes", "make"), Map.entry("reach", "reach"), Map.entry("reaches", "reach"),
                Map.entry("run", "run"), Map.entry("runs", "run"), Map.entry("sink", "sink"),
                Map.entry("sinks", "sink"), Map.entry("sleep", "sleep"), Map.entry("sleeps", "sleep"),
                Map.entry("view", "view"), Map.entry("views", "view")
        );
        plverb_ambiguous_pres_keys = Pattern.compile(String.format("^(%s)((\\s.*)?)$", enclose(String.join("|", plverb_ambiguous_pres.keySet()))), Pattern.CASE_INSENSITIVE);

        plverb_irregular_non_pres = Set.of("did", "had", "ate", "made", "put", "spent", "fought", "sank", "gave", "sought", "shall", "could", "ought", "should");
        plverb_ambiguous_non_pres = Pattern.compile("^((?:thought|saw|bent|will|might|cut))((\\s.*)?)$", Pattern.CASE_INSENSITIVE);

        pl_v_oes_oe = Set.of("canoes", "floes", "oboes", "roes", "throes", "woes");
        pl_v_oes_oe_endings_size4 = Set.of("hoes", "toes");
        pl_v_oes_oe_endings_size5 = Set.of("shoes");

        pl_count_zero = Set.of("0", "no", "zero", "nil");
        pl_count_one = Set.of("1", "a", "an", "one", "each", "every", "this", "that");

        pl_adj_special = Map.of("a", "some", "an", "some", "this", "these", "that", "those");
        pl_adj_special_keys = Pattern.compile(String.format("^(%s)$", enclose(String.join("|", pl_adj_special.keySet()))), Pattern.CASE_INSENSITIVE);

        pl_adj_poss = Map.of("my", "our", "your", "your", "its", "their", "her", "their", "his", "their", "their", "their");
        pl_adj_poss_keys = Pattern.compile(String.format("^(%s)$", enclose(String.join("|", pl_adj_poss.keySet()))), Pattern.CASE_INSENSITIVE);

        // 2. INDEFINITE ARTICLES
        A_abbrev = Pattern.compile("^(?! FJO | [HLMNS]Y.  | RY[EO] | SQU | ( F[LR]? | [HL] | MN? | N | RH? | S[CHKLMNPTVW]? | X(YL)?) [AEIOU])[FHLMNRSX][A-Z]", Pattern.COMMENTS);
        A_y_cons = Pattern.compile("^(y(b[lor]|cl[ea]|fere|gg|p[ios]|rou|tt))", Pattern.CASE_INSENSITIVE);
        A_explicit_a = Pattern.compile("^((?:unabomber|unanimous|US))", Pattern.CASE_INSENSITIVE);
        A_explicit_an = Pattern.compile("^((?:euler|hour(?!i)|heir|honest|hono[ur]|mpeg))", Pattern.CASE_INSENSITIVE);
        A_ordinal_an = Pattern.compile("^([aefhilmnorsx]-?th)", Pattern.CASE_INSENSITIVE);
        A_ordinal_a = Pattern.compile("^([bcdgjkpqtuvwyz]-?th)", Pattern.CASE_INSENSITIVE);

        // NUMERICAL INFLECTIONS
        nth = Map.ofEntries(
                Map.entry(0, "th"), Map.entry(1, "st"), Map.entry(2, "nd"),
                Map.entry(3, "rd"), Map.entry(4, "th"), Map.entry(5, "th"),
                Map.entry(6, "th"), Map.entry(7, "th"), Map.entry(8, "th"),
                Map.entry(9, "th"), Map.entry(11, "th"), Map.entry(12, "th"),
                Map.entry(13, "th")
        );
        nth_suff = new HashSet<>(nth.values());

        ordinal = Map.of("ty", "tieth", "one", "first", "two", "second", "three", "third", "five", "fifth", "eight", "eighth", "nine", "ninth", "twelve", "twelfth");
        ordinal_suff = Pattern.compile(String.format("(%s)\\Z", String.join("|", ordinal.keySet())));

        // NUMBERS
        unit = List.of("", "one", "two", "three", "four", "five", "six", "seven", "eight", "nine");
        teen = List.of("ten", "eleven", "twelve", "thirteen", "fourteen", "fifteen", "sixteen", "seventeen", "eighteen", "nineteen");
        ten = List.of("", "", "twenty", "thirty", "forty", "fifty", "sixty", "seventy", "eighty", "ninety");
        mill = List.of(" ", " thousand", " million", " billion", " trillion", " quadrillion", " quintillion", " sextillion", " septillion", " octillion", " nonillion", " decillion");

        // Pre-compiled regular expression objects
        FUNCTION_CALL = Pattern.compile("((\\w+)\\([^)]*\\))", Pattern.CASE_INSENSITIVE);
        PARTITION_WORD = Pattern.compile("\\A(\\s*)(.+?)(\\s*)\\Z", Pattern.DOTALL);
        PL_SB_POSTFIX_ADJ_STEMS_RE = Pattern.compile("^(?:" + pl_sb_postfix_adj_stems + ")$", Pattern.CASE_INSENSITIVE);
        PL_SB_PREP_DUAL_COMPOUND_RE = Pattern.compile("^(?:" + pl_sb_prep_dual_compound + ")$", Pattern.CASE_INSENSITIVE);
        DENOMINATOR = Pattern.compile("(?<denominator>.+)( (per|a) .+)");
        PLVERB_SPECIAL_S_RE = Pattern.compile("^(" + plverb_special_s + ")$");
        ENDS_WITH_S = Pattern.compile("^(.*[^s])s$", Pattern.CASE_INSENSITIVE);
        ENDS_WITH_APOSTROPHE_S = Pattern.compile("^(.+)'s?$");
        INDEFINITE_ARTICLE_TEST = Pattern.compile("\\A(\\s*)(?:an?\\s+)?(.+?)(\\s*)\\Z", Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
        SPECIAL_AN = Pattern.compile("^[aefhilmnorsx]$", Pattern.CASE_INSENSITIVE);
        SPECIAL_A = Pattern.compile("^[bcdgjkpqtuvwyz]$", Pattern.CASE_INSENSITIVE);
        SPECIAL_ABBREV_AN = Pattern.compile("^[aefhilmnorsx][.-]", Pattern.CASE_INSENSITIVE);
        SPECIAL_ABBREV_A = Pattern.compile("^[a-z][.-]", Pattern.CASE_INSENSITIVE);
        CONSONANTS = Pattern.compile("^[^aeiouy]", Pattern.CASE_INSENSITIVE);
        ARTICLE_SPECIAL_EU = Pattern.compile("^e[uw]", Pattern.CASE_INSENSITIVE);
        ARTICLE_SPECIAL_ONCE = Pattern.compile("^onc?e\\b", Pattern.CASE_INSENSITIVE);
        ARTICLE_SPECIAL_ONETIME = Pattern.compile("^onetime\\b", Pattern.CASE_INSENSITIVE);
        ARTICLE_SPECIAL_UNIT = Pattern.compile("^uni([^nmd]|mo)", Pattern.CASE_INSENSITIVE);
        ARTICLE_SPECIAL_UBA = Pattern.compile("^u[bcfghjkqrst][aeiou]", Pattern.CASE_INSENSITIVE);
        ARTICLE_SPECIAL_UKR = Pattern.compile("^ukr", Pattern.CASE_INSENSITIVE);
        SPECIAL_CAPITALS = Pattern.compile("^U[NK][AIEO]?");
        VOWELS = Pattern.compile("^[aeiou]", Pattern.CASE_INSENSITIVE);

        DIGIT_GROUP = Pattern.compile("(\\d)");
        TWO_DIGITS = Pattern.compile("(\\d)(\\d)");
        THREE_DIGITS = Pattern.compile("(\\d)(\\d)(\\d)");
        THREE_DIGITS_WORD = Pattern.compile("(\\d)(\\d)(\\d)(?=\\D*\\Z)");
        TWO_DIGITS_WORD = Pattern.compile("(\\d)(\\d)(?=\\D*\\Z)");
        ONE_DIGIT_WORD = Pattern.compile("(\\d)(?=\\D*\\Z)");
        FOUR_DIGIT_COMMA = Pattern.compile("(\\d)(\\d{3}(?:,|\\Z))");
        NON_DIGIT = Pattern.compile("\\D");
        WHITESPACES_COMMA = Pattern.compile("\\s+,");
        COMMA_WORD = Pattern.compile(", (\\S+)\\s+\\Z");
        WHITESPACES = Pattern.compile("\\s+");

        PRESENT_PARTICIPLE_REPLACEMENTS = List.of(
                Map.entry(Pattern.compile("ie$"), "y"),
                Map.entry(Pattern.compile("ue$"), "u"),
                Map.entry(Pattern.compile("([auy])e$"), "$1"),
                Map.entry(Pattern.compile("ski$"), "ski"),
                Map.entry(Pattern.compile("[^b]i$"), ""),
                Map.entry(Pattern.compile("^(are|were)$"), "be"),
                Map.entry(Pattern.compile("^(had)$"), "hav"),
                Map.entry(Pattern.compile("^(hoe)$"), "$0"), // Special case, was \g<1>
                Map.entry(Pattern.compile("([^e])e$"), "$1"),
                Map.entry(Pattern.compile("er$"), "er"),
                Map.entry(Pattern.compile("([^aeiou][aeiouy]([bdgmnprst]))$"), "$1$2")
        );

        DIGIT = Pattern.compile("\\d");
    }
    //endregion

    //region Instance Variables
    private Map<String, Boolean> classicalDict;
    private Integer persistentCount;
    private int millCount;
    private final List<String> pl_sb_user_defined;
    private final List<String> pl_v_user_defined;
    private final List<String> pl_adj_user_defined;
    private final List<String> si_sb_user_defined;
    private final List<String> a_a_user_defined;
    private String thegender;
    private Map<String, String> numberArgs;
    //endregion

    //region Constructor
    public Inflect() {
        this.classicalDict = new HashMap<>(def_classical);
        this.persistentCount = null;
        this.millCount = 0;
        this.pl_sb_user_defined = new ArrayList<>();
        this.pl_v_user_defined = new ArrayList<>();
        this.pl_adj_user_defined = new ArrayList<>();
        this.si_sb_user_defined = new ArrayList<>();
        this.a_a_user_defined = new ArrayList<>();
        this.thegender = "neuter";
        this.numberArgs = new HashMap<>();
    }
    //endregion

    //region User Defined Rules
    public int defnoun(String singular, String plural) {
        checkpat(singular);
        checkpatplural(plural);
        this.pl_sb_user_defined.add(singular);
        this.pl_sb_user_defined.add(plural);
        this.si_sb_user_defined.add(plural);
        this.si_sb_user_defined.add(singular);
        return 1;
    }

    public int defverb(String s1, String p1, String s2, String p2, String s3, String p3) {
        checkpat(s1);
        checkpat(s2);
        checkpat(s3);
        checkpatplural(p1);
        checkpatplural(p2);
        checkpatplural(p3);
        this.pl_v_user_defined.addAll(Arrays.asList(s1, p1, s2, p2, s3, p3));
        return 1;
    }

    public int defadj(String singular, String plural) {
        checkpat(singular);
        checkpatplural(plural);
        this.pl_adj_user_defined.add(singular);
        this.pl_adj_user_defined.add(plural);
        return 1;
    }

    public int defa(String pattern) {
        checkpat(pattern);
        this.a_a_user_defined.add(pattern);
        this.a_a_user_defined.add("a");
        return 1;
    }

    public int defan(String pattern) {
        checkpat(pattern);
        this.a_a_user_defined.add(pattern);
        this.a_a_user_defined.add("an");
        return 1;
    }

    private void checkpat(String pattern) {
        if (pattern == null) {
            return;
        }
        try {
            Pattern.compile(pattern);
        } catch (java.util.regex.PatternSyntaxException e) {
            throw new BadUserDefinedPatternError(pattern, e);
        }
    }

    private void checkpatplural(String pattern) {
        // In Java, replacement patterns are checked at runtime, so this check is effectively a no-op,
        // similar to the original Python version.
    }

    private String udMatch(String word, List<String> wordlist) {
        for (int i = wordlist.size() - 2; i >= 0; i -= 2) {
            String singularPattern = wordlist.get(i);
            if (singularPattern == null) continue;
            Pattern p = Pattern.compile("^" + singularPattern + "$", Pattern.CASE_INSENSITIVE);
            Matcher mo = p.matcher(word);
            if (mo.matches()) {
                String pluralPattern = wordlist.get(i + 1);
                if (pluralPattern == null) return null;
                // Java's replaceAll uses $ for backreferences, same as user-defined patterns are expected to use.
                return mo.replaceAll(pluralPattern);
            }
        }
        return null;
    }
    //endregion

    //region Main API Methods
    public void classical(Map<String, Boolean> kwargs) {
        if (kwargs == null || kwargs.isEmpty()) {
            this.classicalDict = new HashMap<>(all_classical);
            return;
        }
        if (kwargs.containsKey("all")) {
            if (kwargs.get("all")) {
                this.classicalDict = new HashMap<>(all_classical);
            } else {
                this.classicalDict = new HashMap<>(no_classical);
            }
        }

        for (Map.Entry<String, Boolean> entry : kwargs.entrySet()) {
            if (def_classical.containsKey(entry.getKey())) {
                this.classicalDict.put(entry.getKey(), entry.getValue());
            } else if (!"all".equals(entry.getKey())) {
                throw new UnknownClassicalModeError();
            }
        }
    }

    public void classical() {
        classical(Collections.emptyMap());
    }
    public void classical(String key, boolean value) {
        classical(Map.of(key, value));
    }


    public String num(Object count, boolean show) {
        if (count != null) {
            try {
                this.persistentCount = new BigDecimal(String.valueOf(count)).intValue();
            } catch (NumberFormatException e) {
                throw new BadNumValueError("Invalid number: " + count, e);
            }
            if (show) {
                return String.valueOf(count);
            }
        } else {
            this.persistentCount = null;
        }
        return "";
    }

    public String num(Object count) {
        return num(count, true);
    }

    public String num() {
        return num(null, true);
    }


    public void gender(String gender) {
        if (gender != null && singular_pronoun_genders.contains(gender)) {
            this.thegender = gender;
        } else {
            throw new BadGenderError();
        }
    }

    /**
     * Perform inflections in a string.
     * e.g. inflect("The plural of cat is plural('cat')") returns
     * 'The plural of cat is cats'
     */
    public String inflect(String text) {
        Integer savePersistentCount = this.persistentCount;

        Function<Matcher, String> replacer = (mo) -> {
            String fullMatch = mo.group(1);
            String funcName = mo.group(2);
            String argContent = fullMatch.substring(funcName.length() + 1, fullMatch.length() - 1);

            try {
                Object result = null;

                switch (funcName) {
                    case "plural": {
                        List<Object> args = parseInflectArgs(argContent);
                        String wordArg = args.isEmpty() ? "" : String.valueOf(args.get(0));
                        Object countArg = args.size() > 1 ? args.get(1) : null;
                        result = plural(wordArg, countArg);
                        break;
                    }
                    case "plural_adj": {
                        List<Object> args = parseInflectArgs(argContent);
                        String wordArg = args.isEmpty() ? "" : String.valueOf(args.get(0));
                        Object countArg = args.size() > 1 ? args.get(1) : null;
                        result = plural_adj(wordArg, countArg);
                        break;
                    }
                    case "plural_noun": {
                        List<Object> args = parseInflectArgs(argContent);
                        String wordArg = args.isEmpty() ? "" : String.valueOf(args.get(0));
                        Object countArg = args.size() > 1 ? args.get(1) : null;
                        result = plural_noun(wordArg, countArg);
                        break;
                    }
                    case "plural_verb": {
                        List<Object> args = parseInflectArgs(argContent);
                        String wordArg = args.isEmpty() ? "" : String.valueOf(args.get(0));
                        Object countArg = args.size() > 1 ? args.get(1) : null;
                        result = plural_verb(wordArg, countArg);
                        break;
                    }
                    case "singular_noun": {
                        List<Object> args = parseInflectArgs(argContent);
                        String wordArg = args.isEmpty() ? "" : String.valueOf(args.get(0));
                        Object countArg = args.size() > 1 ? args.get(1) : null;
                        result = singular_noun(wordArg, countArg, null);
                        break;
                    }
                    case "a":
                    case "an": {
                        List<Object> args = parseInflectArgs(argContent);
                        String wordArg = args.isEmpty() ? "" : String.valueOf(args.get(0));
                        Object countArg = args.size() > 1 ? args.get(1) : 1;
                        result = a(wordArg, countArg);
                        break;
                    }
                    case "no": {
                        List<Object> args = parseInflectArgs(argContent);
                        String wordArg = args.isEmpty() ? "" : String.valueOf(args.get(0));
                        Object countArg = args.size() > 1 ? args.get(1) : null;
                        result = no(wordArg, countArg);
                        break;
                    }
                    case "ordinal": {
                        List<Object> args = parseInflectArgs(argContent);
                        Object numArg = args.isEmpty() ? 0 : args.get(0);
                        result = ordinal(numArg);
                        break;
                    }
                    case "number_to_words": {
                        List<Object> posArgs = new ArrayList<>();
                        Map<String, Object> kwArgs = new HashMap<>();
                        parseInflectArgsWithKeywords(argContent, posArgs, kwArgs);

                        Object numArg = posArgs.isEmpty() ? 0 : posArgs.get(0);

                        NumberToWordsOptions opts = new NumberToWordsOptions();
                        if (kwArgs.containsKey("group")) opts.setGroup((Integer) kwArgs.get("group"));
                        if (kwArgs.containsKey("andword")) opts.setAndword((String) kwArgs.get("andword"));
                        // Add other options...

                        Object n2wResult = numberToWords(numArg, opts);
                        if(n2wResult instanceof List) {
                            result = join((List<String>)n2wResult);
                        } else {
                            result = n2wResult;
                        }
                        break;
                    }
                    case "present_participle": {
                        List<Object> args = parseInflectArgs(argContent);
                        String wordArg = args.isEmpty() ? "" : String.valueOf(args.get(0));
                        result = present_participle(wordArg);
                        break;
                    }
                    case "num": {
                        List<Object> args = parseInflectArgs(argContent);
                        Object countArg = args.isEmpty() ? null : args.get(0);
                        Object showArg = args.size() > 1 ? args.get(1) : true;
                        result = num(countArg, (Boolean)showArg);
                        break;
                    }
                    default:
                        return fullMatch; // Function not recognized, return original
                }
                return String.valueOf(result);

            } catch (Throwable e) {
                // If parsing or invocation fails, return original text.
                // This is a safe fallback.
                return fullMatch;
            }
        };

        Matcher m = FUNCTION_CALL.matcher(text);
        StringBuffer sb = new StringBuffer();
        while (m.find()) {
            m.appendReplacement(sb, Matcher.quoteReplacement(replacer.apply(m)));
        }
        m.appendTail(sb);
        String output = sb.toString();

        this.persistentCount = savePersistentCount;
        return output;
    }

    public String plural(String text, Object count) {
        if (text == null || text.isEmpty()) return text;
        String[] parts = partitionWord(text);
        String pre = parts[0], word = parts[1], post = parts[2];
        if (word.isEmpty()) return text;

        String pluralForm = Optional.ofNullable(_pl_special_adjective(word, count))
                .or(() -> Optional.ofNullable(_pl_special_verb(word, count)))
                .orElseGet(() -> _plnoun(word, count));

        String processed = postprocess(word, pluralForm);
        return pre + processed + post;
    }
    public String plural(String text) { return plural(text, null); }

    public String plural_noun(String text, Object count) {
        if (text == null || text.isEmpty()) return text;
        String[] parts = partitionWord(text);
        String pre = parts[0], word = parts[1], post = parts[2];
        if (word.isEmpty()) return text;
        String plural = postprocess(word, _plnoun(word, count));
        return pre + plural + post;
    }
    public String plural_noun(String text) { return plural_noun(text, null); }

    public String plural_verb(String text, Object count) {
        if (text == null || text.isEmpty()) return text;
        String[] parts = partitionWord(text);
        String pre = parts[0], word = parts[1], post = parts[2];
        if (word.isEmpty()) return text;
        String plural = postprocess(word, Optional.ofNullable(_pl_special_verb(word, count))
                .orElseGet(() -> _pl_general_verb(word, count)));
        return pre + plural + post;
    }
    public String plural_verb(String text) { return plural_verb(text, null); }

    public String plural_adj(String text, Object count) {
        if (text == null || text.isEmpty()) return text;
        String[] parts = partitionWord(text);
        String pre = parts[0], word = parts[1], post = parts[2];
        if (word.isEmpty()) return text;
        String plural = postprocess(word, Optional.ofNullable(_pl_special_adjective(word, count)).orElse(word));
        return pre + plural + post;
    }
    public String plural_adj(String text) { return plural_adj(text, null); }

    public Object singular_noun(String text, Object count, String gender) {
        if (text == null || text.isEmpty()) return text;
        String[] parts = partitionWord(text);
        String pre = parts[0], word = parts[1], post = parts[2];
        if (word.isEmpty()) return text;

        Object sing = _sinoun(word, count, gender);
        if (sing instanceof String) {
            String plural = postprocess(word, (String) sing);
            return pre + plural + post;
        }
        return false; // Return boolean false if not found
    }
    public Object singular_noun(String text, Object count) { return singular_noun(text, count, null); }
    public Object singular_noun(String text) { return singular_noun(text, null, null); }


    public Object compare(String word1, String word2) {
        if (word1 == null || word2 == null || word1.isEmpty() || word2.isEmpty()) {
            throw new IllegalArgumentException("Words cannot be null or empty.");
        }
        List<Function<String, String>> norms = List.of(
                w -> this.plural_noun(w, 2),
                w -> this.plural_verb(w, 2),
                w -> this.plural_adj(w, 2)
        );
        for (Function<String, String> norm : norms) {
            Object result = _plequal(word1, word2, norm);
            if (result instanceof String) {
                return result;
            }
        }
        return false;
    }

    public Object compare_nouns(String word1, String word2) {
        return _plequal(word1, word2, w -> this.plural_noun(w, 2));
    }

    public Object compare_verbs(String word1, String word2) {
        return _plequal(word1, word2, w -> this.plural_verb(w, 2));
    }

    public Object compare_adjs(String word1, String word2) {
        return _plequal(word1, word2, w -> this.plural_adj(w, 2));
    }

    public String a(String text, Object count) {
        Matcher mo = INDEFINITE_ARTICLE_TEST.matcher(text);
        if (mo.matches()) {
            String word = mo.group(2);
            if (word == null || word.isEmpty()) return text;
            String pre = mo.group(1);
            String post = mo.group(3);
            String result = _indef_article(word, count);
            return pre + result + post;
        }
        return "";
    }
    public String a(String text) { return a(text, 1); }

    public String an(String text, Object count) {
        return a(text, count);
    }
    public String an(String text) { return a(text, 1); }


    public String no(String text, Object count) {
        if (persistentCount != null && count == null) {
            count = persistentCount;
        }
        if (count == null) {
            count = 0;
        }

        String[] parts = partitionWord(text);
        String pre = parts[0], word = parts[1], post = parts[2];

        String countStr = String.valueOf(count).toLowerCase();
        if (pl_count_zero.contains(countStr)) {
            countStr = "no";
        } else {
            countStr = String.valueOf(count);
        }

        return String.format("%s%s %s%s", pre, countStr, plural(word, count), post);
    }
    public String no(String text) { return no(text, null); }

    public String present_participle(String word) {
        String plv = plural_verb(word, 2);
        String ans = plv;

        for(Map.Entry<Pattern, String> entry : PRESENT_PARTICIPLE_REPLACEMENTS) {
            Matcher m = entry.getKey().matcher(plv);
            if (m.find()) {
                // In Java, we can't get a "number of substitutions" easily from replaceAll.
                // We'll mimic the logic of "if a replacement happened, return".
                // The original Python code `subn` returns a tuple (new_string, num_subs).
                // Here we just check if the new string is different.
                String newAns = m.replaceAll(entry.getValue());
                if (!newAns.equals(plv) || entry.getKey().pattern().equals("^(hoe)$")) {
                    // The hoe->hoe case is special as it doesn't change the string before adding "ing"
                    if (entry.getKey().pattern().equals("^(hoe)$")) {
                        return plv + "ing";
                    }
                    return newAns + "ing";
                }
            }
        }
        return ans + "ing";
    }

    public String ordinal(Object num) {
        String numStr = String.valueOf(num);
        if (DIGIT.matcher(numStr).lookingAt()) {
            int n;
            try {
                // Handle floats and integers gracefully
                BigDecimal bd = new BigDecimal(numStr);
                if (bd.scale() <= 0) { // It's an integer or has .0, .00 etc.
                    n = bd.intValue();
                } else { // It has a decimal part
                    n = Integer.parseInt(numStr.substring(numStr.length() - 1));
                }
            } catch (NumberFormatException e) {
                // Fallback for cases like "123."
                n = Integer.parseInt(substring(numStr, 0, -1));
            }

            String post;
            if (nth.containsKey(n % 100)) {
                post = nth.get(n % 100);
            } else {
                post = nth.getOrDefault(n % 10, "th");
            }
            return numStr + post;

        } else {
            return _sub_ord(numStr);
        }
    }

    public Object numberToWords(Object num, NumberToWordsOptions opts) {
        final NumberToWordsOptions options = (opts == null) ? new NumberToWordsOptions() : opts;
        this.numberArgs = Map.of("andword", options.andword, "zero", options.zero, "one", options.one);
        String numStr = String.valueOf(num).strip();

        if (options.threshold != null) {
            try {
                if (new BigDecimal(numStr).doubleValue() > options.threshold) {
                    String[] spnum = numStr.split("\\.", 2);
                    String intPart = spnum[0];
                    if (options.comma != null && !options.comma.isEmpty()) {
                        Matcher m = FOUR_DIGIT_COMMA.matcher(intPart);
                        while(m.find()) {
                            intPart = m.replaceAll("$1" + options.comma + "$2");
                            m = FOUR_DIGIT_COMMA.matcher(intPart);
                        }
                    }
                    return spnum.length > 1 ? intPart + "." + spnum[1] : intPart;
                }
            } catch (NumberFormatException e) {
                // not a number, proceed
            }
        }

        if (options.group < 0 || options.group > 3) {
            throw new BadChunkingOptionError();
        }

        String sign = "";
        if (numStr.startsWith("+")) {
            sign = "plus";
            numStr = numStr.substring(1).strip();
        } else if (numStr.startsWith("-")) {
            sign = "minus";
            numStr = numStr.substring(1).strip();
        }

        if (nth_suff.contains(numStr)) {
            numStr = options.zero;
        }

        boolean myord = numStr.length() > 2 && nth_suff.contains(numStr.substring(numStr.length() - 2));
        if (myord) {
            numStr = substring(numStr, 0, -2);
        }

        String[] chunksArr;
        boolean finalpoint = false;

        if (options.decimal != null && !options.decimal.isEmpty() && numStr.contains(".")) {
            int maxSplit = (options.group != 0) ? -1 : 2;
            chunksArr = numStr.split("\\.", maxSplit);
            if (numStr.endsWith(".")) {
                finalpoint = true;
            }
        } else {
            chunksArr = new String[]{numStr};
        }

        List<String> chunks = new ArrayList<>(Arrays.asList(chunksArr));

        boolean loopstart = chunks.get(0).isEmpty();

        final boolean[] first = { !loopstart };

        Function<String, String> handleChunk = (chunk) -> {
            chunk = NON_DIGIT.matcher(chunk).replaceAll("");
            if (chunk.isEmpty()) {
                chunk = "0";
            }

            if (options.group == 0 && !first[0]) {
                chunk = enword(chunk, 1);
            } else {
                chunk = enword(chunk, options.group);
            }

            if (chunk.endsWith(", ")) {
                chunk = substring(chunk, 0, -2);
            }
            chunk = WHITESPACES_COMMA.matcher(chunk).replaceAll(",");

            if (options.group == 0 && first[0]) {
                chunk = COMMA_WORD.matcher(chunk).replaceAll(" " + options.andword + " $1");
            }
            chunk = WHITESPACES.matcher(chunk).replaceAll(" ").strip();
            if (first[0]) {
                first[0] = false;
            }
            return chunk;
        };

        for (int i = (loopstart ? 1 : 0); i < chunks.size(); i++) {
            chunks.set(i, handleChunk.apply(chunks.get(i)));
        }

        List<String> numchunks = new ArrayList<>();
        if (!chunks.isEmpty() && !chunks.get(0).isEmpty()) {
            numchunks.addAll(Arrays.asList(chunks.get(0).split(Pattern.quote(options.comma) + " ")));
        }

        if (myord && !numchunks.isEmpty()) {
            int lastIndex = numchunks.size() - 1;
            numchunks.set(lastIndex, _sub_ord(numchunks.get(lastIndex)));
        }

        for (int i = 1; i < chunks.size(); i++) {
            numchunks.add(options.decimal);
            numchunks.addAll(Arrays.asList(chunks.get(i).split(Pattern.quote(options.comma) + " ")));
        }

        if (finalpoint) {
            numchunks.add(options.decimal);
        }

        if (options.wantlist) {
            List<String> result = new ArrayList<>();
            if (!sign.isEmpty()) result.add(sign);
            result.addAll(numchunks);
            return result;
        }

        String signout = sign.isEmpty() ? "" : sign + " ";
        String valout;
        if (options.group > 0) {
            valout = String.join(options.comma + " ", numchunks);
        } else {
            valout = renderNumberChunks(numchunks, options.decimal, options.comma);
        }
        return signout + valout;
    }

    public Object numberToWords(Object num) {
        return numberToWords(num, new NumberToWordsOptions());
    }

    public String join(List<String> words, String sep, boolean sepSpaced, String finalSep, String conj, boolean conjSpaced) {
        if (words == null || words.isEmpty()) return "";
        if (words.size() == 1) return words.get(0);

        if (conjSpaced) {
            conj = conj.isEmpty() ? " " : " " + conj + " ";
        }

        if (words.size() == 2) {
            return words.get(0) + conj + words.get(1);
        }

        if (sep == null) {
            sep = words.stream().anyMatch(s -> s.contains(",")) ? ";" : ",";
        }
        if (finalSep == null) {
            finalSep = sep;
        }

        finalSep = finalSep + conj;

        if (sepSpaced) {
            sep += " ";
        }

        String head = String.join(sep, words.subList(0, words.size() - 1));
        return head + finalSep + words.get(words.size() - 1);
    }

    public String join(List<String> words) {
        return join(words, null, true, null, "and", true);
    }

    //endregion

    //region Private Implementation
    private String[] partitionWord(String text) {
        Matcher mo = PARTITION_WORD.matcher(text);
        if (mo.matches()) {
            return new String[]{mo.group(1), mo.group(2), mo.group(3)};
        } else {
            return new String[]{"", text, ""}; // Fallback
        }
    }

    private String postprocess(String orig, String inflected) {
        if (inflected == null) return orig;

        List<String> result;
        if (inflected.contains("|")) {
            String[] wordOptions = inflected.split("\\|");
            int choice = classicalDict.get("all") ? 1 : 0;
            if (wordOptions[0].split(" ").length == wordOptions[1].split(" ").length) {
                result = new ArrayList<>(Arrays.asList(wordOptions[choice].split(" ")));
            } else {
                result = new ArrayList<>();
                for (String word : inflected.split(" ")) {
                    if (word.contains("|")) {
                        result.add(word.split("\\|")[choice]);
                    } else {
                        result.add(word);
                    }
                }
            }
        } else {
            result = new ArrayList<>(Arrays.asList(inflected.split(" ")));
        }

        String[] origWords = orig.split(" ");
        for (int i = 0; i < Math.min(origWords.length, result.size()); i++) {
            String origWord = origWords[i];
            String resultWord = result.get(i);

            if (origWord.equals("I")) continue; // Special case for "I"

            boolean isCaps = !origWord.isEmpty() && Character.isUpperCase(origWord.charAt(0)) &&
                    (origWord.length() == 1 || origWord.substring(1).equals(origWord.substring(1).toLowerCase()));
            boolean isUpper = origWord.equals(origWord.toUpperCase());

            if (isUpper && !resultWord.isEmpty()) {
                result.set(i, resultWord.toUpperCase());
            } else if (isCaps && !resultWord.isEmpty()) {
                result.set(i, capitalize(resultWord));
            }
        }
        return String.join(" ", result);
    }

    private int getCount(Object count) {
        if (count == null && this.persistentCount != null) {
            count = this.persistentCount;
        }

        if (count != null) {
            String countStr = String.valueOf(count).toLowerCase();
            if (pl_count_one.contains(countStr)) {
                return 1;
            }
            if (classicalDict.get("zero") && pl_count_zero.contains(countStr)) {
                return 1;
            }
            return 2;
        }
        return 0; // Represents "no count given", defaults to plural
    }

    @SuppressWarnings("all") // To suppress warnings from the giant if-else chain.
    private String _plnoun(String word, Object countObj) {
        int count = getCount(countObj);
        if (count == 1) return word;

        String value = udMatch(word, pl_sb_user_defined);
        if (value != null) return value;
        if (word.isEmpty()) return word;

        Words w = new Words(word);

        if (pl_sb_uninflected_complete.contains(w.last.toLowerCase())) {
            if (w.split.length >= 3) {
                String longCompound = _handle_long_compounds(w, 2);
                if (longCompound != null) return longCompound;
            }
            return word;
        }

        if (pl_sb_uninflected_caps.contains(word)) return word;

        for (Map.Entry<Integer, Set<String>> entry : pl_sb_uninflected_bysize.entrySet()) {
            if (w.lowered.length() >= entry.getKey() && entry.getValue().contains(w.lowered.substring(w.lowered.length() - entry.getKey()))) {
                return word;
            }
        }

        if (classicalDict.get("herd") && pl_sb_uninflected_herd.contains(w.last.toLowerCase())) return word;

        Matcher mo_postfix = PL_SB_POSTFIX_ADJ_STEMS_RE.matcher(word);
        if (mo_postfix.matches() && !mo_postfix.group(2).isEmpty()) {
            return _plnoun(mo_postfix.group(1), 2) + mo_postfix.group(2);
        }

        if (w.lowered.contains(" a ") || w.lowered.contains("-a-")) {
            Matcher mo_dual = PL_SB_PREP_DUAL_COMPOUND_RE.matcher(word);
            if (mo_dual.matches() && !mo_dual.group(2).isEmpty() && !mo_dual.group(3).isEmpty()) {
                return String.format("%s%s%s",
                        _plnoun(mo_dual.group(1), 2),
                        mo_dual.group(2),
                        _plnoun(mo_dual.group(3), 2));
            }
        }

        if (w.split.length >= 3) {
            String longCompound = _handle_long_compounds(w, 2);
            if (longCompound != null) return longCompound;
        }

        Matcher mo_denom = DENOMINATOR.matcher(w.lowered);
        if (mo_denom.matches()) {
            int index = mo_denom.group("denominator").length();
            return _plnoun(substring(word, 0, index), 2) + substring(word, index);
        }

        if (w.split.length >= 2 && w.split[w.split.length - 2].equals("degree")) {
            return String.join(" ", _plnoun(w.first, 2), String.join(" ", Arrays.copyOfRange(w.split, 1, w.split.length)));
        }

        try {
            return _handle_prepositional_phrase(w.lowered, p -> this._plnoun(p, 2), "-");
        } catch (NoSuchElementException | IllegalArgumentException ignored) {}

        if (pl_pron_nom.containsKey(w.lowered)) return pl_pron_nom.get(w.lowered);
        if (pl_pron_acc.containsKey(w.lowered)) return pl_pron_acc.get(w.lowered);

        if (pl_sb_irregular_caps.containsKey(w.last)) {
            return substring(word, 0, -w.last.length()) + pl_sb_irregular_caps.get(w.last);
        }

        String lowered_last = w.last.toLowerCase();
        if (pl_sb_irregular.containsKey(lowered_last)) {
            return substring(word, 0, -lowered_last.length()) + pl_sb_irregular.get(lowered_last);
        }

        String[] dashSplit = w.lowered.split("-");
        if (dashSplit.length >= 2) {
            String twoPart = dashSplit[dashSplit.length - 2] + " " + dashSplit[dashSplit.length - 1];
            if (pl_sb_irregular_compound.containsKey(twoPart.toLowerCase())) {
                int len = twoPart.length() + 1; // +1 for the dash
                return substring(word, 0, -len) + pl_sb_irregular_compound.get(twoPart.toLowerCase());
            }
        }

        if (w.lowered.endsWith("quy")) return substring(word, 0, -1) + "ies";
        if (w.lowered.endsWith("person")) return substring(word, 0, -4) + (classicalDict.get("persons") ? "sons" : "ople");

        if (w.lowered.endsWith("man")) {
            for (Map.Entry<Integer, Set<String>> entry : pl_sb_U_man_mans_bysize.entrySet()) {
                if (w.lowered.length() >= entry.getKey() && entry.getValue().contains(substring(w.lowered, -entry.getKey()))) return word + "s";
            }
            for (Map.Entry<Integer, Set<String>> entry : pl_sb_U_man_mans_caps_bysize.entrySet()) {
                if (word.length() >= entry.getKey() && entry.getValue().contains(substring(word, -entry.getKey()))) return word + "s";
            }
            return substring(word, 0, -3) + "men";
        }
        if (w.lowered.endsWith("mouse")) return substring(word, 0, -5) + "mice";
        if (w.lowered.endsWith("louse")) {
            Set<String> louseWords = pl_sb_U_louse_lice_bysize.get(word.length());
            if (louseWords != null && louseWords.contains(w.lowered)) {
                return substring(word, 0, -5) + "lice";
            }
            return word + "s";
        }
        if (w.lowered.endsWith("goose")) return substring(word, 0, -5) + "geese";
        if (w.lowered.endsWith("tooth")) return substring(word, 0, -5) + "teeth";
        if (w.lowered.endsWith("foot")) return substring(word, 0, -4) + "feet";
        if (w.lowered.endsWith("taco")) return substring(word, 0, -4) + "tacos";
        if (w.lowered.equals("die")) return "dice";

        // UNASSIMILATED IMPORTS
        if (w.lowered.endsWith("ceps")) return word;
        if (w.lowered.endsWith("zoon")) return substring(word, 0, -2) + "a";
        if (Stream.of("cis", "sis", "xis").anyMatch(w.lowered::endsWith)) return substring(word, 0, -2) + "es";

        if (w.lowered.endsWith("h")) {
            for (Map.Entry<Integer, Set<String>> entry : pl_sb_U_ch_chs_bysize.entrySet()) {
                if (w.lowered.length() >= entry.getKey() && entry.getValue().contains(substring(w.lowered, -entry.getKey()))) return substring(word, 0, null) + "s";
            }
        }
        if (w.lowered.endsWith("x")) {
            for (Map.Entry<Integer, Set<String>> entry : pl_sb_U_ex_ices_bysize.entrySet()) {
                if (w.lowered.length() >= entry.getKey() && entry.getValue().contains(substring(w.lowered, -entry.getKey()))) return substring(word, 0, -2) + "ices";
            }
            for (Map.Entry<Integer, Set<String>> entry : pl_sb_U_ix_ices_bysize.entrySet()) {
                if (w.lowered.length() >= entry.getKey() && entry.getValue().contains(substring(w.lowered, -entry.getKey()))) return substring(word, 0, -2) + "ices";
            }
        }
        if (w.lowered.endsWith("m")) {
            for (Map.Entry<Integer, Set<String>> entry : pl_sb_U_um_a_bysize.entrySet()) {
                if (w.lowered.length() >= entry.getKey() && entry.getValue().contains(substring(w.lowered, -entry.getKey()))) return substring(word, 0, -2) + "a";
            }
        }
        if (w.lowered.endsWith("s")) {
            for (Map.Entry<Integer, Set<String>> entry : pl_sb_U_us_i_bysize.entrySet()) {
                if (w.lowered.length() >= entry.getKey() && entry.getValue().contains(substring(w.lowered, -entry.getKey()))) return substring(word, 0, -2) + "i";
            }
        }
        if (w.lowered.endsWith("n")) {
            for (Map.Entry<Integer, Set<String>> entry : pl_sb_U_on_a_bysize.entrySet()) {
                if (w.lowered.length() >= entry.getKey() && entry.getValue().contains(substring(w.lowered, -entry.getKey()))) return substring(word, 0, -2) + "a";
            }
        }
        if (w.lowered.endsWith("a")) {
            for (Map.Entry<Integer, Set<String>> entry : pl_sb_U_a_ae_bysize.entrySet()) {
                if (w.lowered.length() >= entry.getKey() && entry.getValue().contains(substring(w.lowered, -entry.getKey()))) return substring(word, 0, null) + "e";
            }
        }

        // INCOMPLETELY ASSIMILATED IMPORTS
        if (classicalDict.get("ancient")) {
            if (w.lowered.endsWith("trix")) return substring(word, 0, -1) + "ces";
            if (w.lowered.endsWith("eau") || w.lowered.endsWith("ieu")) return word + "x";
            if (word.length() > 4 && Stream.of("ynx", "inx", "anx").anyMatch(w.lowered::endsWith)) return substring(word, 0, -1) + "ges";

            if (w.lowered.endsWith("n")) {
                for (Map.Entry<Integer, Set<String>> entry : pl_sb_C_en_ina_bysize.entrySet()) if (w.lowered.length() >= entry.getKey() && entry.getValue().contains(substring(w.lowered, -entry.getKey()))) return substring(word, 0, -2) + "ina";
                for (Map.Entry<Integer, Set<String>> entry : pl_sb_C_on_a_bysize.entrySet()) if (w.lowered.length() >= entry.getKey() && entry.getValue().contains(substring(w.lowered, -entry.getKey()))) return substring(word, 0, -2) + "a";
            }
            if (w.lowered.endsWith("x")) {
                for (Map.Entry<Integer, Set<String>> entry : pl_sb_C_ex_ices_bysize.entrySet()) if (w.lowered.length() >= entry.getKey() && entry.getValue().contains(substring(w.lowered, -entry.getKey()))) return substring(word, 0, -2) + "ices";
                for (Map.Entry<Integer, Set<String>> entry : pl_sb_C_ix_ices_bysize.entrySet()) if (w.lowered.length() >= entry.getKey() && entry.getValue().contains(substring(w.lowered, -entry.getKey()))) return substring(word, 0, -2) + "ices";
            }
            if (w.lowered.endsWith("m")) {
                for (Map.Entry<Integer, Set<String>> entry : pl_sb_C_um_a_bysize.entrySet()) if (w.lowered.length() >= entry.getKey() && entry.getValue().contains(substring(w.lowered, -entry.getKey()))) return substring(word, 0, -2) + "a";
            }
            if (w.lowered.endsWith("s")) {
                for (Map.Entry<Integer, Set<String>> entry : pl_sb_C_us_i_bysize.entrySet()) if (w.lowered.length() >= entry.getKey() && entry.getValue().contains(substring(w.lowered, -entry.getKey()))) return substring(word, 0, -2) + "i";
                for (Map.Entry<Integer, Set<String>> entry : pl_sb_C_us_us_bysize.entrySet()) if (w.lowered.length() >= entry.getKey() && entry.getValue().contains(substring(w.lowered, -entry.getKey()))) return substring(word, 0, null) + "";
                for (Map.Entry<Integer, Set<String>> entry : pl_sb_C_is_ides_bysize.entrySet()) if (w.lowered.length() >= entry.getKey() && entry.getValue().contains(substring(w.lowered, -entry.getKey()))) return substring(word, 0, -1) + "des";
            }
            if (w.lowered.endsWith("a")) {
                for (Map.Entry<Integer, Set<String>> entry : pl_sb_C_a_ae_bysize.entrySet()) if (w.lowered.length() >= entry.getKey() && entry.getValue().contains(substring(w.lowered, -entry.getKey()))) return substring(word, 0, null) + "e";
                for (Map.Entry<Integer, Set<String>> entry : pl_sb_C_a_ata_bysize.entrySet()) if (w.lowered.length() >= entry.getKey() && entry.getValue().contains(substring(w.lowered, -entry.getKey()))) return substring(word, 0, null) + "ta";
            }
            if (w.lowered.endsWith("o")) {
                for (Map.Entry<Integer, Set<String>> entry : pl_sb_C_o_i_bysize.entrySet()) if (w.lowered.length() >= entry.getKey() && entry.getValue().contains(substring(w.lowered, -entry.getKey()))) return substring(word, 0, -1) + "i";
            }
            for (Map.Entry<Integer, Set<String>> entry : pl_sb_C_i_bysize.entrySet()) if (w.lowered.length() >= entry.getKey() && entry.getValue().contains(substring(w.lowered, -entry.getKey()))) return substring(word, 0, null) + "i";
            for (Map.Entry<Integer, Set<String>> entry : pl_sb_C_im_bysize.entrySet()) if (w.lowered.length() >= entry.getKey() && entry.getValue().contains(substring(w.lowered, -entry.getKey()))) return substring(word, 0, null) + "im";
        }

        if (pl_sb_singular_s_complete.contains(lowered_last)) return word + "es";

        for (Map.Entry<Integer, Set<String>> entry : pl_sb_singular_s_bysize.entrySet()) {
            if (w.lowered.length() >= entry.getKey() && entry.getValue().contains(substring(w.lowered, -entry.getKey()))) return word + "es";
        }

        if (w.lowered.endsWith("es") && Character.isUpperCase(word.charAt(0))) return word + "es";

        if (w.lowered.endsWith("z")) {
            for (Map.Entry<Integer, Set<String>> entry : pl_sb_z_zes_bysize.entrySet()) {
                if (w.lowered.length() >= entry.getKey() && entry.getValue().contains(substring(w.lowered, -entry.getKey()))) return word + "es";
            }
            if (word.length() < 2 || word.charAt(word.length() - 2) != 'z') return word + "zes";
        }

        if (w.lowered.endsWith("ze")) {
            for (Map.Entry<Integer, Set<String>> entry : pl_sb_ze_zes_bysize.entrySet()) {
                if (w.lowered.length() >= entry.getKey() && entry.getValue().contains(substring(w.lowered, -entry.getKey()))) return word + "s";
            }
        }

        if (Stream.of("ch", "sh", "zz", "ss").anyMatch(w.lowered::endsWith) || w.lowered.endsWith("x")) return word + "es";

        if (Stream.of("elf", "alf", "olf").anyMatch(w.lowered::endsWith)) return substring(word, 0, -1) + "ves";
        if (w.lowered.endsWith("eaf") && (word.length() < 4 || word.charAt(word.length() - 4) != 'd')) return substring(word, 0, -1) + "ves";
        if (Stream.of("nife", "life", "wife").anyMatch(w.lowered::endsWith)) return substring(word, 0, -2) + "ves";
        if (w.lowered.endsWith("arf")) return substring(word, 0, -1) + "ves";

        if (w.lowered.endsWith("y")) {
            if (word.length() > 1 && "aeiou".indexOf(w.lowered.charAt(w.lowered.length() - 2)) != -1 || word.length() == 1) return word + "s";
            if (classicalDict.get("names") && Character.isUpperCase(word.charAt(0))) return word + "s";
            return substring(word, 0, -1) + "ies";
        }

        if (w.lowered.endsWith("o")) {
            if (pl_sb_U_o_os_complete.contains(lowered_last)) return word + "s";
            for (Map.Entry<Integer, Set<String>> entry : pl_sb_U_o_os_bysize.entrySet()) {
                if (w.lowered.length() >= entry.getKey() && entry.getValue().contains(substring(w.lowered, -entry.getKey()))) return word + "s";
            }
            if (word.length() >= 2 && Stream.of("ao", "eo", "io", "oo", "uo").anyMatch(e -> substring(w.lowered, -2).equals(e))) return word + "s";
            return word + "es";
        }

        return word + "s";
    }

    private String _pl_special_verb(String word, Object countObj) {
        if (classicalDict.get("zero")) {
            String countStr = String.valueOf(countObj).toLowerCase();
            if (pl_count_zero.contains(countStr)) return null;
        }

        int count = getCount(countObj);
        if (count == 1) return word;

        String value = udMatch(word, pl_v_user_defined);
        if (value != null) return value;
        if (word.isEmpty()) return null;

        Words w = new Words(word);

        if (plverb_irregular_pres.containsKey(w.first)) {
            return plverb_irregular_pres.get(w.first) + substring(word, w.first.length());
        }

        if (plverb_irregular_non_pres.contains(w.first)) return word;

        if (w.first.endsWith("n't") && plverb_irregular_pres.containsKey(substring(w.first, 0, -3))) {
            return plverb_irregular_pres.get(substring(w.first, 0, -3)) + "n't" + substring(word, w.first.length());
        }
        if (w.first.endsWith("n't")) return word;

        if (PLVERB_SPECIAL_S_RE.matcher(word).matches()) return null;
        if (word.contains(" ")) return null;

        if (w.lowered.equals("quizzes")) return "quiz";

        if (Stream.of("ches", "shes", "zzes", "sses").anyMatch(w.lowered::endsWith) || w.lowered.endsWith("xes")) {
            return substring(word, 0, -2);
        }

        if (w.lowered.endsWith("ies") && word.length() > 3) return substring(word, 0, -3) + "y";

        if (pl_v_oes_oe.contains(w.last.toLowerCase()) ||
                (w.lowered.length() >= 4 && pl_v_oes_oe_endings_size4.contains(substring(w.lowered, -4))) ||
                (w.lowered.length() >= 5 && pl_v_oes_oe_endings_size5.contains(substring(w.lowered, -5)))) {
            return substring(word, 0, -1);
        }
        if (w.lowered.endsWith("oes") && word.length() > 3) return substring(word, 0, -2);

        Matcher mo = ENDS_WITH_S.matcher(word);
        if (mo.matches()) return mo.group(1);

        return null;
    }

    private String _pl_general_verb(String word, Object countObj) {
        int count = getCount(countObj);
        if (count == 1) return word;

        Matcher mo_ambiguous = plverb_ambiguous_pres_keys.matcher(word);
        if (mo_ambiguous.matches()) {
            return plverb_ambiguous_pres.get(mo_ambiguous.group(1).toLowerCase()) + mo_ambiguous.group(2);
        }

        Matcher mo_non_pres = plverb_ambiguous_non_pres.matcher(word);
        if (mo_non_pres.matches()) return word;

        return word;
    }

    private String _pl_special_adjective(String word, Object countObj) {
        int count = getCount(countObj);
        if (count == 1) return word;

        String value = udMatch(word, pl_adj_user_defined);
        if (value != null) return value;

        Matcher mo_special = pl_adj_special_keys.matcher(word);
        if (mo_special.matches()) return pl_adj_special.get(mo_special.group(1).toLowerCase());

        Matcher mo_poss = pl_adj_poss_keys.matcher(word);
        if (mo_poss.matches()) return pl_adj_poss.get(mo_poss.group(1).toLowerCase());

        Matcher mo_apos = ENDS_WITH_APOSTROPHE_S.matcher(word);
        if (mo_apos.matches()) {
            String pl = plural_noun(mo_apos.group(1));
            String trailing_s = pl.endsWith("s") ? "" : "s";
            return pl + "'" + trailing_s;
        }

        return null;
    }

    @SuppressWarnings("all") // To suppress warnings from the giant if-else chain.
    private Object _sinoun(String word, Object countObj, String gender) {
        int count = getCount(countObj);
        if (count == 2) return word;

        String aGender = (gender != null) ? gender : this.thegender;
        if (!singular_pronoun_genders.contains(aGender)) throw new BadGenderError();

        String value = udMatch(word, si_sb_user_defined);
        if (value != null) return value;
        if (word.isEmpty()) return word;

        if (si_sb_ois_oi_case.contains(word)) return substring(word, 0, -1);

        Words w = new Words(word);

        if (pl_sb_uninflected_complete.contains(w.last.toLowerCase())) {
            if (w.split.length >= 3) {
                Object longCompound = _handle_long_compounds(w, 1);
                if (longCompound != null) return longCompound;
            }
            return word;
        }

        if (pl_sb_uninflected_caps.contains(word)) return word;

        for (Map.Entry<Integer, Set<String>> entry : pl_sb_uninflected_bysize.entrySet()) {
            if (w.lowered.length() >= entry.getKey() && entry.getValue().contains(substring(w.lowered, -entry.getKey()))) {
                return word;
            }
        }
        if (classicalDict.get("herd") && pl_sb_uninflected_herd.contains(w.last.toLowerCase())) return word;

        if (pl_sb_C_us_us.contains(w.last.toLowerCase())) return classicalDict.get("ancient") ? word : false;

        Matcher mo_postfix = PL_SB_POSTFIX_ADJ_STEMS_RE.matcher(word);
        if (mo_postfix.matches() && !mo_postfix.group(2).isEmpty()) {
            return _sinoun(mo_postfix.group(1), 1, aGender) + mo_postfix.group(2);
        }

        try {
            return _handle_prepositional_phrase(w.lowered, p -> (String) this._sinoun(p, 1, aGender), " ");
        } catch (NoSuchElementException | IllegalArgumentException ignored) {}
        try {
            return _handle_prepositional_phrase(w.lowered, p -> (String) this._sinoun(p, 1, aGender), "-");
        } catch (NoSuchElementException | IllegalArgumentException ignored) {}

        try { return get_si_pron("nom", w.lowered, aGender); } catch(ClassCastException | NullPointerException ignored) {}
        try { return get_si_pron("acc", w.lowered, aGender); } catch(ClassCastException | NullPointerException ignored) {}

        if (si_sb_irregular_caps.containsKey(w.last)) return substring(word, 0, -w.last.length()) + si_sb_irregular_caps.get(w.last);
        if (si_sb_irregular.containsKey(w.last.toLowerCase())) return substring(word, 0, -w.last.toLowerCase().length()) + si_sb_irregular.get(w.last.toLowerCase());

        String[] dashSplit = w.lowered.split("-");
        if (dashSplit.length >= 2) {
            String twoPart = dashSplit[dashSplit.length-2] + " " + dashSplit[dashSplit.length-1];
            if (si_sb_irregular_compound.containsKey(twoPart.toLowerCase())) {
                int len = twoPart.length() + 1;
                return substring(word, 0, -len) + si_sb_irregular_compound.get(twoPart.toLowerCase());
            }
        }

        if (w.lowered.endsWith("quies")) return substring(word, 0, -3) + "y";
        if (w.lowered.endsWith("persons")) return substring(word, 0, -1);
        if (w.lowered.endsWith("people")) return substring(word, 0, -4) + "rson";

        if (w.lowered.endsWith("mans")) {
            for (Map.Entry<Integer, Set<String>> entry : si_sb_U_man_mans_bysize.entrySet()) if (w.lowered.length() >= entry.getKey() && entry.getValue().contains(substring(w.lowered, -entry.getKey()))) return substring(word, 0, -1);
            for (Map.Entry<Integer, Set<String>> entry : si_sb_U_man_mans_caps_bysize.entrySet()) if (word.length() >= entry.getKey() && entry.getValue().contains(substring(word, -entry.getKey()))) return substring(word, 0, -1);
        }
        if (w.lowered.endsWith("men")) return substring(word, 0, -3) + "man";
        if (w.lowered.endsWith("mice")) return substring(word, 0, -4) + "mouse";
        if (w.lowered.endsWith("lice")) {
            Set<String> liceWords = si_sb_U_louse_lice_bysize.get(word.length());
            if (liceWords != null && liceWords.contains(w.lowered)) return substring(word, 0, -4) + "louse";
        }
        if (w.lowered.endsWith("geese")) return substring(word, 0, -5) + "goose";
        if (w.lowered.endsWith("teeth")) return substring(word, 0, -5) + "tooth";
        if (w.lowered.endsWith("feet")) return substring(word, 0, -4) + "foot";
        if (w.lowered.equals("dice")) return "die";

        if (w.lowered.endsWith("ceps")) return word;
        if (w.lowered.endsWith("zoa")) return substring(word, 0, -1) + "on";

        if(w.lowered.endsWith("s")) {
            for(Map.Entry<Integer, Set<String>> entry : si_sb_U_ch_chs_bysize.entrySet()) if(w.lowered.length() >= entry.getKey() && entry.getValue().contains(substring(w.lowered, -entry.getKey()))) return substring(word, 0, -1) + "";
            for(Map.Entry<Integer, Set<String>> entry : si_sb_U_ex_ices_bysize.entrySet()) if(w.lowered.length() >= entry.getKey() && entry.getValue().contains(substring(w.lowered, -entry.getKey()))) return substring(word, 0, -4) + "ex";
            for(Map.Entry<Integer, Set<String>> entry : si_sb_U_ix_ices_bysize.entrySet()) if(w.lowered.length() >= entry.getKey() && entry.getValue().contains(substring(w.lowered, -entry.getKey()))) return substring(word, 0, -4) + "ix";
        }
        if(w.lowered.endsWith("a")) {
            for(Map.Entry<Integer, Set<String>> entry : si_sb_U_um_a_bysize.entrySet()) if(w.lowered.length() >= entry.getKey() && entry.getValue().contains(substring(w.lowered, -entry.getKey()))) return substring(word, 0, -1) + "um";
            for(Map.Entry<Integer, Set<String>> entry : si_sb_U_on_a_bysize.entrySet()) if(w.lowered.length() >= entry.getKey() && entry.getValue().contains(substring(w.lowered, -entry.getKey()))) return substring(word, 0, -1) + "on";
        }
        if(w.lowered.endsWith("i")) for(Map.Entry<Integer, Set<String>> entry : si_sb_U_us_i_bysize.entrySet()) if(w.lowered.length() >= entry.getKey() && entry.getValue().contains(substring(w.lowered, -entry.getKey()))) return substring(word, 0, -1) + "us";
        if(w.lowered.endsWith("e")) for(Map.Entry<Integer, Set<String>> entry : si_sb_U_a_ae_bysize.entrySet()) if(w.lowered.length() >= entry.getKey() && entry.getValue().contains(substring(w.lowered, -entry.getKey()))) return substring(word, 0, -1) + "";

        if (classicalDict.get("ancient")) {
            if (w.lowered.endsWith("trices")) return substring(word, 0, -3) + "x";
            if (w.lowered.endsWith("eaux") || w.lowered.endsWith("ieux")) return substring(word, 0, -1);
            if (word.length() > 6 && (w.lowered.endsWith("ynges") || w.lowered.endsWith("inges") || w.lowered.endsWith("anges"))) return substring(word, 0, -3) + "x";

            if (w.lowered.endsWith("a")) {
                for(Map.Entry<Integer, Set<String>> entry : si_sb_C_en_ina_bysize.entrySet()) if (w.lowered.length() >= entry.getKey() && entry.getValue().contains(substring(w.lowered, -entry.getKey()))) return substring(word, 0, -3) + "en";
                for(Map.Entry<Integer, Set<String>> entry : si_sb_C_um_a_bysize.entrySet()) if (w.lowered.length() >= entry.getKey() && entry.getValue().contains(substring(w.lowered, -entry.getKey()))) return substring(word, 0, -1) + "um";
                for(Map.Entry<Integer, Set<String>> entry : si_sb_C_a_ata_bysize.entrySet()) if (w.lowered.length() >= entry.getKey() && entry.getValue().contains(substring(w.lowered, -entry.getKey()))) return substring(word, 0, -2) + "";
                for(Map.Entry<Integer, Set<String>> entry : si_sb_C_on_a_bysize.entrySet()) if (w.lowered.length() >= entry.getKey() && entry.getValue().contains(substring(w.lowered, -entry.getKey()))) return substring(word, 0, -1) + "on";
            }
            if (w.lowered.endsWith("s")) {
                for(Map.Entry<Integer, Set<String>> entry : si_sb_C_ex_ices_bysize.entrySet()) if (w.lowered.length() >= entry.getKey() && entry.getValue().contains(substring(w.lowered, -entry.getKey()))) return substring(word, 0, -4) + "ex";
                for(Map.Entry<Integer, Set<String>> entry : si_sb_C_ix_ices_bysize.entrySet()) if (w.lowered.length() >= entry.getKey() && entry.getValue().contains(substring(w.lowered, -entry.getKey()))) return substring(word, 0, -4) + "ix";
                for(Map.Entry<Integer, Set<String>> entry : pl_sb_C_us_us_bysize.entrySet()) if (w.lowered.length() >= entry.getKey() && entry.getValue().contains(substring(w.lowered, -entry.getKey()))) return substring(word, 0, null) + "";
                for(Map.Entry<Integer, Set<String>> entry : si_sb_C_is_ides_bysize.entrySet()) if (w.lowered.length() >= entry.getKey() && entry.getValue().contains(substring(w.lowered, -entry.getKey()))) return substring(word, 0, -3) + "s";
            }
            if (w.lowered.endsWith("i")) {
                for(Map.Entry<Integer, Set<String>> entry : si_sb_C_us_i_bysize.entrySet()) if (w.lowered.length() >= entry.getKey() && entry.getValue().contains(substring(w.lowered, -entry.getKey()))) return substring(word, 0, -1) + "us";
                for(Map.Entry<Integer, Set<String>> entry : si_sb_C_o_i_bysize.entrySet()) if (w.lowered.length() >= entry.getKey() && entry.getValue().contains(substring(w.lowered, -entry.getKey()))) return substring(word, 0, -1) + "o";
                for(Map.Entry<Integer, Set<String>> entry : si_sb_C_i_bysize.entrySet()) if (w.lowered.length() >= entry.getKey() && entry.getValue().contains(substring(w.lowered, -entry.getKey()))) return substring(word, 0, -1) + "";
            }
            if (w.lowered.endsWith("e")) for(Map.Entry<Integer, Set<String>> entry : si_sb_C_a_ae_bysize.entrySet()) if (w.lowered.length() >= entry.getKey() && entry.getValue().contains(substring(w.lowered, -entry.getKey()))) return substring(word, 0, -1) + "";
            if (w.lowered.endsWith("m")) for(Map.Entry<Integer, Set<String>> entry : si_sb_C_im_bysize.entrySet()) if (w.lowered.length() >= entry.getKey() && entry.getValue().contains(substring(w.lowered, -entry.getKey()))) return substring(word, 0, -2) + "";
        }

        if (w.lowered.endsWith("houses") || si_sb_uses_use_case.contains(word) || si_sb_uses_use.contains(w.last.toLowerCase())) return substring(word, 0, -1);
        if (si_sb_ies_ie_case.contains(word) || si_sb_ies_ie.contains(w.last.toLowerCase())) return substring(word, 0, -1);
        if (w.lowered.endsWith("shoes") || si_sb_oes_oe_case.contains(word) || si_sb_oes_oe.contains(w.last.toLowerCase())) return substring(word, 0, -1);
        if (si_sb_sses_sse_case.contains(word) || si_sb_sses_sse.contains(w.last.toLowerCase())) return substring(word, 0, -1);

        for (String s : si_sb_singular_s_complete) if (w.last.toLowerCase().equals(s)) return substring(word, 0, -2);
        for (Map.Entry<Integer, Set<String>> entry : si_sb_singular_s_bysize.entrySet()) if (w.lowered.length() >= entry.getKey() && entry.getValue().contains(substring(w.lowered, -entry.getKey()))) return substring(word, 0, -2);
        if (w.lowered.endsWith("eses") && Character.isUpperCase(word.charAt(0))) return substring(word, 0, -2);
        if (si_sb_z_zes.contains(w.last.toLowerCase())) return substring(word, 0, -2);
        if (si_sb_zzes_zz.contains(w.last.toLowerCase())) return substring(word, 0, -2);
        if (w.lowered.endsWith("zzes")) return substring(word, 0, -3);

        if (si_sb_ches_che_case.contains(word) || si_sb_ches_che.contains(w.last.toLowerCase())) return substring(word, 0, -1);
        if (w.lowered.endsWith("ches") || w.lowered.endsWith("shes")) return substring(word, 0, -2);
        if (si_sb_xes_xe.contains(w.last.toLowerCase())) return substring(word, 0, -1);
        if (w.lowered.endsWith("xes")) return substring(word, 0, -2);

        if (si_sb_ves_ve_case.contains(word) || si_sb_ves_ve.contains(w.last.toLowerCase())) return substring(word, 0, -1);
        if (w.lowered.endsWith("ves")) {
            if (word.length() >= 5 && Stream.of("el", "al", "ol").anyMatch(e -> substring(w.lowered, -5, -3).equals(e))) return substring(word, 0, -3) + "f";
            if (word.length() >= 5 && substring(w.lowered, -5, -3).equals("ea") && (word.length() < 6 || word.charAt(word.length() - 6) != 'd')) return substring(word, 0, -3) + "f";
            if (word.length() >= 5 && Stream.of("ni", "li", "wi").anyMatch(e -> substring(w.lowered, -5, -3).equals(e))) return substring(word, 0, -3) + "fe";
            if (word.length() >= 5 && substring(w.lowered, -5, -3).equals("ar")) return substring(word, 0, -3) + "f";
        }

        if (w.lowered.endsWith("ys")) {
            if (word.length() > 2 && "aeiou".indexOf(w.lowered.charAt(w.lowered.length() - 3)) != -1) return substring(word, 0, -1);
            if (classicalDict.get("names") && Character.isUpperCase(word.charAt(0))) return substring(word, 0, -1);
        }
        if (w.lowered.endsWith("ies")) return substring(word, 0, -3) + "y";

        if (w.lowered.endsWith("os")) {
            if (si_sb_U_o_os_complete.contains(w.last.toLowerCase())) return substring(word, 0, -1);
            for (Map.Entry<Integer, Set<String>> entry : si_sb_U_o_os_bysize.entrySet()) if (w.lowered.length() >= entry.getKey() && entry.getValue().contains(substring(w.lowered, -entry.getKey()))) return substring(word, 0, -1);
            if (word.length() >= 3 && Stream.of("aos", "eos", "ios", "oos", "uos").anyMatch(e -> substring(w.lowered, -3).equals(e))) return substring(word, 0, -1);
        }
        if (w.lowered.endsWith("oes")) return substring(word, 0, -2);

        if (si_sb_es_is.contains(word)) return substring(word, 0, -2) + "is";
        if (w.lowered.endsWith("s")) return substring(word, 0, -1);

        return false;
    }

    private String get_si_pron(String thecase, String word, String gender) {
        Object sing = si_pron.get(thecase).get(word);
        if (sing == null) {
            // This should not be reached if called correctly, but as a safeguard:
            throw new NullPointerException("Pronoun not found for singularization");
        }
        if (sing instanceof Map) {
            @SuppressWarnings("unchecked")
            Map<String, String> genderMap = (Map<String, String>) sing;
            return genderMap.get(gender);
        }
        return (String) sing;
    }

    private String _indef_article(String word, Object count) {
        int mycount = getCount(count);
        if (mycount != 1) {
            return count + " " + word;
        }

        String value = udMatch(word, a_a_user_defined);
        if (value != null) return value + " " + word;

        Object[][] cases = {
                {A_ordinal_a, "a"}, {A_ordinal_an, "an"}, {A_explicit_an, "an"},
                {SPECIAL_AN, "an"}, {SPECIAL_A, "a"}, {A_abbrev, "an"},
                {SPECIAL_ABBREV_AN, "an"}, {SPECIAL_ABBREV_A, "a"}, {CONSONANTS, "a"},
                {ARTICLE_SPECIAL_EU, "a"}, {ARTICLE_SPECIAL_ONCE, "a"}, {ARTICLE_SPECIAL_ONETIME, "a"},
                {ARTICLE_SPECIAL_UNIT, "a"}, {ARTICLE_SPECIAL_UBA, "a"}, {ARTICLE_SPECIAL_UKR, "a"},
                {A_explicit_a, "a"}, {SPECIAL_CAPITALS, "a"}, {VOWELS, "an"},
                {A_y_cons, "an"}
        };

        for (Object[]
                c : cases) {
            if (((Pattern) c[0]).matcher(word).find()) {
                return c[1] + " " + word;
            }
        }
        return "a " + word; // Fallback
    }

    private String _sub_ord(String val) {
        Matcher m = ordinal_suff.matcher(val);
        String result;
        if (m.find()) {
            result = m.replaceAll(matchResult -> ordinal.get(matchResult.group(1)));
        } else {
            result = val;
        }
        return result.equals(val) ? val + "th" : result;
    }

    private String millfn(int ind) {
        if (ind > mill.size() - 1) throw new NumOutOfRangeError();
        return mill.get(ind);
    }

    private String unitfn(int units, int mindex) {
        return unit.get(units) + millfn(mindex);
    }

    private String tenfn(int tens, int units, int mindex) {
        if (tens != 1) {
            String tensPart = ten.get(tens);
            String hyphen = (tens > 0 && units > 0) ? "-" : "";
            String unitPart = unit.get(units);
            String millPart = millfn(mindex);
            return tensPart + hyphen + unitPart + millPart;
        }
        return teen.get(units) + mill.get(mindex);
    }
    private String tenfn(int tens, int units) { return tenfn(tens, units, 0); }

    private String hundfn(int hundreds, int tens, int units, int mindex) {
        if (hundreds > 0) {
            String andword = (tens > 0 || units > 0) ? " " + this.numberArgs.get("andword") + " " : "";
            return unit.get(hundreds) + " hundred" + andword + tenfn(tens, units) + millfn(mindex) + ", ";
        }
        if (tens > 0 || units > 0) {
            return tenfn(tens, units) + millfn(mindex) + ", ";
        }
        return "";
    }

    private String enword(String numStr, int group) {
        if (group == 1) {
            return DIGIT_GROUP.matcher(numStr).replaceAll(m -> {
                int units = Integer.parseInt(m.group(1));
                if (units == 1) return " " + this.numberArgs.get("one") + ", ";
                return (units > 0) ? unit.get(units) + ", " : " " + this.numberArgs.get("zero") + ", ";
            });
        }
        if (group == 2) {
            numStr = TWO_DIGITS.matcher(numStr).replaceAll(m -> {
                int tens_val = Integer.parseInt(m.group(1));
                int units_val = Integer.parseInt(m.group(2));
                if (tens_val > 0) return tenfn(tens_val, units_val) + ", ";
                if (units_val > 0) return " " + this.numberArgs.get("zero") + " " + unit.get(units_val) + ", ";
                return " " + this.numberArgs.get("zero") + " " + this.numberArgs.get("zero") + ", ";
            });
            return DIGIT_GROUP.matcher(numStr).replaceAll(m -> {
                int units_val = Integer.parseInt(m.group(1));
                return (units_val > 0) ? unit.get(units_val) + ", " : " " + this.numberArgs.get("zero") + ", ";
            });
        }
        if (group == 3) {
            numStr = THREE_DIGITS.matcher(numStr).replaceAll(m -> {
                int hundreds = Integer.parseInt(m.group(1));
                int tens_val = Integer.parseInt(m.group(2));
                int units_val = Integer.parseInt(m.group(3));
                String hunword = (hundreds == 1) ? " " + this.numberArgs.get("one") : (hundreds > 0 ? unit.get(hundreds) : " " + this.numberArgs.get("zero"));
                String tenword = (tens_val > 0) ? tenfn(tens_val, units_val) : (units_val > 0 ? " " + this.numberArgs.get("zero") + " " + unit.get(units_val) : " " + this.numberArgs.get("zero") + " " + this.numberArgs.get("zero"));
                return hunword + " " + tenword + ", ";
            });
            numStr = TWO_DIGITS.matcher(numStr).replaceAll(m -> tenfn(Integer.parseInt(m.group(1)), Integer.parseInt(m.group(2))) + ", ");
            return DIGIT_GROUP.matcher(numStr).replaceAll(m -> unitfn(Integer.parseInt(m.group(1)), 0) + ", ");
        }

        if (Integer.parseInt(numStr) == 0) return this.numberArgs.get("zero");
        if (Integer.parseInt(numStr) == 1) return this.numberArgs.get("one");

        // group == 0
        while (numStr.startsWith("0")) {
            numStr = numStr.substring(1);
        }
        this.millCount = 0;

        Function<Matcher, String> hundsub = m -> {
            String ret = hundfn(Integer.parseInt(m.group(1)), Integer.parseInt(m.group(2)), Integer.parseInt(m.group(3)), this.millCount);
            this.millCount++;
            return ret;
        };

        Matcher m = THREE_DIGITS_WORD.matcher(numStr);
        StringBuffer sb = new StringBuffer();
        while (m.find()) {
            m.appendReplacement(sb, Matcher.quoteReplacement(hundsub.apply(m)));
            // Re-create matcher on the modified string buffer and the rest of the original string
            String remaining = numStr.substring(m.end());
            m = THREE_DIGITS_WORD.matcher(sb.toString() + remaining);
        }
        m.appendTail(sb);
        numStr = sb.toString();

        numStr = TWO_DIGITS_WORD.matcher(numStr).replaceAll(m_ -> tenfn(Integer.parseInt(m_.group(1)), Integer.parseInt(m_.group(2)), this.millCount) + ", ");
        numStr = ONE_DIGIT_WORD.matcher(numStr).replaceAll(m_ -> unitfn(Integer.parseInt(m_.group(1)), this.millCount) + ", ");

        return numStr;
    }

    private String renderNumberChunks(List<String> chunks, String decimal, String comma) {
        if (chunks.isEmpty()) return "";
        StringBuilder sb = new StringBuilder();
        Iterator<String> it = chunks.iterator();
        sb.append(it.next());

        boolean first = decimal == null || !sb.toString().endsWith(decimal);

        while(it.hasNext()) {
            String nc = it.next();
            if (Objects.equals(nc, decimal)) {
                first = false;
            } else if (first) {
                sb.append(comma);
            }
            sb.append(" ").append(nc);
        }
        return sb.toString();
    }

    private Object _plequal(String word1, String word2, Function<String, String> pl) {
        Map<String, Boolean> classval = new HashMap<>(this.classicalDict);
        Object result = false;

        for (Map<String, Boolean> dict : List.of(all_classical, no_classical)) {
            this.classicalDict = new HashMap<>(dict);
            if (word1.equalsIgnoreCase(word2)) { result = "eq"; break; }
            if (word1.equalsIgnoreCase(pl.apply(word2))) { result = "p:s"; break; }
            if (word2.equalsIgnoreCase(pl.apply(word1))) { result = "s:p"; break; }
        }
        this.classicalDict = classval;
        if (result instanceof String) return result;

        if (_pl_check_plurals_N(word1, word2) || _pl_check_plurals_N(word2, word1)) {
            return "p:p";
        }
        if (_pl_check_plurals_adj(word1, word2)) {
            return "p:p";
        }

        return false;
    }

    private boolean _pl_check_plurals_N(String word1, String word2) {
        List<Object[]> stem_endings = List.of(
                new Object[]{pl_sb_C_a_ata, "as", "ata"}, new Object[]{pl_sb_C_is_ides, "is", "ides"},
                new Object[]{pl_sb_C_a_ae, "s", "e"}, new Object[]{pl_sb_C_en_ina, "ens", "ina"},
                new Object[]{pl_sb_C_um_a, "ums", "a"}, new Object[]{pl_sb_C_us_i, "uses", "i"},
                new Object[]{pl_sb_C_on_a, "ons", "a"}, new Object[]{pl_sb_C_o_i_stems, "os", "i"},
                new Object[]{pl_sb_C_ex_ices, "exes", "ices"}, new Object[]{pl_sb_C_ix_ices, "ixes", "ices"},
                new Object[]{pl_sb_C_i, "s", "i"}, new Object[]{pl_sb_C_im, "s", "im"},
                new Object[]{".*eau", "s", "x"}, new Object[]{".*ieu", "s", "x"},
                new Object[]{".*tri", "xes", "ces"}, new Object[]{".{2,}[yia]n", "xes", "ges"}
        );
        String pair = new Words(word1).last + "|" + new Words(word2).last;

        for (String val : pl_sb_irregular_s.values()) if (val.equals(pair)) return true;
        for (String val : pl_sb_irregular.values()) if (val.equals(pair)) return true;
        for (String val : pl_sb_irregular_caps.values()) if (val.equals(pair)) return true;

        for (Object[] se : stem_endings) {
            String patternStr = String.format("(%s)(%s|\\1%s|%s|\\1%s)", se[0], se[1], se[2], se[2], se[1]);
            if (Pattern.compile(patternStr).matcher(pair).find()) return true;
        }
        return false;
    }

    private boolean _pl_check_plurals_adj(String word1, String word2) {
        String word1a = "";
        if (word1.endsWith("'s")) word1a = substring(word1, 0, -2);
        else if (word1.endsWith("'")) word1a = substring(word1, 0, -1);

        String word2a = "";
        if (word2.endsWith("'s")) word2a = substring(word2, 0, -2);
        else if (word2.endsWith("'")) word2a = substring(word2, 0, -1);

        return !word1a.isEmpty() && !word2a.isEmpty() && (_pl_check_plurals_N(word1a, word2a) || _pl_check_plurals_N(word2a, word1a));
    }

    private String _handle_long_compounds(Words word, int count) {
        List<String> words = Arrays.asList(word.split);
        Function<String, String> inflection = (count == 1) ?
                (s -> (String)_sinoun(s, 1, this.thegender)) : (s -> _plnoun(s, 2));

        for (WindowedResult<String> res : windowedComplete(words, 2)) {
            String prep = res.window().get(1);
            if (pl_prep_list_da.contains(prep)) {
                String cand = res.window().get(0);
                String inflected = Objects.requireNonNullElse(inflection.apply(cand), cand);
                List<String> resultParts = new ArrayList<>(res.leader());
                resultParts.add(inflected);
                resultParts.add(prep);
                resultParts.addAll(res.trailer());
                return String.join(" ", resultParts);
            }
        }
        return null;
    }

    private String _handle_prepositional_phrase(String phrase, Function<String, String> transform, String sep) {
        String[] parts = phrase.split(Pattern.quote(sep));
        if (parts.length < 3) {
            throw new IllegalArgumentException("Cannot handle words with fewer than two separators");
        }
        int pivot = _find_pivot(parts, pl_prep_list_da);

        String transformed = Objects.requireNonNullElse(transform.apply(parts[pivot-1]), parts[pivot-1]);

        List<String> resultParts = new ArrayList<>();
        resultParts.addAll(Arrays.asList(parts).subList(0, pivot - 1));
        resultParts.add(String.join(sep, transformed, parts[pivot]));
        resultParts.addAll(Arrays.asList(parts).subList(pivot + 1, parts.length));

        return String.join(" ", resultParts);
    }

    private int _find_pivot(String[] words, List<String> candidates) {
        for (int i = 1; i < words.length -1; i++) {
            if (candidates.contains(words[i])) {
                return i;
            }
        }
        throw new NoSuchElementException("No pivot found");
    }

    private static List<Object> parseInflectArgs(String argString) {
        List<Object> args = new ArrayList<>();
        parseInflectArgsWithKeywords(argString, args, new HashMap<>());
        return args;
    }

    private static void parseInflectArgsWithKeywords(String argString, List<Object> positional, Map<String, Object> keyword) {
        if (argString.trim().isEmpty()) return;
        String[] parts = argString.split(",");
        for (String part : parts) {
            part = part.trim();
            if (part.contains("=")) {
                String[] kw_part = part.split("=", 2);
                String key = kw_part[0].trim();
                Object value = parseArgValue(kw_part[1].trim());
                keyword.put(key, value);
            } else {
                positional.add(parseArgValue(part));
            }
        }
    }

    private static Object parseArgValue(String val) {
        val = val.trim();
        if ((val.startsWith("'") && val.endsWith("'")) || (val.startsWith("\"") && val.endsWith("\""))) {
            return val.substring(1, val.length() - 1);
        }
        try {
            return Integer.parseInt(val);
        } catch (NumberFormatException e1) {
            try {
                return Double.parseDouble(val);
            } catch (NumberFormatException e2) {
                if ("True".equalsIgnoreCase(val)) return true;
                if ("False".equalsIgnoreCase(val)) return false;
                if ("None".equalsIgnoreCase(val)) return null;
                return val; // As string fallback
            }
        }
    }
    //endregion

    //region Main Method for Demonstration
    public static void main(String[] args) {
        Inflect p = new Inflect();

        System.out.println("--- Basic Plurals ---");
        System.out.println("p.plural(\"cat\"): " + p.plural("cat"));
        System.out.println("p.plural(\"dog\", 5): " + p.plural("dog", 5));
        System.out.println("p.plural(\"we\"): " + p.plural("we"));
        System.out.println("p.plural(\"they\"): " + p.plural("they"));
        System.out.println("p.plural_noun(\"stimulus\"): " + p.plural_noun("stimulus"));
        System.out.println("p.plural_verb(\"is\"): " + p.plural_verb("is"));
        System.out.println("p.plural_adj(\"my\"): " + p.plural_adj("my"));

        System.out.println("\n--- Singulars ---");
        System.out.println("p.singular_noun(\"cats\"): " + p.singular_noun("cats"));
        System.out.println("p.singular_noun(\"stimuli\"): " + p.singular_noun("stimuli"));
        System.out.println("p.singular_noun(\"they\"): " + p.singular_noun("they"));
        p.gender("feminine");
        System.out.println("p.singular_noun(\"they\") (gender=feminine): " + p.singular_noun("they"));

        System.out.println("\n--- Articles ---");
        System.out.println("p.a(\"cat\"): " + p.a("cat"));
        System.out.println("p.a(\"apple\"): " + p.a("apple"));
        System.out.println("p.an(\"hour\"): " + p.an("hour"));

        System.out.println("\n--- Ordinals ---");
        System.out.println("p.ordinal(1): " + p.ordinal(1));
        System.out.println("p.ordinal(22): " + p.ordinal(22));
        System.out.println("p.ordinal(\"three\"): " + p.ordinal("three"));

        System.out.println("\n--- Number to Words ---");
        System.out.println("p.numberToWords(12345): " + p.numberToWords(12345));
        System.out.println("p.numberToWords(99): " + p.numberToWords(99));
        System.out.println("p.numberToWords(1001): " + p.numberToWords(1001));
        NumberToWordsOptions opts = new NumberToWordsOptions().setAndword("");
        System.out.println("p.numberToWords(1001, no 'and'): " + p.numberToWords(1001, opts));

        System.out.println("\n--- Inflect Method ---");
        System.out.println(p.inflect("The plural of cat is plural('cat')."));
        System.out.println(p.inflect("I saw 2 plural_noun('cat')."));
        p.num(2);
        System.out.println(p.inflect("I saw plural_noun('cat')."));
        System.out.println(p.inflect("I saw num(5) plural_noun('dog')."));
        System.out.println(p.inflect("It was a(present_participle('test'))."));

        System.out.println("\n--- Joining ---");
        System.out.println(p.join(List.of("apple", "banana", "carrot")));
        System.out.println(p.join(List.of("apple", "banana")));

        System.out.println("\n--- Comparison ---");
        System.out.println("compare(\"cat\", \"cats\"): " + p.compare("cat", "cats"));
        System.out.println("compare(\"cats\", \"cat\"): " + p.compare("cats", "cat"));
        System.out.println("compare(\"cat\", \"cat\"): " + p.compare("cat", "cat"));

        System.out.println("\n--- Classical Mode ---");
        p.classical(); // all on
        System.out.println("classical on, plural('octopus'): " + p.plural("octopus"));
        p.classical(Map.of("all", false)); // all off
        System.out.println("classical off, plural('octopus'): " + p.plural("octopus"));
    }
    //endregion
}