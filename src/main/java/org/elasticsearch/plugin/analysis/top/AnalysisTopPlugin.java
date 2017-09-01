package org.elasticsearch.plugin.analysis.top;

/**
 * Created by cclient on 01/09/2017.
 */


import org.elasticsearch.index.analysis.TokenFilterFactory;
import org.elasticsearch.index.analysis.TopTokenFilterFactory;
import org.elasticsearch.indices.analysis.AnalysisModule;
import org.elasticsearch.plugins.AnalysisPlugin;
import org.elasticsearch.plugins.Plugin;

import java.util.HashMap;
import java.util.Map;

public class AnalysisTopPlugin extends Plugin implements AnalysisPlugin {


    @Override
    public Map<String, AnalysisModule.AnalysisProvider<TokenFilterFactory>> getTokenFilters() {
        Map<String, AnalysisModule.AnalysisProvider<TokenFilterFactory>> extra = new HashMap<>();

        extra.put("limit_by_freq", TopTokenFilterFactory::new);
        return extra;
    }
}