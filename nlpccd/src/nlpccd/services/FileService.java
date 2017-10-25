package nlpccd.services;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.UUID;

import org.apache.log4j.Logger;

import org.springframework.web.multipart.MultipartFile;

import ccd.tools.domain.TestEntry;
import ccd.tools.domain.TestFileBean;
import ccd.tools.domain.WordBean;
import ccd.tools.repository.TestFileRepository;

public class FileService {

	public static String dirPath = "/upload/";

	TestFileRepository testFileRepository = new TestFileRepository();

	private Logger logger = Logger.getLogger(FileService.class.getName());

	public ArrayList<File> getAll() {
		ArrayList<File> result = new ArrayList<File>();

		File dir = new File(dirPath);
		File[] files = dir.listFiles();
		if (files != null) {
			for (File file : files) {
				if (!file.isDirectory()) {
					result.add(file);
				}
			}
		}

		return result;
	}

	public File get(String fileName) {
		File file = new File(dirPath, fileName);
		if (file.exists()) {
			return file;
		}
		return null;
	}

	public String getContent(File file) {
		String result = null;
		try {
			BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file), "UTF-8"));
			StringBuilder builder = new StringBuilder();
			String line = null;

			while ((line = reader.readLine()) != null) {
				builder.append(line);
				builder.append("\n");
			}

			reader.close();
			result = builder.toString();
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
		return result;
	}

	public boolean upload(MultipartFile file, String usage) {
		long fileSize = 3 * 1024 * 1024;

		if (file.getSize() > fileSize) {
			return false;
		}

		File uploadDir = new File(dirPath);
		if (!uploadDir.isDirectory()) {
			if (!uploadDir.mkdir()) {
				return false;
			}
		}

		if (!uploadDir.canWrite()) {
			return false;
		}

		String newname = usage + "_" + System.currentTimeMillis();

		File saveFile = new File(dirPath, newname);

		try {
			file.transferTo(saveFile);
			// logger.info("File has been saved in " +
			// saveFile.getAbsolutePath());
			return true;
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			return false;
		}
	}

	public UUID uploadTestFile(MultipartFile file) {
		TestFileBean testFileBean = new TestFileBean();

		try {
			logger.info("Start reading test file " + file.getOriginalFilename());

			BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream(), "UTF-8"));
			String line = null;

			while ((line = reader.readLine()) != null) {
				WordBean bean = getFormattedBean(line);
				if (bean != null) {
					testFileBean.entries
							.add(new TestEntry(bean.word, bean.context, bean.index, bean.sense, "", 0.0, ""));
				}
			}
			reader.close();

			boolean result = testFileRepository.insert(testFileBean);
			if (!result) {
				testFileRepository.delete(testFileBean.no.toString());
			}

			return result ? testFileBean.no : null;
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			return null;
		}

	}

	public byte[] getFileContent(String fileName) {
		byte[] result = new byte[0];
		File file = new File(dirPath, fileName);
		if (file != null) {
			try {
				FileInputStream fis = new FileInputStream(file);
				result = new byte[(int) file.length()];
				fis.read(result);
				fis.close();
			} catch (Exception e) {
				logger.error(e.getMessage(), e);
			}
		}
		return result;
	}

	public String getFormattedOutput(WordBean bean) {
		return String.format("%s%%%s%%%s%%%s", bean.word, bean.context, bean.index, bean.sense);
	}

	public WordBean getFormattedBean(String string) {
		WordBean bean = null;

		try {
			int index = string.indexOf("%");
			if (index != -1) {
				String word = string.substring(0, index);
				string = string.substring(index + 1);
				index = string.indexOf("%");

				if (index != -1) {
					String context = string.substring(0, index);
					string = string.substring(index + 1);
					index = string.indexOf("%");

					String index_str = string;
					String sense = "";

					if (index != -1) {
						index_str = string.substring(0, index);
						sense = string.substring(index + 1);
					}
					int word_index = Integer.parseInt(index_str);

					if (context.substring(word_index).startsWith(word)) {
						bean = new WordBean(word, sense, context, word_index);
					}
				}
			}
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}

		return bean;
	}

	public boolean delete(String fileName) {
		File file = new File(dirPath, fileName);
		file.delete();
		return true;
	}
}
