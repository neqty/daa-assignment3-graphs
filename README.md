# Assignment 3: графы — DSU, MST, SCC и кратчайшие пути

Java-проект для сценария «городские дороги и планировщик задач». Все обязательные алгоритмы реализованы самостоятельно; коллекции и сортировка взяты из стандартной библиотеки. Пояснения и отчёт — на русском.

## Требования

- JDK 17 или новее. Проект проверен локально на JDK 25.0.1, компиляция нацелена на Java 17; CI настроен на Java 17.
- Maven устанавливать отдельно не нужно: в проекте есть Maven Wrapper 3.9.9. При первом запуске требуется интернет для скачивания Maven и JUnit.
- Python 3.10+ и matplotlib нужны только для повторного построения PNG. Готовые изображения уже лежат в `results/plots/`.

## Открытие в IntelliJ IDEA

1. **File → Open** → выбрать `pom.xml` этой папки → открыть как проект.
2. Дождаться импорта Maven. В **Project Structure → Project SDK** выбрать JDK 17+.
3. Открыть `benchmark.Demo` или `benchmark.Benchmark`, нажать зелёную стрелку рядом с `main`.
4. Для `Demo` задать Program arguments, например `data/dag.txt`. Working directory — корень проекта.
5. Для полного `Benchmark` добавить VM options `-Xmx3g`. Для быстрого запуска — Program arguments `--quick`.
6. Тесты доступны через `src/test/java` или Maven → Lifecycle → test.

## Сборка и тесты

macOS / Linux:

```sh
./mvnw clean verify
```

Windows PowerShell:

```powershell
.\mvnw.cmd clean verify
```

## Небольшие примеры для защиты

После сборки:

```sh
java -cp target/classes benchmark.Demo data/connected.txt
java -cp target/classes benchmark.Demo data/disconnected.txt
java -cp target/classes benchmark.Demo data/cyclic.txt
java -cp target/classes benchmark.Demo data/dag.txt
```

Ожидания: связный граф — вес MST 6; несвязный — вес леса 8 и 3 компоненты; циклический — 3 SCC и сообщение о цикле; DAG — расстояния `[0, 0, -2, -3, 2, -2]`, путь к 5: `[1, 4, 3, 5]`.

## Benchmark

```sh
java -Xmx3g -jar target/assignment3-graphs-1.0.jar
```

Пять размеров × две плотности × пять алгоритмов = **50 строк данных** в `results/results.csv`. Для каждой строки выполняется 5 запусков, берётся медиана времени. Генерация графа и проверка результата не входят в измеряемый интервал. Предварительный прогрев выполняется на маленьких графах.

Каждый запуск проверяет совпадение Kruskal/Prim, топологический порядок, контрольные результаты из задания и точные счётчики обходов. Ошибка завершает программу исключением; CSV записывается только после успешных проверок всего набора.

Быстрая проверка двух плотностей при n=1000:

```sh
java -jar target/assignment3-graphs-1.0.jar --quick
```

Она сохраняет отдельный `results/quick-results.csv`, не заменяя полный CSV.

### Повторить измерения и построить графики одной командой

Однократная установка зависимостей для рисунков:

```sh
python3 -m venv .venv
.venv/bin/python -m pip install -r scripts/requirements.txt
```

Далее:

```sh
PYTHON=.venv/bin/python ./scripts/reproduce.sh
```

Скрипт запускает тесты, полный benchmark, создаёт три PNG и `results/analysis.md`. Измеренные времена зависят от машины. Новые значения для статического `REPORT.md` следует брать из обновлённого `results/analysis.md`.

## Структура

```text
src/main/java/graph/       общий граф и чтение файлов
src/main/java/algorithms/  DSU, Kruskal, Prim, Kosaraju, Kahn, DAG-пути
src/main/java/metrics/     счётчики операций
src/main/java/benchmark/   точный генератор, benchmark и демонстрация
src/test/java/tests/      JUnit 5, независимые эталоны и граничные случаи
data/                     четыре небольших графа
results/results.csv       50 строк фактических измерений
results/plots/            три PNG
results/analysis.md       нормированные счётчики и log-log наклоны
REPORT.md                 отчёт по требованиям задания
DEFENSE.md                короткие ответы и порядок демонстрации
```

## Правила измерения

- `dsu_ops = findCalls + successfulUnions`. Вызовы `find` внутри `union` учитываются. Неуспешный `union` не увеличивает `successfulUnions`.
- `edge_scans`: Kruskal — рёбра, рассмотренные **после** сортировки; Prim — записи списков смежности (2m); Kosaraju — два DFS (2m); Kahn — обработка очереди (m для DAG); DAG shortest paths — проход релаксаций (m).
- `comparisons`: сравнения весов при сортировке/в очереди, а для DAG — сравнения кандидата с текущим расстоянием.
- `max_depth`: максимальное число элементов в явном DFS-стеке. Для алгоритмов без такого стека — 0.
- У Kosaraju создание обратного графа входит во время и память, но не в счётчик двух DFS. У DAG-путей вычисление топологического порядка входит во время, а его просмотры считаются отдельно от релаксаций. Это соответствует требуемым 2m/m/m.
- Точное число сравнений библиотечной сортировки/очереди связано с их реализацией; при одинаковой JDK и входе оно воспроизводится. Контрольные результаты и точные счётчики обходов от версии JDK не зависят.

## Git

Ветки: `main`, `feature/dsu`, `feature/mst`, `feature/scc`, `feature/paths`, `feature/metrics`. Слияния fast-forward сохраняют последовательную историю из **12 содержательных коммитов**. Версия для сдачи — тег `v1.0`.

Репозиторий: https://github.com/neqty/daa-assignment3-graphs

Исходный PDF задания не включён в публичный репозиторий. ZIP можно получить через GitHub → Code → Download ZIP либо командой `git archive`. Перед загрузкой в Moodle архив нужно назвать `DAA_Assignment3_name_surname_group.zip`, подставив свои данные.

## Использование AI

При разработке использован OpenAI Codex: помощь с реализацией алгоритмов, тестами, benchmark, графиками и русскими пояснениями. Проверки выполнены автоматически; это не означает, что студент уже самостоятельно разобрал каждую строку. Перед защитой необходимо изучить код, самостоятельно запустить примеры и уметь объяснить инварианты, сложность и границы применимости алгоритмов. Подготовка к этому собрана в `DEFENSE.md`.
