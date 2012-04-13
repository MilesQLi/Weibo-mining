import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;


/**
 * 权重计算类
 * @author forhappy
 *
 */
public class WeightCalculator {
	/**
	 * 微博语料集统计类
	 */
	private CollectionStatistic cs = null;
	
	/**
	 * 计算得到的特征词权重信息写入文件
	 */
	private String fileName = null;
	
	/**
	 * 所有微博中出现的特征词
	 */
	private Set<String> setUniqueTerms = null;
	
	/**
	 * @return the cs
	 */
	public CollectionStatistic getCs() {
		return cs;
	}

	/**
	 * @param cs the cs to set
	 */
	public void setCs(CollectionStatistic cs) {
		this.cs = cs;
	}

	/**
	 * @return the fileName
	 */
	public String getFileName() {
		return fileName;
	}

	/**
	 * @param fileName the fileName to set
	 */
	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public void fillUniqueTermsSet(CollectionStatistic cs) {
		setUniqueTerms = cs.getSetUniqueTerms();
	}
	
	public WeightCalculator(CollectionStatistic cs, String fileName) {
		super();
		this.cs = cs;
		this.fileName = fileName;
		fillUniqueTermsSet(cs);
	}

	/**
	 * 统计某一词项的权重
	 * @param term 需要计算权重的特征词
	 * @param weibo 特征次所在的微博
	 * @return 词的权重
	 */
	public double calculateTermWeight(String term, WeiboXMLHandler.Weibo weibo) {
		/**
		 * Okapi's TF + IDF
		 * w(t, d) = k1 * tf / (tf + k1 * (1 - b + b * ld / E(l))) * log2((N - df + 0.5) / (df + 0.5))
		 */
		if (!setUniqueTerms.contains(term)) return 0.0;
		
		long numberOfWeibos = cs.getNumberOfWeibos();
		long termFrequency = termFrequency(term, weibo);
		long documentFrequency = documentFrequency(term);
		long documentLength = documentLength(weibo);
		double k1 = 0.75;
		double b = 0.4;
		double tf = k1 * termFrequency / (termFrequency + k1 * (1 - b + b * documentLength / cs.getAverageWeiboLength()));
		double idf = Math.log((numberOfWeibos - documentFrequency + 0.5) / (documentFrequency + 0.5));
		return tf * idf;
	}
	

	/**
	 * 计算微博中所有词的权重（包括未在该微博中出现的词）
	 * @param weibo 需要计算的微博
	 * @return 微博ID号与微博语料集中特征词的权重的Map
	 * 微博ID : <特征词1, 权重1> <特征词2, 权重2> ... <特征词n, 权重n>

	 */
	@SuppressWarnings("rawtypes")
	public Map<String, Map<String, Double>> weight(WeiboXMLHandler.Weibo weibo) {
		String docNo = weibo.getDocNo();
		//需返回的结构(微博ID : <特征词1, 权重1> <特征词2, 权重2> ... <特征词n, 权重n>)
		Map<String, Map<String, Double>> mapWeight = new HashMap<String, Map<String,Double>>();
		//特征词及其权重(<特征词1, 权重1> <特征词2, 权重2> ... <特征词n, 权重n>)
		Map<String, Double> mapTermWeight = new TreeMap<String, Double>();
		Iterator iterSetUniqueTerms = setUniqueTerms.iterator();
		while (iterSetUniqueTerms.hasNext()) {
			String term = (String) iterSetUniqueTerms.next();
			double weight = calculateTermWeight(term, weibo);
			mapTermWeight.put(term, new Double(weight));
		}
		mapWeight.put(docNo, mapTermWeight);
		return mapWeight;
	}
	
	/**
	 * 获取词的文档频率
	 * @param term 特征词
	 * @return 该特征词的文档频率
	 */
	private long documentFrequency(String term) {
		WeiboInvertedIndex weiboInvertedIndex = cs.getWeiboInvertedIndex();
		List<Map<String, Integer>> list = weiboInvertedIndex.getInvertedIndex(term);
		return list.size();
	}
	
	private long termFrequency(String term, WeiboXMLHandler.Weibo weibo) {
		long termFrequency = 0;
		String[] terms = weibo.getWeiboText().split("\\s+"); 
		for (String t : terms) {
			if (term.equals(t)) termFrequency++;
		}
		return termFrequency;
	}
	
	private short documentLength(WeiboXMLHandler.Weibo weibo) {
		int documentLength = 0;
		String text = weibo.getWeiboText();
		documentLength = text.length();
		return (short) documentLength;
		
	}
}