package love.wangqi;

import java.util.Set;

/**
 * @author: wangqi
 * @description:
 * @date: Created in 2018/9/17 19:00
 */
public interface SensitiveWordsReader {
    /**
     * 获取敏感词列表
     * @return
     */
    Set<String> readSensitiveWords();
}
