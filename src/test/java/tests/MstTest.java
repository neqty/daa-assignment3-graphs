package tests;

import algorithms.*;
import graph.Graph;
import metrics.Metrics;
import org.junit.jupiter.api.Test;
import java.nio.file.Path;
import java.util.Random;
import static org.junit.jupiter.api.Assertions.*;

class MstTest {
    @Test void oneHundredConnectedGraphsAgree() {
        Random random = new Random(17);
        for (int test = 0; test < 100; test++) {
            int n = 2 + random.nextInt(60);
            Graph graph = new Graph(n, false);
            for (int v = 1; v < n; v++) graph.addEdge(random.nextInt(v), v, random.nextInt(101) - 50);
            for (int i = 0; i < 3 * n; i++) graph.addEdge(random.nextInt(n), random.nextInt(n), random.nextInt(101) - 50);
            MstResult kruskal = Kruskal.run(graph, new Metrics());
            MstResult prim = Prim.run(graph, new Metrics());
            assertEquals(kruskal.totalWeight(), prim.totalWeight());
            validateForest(graph, kruskal);
            validateForest(graph, prim);
            assertEquals(n - 1, kruskal.edges().size());
            assertEquals(1, prim.components());
        }
    }

    @Test void tinyGraphsMatchExhaustiveSpanningTrees() {
        Random random = new Random(23);
        for (int test = 0; test < 40; test++) {
            Graph graph = new Graph(5, false);
            for (int v = 1; v < 5; v++) graph.addEdge(v - 1, v, random.nextInt(21) - 10);
            for (int i = 0; i < 5; i++) graph.addEdge(random.nextInt(5), random.nextInt(5), random.nextInt(21) - 10);
            long best = Long.MAX_VALUE;
            for (int mask = 0; mask < (1 << graph.edgeCount()); mask++) {
                if (Integer.bitCount(mask) != 4) continue;
                // Независимая проверка связности через матрицу достижимости.
                boolean[][] reachable = new boolean[5][5];
                long weight = 0;
                for (int v = 0; v < 5; v++) reachable[v][v] = true;
                for (int e = 0; e < graph.edgeCount(); e++) if ((mask & (1 << e)) != 0) {
                    Graph.Edge edge = graph.edges().get(e);
                    reachable[edge.from()][edge.to()] = reachable[edge.to()][edge.from()] = true;
                    weight += edge.weight();
                }
                for (int k = 0; k < 5; k++) for (int i = 0; i < 5; i++) for (int j = 0; j < 5; j++)
                    reachable[i][j] |= reachable[i][k] && reachable[k][j];
                boolean connected = true;
                for (int v = 0; v < 5; v++) connected &= reachable[0][v];
                if (connected) best = Math.min(best, weight);
            }
            assertEquals(best, Kruskal.run(graph, new Metrics()).totalWeight());
            assertEquals(best, Prim.run(graph, new Metrics()).totalWeight());
        }
    }

    @Test void disconnectedLoopsParallelEdgesEmptyAndOverflow() throws Exception {
        Graph graph = Graph.read(Path.of("data/disconnected.txt"));
        for (MstResult result : new MstResult[]{Kruskal.run(graph, new Metrics()), Prim.run(graph, new Metrics())}) {
            assertEquals(8, result.totalWeight());
            assertEquals(3, result.components());
            validateForest(graph, result);
        }
        assertEquals(6, Kruskal.run(Graph.read(Path.of("data/connected.txt")), new Metrics()).totalWeight());
        for (int n : new int[]{0, 1, 7}) {
            Graph empty = new Graph(n, false);
            assertEquals(n, Kruskal.run(empty, new Metrics()).components());
            assertEquals(n, Prim.run(empty, new Metrics()).components());
        }
        Graph large = new Graph(3, false);
        large.addEdge(0, 1, Integer.MAX_VALUE);
        large.addEdge(1, 2, Integer.MAX_VALUE);
        assertEquals(2L * Integer.MAX_VALUE, Kruskal.run(large, new Metrics()).totalWeight());
        assertEquals(2L * Integer.MAX_VALUE, Prim.run(large, new Metrics()).totalWeight());
        assertThrows(IllegalArgumentException.class, () -> Prim.run(new Graph(2, true), new Metrics()));
    }

    private static void validateForest(Graph graph, MstResult result) {
        DSU sets = DSU.makeSet(graph.vertexCount());
        long sum = 0;
        for (Graph.Edge edge : result.edges()) {
            assertTrue(sets.union(edge.from(), edge.to()), "Цикл в остове");
            sum += edge.weight();
        }
        for (Graph.Edge edge : graph.edges()) assertEquals(sets.find(edge.from()), sets.find(edge.to()));
        assertEquals(result.components(), sets.components());
        assertEquals(graph.vertexCount() - result.components(), result.edges().size());
        assertEquals(sum, result.totalWeight());
    }
}
