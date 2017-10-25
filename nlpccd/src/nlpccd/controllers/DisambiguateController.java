package nlpccd.controllers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

import org.apache.log4j.Logger;
import org.codehaus.jackson.map.ObjectMapper;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;

import ccd.tools.domain.PageResult;
import nlpccd.models.DisambRequestModel;
import nlpccd.models.DisambResultModel;
import nlpccd.models.QueryModel;
import nlpccd.models.RetrainRequestModel;
import nlpccd.models.TestEntryModel;
import nlpccd.models.TestFileModel;
import nlpccd.services.CorpusService;
import nlpccd.services.DisambiguationService;
import nlpccd.services.FileService;

@Controller
public class DisambiguateController {

	static Logger logger = Logger.getLogger(TrainController.class.getName());

	FileService fileService = new FileService();
	DisambiguationService disambiguationService = new DisambiguationService();
	CorpusService corpusService = new CorpusService();

	@RequestMapping(value = "/", method = RequestMethod.GET, produces = "text/plain;charset=UTF-8")
	public ModelAndView index() {
		return new ModelAndView("index");
	}

	@RequestMapping(value = "/dis/testfiles", method = RequestMethod.GET)
	public ModelAndView testFiles() {
		ArrayList<TestFileModel> models = disambiguationService.getTestRecords(false);

		String modelStr = "";
		ObjectMapper mapper = new ObjectMapper();
		try {
			modelStr = mapper.writeValueAsString(models);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}

		HashMap<String, String> model = new HashMap<>();
		model.put("records", modelStr);

		return new ModelAndView("dis_testfiles", model);
	}

	@RequestMapping(value = "/dis/file/{no}", method = RequestMethod.GET)
	public ModelAndView getTestFile(@PathVariable String no) {
		TestFileModel testFileModel = disambiguationService.getTestRecord(UUID.fromString(no), false);

		String modelStr = "";
		ObjectMapper mapper = new ObjectMapper();
		try {
			modelStr = mapper.writeValueAsString(testFileModel);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}

		HashMap<String, String> model = new HashMap<>();
		model.put("details", modelStr);

		return new ModelAndView("dis_files", model);
	}

	@RequestMapping(value = "/dis/disamb", method = RequestMethod.POST)
	public @ResponseBody String disambiguate(@RequestParam("model") String model_str) {

		ObjectMapper mapper = new ObjectMapper();

		try {
			DisambRequestModel model = mapper.readValue(model_str, DisambRequestModel.class);

			DisambResultModel result = disambiguationService.disambiguate(model.word, model.context,
					Integer.parseInt(model.index));
			return mapper.writeValueAsString(result);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			return null;
		}
	}

	@RequestMapping(value = "/dis/uploadtestfile", method = RequestMethod.POST)
	public @ResponseBody String upload(@RequestParam("file") MultipartFile file) {
		UUID id = fileService.uploadTestFile(file);
		boolean result = id != null;
		if (id != null) {
			disambiguationService.disambiguate(id);
		}
		return result ? id.toString() : "";
	}

	@RequestMapping(value = "/dis/test/progress/{no}", method = RequestMethod.POST)
	public @ResponseBody String getProgress(@PathVariable String no) {
		TestFileModel model = disambiguationService.getTestRecord(UUID.fromString(no), false);
		ObjectMapper mapper = new ObjectMapper();

		try {
			return mapper.writeValueAsString(model);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			return "";
		}
	}

	@RequestMapping(value = "/dis/testfile/delete/{no}", method = RequestMethod.POST)
	public @ResponseBody String deleteTestFile(@PathVariable String no) {
		boolean result = disambiguationService.deleteTestFile(UUID.fromString(no));
		return result ? "success" : "";
	}

	@RequestMapping(value = "/dis/file/{no}/entries", method = RequestMethod.POST)
	public @ResponseBody String getWords(@PathVariable String no, @RequestParam("query") String query) {
		ObjectMapper mapper = new ObjectMapper();

		try {
			QueryModel queryModel = mapper.readValue(query, QueryModel.class);

			PageResult<TestEntryModel> entryList = disambiguationService.getEntries(UUID.fromString(no),
					queryModel.searchString, queryModel.pageIndex, queryModel.pageSize, queryModel.orderBy);

			return mapper.writeValueAsString(entryList);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			return "";
		}
	}

	@RequestMapping(value = "/dis/file/{no}/retrain", method = RequestMethod.POST)
	public @ResponseBody String retrain(@PathVariable String no, @RequestParam("param") String param,
			@RequestParam("type") String typeStr) {
		ObjectMapper mapper = new ObjectMapper();

		try {
			RetrainRequestModel[] models = null;
			if (param != null && !param.isEmpty()) {
				models = mapper.readValue(param, RetrainRequestModel[].class);
			}
			int type = Integer.parseInt(typeStr);

			String result = corpusService.retrain(UUID.fromString(no), type, models);

			return result;
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			return "";
		}
	}

	@RequestMapping(value = "/dis/file/{no}/retest", method = RequestMethod.POST)
	public @ResponseBody String retest(@PathVariable String no) {
		UUID result = disambiguationService.retest(UUID.fromString(no));
		return result == null ? "" : result.toString();
	}
}
