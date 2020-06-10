import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class PhoneLibrary {
	private List<Phone> phoneLibrary = new ArrayList<Phone>();
	private List<String> baseValues = new ArrayList<String>();

	/**
	 * Reads phonological features from a given filename with a given delimiter to
	 * separate values.
	 * 
	 * @param filename A filename string
	 * @param del      A delimiter string
	 * @throws IOException If the <code>filename</code> cannot be found in the
	 *                     current directory.
	 */
	private void readFile(String filename, String del) throws IOException {
		File file = new File(filename);

		BufferedReader br = new BufferedReader(new FileReader(file));

		String line;
		while ((line = br.readLine()) != null) {
			String split[] = line.split(del);
			String segment = split[0];
			if (segment.equals("SEGMENT"))
				continue;
			Matrix features = new Matrix();
			int i = 1;
			for (Phone.Feature f : Phone.Feature.values()) {
				switch (split[i].charAt(0)) {
					case '-':
						features.put(f, Phone.Quality.FALSE);
						break;
					case '+':
						features.put(f, Phone.Quality.TRUE);
						break;
					case '0':
						features.put(f, Phone.Quality.NULL);
						break;
				}
				i++;
			}
			new Phone(this, segment, features);
		}

		br.close();
	}

	/**
	 * Creates a new <code>PhoneLibrary</code> object based upon a given TSV file
	 * containing the base phonological features from which to extrapolate data. If
	 * a cache for that file exists, under the name <code>.{filename}_cache</code>,
	 * then it will read that first. Otherwise it will generate a cache to be used
	 * in future runs for the sake of speed.
	 * 
	 * @param base_filename The filename without <code>.TSV</code>
	 */
	public PhoneLibrary(String base_filename) {
		try {
			readFile("." + base_filename + "_cache", ",");
		} catch (IOException e) {
			System.out.println("Cache not initialized...");
			try {
				readFile(base_filename + ".tsv", "\t");

				List<Phone> base = new ArrayList<Phone>();
				base.addAll(phoneLibrary);

				for (Phone p : base)
					recursiveApply(p, new ArrayList<Phone.Secondary>(),
							new ArrayList<Phone.Secondary>(List.of(Phone.Secondary.values())));

				System.out.println("Saving cache...");
				List<Phone> sorted = new ArrayList<Phone>();
				sorted.addAll(phoneLibrary);
				Collections.sort(sorted);
				BufferedWriter out;
				out = new BufferedWriter(new FileWriter("." + base_filename + "_cache"));
				for (Phone p : sorted) {
					out.write(p.getDataString(",") + "\n");
				}
				out.close();
				System.out.println("Done.");
			} catch (IOException e2) {
				System.err.println("File <" + base_filename + ".tsv> not in directory, cannot load base phones");
				System.exit(1);
			}
		}
	}

	private void recursiveApply(Phone p, ArrayList<Phone.Secondary> applied, ArrayList<Phone.Secondary> list) {
		if (list.isEmpty())
			return;

		for (int i = 0; i < list.size(); i++) {
			if (!p.canApply(list.get(i), applied))
				continue;
			Phone newP = p.apply(list.get(i));
			ArrayList<Phone.Secondary> newList = new ArrayList<Phone.Secondary>();
			newList.addAll(list);
			for (int j = 0; j <= i; j++)
				newList.remove(0);
			ArrayList<Phone.Secondary> newListA = new ArrayList<Phone.Secondary>();
			newListA.addAll(applied);
			newListA.add(list.get(i));
			recursiveApply(newP, newListA, newList);
		}
	}

	/**
	 * Interprets a given string of text into phones making it up, for instance
	 * <code>fənɑləd_ʒi</code> will be interpreted as
	 * <code>["f", "ə", "n", "ɑ", "l", "ə", "d_ʒ", "i"]</code>.
	 * 
	 * @param s String of phones to be interpreted
	 * @return A <code>List</code> containing <code>Phone</code> objects
	 * @see PhoneLibrary#interpretSegment interpretSegment()
	 */
	public List<Phone> interpretSequence(String s) {
		List<Phone> phones = new ArrayList<Phone>();
		int i = 0;
		while (i < s.length()) {
			StringBuilder curr = new StringBuilder();
			while (i < s.length() && (s.charAt(i) == '_' || !baseValues.contains(curr.toString()))) {
				curr.append(s.charAt(i++));
			}
			while (i < s.length() && Phone.isSecondary(s.charAt(i))) {
				curr.append(s.charAt(i++));
			}
			if (curr.length() > 0) {
				phones.add(interpretSegment(curr.toString()));
			}
		}
		return phones;
	}

	/**
	 * Interprets a given string containing a single phone.
	 * 
	 * @param s String of the phone
	 * @return A {@code Phone} equivalent to the parameter
	 * @see PhoneLibrary#interpretSequence interpretSequence()
	 * @throws IllegalArgumentException if the segment {@code s} cannot be parsed
	 */
	public Phone interpretSegment(String s) {
		String segment;
		if (s.length() >= 3 && s.charAt(1) == '_') {
			segment = s.substring(0, 3);
			s = s.substring(3);
		} else {
			segment = s.substring(0, 1);
			s = s.substring(1);
		}
		Phone base = null;
		for (Phone p : phoneLibrary) {
			if (p.getSegment().equals(segment)) {
				base = p;
				break;
			}
		}
		if (base == null)
			throw new IllegalArgumentException(
					"Cannot interpret [" + segment + s + "], no base phone found from data.");

		List<Phone.Secondary> applied = new ArrayList<Phone.Secondary>();
		for (int i = 0; i < s.length(); i++) {
			boolean flag = false;
			for (Map.Entry<Phone.Secondary, secondaryArticulation> e : Phone.secondaryLibrary.entrySet()) {
				if (s.charAt(i) == e.getValue().getSegment().charAt(0)) {
					flag = true;
					if (e.getValue().canApply(base, applied)) {
						applied.add(e.getKey());
						base = base.apply(e.getKey());
					} else {
						throw new IllegalArgumentException("Cannot interpret [" + segment + s
								+ "], secondary articulation is restricted from application to competing phonological features: "
								+ e.getKey().name());
					}
					break;
				}
			}
			if (!flag) {
				throw new IllegalArgumentException("Cannot interpret [" + segment + s
						+ "], secondary articulation is unknown: " + String.valueOf(s.charAt(i)));
			}
		}
		return base;
	}

	public Phone fuzzySearch(Phone p) {
		Matrix features = p.getMatrix();
		for (int i = 0; i < phoneLibrary.size(); i++) {
			Phone temp = phoneLibrary.get(i);
			boolean flag = true;
			for (Map.Entry<Phone.Feature, Phone.Quality> e : features.entrySet()) {
				if (e.getValue() == Phone.Quality.NULL)
					continue;
				if (e.getValue() != temp.getFeatureQuality(e.getKey())) {
					flag = false;
					break;
				}
			}
			if (flag)
				return phoneLibrary.get(i);
		}
		return null;
	}

	public List<Phone> getPhones(Matrix map) {
		List<Phone> phones = new ArrayList<Phone>();

		for (int i = 0; i < phoneLibrary.size(); i++) {
			if (phoneLibrary.get(i).hasFeatures(map))
				phones.add(phoneLibrary.get(i));
		}

		return phones;
	}

	public boolean contains(Phone phone) {
		return phoneLibrary.contains(phone);
	}

	public void add(Phone phone) {
		if (!phone.getSegment().equals("*")) {
			if (!phoneLibrary.contains(phone)) {
				phoneLibrary.add(phone);
			} else if (phoneLibrary.get(phoneLibrary.indexOf(phone)).getSegment().length() > phone.getSegment()
					.length()) {
				phoneLibrary.remove(phone);
				phoneLibrary.add(phone);
				// phoneLibrary.get(phoneLibrary.indexOf(phone)).getSegment() =
				// phone.getSegment();
			}

			if (!baseValues.contains(phone.getSegment())) {
				if ((phone.getSegment().length() == 3 && phone.getSegment().charAt(1) == '_')
						|| phone.getSegment().length() == 1)
					baseValues.add(phone.getSegment());
			}
		}
	}

	public Matrix getCommon(Phone... phones) {
		Matrix common = new Matrix();
		for (int i = 0; i < Phone.Feature.values().length; i++) {
			Phone.Quality f = phones[0].getFeatureQuality(Phone.Feature.values()[i]);
			boolean flag = true;
			for (int j = 1; j < phones.length; j++)
				if (phones[j].getFeatureQuality(Phone.Feature.values()[i]) != f) {
					flag = false;
					break;
				}
			if (flag && f != Phone.Quality.NULL)
				common.put(Phone.Feature.values()[i], f);
		}
		return common;
	}

	public Matrix getContrast(Phone a, Phone b) {
		Matrix contrast = new Matrix();
		for (int i = 0; i < Phone.Feature.values().length; i++) {
			if (a.getFeatureQuality(Phone.Feature.values()[i]) != b.getFeatureQuality(Phone.Feature.values()[i])) {
				contrast.put(Phone.Feature.values()[i], b.getFeatureQuality(Phone.Feature.values()[i]));
			}
		}
		return contrast;
	}

	public Phone validate(Phone p) {
		return phoneLibrary.get(phoneLibrary.indexOf(p));
	}
}