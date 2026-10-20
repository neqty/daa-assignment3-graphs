package algorithms;

import metrics.Metrics;

/** Объединение по размеру + полное сжатие путей; без рекурсии. */
public final class DSU {
    private final int[] parent;
    private final int[] size;
    private int components;
    private final Metrics metrics;

    public DSU(int n, Metrics metrics) {
        if (n < 0) throw new IllegalArgumentException("Отрицательный размер DSU");
        this.metrics = metrics;
        parent = new int[n];
        size = new int[n];
        components = n;
        for (int i = 0; i < n; i++) { parent[i] = i; size[i] = 1; }
    }

    public static DSU makeSet(int n) { return new DSU(n, new Metrics()); }
    public int components() { return components; }

    public int find(int x) {
        check(x);
        metrics.findCalls++;
        int root = x;
        while (parent[root] != root) root = parent[root];
        while (parent[x] != x) {
            int next = parent[x];
            parent[x] = root;
            x = next;
        }
        return root;
    }

    public boolean union(int a, int b) {
        check(a);
        check(b);
        int rootA = find(a), rootB = find(b);
        if (rootA == rootB) return false;
        if (size[rootA] < size[rootB]) {
            int temp = rootA; rootA = rootB; rootB = temp;
        }
        parent[rootB] = rootA;
        size[rootA] += size[rootB];
        components--;
        metrics.successfulUnions++;
        return true;
    }

    private void check(int x) {
        if (x < 0 || x >= parent.length) throw new IllegalArgumentException("Нет элемента " + x);
    }
}
