package it.unibo.lss.firstplugin
import org.gradle.api.DefaultTask
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.TaskAction
//Thanks to gradleApi() method into the build.gradle.kts file:

/*Plugin*/
open class GreetingPlugin: Plugin<Project> {
    //plugin apply plugins
    override fun apply(target: Project) {
        //Extensions are created via a Project object
        val extension = target.extensions.create("greetings", GreetingExtension::class.java, target)

        target.tasks.register("greet", Greet::class.java) {
            // Set the default greeting to be the one configured in the extension
            it.greetIntro.set(extension.defaultGreeting) //ho agganciato il provider all'altro.
            // Configuration per-task can still be changed manually by users
        }
    }
}

/*Extension -> Extensions can be seen as global configuration containers*/
open class GreetingExtension(val project: Project) {
    val defaultGreeting: Property<String> = project.objects.property(String::class.java)
            .apply { convention("Hello from") } // Set a conventional value. Convention -> method inside property

    // A DSL would go there
    fun greetWith(greeting: () -> String) = defaultGreeting.set(greeting())
    operator fun String.invoke() = defaultGreeting.set(this)
}

/*Task*/
open class Greet: DefaultTask() {
    @Input
    //var greetIntro: String = "Hello from"
    var greetIntro: Property<String> = project.objects.property<String>(String::class.java) // Lazy property creation

    @Internal // Read-only property calculated from `greeting`
    val message: Provider<String> = /*project.objects.property(String::class.java)
            .also { it.set ("$greeting Gradle")}*/
            greetIntro.map { "$it Gradle" }

    // In order to make this action executable when task is invoked, we have to use marker
    @TaskAction
    fun greet() {
        println("$greetIntro Gradle")
    }
}

/* build gradle
* greetings{ this: it.unibo.lss.firstplugin.GreetingExtension
*   //greetWith { "ciao" }
*   'ciao' {}
* }
* //ciao{} //errore perche this non è it.unibo.lss.firstplugin.GreetingExtension
* */