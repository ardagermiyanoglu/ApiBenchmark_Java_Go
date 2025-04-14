import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

public class ApiBenchmark {
    public static void main(String[] args) throws InterruptedException {
        String url = "https://ardagermiyanoglu.github.io/";
        int totalRequests = 100;
        int concurrency = 10;

        ExecutorService executor = Executors.newFixedThreadPool(concurrency);
        HttpClient client = HttpClient.newHttpClient();

        CountDownLatch latch = new CountDownLatch(totalRequests);
        ConcurrentLinkedQueue<Long> durations = new ConcurrentLinkedQueue<>();
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failureCount = new AtomicInteger(0);

        Instant startTime = Instant.now();

        for (int i = 0; i < totalRequests; i++) {
            executor.submit(() -> {
                try {
                    Instant start = Instant.now();

                    HttpRequest request = HttpRequest.newBuilder()
                            .uri(URI.create(url))
                            .GET()
                            .build();

                    HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

                    Instant end = Instant.now();
                    long elapsed = Duration.between(start, end).toMillis();
                    durations.add(elapsed);

                    if (response.statusCode() == 200) {
                        successCount.incrementAndGet();
                    } else {
                        failureCount.incrementAndGet();
                    }

                } catch (Exception e) {
                    failureCount.incrementAndGet();
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await(); // Tüm isteklerin bitmesini bekle
        executor.shutdown();

        Instant endTime = Instant.now();
        long totalTime = Duration.between(startTime, endTime).toMillis();

        long totalDuration = durations.stream().mapToLong(Long::longValue).sum();
        double average = totalDuration / (double) durations.size();
        double qps = totalRequests / (totalTime / 1000.0);

        System.out.println("=================================");
        System.out.println("✅ Toplam İstek: " + totalRequests);
        System.out.println("✅ Başarılı İstekler: " + successCount.get());
        System.out.println("❌ Başarısız İstekler: " + failureCount.get());
        System.out.println("⏱️ Ortalama Yanıt Süresi: " + average + " ms");
        System.out.println("📊 QPS (İstek/sn): " + String.format("%.2f", qps));
        System.out.println("⏳ Toplam Süre: " + totalTime + " ms");
        System.out.println("=================================");
    }
}
