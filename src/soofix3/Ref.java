package soofix3;

public class Ref<T extends Object> {

	T t = null;

	public Ref(T t) {
		this.t = t;
	}

	public Ref() {
	}

	public T get() {
		return this.t;
	}

	public void set(T t) {
		this.t = t;
	}

	public void reset() {
		this.t = null;
	}
}
