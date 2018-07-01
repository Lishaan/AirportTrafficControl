public class Main {
	public static void main(String[] args) {
		final AirportTrafficControl airportTrafficControl = AirportTrafficControl.create(); 
		
		try {
			airportTrafficControl.start();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		System.exit(0);
	}
}