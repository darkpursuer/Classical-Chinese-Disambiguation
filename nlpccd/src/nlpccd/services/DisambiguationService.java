package nlpccd.services;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import org.apache.log4j.Logger;

import ccd.crf.CRFClassifier;
import ccd.tools.domain.DisambResult;
import ccd.tools.domain.PageResult;
import ccd.tools.domain.TestEntry;
import ccd.tools.domain.TestFileBean;
import ccd.tools.domain.WordSensePr;
import ccd.tools.repository.TestFileRepository;
import ccd.tools.repository.WordSenseRepository;
import nlpccd.models.DisambResultModel;
import nlpccd.models.TestEntryModel;
import nlpccd.models.TestFileModel;

public class DisambiguationService {

	Logger logger = Logger.getLogger(DisambiguationService.class.getName());

	static WordSenseRepository wordSenseRepository = new WordSenseRepository();
	TestFileRepository testFileRepository = new TestFileRepository();

	public static HashMap<String, WordSensePr> prior = wordSenseRepository.initPrior();
	static CRFClassifier crf = new CRFClassifier(prior, 10);

	public DisambResultModel disambiguate(String word, String context, int index) {
		DisambResultModel result = new DisambResultModel();
		result.word = word;
		result.context = context;
		result.index = index + "";

		result.senses = crf.Disambiguate(word, context, index).getMax();

		return result;
	}

	public UUID retest(UUID oldId) {
		UUID result = null;
		ArrayList<TestEntry> entries = testFileRepository.getEntries(oldId, null, 0, 999999999, 6).data;
		if (!entries.isEmpty()) {
			TestFileBean bean = new TestFileBean();
			for (TestEntry entry : entries) {
				bean.entries.add(new TestEntry(entry.word, entry.context, entry.index, entry.realSense, "", 0.0, ""));
			}

			boolean insertResult = testFileRepository.insert(bean);
			if (!insertResult) {
				testFileRepository.delete(bean.no.toString());
			}
			result = bean.no;

			disambiguate(result);
		}

		return result;
	}

	public boolean disambiguate(UUID id) {
		new Thread(new Runnable() {

			private static final int MAXTHREADNUM = 10;

			@Override
			public void run() {
				TestFileBean bean = testFileRepository.get(id, true);
				int totalCount = bean.entries.size();

				Date start = convertDate(bean.date);

				///
				int finished = 0;
				ArrayList<Integer> finishedIndexes = new ArrayList<Integer>();

				int threadNum = Math.min((int) Math.ceil((double) totalCount / (double) MAXTHREADNUM), MAXTHREADNUM);
				int taskNum = (int) Math.ceil((double) totalCount / (double) threadNum);

				InnerDisamb[] threads = new InnerDisamb[threadNum];

				int base = 0;
				for (int i = 0; i < threadNum; i++) {
					int end = Math.min(base + taskNum - 1, totalCount - 1);
					threads[i] = new InnerDisamb(bean.entries, base, end, finishedIndexes);
					threads[i].start();
					base = base + taskNum;
				}

				base = 0;
				while (finished < totalCount) {
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e) {
						logger.error(e.getMessage(), e);
					}

					ArrayList<TestEntry> entries = new ArrayList<>();
					synchronized (finishedIndexes) {
						finished = finishedIndexes.size();
					}
					for (int i = base; i < finished; i++) {
						entries.add(bean.entries.get(finishedIndexes.get(i)));
					}

					boolean insertResult = testFileRepository.insertTestSense(bean.no, entries);
					testFileRepository.updateProgress(bean.no, (int) ((float) finished / (float) totalCount * 100));

					if (insertResult) {
						base = finished;
					}
				}

				///

				int totalTaggedCount = 0;
				int totalCorrectCount = 0;

				for (int i = 0; i < totalCount; i++) {
					TestEntry entry = bean.entries.get(i);
					String realSense = entry.realSense;
					String testSense = entry.testSense;
					if (realSense != null && !realSense.isEmpty()) {
						totalTaggedCount++;
						if (testSense.equals(realSense)) {
							totalCorrectCount++;
						}
					}
				}

				Date end = new Date();
				int interval = (int) ((end.getTime() - start.getTime()) / 1000);

				bean.progress = 100;
				bean.isCompleted = true;
				bean.result = String.format(
						"Total tagged entries: %d, Correct entries: %d, Correct rate: %2f. Total time consuming: %d s, consuming time per entry: %2f s",
						totalTaggedCount, totalCorrectCount, (float) totalCorrectCount / (float) totalTaggedCount,
						interval, (float) interval / (float) totalCount);
				testFileRepository.update(bean);
			}
		}).start();

		return true;
	}

	public ArrayList<TestFileModel> getTestRecords(boolean needData) {
		ArrayList<TestFileBean> beans = testFileRepository.getAll(needData);
		ArrayList<TestFileModel> result = new ArrayList<>();

		for (TestFileBean bean : beans) {
			result.add(TestFileModel.convertTo(bean));
		}

		return result;
	}

	public TestFileModel getTestRecord(UUID no, boolean needData) {
		TestFileBean bean = testFileRepository.get(no, needData);
		return TestFileModel.convertTo(bean);
	}

	public PageResult<TestEntryModel> getEntries(UUID no, String word, int pageIndex, int pageSize, String showType) {
		int type = 0;
		if (showType.equals("errors")) {
			type = 1;
		} else if (showType.equals("notagged")) {
			type = 2;
		} else if (showType.equals("tested")) {
			type = 4;
		} else if (showType.equals("notretrained")) {
			type = 5;
		}

		PageResult<TestEntry> bean = testFileRepository.getEntries(no, word, pageIndex, pageSize, type);

		PageResult<TestEntryModel> result = new PageResult<>(bean.pageIndex, bean.pageSize);
		result.totalCount = bean.totalCount;
		result.data = new ArrayList<>();

		for (TestEntry b : bean.data) {
			result.data.add(TestEntryModel.convertTo(b));
		}
		return result;
	}

	public boolean deleteTestFile(UUID no) {
		return testFileRepository.delete(no.toString());
	}

	public static void reset() {
		prior = wordSenseRepository.initPrior();
		crf = new CRFClassifier(prior, 10);
	}

	private Date convertDate(String str) {
		SimpleDateFormat format = new SimpleDateFormat("EEE MMM dd hh:mm:ss z yyyy", Locale.ENGLISH);
		try {
			Date date = format.parse(str);
			return date;
		} catch (ParseException e) {
			logger.error(e.getMessage(), e);
			return null;
		}
	}

	class InnerDisamb extends Thread {

		private ArrayList<TestEntry> entries;
		int start, end;
		List<Integer> resultIndexes;

		public InnerDisamb(ArrayList<TestEntry> entries, int start, int end, List<Integer> resultIndexes) {
			this.entries = entries;
			this.start = start;
			this.end = end;
			this.resultIndexes = resultIndexes;
		}

		public void run() {
			for (int i = start; i <= end; i++) {
				TestEntry entry = entries.get(i);

				DisambResult result = crf.Disambiguate(entry.word, entry.context, entry.index);
				String testSense = result == null ? "" : String.join(", ", result.getMax());

				entry.testSense = testSense;
				if (result != null)
					entry.confidence = result.getConfidence();

				synchronized (resultIndexes) {
					resultIndexes.add(i);
				}
			}
		}
	}
}
