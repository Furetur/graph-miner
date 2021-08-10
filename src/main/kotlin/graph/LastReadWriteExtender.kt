import com.intellij.psi.PsiMethod
import com.intellij.psi.controlFlow.*
import java.util.*

object LastReadWriteStep : GraphBuildingStep {
    override fun build(graphBuilder: GraphBuilder) {
        val psiElement = graphBuilder.root
        require(psiElement is PsiMethod)
        val controlFlow = psiElement.controlFlow

    }
}




private val PsiMethod.controlFlow: ControlFlow
    get() = ControlFlowFactory.getInstance(project).getControlFlow(
        body!!,
        AllVariablesControlFlowPolicy.getInstance()
    )
