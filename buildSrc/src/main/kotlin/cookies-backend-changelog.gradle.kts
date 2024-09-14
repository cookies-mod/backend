import org.eclipse.jgit.api.Git
import kotlin.io.path.readText

tasks {
    val changelogTask = register("createChangelog")
    changelogTask.configure {
        group = "cookies"
        enabled = true
        doLast {
            val git = Git.open(rootDir)

            val currentTag = git.tagList().call().last()
            val previousTag = git.tagList().call().let { it[it.size - 1] }
            val gitHistory = git.log().addRange(previousTag.objectId, currentTag.objectId).call()
            val changeLogBuilder = StringBuilder()
            val changeLogHeader = rootDir.toPath().resolve("gradle/CHANGELOG_HEADER.md").readText(Charsets.UTF_8)

            changeLogBuilder.append(applyPlaceholders(changeLogHeader))

            gitHistory.forEach {
                changeLogBuilder.append(it.name).append(" - @").append(it.committerIdent.name).append("\n")
            }

            rootDir.resolve("CHANGELOG.md").writeText(changeLogBuilder.toString())
        }
    }
}

fun applyPlaceholders(text: String): String {
    return text.replace("\$version$", rootProject.version.toString())
        .replace("\$projectName$", rootProject.name)
}