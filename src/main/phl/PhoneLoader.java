package main.phl;

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

import main.Main;

public class PhoneLoader {
	private PhoneManager pm;

	private static void readFile(final PhoneManager pm, final String directory, final String filename, final String del)
			throws IOException {
		final File file = new File(directory, filename);
		try (BufferedReader br = new BufferedReader(new FileReader(file))) {
			String line;
			while ((line = br.readLine()) != null) {
				final String[] split = line.split(del);
				final String segment = split[0];
				if (segment.equals("SEGMENT"))
					continue;
				final Matrix features = new Matrix();
				int i = 1;
				for (final Phone.Feature f : Phone.Feature.values()) {
					features.put(f, split[i]);
					i++;
				}
				new Phone(pm, segment, features);
			}
		}
	}

	private void initCache(final String baseFilename) throws IOException {
		final String cacheFilename = baseFilename.replaceFirst(".*\\\\", "");
		this.pm = new PhoneManager();
		System.out.println("Cache not initialized...");
		try {
			System.out.println("Reading file <" + baseFilename + ">...");
			readFile(pm, null, baseFilename, "\t");

			final List<Phone> base = new ArrayList<>();
			base.addAll(pm.getAllPhones());

			System.out.println("Generating variants...");

			for (final Phone p : base) {
				System.out.println("\tVariants of " + p);
				recursiveApply(p, new ArrayList<>(), new ArrayList<>(List.of(Phone.Secondary.values())));
			}

			System.out.println("Saving cache...");
			final List<Phone> sorted = new ArrayList<>();
			sorted.addAll(pm.getAllPhones());
			Collections.sort(sorted);

			final File directory = new File(Main.getGlobalOption("PATH"), ".config/cache");
			if (!directory.exists())
				directory.mkdir();

			try (FileOutputStream fos = new FileOutputStream(new File(directory, cacheFilename + ".data"));
					ObjectOutputStream oos = new ObjectOutputStream(fos);) {
				oos.writeObject(this.pm);
				System.out.println("Done.");
			} catch (final Exception e3) {
				throw new IOException("Cannot save cache.");
			}
		} catch (final Exception e2) {
			throw new IOException("File <" + baseFilename + "> not in directory, cannot load base phones");
		}
	}

	public PhoneLoader(final String baseFilename, final boolean force) throws IOException {
		if (force)
			initCache(baseFilename);
		else {
			final File directory = new File(Main.getGlobalOption("PATH"), ".config/cache");
			final String cacheFilename = baseFilename.replaceFirst(".*\\\\", "");
			try (FileInputStream fis = new FileInputStream(new File(directory, cacheFilename + ".data"));
					ObjectInputStream ois = new ObjectInputStream(fis);) {
				this.pm = (PhoneManager) ois.readObject();
			} catch (final Exception e) {
				initCache(baseFilename);
			}
		}
	}

	private void recursiveApply(final Phone p, final ArrayList<Phone.Secondary> applied,
			final ArrayList<Phone.Secondary> list) {
		if (list.isEmpty())
			return;

		for (int i = 0; i < list.size(); i++) {
			if (!p.canApply(list.get(i), applied))
				continue;
			final Phone newP = p.apply(list.get(i));
			final ArrayList<Phone.Secondary> newList = new ArrayList<>();
			newList.addAll(list);
			for (int j = 0; j <= i; j++)
				newList.remove(0);
			final ArrayList<Phone.Secondary> newListA = new ArrayList<>();
			newListA.addAll(applied);
			newListA.add(list.get(i));
			recursiveApply(newP, newListA, newList);
		}
	}

	public PhoneManager getManager() {
		return pm;
	}
}