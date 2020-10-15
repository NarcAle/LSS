import io.kotest.core.spec.style.FreeSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import org.gradle.internal.impldep.org.junit.rules.TemporaryFolder
import org.gradle.testkit.runner.GradleRunner
import org.gradle.testkit.runner.TaskOutcome
import java.io.File

class PluginTest : FreeSpec({
    val pluginClasspathResource = ClassLoader.getSystemClassLoader().getResource("plugin-classpath.txt")
            ?: throw IllegalStateException("Did not find the plugin classpath descriptor.")

    val classpath = pluginClasspathResource.openStream().bufferedReader().use { reader ->
        reader.readLines().map { File(it) }
    }

    val greetTask = ":greet" // Colon needed!

    fun projectSetup(content: String) = TemporaryFolder().apply{
        create()
        newFile("build.gradle.kts").writeText(content)
    }

    fun projectSetup(content: () -> String) = projectSetup(content())

    fun testSetup(buildFile: () -> String) = projectSetup(buildFile).let { testFolder ->
        GradleRunner.create()
                .withProjectDir(testFolder.root)
                .withPluginClasspath(classpath)
                .withArguments(greetTask)
    }

    /*
    * 1. Leggo il file plugin.classpath.txt
    * 2. Lo uso per creare una List<File>
    * 3. Crea cartella temporanea
    * 4. Crea dentro il file build.gradle.kts con il contenuto che ti viene dato in input
    * 5. Come argomento a gradle passa :greet
    */

    "running the plugin with" - {
        "default configuration should" - {
            val runner = testSetup {
                """
                    plugins {
                        id("it.unibo.lss2020.greetings-plugin")
                    }
                """.trimIndent()
            }.build()
            "run the greet task" {
                runner.task(greetTask)?.outcome shouldBe TaskOutcome.SUCCESS
            }
            "print an hello message" {
                runner.output shouldContain "Hello from Gradle"
            }
        }
        "a message in Italian" - {
            val runner = testSetup {
                """
                    plugins {
                        id("it.unibo.lss2020.greetings-plugin")
                    }
                    greetings {
                        greetWith { "Ciao da" }
                    }
                """.trimIndent()
            }.build()
            "run the greet task" {
                runner.task(greetTask)?.outcome shouldBe TaskOutcome.SUCCESS
            }
            "print an hello message" {
                runner.output shouldContain "Ciao da Gradle"
            }
        }
    }
})


