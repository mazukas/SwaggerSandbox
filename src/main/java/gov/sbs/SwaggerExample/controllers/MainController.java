package gov.sbs.SwaggerExample.controllers;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController(value = "main")
public class MainController {

	private String temp = "";

	@RequestMapping(value = "/hello", method = RequestMethod.GET)
	public String hello() {
		System.out.println("HIT HELLO WORLD");
		return "helloworld";
	}

	@RequestMapping(method = RequestMethod.GET)
	public String get() {
		System.out.println("Hit Get Method");
		return "GET METHOD";
	}
	
	@RequestMapping(method = RequestMethod.POST)
	public String post(@RequestParam(value = "name", required = false, defaultValue = "World") String name) {
		System.out.println("Hit Post Method");
		return "POST METHOD : " + name;
	}

}