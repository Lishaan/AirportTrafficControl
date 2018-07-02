import java.util.ArrayList;

public class Util {
	private static java.io.File logFile = new java.io.File("log.txt"); // Log File
	private static ArrayList<Log> logs = new ArrayList<Log>(); // logs ArrayList

	// Gets a random integer from the given range
	public static int getRandomInt(int min, int max) {
		return new java.util.Random().nextInt((max - min) + 1) + min;
	}
	
	// Create and initialize the log file
	public static void initLogFile() {
		try {
			logFile = new java.io.File("log.txt");
			java.io.PrintWriter writer = new java.io.PrintWriter(logFile);

			String startTime = Util.formatTime(System.currentTimeMillis());
			String initString = String.format("Start time: %s\n\n", startTime);

			writer.print(initString);
			writer.close();
		} catch (java.io.FileNotFoundException e) {
			e.printStackTrace();
		}
	}
	
	// Add log to the logs arraylist
	public synchronized static void addLog(String message, int ID) {
		logs.add(new Log(message, ID));
	}

	// Write all the logs from the arraylist to the log.txt file (SORTED)
	public static void writeToLogsSorted() {
		int ID = 1;
		int highest = -1;

		for (int i = 0; i < logs.size(); i++) {
			if (logs.get(i).getID() >= highest) {
				highest = logs.get(i).getID();
			}
		}

		ArrayList<Log> spawns = new ArrayList<Log>();

		for (int i = 0; i < logs.size(); i++) {
			if (logs.get(i).getID() == 0) {
				spawns.add(logs.get(i));
			}
		}

		for (int i = 0; i < spawns.size(); i++) {
			writeToLogFile(spawns.get(i));

			for (int j = 0; j < logs.size(); j++) {
				if (ID == logs.get(j).getID()) {
					writeToLogFile(logs.get(j));
				}
			}
			writeToLogFile("");
			ID++;
		}
	}

	// Appends the log to the log.txt file
	private static void writeToLogFile(Log log) {
		try {
			java.nio.file.Files.write(
				java.nio.file.Paths.get("log.txt"), 
				(log.toString() + "\n").getBytes(), 
				java.nio.file.StandardOpenOption.APPEND
			);
		} catch (java.io.IOException e) {
			e.printStackTrace();
		}
	}

	// Appends the string to the log.txt file
	private static void writeToLogFile(String message) {
		try {
			java.nio.file.Files.write(
				java.nio.file.Paths.get("log.txt"), 
				(message + "\n").getBytes(), 
				java.nio.file.StandardOpenOption.APPEND
			);
		} catch (java.io.IOException e) {
			e.printStackTrace();
		}
	}

	// Prints the first section of the main console display
	public static void printCurrentTime(long startTime, int remainingAircrafts, int spawnTime) {
		long currentTime = System.currentTimeMillis();

		System.out.format("Start Time: %s\nCurrent Time: %s\n\nNew aircraft in: %s\nRemaining Aircrafts: %d\n\n", 
			Util.formatTime(startTime),
			Util.formatTime(currentTime),
			(remainingAircrafts <= 0) ? "ended" : ((spawnTime != -1) ? (spawnTime + 1) : ((currentTime > startTime + 1) ? "spawned" : "starting" )),
			remainingAircrafts
		);
	}

	// Prints the second section of the main console display: aircraft statuses
	public static void printAircraftStatuses(ArrayList<Aircraft> aircrafts) {
		System.out.println("Arrivals & Departures");
		System.out.format("+ ---- + ------------- + ---------------- + ------ + ----- +\n");
		System.out.format("| %-4s | %-13s | %-16s | %-6s | %-5s |\n", " ID ", "AIRCRAFT NAME", "DESTINATION CITY", "STATUS", "STAGE");
		System.out.format("+ ---- + ------------- + ---------------- + ------ + ----- +\n");
		for (Aircraft a : aircrafts) {
			synchronized(a) {
				System.out.format("| %-4s | %-13s | %-16s | %-6s |   %-1s   |\n", a.getID(), a.getName(), a.getDestination(), a.getStatus(), a.getStage());
			}
		}
		for (int i = 0; i < Airport.getCapacity()-aircrafts.size(); i++) {
			System.out.format("| %-4s | %-13s | %-16s | %-6s | %-5s |\n", "", "", "", "", "");
		}
		System.out.format("+ ---- + ------------- + ---------------- + ------ + ----- +\n");

		System.out.println();
	}

	// Prints the third section of the main console display: runway statuses
	public static void printRunwayStatuses(Runway[] runways) {
		System.out.println("Runway statuses");
		System.out.format("+ ------------ + ---------------------------- + ---------- +\n");
		System.out.format("| %-12s | %-28s | %-10s |\n", 
			"RUNWAY NAME", "CURRENT AIRCRAFT", "STATUS");
		System.out.format("+ ------------ + ---------------------------- + ---------- +\n");
			
		for (Runway runway : runways) {
			System.out.format("| %-12s | %-28s | %-10s |\n", 
				runway.getName(), runway.getCurrentAircraft(), runway.getStatus()
			);
		}
		System.out.format("+ ------------ + ---------------------------- + ---------- +\n");
		System.out.println();
	}

	// Prints the fourth section of the main console display: x recent logs
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

	// Writes the last section of the log file and finalizes the file
	public static void printRunwayStats(Runway[] runways, long startTime, int aircraftsProcessed) {
		long endTime = System.currentTimeMillis();

		System.out.println("End\n");
		System.out.println("Runway Stats");
		writeToLogFile("Runway Stats");
		
		int total_a = 0;
		int total_d = 0;

		for (Runway runway : runways) {
			String runway_out = String.format("- %s: [Arrivals: %2d | Departures: %2d] total: %d",
				runway.getName(),
				runway.getArrivalCount(), 
				runway.getDepartureCount(),
				runway.getArrivalCount() + runway.getDepartureCount()
			);

			total_a += runway.getArrivalCount();
			total_d += runway.getDepartureCount();

			writeToLogFile(runway_out);
			System.out.println(runway_out);
		}

		String total_out = String.format("\nTotal Arrivals: %d\nTotal Departures: %d", total_a, total_d);
		String endTime_out = "\nEnd Time: " + Util.formatTime(endTime);
		String timeTaken_out = "Time Taken: " + Util.formatTime(endTime-startTime, true);
		String aircraftsProcessed_out = "Aircrafts Processed: " + aircraftsProcessed;

		writeToLogFile(total_out);
		writeToLogFile(endTime_out);
		writeToLogFile(timeTaken_out);
		writeToLogFile(aircraftsProcessed_out);

		System.out.println(total_out);
		System.out.println(endTime_out);
		System.out.println(timeTaken_out);
		System.out.println(aircraftsProcessed_out);
	}
	
	// Clears the console screen according to the operating system
	public static void clearScreen() {
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

	// Formats time from millis to String pattern("HH:mm:ss") 
	public static String formatTime(long millis) {
		return formatTime(millis, false);
	}

	// Formats time from millis to String pattern("mm:ss")
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