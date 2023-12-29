import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class WordCount {

    public static void main(String[] args) {
        long startTime = System.currentTimeMillis();
        String directory = "./hadoop-2.6.0/etc/hadoop"; 
	Map<String, Integer> wordFreq = new HashMap<>();
	processFilesInDirectory(directory, wordFreq);

        long endTime = System.currentTimeMillis();
        long totalTime = endTime - startTime;
	for (Map.Entry<String, Integer> entry : wordFreq.entrySet()) {
            System.out.println(entry.getKey() + ": " + entry.getValue());
        }

        System.out.println("Running time: " + totalTime + "ms");
    }

    private static void processFilesInDirectory(String directory, Map<String, Integer> wordFreq) {
        try {
            // 遍历目录下的所有文件
            	 for (String filename : new java.io.File(directory).list()) {
                 	 String filePath = directory + "/" + filename;
                         if (new java.io.File(filePath).isFile()) {
                         	 processFile(filePath, wordFreq);
                          }
                                                                                                         }
                                                                                } catch (Exception e) {
                                                                                                                             e.printStackTrace();
                                                                                                                                     }
                                                                                                                                         }

    private static void processFile(String filePath, Map<String, Integer> wordFreq) {
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
		String[] words = line.split("[ ,.?!\"'()-]");
                for (String word : words) {
                    word = word.toLowerCase().trim();
                    if (!word.isEmpty()) {
                        wordFreq.put(word, wordFreq.getOrDefault(word, 0) + 1);
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
