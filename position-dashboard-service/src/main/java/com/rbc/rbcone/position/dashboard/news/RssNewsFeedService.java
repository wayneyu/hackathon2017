package com.rbc.rbcone.position.dashboard.news;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestOperations;

import com.rbc.rbcone.position.dashboard.model.NewsItem;
import com.rometools.rome.io.FeedException;

@Service
public class RssNewsFeedService {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(RssNewsFeedService.class);
	
	private static final String GOOGLE_RSS_URL_TEMPLATE = "http://www.google.ca/finance/company_news?q={topic}&output=rss";
	private static final String LINE_SEPARATOR = System.getProperty("line.separator");
	
	@Autowired
	private RssFeedParser feedParser;
	
	@Autowired
	private RestOperations rssFeedFetcher;
	
	private Map<String, RssSessionState> websocketSessionMap = new HashMap<>();
	
	public List<NewsItem> getRssNewsItem(String websocketId, String topic) {
		LOGGER.info("Searching RSS feeds for " + topic);
		updateCurrentTopic(websocketId, topic);
		List<NewsItem> newsItems = new ArrayList<>();
		try {
			String news = fetchNewsFromGoogle(topic);
			//String news = loadRssSample();
			feedParser.parseRssFeed(news, "Google Finance", newsItems);
			newsItems = filterOldNews(websocketId, newsItems);
		} catch (FeedException | IOException e) {
			e.printStackTrace();
		}
		return newsItems;
	}
	
	private void updateCurrentTopic(String websocketId, String topic) {
		RssSessionState sessionState = websocketSessionMap.get(websocketId);
		if (sessionState == null) {
			sessionState = new RssSessionState();
			websocketSessionMap.put(websocketId, sessionState);
		}
		if (!topic.equals(sessionState.getCurrentTopic())) {
			sessionState.setCurrentTopic(topic);
			sessionState.setLatestNewsDate(null);
		}
	}

	private List<NewsItem> filterOldNews(String websocketId, List<NewsItem> newsItems) {
		RssSessionState sessionState = websocketSessionMap.get(websocketId);
		List<NewsItem> filteredItems = new ArrayList<>();
		for (NewsItem rssNewsItem : newsItems) {
			if (sessionState.getLatestNewsDate() != null) {
				if (rssNewsItem.getPublishedDate() != null && rssNewsItem.getPublishedDate().after(sessionState.getLatestNewsDate())) {
					filteredItems.add(rssNewsItem);
				}
			} else {
				filteredItems.add(rssNewsItem);
			}
		}
		for (NewsItem rssNewsItem : newsItems) {
			if (sessionState.getLatestNewsDate() == null || 
						(rssNewsItem.getPublishedDate() != null && rssNewsItem.getPublishedDate().after(sessionState.getLatestNewsDate()))) {
				sessionState.setLatestNewsDate(rssNewsItem.getPublishedDate());
			}
		}
		return filteredItems;
	}

	private String fetchNewsFromGoogle(String topic) {
		return rssFeedFetcher.getForObject(GOOGLE_RSS_URL_TEMPLATE, String.class, topic);
	}
	
	private String loadRssSample() throws IOException {
		StringBuilder rssFeed = new StringBuilder();
		try (LineNumberReader reader = new LineNumberReader(new InputStreamReader(getClass().getResourceAsStream("/google-stock.xml")))) {
			String line = reader.readLine();
			while (line != null) {
				rssFeed.append(line);
				rssFeed.append(LINE_SEPARATOR);
				line = reader.readLine();
			}
		}
		return rssFeed.toString();
	}
}