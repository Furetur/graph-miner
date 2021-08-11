package graph.steps.nextToken

import GraphBuilder
import GraphBuildingStep
import com.intellij.psi.JavaRecursiveElementVisitor
import com.intellij.psi.PsiJavaToken

object NextTokenStep : GraphBuildingStep {
    override fun build(graphBuilder: GraphBuilder) {
        graphBuilder.root.accept(NextTokenMiner(graphBuilder))
    }
}

private class NextTokenMiner(private val graphBuilder: GraphBuilder) : JavaRecursiveElementVisitor() {
    private var prevJavaToken: PsiJavaToken? = null

    override fun visitJavaToken(token: PsiJavaToken?) {
        if (token == null) {
            return
        }
        prevJavaToken?.let { prevJavaToken ->
            graphBuilder.addEdge("NextToken", prevJavaToken, token)
        }
        prevJavaToken = token
    }
}
