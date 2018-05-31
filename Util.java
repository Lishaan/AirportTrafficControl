import java.util.ArrayList;

class Util {
	private static java.io.File logFile = new java.io.File("log.txt");
	private static ArrayList<Log> logs = new ArrayList<Log>();
	
	public static void initLogFile(final long duration) {
		try {
			logFile = new java.io.File("log.txt");
			java.io.PrintWriter writer = new java.io.PrintWriter(logFile);

			String startTime = Util.formatTime(System.currentTimeMillis());

			String initString = String.format("Start time: %s\nDuration: %d seconds\n\n", startTime, duration/1000);

			writer.print(initString);
			writer.close();
		} catch (java.io.FileNotFoundException e) {
			e.printStackTrace();
		}
	}
	
	public static void addLog(String message, int ID) {
		Log log = new Log(message, ID);
		logs.add(log);
	}

	public static void writeToLogsSorted() {
		int ID = 0;
		int highest = -1;

		for (int i = 0; i < logs.size(); i++) {
			if (logs.get(i).getID() >= highest) {
				highest = logs.get(i).getID();
			}
		}

		for (int x = 0; x < highest; x++) {
			for (int i = 0; i < logs.size(); i++) {
				if (ID == logs.get(i).getID()) {
					writeToLogFile(logs.get(i));
				}
			}
			writeToLogFile("");
			ID++;
		}
	}

	private static void writeToLogFile(Log log) {
		try {
			java.nio.file.Files.write(java.nio.file.Paths.get("log.txt"), (log.toString() + "\n").getBytes(), java.nio.file.StandardOpenOption.APPEND);
		} catch (java.io.IOException e) {
			e.printStackTrace();
		}
	}

	private static void writeToLogFile(String message) {
		try {
			java.nio.file.Files.write(java.nio.file.Paths.get("log.txt"), (message + "\n").getBytes(), java.nio.file.StandardOpenOption.APPEND);
		} catch (java.io.IOException e) {
			e.printStackTrace();
		}
	}

	public static void printCurrentTime(long startTime, long duration) {
		long currentTime = System.currentTimeMillis();
		long endTime = startTime+duration;
		long remainingTime = startTime-currentTime + duration;

		boolean timeUp = System.currentTimeMillis() >= endTime;

		System.out.format("Start Time: %s\nCurrent Time: %s\nEnd Time: %s\n\nRemaining: %s\n", 
			Util.formatTime(startTime),
			Util.formatTime(currentTime), 
			Util.formatTime(endTime),
			timeUp ? "waiting for aircrafts to clear" : Util.formatTime(remainingTime, true));

		System.out.println();
	}

	public static void printAircraftStatuses(ArrayList<Aircraft> aircrafts) {
		System.out.println("Arrivals & Departures");
		System.out.format("+ -- + ------------- + ---------------- + ------ +\n");
		System.out.format("| %-2s | %-13s | %-16s | %-6s |\n", "ID", "AIRCRAFT NAME", "DESTINATION CITY", "STATUS");
		System.out.format("+ -- + ------------- + ---------------- + ------ +\n");
		for (Aircraft a : aircrafts)
			System.out.format("| %2s | %-13s | %-16s | %-6s |\n", a.getID(), a.getName(), a.getDestination(), a.getStatus());
		for (int i = 0; i < Airport.CAPACITY-aircrafts.size(); i++)
			System.out.format("| %2s | %-13s | %-16s | %-6s |\n", "", "", "", "");
		System.out.format("+ -- + ------------- + ---------------- + ------ +\n");

		System.out.println();
	}

	public static void printRunwayStatuses(Runway[] runways) {
		System.out.println("Runways' status");
		for (Runway runway : runways) {
			System.out.format("- %s: %s (%s)\n", runway.getName(), runway.getStatus(), runway.getCurrentAircraft());
		}

		System.out.println();
	}

	public static void printLogs(int count) {
		int start = logs.size();
		int end = logs.size()-count;

		if (end < 0) {
			end = 0;
		}

		System.out.println("Logs ("+(start-end)+")");
		for (int i = start-1; i >= end; i--) {
			System.out.println(logs.get(i));
		}
		System.out.println();
	}

	public static void printRunwayStats(Runway[] runways) {
		String r1out = String.format("Runway 1 [Arrivals: %d | Departures: %d]", 
			runways[0].getArrivalCount(), 
			runways[0].getDepartureCount());
		String r2out = String.format("Runway 2 [Arrivals: %d | Departures: %d]", 
			runways[1].getArrivalCount(), 
			runways[1].getDepartureCount());
		String r3out = String.format("Runway 3 [Arrivals: %d | Departures: %d]", 
			runways[2].getArrivalCount(), 
			runways[2].getDepartureCount());

		int total_a = runways[0].getArrivalCount() + runways[1].getArrivalCount() + runways[2].getArrivalCount();
		int total_d = runways[0].getDepartureCount() + runways[1].getDepartureCount() + runways[2].getDepartureCount();

		String total_out = String.format("\nTotal Arrivals: %d\nTotal Departures: %d", total_a, total_d);

		writeToLogFile(new Log(r1out, 0));
		writeToLogFile(new Log(r2out, 0));
		writeToLogFile(new Log(r3out, 0));
		writeToLogFile(total_out);

		System.out.println("End\n");
		System.out.println(r1out);
		System.out.println(r2out);
		System.out.println(r3out);
		System.out.println(total_out);
	}
	
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
		return formatTime(millis, false);
	}

	public static String formatTime(long millis, boolean minsOnly) {
		String timePattern = "HH:mm:ss";

		if (minsOnly) {
			timePattern = "mm:ss";
		}
		
		java.time.Instant instant = java.time.Instant.ofEpochMilli(millis);
		java.time.ZonedDateTime zdt = java.time.ZonedDateTime.ofInstant(instant, java.time.OffsetDateTime.now().getOffset());
		java.time.format.DateTimeFormatter fmt = java.time.format.DateTimeFormatter.ofPattern(timePattern);
		
		return fmt.format(zdt);
	}
}