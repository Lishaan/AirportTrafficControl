import java.util.ArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;

public class Main {
	public static void main(String[] args) {
		Util.clearScreen();
		System.out.print("Enter simulation time (seconds): ");
		final long duration = (new java.util.Scanner(System.in).nextLong()) * 1000;
		initLogFile(duration);
		
		final long startTime = System.currentTimeMillis();
		final long endTime = startTime + duration;

		// BlockingQueue<Aircraft> aircrafts = new ArrayBlockingQueue<Aircraft>(Airport.CAPACITY);
		Container<Aircraft> aircraftContainer = new Container<Aircraft>(Airport.CAPACITY);
		Runway[] runways = new Runway[] {
			new Runway(aircraftContainer),
			new Runway(aircraftContainer),
			new Runway(aircraftContainer)
		};

		ExecutorService exec = Executors.newFixedThreadPool(3);
		Thread aircraftSpawner = new Thread() {
			public @Override void run() {
				int spawnCount = 2;

				// for (int i = 0; i < spawnCount; i++) {
				// while (true) {
					try {
						int spawnTime = 2;
						// int spawnTime = new java.util.Random().nextInt(5) + 5;
						// Thread.sleep(3000); // Wait time

						// Main.addLog(String.format("Creating an Aircraft in %d seconds", spawnTime));

						// Thread.sleep(spawnTime*1000);
						aircraftContainer.add(Airport.newAircraft());
						aircraftContainer.add(Airport.newAircraft());
						aircraftContainer.add(Airport.newAircraft());
						aircraftContainer.add(Airport.newAircraft());
						aircraftContainer.add(Airport.newAircraft());

						// synchronized(aircraftContainer) {
						// 	aircraftContainer.notifyAll();
						// }

					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				// }
			}
		};

		aircraftSpawner.start();

		for (Runway runway : runways) {
			exec.execute(runway);
		}

		do {
			Util.clearScreen();
			printCurrentTime(startTime, duration); 
			printAircraftStatuses(aircraftContainer.getArrayList());
			printRunwayStatuses(runways);
			printLogs(20);

			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		} while (System.currentTimeMillis() <= endTime);

		exec.shutdown();

		writeToLogsSorted();

		printRunwayStats(runways);

		System.exit(0);
	}

	private static java.io.File logFile = new java.io.File("log.txt");
	private static ArrayList<Log> logs = new ArrayList<Log>();
	private static void initLogFile(final long duration) {
		try {
			logFile = new java.io.File("log.txt");
			java.io.PrintWriter writer = new java.io.PrintWriter(logFile);

			String startTime = Util.formatTime(System.currentTimeMillis());

			String init = String.format("Start time: %s\nDuration: %d seconds\n", startTime, duration/1000);

			writer.print(init);
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

		System.out.println("Start Time: " + Util.formatTime(startTime));
		System.out.format("Current Time: %s\n\nRemaining: %s\n", 
			Util.formatTime(currentTime), 
			Util.formatTime((startTime-currentTime) + duration));
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
			System.out.format("- %s: %s\n", runway.getName(), runway.getStatus());
		}
		System.out.println();
	}

	public static void printLogs(int count) {
		int start = logs.size()-1;
		int end = logs.size()-count;

		if (end < 0) {
			end = 0;
		}

		System.out.println("Logs ("+(start-end+1)+")");
		for (int i = start; i >= end; i--) {
			System.out.println(logs.get(i));
		}
		System.out.println();
	}

	private static void printRunwayStats(Runway[] runways) {
		String r1out = String.format("Runway 1 - [Arrivals: %d, Departures: %d]", 
			runways[0].getArrivalCount(), 
			runways[0].getDepartureCount());
		String r2out = String.format("Runway 2 - [Arrivals: %d, Departures: %d]", 
			runways[1].getArrivalCount(), 
			runways[1].getDepartureCount());
		String r3out = String.format("Runway 3 - [Arrivals: %d, Departures: %d]", 
			runways[2].getArrivalCount(), 
			runways[2].getDepartureCount());

		int total_d = runways[0].getDepartureCount() + runways[1].getDepartureCount() + runways[2].getDepartureCount();
		int total_a = runways[0].getArrivalCount() + runways[1].getArrivalCount() + runways[2].getArrivalCount();

		String total_out = String.format("\nTotal Arrivals: %d\nTotal Departures: %d", total_a, total_d);

		writeToLogFile(new Log(r1out, 0));
		writeToLogFile(new Log(r2out, 0));
		writeToLogFile(new Log(r3out, 0));
		writeToLogFile(total_out);

		System.out.println("\nEnd");
		System.out.println(r1out);
		System.out.println(r2out);
		System.out.println(r3out);
		System.out.println(total_out);
	}
}
/*
a) Airport Traffic Control (Maximum 15 marks)
You are required to write a concurrent program to control the air traffic in an international airport. In this airport, there are THREE (3) runways that allow aircraft to land and depart. Following is a set of conditions that has to be fulfilled:

	i) Aircraft that is going to land or depart has to be generated randomly.
	
	ii) Departing aircraft is not allowed to take off in less than 5 seconds, but it can take longer duration if there is no other aircraft that is going to take off.
	
	iii) Landing aircraft will take 10 seconds to land, and no aircraft is allowed to take off by using the same runway during this period of time.

Aircraft is created


Each aircraft is represented by a thread and assigned with a different ID. You have to print out a statement when an aircraft is created, departed, or landed, along with the time stamp. In addition to this, you are required to print out a statement when the aircraft takes the runway as well. Furthermore, appropriate action should be taken into consideration to prevent deadlock and starvation.

Count the number of times that each runway is used for departing/landing and include it as part of your output to ensure all runways are used fairly.

Your code will be marked on correctness, design, clarity, efficiency, and appropriate comments within the program.
*/