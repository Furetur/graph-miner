package graph.steps.lastReadWrite

import Edge
import GraphBuilder
import GraphBuildingStep
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiMethod
import com.intellij.psi.controlFlow.AllVariablesControlFlowPolicy
import com.intellij.psi.controlFlow.ControlFlow
import com.intellij.psi.controlFlow.ControlFlowFactory
import com.intellij.psi.util.descendantsOfType

object LastReadWriteStep : GraphBuildingStep {
    override fun build(graphBuilder: GraphBuilder) {
        graphBuilder.root.descendantsOfType<PsiMethod>()
            .forEach { psiMethod -> buildForMethod(psiMethod, graphBuilder) }
    }

    private fun buildForMethod(psiMethod: PsiMethod, graphBuilder: GraphBuilder) {
        val methodName = "${graphBuilder.root}/$psiMethod"
        println("Building graph for method $methodName")
        val controlFlow = psiMethod.body?.controlFlow ?: return

        println("Extracted control flow for method $methodName:\n$controlFlow")

        val (lastReadEdges, lastWriteEdges) = LastReadWriteMiner(controlFlow.instructions).build()
        addEdges("LastRead", lastReadEdges, graphBuilder, controlFlow)
        addEdges("LastWrite", lastWriteEdges, graphBuilder, controlFlow)
    }

    private fun addEdges(
        edgeType: String,
        edges: Set<Edge<Offset>>,
        graphBuilder: GraphBuilder,
        controlFlow: ControlFlow
    ) {
        for (edge in edges) {
            val from = controlFlow.getElement(edge.from)
            val to = controlFlow.getElement(edge.to)
            graphBuilder.addEdge(edgeType, from, to)
        }
    }

    private val PsiElement.controlFlow: ControlFlow
        get() = ControlFlowFactory.getInstance(project).getControlFlow(
            this,
            AllVariablesControlFlowPolicy.getInstance()
        )
}

