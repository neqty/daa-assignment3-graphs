package tests;

import algorithms.DagShortestPath;
import algorithms.Kahn;
import graph.Graph;
import metrics.Metrics;
import org.junit.jupiter.api.Test;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Random;
import static org.junit.jupiter.api.Assertions.*;

class PathsTest {
    @Test void randomDagsMatchMultiSourceBellmanFordIncludingNegativeWeights() {
        Random random = new Random(99);
        for (int test = 0; test < 100; test++) {
            int n = 2 + random.nextInt(49);
            int[] permutation = new int[n];
            for (int i = 0; i < n; i++) permutation[i] = i;
            for (int i = n - 1; i > 0; i--) {
                int j = random.nextInt(i + 1), value = permutation[i];
                permutation[i] = permutation[j]; permutation[j] = value;
            }
            Graph graph = new Graph(n, true);
            for (int e = 0; e < 4 * n; e++) {
                int a = random.nextInt(n), b = random.nextInt(n);
                if (a == b) continue;
                graph.addEdge(permutation[Math.min(a, b)], permutation[Math.max(a, b)], random.nextInt(101) - 50);
            }
            Metrics topologyMetrics = new Metrics();
            var topology = Kahn.run(graph, topologyMetrics);
            assertFalse(topology.hasCycle());
            assertTrue(Kahn.isValidOrder(graph, topology.order()));
            assertEquals(graph.edgeCount(), topologyMetrics.edgeScans);
            Metrics pathMetrics = new Metrics();
            var paths = DagShortestPath.run(graph, pathMetrics);
            assertArrayEquals(bellmanFord(graph), paths.distance());
            assertEquals(graph.edgeCount(), pathMetrics.edgeScans);
            for (int v = 0; v < n; v++) {
                var path = paths.pathTo(v);
                assertEquals(v, path.get(path.size() - 1));
                assertEquals(0, graph.indegree(path.get(0)));
                assertTrue(path.size() <= n);
                for (int i = 1; i < path.size(); i++) {
                    int from = path.get(i - 1), to = path.get(i);
                    assertTrue(graph.outgoing(from).stream().anyMatch(e -> e.to() == to && paths.distance()[from] + e.weight() == paths.distance()[to]));
                }
            }
        }
    }

    @Test void fixtureSourcesNegativePathAndCycleDetection() throws Exception {
        Graph dag = Graph.read(Path.of("data/dag.txt"));
        var paths = DagShortestPath.run(dag, new Metrics());
        assertArrayEquals(new long[]{0, 0, -2, -3, 2, -2}, paths.distance());
        assertEquals(java.util.List.of(1, 4, 3, 5), paths.pathTo(5));
        Graph cyclic = Graph.read(Path.of("data/cyclic.txt"));
        assertTrue(Kahn.run(cyclic, new Metrics()).hasCycle());
        assertEquals(0, Kahn.run(cyclic, new Metrics()).order().length);
        assertThrows(IllegalArgumentException.class, () -> DagShortestPath.run(cyclic, new Metrics()));
        Graph loop = new Graph(1, true); loop.addEdge(0, 0, -1);
        assertTrue(Kahn.run(loop, new Metrics()).hasCycle());
        assertFalse(Kahn.isValidOrder(dag, new int[]{0, 1, 2, 3, 4, 5}));
        assertFalse(Kahn.isValidOrder(dag, new int[]{0, 0, 0, 0, 0, 0}));
    }

    @Test void isolatedEmptyOverflowAndWrongDirection() {
        Graph isolated = new Graph(4, true);
        assertArrayEquals(new long[4], DagShortestPath.run(isolated, new Metrics()).distance());
        assertEquals(java.util.List.of(3), DagShortestPath.run(isolated, new Metrics()).pathTo(3));
        assertEquals(0, DagShortestPath.run(new Graph(0, true), new Metrics()).sum());
        Graph large = new Graph(3, true);
        large.addEdge(0, 1, Integer.MAX_VALUE); large.addEdge(1, 2, Integer.MAX_VALUE);
        assertEquals(2L * Integer.MAX_VALUE, DagShortestPath.run(large, new Metrics()).distance()[2]);
        assertThrows(IllegalArgumentException.class, () -> Kahn.run(new Graph(1, false), new Metrics()));
    }

    private static long[] bellmanFord(Graph graph) {
        long[] dist = new long[graph.vertexCount()];
        Arrays.fill(dist, Long.MAX_VALUE);
        for (int v = 0; v < dist.length; v++) if (graph.indegree(v) == 0) dist[v] = 0;
        for (int pass = 1; pass < dist.length; pass++) {
            boolean changed = false;
            for (Graph.Edge edge : graph.edges()) {
                if (dist[edge.from()] != Long.MAX_VALUE && dist[edge.from()] + edge.weight() < dist[edge.to()]) {
                    dist[edge.to()] = dist[edge.from()] + edge.weight();
                    changed = true;
                }
            }
            if (!changed) break;
        }
        return dist;
    }
}
