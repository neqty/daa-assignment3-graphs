package benchmark;

import algorithms.*;
import graph.Graph;
import metrics.Metrics;
import java.nio.file.Path;
import java.util.Arrays;

/** Короткий запуск для защиты: показать результат на небольшом входном файле. */
public final class Demo {
    private Demo() { }
    public static void main(String[] args) throws Exception {
        if (args.length != 1) throw new IllegalArgumentException("Укажи файл: data/connected.txt или data/dag.txt");
        Graph graph = Graph.read(Path.of(args[0]));
        System.out.printf("Вершин: %d, рёбер: %d%n", graph.vertexCount(), graph.edgeCount());
        if (!graph.isDirected()) {
            var kruskal = Kruskal.run(graph, new Metrics());
            var prim = Prim.run(graph, new Metrics());
            System.out.println("Kruskal: " + kruskal);
            System.out.println("Prim:    " + prim);
        } else {
            var scc = Kosaraju.run(graph, new Metrics());
            System.out.println("SCC: " + scc.count() + ", номера компонент: " + Arrays.toString(scc.component()));
            var topology = Kahn.run(graph, new Metrics());
            if (topology.hasCycle()) System.out.println("Есть цикл: топологический порядок и DAG-пути не определены.");
            else {
                System.out.println("Топологический порядок: " + Arrays.toString(topology.order()));
                var paths = DagShortestPath.run(graph, new Metrics());
                for (int v = 0; v < graph.vertexCount(); v++)
                    System.out.printf("До %d: расстояние %d, путь %s%n", v, paths.distance()[v], paths.pathTo(v));
            }
        }
    }
}
