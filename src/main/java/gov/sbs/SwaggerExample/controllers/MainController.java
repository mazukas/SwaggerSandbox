package gov.sbs.SwaggerExample.controllers;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController(value = "main")
public class MainController {

	private String temp = "";
	private String temp2 = "";

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
	
	@RequestMapping(value = "/nacho", method = RequestMethod.GET)
	public String nacho() {
		System.out.println("I'M NACHO FATHER");
		return "helloworld";
	}
	
	@RequestMapping(value = "/travel", method = RequestMethod.GET)
	public String travel() {
		System.out.println("I'M Going Away for the 4th of July!!!!");
		return "helloworld";
	}
	
	@RequestMapping(method = RequestMethod.POST)
	public String post(@RequestParam(value = "name", required = false, defaultValue = "World") String name) {
		System.out.println("Hit Post Method");
		return "POST METHOD : " + name;
	}

}