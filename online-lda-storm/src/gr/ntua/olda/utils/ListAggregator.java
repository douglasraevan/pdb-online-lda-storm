package gr.ntua.olda.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.storm.trident.operation.Aggregator;
import org.apache.storm.trident.operation.TridentCollector;
import org.apache.storm.trident.operation.TridentOperationContext;
import org.apache.storm.trident.tuple.TridentTuple;
import org.apache.storm.tuple.Values;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Just aggregates lists to be processed from LDA
 */
public class ListAggregator implements Aggregator<List<String>> {

    private static final long serialVersionUID = 1L;

    private final static Logger logger = LoggerFactory.getLogger(ListAggregator.class);
    private static int cnt;

    @Override
    public void prepare(Map conf, TridentOperationContext context) {

    }

    @Override
    public void cleanup() {
    }

    @Override
    public List<String> init(Object batchId, TridentCollector collector) {
        return new ArrayList<>();
    }

    @Override
    public void aggregate(List<String> val, TridentTuple tuple,
                          TridentCollector collector) {
        String value = (String) tuple.getValue(0);
        val.add(value);
    }

    @Override
    public void complete(List<String> val, TridentCollector collector) {
        if (val.size() != 0) {
            collector.emit(new Values(val, "1"));
            logger.info("List no: " + String.valueOf(cnt++));
        }
    }
}
