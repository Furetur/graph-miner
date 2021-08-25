package graph.steps.computedFrom

import GraphBuilder
import GraphBuildingStep
import com.intellij.psi.*
import java.util.*

object ComputedFromStep : GraphBuildingStep {
    override fun build(graphBuilder: GraphBuilder) {
        val miner = ComputedFromMiner(graphBuilder)
        graphBuilder.root.accept(miner)
    }
}

class ComputedFromMiner(private val graphBuilder: GraphBuilder) : JavaRecursiveElementVisitor(), PsiRecursiveVisitor {
    private val currentAssignments = Stack<PsiElement>()

    override fun visitAssignmentExpression(expression: PsiAssignmentExpression?) {
        val referenceExpression = expression?.lExpression as? PsiReferenceExpression
        if (referenceExpression != null) {
            currentAssignments.add(referenceExpression)
            expression.rExpression?.accept(this)
            currentAssignments.pop()
        } else {
            // if we failed we just continue visiting
            expression?.rExpression?.accept(this)
        }
    }

    override fun visitVariable(variable: PsiVariable?) {
        val nameIdentifier = variable?.nameIdentifier
        val initializer = variable?.initializer

        if (nameIdentifier != null && initializer != null) {
            currentAssignments.add(nameIdentifier)
            initializer.accept(this)
            currentAssignments.pop()
        } else {
            // if we failed we just continue visiting
            initializer?.accept(this)
        }
    }

    override fun visitIdentifier(identifier: PsiIdentifier?) {
        identifier?.let { addEdgesToIdentifier(it) }
    }

    private fun addEdgesToIdentifier(identifier: PsiIdentifier) {
        for (assignment in currentAssignments) {
            graphBuilder.addEdge("ComputedFrom", assignment, identifier)
        }
    }
}
