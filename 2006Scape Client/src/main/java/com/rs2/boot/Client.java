package com.rs2.boot;
import com.rs2.domain.ClientSettings;

public final class Client {
	public static void main(String[] args) {
		ClientSettings.SERVER_IP = "localhost";
		Main.main(args);
	}
}