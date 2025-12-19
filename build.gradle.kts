plugins {
    kotlin("jvm") version "2.2.20"
    application
}

group = "com.dsw.crawler"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(kotlin("test"))
    implementation("org.ktorm:ktorm-core:3.6.0")
    implementation("org.xerial:sqlite-jdbc:3.45.1.0")
    implementation("org.jsoup:jsoup:1.17.2")
    implementation("org.seleniumhq.selenium:selenium-java:4.39.0")
    implementation("org.seleniumhq.selenium:selenium-devtools-v143:4.39.0")
}

tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(21)
}
application {
    mainClass.set("com.dsw.crawler.WebCrawlerKt")
}
