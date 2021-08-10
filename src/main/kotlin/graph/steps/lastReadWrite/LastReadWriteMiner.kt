package graph.steps.lastReadWrite

import com.intellij.psi.PsiVariable
import com.intellij.psi.controlFlow.Instruction
import com.intellij.psi.controlFlow.ReadVariableInstruction
import com.intellij.psi.controlFlow.WriteVariableInstruction
import java.util.*

data class FlowEdge(val from: Offset, val to: Offset)

data class LastReadWriteEdges(val lastReadEdges: Set<FlowEdge>, val lastWriteEdges: Set<FlowEdge>)

class LastReadWriteMiner(instructions: List<Instruction>) {
    private val nodes = instructions.withIndex().map { (offset, instruction) -> DataflowNode(offset, instruction) }

    init {
        require(instructions.isNotEmpty()) { "List of nodes must not be empty" }
    }

    private val queue = PriorityQueue<DataflowNode>().apply {
        add(nodes.first())
    }

    fun build(): LastReadWriteEdges {
        calculateAllVarStates()
        return calculateEdgesFromStates()
    }

    private fun calculateEdgesFromStates(): LastReadWriteEdges {
        val lastReadEdges = mutableSetOf<FlowEdge>()
        val lastWriteEdges = mutableSetOf<FlowEdge>()

        for (node in nodes) {
            val variable = node.touchedVariable ?: continue
            node.stateJustBeforeVisit?.getLastReads(variable)
                ?.forEach { lastReadOffset ->
                    lastReadEdges.add(
                        FlowEdge(
                            node.offset,
                            lastReadOffset
                        )
                    )
                }
            node.stateJustBeforeVisit?.getLastWrites(variable)
                ?.forEach { lastWriteOffset ->
                    lastWriteEdges.add(
                        FlowEdge(
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
        for (nextOffset in node.nextOffsets) {
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
    val touchedVariable: PsiVariable?
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
