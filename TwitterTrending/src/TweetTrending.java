package org.myorg;
import java.io.IOException;
import java.util.*;
import java.io.*;

import org.apache.hadoop.fs.Path;
import org.apache.hadoop.conf.*;
import org.apache.hadoop.io.*;
import org.apache.hadoop.mapred.*;
import org.apache.hadoop.util.*;
import org.apache.hadoop.mapred.lib.InverseMapper;

public class TweetTrending {

  public static class Map extends MapReduceBase implements Mapper<LongWritable, Text, Text, IntWritable> {
    private final static IntWritable one = new IntWritable(1);
    private Text word = new Text();
    private boolean strAllLetters(String word) {   
        for (int i = 0; i < word.length(); i++) {  
            if (!(word.charAt(i) >= 'A' && word.charAt(i) <= 'Z')  
                    && !(word.charAt(i) >= 'a' && word.charAt(i) <= 'z')) {  
                return false;  
            }  
        }  
        return true;  
    }  
    public void map(LongWritable key, Text value, OutputCollector<Text, IntWritable> output, Reporter reporter) throws IOException {
      String content = new Scanner(new File("./stopwordsList.txt")).useDelimiter("\\Z").next();
      String[] stopwords = content.split(", ");
      List<String> stopwordsList = Arrays.asList(stopwords);
      String line = value.toString().toLowerCase();
      String[] words = line.split("\\s+|,|\\.|!|\\?|;|:|\\+|\\-|\\*|\\\\|\\||\"|\'|\\/|~|`|\\@|#|\\$|%|\\^|&|\\(|\\)|\\_|\\[|\\]|\\{|\\}|\\<|\\>");
      List<String> wordsList = Arrays.asList(words);
      
      for (String token : wordsList) {       
        if(!stopwordsList.contains(token) && strAllLetters(token)){
          word.set(token);
          output.collect(word, one);
        }
        
      }
    }
  }

  public static class Reduce extends MapReduceBase implements Reducer<Text, IntWritable, Text, IntWritable> {
    public void reduce(Text key, Iterator<IntWritable> values, OutputCollector<Text, IntWritable> output, Reporter reporter) throws IOException {
      int sum = 0;
      while (values.hasNext()) {
        sum += values.next().get();
      }
      output.collect(key, new IntWritable(sum));
    }
  }

   private static class IntWritableDecreasingComparator extends
            IntWritable.Comparator {
        public int compare(WritableComparable a, WritableComparable b) {
            return -super.compare(a, b);
        }

        public int compare(byte[] b1, int s1, int l1, byte[] b2, int s2, int l2) {
            return -super.compare(b1, s1, l1, b2, s2, l2);
        }
    }

  public static void main(String[] args) throws Exception {

    String outDirTemp = "./word_count_temp";

    JobConf conf = new JobConf(TweetTrending.class);
    conf.setJobName("TweetTrending");

    conf.setOutputKeyClass(Text.class);
    conf.setOutputValueClass(IntWritable.class);

    conf.setMapperClass(Map.class);
    conf.setCombinerClass(Reduce.class);
    conf.setReducerClass(Reduce.class);

    conf.setInputFormat(TextInputFormat.class);
    conf.setOutputFormat(SequenceFileOutputFormat.class);

    FileInputFormat.setInputPaths(conf, new Path(args[0]));
    FileOutputFormat.setOutputPath(conf, new Path(outDirTemp));

    JobClient.runJob(conf);

    JobConf confSort = new JobConf(TweetTrending.class);
    confSort.setJobName("WordSort");
    confSort.setMapperClass(InverseMapper.class);
    confSort.setNumReduceTasks(1);

    confSort.setOutputKeyClass(IntWritable.class);
    confSort.setOutputValueClass(Text.class);

    confSort.setInputFormat(SequenceFileInputFormat.class);
    confSort.setOutputFormat(TextOutputFormat.class);
    
    confSort.setOutputKeyComparatorClass(IntWritableDecreasingComparator.class);

    FileInputFormat.setInputPaths(confSort, new Path(outDirTemp));
    FileOutputFormat.setOutputPath(confSort, new Path(args[1]));

    JobClient.runJob(confSort);

    try {
            FileReader fr = new FileReader(args[1]+"/part-00000");
            BufferedReader br = new BufferedReader(fr);  
            FileWriter fw = new FileWriter("./topword.txt");
            BufferedWriter bw = new BufferedWriter(fw);
            String myreadline;
            br.readLine();
            int n=100;
            while (br.ready() && n!=0) {
                myreadline = br.readLine();
                bw.write(myreadline);
                bw.newLine();
                n--;
            }
            bw.flush();   
            bw.close();
            fw.close();
            br.close();
            fr.close();
        } catch (IOException e) {
            e.printStackTrace();
    }
  }
}