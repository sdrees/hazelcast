/*
 * Copyright (c) 2008-2016, Hazelcast, Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.hazelcast.jet.stream.impl.processor;

import com.hazelcast.jet.data.io.OutputCollector;
import com.hazelcast.jet.data.io.InputChunk;
import com.hazelcast.jet.data.JetPair;
import com.hazelcast.jet.io.Pair;
import com.hazelcast.jet.processor.Processor;
import com.hazelcast.jet.processor.ProcessorContext;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.stream.Collector;

public class GroupingCombinerProcessor<K, V, A, R> implements Processor<Pair<K, A>, Pair<K, R>> {

    private final Map<K, A> cache = new HashMap<>();
    private final Collector<V, A, R> collector;
    private Iterator<Map.Entry<K, A>> finalizationIterator;

    public GroupingCombinerProcessor(Collector<V, A, R> collector) {
        this.collector = collector;
    }

    @Override
    public boolean process(InputChunk<Pair<K, A>> inputChunk,
                           OutputCollector<Pair<K, R>> output,
                           String sourceName,
                           ProcessorContext context) throws Exception {
        for (Pair<K, A> input : inputChunk) {
            A value = cache.get(input.getKey());
            if (value == null) {
                value = collector.supplier().get();
                cache.put(input.getKey(), value);
            }
            collector.combiner().apply(value, input.getValue());
        }
        return true;
    }

    @Override
    public boolean complete(OutputCollector<Pair<K, R>> output,
                            ProcessorContext processorContext) throws Exception {
        boolean finalized = false;
        try {
            if (finalizationIterator == null) {
                finalizationIterator = cache.entrySet().iterator();
            }

            int idx = 0;
            while (finalizationIterator.hasNext()) {
                Map.Entry<K, A> next = finalizationIterator.next();
                K key = next.getKey();
                R value = collector.finisher().apply(next.getValue());
                output.collect(new JetPair<>(key, value));
                if (idx == processorContext.getConfig().getChunkSize() - 1) {
                    break;
                }
                idx++;
            }
            finalized = !finalizationIterator.hasNext();
        } finally {
            if (finalized) {
                finalizationIterator = null;
                cache.clear();
            }
        }
        return finalized;
    }
}
