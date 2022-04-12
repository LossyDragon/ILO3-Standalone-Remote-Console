package com.hp.ilo2.remcons;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Locale;

@SuppressWarnings("SameParameterValue")
public class LocaleTranslator {

    Hashtable<Character, String> selected;
    Hashtable<String, Hashtable<Character, String>> locales = new Hashtable<>();
    Hashtable<String, String> aliases = new Hashtable<>();
    Hashtable<String, String> reverse_alias = new Hashtable<>();
    String belgian = "    !8 \"3 #[+3 $] %\" &1 '4 (5 )- *} +? ,m -= .< /> 0) 1! 2@ 3# 4$ 5% 6^ 7& 8* 9( :. ;, <ð =/ >ñ ?M @[+2 AQ M: QA WZ ZW [[+[ \\[+ð ][+] ^[  _+ `[+\\  aq m; qa wz zw {[+9 |[+1 }[+0 ~[+/  £| §6 ¨{  °_ ²` ³~ ´[+'  µ\\ À[+\\Q Á[+'Q Â[Q Ã[+/Q Ä{Q È[+\\E É[+'E Ê[E Ë{E Ì[+\\I Í[+'I Î[I Ï{I Ñ[+/N Ò[+\\O Ó[+'O Ô[O Õ[+/O Ö{O Ù[+\\U Ú[+'U Û[U Ü{U Ý[+'Y à[+\\q á[+'q â[q ã[+/q ä{q ç9 è[+\\e é[+'e ê[e ë{e ì[+\\i í[+'i î[i ï{i ñ[+/n ò[+\\o ó[+'o ô[o õ[+/o ö{o ù[+\\u ú[+'u û[u ü{u ý[+'y ÿ{y";
    String british = "\"@ #\\ @\" \\ð |ñ ~| £# ¦[+` ¬~ Á[+A á[+a É[+E é[+e Í[+I í[+i Ó[+O ó[+o Ú[+U ú[+u";
    String danish = "\"@ $[+4 &^ '\\ (* )( *| +- -/ /& :> ;< <ð =) >ñ ?_ @[+2 [[+8 \\[+ð ][+9 ^}  _? `+  {[+7 |[+= }[+0 ~[+]  £[+3 ¤$ §~ ¨]  ´=  ½` À+A Á=A Â}A Ã[+]A Ä]A Å{ Æ: È+E É=E Ê}E Ë]E Ì+I Í=I Î}I Ï]I Ñ[+]N Ò+O Ó=O Ô}O Õ[+]O Ö]O Ø\" Ù+U Ú=U Û}U Ü]U Ý=Y à+a á=a â}a ã[+]a ä]a å[ æ; è+e é=e ê}e ë]e ì+i í=i î}i ï]i ñ[+]n ò+o ó=o ô}o õ[+]o ö]o ø' ù+u ú=u û}u ü]u ý=y ÿ]y";
    String euro1 = " €[+4";
    String euro2 = " €[+e";
    String finnish = "\"@ $[+4 &^ '\\ (* )( *| +- -/ /& :> ;< <ð =) >ñ ?_ @[+2 [[+8 \\[+- ][+9 ^}  _? `+  {[+7 |[+ð }[+0 ~[+]  £[+3 ¤$ §` ¨]  ´=  ½~ À+A Á=A Â}A Ã[+]A Ä]A Å{ È+E É=E Ê}E Ë]E Ì+I Í=I Î}I Ï]I Ñ[+]N Ò+O Ó=O Ô}O Õ[+]O Ö]O Ù+U Ú=U Û}U Ü]U Ý=Y à+a á=a â}a ã[+]a ä]a å[ è+e é=e ê}e ë]e ì+i í=i î}i ï]i ñ[+]n ò+o ó=o ô}o õ[+]o ö]o ù+u ú=u û}u ü]u ý=y ÿ]y";
    String french = "    !/ \"3 #[+3 $] %\" &1 '4 (5 )- *\\ ,m -6 .< /> 0) 1! 2@ 3# 4$ 5% 6^ 7& 8* 9( :. ;, <ð >ñ ?M @[+0 AQ M: QA WZ ZW [[+5 \\[+8 ][+- ^[+9 _8 `[+7 aq m; qa wz zw {[+4 |[+6 }[+= ~[+2 £} ¤[+] §? ¨{  °_ ²` µ| Â[Q Ä{Q Ê[E Ë{E Î[I Ï{I Ô[O Ö{O Û[U Ü{U à0 â[q ä{q ç9 è7 é2 ê[e ë{e î[i ï{i ô[o ö{o ù' û[u ü{u ÿ{y";
    String french_canadian = "\"@ #` '< /# <\\ >| ?^ @[+2 [[+[ \\[+` ][+] ^[  `'  {[+' |~ }[+\\ ~[+; ¢[+4 £[+3 ¤[+5 ¦[+7 §[+o ¨}  «ð ¬[+6 ­[+. ¯[+, °[+ð ±[+1 ²[+8 ³[+9 ´[+/  µ[+m ¶[+p ¸]  »ñ ¼[+0 ½[+- ¾[+= À'A Á[+/A Â[A Ä}A Ç]C È'E É? Ê[E Ë}E Ì'I Í[+/I Î[I Ï}I Ò'O Ó[+/O Ô[O Ö}O Ù'U Ú[+/U Û[U Ü}U Ý[+/Y à'a á[+/a â[a ä}a ç]c è'e é[+/e ê[e ë}e ì'i í[+/i î[i ï}i ò'o ó[+/o ô[o ö}o ù'u ú[+/u û[u ü}u ý[+/y ÿ}y";
    String german = "  \"@ #\\ &^ '| (* )( *} +] -/ /& :> ;< <ð =) >ñ ?_ @[+q YZ ZY [[+8 \\[+- ][+9 ^`  _? `+  yz zy {[+7 |[+ð }[+0 ~[+] §# °~ ²[+2 ³[+3 ´=  µ[+m À+A Á=A Â`A Ä\" È+E É=E Ê`E Ì+I Í=I Î`I Ò+O Ó=O Ô`O Ö: Ù+U Ú=U Û`U Ü{ Ý=Z ß- à+a á=a â`a ä' è+e é=e ê`e ì+i í=i î`i ò+o ó=o ô`o ö; ù+u ú=u û`u ü[ ý=z";
    String italian = "\"@ #[+' &^ '- (* )( *} +] -/ /& :> ;< <ð =) >ñ ?_ @[+; [[+[ \\` ][+] ^+ _? |~ £# §| °\" à' ç: è[ é{ ì= ò; ù\\";
    String japanese = "\"@ &^ '& (* )( *\" +: :' =_ @[ [] \\ò ]\\ ^= _ó `{ {} ¥ô |õ }| ~+";
    String latin_american = "\"@ &^ '- (* )( *} +] -/ /& :> ;< <ð =) >ñ ?_ @[+q [\" \\[+- ]| ^[+'  _? `[+\\  {' |` }\\ ~[+] ¡+ ¨{  ¬[+` °~ ´[  ¿= À[+\\A Á[A Â[+'A Ä{A È[+\\E É[E Ê[+'E Ë{E Ì[+\\I Í[I Î[+'I Ï{I Ñ: Ò[+\\O Ó[O Ô[+'O Ö{O Ù[+\\U Ú[U Û[+'U Ü{U Ý[Y à[+\\a á[a â[+'a ä{a è[+\\e é[e ê[+'e ë{e ì[+\\i í[i î[+'i ï{i ñ; ò[+\\o ó[o ô[+'o ö{o ù[+\\u ú[u û[+'u ü{u ý[y ÿ{y";
    String norwegian = "\"@ $[+4 &^ '\\ (* )( *| +- -/ /& :> ;< <ð =) >ñ ?_ @[+2 [[+8 \\= ][+9 ^}  _? `+  {[+7 |` }[+0 ~[+]  £[+3 ¤$ §~ ¨]  ´[+=  À+A Á[+=A Â}A Ã[+]A Ä]A Å{ Æ\" È+E É[+=E Ê}E Ë]E Ì+I Í[+=I Î}I Ï]I Ñ[+]N Ò+O Ó[+=O Ô}O Õ[+]O Ö]O Ø: Ù+U Ú[+=U Û}U Ü]U Ý[+=Y à+a á[+=a â}a ã[+]a ä]a å[ æ' è+e é[+=e ê}e ë]e ì+i í[+=i î}i ï]i ñ[+]n ò+o ó[+=o ô}o õ[+]o ö]o ø; ù+u ú[+=u û}u ü]u ý[+=y ÿ]y";
    String portuguese = "\"@ &^ '- (* )( *{ +[ -/ /& :> ;< <ð =) >ñ ?_ @[+2 [[+8 \\` ][+9 ^|  _? `}  {[+7 |~ }[+0 ~\\  £[+3 §[+4 ¨[+[  ª\" «= ´]  º' »+ À}A Á]A Â|A Ã\\A Ä[+[A Ç: È}E É]E Ê|E Ë[+[E Ì}I Í]I Î|I Ï[+[I Ñ\\N Ò}O Ó]O Ô|O Õ\\O Ö[+[O Ù}U Ú]U Û|U Ü[+[U Ý]Y à}a á]a â|a ã\\a ä[+[a ç; è}e é]e ê|e ë[+[e ì}i í]i î|i ï[+[i ñ\\n ò}o ó]o ô|o õ\\o ö[+[o ù}u ú]u û|u ü[+[u ý]y ÿ[+[y";
    String selected_name;
    String spanish = "\"@ #[+3 &^ '- (* )( *} +] -/ /& :> ;< <ð =) >ñ ?_ @[+2 [[+[ \\[+` ][+] ^{  _? `[  {[+' |[+1 }[+\\ ¡= ¨\"  ª~ ¬[+6 ´'  ·# º` ¿+ À[A Á'A Â{A Ä\"A Ç| È[E É'E Ê{E Ë\"E Ì[I Í'I Î{I Ï\"I Ñ: Ò[O Ó'O Ô{O Ö\"O Ù[U Ú'U Û{U Ü\"U Ý'Y à[a á'a â{a ä\"a ç\\ è[e é'e ê{e ë\"e ì[i í'i î{i ï\"i ñ; ò[o ó'o ô{o ö\"o ù[u ú'u û{u ü\"u ý'y ÿ\"y";
    String swedish = "\"@ $[+4 &^ '\\ (* )( *| +- -/ /& :> ;< <ð =) >ñ ?_ @[+2 [[+8 \\[+- ][+9 ^}  _? `+  {[+7 |[+ð }[+0 ~[+]  £[+3 ¤$ §` ¨]  ´=  ½~ À+A Á=A Â}A Ã[+]A Ä]A Å{ È+E É=E Ê}E Ë]E Ì+I Í=I Î}I Ï]I Ñ[+]N Ò+O Ó=O Ô}O Õ[+]O Ö]O Ù+U Ú=U Û}U Ü]U Ý=Y à+a á=a â}a ã[+]a ä]a å[ è+e é=e ê}e ë]e ì+i í=i î}i ï]i ñ[+]n ò+o ó=o ô}o õ[+]o ö]o ù+u ú=u û}u ü]u ý=y ÿ]y";
    String swiss_french = "  !} \"@ #[+3 $\\ &^ '- (* )( *# +! -/ /& :> ;< <ð =) >ñ ?_ @[+2 YZ ZY [[+[ \\[+ð ][+] ^=  _? `+  yz zy {[+' |[+7 }[+\\ ~[+=  ¢[+8 £| ¦[+1 §` ¨]  ¬[+6 °~ ´[+-  À+A Á[+-A Â=A Ã[+=A Ä]A È+E É[+-E Ê=E Ë]E Ì+I Í[+-I Î=I Ï]I Ñ[+=N Ò+O Ó[+-O Ô=O Õ[+=O Ö]O Ù+U Ú[+-U Û=U Ü]U Ý[+-Z à+a á[+-a â=a ã[+=a ä]a ç$ è+e é[+-e ê=e ë]e ì+i í[+-i î=i ï]i ñ[+=n ò+o ó[+-o ô=o õ[+=o ö]o ù+u ú[+-u û=u ü]u ý[+-z ÿ]z";
    String swiss_german = "  !} \"@ #[+3 $\\ &^ '- (* )( *# +! -/ /& :> ;< <ð =) >ñ ?_ @[+2 YZ ZY [[+[ \\[+ð ][+] ^=  _? `+  yz zy {[+' |[+7 }[+\\ ~[+=  ¢[+8 £| ¦[+1 §` ¨]  ¬[+6 °~ ´[+-  À+A Á[+-A Â=A Ã[+=A Ä]A È+E É[+-E Ê=E Ë]E Ì+I Í[+-I Î=I Ï]I Ñ[+=N Ò+O Ó[+-O Ô=O Õ[+=O Ö]O Ù+U Ú[+-U Û=U Ü]U Ý[+-Z à+a á[+-a â=a ã[+=a ä]a ç$ è+e é[+-e ê=e ë]e ì+i í[+-i î=i ï]i ñ[+=n ò+o ó[+-o ô=o õ[+=o ö]o ù+u ú[+-u û=u ü]u ý[+-z ÿ]z";
    public boolean showgui;
    public boolean windows;

    void parse_locale_str(String str, Hashtable<Character, String> hashtable) {
        int i = 0;
        Character ch = null;
        StringBuilder stringBuffer = new StringBuilder(16);

        for (int i2 = 0; i2 < str.length(); i2++) {
            char charAt = str.charAt(i2);

            if (i != 0 || charAt == ' ') {
                if (i == 1 && charAt != ' ') {
                    if (charAt == 160) {
                        charAt = ' ';
                    }

                    stringBuffer.append(charAt);
                }

                if (i == 1 && charAt == ' ') {
                    hashtable.put(ch, stringBuffer.toString());
                    i = 0;
                    stringBuffer = new StringBuilder(16);
                }
            } else {
                i++;
                ch = charAt;
            }
        }
        hashtable.put(ch, stringBuffer.toString());
    }

    void add_locale(String str, String str2, String str3) {
        Hashtable<Character, String> hashtable = new Hashtable<>();

        parse_locale_str(str2, hashtable);

        locales.put(str, hashtable);
        aliases.put(str3, str);
        reverse_alias.put(str, str3);
    }

    void add_iso_alias(String str, String str2) {
        locales.put(str2, locales.get(str));
        reverse_alias.put(str2, reverse_alias.get(str));
    }

    void add_alias(String str, String str2) {
        aliases.put(str2, str);
        reverse_alias.put(str, str2);
    }

    public LocaleTranslator() {
        String str = null;
        locales.put("en_US", new Hashtable<>());
        showgui = false;
        windows = true;

        add_alias("en_US", "English (United States)");
        add_locale("en_GB", british + euro1, "English (United Kingdom)");
        add_locale("fr_FR", french + euro2, "French");
        add_locale("it_IT", italian + euro2, "Italian");
        add_locale("de_DE", german + euro2, "German");
        add_locale("es_ES", spanish + euro2, "Spanish (Spain)");
        add_locale("ja_JP", japanese, "Japanese");
        add_locale("es_MX", latin_american + euro2, "Spanish (Latin America)");
        add_iso_alias("es_MX", "es_AR");
        add_iso_alias("es_MX", "es_BO");
        add_iso_alias("es_MX", "es_CL");
        add_iso_alias("es_MX", "es_CO");
        add_iso_alias("es_MX", "es_CR");
        add_iso_alias("es_MX", "es_DO");
        add_iso_alias("es_MX", "es_EC");
        add_iso_alias("es_MX", "es_GT");
        add_iso_alias("es_MX", "es_HN");
        add_iso_alias("es_MX", "es_NI");
        add_iso_alias("es_MX", "es_PA");
        add_iso_alias("es_MX", "es_PE");
        add_iso_alias("es_MX", "es_PR");
        add_iso_alias("es_MX", "es_PY");
        add_iso_alias("es_MX", "es_SV");
        add_iso_alias("es_MX", "es_UY");
        add_iso_alias("es_MX", "es_VE");
        add_locale("fr_BE", belgian + euro2, "French Belgium");
        add_locale("fr_CA", french_canadian + euro2, "French Canadian");
        add_locale("da_DK", danish + euro2, "Danish");
        add_locale("no_NO", norwegian + euro2, "Norwegian");
        add_locale("pt_PT", portuguese + euro2, "Portugese");
        add_locale("sv_SE", swedish + euro2, "Swedish");
        add_locale("fi_FI", finnish + euro2, "Finnish");
        add_locale("fr_CH", swiss_french + euro2, "Swiss (French)");
        add_locale("de_CH", swiss_german + euro2, "Swiss (German)");

        Enumeration<?> propertyNames = remcons.prop.propertyNames();

        while (propertyNames.hasMoreElements()) {
            String str2 = (String) propertyNames.nextElement();

            if (str2.equals("locale.override")) {
                str = remcons.prop.getProperty(str2);
                System.out.println("Locale override: " + str);
            } else if (str2.startsWith("locale.windows")) {
                windows = Boolean.parseBoolean(remcons.prop.getProperty(str2));
            } else if (str2.startsWith("locale.showgui")) {
                showgui = Boolean.parseBoolean(remcons.prop.getProperty(str2));
            } else if (str2.startsWith("locale.")) {
                String substring = str2.substring(7);
                String property = remcons.prop.getProperty(str2);

                System.out.println("Adding user defined local for " + substring);

                add_locale(substring, property, substring + " (User Defined)");
            }
        }

        if (str != null) {
            System.out.println("Trying to select locale: " + str);

            if (selectLocale(str) != 0) {
                System.out.println("No keyboard definition for " + str);
                return;
            }

            return;
        }

        Locale locale = Locale.getDefault();

        System.out.println("Trying to select locale: " + locale.toString());

        if (selectLocale(locale.toString()) != 0) {
            System.out.println("No keyboard definition for '" + locale + "'");
        }
    }

    public int selectLocale(String str) {
        String str2 = aliases.get(str);

        if (str2 != null) {
            str = str2;
        }

        selected = locales.get(str);
        selected_name = reverse_alias.get(str);

        return selected != null ? 0 : -1;
    }

    public String translate(char c) {
        Character ch = c;
        String str = null;

        if (selected != null) {
            str = selected.get(ch);
        }

        return str == null ? ch.toString() : str;
    }
}
