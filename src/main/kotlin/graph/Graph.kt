import com.intellij.psi.PsiElement

data class Edge<V>(val from: V, val to: V) {
    override fun toString(): String = "$from -> $to"
}

private typealias EdgeType = String

data class Graph(val root: PsiElement, val edges: Map<EdgeType, Set<Edge<PsiElement>>>) {
    override fun toString(): String = "Graph for $root\n" +
            edges.map { "\t${it.key}: ${it.value}" }.joinToString("\n")
}

class GraphBuilder(val root: PsiElement) {
    private val edges = mutableMapOf<EdgeType, MutableSet<Edge<PsiElement>>>()

    fun graph() = Graph(root, edges)

    fun addEdge(edgeType: EdgeType, from: PsiElement, to: PsiElement) {
        edges.getOrPut(edgeType) { mutableSetOf() }.add(Edge(from, to))
    }
}

interface GraphBuildingStep {
    fun build(graphBuilder: GraphBuilder)
}
