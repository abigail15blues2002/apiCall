package com.controller;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.ModelAndView;
import com.object.search.Item;
import com.object.search.SearchResult;
import com.utility.Util;

@Controller
@EnableAutoConfiguration
public class HomeController {

	@RequestMapping("/")
	@ResponseBody
	public ModelAndView index() {
		File file = new File("src/main/resources/static/");
		Util.deleteFile(file);
		ModelAndView modelAndView = new ModelAndView();
		modelAndView.setViewName("index");
		return modelAndView;
	}

	public static void main(String[] args) throws Exception {
		SpringApplication.run(HomeController.class, args);
	}

	@RequestMapping(value = "/search", method = RequestMethod.POST)
	public @ResponseBody void generateReport(HttpServletRequest request, HttpServletResponse response) {
		System.out.println("doPost() method is invoked.");
		try {
			doSearch(request, response);
		} catch (ServletException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	private void doSearch(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		String searchText = req.getParameter("searchText");

		String uri = "https://api.github.com/search/repositories?q=topic:" + searchText;
		RestTemplate restTemplate = new RestTemplate();

		SearchResult resultObj = restTemplate.getForObject(uri, SearchResult.class);

		for (Item item : resultObj.getItems()) {
			System.out.println(item.getName());
			System.out.println(item.getOwner().getLogin());
		}

		Util.createRepoSearchResults(resultObj.getItems());

		PrintWriter out = resp.getWriter();
		out.println(Util.readFileContents("src/main/resources/static/repoSearchResults.html"));

	}
}
