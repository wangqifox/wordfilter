package love.wangqi;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author: wangqi
 * @description:
 * @date: Created in 2018/9/18 上午10:53
 */
public class CombinationSensitiveWordsFilter {
    private static CombinationSensitiveWordsFilter instance;
    private SensitiveWordsReader sensitiveWordsReader;

    private Map<String, Set<String>> invertedIndex;

    private CombinationSensitiveWordsFilter() {}

    private CombinationSensitiveWordsFilter(SensitiveWordsReader sensitiveWordsReader) {
        this.sensitiveWordsReader = sensitiveWordsReader;
    }

    public static CombinationSensitiveWordsFilter getInstance(SensitiveWordsReader sensitiveWordsReader) {
        if (instance == null) {
            synchronized (CombinationSensitiveWordsFilter.class) {
                if (instance == null) {
                    instance = new CombinationSensitiveWordsFilter(sensitiveWordsReader);
                    instance.init();
                }
            }
        }
        return instance;
    }

    private Set<String> getDocuments(String term) {
        return invertedIndex.get(term);
    }

    /**
     * 获取敏感词
     * @param sensitiveWords   单个敏感词
     * @return  敏感词列表
     */
    public Set<String> getSensitiveWords(Set<String> sensitiveWords) {
        Set<String> result = new HashSet<>();
        Set<String> documents = sensitiveWords.stream()
                .map(this::getDocuments)
                .flatMap(Collection::stream)
                .collect(Collectors.toSet());
        for (String document : documents) {
            boolean isContain = true;
            if (document.indexOf(" ") > 0) {
                String[] words = document.split(" ");
                for (String word : words) {
                    if (!sensitiveWords.contains(word)) {
                        isContain = false;
                        break;
                    }
                }
            } else {
                isContain = sensitiveWords.contains(document);
            }
            if (isContain) {
                result.add(document);
            }
        }
        return result;
    }


    private void init() {
        invertedIndex = new HashMap<>();
        Set<String> lines = sensitiveWordsReader.readSensitiveWords();
        for (String line : lines) {
            addLine(line);
        }
    }

    /**
     * 增加一组组合敏感词，敏感词之间以空格分隔
     * @param line
     */
    public synchronized void addLine(String line) {
        line = line.trim();
        String[] words = line.split(" ");
        for (String word : words) {
            if (word == null || "".equals(word)) {
                continue;
            }
            invertedIndex.computeIfAbsent(word , k -> new HashSet<>());
            invertedIndex.get(word).add(line);
        }
    }

    /**
     * 删除一组组合敏感词，敏感词之间以空格分隔
     * @param line
     */
    public synchronized void delLine(String line) {
        String[] words = line.split(" ");
        for (String word : words) {
            if (word == null || "".equals(word)) {
                continue;
            }
            Set<String> lines = invertedIndex.get(word);
            if (!line.isEmpty() && lines.contains(word)) {
                lines.remove(word);
            }
            if (lines.isEmpty()) {
                invertedIndex.remove(word);
            }
        }
    }
}
