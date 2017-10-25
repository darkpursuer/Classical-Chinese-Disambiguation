package nlpccd.controllers;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import javax.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Controller;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;

import ccd.tools.ReadTrainingFile;
import ccd.tools.domain.PageResult;
import nlpccd.models.FileInfoModel;
import nlpccd.models.MergeSensesModel;
import nlpccd.models.QueryModel;
import nlpccd.models.TrainingHistoryModel;
import nlpccd.models.WordModel;
import nlpccd.models.WordOverviewModel;
import nlpccd.services.CorpusService;
import nlpccd.services.FileService;

import org.apache.log4j.Logger;
import org.codehaus.jackson.map.ObjectMapper;

@Controller
public class TrainController {

	static Logger logger = Logger.getLogger(TrainController.class.getName());
	FileService fileService = new FileService();
	CorpusService corpusService = new CorpusService();

	@RequestMapping(value = "/train", method = RequestMethod.GET)
	public ModelAndView index() {
		ArrayList<TrainingHistoryModel> models = corpusService.getProgresses();

		String modelStr = "";
		ObjectMapper mapper = new ObjectMapper();
		try {
			modelStr = mapper.writeValueAsString(models);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}

		HashMap<String, String> model = new HashMap<>();
		model.put("records", modelStr);

		return new ModelAndView("train", model);
	}

	@RequestMapping(value = "/train/corpus", method = RequestMethod.GET)
	public ModelAndView corpus() {
		return new ModelAndView("train_corpus");
	}

	@RequestMapping(value = "/train/files", method = RequestMethod.GET)
	public ModelAndView files() {
		ArrayList<File> files = fileService.getAll();
		ArrayList<FileInfoModel> models = new ArrayList<>();
		for (File file : files) {
			FileInfoModel model = new FileInfoModel();
			model.fileName = file.getName();
			model.uploadDate = new Date(file.lastModified()).toString();
			model.size = file.length() + " B";

			models.add(model);
		}

		String modelStr = "";
		ObjectMapper mapper = new ObjectMapper();
		try {
			modelStr = mapper.writeValueAsString(models);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}

		HashMap<String, String> model = new HashMap<>();
		model.put("files", modelStr);

		return new ModelAndView("train_files", model);
	}

	@RequestMapping(value = "/train/word/{word}", method = RequestMethod.GET)
	public ModelAndView word(@PathVariable String word) {
		WordModel wordModel = corpusService.getWord(word);
		String wordModelString = "";

		ObjectMapper mapper = new ObjectMapper();
		try {
			wordModelString = mapper.writeValueAsString(wordModel);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}

		HashMap<String, String> model = new HashMap<>();
		model.put("word", word);
		model.put("model", wordModelString);
		return new ModelAndView("train_word", model);
	}

	@RequestMapping(value = "/train", method = RequestMethod.POST)
	public @ResponseBody String train() {
		String result = corpusService.train();
		return result;
	}

	@RequestMapping(value = "/train/words", method = RequestMethod.POST)
	public @ResponseBody String train(@RequestParam("words") String words) {
		ObjectMapper mapper = new ObjectMapper();

		try {
			String[] list = mapper.readValue(words, String[].class);
			ArrayList<String> array = new ArrayList<String>();
			for (String string : list) {
				array.add(string);
			}
			String result = corpusService.train(array);
			return result;
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			return "";
		}
	}

	@RequestMapping(value = "/train/progress/current", method = RequestMethod.POST)
	public @ResponseBody String getCurrentProgress() {
		TrainingHistoryModel model = corpusService.getCurrentProgress();
		ObjectMapper mapper = new ObjectMapper();

		try {
			return mapper.writeValueAsString(model);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			return "";
		}
	}

	@RequestMapping(value = "/train/progress", method = RequestMethod.POST)
	public @ResponseBody String getProgress(@RequestParam("no") String no) {
		TrainingHistoryModel model = corpusService.getProgress(no);
		ObjectMapper mapper = new ObjectMapper();

		try {
			return mapper.writeValueAsString(model);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			return "";
		}
	}

	@RequestMapping(value = "/train/getwords", method = RequestMethod.POST)
	public @ResponseBody String getWords(@RequestParam("query") String query) {
		ObjectMapper mapper = new ObjectMapper();

		try {
			QueryModel queryModel = mapper.readValue(query, QueryModel.class);

			PageResult<WordOverviewModel> wordList = corpusService.getWordOverview(queryModel);

			return mapper.writeValueAsString(wordList);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			return null;
		}
	}

	@RequestMapping(value = "/train/mergesenses", method = RequestMethod.POST)
	public @ResponseBody String mergeSenses(@RequestParam("data") String data) {
		ObjectMapper mapper = new ObjectMapper();
		boolean result = true;

		try {
			MergeSensesModel model = mapper.readValue(data, MergeSensesModel.class);

			result = corpusService.updateSenses(model.word, model.senses, model.targetSense);

		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			result = false;
		}
		return result ? "success" : "";
	}

	@RequestMapping(value = "/train/corpus/download", method = RequestMethod.GET)
	public void downloadCorpus(HttpServletResponse response) {
		byte[] bytes = corpusService.getCorpus();
		try {
			FileCopyUtils.copy(bytes, response.getOutputStream());
		} catch (IOException e) {
			logger.error(e.getMessage(), e);
		}
	}

	@RequestMapping(value = "/train/upload", method = RequestMethod.POST)
	public void upload(@RequestParam("file") MultipartFile file) {

		logger.info("file name is :" + file.getOriginalFilename());

		if (!file.isEmpty()) {
			fileService.upload(file, "train");
		}
	}

	@RequestMapping(value = "/train/file/download/{fileName}", method = RequestMethod.GET)
	public void downloadFile(@PathVariable("fileName") String fileName, HttpServletResponse response) {
		byte[] bytes = fileService.getFileContent(fileName);
		try {
			FileCopyUtils.copy(bytes, response.getOutputStream());
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
	}

	@RequestMapping(value = "/train/file/load", method = RequestMethod.POST)
	public @ResponseBody String loadFilesIntoDatabase(@RequestParam("fileName") String fileName) {
		String result = "";
		File file = fileService.get(fileName);
		if (file != null) {
			try {
				ReadTrainingFile.readTrainingFile(file.getAbsolutePath(), false);
				result = "success";
			} catch (Exception e) {
				logger.error(e.getMessage(), e);
			}
		}
		return result;
	}

	@RequestMapping(value = "/train/file/delete", method = RequestMethod.POST)
	public @ResponseBody String deleteFile(@RequestParam("fileName") String fileName) {
		boolean result = true;
		try {
			result = fileService.delete(fileName);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			result = false;
		}
		return result ? "success" : "";
	}
}
