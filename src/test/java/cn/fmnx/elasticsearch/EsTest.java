package cn.fmnx.elasticsearch;

import cn.fmnx.demo.Item;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.aggregations.Aggregation;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.terms.StringTerms;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.core.aggregation.AggregatedPage;
import org.springframework.data.elasticsearch.core.query.FetchSourceFilter;
import org.springframework.data.elasticsearch.core.query.NativeSearchQuery;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.ArrayList;
import java.util.List;

/**
 * @description:
 * @author: gmf
 * @date: Created in 2020/1/27 18:00
 * @version:
 * @modified By:
 */
@RunWith(SpringRunner.class)
@SpringBootTest
public class EsTest {
    @Autowired
    private ElasticsearchTemplate template;
    @Autowired
    private  ItemRepository repository;

    @Test
    public void testCreatIndex(){
        template.createIndex(Item.class);
    }

    @Test
    public void testPutMapping(){
        template.putMapping(Item.class);
    }

    @Test
    public void testDelete(){
        template.deleteIndex("");
    }
    //新增一个对象
    @Test
    public void index(){
        Item item = new Item(1L, "小米手机7", " 手机","小米", 3499.00, "http://image.leyou.com/13123.jpg");
        repository.save(item);
    }
    //批量新增
    @Test
    public void indexList(){
        List<Item> list = new ArrayList<>();
        list.add(new Item(2L,"坚果手机R1", " 手机", "锤子", 3699.00,"http://image.leyou.com/123.jpg"));
        list.add(new Item(3L, "华为META10", " 手机", "华为", 4499.00, "http://image.leyou.com/3.jpg"));
        list.add(new Item(4L, "三星手机", " 手机", "三星", 4499.00, "http://image.leyou.com/4.jpg"));
        // 接收对象集合，实现批量新增
        repository.saveAll(list);
    }
    //修改
    @Test
    public void testEidt(){
        Item item = new Item(2L, "苹果果手机R1", " 手机", "apple", 9999.00, "http://image.leyou.com/123.jpg");
        repository.save(item);
    }
    //查询所有
    @Test
    public void findAll(){
        // 查询全部，并按照价格降序排序
        Iterable<Item> items = repository.findAll(Sort.by("price").descending());
        items.forEach(System.out::println);
    }
    //自定义方法查询
    @Test
    public void demo1(){
//        List<Item> items = repository.findByPriceBetween(3000.0, 5000.0);
//        items.forEach(System.out::println);
         System.out.println(repository.findByTitle("小米"));
    }
    //自定义查询
    @Test
    public void search(){
        //构建条件
        NativeSearchQueryBuilder queryBuilder = new NativeSearchQueryBuilder();
        //添加基本分词查询
        queryBuilder.withQuery(QueryBuilders.matchQuery("title","小米手机"));
        //搜索获取结果
        Page<Item> i = repository.search(queryBuilder.build());
        //总条数
        long conunt = i.getTotalElements();
        System.out.println("conunt = " + conunt);
        //总页数
        int totalPages = i.getTotalPages();
        System.out.println("totalPages = " + totalPages);
        for (Item item : i) {
            System.out.println("item = " + item);
        }
    }
    //分页查询
    @Test
    public void page(){
        NativeSearchQueryBuilder queryBuilder = new NativeSearchQueryBuilder();
        //分类手机中间空格
        queryBuilder.withQuery(QueryBuilders.termQuery("category"," 手机"));
        //分页
        int page = 0;
        int size = 2;
        queryBuilder.withPageable(PageRequest.of(page,size));
        Page<Item> items = repository.search(queryBuilder.build());
        //总条数
        System.out.println("总条数 = " + items.getTotalElements());
        // 总页数
        System.out.println("总页数 = " + items.getTotalPages());
        // 当前页
        System.out.println("当前页：" + items.getNumber());
        // 每页大小
        System.out.println("每页大小：" + items.getSize());
        items.forEach(System.out::println);
    }
    //排序
    @Test
    public void sort(){
        NativeSearchQueryBuilder queryBuilder = new NativeSearchQueryBuilder();
        queryBuilder.withQuery(QueryBuilders.termQuery("category"," 手机"));
        queryBuilder.withSort(SortBuilders.fieldSort("price").order(SortOrder.ASC));
        Page<Item> items = repository.search(queryBuilder.build());
        queryBuilder.withPageable(PageRequest.of(0,4));
        // 总条数
        long total = items.getTotalElements();
        System.out.println("总条数 = " + total);
        for (Item item : items) {
            System.out.println(item);
        }
    }
    //聚合为桶
    @Test
    public void  testAgg(){
        NativeSearchQueryBuilder queryBuilder = new NativeSearchQueryBuilder();
        //不查询任何结果
        queryBuilder.withSourceFilter(new FetchSourceFilter(new String[]{""},null));
        // 1、添加一个新的聚合，聚合类型为terms，聚合名称为brands，聚合字段为brand
        queryBuilder.addAggregation(
                AggregationBuilders.terms("brands").field("brand"));
                // 2、查询,需要把结果强转为AggregatedPage类型
        AggregatedPage<Item> aggPage = (AggregatedPage<Item>)repository.search(queryBuilder.build());
        // 3、解析
        // 3.1、从结果中取出名为brands的那个聚合，
        // 因为是利用String类型字段来进行的term聚合，所以结果要强转为StringTerm类型
        StringTerms agg = (StringTerms)aggPage.getAggregation("brands");
        //获取桶
        List<StringTerms.Bucket> buckets = agg.getBuckets();
        for (StringTerms.Bucket bucket : buckets) {
            // 3.4、获取桶中的key，即品牌名称
            String keyAsString = bucket.getKeyAsString();
            System.out.println("keyAsString = " + keyAsString);
            // 3.5、获取桶中的文档数量
            System.out.println(bucket.getDocCount());
        }
    }
}
