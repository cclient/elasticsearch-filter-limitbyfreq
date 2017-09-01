package org.elasticsearch.index.analysis;


import org.apache.lucene.analysis.TokenStream;
import org.elasticsearch.common.inject.Inject;
import org.elasticsearch.common.inject.assistedinject.Assisted;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.env.Environment;
import org.elasticsearch.index.IndexSettings;


public class TopTokenFilterFactory extends AbstractTokenFilterFactory {
    private Integer maxTokenCount;

    @Inject
    public TopTokenFilterFactory(IndexSettings indexSettings, Environment environment, @Assisted String name, @Assisted Settings settings) {
        super(indexSettings, name, settings);
        maxTokenCount = settings.getAsInt("max_token_count", 512);
    }


    @Override
    public TokenStream create(TokenStream tokenStream) {
        return new TopTokenFilter(tokenStream, maxTokenCount);
    }
}
