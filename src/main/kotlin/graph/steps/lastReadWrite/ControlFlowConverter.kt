package graph

import CondGotoInstruction
import FlowInstruction
import GotoInstruction
import Offset
import ReadInstruction
import UnknownInstruction
import WriteInstruction
import com.intellij.psi.PsiElement
import com.intellij.psi.controlFlow.*

class A : ControlFlowInstructionVisitor() {
    override fun visitReadVariableInstruction(instruction: ReadVariableInstruction?, offset: Int, nextOffset: Int) {
        super.visitReadVariableInstruction(instruction, offset, nextOffset)
    }
}

class ControlFlowConverter {
    fun convert(controlFlow: ControlFlowInstructionVisitor): List<FlowInstruction> {

    }


    private fun convert(instruction: Instruction, offset: Offset, psiElement: PsiElement): FlowInstruction =
        with(instruction) {
            when (this) {
                is WriteVariableInstruction -> WriteInstruction(offset, psiElement, variable)
                is ReadVariableInstruction -> ReadInstruction(offset, psiElement, variable)
                is GoToInstruction -> GotoInstruction(offset, psiElement, this.offset)
                is ThrowToInstruction -> GotoInstruction(offset, psiElement, this.offset)
                is ConditionalBranchingInstruction -> CondGotoInstruction(offset, psiElement, this.offset)
                else -> UnknownInstruction(offset, psiElement)
            }
        }


}
