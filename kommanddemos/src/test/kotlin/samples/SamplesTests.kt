package pl.mareklangiewicz.kommand.samples

import org.junit.jupiter.api.TestFactory
import pl.mareklangiewicz.kommand.github.GhSamples
import pl.mareklangiewicz.kommand.line
import pl.mareklangiewicz.uspek.eq
import pl.mareklangiewicz.uspek.o
import pl.mareklangiewicz.uspek.uspekTestFactory

class SamplesTests {

    @TestFactory
    fun uspekSamplesTestFactory() = uspekTestFactory {
        testSampleTree(GhSamples)
    }
}

fun testSampleTree(tree: SampleTree) {
    "On tree ${tree.label}" o {
        for (node in tree.nodes) when (node) {
            is Sample -> testSample(node)
            is SampleTree -> testSampleTree(node)
        }
    }
}

fun testSample(sample: Sample) {
    "On sample ${sample.label}" o {
        "check kommand.line" o {
            val line = sample.kommand.line()
            if (sample.expectedLine == null) println("Expected line not provided. Actual kommand.line is: $line")
            else line eq sample.expectedLine
        }
    }
}
