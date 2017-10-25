package ccd.crf;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;

import ccd.tools.domain.DisambResult;
import ccd.tools.domain.FeatParaBean;
import ccd.tools.domain.WordBean;
import ccd.tools.domain.WordSensePr;
import ccd.tools.repository.WordSenseRepository;

public class TestTools {

	public static void main(String[] args) {
		String word = "一";
		String sense = "音义并同“赐”";
		String context = "音义用心一也并同";
		String feature = "心34用~";
		int field = 3;

		/*
		 * //generate the features ArrayList<FeatParaBean> g_features =
		 * CRFTools.generateFuzzyInner(word, sense, context, field);
		 * g_features.addAll(CRFTools.generateInner(word, sense, context,
		 * field));
		 * 
		 * for(FeatParaBean bean : g_features) { System.out.printf("%s\t%b\n",
		 * bean.feature_str, CRFTools.compareFuzzyFeature(word, context,
		 * bean.feature_str)); }
		 * 
		 * //System.out.println(CRFTools.compareFuzzyFeature(word, context,
		 * feature));
		 * 
		 * SQLTools tools = new SQLTools();
		 * 
		 * /* ArrayList<FeatParaBean> paraList =
		 * CRFTools.generateFeatures(tools, word, sense, context, field);
		 * 
		 * for(FeatParaBean bean : paraList) { System.out.
		 * printf("sense: %s, index: %d, feature: %s, parameter: %d\n",
		 * bean.sense, bean.feature, bean.feature_str, bean.parameter); }
		 * 
		 * /* HashMap<Integer, String> features = CRFTools.findFeatures(tools,
		 * word, context);
		 * 
		 * ArrayList<Integer> O = new ArrayList<Integer>();
		 * 
		 * Iterator<Entry<Integer, String>> iter =
		 * features.entrySet().iterator();
		 * 
		 * while(iter.hasNext()) { Entry<Integer, String> entry =
		 * (Entry<Integer, String>) iter.next(); int index = entry.getKey();
		 * String feature = entry.getValue();
		 * 
		 * O.add(index);
		 * 
		 * System.out.println(feature); } /*
		 */

		// System.out.println(CRFTools.compareFeature(word, context, feature));

		String[] contexts = { " 忆昨雄都旧朝市，轩车照耀歌钟起， 军容带甲三十万，国步连营一千里。", "公子诚一开口请如姬，如姬必许诺。", "一觞一咏，亦足以畅叙幽情。", "用心一也。",
				"而或长烟一空，皓月千里。", "一鼓作气。", "此地一为别，孤蓬万里征。", "为我一挥手，如听万壑松。", "古今一也。", "初一交战，操军不利。", "固知一死生为虚诞，齐彭殇为妄作。",
				"六王毕，四海一。", "一屠晚归。", "王事适我，政事一埤益我。" };

		/*
		 * ArrayList<WordBean> wordsource = tools.
		 * getWordsBean("SELECT * FROM wordsourcecontexts order by `word`, `sense` limit 99999"
		 * );
		 * 
		 * for(WordBean bean : wordsource) { String contextb =
		 * bean.getContext();
		 * 
		 * if(contextb.charAt(contextb.length() - 1) == '。') { tools.
		 * executeSQL("UPDATE `wordset`.`wordsourcecontexts` SET `context`='" +
		 * contextb.substring(0, contextb.length() - 1) + "' WHERE `No`='" +
		 * bean.getNo() + "';"); }
		 * 
		 * ; }
		 * 
		 * /* for(int i = 0; i < contexts.length; i++) {
		 * System.out.println(contexts[i]);
		 * 
		 * HashMap<Integer, String> result = CRFTools.findFeatures(tools, word,
		 * contexts[i]);
		 * 
		 * Iterator<Entry<Integer, String>> iter = result.entrySet().iterator();
		 * 
		 * while(iter.hasNext()) { Entry<Integer, String> entry =
		 * (Entry<Integer, String>) iter.next(); int index = entry.getKey();
		 * String f = entry.getValue();
		 * 
		 * System.out.println(f); } } /*
		 */

		ArrayList<String> strings = new ArrayList<>();
		String string = "一";
		for (int i = 0; i < string.length(); i++) {
			strings.add(string.charAt(i) + "");
		}
		CRFTraining training = new CRFTraining(strings, null);
		training.start();

		// 7326

		// WordSenseRepository wordSenseRepository = new WordSenseRepository();
		// HashMap<String, WordSensePr> prior = wordSenseRepository.initPrior();
		// CRFClassifier crf = new CRFClassifier(prior, 10);
		// DisambResult result = crf.Disambiguate("必", "无参验而必之者,愚也。", 4);
		// for (String l : result.getMax()) {
		// System.out.println(l);
		// }
		// System.out.println(result.getConfidence());
	}

}
