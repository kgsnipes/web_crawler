package com.dsw.crawler

import org.ktorm.database.Database
import org.ktorm.dsl.*
import org.ktorm.schema.*
import org.openqa.selenium.WebDriver
import org.openqa.selenium.chrome.ChromeDriver
import org.openqa.selenium.By
import org.openqa.selenium.support.ui.WebDriverWait
import org.openqa.selenium.support.ui.ExpectedConditions
import java.time.Duration

object Links : Table<Nothing>("links") {
    val id = int("id").primaryKey()
    val parentUrl = varchar("parent_url")
    val childUrl = varchar("child_url")
}

fun initDatabase(db: Database) {
    db.useConnection { conn ->
        val stmt = conn.createStatement()
        stmt.executeUpdate(
            """
            CREATE TABLE IF NOT EXISTS links (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                parent_url TEXT NOT NULL,
                child_url TEXT NOT NULL
            )
            """
        )
        stmt.close()
    }
}

fun crawl(db: Database, parentUrl: String, depth: Int = 10, driver: WebDriver? = null) {
    if (depth == 0) return
    val alreadyCrawled = db.from(Links)
        .select()
        .where { Links.parentUrl eq parentUrl }
        .totalRecordsInAllPages > 0
    if (alreadyCrawled) return
    val localDriver = driver ?: ChromeDriver()
    try {

        localDriver.get(parentUrl)
        // Wait for links to be present (up to 10 seconds)
        WebDriverWait(localDriver, Duration.ofSeconds(10)).until(
            ExpectedConditions.presenceOfElementLocated(By.tagName("a"))
        )
        val links = localDriver.findElements(By.tagName("a"))
        for (link in links) {
            val childUrl = link.getAttribute("href") ?: ""
            if (childUrl.isNotBlank()) {
                db.insert(Links) {
                    set(Links.parentUrl, parentUrl)
                    set(Links.childUrl, childUrl)
                }
                crawl(db, childUrl, depth - 1, localDriver)
            }
        }
    } catch (e: Exception) {
        println("Failed to crawl $parentUrl: ${e.message}")
    } finally {
        if (driver == null) {
            println("Closing local driver for $parentUrl as the driver is null")
            localDriver.quit()
        }
    }
}

fun main(args: Array<String>) {
    if (args.isEmpty()) {
        println("Usage: <program> <target-url>")
        return
    }
    val targetUrl = args[0]
    val db = Database.connect("jdbc:sqlite:webcrawler.db")
    initDatabase(db)
    // Set up ChromeDriver (requires chromedriver in PATH)
    System.setProperty("webdriver.chrome.silentOutput", "true")
    val driver = ChromeDriver()
    crawl(db, targetUrl, driver = driver)
    driver.quit()
    println("Crawling complete.")
}
