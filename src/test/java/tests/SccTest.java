package tests;

import algorithms.Kosaraju;
import graph.Graph;
import metrics.Metrics;
import org.junit.jupiter.api.Test;
import java.nio.file.Path;
import java.util.ArrayDeque;
import java.util.Random;
import static org.junit.jupiter.api.Assertions.*;

class SccTest {
    @Test void agreesWithBfsReachabilityOnRandomGraphs() {
        Random random = new Random(81);
        for (int test = 0; test < 60; test++) {
            int n = 1 + random.nextInt(200);
            Graph graph = new Graph(n, true);
            for (int e = 0; e < n * 4; e++) graph.addEdge(random.nextInt(n), random.nextInt(n), 1);
            boolean[][] reachable = new boolean[n][];
            for (int v = 0; v < n; v++) reachable[v] = bfs(graph, v);
            Metrics metrics = new Metrics();
            var result = Kosaraju.run(graph, metrics);
            for (int u = 0; u < n; u++) for (int v = 0; v < n; v++)
                assertEquals(reachable[u][v] && reachable[v][u], result.component()[u] == result.component()[v]);
            assertEquals(java.util.Arrays.stream(result.component()).distinct().count(), result.count());
            assertEquals(2L * graph.edgeCount(), metrics.edgeScans);
            assertTrue(metrics.maxDepth <= n);
        }
    }

    @Test void hundredThousandVertexPathIsStackSafe() {
        int n = 100_000;
        Graph graph = new Graph(n, true);
        for (int v = 1; v < n; v++) graph.addEdge(v - 1, v, 1);
        Metrics metrics = new Metrics();
        assertEquals(n, Kosaraju.run(graph, metrics).count());
        assertEquals(n, metrics.maxDepth);
        assertEquals(2L * (n - 1), metrics.edgeScans);
    }

    @Test void cyclicFileAndEmptyGraph() throws Exception {
        Graph graph = Graph.read(Path.of("data/cyclic.txt"));
        var result = Kosaraju.run(graph, new Metrics());
        assertEquals(3, result.count());
        assertEquals(result.component()[0], result.component()[2]);
        assertEquals(result.component()[3], result.component()[4]);
        assertNotEquals(result.component()[2], result.component()[3]);
        assertNotEquals(result.component()[4], result.component()[5]);
        assertEquals(0, Kosaraju.run(new Graph(0, true), new Metrics()).count());
        assertThrows(IllegalArgumentException.class, () -> Kosaraju.run(new Graph(2, false), new Metrics()));
    }

    private static boolean[] bfs(Graph graph, int source) {
        boolean[] seen = new boolean[graph.vertexCount()];
        var queue = new ArrayDeque<Integer>();
        seen[source] = true;
        queue.add(source);
        while (!queue.isEmpty()) {
            int v = queue.remove();
            for (Graph.Edge edge : graph.outgoing(v)) if (!seen[edge.to()]) {
                seen[edge.to()] = true;
                queue.add(edge.to());
            }
        }
        return seen;
    }
}
