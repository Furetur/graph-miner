package graph.steps.lastReadWrite

import com.intellij.psi.PsiVariable
import com.intellij.psi.controlFlow.Instruction
import com.intellij.psi.controlFlow.ReadVariableInstruction
import com.intellij.psi.controlFlow.WriteVariableInstruction

class VarState(
    private val lastReads: Map<PsiVariable, Set<Offset>> = mapOf(),
    private val lastWrites: Map<PsiVariable, Set<Offset>> = mapOf()
) {
    companion object {
        val EMPTY = VarState()
    }

    fun getLastReads(variable: PsiVariable): Set<Offset> = lastReads[variable] ?: emptySet()

    fun getLastWrites(variable: PsiVariable): Set<Offset> = lastWrites[variable] ?: emptySet()

    fun nextStateAfterVisit(offset: Offset, instruction: Instruction): VarState = when (instruction) {
        is ReadVariableInstruction -> nextStateAfterNewRead(instruction.variable, offset)
        is WriteVariableInstruction -> nextStateAfterNewWrite(instruction.variable, offset)
        else -> this
    }

    private fun nextStateAfterNewRead(readVariable: PsiVariable, readInstructionOffset: Offset): VarState {
        val newLastReads = lastReads.cloneIntoMutable()
        val newLastWrites = lastWrites.cloneIntoMutable()
        newLastReads[readVariable] = mutableSetOf(readInstructionOffset)
        return VarState(newLastReads, newLastWrites)
    }

    private fun nextStateAfterNewWrite(writtenVariable: PsiVariable, writeInstructionOffset: Offset): VarState {
        val newLastReads = lastReads.cloneIntoMutable()
        val newLastWrites = lastWrites.cloneIntoMutable()
        newLastWrites[writtenVariable] = mutableSetOf(writeInstructionOffset)
        return VarState(newLastReads, newLastWrites)
    }

    /**
     * Merges two states mutating this state, the other state remains unchanged
     * @return true iff merge actually updated this state
     */
    fun merge(otherState: VarState): MergeResult {
        val newLastReads = lastReads.cloneIntoMutable()
        val newLastWrites = lastWrites.cloneIntoMutable()

        val didUpdateLastReads = mergeMaps(newLastReads, otherState.lastReads)
        val didUpdateLastWrites = mergeMaps(newLastWrites, otherState.lastWrites)
        return MergeResult(didUpdateLastReads || didUpdateLastWrites, VarState(newLastReads, newLastWrites))
    }

    data class MergeResult(val didChange: Boolean, val state: VarState)

    private fun mergeMaps(
        mutableMap: MutableMap<PsiVariable, MutableSet<Offset>>,
        otherMap: Map<PsiVariable, Set<Offset>>
    ): Boolean {
        var didUpdate = false
        for ((variable, otherOffsets) in otherMap) {
            val myOffsets = mutableMap.getOrPut(variable) { mutableSetOf() }
            val oldMyOffsetsCount = myOffsets.size
            myOffsets.addAll(otherOffsets)
            if (myOffsets.size > oldMyOffsetsCount) {
                didUpdate = true
            }
        }
        return didUpdate
    }

    private fun Map<PsiVariable, Set<Offset>>.cloneIntoMutable() =
        asSequence().associateTo(mutableMapOf()) { it.key to it.value.toMutableSet() }
}
