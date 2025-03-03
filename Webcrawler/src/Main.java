
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

class Solution {
    Set<String> urls = ConcurrentHashMap.newKeySet(); // Thread-safe visited set
    ExecutorService threadPool = Executors.newFixedThreadPool(15); // Correct initialization

    public List<String> crawl(String startUrl, HtmlParser htmlParser) {
        String hostname = getHostname(startUrl);
        List<String> urlList = htmlParser.getUrls(startUrl).stream()
                .filter(url -> isSameHostname(url, hostname))
                .filter(url -> urls.add(url)) // Ensures URL is visited only once
                .collect(Collectors.toList());

        List<Future<List<String>>> futures = new ArrayList<>();
        for (String url : urlList) {
            futures.add(threadPool.submit(() -> crawl(url, htmlParser))); // Recursive call
        }

        List<String> result = new ArrayList<>(urlList);
        for (Future<List<String>> future : futures) {
            try {
                result.addAll(future.get()); // Aggregate results from recursive calls
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
        }

        return result;
    }

    private boolean isSameHostname(String url, String hostname) {
        return getHostname(url).equals(hostname);
    }

    private String getHostname(String url) {
        int idx = url.indexOf('/', 7); // Skip "http://"
        return (idx != -1) ? url.substring(0, idx) : url;
    }
}


public class Main {
    public static void main(String[] args) {
        System.out.println("Hello world!");
    }
}