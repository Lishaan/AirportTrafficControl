public class Airport {
	public static final int CAPACITY = 5;

	private static final String[] AIRCRAFT_NAMES = {
		"Airbus A350", 
		"Airbus A380",
		"Boeing 787", 
		"Boeing 717"
	};

	private static final String[] LOCATIONS = {
		"Singapore",
		"Melbourne", 
		"New Delhi", 
		"Plaisance", 
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

		return new Aircraft(name, destination, true);
	}
}