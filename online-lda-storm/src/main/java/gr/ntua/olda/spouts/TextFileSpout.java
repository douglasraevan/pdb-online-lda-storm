package gr.ntua.olda.spouts;


import com.google.common.base.Preconditions;
import org.apache.storm.spout.SpoutOutputCollector;
import org.apache.storm.task.TopologyContext;
import org.apache.storm.topology.OutputFieldsDeclarer;
import org.apache.storm.topology.base.BaseRichSpout;
import org.apache.storm.tuple.Fields;
import org.apache.storm.tuple.Values;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Map;

/**
 * Spout to feed messages into Storm from a file.
 *
 * This spout emits tuples containing only one field, named "line" for each file
 * line.
 *
 */
public class TextFileSpout extends BaseRichSpout {
    /**
     *
     */
    private static final long serialVersionUID = 1L;
    protected final Logger logger = LoggerFactory.getLogger(getClass());
    private SpoutOutputCollector collector;
    private RandomAccessFile reader;
    private String filename;
    private File file;
    private boolean open = false;

    public TextFileSpout(String filename) {
        this(new File(filename));
    }

    public TextFileSpout(File file) {
        Preconditions.checkArgument(file.isFile(),
                "TextFileSpout expects a file but '" + file + "' is not.");
        this.filename = file.getAbsolutePath();
        this.file = file;
    }

    public void open(Map conf, TopologyContext context,
                     SpoutOutputCollector collector) {
        try {
            this.collector = collector;
            this.reader = new RandomAccessFile(file, "r");
            logger.info("Opening TextFileSpout on file " + filename);
            open = true;
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    public void close() {
        try {
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        open = false;
        logger.info("Closing TextFileSpout on file " + filename);
    }

    public void nextTuple() {
        Preconditions.checkState(open && reader != null, "The file " + filename
                + " must be open before reading from it");
        try {
            // case specific as I used these datasets from tweets to test accuracy of LDA algorithm resultd
            // http://snap.stanford.edu/data/bigdata/twitter7/
            reader.readLine();
            reader.readLine();
            reader.readLine();
            String rawLine = reader.readLine();

            if (rawLine.startsWith("W"))
                rawLine = rawLine.replaceFirst("W", "").trim();

            collector.emit(new Values(rawLine));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Emits tuples containing only one field, named "line".
     */
    public void declareOutputFields(OutputFieldsDeclarer declarer) {
        declarer.declare(new Fields("sentence"));
    }

    @Override
    public Map<String, Object> getComponentConfiguration() {
        return null;
    }
}
