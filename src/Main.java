import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class Main {

    public static final Map<Integer, Integer> sizeToFreq = new HashMap<>();
    private static final Object monitor = new Object();
    private static int mostFrequentSize = 0;
    private static int maxFrequency = 0;

    public static void main(String[] args) throws InterruptedException {
        int numberOfRoutes = 1000;
        ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

        Thread printerThread = new Thread(() -> {
            try {
                while (!Thread.interrupted()) {
                    synchronized (monitor) {
                        monitor.wait(); // Ждем сигнала
                        calculateAndPrintMax();
                    }
                }
            } catch (InterruptedException e) {
                System.out.println("Printer thread interrupted.");
            }
        });
        printerThread.start();

        for (int i = 0; i < numberOfRoutes; i++) {
            executor.submit(() -> {
                String route = generateRoute("RLRFR", 100);
                int rCount = countR(route);

                synchronized (sizeToFreq) {
                    sizeToFreq.put(rCount, sizeToFreq.getOrDefault(rCount, 0) + 1);
                }

                synchronized (monitor) {
                    monitor.notify(); // Отправляем сигнал
                }
            });
        }

        executor.shutdown();
        executor.awaitTermination(1, TimeUnit.HOURS);

        printerThread.interrupt(); // Прерываем поток вывода

        printerThread.join(); // дожидаемся завершения потока printerThread

        printResults(); // Выводим финальные результаты
    }

    public static String generateRoute(String letters, int length) {
        Random random = new Random();
        StringBuilder route = new StringBuilder();
        for (int i = 0; i < length; i++) {
            route.append(letters.charAt(random.nextInt(letters.length())));
        }
        return route.toString();
    }

    private static int countR(String route) {
        int count = 0;
        for (int i = 0; i < route.length(); i++) {
            if (route.charAt(i) == 'R') {
                count++;
            }
        }
        return count;
    }

    private static void calculateAndPrintMax() {
        synchronized (sizeToFreq) { // Синхронизируем доступ к sizeToFreq
            int currentMostFrequentSize = 0;
            int currentMaxFrequency = 0;

            for (Map.Entry<Integer, Integer> entry : sizeToFreq.entrySet()) {
                if (entry.getValue() > currentMaxFrequency) {
                    currentMaxFrequency = entry.getValue();
                    currentMostFrequentSize = entry.getKey();
                }
            }

            if (currentMostFrequentSize != mostFrequentSize || currentMaxFrequency != maxFrequency) {
                mostFrequentSize = currentMostFrequentSize;
                maxFrequency = currentMaxFrequency;

                System.out.println("Текущий лидер: " + mostFrequentSize + " (встретилось " + maxFrequency + " раз)");
            }
        }
    }

    private static void printResults() {
        System.out.println("\nФинальные результаты:");
        System.out.println("Самое частое количество повторений " + mostFrequentSize + " (встретилось " + maxFrequency + " раз)");
        System.out.println("Другие размеры:");

        List<Map.Entry<Integer, Integer>> sortedEntries = new ArrayList<>(sizeToFreq.entrySet());
        sortedEntries.sort((e1, e2) -> e2.getValue().compareTo(e1.getValue())); // Сортировка по убыванию частоты

        for (Map.Entry<Integer, Integer> entry : sortedEntries) {
            if (entry.getKey() != mostFrequentSize) {
                System.out.println("- " + entry.getKey() + " (" + entry.getValue() + " раз)");
            }
        }
    }
}