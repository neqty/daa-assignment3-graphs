package benchmark;

import algorithms.*;
import graph.Graph;
import metrics.Metrics;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.function.Function;
import java.util.function.ToLongFunction;

public final class Benchmark {
    private Benchmark() { }
    private static final int[] SIZES = {1000, 3162, 10000, 31623, 100000};
    // Только ожидаемые значения для проверок. Фактические результаты вычисляют алгоритмы.
    private static final long[][] REFERENCE = {
        {147970, 27, 396274}, {30363, 1, 252411},
        {468911, 137, 1271911}, {97589, 1, 723157},
        {1486139, 373, 3941828}, {309550, 1, 2381342},
        {4715515, 1188, 12551891}, {968517, 1, 7376612},
        {15012094, 3923, 40096539}, {3056681, 1, 23512087}
    };
    private record Measurement(double milliseconds, Metrics metrics, long result) { }

    public static void main(String[] args) throws Exception {
        if (args.length > 1 || (args.length == 1 && !args[0].equals("--quick")))
            throw new IllegalArgumentException("Использование: Benchmark [--quick]");
        boolean quick = args.length == 1;
        warmUp();
        List<String> rows = new ArrayList<>();
        rows.add("algorithm,graph,n,m,time_ms,dsu_ops,edge_scans,comparisons,max_depth,result");
        int index = 0;
        for (int n : SIZES) {
            if (quick && n != 1000) break;
            for (int density : new int[]{4, 20}) {
                int m = n * density;
                String label = density == 4 ? "sparse" : "dense";
                long[] expected = REFERENCE[index++];
                long mst = benchmarkMst(n, m, label, expected[0], rows);
                long scc = benchmarkScc(n, m, label, expected[1], rows);
                long sum = benchmarkDag(n, m, label, expected[2], rows);
                System.out.printf(Locale.ROOT, "%d / %d | MST %d | SCC %d | DAG %d | OK%n", n, m, mst, scc, sum);
            }
        }
        require(rows.size() == (quick ? 11 : 51), "Неверное число строк CSV");
        Path output = Path.of("results", quick ? "quick-results.csv" : "results.csv");
        Files.createDirectories(output.getParent());
        Path temporary = Files.createTempFile(output.getParent(), "benchmark-", ".tmp");
        Files.write(temporary, rows);
        Files.move(temporary, output, StandardCopyOption.REPLACE_EXISTING);
        System.out.println("Сохранено: " + output + "; 5 запусков на строку, медиана времени.");
    }

    private static long benchmarkMst(int n, int m, String label, long expected, List<String> rows) {
        Graph graph = GraphGenerator.generate(n, m, GraphGenerator.Kind.UNDIRECTED);
        ToLongFunction<MstResult> validate = result -> {
            require(result.components() == 1 && result.edges().size() == n - 1, "Неверный остов");
            return result.totalWeight();
        };
        Measurement kruskal = measure(metrics -> Kruskal.run(graph, metrics), validate);
        Measurement prim = measure(metrics -> Prim.run(graph, metrics), validate);
        require(kruskal.result == prim.result, "Kruskal и Prim расходятся");
        require(kruskal.result == expected, "MST не совпал с эталоном: " + kruskal.result + " != " + expected);
        require(prim.metrics.edgeScans == 2L * m, "Prim должен просмотреть 2m записей");
        add(rows, "Kruskal", "undirected_" + label, n, m, kruskal);
        add(rows, "Prim", "undirected_" + label, n, m, prim);
        return kruskal.result;
    }

    private static long benchmarkScc(int n, int m, String label, long expected, List<String> rows) {
        Graph graph = GraphGenerator.generate(n, m, GraphGenerator.Kind.DIRECTED);
        Measurement result = measure(metrics -> Kosaraju.run(graph, metrics), Kosaraju.Result::count);
        require(result.result == expected, "SCC не совпал с эталоном: " + result.result + " != " + expected);
        require(result.metrics.edgeScans == 2L * m, "Kosaraju должен просмотреть 2m рёбер");
        add(rows, "Kosaraju", "directed_" + label, n, m, result);
        return result.result;
    }

    private static long benchmarkDag(int n, int m, String label, long expected, List<String> rows) {
        Graph graph = GraphGenerator.generate(n, m, GraphGenerator.Kind.DAG);
        Measurement kahn = measure(metrics -> Kahn.run(graph, metrics), result -> {
            require(!result.hasCycle() && Kahn.isValidOrder(graph, result.order()), "Неверный топологический порядок");
            return result.order().length;
        });
        Measurement paths = measure(metrics -> DagShortestPath.run(graph, metrics), DagShortestPath.Result::sum);
        require(paths.result == expected, "DAG не совпал с эталоном: " + paths.result + " != " + expected);
        require(kahn.metrics.edgeScans == m && paths.metrics.edgeScans == m, "Kahn и DAG должны просмотреть m рёбер");
        add(rows, "Kahn", "dag_" + label, n, m, kahn);
        add(rows, "DagShortestPath", "dag_" + label, n, m, paths);
        return paths.result;
    }

    private static <T> Measurement measure(Function<Metrics, T> algorithm, ToLongFunction<T> validate) {
        long[] times = new long[5];
        Metrics first = null;
        long expected = 0;
        for (int run = 0; run < times.length; run++) {
            Metrics metrics = new Metrics();
            long start = System.nanoTime();
            T result = algorithm.apply(metrics);
            times[run] = System.nanoTime() - start;
            long value = validate.applyAsLong(result); // Валидация вне измеряемого интервала.
            if (run == 0) { first = metrics; expected = value; }
            else {
                require(value == expected, "Результат меняется между запусками");
                require(metrics.findCalls == first.findCalls && metrics.successfulUnions == first.successfulUnions
                    && metrics.edgeScans == first.edgeScans && metrics.comparisons == first.comparisons
                    && metrics.maxDepth == first.maxDepth, "Счётчики меняются между запусками");
            }
        }
        Arrays.sort(times);
        return new Measurement(times[2] / 1_000_000.0, first, expected);
    }

    private static void add(List<String> rows, String name, String kind, int n, int m, Measurement value) {
        Metrics metrics = value.metrics;
        rows.add(String.format(Locale.ROOT, "%s,%s,%d,%d,%.6f,%d,%d,%d,%d,%d", name, kind, n, m,
            value.milliseconds, metrics.dsuOps(), metrics.edgeScans, metrics.comparisons, metrics.maxDepth, value.result));
    }

    private static void warmUp() {
        Graph undirected = GraphGenerator.generate(1000, 4000, GraphGenerator.Kind.UNDIRECTED);
        Graph directed = GraphGenerator.generate(1000, 4000, GraphGenerator.Kind.DIRECTED);
        Graph dag = GraphGenerator.generate(1000, 4000, GraphGenerator.Kind.DAG);
        for (int i = 0; i < 3; i++) {
            Kruskal.run(undirected, new Metrics()); Prim.run(undirected, new Metrics());
            Kosaraju.run(directed, new Metrics()); Kahn.run(dag, new Metrics());
            DagShortestPath.run(dag, new Metrics());
        }
    }
    private static void require(boolean condition, String message) {
        if (!condition) throw new IllegalStateException(message);
    }
}
