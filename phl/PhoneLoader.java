package phl;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class PhoneLoader {
	private PhoneManager pm;

	private static void readFile(PhoneManager pm, String directory, String filename, String del) throws IOException {
		File file = new File(directory, filename);
		try (BufferedReader br = new BufferedReader(new FileReader(file))) {
			String line;
			while ((line = br.readLine()) != null) {
				String[] split = line.split(del);
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
						case '~':
							features.put(f, Phone.Quality.ANY);
							break;
						case '0':
							features.put(f, Phone.Quality.NULL);
							break;
						default:
							throw new IOException("Quality <" + split[i] + "> is not applicable (+, -, ~, 0)");
					}
					i++;
				}
				new Phone(pm, segment, features);
			}
		}
	}

	private void initCache(String baseFilename) throws IOException {
		String cacheFilename = baseFilename.replaceFirst(".*?\\/", "");
		this.pm = new PhoneManager();
		System.out.println("Cache not initialized...");
		try {
			System.out.println("Reading file <" + baseFilename + ">...");
			readFile(pm, null, baseFilename, "\t");

			List<Phone> base = new ArrayList<>();
			base.addAll(pm.getAllPhones());

			System.out.println("Generating variants...");

			for (Phone p : base) {
				System.out.println("\tVariants of " + p);
				recursiveApply(p, new ArrayList<>(), new ArrayList<>(List.of(Phone.Secondary.values())));
			}

			System.out.println("Saving cache...");
			List<Phone> sorted = new ArrayList<>();
			sorted.addAll(pm.getAllPhones());
			Collections.sort(sorted);

			File directory = new File(".cache");
			if (!directory.exists())
				directory.mkdir();

			try (FileOutputStream fos = new FileOutputStream(new File(".cache", cacheFilename + ".data"));
					ObjectOutputStream oos = new ObjectOutputStream(fos);) {
				oos.writeObject(this.pm);
				System.out.println("Done.");
			} catch (Exception e3) {
				throw new IOException("Cannot save cache.");
			}
		} catch (Exception e2) {
			throw new IOException("File <" + baseFilename + "> not in directory, cannot load base phones");
		}
	}

	public PhoneLoader(String baseFilename, boolean force) throws IOException {
		if (force)
			initCache(baseFilename);
		else {
			String cacheFilename = baseFilename.replaceFirst(".*?\\/", "");
			try (FileInputStream fis = new FileInputStream(new File(".cache", cacheFilename + ".data"));
					ObjectInputStream ois = new ObjectInputStream(fis);) {
				this.pm = (PhoneManager) ois.readObject();
			} catch (Exception e) {
				initCache(baseFilename);
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
			ArrayList<Phone.Secondary> newList = new ArrayList<>();
			newList.addAll(list);
			for (int j = 0; j <= i; j++)
				newList.remove(0);
			ArrayList<Phone.Secondary> newListA = new ArrayList<>();
			newListA.addAll(applied);
			newListA.add(list.get(i));
			recursiveApply(newP, newListA, newList);
		}
	}

	public PhoneManager getManager() {
		return pm;
	}
}