package gr.ntua.olda.db;

import java.io.Serializable;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.google.common.base.Strings;


import org.apache.storm.state.Serializer;
import org.apache.storm.task.IMetricsContext;
import org.apache.storm.trident.state.State;
import org.apache.storm.trident.state.StateFactory;
import org.apache.storm.trident.state.ValueUpdater;
import org.apache.storm.trident.state.map.IBackingMap;
import org.apache.storm.trident.state.map.MapState;
import org.apache.storm.trident.state.map.NonTransactionalMap;
import org.apache.storm.trident.state.map.SnapshottableMap;
import org.apache.storm.trident.state.snapshot.Snapshottable;
import org.apache.storm.tuple.Values;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.Protocol;

/**
 * Redis State Persistence
 *
 * @param <T>
 */
public class RedisMapState<T> implements Snapshottable<T>, MapState<T> {

    private RedisMapStateBacking _backing;
    private SnapshottableMap<T> _delegate;

    RedisMapState(JedisPool pool, String id, Options opts) {
        _backing = new RedisMapStateBacking(pool, opts);
        _delegate = new SnapshottableMap(NonTransactionalMap.build(_backing), new Values(opts.globalKey));
    }

    @Override
    public T get() {
        return _delegate.get();
    }

    @Override
    public T update(ValueUpdater updater) {
        return _delegate.update(updater);
    }

    @Override
    public void set(T o) {
        _delegate.set(o);
    }

    @Override
    public void beginCommit(Long txid) {
        _delegate.beginCommit(txid);
    }

    @Override
    public void commit(Long txid) {
        _delegate.commit(txid);
    }

    @Override
    public List<T> multiGet(List<List<Object>> keys) {
        return _delegate.multiGet(keys);
    }

    @Override
    public List<T> multiUpdate(List<List<Object>> keys,
                               List<ValueUpdater> updaters) {
        return _delegate.multiUpdate(keys, updaters);
    }

    @Override
    public void multiPut(List<List<Object>> keys, List<T> vals) {
        _delegate.multiPut(keys, vals);
    }

    public static class Factory implements StateFactory {
        String _id;
        Options options;
        InetSocketAddress server;

        public Factory(InetSocketAddress server, String globalKey) {
            this._id = UUID.randomUUID().toString();
            this.options = new Options();
            this.options.globalKey = globalKey;
            this.server = server;
        }

        @Override
        public State makeState(Map conf, IMetricsContext metrics, int partitionIndex, int numPartitions) {
            JedisPool pool = new JedisPool(new JedisPoolConfig(),
                    server.getHostName(), server.getPort(),
                    options.connectionTimeout, options.password,
                    options.database);
            return new RedisMapState(pool, _id + partitionIndex, options);
        }
    }

    static class RedisMapStateBacking<T> implements IBackingMap<T> {

        private final JedisPool pool;
        private Options options;
        RSerializer serializer;

        public RedisMapStateBacking(JedisPool p, Options o) {
            pool = p;
            options = o;
            serializer = new RSerializer();
        }

        @Override
        public List<T> multiGet(List<List<Object>> keys) {
            if (keys.size() == 0) {
                return Collections.emptyList();
            }

            String[] stringKeys = buildKeys(keys);
            List<String> values = mget(stringKeys);
            return deserializeValues(keys, values);
        }

        @Override
        public void multiPut(List<List<Object>> keys, List<T> vals) {
            if (keys.size() == 0) {
                return;
            }
            if (Strings.isNullOrEmpty(this.options.hkey)) {
                String[] keyValues = buildKeyValuesList(keys, vals);
                mset(keyValues);
            }
        }

        @SuppressWarnings("unchecked")
        private List<T> deserializeValues(List<List<Object>> keys,
                                          List<String> values) {
            List<T> result = new ArrayList<T>();
            for (String value : values) {
                if (value != null) {
                    result.add((T) serializer.deserialize(value));
                } else {
                    result.add(null);
                }
            }
            return result;
        }

        @SuppressWarnings("unchecked")
        private String[] buildKeyValuesList(List<List<Object>> keys, List<T> vals) {
            String[] keyValues = new String[keys.size() * 2];
            for (int i = 0; i < keys.size(); i++) {
                keyValues[i * 2] = options.globalKey;
                keyValues[i * 2 + 1] = serializer.serialize(vals.get(i));
            }
            return keyValues;
        }

        private String[] buildKeys(List<List<Object>> keys) {
            String[] stringKeys = new String[keys.size()];
            int index = 0;
            for (List<Object> key : keys)
                stringKeys[index++] = options.globalKey;
            return stringKeys;
        }

        private void mset(String... keyValues) {
            Jedis jedis = pool.getResource();
            try {
                jedis.mset(keyValues);
            } finally {
                pool.returnResource(jedis);
            }
        }

        private List<String> mget(String... keys) {
            Jedis jedis = pool.getResource();
            try {
                return jedis.mget(keys);
            } finally {
                pool.returnResource(jedis);
            }
        }
    }

    private static class Options<T> implements Serializable {
        private static final long serialVersionUID = 1L;
        private String globalKey;
        private int connectionTimeout = Protocol.DEFAULT_TIMEOUT;
        private String password = null;
        private int database = Protocol.DEFAULT_DATABASE;
        private String hkey = null;
    }
}
