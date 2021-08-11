package graph.steps.returnsTo

import GraphBuilder
import GraphBuildingStep
import com.intellij.psi.JavaRecursiveElementVisitor
import com.intellij.psi.PsiMethod
import com.intellij.psi.PsiReturnStatement
import org.jetbrains.kotlin.backend.common.peek
import java.util.*

object ReturnsToStep : GraphBuildingStep {
    override fun build(graphBuilder: GraphBuilder) {
        graphBuilder.root.accept(ReturnsToMiner(graphBuilder))
    }
}

class ReturnsToMiner(private val graphBuilder: GraphBuilder) : JavaRecursiveElementVisitor() {
    private val enteredMethods = Stack<PsiMethod>()
    override fun visitMethod(method: PsiMethod?) {
        if (method == null) {
            return
        }
        enteredMethods.push(method)
        super.visitMethod(method)
        enteredMethods.pop()
    }

    override fun visitReturnStatement(statement: PsiReturnStatement?) {
        if (statement != null && enteredMethods.isNotEmpty()) {
            graphBuilder.addEdge("ReturnsTo", statement, enteredMethods.peek())
        }
        super.visitReturnStatement(statement)
    }
}

