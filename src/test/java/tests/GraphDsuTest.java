package tests;

import algorithms.DSU;
import graph.Graph;
import metrics.Metrics;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Random;
import static org.junit.jupiter.api.Assertions.*;

class GraphDsuTest {
    @Test void randomUnionsAgreeWithNaiveLabels() {
        int n = 60;
        int[] label = new int[n];
        for (int i = 0; i < n; i++) label[i] = i;
        Metrics metrics = new Metrics();
        DSU dsu = new DSU(n, metrics);
        Random random = new Random(123);
        for (int step = 0; step < 500; step++) {
            int a = random.nextInt(n), b = random.nextInt(n);
            int old = label[b], replacement = label[a];
            assertEquals(old != replacement, dsu.union(a, b));
            for (int i = 0; i < n; i++) if (label[i] == old) label[i] = replacement;
            for (int i = 0; i < n; i++)
                for (int j = 0; j < n; j++)
                    assertEquals(label[i] == label[j], dsu.find(i) == dsu.find(j));
            assertEquals(Arrays.stream(label).distinct().count(), dsu.components());
        }
        assertEquals(n - dsu.components(), metrics.successfulUnions);
    }

    @Test void countersTrackCallsAndOnlySuccessfulUnions() {
        Metrics metrics = new Metrics();
        DSU dsu = new DSU(3, metrics);
        assertTrue(dsu.union(0, 1));
        assertFalse(dsu.union(1, 0));
        dsu.find(2);
        assertEquals(5, metrics.findCalls);
        assertEquals(1, metrics.successfulUnions);
        assertEquals(6, metrics.dsuOps());
        assertEquals(0, DSU.makeSet(0).components());
        assertThrows(IllegalArgumentException.class, () -> dsu.find(3));
    }

    @Test void fileInputPreservesParallelEdgesLoopsAndIsolatedVertices() throws Exception {
        Graph graph = Graph.read(Path.of("data/disconnected.txt"));
        assertEquals(6, graph.vertexCount());
        assertEquals(5, graph.edgeCount());
        assertEquals(0, graph.outgoing(5).size());
        assertEquals(10, java.util.stream.IntStream.range(0, 6).map(v -> graph.outgoing(v).size()).sum());
        assertThrows(UnsupportedOperationException.class, () -> graph.edges().clear());
        Graph directed = Graph.read(Path.of("data/cyclic.txt"));
        Graph reversed = directed.reversed();
        for (Graph.Edge edge : directed.edges())
            assertTrue(reversed.outgoing(edge.to()).contains(new Graph.Edge(edge.to(), edge.from(), edge.weight())));
    }

    @Test void malformedFilesAreRejected(@TempDir Path directory) throws Exception {
        String[] invalid = {"", "2 1 unknown\n0 1 3", "2 1 directed", "2 1 directed\n0 2 1", "2 0 directed\n0 1 1", "2 -1 directed"};
        for (String text : invalid) {
            Path input = directory.resolve("bad.txt");
            Files.writeString(input, text);
            assertThrows(IllegalArgumentException.class, () -> Graph.read(input));
        }
    }
}
