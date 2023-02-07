package org.example.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class HtmlUtil {
    public static String replaceLinksForImage(String html, String regex, String replacement) {
        Pattern pattern = Pattern.compile("(" + regex + "jpg)|" +
                "(" + regex + "svg)|" +
                "(" + regex + "png)|" +
                "(" + regex + "gif)");
        Matcher matcher = pattern.matcher(html);
        StringBuilder newString = new StringBuilder();
        int start = 0;
        while (matcher.find()) {
            String link = html.substring(matcher.start(), matcher.end());
            int end = matcher.start();
            newString.append(html, start, end).append(replacement).append(link);
            start = matcher.end();
        }
        newString.append(html.substring(start));
        return String.valueOf(newString);
    }
}
