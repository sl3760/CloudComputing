package twitterStream;
import java.io.FileWriter;
import  java .io.IOException;  
import twitter4j.*;

public class GetTweets {
	
	public static void main(String[] args) throws TwitterException {
        TwitterStream twitterStream = new TwitterStreamFactory().getInstance();
        
                    
        StatusListener listener = new StatusListener() {
        	
            @Override
            public void onStatus(Status status) {
            	try{
            	   FileWriter fw = new FileWriter("tweets.txt",true);
                   fw.write(status.getText());
                   fw.write("\r\n");
                   fw.close();
            	}catch  (IOException e) {  
                    e.printStackTrace();  
                } 
            }

            @Override
            public void onDeletionNotice(StatusDeletionNotice statusDeletionNotice) {
                //System.out.println("Got a status deletion notice id:" + statusDeletionNotice.getStatusId());
            }

            @Override
            public void onTrackLimitationNotice(int numberOfLimitedStatuses) {
                //System.out.println("Got track limitation notice:" + numberOfLimitedStatuses);
            }

            @Override
            public void onScrubGeo(long userId, long upToStatusId) {
                //System.out.println("Got scrub_geo event userId:" + userId + " upToStatusId:" + upToStatusId);
            }

            @Override
            public void onStallWarning(StallWarning warning) {
                //System.out.println("Got stall warning:" + warning);
            }

            @Override
            public void onException(Exception ex) {
                ex.printStackTrace();
            }
        };
        twitterStream.addListener(listener);
        //twitterStream.sample();   
        FilterQuery query = new FilterQuery();
        String[] keywordsArray = { "sports" };
        query.track(keywordsArray);
        double[][] locations = { { 40.714623d, -74.006605d },
                { 42.3583d, -71.0603d } };
        query.locations(locations);
        String[] lang = {"en"};
        query.language(lang);
        twitterStream.filter(query);
         
    }
}
