package love.wangqi;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author: wangqi
 * @description:
 * @date: Created in 2018/10/15 20:14
 */
public class KFCChecker {
    private String path;

    private Map<String, Object> sensitiveWordMap;


    private Set<String> readContent() throws IOException {
        return Files.lines(Paths.get(path), StandardCharsets.UTF_8).collect(Collectors.toSet());
    }


    public KFCChecker(String path) throws IOException {
        this.path = path;
        init();
    }


    private void init() throws IOException {
        Set<String> words = readContent();
        sensitiveWordMap = new HashMap<>(words.size());
        addSensitiveWords(words);
    }

    public void addSensitiveWords(Set<String> words) {
        words.forEach(this::addSensitiveWord);

    }

    public synchronized void addSensitiveWord(String word) {
        if (word == null || "".equals(word)) {
            return;
        }
        word = word.trim();
        Map map = sensitiveWordMap;
        Map subMap;
        for (int i = 0; i < word.length(); i++) {
            String ch = word.substring(i, i + 1);
            Object wordMap = sensitiveWordMap.get(ch);

            if (wordMap != null) {
                map = (Map) wordMap;
            } else {
                subMap = new HashMap();
                subMap.put("isEnd", "0");
                map.put(ch, subMap);
                map = subMap;
            }
            if (i == word.length() - 1) {
                map.put("isEnd", "1");
            }
        }
    }

    public boolean checkSensitiveWord(String str) {
        for (int i = 0; i < str.length(); i++) {
            if (checkSensitiveWord(str, i)) {
                return true;
            }
        }
        return false;
    }


    private boolean checkSensitiveWord(String str, int beginIndex) {
        Map map = sensitiveWordMap;
        for (int i = beginIndex; i < str.length(); i++) {
            String ch = str.substring(i, i + 1);
            map = (Map) map.get(ch);
            if (map != null) {
                if ("1".equals(map.get("isEnd"))) {
                    return true;
                }
            } else {
                break;
            }
        }
        return false;
    }

    public static void main(String[] args) throws IOException {
        KFCChecker kfcChecker = new KFCChecker("words.txt");
        Boolean contains = kfcChecker.checkSensitiveWord("123");
        System.out.println(contains);

    }
}
