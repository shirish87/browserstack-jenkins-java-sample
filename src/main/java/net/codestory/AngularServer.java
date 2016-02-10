package net.codestory;

import net.codestory.http.*;
import net.codestory.http.routes.*;

import java.util.Arrays;

public class AngularServer {
	public static void main(String[] args) {
		int port = 8080;
		if (args.length > 0) {
			try {
				port = Integer.parseInt(args[0]);
			} catch (NumberFormatException e) {
				System.out.println("Using default port: " + port);
			}
		}

		new WebServer().configure(new WebConfiguration()).start(port);
	}

	public static class WebConfiguration implements Configuration {
		@Override
		public void configure(Routes routes) {
			routes.get("/hello/:name", (context, name) -> "Hello, " + name.toUpperCase() + "!");
		}
	}
}
