# graph-miner


<!-- Plugin description -->
Extracts graphs from Java code
<!-- Plugin description end -->

## How to run

1. Clone this repository
2. Get absolute path to `src/main/resources/data`, we will call it `<abspath>`
3. Run `./gradlew runIde -Pinput=<abspath>`

## Output

This plugin builds a `Graph` object that you will see in _stdout_.

Each edge is a triple `(edgeType, vertex1, vertex2)` that is represented by `vertex1 -> vertex2`.

**Example**

For instance, this output corresponds to a graph that has 1 edge of type LastRead from vertex a to b.

```
Graph for ...
    LastRead: [a -> b]
```
