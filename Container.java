import java.util.ArrayList;

public class Container<T> {
	private int size;
	private volatile ArrayList<T> items;

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
		notifyAll();
		return items;
	}

	public synchronized T get(int index) {
		notifyAll();
		return items.get(index);
	}

	public synchronized void remove(int index) {
		items.remove(index);
		notifyAll();
	}

	public synchronized boolean isEmpty() {
		notifyAll();
		return items.size() <= 0;
	}
}