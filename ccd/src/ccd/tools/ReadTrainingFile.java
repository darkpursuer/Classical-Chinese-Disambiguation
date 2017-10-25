package ccd.tools;

import java.io.*;
import java.util.ArrayList;

import ccd.tools.domain.WordBean;
import ccd.tools.repository.WordSourceRepository;
import ccd.training.TrainingMain;

public class ReadTrainingFile {

	private static WordSourceRepository repository = new WordSourceRepository();

	public static void readTrainingFile(String filename, boolean isOverwritten) throws Exception {
		if (isOverwritten) {
			repository.truncate();
		}

		String seperator = TrainingMain.seperator;

		File file = new File(filename);
		BufferedReader reader = null;
		InputStreamReader isr = new InputStreamReader(new FileInputStream(file), "UTF-8");
		reader = new BufferedReader(isr);

		String word, sense, context, index;

		ArrayList<WordBean> beans = new ArrayList<WordBean>();
		String line;
		while ((line = reader.readLine()) != null) {
			String[] tmp = line.split(seperator);

			try {
				word = tmp[0];
				context = tmp[1];
				index = tmp[2];
				sense = tmp[3];

				WordBean bean = new WordBean(word, sense, context, Integer.parseInt(index));
				beans.add(bean);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		repository.insert(beans);
		repository.removeRedundant();

		isr.close();
		reader.close();
	}
}
