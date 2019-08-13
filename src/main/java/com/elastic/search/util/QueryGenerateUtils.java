package com.elastic.search.util;

import org.elasticsearch.index.query.*;

public class QueryGenerateUtils {

    //中文、拼音混合搜索
    public static QueryBuilder chineseAndPinYinSearch(String filed, String value){

        //使用dis_max直接取多个query中，分数最高的那一个query的分数即可
        DisMaxQueryBuilder disMaxQueryBuilder= QueryBuilders.disMaxQuery();

        /**
         * 纯中文搜索，不做拼音转换,采用edge_ngram分词(优先级最高)
         * 权重* 5
         */
        QueryBuilder normSearchBuilder=QueryBuilders.matchQuery(filed+".ik",value).analyzer("ik_max_word").boost(5f);

        /**
         * 拼音简写搜索
         * 1、分析key，转换为简写  case:  天府三街==>tfsj，天府sj==>tfsj，tfsj==>tfsj
         * 2、搜索匹配，必须完整匹配简写词干
         * 3、如果有中文前缀，则排序优先
         * 权重*1
         */
        String firstChar = PinYinUtils.chineseToSimplePY(value);
        TermQueryBuilder pingYinSampleQueryBuilder = QueryBuilders.termQuery(filed+".spy", firstChar);

        /**
         * 拼音简写包含匹配，如 tfsj可以查出 "城市公牛 天府三街"，虽然非天府三街开头
         * 权重*0.8
         */
        QueryBuilder  pingYinSampleContainQueryBuilder=null;
        if(firstChar.length()>1){
            pingYinSampleContainQueryBuilder=QueryBuilders.wildcardQuery(filed+".spy", "*"+firstChar+"*").boost(0.8f);
        }

        /**
         * 拼音全拼搜索
         * 1、分析key，获取拼音词干   case :  天府三街==>[tian,fu,san,jie]，天府sanjie==>[tian,fu,san,jie]
         * 2、搜索查询，必须匹配所有拼音词，如天府三街，则tian,fu,san,jie四个词干必须完全匹配
         * 3、如果有中文前缀，则排序优先
         * 权重*1
         */
        QueryBuilder pingYinFullQueryBuilder=null;
        if(value.length()>1){
            pingYinFullQueryBuilder=QueryBuilders.matchPhraseQuery(filed+".fpy", value).analyzer("pinyin_full_search");
        }

        /**
         * 完整包含关键字查询(优先级最低，只有以上四种方式查询无结果时才考虑）
         * 权重*0.8
         */
        QueryBuilder containSearchBuilder=QueryBuilders.matchQuery(filed, value).analyzer("ik_max_word").minimumShouldMatch("100%");

        disMaxQueryBuilder
                .add(normSearchBuilder)
                .add(pingYinSampleQueryBuilder)
                .add(containSearchBuilder);

        //以下两个对性能有一定的影响，故作此判定，单个字符不执行此类搜索
        if(pingYinFullQueryBuilder!=null){
            disMaxQueryBuilder.add(pingYinFullQueryBuilder);
        }
        if(pingYinSampleContainQueryBuilder!=null){
            disMaxQueryBuilder.add(pingYinSampleContainQueryBuilder);
        }

        return disMaxQueryBuilder;
    }


    /**
     * 纯中文搜索
     * @return
     */
    public static QueryBuilder chineseSearch(String filed, String value){
        DisMaxQueryBuilder  disMaxQueryBuilder=QueryBuilders.disMaxQuery();
        //以关键字开头(优先级最高)
        MatchQueryBuilder q1=QueryBuilders.matchQuery(filed+".ik",value).analyzer("ik_max_word").boost(5);
        //完整包含经过分析过的关键字
//         boolean  whitespace=key.contains(" ");
//         int slop=whitespace?50:5;
        QueryBuilder q2=QueryBuilders.matchQuery(filed, value).analyzer("ik_max_word").minimumShouldMatch("100%");
        disMaxQueryBuilder.add(q1);
        disMaxQueryBuilder.add(q2);
        return  disMaxQueryBuilder;
    }
}