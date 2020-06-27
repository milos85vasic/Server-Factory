package net.milosvasic.factory.test

import net.milosvasic.factory.EMPTY
import net.milosvasic.factory.common.filesystem.FilePathBuilder
import net.milosvasic.factory.log
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import java.nio.file.InvalidPathException

class FilePathBuilderTest : BaseTest() {

    @Test
    fun testFilePathBuilder() {
        initLogging()
        log.i("File path builder test started")

        val validPaths = listOf(
                listOf("Test", "test", "test123"),
                listOf("123", "aaa,", "!@#$%^&*"),
                listOf(".", "test"),
                listOf("test", "."),
                listOf("..", "test"),
                listOf("test", "..")
        )

        val invalidPaths = listOf(
                listOf(" test", "test\n"),
                listOf("test", " test"),
                listOf(" test", "test"),
                listOf("test", "\ttest"),
                listOf("\ttest", "test"),
                listOf("."),
                listOf("..")
        )

        var valid = 0
        var invalid = 0

        fun buildPath(builder: FilePathBuilder) {

            try {
                val path = builder.build()
                if (path != String.EMPTY) {
                    log.v("Valid path: $path")
                    valid++
                } else {
                    invalid++
                }
            } catch (e: InvalidPathException) {

                invalid++
            }
        }

        fun getBuilder(elements: List<String>): FilePathBuilder {

            val builder = FilePathBuilder()
            elements.forEach { element ->

                builder.addContext(element)
            }
            return builder
        }

        validPaths.forEach {

            val builder = getBuilder(it)
            buildPath(builder)
        }

        invalidPaths.forEach {

            val builder = getBuilder(it)
            buildPath(builder)
        }

        Assertions.assertEquals(validPaths.size, valid)
        Assertions.assertEquals(invalidPaths.size, invalid)
        log.i("File path builder test completed")
    }
}