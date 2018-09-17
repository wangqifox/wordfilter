package love.wangqi;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author: wangqi
 * @description:
 * @date: Created in 2018/9/17 下午3:29
 */
public class SensitiveWordsFilter {
    private static final Logger logger = LoggerFactory.getLogger(SensitiveWordsFilter.class);

    private Map<String, Object> sensitiveWordMap;

    private static SensitiveWordsFilter instance;
    private SensitiveWordsReader sensitiveWordsReader;

    private SensitiveWordsFilter() {}

    private SensitiveWordsFilter(SensitiveWordsReader sensitiveWordsReader) {
        this.sensitiveWordsReader = sensitiveWordsReader;
    }

    public static SensitiveWordsFilter getInstance(SensitiveWordsReader sensitiveWordsReader) {
        if (instance == null) {
            synchronized (SensitiveWordsFilter.class) {
                if (instance == null) {
                    instance = new SensitiveWordsFilter(sensitiveWordsReader);
                    instance.init();
                }
            }
        }
        return instance;
    }

    private void init() {
        Set<String> words = sensitiveWordsReader.readSensitiveWords();
        sensitiveWordMap = new HashMap<>(words.size());
        addSensitiveWords(words);
    }

    /**
     * 增加敏感词列表
     * @param words 敏感词列表
     */
    public void addSensitiveWords(Set<String> words) {
        words.forEach(this::addSensitiveWord);
    }

    /**
     * 增加单个敏感词
     * @param word 敏感词
     */
    @SuppressWarnings("unchecked")
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

    /**
     * 删除单个敏感词
     * @param word 敏感词
     */
    public synchronized void delSensitiveWord(String word) {
        if (word == null || "".equals(word)) {
            return;
        }
    }

    /**
     * 检查是否包含敏感词
     * @param str   待检测字符串
     * @param beginIndex    起始的检测位置
     * @return  从起始位置开始，敏感词的长度列表
     */
    private List<Integer> checkSensitiveWord(String str, int beginIndex) {
        int matchLength = 0;
        List<Integer> matchLengthList = new ArrayList<>();
        Map map = sensitiveWordMap;
        for (int i = beginIndex; i < str.length(); i++) {
            String ch = str.substring(i, i + 1);
            map = (Map) map.get(ch);
            if (map != null) {
                matchLength++;
                if ("1".equals(map.get("isEnd"))) {
                    matchLengthList.add(matchLength);
                }
            } else {
                break;
            }
        }
        return matchLengthList;
    }

    /**
     * 获取敏感词
     * @param str   待检测字符串
     * @return  敏感词列表
     */
    public Set<String> getSensitiveWords(String str) {
        Set<String> sensitiveWords = new HashSet<>();
        for (int i = 0; i < str.length(); i++) {
            int beginIndex = i;
            List<Integer> matchLengthList = checkSensitiveWord(str, beginIndex);
            sensitiveWords.addAll(matchLengthList.stream()
                    .map(matchLength -> str.substring(beginIndex, beginIndex + matchLength))
                    .collect(Collectors.toSet()));
            if (matchLengthList.size() > 0) {
                i = i + matchLengthList.get(0) - 1;
            }
        }
        return sensitiveWords;
    }

    /**
     * 获取敏感词的起止位置
     * @param str   待检测字符串
     * @return  map，key为起始位置，value为敏感词的长度列表
     */
    public Map<Integer, List<Integer>> getSensitiveWordsPosition(String str) {
        Map<Integer, List<Integer>> sensitiveWordsPosition = new HashMap<>();
        for (int i = 0; i < str.length(); i++) {
            int beginIndex = i;
            List<Integer> matchLengthList = checkSensitiveWord(str, beginIndex);
            sensitiveWordsPosition.put(i, matchLengthList);
            if (matchLengthList.size() > 0) {
                i = i + matchLengthList.get(0) - 1;
            }
        }
        return sensitiveWordsPosition;
    }

    /**
     * 将字符串中的敏感词替换成给定的字符
     * @param str   待检测字符串
     * @param replaceStr    替换字符
     * @return  替换后的字符串
     */
    public String replaceSensitiveWords(String str, String replaceStr) {
        StringBuilder s = new StringBuilder(str);
        Map<Integer, List<Integer>> sensitiveWordsPosition = getSensitiveWordsPosition(str);
        for (Map.Entry<Integer, List<Integer>> entry : sensitiveWordsPosition.entrySet()) {
            Integer beginIndex = entry.getKey();
            List<Integer> matchLengthList = entry.getValue();
            for (Integer matchLength : matchLengthList) {
                s.replace(beginIndex, beginIndex + matchLength, getReplaceChars(replaceStr, matchLength));
            }
        }
        return s.toString();
    }

    /**
     * 获取和替换的字符串
     *
     * @param replaceChar
     * @param length
     * @return
     */
    private String getReplaceChars(String replaceChar, int length) {
        StringBuilder resultReplace = new StringBuilder(replaceChar);
        for (int i = 1; i < length; i++) {
            resultReplace.append(replaceChar);
        }
        return resultReplace.toString();
    }

}
