package gr.ntua.olda.spouts;

import java.util.Map;
import java.util.concurrent.LinkedBlockingQueue;

import gr.ntua.olda.utils.LocalConfig;
import org.apache.storm.Config;
import org.apache.storm.spout.SpoutOutputCollector;
import org.apache.storm.task.TopologyContext;
import org.apache.storm.topology.OutputFieldsDeclarer;
import org.apache.storm.topology.base.BaseRichSpout;
import org.apache.storm.tuple.Fields;
import org.apache.storm.tuple.Values;
import org.apache.storm.utils.Utils;
import twitter4j.*;
import twitter4j.conf.ConfigurationBuilder;

// Stolen from https://github.com/nathanmarz/storm-starter/blob/master/src/jvm/storm/starter/spout/TwitterSampleSpout.java
public class TwitterSampleSpout extends BaseRichSpout {

    private static final long serialVersionUID = 1L;
    SpoutOutputCollector _collector;
    LinkedBlockingQueue<Status> queue = null;
    TwitterStream _twitterStream;
    String _username;
    String _pwd;
    String _consumerKey = LocalConfig.get("twitter.dev.consumer.key");
    String _consumerSecret = LocalConfig.get("twitter.dev.consumer.secret");
    String _oauthAccessToken = LocalConfig.get("twitter.dev.oauth_access_token");
    String _oauthAccessSecret = LocalConfig.get("twitter.dev.oauth_access_secret");


    public TwitterSampleSpout(String username, String pwd) {
        _username = username;
        _pwd = pwd;
    }

    @Override
    public void open(Map conf, TopologyContext context, SpoutOutputCollector collector) {
        queue = new LinkedBlockingQueue<Status>(1000);
        _collector = collector;
        StatusListener listener = new StatusListener() {

            @Override
            public void onStatus(Status status) {
                System.out.println("TWEET: " + status.getText());
                queue.offer(status);
            }

            @Override
            public void onDeletionNotice(StatusDeletionNotice sdn) {
            }

            @Override
            public void onTrackLimitationNotice(int i) {
            }

            @Override
            public void onScrubGeo(long l, long l1) {
            }

            @Override
            public void onStallWarning(StallWarning stallWarning) {

            }

            @Override
            public void onException(Exception e) {
            }
        };
        TwitterStreamFactory fact = new TwitterStreamFactory(
                new ConfigurationBuilder()
                        .setUser(_username)
                        .setPassword(_pwd)
                        .setOAuthAccessToken(_oauthAccessToken)
                        .setOAuthAccessTokenSecret(_oauthAccessSecret)
                        .setOAuthConsumerKey(_consumerKey)
                        .setOAuthConsumerSecret(_consumerSecret)
                        .setTweetModeExtended(true)
                        .build());

        // Filter
        FilterQuery filter = new FilterQuery();
        filter.language("in");
        filter.track("covid", "curhat", "pemerintah", "humor", "seleb");

        _twitterStream = fact.getInstance();
        _twitterStream.addListener(listener);
        _twitterStream.filter(filter);
    }

    @Override
    public void nextTuple() {
        Status ret = queue.poll();

        if (ret == null) {
            Utils.sleep(50);
        } else {
            String text = ret.getText();
            _collector.emit(new Values(text));
        }
    }

    @Override
    public void close() {
        _twitterStream.shutdown();
    }

    @Override
    public Map<String, Object> getComponentConfiguration() {
        Config ret = new Config();
        ret.setMaxTaskParallelism(1);
        return ret;
    }

    @Override
    public void ack(Object id) {
    }

    @Override
    public void fail(Object id) {
    }

    @Override
    public void declareOutputFields(OutputFieldsDeclarer declarer) {
        declarer.declare(new Fields("sentence"));
    }

}

