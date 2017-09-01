package org.elasticsearch.index.analysis;

/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


import org.apache.lucene.analysis.TokenFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.PayloadAttribute;
import org.apache.lucene.analysis.tokenattributes.TypeAttribute;
import org.apache.lucene.util.BytesRef;

import java.io.IOException;
import java.util.*;

public class TopTokenFilter extends TokenFilter {


    public static final int DEFAULT_MAX_TOKEN_COUNT = 512;

    static final String TOP_TYPE = "TOP_TOKEN";
    private final CharTermAttribute termAttribute = addAttribute(CharTermAttribute.class);
    private final TypeAttribute typeAttribute = addAttribute(TypeAttribute.class);
    private final PayloadAttribute payloadAttribute = addAttribute(PayloadAttribute.class);
    private final Map<String, Integer> topMap;
    private final List<Map.Entry<String, Integer>> topList;

//    protected final Logger logger;
    private int maxTokenCount = DEFAULT_MAX_TOKEN_COUNT;
    private int topPosition = -1;
    private State endState;
    private boolean exhausted = false;
    private boolean requiresInitialisation = true;

    /**
     * create a MinHash filter
     *
     * @param input         the token stream
     * @param maxTokenCount the no. of hashes
     */
    public TopTokenFilter(TokenStream input, int maxTokenCount) {
        super(input);
        if (maxTokenCount <= 0) {
            throw new IllegalArgumentException("maxTokenCount must be greater than zero");
        }
//        this.logger = Loggers.getLogger(getClass());
        this.maxTokenCount = maxTokenCount;
        topMap = new HashMap<>(maxTokenCount);
        topList = new ArrayList<>(maxTokenCount);
    }

    @Override
    public final boolean incrementToken() throws IOException {
        if (requiresInitialisation) {
            requiresInitialisation = false;
            boolean found = false;
            // First time through so we pull and hash everything
            while (input.incrementToken()) {
                found = true;
                String current = new String(termAttribute.buffer(), 0, termAttribute.length());
                topMap.put(current, topMap.getOrDefault(current, 0) + 1);
            }
            input.end();
            // We need the end state so an underlying shingle filter can have its state restored correctly.
            endState = captureState();
            if (!found) {
                return false;
            }
            // Sort and limit
            topList.addAll(topMap.entrySet());
            Collections.sort(topList, (o1, o2) -> {
                Integer compareval = o2.getValue().compareTo(o1.getValue());
                if (compareval.equals(0)) {
                    return o1.getKey().compareTo(o2.getKey());
                }
                return compareval;
            });
            //require token number
            if (maxTokenCount < topList.size()) {
                topList.subList(0, maxTokenCount);
            } else {
                maxTokenCount = topList.size();
            }
        }

        clearAttributes();

        while (topPosition < maxTokenCount) {
            if (topPosition == -1) {
                topPosition++;
            } else {
                Map.Entry<String, Integer> entry = topList.get(topPosition);
                termAttribute.setEmpty();
                termAttribute.append(entry.getKey());
                typeAttribute.setType(TOP_TYPE);
                payloadAttribute.setPayload(new BytesRef(Integer.toString(entry.getValue())));
                topPosition++;
                return true;
            }
        }
        return false;
    }


    @Override
    public void end() throws IOException {
        if (!exhausted) {
            input.end();
        }

        restoreState(endState);
    }

    @Override
    public void reset() throws IOException {
        super.reset();
        doRest();
    }

    private void doRest() {
        topMap.clear();
        topList.clear();
        endState = null;
        topPosition = -1;
        requiresInitialisation = true;
        exhausted = false;
    }

}
