package love.wangqi;

import org.junit.Test;

import java.util.HashSet;
import java.util.Set;

/**
 * @author: wangqi
 * @description:
 * @date: Created in 2018/9/17 下午7:47
 */
public class SensitiveWordsFilterTest {
    @Test
    public void test01() {
        SensitiveWordsReader sensitiveWordsReader = new SensitiveWordsReader() {
            @Override
            public Set<String> readSensitiveWords() {
                Set<String> words = new HashSet<>();
                words.add("abc");
                words.add("hii");
                words.add("z");
                return words;
            }
        };
        SensitiveWordsFilter sensitiveWordsFilter = SensitiveWordsFilter.getInstance(sensitiveWordsReader);
        Set<String> words = sensitiveWordsFilter.getSensitiveWord("abczhiiqbc");
        System.out.println(words);

    }
}
