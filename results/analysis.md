# Численный анализ CSV

Наклон: линейная регрессия log(счётчик) по log(n), пять точек отдельно для каждой плотности.

| Алгоритм | Плотность | Наклон scans | Наклон comparisons | Наклон суммы операций |
|---|---|---:|---:|---:|
| Kruskal | sparse | 1.0550 | 1.0545 | 1.0520 |
| Kruskal | dense | 1.1194 | 1.0159 | 1.0218 |
| Prim | sparse | 1.0000 | 1.1079 | 1.1007 |
| Prim | dense | 1.0000 | 1.0903 | 1.0850 |
| Kosaraju | sparse | 1.0000 | - | 1.0000 |
| Kosaraju | dense | 1.0000 | - | 1.0000 |
| Kahn | sparse | 1.0000 | - | 1.0000 |
| Kahn | dense | 1.0000 | - | 1.0000 |
| DagShortestPath | sparse | 1.0000 | 1.0000 | 1.0000 |
| DagShortestPath | dense | 1.0000 | 1.0000 | 1.0000 |

Нормированные счётчики (в порядке n = 1000, 3162, 10000, 31623, 100000):

| Алгоритм / отношение | Плотность | Пять значений |
|---|---|---|
| Kruskal: comparisons / (m log₂ m) | sparse | 0.8914, 0.9069, 0.8692, 0.8091, 0.7447 |
| DSU внутри Kruskal: dsuOps / m | sparse | 2.2902, 2.5189, 2.6818, 2.7317, 2.7483 |
| Kruskal: comparisons / (m log₂ m) | dense | 0.9027, 0.8464, 0.7839, 0.7200, 0.6662 |
| DSU внутри Kruskal: dsuOps / m | dense | 0.5800, 0.6062, 0.5647, 0.7540, 0.8936 |
| Prim: comparisons / (m log₂ m) | sparse | 1.8291, 1.8654, 1.8952, 1.9210, 1.9344 |
| Prim: comparisons / (m log₂ m) | dense | 1.8248, 1.8468, 1.8657, 1.8779, 1.8896 |
| Kosaraju: edgeScans / (n + m) | sparse | 1.6000, 1.6000, 1.6000, 1.6000, 1.6000 |
| Kosaraju: edgeScans / (n + m) | dense | 1.9048, 1.9048, 1.9048, 1.9048, 1.9048 |
| Kahn: edgeScans / (n + m) | sparse | 0.8000, 0.8000, 0.8000, 0.8000, 0.8000 |
| Kahn: edgeScans / (n + m) | dense | 0.9524, 0.9524, 0.9524, 0.9524, 0.9524 |
| DagShortestPath: edgeScans / (n + m) | sparse | 0.8000, 0.8000, 0.8000, 0.8000, 0.8000 |
| DagShortestPath: edgeScans / (n + m) | dense | 0.9524, 0.9524, 0.9524, 0.9524, 0.9524 |

| Алгоритм | Время, минимум–максимум медиан (мс) |
|---|---:|
| Kruskal | 0.975–255.410 |
| Prim | 0.707–1021.307 |
| Kosaraju | 1.132–209.330 |
| Kahn | 0.261–38.201 |
| DagShortestPath | 0.573–73.971 |
