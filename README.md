# Airport Traffic Control

AirportTrafficControl is a multithreaded program written using the Java programming language that simulates an airport controlling its incoming traffic. In the simulation, the main actors are runways and aircrafts. Runways are represented as threads, and are incharge of handling the arrivals and departures of aircraft objects as they spawn. Aircraft objects are uniquely represented by an ID, and are stored inside a thread-safe data structure. They spawn randomly (from 4 to 6 seconds) and goes through multiple stages of processing before getting removed from the data structure. 

The aircrafts are stored inside a custom thread-safe data structure called a Container. The container makes sure that no aircrafts are fetched from it when it is empty, and no more aircrafts are added when it is full. 

As soon as the simulation ends, the program outputs a log.txt file that contains a sorted list of logs that were created during runtime. The log file can be used to test whether the program ran correctly, or even for debugging purposes. 
