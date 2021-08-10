import com.intellij.psi.PsiElement

typealias EdgeType = String

data class Edge(val from: PsiElement, val to: PsiElement) {
    override fun toString(): String = "$from -> $to"
}

data class Graph(val root: PsiElement, val edges: Map<EdgeType, Set<Edge>>) {
    override fun toString(): String = """
        Graph for $root
            edges:
        
    """.trimIndent() + edges.map { "\t\t${it.key}: ${it.value}" }.joinToString("\n")
}

class GraphBuilder(val root: PsiElement) {
    private val edges = mutableMapOf<EdgeType, MutableSet<Edge>>()

    fun graph() = Graph(root, edges)

    inner class EdgeBuilder(private val edgeType: EdgeType) {
        fun addEdge(from: PsiElement, to: PsiElement) {
            edges.getOrPut(edgeType) { mutableSetOf() }.add(Edge(from, to))
        }
    }
}

interface GraphBuildingStep {
    fun build(graphBuilder: GraphBuilder)
}
