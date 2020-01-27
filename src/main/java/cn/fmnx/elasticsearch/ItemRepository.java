package cn.fmnx.elasticsearch;

import cn.fmnx.demo.Item;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

import java.util.List;

/**
 * @description:
 * @author: gmf
 * @date: Created in 2020/1/27 18:14
 * @version:
 * @modified By:
 */
public interface ItemRepository extends ElasticsearchRepository<Item,Long> {
    /**
     * 根据价格区间查询
     * @param price1
     * @param price2
     * @return
     */
    List<Item> findByPriceBetween(double price1,double price2);
    /**
     * @description 根据titile查询
     * @author gmf
     * @date 2020/1/27 18:35
     * @param string
     * @return
     */

    Item findByTitle(String string);
}
