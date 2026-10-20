package benchmark;

import graph.Graph;
import java.util.Random;

/** Новый Random(42) для КАЖДОГО графа; порядок вызовов генератора существенен. */
public final class GraphGenerator {
    private GraphGenerator() { }
    public enum Kind { UNDIRECTED, DIRECTED, DAG }

    public static Graph generate(int n, int m, Kind kind) {
        if (n < 1 || m < 0 || (n == 1 && m > 0)) throw new IllegalArgumentException("Неверный размер");
        if (kind == Kind.UNDIRECTED && m < n - 1) throw new IllegalArgumentException("Недостаточно рёбер для дерева");
        Random random = new Random(42);
        Graph graph = new Graph(n, kind != Kind.UNDIRECTED);
        int[] order = new int[n];
        for (int i = 0; i < n; i++) order[i] = i;
        if (kind != Kind.DIRECTED) {
            for (int i = n - 1; i > 0; i--) {
                int j = random.nextInt(i + 1), temp = order[i];
                order[i] = order[j]; order[j] = temp;
            }
        }
        if (kind == Kind.UNDIRECTED) {
            for (int i = 1; i < n; i++) {
                int from = order[random.nextInt(i)];
                graph.addEdge(from, order[i], 1 + random.nextInt(1000));
            }
        }
        while (graph.edgeCount() < m) {
            int a = random.nextInt(n), b = random.nextInt(n);
            while (b == a) b = random.nextInt(n);
            if (kind == Kind.DAG && order[a] > order[b]) {
                int temp = a; a = b; b = temp;
            }
            graph.addEdge(a, b, 1 + random.nextInt(1000));
        }
        return graph;
    }
}
