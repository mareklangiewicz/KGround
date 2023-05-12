package pl.mareklangiewicz.kommand.samples

import pl.mareklangiewicz.kommand.Kommand
import pl.mareklangiewicz.kommand.line

sealed interface SampleNode {
    val label: String
}

data class Sample(
    val kommand: Kommand,
    override val label: String = kommand.line(),
    val expectedLine: String? = null,
) : SampleNode, Kommand by kommand

data class SampleTree(override val label: String, val nodes: List<SampleNode>) : SampleNode

internal infix fun Kommand.s(expectedLine: String) = Sample(this, expectedLine)

internal infix fun Sample.l(label: String) = copy(label = label)

internal fun String.st(vararg nodes: SampleNode) = SampleTree(this, nodes.toList())
