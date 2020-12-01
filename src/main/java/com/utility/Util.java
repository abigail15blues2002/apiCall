package com.utility;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import org.apache.tomcat.util.http.fileupload.FileUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import com.object.commit.CommitResult;
import com.object.search.Item;

public class Util {
	public static void createRepoSearchResults(List<Item> items) {
		File htmlfile;
		try {

			StringBuilder htmlTemplate = new StringBuilder();
			htmlfile = new File("src/main/resources/static/repoSearchResults.html");

			String htmlheader = "<html><head>";
			htmlheader += "<title>Search Repository Results</title>";
			htmlheader += "</head><body>";
			htmlheader += "<h1>Repository Search Results</h1>";
			String htmlfooter = "</body></html>";

			htmlTemplate.append(htmlheader);
			for (Item item : items) {
				StringBuilder hrefString = new StringBuilder();
				hrefString.append("<a href=\"");
				hrefString.append("http://localhost:8080/");
				hrefString.append(item.getId() + item.getName() + ".html");
				hrefString.append("\">");
				htmlTemplate.append("<p>" + hrefString.toString() + "ID : " + item.getId() + " Name : " + item.getName()
						+ "</a></p>");
				createAnalytics(item);
			}
			htmlTemplate.append(htmlfooter);
			FileWriter fr = new FileWriter(htmlfile);
			fr.write(htmlTemplate.toString());
			fr.close();

		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	private static void createAnalytics(Item item) {
		File htmlfile = new File("src/main/resources/static/" + item.getId() + item.getName() + ".html");

		String htmlTemplate = readFileContents("src/main/resources/templates/template.html");
		String timeline = "https://github.com/" + item.getOwner().getLogin() + "/" + item.getName()
				+ "/graphs/commit-activity";

		htmlTemplate = htmlTemplate.replace("$id", item.getId().toString());
		htmlTemplate = htmlTemplate.replace("$name", item.getName());
		htmlTemplate = htmlTemplate.replace("$timeline", timeline);
		htmlTemplate = htmlTemplate.replace("$timestamp", java.time.LocalDateTime.now().toString());

		String uri = "https://api.github.com/repos/" + item.getOwner().getLogin() + "/" + item.getName() + "/commits";
		RestTemplate restTemplate = new RestTemplate();
		StringBuilder commit = new StringBuilder();
		
		// org.springframework.web.client.HttpClientErrorException: 403 rate limit
		// exceeded - too many API calls
		/*
		 * For unauthenticated requests, the rate limit allows for up to 60 requests per
		 * hour. Unauthenticated requests are associated with the originating IP
		 * address, and not the user making requests.
		 */
		//comment for limit rate
		System.out.println("URI : "+uri);
		ResponseEntity<CommitResult[]> response = restTemplate.getForEntity(uri, CommitResult[].class);
		CommitResult[] commitResultList = response.getBody();

		
		commit.append("<ul>");
		for (CommitResult commitResult : commitResultList) {
			System.out.println(commitResult.getSha());
			System.out.println(commitResult.getCommit().getAuthor().getName());
			System.out.println(commitResult.getCommit().getAuthor().getEmail());
			System.out.println(commitResult.getCommit().getAuthor().getDate());
			System.out.println(commitResult.getUrl());

			commit.append("<li>");
			commit.append(commitResult.getSha() + "<p>" + commitResult.getCommit().getAuthor().getName() + "</p><p>"
					+ commitResult.getCommit().getAuthor().getEmail() + "</p><p>"
					+ commitResult.getCommit().getAuthor().getDate() + "</p>");
			commit.append("<p>Impact/Changes : " + commitResult.getUrl() + "</p>");
			// For changes dont call anymore -
			// org.springframework.web.client.HttpClientErrorException: 403 rate limit
			// exceeded - too many API calls
			commit.append("</li>");
		}
		
		commit.append("</ul>");
		
		//comment for limit rate
		htmlTemplate = htmlTemplate.replace("$commit", commit.toString());

		FileWriter fr = null;

		try {
			fr = new FileWriter(htmlfile);
			fr.write(htmlTemplate);
			fr.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void deleteFile(File file) {

		try {
			FileUtils.cleanDirectory(file);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static String readFileContents(String fileName) {
		Path path = Paths.get(fileName);
		String content = "";
		try {
			content = Files.readString(path, StandardCharsets.US_ASCII);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return content;
	}
}
