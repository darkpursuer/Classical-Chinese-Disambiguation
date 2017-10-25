package nlpccd.services;

import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.UUID;

import org.apache.log4j.Logger;

import ccd.crf.CRFClassifier;
import ccd.crf.CRFTraining;
import ccd.tools.domain.ICallback;
import ccd.tools.domain.PageResult;
import ccd.tools.domain.TestEntry;
import ccd.tools.domain.TrainingHistoryBean;
import ccd.tools.domain.WordBean;
import ccd.tools.domain.WordOverviewBean;
import ccd.tools.repository.ConfigRepository;
import ccd.tools.repository.TestFileRepository;
import ccd.tools.repository.WordSourceRepository;
import nlpccd.models.QueryModel;
import nlpccd.models.RetrainRequestModel;
import nlpccd.models.TrainingHistoryModel;
import nlpccd.models.WordModel;
import nlpccd.models.WordOverviewModel;

public class CorpusService {

	public static double ConfidenceThreshold = 0.9;

	Logger logger = Logger.getLogger(CorpusService.class.getName());

	FileService fileService = new FileService();
	WordSourceRepository wordSourceRepository = new WordSourceRepository();
	ConfigRepository configRepository = new ConfigRepository();
	TestFileRepository testFileRepository = new TestFileRepository();

	public PageResult<WordOverviewModel> getWordOverview(QueryModel model) {
		PageResult<WordOverviewBean> bean = wordSourceRepository.getOverview(model.searchString, model.pageIndex,
				model.pageSize);

		PageResult<WordOverviewModel> result = new PageResult<>(bean.pageIndex, bean.pageSize);
		result.totalCount = bean.totalCount;
		result.data = new ArrayList<>();

		for (WordOverviewBean b : bean.data) {
			result.data.add(WordOverviewModel.convertTo(b));
		}
		return result;
	}

	public WordModel getWord(String word) {
		ArrayList<WordBean> beans = wordSourceRepository.get(word);

		return WordModel.convertTo(beans);
	}

	public boolean updateSenses(String word, ArrayList<String> senses, String targetSense) {
		return wordSourceRepository.updateSenses(word, senses, targetSense);
	}

	public String train() {
		return train(null);
	}

	public String train(ArrayList<String> words) {
		try {
			TrainingHistoryBean bean = configRepository.getLatestTrainingHistory();
			if (bean == null || bean.status != 1) {
				bean = new TrainingHistoryBean();
				if (words != null) {
					bean.scope = words;
				}
				configRepository.addTrainingHistory(bean);

				if (words != null && words.isEmpty()) {
					words = null;
				}
				CRFTraining training = new CRFTraining(words, new ICallback() {
					@Override
					public void run() {
						DisambiguationService.reset();
						CRFClassifier.reset();
					}
				});
				training.start();

				return bean.no + "";
			} else {
				return null;
			}
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			return null;
		}
	}

	/**
	 * 
	 * @param testFileId:
	 *            the id of test file
	 * @param type:
	 *            the type of retrain. 0: retrain all words that has real sense;
	 *            1: retrain the selected tagged words; 2: retrain the uni-sense
	 *            words; 3: retrain the high confidence words
	 */
	public String retrain(UUID testFileId, int type, RetrainRequestModel[] models) {
		String result = "";
		ArrayList<TestEntry> entries = new ArrayList<>();
		if (type == 0) {
			entries = testFileRepository.getEntries(testFileId, null, 0, 999999999, 3).data;
			if (!entries.isEmpty()) {
				ArrayList<String> words = new ArrayList<>();
				ArrayList<WordBean> beans = new ArrayList<>();

				for (TestEntry entry : entries) {
					if (!entry.isRetrained) {
						beans.add(new WordBean(entry.word, entry.realSense, entry.context, entry.index));
						if (!words.contains(entry.word)) {
							words.add(entry.word);
						}
						entry.tag = "1";
					}
				}
				boolean insertResult = wordSourceRepository.insert(beans);
				if (insertResult) {
					testFileRepository.updateRetrainState(entries);
					// wordSourceRepository.removeRedundant();
					result = train(words);
				}
			}
		} else if (type == 1) {
			ArrayList<UUID> idList = new ArrayList<>();
			ArrayList<UUID> errorList = new ArrayList<>();
			for (RetrainRequestModel model : models) {
				if (model.tag == 1) {
					idList.add(UUID.fromString(model.id));
				} else if (model.tag == 2) {
					errorList.add(UUID.fromString(model.id));
				}
			}

			entries = testFileRepository.getEntries(testFileId, idList);
			ArrayList<TestEntry> errorEntries = testFileRepository.getEntries(testFileId, errorList);
			ArrayList<String> words = new ArrayList<>();
			ArrayList<WordBean> beans = new ArrayList<>();

			for (TestEntry entry : entries) {
				if (!entry.isRetrained) {
					beans.add(new WordBean(entry.word, entry.testSense, entry.context, entry.index));
					if (!words.contains(entry.word)) {
						words.add(entry.word);
					}
					entry.tag = "1";
				}
			}
			for (TestEntry entry : errorEntries) {
				if (!entry.isRetrained) {
					ArrayList<String> senses = DisambiguationService.prior.get(entry.word).senses;
					if (senses.size() == 2) {
						String correctSense = senses.get(0);
						if (correctSense.equals(entry.testSense)) {
							correctSense = senses.get(1);
						}
						beans.add(new WordBean(entry.word, correctSense, entry.context, entry.index));
						if (!words.contains(entry.word)) {
							words.add(entry.word);
						}
					}
					entry.tag = "2";
				}
			}
			boolean insertResult = wordSourceRepository.insert(beans);
			if (insertResult) {
				testFileRepository.updateRetrainState(entries);
				testFileRepository.updateRetrainState(errorEntries);
				// wordSourceRepository.removeRedundant();
				if (!words.isEmpty()) {
					result = train(words);
				}
			}
		} else if (type == 2) {
			entries = testFileRepository.getEntries(testFileId, null, 0, 999999999, 4).data;
			if (!entries.isEmpty()) {
				ArrayList<String> words = new ArrayList<>();
				ArrayList<WordBean> beans = new ArrayList<>();
				ArrayList<TestEntry> retrainedEntries = new ArrayList<>();

				for (TestEntry entry : entries) {
					if (!entry.isRetrained) {
						ArrayList<String> senses = DisambiguationService.prior.get(entry.word).senses;
						if (words.contains(entry.word)) {
							beans.add(new WordBean(entry.word, senses.get(0), entry.context, entry.index));
							entry.tag = "1";
							retrainedEntries.add(entry);
						} else {
							if (senses.size() == 1) {
								words.add(entry.word);
								beans.add(new WordBean(entry.word, senses.get(0), entry.context, entry.index));
								entry.tag = "1";
								retrainedEntries.add(entry);
							}
						}
					}
				}
				boolean insertResult = wordSourceRepository.insert(beans);
				if (insertResult) {
					testFileRepository.updateRetrainState(retrainedEntries);
					// wordSourceRepository.removeRedundant();
					result = train(words);
				}
			}
		} else if (type == 3) {
			entries = testFileRepository.getEntries(testFileId, null, 0, 999999999, 4).data;
			if (!entries.isEmpty()) {
				ArrayList<String> words = new ArrayList<>();
				ArrayList<WordBean> beans = new ArrayList<>();
				ArrayList<TestEntry> retrainedEntries = new ArrayList<>();

				for (TestEntry entry : entries) {
					if (!entry.isRetrained) {
						if (entry.confidence >= ConfidenceThreshold) {
							beans.add(new WordBean(entry.word, entry.testSense, entry.context, entry.index));
							if (!words.contains(entry.word)) {
								words.add(entry.word);
							}
							entry.tag = "1";
							retrainedEntries.add(entry);
						}
					}
				}
				boolean insertResult = wordSourceRepository.insert(beans);
				if (insertResult) {
					testFileRepository.updateRetrainState(retrainedEntries);
					// wordSourceRepository.removeRedundant();
					result = train(words);
				}
			}
		}

		return result;
	}

	public byte[] getCorpus() {
		ArrayList<WordBean> beans = wordSourceRepository.getAllWordBeans();

		byte[] result = new byte[0];
		try {
			ByteArrayOutputStream bao = new ByteArrayOutputStream();

			BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(bao));
			for (WordBean wordBean : beans) {
				writer.write(fileService.getFormattedOutput(wordBean));
				writer.newLine();
			}

			writer.close();
			result = bao.toByteArray();
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
		return result;
	}

	public TrainingHistoryModel getProgress(String no) {
		TrainingHistoryBean bean = configRepository.getTrainingHistory(no);
		if (bean == null) {
			return null;
		}
		return TrainingHistoryModel.convertTo(bean);
	}

	public TrainingHistoryModel getCurrentProgress() {
		TrainingHistoryBean bean = configRepository.getLatestTrainingHistory();
		if (bean != null && bean.status != 1) {
			bean = null;
		}
		if (bean == null) {
			return null;
		}
		return TrainingHistoryModel.convertTo(bean);
	}

	public ArrayList<TrainingHistoryModel> getProgresses() {
		ArrayList<TrainingHistoryBean> beans = configRepository.getTrainingHistoryList();
		ArrayList<TrainingHistoryModel> result = new ArrayList<>();
		for (TrainingHistoryBean bean : beans) {
			result.add(TrainingHistoryModel.convertTo(bean));
		}
		return result;
	}
}
