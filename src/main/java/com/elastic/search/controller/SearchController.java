package com.elastic.search.controller;

import com.elastic.search.util.PinYinUtils;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.suggest.Suggest;
import org.elasticsearch.search.suggest.SuggestBuilder;
import org.elasticsearch.search.suggest.SuggestBuilders;
import org.elasticsearch.search.suggest.completion.CompletionSuggestion;
import org.elasticsearch.search.suggest.completion.CompletionSuggestionBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Controller
public class SearchController {


    @Autowired
    private RestHighLevelClient client;

    @PostMapping("/suggest")
    @ResponseBody
    public List<String> search(@RequestParam("query") String query) throws IOException {
        System.out.println(query);
        String analyzer = null;
        //判断是否全部是中文
        if (PinYinUtils.isAllChinese(query)) {
            analyzer = "ik";
        } else {
            analyzer = "fpy";
        }
        List<String> strList = new ArrayList<>();
        CompletionSuggestionBuilder completionSuggestionBuilder = SuggestBuilders.completionSuggestion("address." + analyzer).text(query);
        SuggestBuilder suggestBuilder = new SuggestBuilder();
        //索引库名
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
                strList.add(option.getText().toString());
                System.out.println(option.getText().toString());
            }
        }
        System.out.println(searchResponse);
        //关闭客户端
        return strList;
    }

}
