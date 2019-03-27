package com.clydeev.scraper.catholicbible;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.TimeUnit;
import org.apache.commons.io.FileUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.WebDriverWait;

/**
 *
 * @author cvelasquez
 */
public class BibleScraper {

	private String path;

	public BibleScraper(String path) {
		this.path = path;
	}

	public void scrapeDouayRheims() throws IOException {
		scrape("douay_rheims");
	}

	public void scrapeVulgate() throws IOException {
		scrape("vulgate");
	}

	public void scrapeKnox() throws IOException {
		scrape("knox");
	}

	private void scrape(String bible) throws IOException {
		boolean hasNext;
		WebDriver driver = new ChromeDriver();
		for (int i = 1; i <= 2; i++) {
			driver.get("http://catholicbible.online/" + bible + "?bible_part_no=" + i + "&book_no=1&chapter_no=1");

			WebDriverWait wait = new WebDriverWait(driver, TimeUnit.MINUTES.toSeconds(1));
			wait.until(contains("2016 © CatholicBible.online, Baronius Press"));

			// Click to show notes
			try {
				driver.findElement(By.xpath("/html/body/div[1]/div[2]/div/div[1]/div[1]/a[2]")).click();
			} catch (NoSuchElementException e) {
				// Ignore
			}

			do {
				String division = driver.findElement(By.xpath("//li[contains(@class,'sheet-caption') and contains(@class,'active')]")).getText();
				String bookName = driver.findElement(By.xpath("//li[@class='book-item active']")).getText();
				String chapter = driver.findElement(By.xpath("//div[@class='chapter'][1]")).getText();

				StringBuilder verses = new StringBuilder();
				StringBuilder notes = new StringBuilder();

				// Get the verses
				Document doc = Jsoup.parse(driver.getPageSource(), "UTF-8");
				verses.append("Title: ").append(doc.selectFirst("div[class=book-name]").text()).append("\n");
				verses.append(chapter).append("\n");
				doc.select("div[class=vers]").forEach(verse -> {
					verses.append(verse.selectFirst("div[class=vers-no]").text())
							.append(" ")
							.append(verse.selectFirst("div[class=vers-content]").text())
							.append("\n");

					try {
						notes.append(verse.selectFirst("div[class=vers-comment]").siblingElements().first().text())
								.append(" ")
								.append(verse.selectFirst("div[class=vers-comment]").text())
								.append("\n");
					} catch (NullPointerException e) {
						// Ignore
					}
				});

				// Prevent additional new line
				if (!notes.toString().isEmpty()) {
					verses.append("\n");
					verses.append(notes.toString());
				}

				FileUtils.writeStringToFile(new File(path + "/" + division + "/" + bookName + "/" + chapter + ".txt"), verses.toString(), "UTF-8");
				hasNext = !driver.findElement(By.xpath("//div[@class='next-chapter']")).getText().isEmpty();
				if (hasNext) {
					// Next chapter
					driver.findElement(By.xpath("//div[@class='next-chapter']")).click();
				} else {
					try {
						// Next book
						driver.findElements(By.xpath("//li[@class='book-item active']//following-sibling::li/a")).get(0).click();
						hasNext = true;
					} catch (IndexOutOfBoundsException e1) {
						try {
							// Next section
							driver.findElement(By.xpath("//li[contains(@class,'sheet-caption') and contains(@class,'active')]//following-sibling::li")).click();
							hasNext = true;
						} catch (NoSuchElementException e2) {
							hasNext = false;
						}
					}
				}
				wait.until(contains("2016 © CatholicBible.online, Baronius Press"));
			} while (hasNext);
		}
		driver.quit();
	}

	public ExpectedCondition<Boolean> contains(String text) {
		return (ExpectedCondition<Boolean>) (WebDriver f) -> f.getPageSource().contains(text);
	}
}
