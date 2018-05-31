import java.util.ArrayList;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ArrayBlockingQueue;

public class Main {
	public static ArrayList<Log> logs = new ArrayList<Log>();
	public static void addLog(String message) { logs.add(new Log(message)); }

	public static void main(String[] args) {
		Util.clearScreen();
		// System.out.print("Enter simulation time (seconds): ");
		final long duration = 60*1000;
		//(new java.util.Scanner(System.in).nextLong()) * 1000;
		
		final long startTime = System.currentTimeMillis();
		final long endTime = startTime + duration;

		BlockingQueue<Aircraft> aircrafts = new ArrayBlockingQueue<Aircraft>(Airport.CAPACITY);

		new Thread(new Runnable() {
			public @Override void run() {
				while (true) {
					try {
						int spawnTime = 2;
						// new java.util.Random().nextInt(5) + 5;
						Main.addLog(String.format("Creating an Aircraft in %d seconds", spawnTime));
						Thread.sleep(spawnTime*1000);

						aircrafts.put(Airport.newAircraft());
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
		}).start();

		new Thread(new Runway(aircrafts)).start();
		new Thread(new Runway(aircrafts)).start();
		new Thread(new Runway(aircrafts)).start();

		do {
			Util.clearScreen();
			printCurrentTime(); 
			printAircraftStatuses(aircrafts);
			printLogs(20);

			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		} while (System.currentTimeMillis() < endTime);

		System.out.println("End");
	}

	public static void printCurrentTime() {
		String timePattern = "HH:mm:ss";
		
		java.time.Instant instant = java.time.Instant.ofEpochMilli(System.currentTimeMillis());
		java.time.ZonedDateTime zdt = java.time.ZonedDateTime.ofInstant(instant, java.time.ZoneOffset.UTC);
		java.time.format.DateTimeFormatter fmt = java.time.format.DateTimeFormatter.ofPattern(timePattern);
		
		String output = fmt.format(zdt);

		System.out.println("Current Time: " + output);
		System.out.println();
	}

	public static void printAircraftStatuses(BlockingQueue<Aircraft> aircraftsQueue) {
		ArrayList<Aircraft> aircrafts = new ArrayList<Aircraft>(aircraftsQueue);
		System.out.println("Departures & Arrivals");
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
}

class Log {
	private final String message;
	private final Long timeCreated;

	public Log(String msg) {
		message = msg;
		timeCreated = System.currentTimeMillis();
	}

	public @Override String toString() {
		String timePattern = "MM/dd HH:mm:ss";
		
		java.time.Instant instant = java.time.Instant.ofEpochMilli(timeCreated);
		java.time.ZonedDateTime zdt = java.time.ZonedDateTime.ofInstant(instant, java.time.ZoneOffset.UTC);
		java.time.format.DateTimeFormatter fmt = java.time.format.DateTimeFormatter.ofPattern(timePattern);
		
		String output = fmt.format(zdt);

		return String.format("%s - %s", output, this.message);
	}
}

class Runway implements Runnable {
	private static int ID_INC = 1;

	private final int ID;
	private Object lock;
	BlockingQueue<Aircraft> aircrafts;

	public Runway(BlockingQueue<Aircraft> aircrafts) {
		this.aircrafts = aircrafts;
		this.ID = Runway.ID_INC++;
		this.lock = new Object();
	}

	public @Override void run() {
		while (true) {
			try {
				while (aircrafts.isEmpty()) {

				}

				// Aircraft aircraft = aircrafts.peek();
				for (Aircraft aircraft : aircrafts) {				
					if (aircraft.isFlying() && !aircraft.isRunway()) {
						synchronized(lock) {
							aircraft.setAtRunway();

							Main.addLog(String.format("(ID: %d) %s is landing at Runway %d in 10seconds", aircraft.getID(), aircraft.getName(), this.getID()));
							
							Thread.sleep(10*1000);
							Main.addLog(String.format("(ID: %d) %s finished landing at Runway %d and is parked in the airport", aircraft.getID(), aircraft.getName(), this.getID()));
							aircraft.unsetAtRunway();
							aircraft.switchStatus();
						}
						int parkTime = new java.util.Random().nextInt(5) + 5;

						Main.addLog(String.format("(ID: %d) %s will be parked in the airport for %dseconds", aircraft.getID(), aircraft.getName(), parkTime));
						Thread.sleep(parkTime*1000);
					}

					if (aircraft.isLanded() && !aircraft.isRunway()) {
						synchronized(lock) {
							aircraft.setAtRunway();

							int takeOffTime = new java.util.Random().nextInt(5) + 5;
							Main.addLog(String.format("(ID: %d) %s is taking off from Runway %d to %s in %dseconds", aircraft.getID(), aircraft.getName(), this.getID(), aircraft.getDestination(), takeOffTime));
							
							Thread.sleep(takeOffTime*1000);
							// Main.addLog(String.format("%d", aircraft.getID()));
							// Main.addLog(String.format("%d", aircrafts.peek().getID()));

							if (aircraft.getID() == aircrafts.peek().getID()) {
								Main.addLog(String.format("(REMOVED ID: %d) %s has flown off from Runway %d to %s", aircraft.getID(), aircraft.getName(), this.getID(), aircraft.getDestination()));
								aircrafts.take();
							}
						}
					}
				}
			} catch (InterruptedException e) {
				e.printStackTrace();
			}

		}
	}

	public int getID() { return ID; }
}

class Aircraft {
	private static int ID_INC = 1;
	private static String STATUS_FLYING = "Flying";
	private static String STATUS_LANDED = "Landed";
	private static String STATUS_RUNWAY = "Runway";

	private final int ID;
	private final String name;
	
	private String status; // Flying/Landed/Runway
	private String prevStatus;
	private String destination;

	public Aircraft(String name, String destination) {
		this.ID = Aircraft.ID_INC++;
		this.name = name;
		this.destination = destination;

		status = (new java.util.Random().nextInt(100) > 50) ? STATUS_FLYING : STATUS_LANDED;
		prevStatus = null;
		
		if (isLanded()) {
			Main.addLog(String.format("(CREATED ID: %d) %s created and is waiting for depature to %s", ID, name, destination));
		} else if (isFlying()) {
			Main.addLog(String.format("(CREATED ID: %d) %s created and is arriving soon from %s", ID, name, destination));
		}
	}

	public void switchStatus() {
		if (status.equals(STATUS_FLYING)) {
			status = STATUS_LANDED;
		} else {
			status = STATUS_FLYING;
		}
	}

	public void setAtRunway() {
		prevStatus = status;
		status = STATUS_RUNWAY;
		Main.addLog("(ID: " + getID() + ") RUNWAY NOTIF: " + getName() + " at the runway");
	}

	public void unsetAtRunway() {
		if (prevStatus == null) {
			prevStatus = status;
		} else {
			status = prevStatus;
		}
		Main.addLog("(ID: " + getID() + ") RUNWAY NOTIF: " + getName() + " off the runway");
	}

	public boolean isFlying() { return status.equals(STATUS_FLYING); }
	public boolean isLanded() { return status.equals(STATUS_LANDED); }
	public boolean isRunway() { return status.equals(STATUS_RUNWAY); }

	public int getID() { return ID; }
	public String getName() { return name; }
	public String getStatus() { return status; }
	public String getDestination() { return destination; }
}

class Airport {
	public static int CAPACITY = 5;

	private static String[] AIRCRAFT_NAMES = {
		"Airbus A350", 
		"Airbus A380",
		"Boeing 787", 
		"Boeing 717"
	};

	private static String[] LOCATIONS = {
		"Singapore",
		"Melbourne", 
		"New Delhi", 
		"New York",
		"Colombo",
		"Jakarta",
		"Beijing", 
		"Toronto",
		"Tokyo",
		"Seoul",
		"Bangkok",
		"Hanoi",
		"Malé"
	};

	public static Aircraft newAircraft() {
		String name = AIRCRAFT_NAMES[new java.util.Random().nextInt(AIRCRAFT_NAMES.length)];
		String destination = LOCATIONS[new java.util.Random().nextInt(LOCATIONS.length)];

		return new Aircraft(name, destination);
	}
}

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