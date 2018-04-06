package stone.lmj.demo.persistence.dao;

import stone.lmj.demo.persistence.dobj.CityDO;
import org.apache.ibatis.annotations.Param;
import java.util.List;
import stone.lmj.com.dao.TableDAO;
import org.apache.ibatis.annotations.Mapper;
import stone.lmj.com.dao.SQL;


/**
 * Owner: Robot
 * Creator: lingminjun
 * Version: 1.0.0
 * Since: Fri Apr 06 17:55:50 CST 2018
 * Table: city
 */
public interface CityDAO extends TableDAO<CityDO> {
    /**
     * 获取城市信息列表
     *
     * @return
     */
    @SQL("select `id`,`province_id`,`city_name`,`description` from `city` ")
    List<CityDO> findAllCity();

    /**
     * 根据城市名称，查询城市信息
     *
     * @param cityName 城市名
     */
    @SQL("select `id`,`province_id`,`city_name`,`description` from `city` where `city_name` = #{cityName}")
    CityDO findByName(@Param("cityName") String cityName);
}