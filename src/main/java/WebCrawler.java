import org.jetbrains.annotations.NotNull;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class WebCrawler {
    private static final int MAX_STEPS = 10;
    private static final long CRAWLING_TIME = TimeUnit.MINUTES.toMillis(5);
    private static final String START_PAGE = "https://en.wikipedia.org/wiki/Open-source_intelligence";
    private static final Map<String, Integer> WORD_FREQUENCY = new HashMap<>();
    private static final Set<String> VISITED_URLS = new HashSet<>();

    public static void main(String[] args) {
        crawl(START_PAGE, 0, System.currentTimeMillis() + CRAWLING_TIME);
        AtomicInteger rank = new AtomicInteger(1);
        WORD_FREQUENCY.entrySet().stream()
                .sorted((entry1, entry2) -> entry2.getValue().compareTo(entry1.getValue()))
                .limit(10)
                .forEach(entry -> System.out.println("Most important word " + rank.getAndIncrement() + ": " + entry.getKey() + " - Total = " + entry.getValue()));

        //TODO: printing all valid visited URLs and their count
        System.out.println("\nVisited URLs (" + VISITED_URLS.size() + "):");
        for (String url : VISITED_URLS) {
            System.out.println(url);
        }
    }
    private static void crawl(String url, int depth, long endTime) {
        if (depth >= MAX_STEPS || System.currentTimeMillis() >= endTime || VISITED_URLS.contains(url)) {
            return;
        }

        VISITED_URLS.add(url);

        try {
            Document doc = Jsoup.connect(url).get();
            processPage(doc);
            Elements links = doc.select("a[href]");
            for (Element link : links) {
                String nextUrl = link.absUrl("href");
                if (!nextUrl.isEmpty() && !nextUrl.startsWith("#") && !nextUrl.contains("javascript:")) {
                    crawl(nextUrl, depth + 1, endTime);
                }
            }
        } catch (IOException e) {
            //TODO: to print the broken links 404
            // e.printStackTrace();
        }
    }
    private static void processPage(@NotNull Document doc) {
        String text = doc.body().text().toLowerCase();
        String[] words = text.split("\\W+");
        for (String word : words) {
            //TODO: excluding small words.
            if (!word.isEmpty() && word.length() > 5) {
                WORD_FREQUENCY.put(word, WORD_FREQUENCY.getOrDefault(word, 0) + 1);
            }
        }
    }
}

