tasks {
    val register = register("version")
    register.configure {
        group = "cookies"
        enabled = true
        doLast {
            rootDir.resolve("version.txt").writeText("${rootProject.version}")
        }
    }
}