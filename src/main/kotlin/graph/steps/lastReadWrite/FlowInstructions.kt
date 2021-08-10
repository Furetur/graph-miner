package graph.steps.lastReadWrite

import com.intellij.psi.PsiVariable

typealias Offset = Int

sealed class FlowInstruction {
    abstract val offset: Offset
    abstract val next: List<Offset>

    abstract fun nextState(prevState: VarState): VarState

    protected abstract fun toInstructionString(): String
    override fun toString(): String = "$offset: ${toInstructionString()}"
}

abstract class LinearInstruction(myOffset: Offset) : FlowInstruction() {
    override val next: List<Offset> = listOf(myOffset + 1)
}

class ReadInstruction(override val offset: Offset, val variable: PsiVariable) :
    LinearInstruction(offset) {
    override fun nextState(prevState: VarState): VarState = prevState.nextAfterNewRead(variable, this)
    override fun toInstructionString(): String = "READ $variable"
}

class WriteInstruction(override val offset: Offset, val variable: PsiVariable) :
    LinearInstruction(offset) {
    override fun nextState(prevState: VarState): VarState = prevState.nextAfterNewWrite(variable, this)
    override fun toInstructionString(): String = "WRITE $variable"
}

class GotoInstruction(override val offset: Offset, val target: Offset) :
    FlowInstruction() {
    override val next: List<Offset> = listOf(target)
    override fun nextState(prevState: VarState): VarState = prevState
    override fun toInstructionString(): String = "GOTO $target"
}

class CondGotoInstruction(override val offset: Offset, val target: Offset) :
    FlowInstruction() {
    override val next: List<Offset> = listOf(offset + 1, target)
    override fun nextState(prevState: VarState): VarState = prevState
    override fun toInstructionString(): String = "COND_GOTO $target "
}

class UnknownInstruction(override val offset: Offset) : LinearInstruction(offset) {
    override fun nextState(prevState: VarState): VarState = prevState
    override fun toInstructionString(): String = "UNKNOWN"
}
