package com.clydeev.scraper.catholicbible;

/**
 *
 * @author cvelasquez
 */
public class Main {

	public static void main(String[] args) throws Exception {

		System.setProperty("webdriver.gecko.driver", "/home/cvelasquez/NetBeansProjects/SeleniumScraper/geckodriver");
		System.setProperty("webdriver.chrome.driver", "/home/cvelasquez/NetBeansProjects/SeleniumScraper/chromedriver");

		BibleScraper notes = new BibleScraper("/home/cvelasquez/Desktop/DouayRheims");
		notes.scrape();
	}
}
