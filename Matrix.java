import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

public class Matrix {
	private Map<Phone.Feature, Phone.Quality> holder;

	public Matrix() {
		holder = new HashMap<Phone.Feature, Phone.Quality>();
	}

	@SafeVarargs
	public Matrix(Entry<Phone.Feature, Phone.Quality>... entries) {
		holder = new HashMap<Phone.Feature, Phone.Quality>(Map.ofEntries(entries));
	}

	public Phone.Quality get(Phone.Feature key) {
		return holder.get(key);
	}

	public void put(Phone.Feature key, Phone.Quality value) {
		holder.put(key, value);
	}

	public void putAll(Matrix m) {
		holder.putAll(m.holder);
	}

	public int size() {
		return holder.size();
	}

	@Override
	public String toString() {
		StringBuilder s = null;
		for (Map.Entry<Phone.Feature, Phone.Quality> e : holder.entrySet()) {
			if (s != null)
				s.append(", " + e.getValue() + e.getKey());
			else {
				s = new StringBuilder("[" + e.getValue() + e.getKey());
			}
		}
		if (s == null)
			return "[]";
		s.append("]");
		return s.toString();
	}

	public Set<Entry<Phone.Feature, Phone.Quality>> entrySet() {
		return holder.entrySet();
	}

	public boolean isEmpty() {
		return holder.isEmpty();
	}
}