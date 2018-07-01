import java.util.ArrayList;
	
public class Container<T> {
	private final int size;
	private final ArrayList<T> items;

	public Container(int size) {
		this.size = size;
		this.items = new ArrayList<T>();
	}

	public synchronized void add(T item) throws InterruptedException {
		while (items.size() >= this.size) {
			wait();
		}
		items.add(item);
		notifyAll();
	}

	public synchronized ArrayList<T> getArrayList() {
		return items;
	}

	public synchronized T get(int index) {
		return items.get(index);
	}

	public synchronized void remove(int index) {
		items.remove(index);
	}

	public synchronized boolean isEmpty() {
		return items.size() <= 0;
	}

	public synchronized boolean isFull() {
		return items.size() >= size;
	}
}