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
		String word = "һ";
		String sense = "���岢ͬ���͡�";
		String context = "��������һҲ��ͬ";
		String feature = "��34��~";
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

		String[] contexts = { " �����۶��ɳ��У�������ҫ������ ���ݴ�����ʮ�򣬹�����Ӫһǧ�", "���ӳ�һ�������缧���缧����ŵ��", "һ��һӽ�������Գ������顣", "����һҲ��",
				"������һ�գ����ǧ�", "һ��������", "�˵�һΪ�𣬹�����������", "Ϊ��һ���֣����������ɡ�", "�Ž�һҲ��", "��һ��ս���پ�������", "��֪һ����Ϊ�鵮��������Ϊ������",
				"�����ϣ��ĺ�һ��", "һ����顣", "�������ң�����һ�����ҡ�" };

		/*
		 * ArrayList<WordBean> wordsource = tools.
		 * getWordsBean("SELECT * FROM wordsourcecontexts order by `word`, `sense` limit 99999"
		 * );
		 * 
		 * for(WordBean bean : wordsource) { String contextb =
		 * bean.getContext();
		 * 
		 * if(contextb.charAt(contextb.length() - 1) == '��') { tools.
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
		String string = "һ";
		for (int i = 0; i < string.length(); i++) {
			strings.add(string.charAt(i) + "");
		}
		CRFTraining training = new CRFTraining(strings, null);
		training.start();

		// 7326

		// WordSenseRepository wordSenseRepository = new WordSenseRepository();
		// HashMap<String, WordSensePr> prior = wordSenseRepository.initPrior();
		// CRFClassifier crf = new CRFClassifier(prior, 10);
		// DisambResult result = crf.Disambiguate("��", "�޲������֮��,��Ҳ��", 4);
		// for (String l : result.getMax()) {
		// System.out.println(l);
		// }
		// System.out.println(result.getConfidence());
	}

}
