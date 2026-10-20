package tests;

import algorithms.*;
import benchmark.GraphGenerator;
import graph.Graph;
import metrics.Metrics;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class GeneratorTest {
    @Test void exactSmallReferenceValuesForBothDensities() {
        long[][] references = {{4000, 147970, 27, 396274}, {20000, 30363, 1, 252411}};
        for (long[] expected : references) {
            int m = (int) expected[0];
            Graph undirected = GraphGenerator.generate(1000, m, GraphGenerator.Kind.UNDIRECTED);
            assertEquals(expected[1], Kruskal.run(undirected, new Metrics()).totalWeight());
            assertEquals(expected[1], Prim.run(undirected, new Metrics()).totalWeight());
            assertEquals(expected[2], Kosaraju.run(GraphGenerator.generate(1000, m, GraphGenerator.Kind.DIRECTED), new Metrics()).count());
            assertEquals(expected[3], DagShortestPath.run(GraphGenerator.generate(1000, m, GraphGenerator.Kind.DAG), new Metrics()).sum());
        }
    }

    @Test void deterministicGraphsWithExactEdgeCountsAndNoSelfLoops() {
        for (var kind : GraphGenerator.Kind.values()) {
            Graph first = GraphGenerator.generate(200, 800, kind);
            Graph second = GraphGenerator.generate(200, 800, kind);
            assertEquals(first.edges(), second.edges());
            assertEquals(800, first.edgeCount());
            for (Graph.Edge edge : first.edges()) {
                assertNotEquals(edge.from(), edge.to());
                assertTrue(edge.weight() >= 1 && edge.weight() <= 1000);
            }
            if (kind == GraphGenerator.Kind.DAG) assertFalse(Kahn.run(first, new Metrics()).hasCycle());
        }
    }
}
