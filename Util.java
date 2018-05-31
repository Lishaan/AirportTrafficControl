class Util {
	public static void clearScreen() {
        // Clears the console screen according to the operating system
		try {
			if (System.getProperty("os.name").contains("Windows")) {
				new ProcessBuilder("cmd", "/c", "cls").inheritIO().start().waitFor();
			} else {
				System.out.print("\033[H\033[2J");
				System.out.flush();
			}
		} catch (InterruptedException | java.io.IOException e) {
			e.printStackTrace();
		}
	}

	public static String formatTime(long millis) {
		String timePattern = "HH:mm:ss";
		
		java.time.Instant instant = java.time.Instant.ofEpochMilli(millis);
		java.time.ZonedDateTime zdt = java.time.ZonedDateTime.ofInstant(instant, java.time.ZoneOffset.UTC);
		java.time.format.DateTimeFormatter fmt = java.time.format.DateTimeFormatter.ofPattern(timePattern);
		
		return fmt.format(zdt);
	}
}