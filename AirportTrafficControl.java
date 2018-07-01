import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;

public class AirportTrafficControl {
	private final Container<Aircraft> aircraftContainer;
	private final Runway[] runways;
	private final ExecutorService runwayExecutor;
	private final int aircraftsToSpawn;
	private final int logsCount;

	private AirportTrafficControl() {
		// DEFAULT GAME SETUP
		// runwayCount = 3
		// airportSize = 5
		// logsCount = 5
		// aircraftsToSpawn = 10
		this(3, 5, 5, 10);
	}

	private AirportTrafficControl(int runwayCount, int containerSize, int logsCount, int aircraftsToSpawn) {
		aircraftContainer = new Container<Aircraft>(containerSize);

		runways = new Runway[runwayCount];

		for (int i = 0; i < runways.length; i++) {
			runways[i] = new Runway(aircraftContainer);
		}

		runwayExecutor = Executors.newFixedThreadPool(runways.length);

		this.aircraftsToSpawn = aircraftsToSpawn;
		this.logsCount = logsCount;
	}

	public int start() throws InterruptedException {
		Util.initLogFile();
		int aircraftsRemaining = aircraftsToSpawn;

		for (Runway runway : runways) {
			runwayExecutor.execute(runway);
		}

		final long startTime = System.currentTimeMillis();
		int spawnTime = 0;
		boolean spawning = false;
		boolean ended = false;
		
		// Main Thread: Displaying & Spawning aircrafts
		do {
			// Display
			Util.clearScreen();
			Util.printCurrentTime(startTime, aircraftsRemaining);
			Util.printAircraftStatuses(aircraftContainer.getArrayList());
			Util.printRunwayStatuses(runways);
			Util.printLogs(logsCount);

			// Spawn Aircraft
			if (!aircraftContainer.isFull() && aircraftsRemaining > 0) {
				if (!spawning) {
					spawnTime = Util.getRandomInt(4, 7);
					Util.addLog(String.format("Creating an Aircraft in %d seconds", spawnTime), 0);
					spawning = true;
				}

				if (spawnTime <= 0) {
					aircraftContainer.add(Airport.newAircraft());
					aircraftsRemaining -= 1;
					spawning = false;
				}
			}

			if (aircraftsRemaining <= 0) {
				System.out.println("Waiting for aircrafts to clear\n");
			}
			
			// Ending Condition
			ended = (aircraftContainer.isEmpty()) && (aircraftsRemaining <= 0);

			Thread.sleep(1000);

			if (!aircraftContainer.isFull()) {
				spawnTime -= 1;
			}

		} while (!ended);

		runwayExecutor.shutdown();

		while (!runwayExecutor.isTerminated()) {
			if (ended) {
				Util.writeToLogsSorted();
				Util.printRunwayStats(runways, startTime, aircraftsToSpawn);
				break;
			}
		}

		return 0;
	}

	public static AirportTrafficControl create() {
		java.util.Scanner userInput = new java.util.Scanner(System.in);

		Util.clearScreen();
		System.out.println("Airport Traffic Control v1.0\n");
		System.out.println("1. Default Setup");
		System.out.println(" - Aircrafts to spawn: 10");
		System.out.println(" - Logs to display: 5");
		System.out.println(" - Airport capacity: 5");
		System.out.println(" - Runways: 3\n");
		System.out.println("2. Custom Setup\n");
		
		int choice;
		do {
			System.out.print("Choice: ");
			choice = userInput.nextInt();
		} while (choice > 2 || choice < 1);

		if (choice == 2) {
			Util.clearScreen();
			System.out.println("Custom Setup\n");
			System.out.print("Aircrafts to spawn (recommended MAX=99): ");
			int aircraftsToSpawn = userInput.nextInt();
			System.out.print("Logs to display (recommended MAX=10): ");
			int logsCount = userInput.nextInt();
			System.out.print("Airport capacity: ");
			int airportSize = userInput.nextInt();
			System.out.print("Runways: ");
			int runwayCount = userInput.nextInt();
			
			Airport.setCapacity(airportSize);
			return new AirportTrafficControl(runwayCount, airportSize, logsCount, aircraftsToSpawn);
		} else {
			return new AirportTrafficControl();
		}
	}
}