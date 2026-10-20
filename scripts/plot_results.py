#!/usr/bin/env python3
"""Графики и численный анализ настоящего CSV. Формулы не заменяют счётчики."""
import csv
import math
from pathlib import Path
import matplotlib
matplotlib.use("Agg")
import matplotlib.pyplot as plt

ROOT = Path(__file__).resolve().parents[1]
with (ROOT / "results/results.csv").open() as source:
    rows = list(csv.DictReader(source))
assert len(rows) == 50, "Для отчёта требуется полный benchmark: 50 строк"
for row in rows:
    for field in ("n", "m", "dsu_ops", "edge_scans", "comparisons", "max_depth", "result"):
        row[field] = int(row[field])
    row["time_ms"] = float(row["time_ms"])
    row["operations"] = row["dsu_ops"] + row["edge_scans"] + row["comparisons"]

ALGORITHMS = ["Kruskal", "Prim", "Kosaraju", "Kahn", "DagShortestPath"]
COLORS = ["#2563eb", "#e67e22", "#159570", "#ab47bc", "#d63e54"]
MARKERS = ["o", "s", "^", "D", "x"]
OUT = ROOT / "results/plots"
OUT.mkdir(parents=True, exist_ok=True)
plt.rcParams.update({"font.family": "DejaVu Sans", "font.size": 10, "axes.spines.top": False, "axes.spines.right": False})

def select(algorithm, density):
    return sorted((r for r in rows if r["algorithm"] == algorithm and r["graph"].endswith("_" + density)), key=lambda r: r["n"])

def slope(data, field):
    x = [math.log(r["n"]) for r in data]
    y = [math.log(r[field]) for r in data]
    mx, my = sum(x)/len(x), sum(y)/len(y)
    return sum((a-mx)*(b-my) for a,b in zip(x,y)) / sum((a-mx)**2 for a in x)

for filename, title, field in [("operations.png", "Учтённые операции: DSU + просмотры + сравнения", "operations"),
                               ("scans_per_edge.png", "Просмотры рёбер / число логических рёбер", "ratio")]:
    fig, axes = plt.subplots(1, 2, figsize=(12, 4.8), layout="constrained")
    for ax, density, factor in zip(axes, ["sparse", "dense"], [4, 20]):
        for algorithm, color, marker in zip(ALGORITHMS, COLORS, MARKERS):
            data = select(algorithm, density)
            y = [r[field] if field != "ratio" else r["edge_scans"]/r["m"] for r in data]
            ax.plot([r["n"] for r in data], y, marker=marker, color=color,
                    linestyle="--" if algorithm in ("Kosaraju", "DagShortestPath") else "-", label=algorithm, alpha=0.85)
        ax.set_xscale("log")
        if field == "operations": ax.set_yscale("log")
        else: ax.set_ylim(0, 2.2)
        ax.set_title(f"{density}: m = {factor}n")
        ax.set_xlabel("Число вершин n")
        ax.set_ylabel("Сумма счётчиков" if field == "operations" else "edgeScans / m")
        ax.grid(True, which="both", alpha=0.2)
    handles, labels = axes[0].get_legend_handles_labels()
    fig.legend(handles, labels, loc="outside lower center", ncol=5, fontsize=9)
    fig.suptitle(title, fontsize=14)
    fig.savefig(OUT / filename, dpi=180)
    plt.close(fig)

fig, ax = plt.subplots(figsize=(9, 4.5), layout="constrained")
for density, color in zip(["sparse", "dense"], COLORS):
    data = select("Kruskal", density)
    ax.plot([r["n"] for r in data], [r["edge_scans"]/r["m"] for r in data], "o-", label=density, color=color)
ax.set(xscale="log", ylim=(0, 1.05), xlabel="Число вершин n", ylabel="edgeScans / m", title="Kruskal: доля рёбер, просмотренных после сортировки")
ax.grid(True, which="both", alpha=0.2)
ax.legend()
fig.savefig(OUT / "kruskal_share.png", dpi=180)
plt.close(fig)

lines = ["# Численный анализ CSV", "", "Наклон: линейная регрессия log(счётчик) по log(n), пять точек отдельно для каждой плотности.", "",
         "| Алгоритм | Плотность | Наклон scans | Наклон comparisons | Наклон суммы операций |", "|---|---|---:|---:|---:|"]
for algorithm in ALGORITHMS:
    for density in ("sparse", "dense"):
        data = select(algorithm, density)
        comparison = f"{slope(data, 'comparisons'):.4f}" if all(r['comparisons'] > 0 for r in data) else "-"
        lines.append(f"| {algorithm} | {density} | {slope(data, 'edge_scans'):.4f} | {comparison} | {slope(data, 'operations'):.4f} |")
lines += ["", "Нормированные счётчики (в порядке n = 1000, 3162, 10000, 31623, 100000):", "", "| Алгоритм / отношение | Плотность | Пять значений |", "|---|---|---|"]
for algorithm in ALGORITHMS:
    for density in ("sparse", "dense"):
        data = select(algorithm, density)
        if algorithm in ("Kruskal", "Prim"):
            values = [r['comparisons']/(r['m']*math.log2(r['m'])) for r in data]
            formula = "comparisons / (m log₂ m)"
        else:
            values = [r['edge_scans']/(r['n']+r['m']) for r in data]
            formula = "edgeScans / (n + m)"
        lines.append(f"| {algorithm}: {formula} | {density} | " + ", ".join(f"{v:.4f}" for v in values) + " |")
        if algorithm == "Kruskal":
            values = [r['dsu_ops']/r['m'] for r in data]
            lines.append(f"| DSU внутри Kruskal: dsuOps / m | {density} | " + ", ".join(f"{v:.4f}" for v in values) + " |")
lines += ["", "| Алгоритм | Время, минимум–максимум медиан (мс) |", "|---|---:|"]
for algorithm in ALGORITHMS:
    values = [r['time_ms'] for r in rows if r['algorithm'] == algorithm]
    lines.append(f"| {algorithm} | {min(values):.3f}–{max(values):.3f} |")
(ROOT / "results/analysis.md").write_text("\n".join(lines) + "\n", encoding="utf-8")
print("Созданы 3 PNG и results/analysis.md")
