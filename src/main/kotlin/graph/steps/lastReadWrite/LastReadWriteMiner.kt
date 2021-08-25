package graph.steps.lastReadWrite

import Edge
import com.intellij.psi.PsiVariable
import com.intellij.psi.controlFlow.Instruction
import com.intellij.psi.controlFlow.ReadVariableInstruction
import com.intellij.psi.controlFlow.WriteVariableInstruction
import java.util.*

typealias Offset = Int

data class LastReadWriteEdges(val lastReadEdges: Set<Edge<Offset>>, val lastWriteEdges: Set<Edge<Offset>>)

class LastReadWriteMiner(instructions: List<Instruction>) {
    private val nodes = instructions.withIndex().map { (offset, instruction) -> DataflowNode(offset, instruction) }

    private val queue = PriorityQueue<DataflowNode>().apply {
        nodes.firstOrNull()?.let { add(it) }
    }

    fun build(): LastReadWriteEdges {
        calculateAllVarStates()
        return calculateEdgesFromStates()
    }

    private fun calculateEdgesFromStates(): LastReadWriteEdges {
        val lastReadEdges = mutableSetOf<Edge<Offset>>()
        val lastWriteEdges = mutableSetOf<Edge<Offset>>()

        for (node in nodes) {
            val variable = node.variable ?: continue
            node.stateJustBeforeVisit?.getLastReads(variable)
                ?.forEach { lastReadOffset ->
                    lastReadEdges.add(
                        Edge(
                            node.offset,
                            lastReadOffset
                        )
                    )
                }
            node.stateJustBeforeVisit?.getLastWrites(variable)
                ?.forEach { lastWriteOffset ->
                    lastWriteEdges.add(
                        Edge(
                            node.offset,
                            lastWriteOffset
                        )
                    )
                }
        }
        return LastReadWriteEdges(lastReadEdges, lastWriteEdges)
    }

    private fun calculateAllVarStates() {
        while (queue.isNotEmpty()) {
            val currentNode = queue.poll()
            if (currentNode.shouldBeVisited) {
                visitNode(currentNode)
            }
        }
    }

    private fun visitNode(node: DataflowNode) {
        val stateAfterPreviousVisit = node.stateJustAfterVisit
        node.nextOffsets
            .filter { nextOffset -> nextOffset in nodes.indices }
            .forEach { nextOffset ->
                val nextNode = nodes[nextOffset]
                nextNode.updateStateJustBeforeVisit(stateAfterPreviousVisit)
                queue.add(nextNode)
            }

    }
}

private class DataflowNode(
    val offset: Offset,
    val instruction: Instruction,
) : Comparable<DataflowNode> {
    var shouldBeVisited: Boolean = true
        private set
    var stateJustBeforeVisit: VarState? = null
        private set
    val stateJustAfterVisit
        get() = (stateJustBeforeVisit ?: VarState.EMPTY).nextStateAfterVisit(offset, instruction)
    val nextOffsets: List<Offset>
        get() = (0 until instruction.nNext()).map { i -> instruction.getNext(offset, i) }
    val variable: PsiVariable?
        get() = when (instruction) {
            is ReadVariableInstruction -> instruction.variable
            is WriteVariableInstruction -> instruction.variable
            else -> null
        }

    fun updateStateJustBeforeVisit(stateAfterPreviousNode: VarState) {
        val previousStateBeforeVisit = stateJustBeforeVisit
        if (previousStateBeforeVisit == null) {
            stateJustBeforeVisit = stateAfterPreviousNode
            shouldBeVisited = true
        } else {
            val (didStateUpdate, newState) = previousStateBeforeVisit.merge(stateAfterPreviousNode)
            shouldBeVisited = didStateUpdate
            stateJustBeforeVisit = newState
        }
    }

    /**
     * Trivially compares offsets:
     * node1 < node2 iff node1.offset < node2.offset
     * node1 == node2 iff node1.offset == node2.offset
     * node1 > node2 iff node1.offset > node2.offset
     */
    override fun compareTo(other: DataflowNode): Int = offset.compareTo(other.offset)

    override fun toString(): String = instruction.toString()
}
