package com.elastic.search;

import org.apache.http.HttpHost;
import org.apache.lucene.search.suggest.fst.FSTCompletion;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.text.Text;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.script.Script;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.elasticsearch.search.suggest.Suggest;
import org.elasticsearch.search.suggest.SuggestBuilder;
import org.elasticsearch.search.suggest.SuggestBuilders;
import org.elasticsearch.search.suggest.SuggestionBuilder;
import org.elasticsearch.search.suggest.completion.CompletionSuggestion;
import org.elasticsearch.search.suggest.completion.CompletionSuggestionBuilder;
import org.elasticsearch.search.suggest.phrase.PhraseSuggestion;
import org.elasticsearch.search.suggest.phrase.PhraseSuggestionBuilder;
import org.elasticsearch.search.suggest.term.TermSuggestion;
import org.elasticsearch.search.suggest.term.TermSuggestionBuilder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

//@RunWith(SpringRunner.class)
//@SpringBootTest
public class Tests {


    /**
     * 智能补全测试
     *
     * @throws IOException
     */
    @Test
    public void suggestCompletionTest() throws IOException {
        //创建客户端
        RestHighLevelClient client = new RestHighLevelClient(RestClient.builder(
                new HttpHost("localhost", 9200, "http")));

        //智能补全 只能填写一个字段，这需要先判断是不是中文，在按照需要的分词器进行搜索
        //spy fpy ik
        CompletionSuggestionBuilder completionSuggestionBuilder = SuggestBuilders.completionSuggestion("address.ik").text("广东");
        SuggestBuilder suggestBuilder = new SuggestBuilder();
        suggestBuilder.addSuggestion("address", completionSuggestionBuilder);

        //创建搜索资源
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        searchSourceBuilder.suggest(suggestBuilder);


        //创建搜索请求
        SearchRequest searchRequest = new SearchRequest();
        searchRequest.source(searchSourceBuilder);

        //请求响应
        SearchResponse searchResponse = client.search(searchRequest, RequestOptions.DEFAULT);

        Suggest suggest = searchResponse.getSuggest();
        //获取建议词
        //CompletionSuggestion completionSuggestion = suggest.getSuggestion("address");
        CompletionSuggestion completionSuggestion = suggest.getSuggestion("address");

        //遍历取出建议词
        List<CompletionSuggestion.Entry> entries = completionSuggestion.getEntries();
        for (CompletionSuggestion.Entry en : entries) {
            for (CompletionSuggestion.Entry.Option option : en) {
                System.out.println(option.getText().toString());
            }
        }

        System.out.println(searchResponse);
        //关闭客户端
        client.close();
    }



    /**
     * 智能补全测试
     *
     * @throws IOException
     */
    @Test
    public void suggestTest() throws IOException {
        //创建客户端
        RestHighLevelClient client = new RestHighLevelClient(RestClient.builder(
                new HttpHost("localhost", 9200, "http")));

        //智能补全 只能填写一个字段，这需要先判断是不是中文，在按照需要的分词器进行搜索
        TermSuggestionBuilder termSuggestionBuilder = SuggestBuilders.termSuggestion("address.ik").text("广");
        SuggestBuilder suggestBuilder = new SuggestBuilder();
        suggestBuilder.addSuggestion("address", termSuggestionBuilder);

        //创建搜索资源
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        searchSourceBuilder.suggest(suggestBuilder);


        //创建搜索请求
        SearchRequest searchRequest = new SearchRequest();
        searchRequest.source(searchSourceBuilder);

        //请求响应
        SearchResponse searchResponse = client.search(searchRequest, RequestOptions.DEFAULT);

        Suggest suggest = searchResponse.getSuggest();
        //获取建议词
        TermSuggestion termSuggestion = suggest.getSuggestion("address");

        //遍历取出建议词
        List<TermSuggestion.Entry> entries = termSuggestion.getEntries();
        for (TermSuggestion.Entry en : entries) {
            for (TermSuggestion.Entry.Option option : en) {
                System.out.println(option.getText().toString());
            }
        }

        System.out.println(searchResponse);
        //关闭客户端
        client.close();
    }

}
