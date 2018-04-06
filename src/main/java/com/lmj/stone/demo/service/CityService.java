package com.lmj.stone.demo.service;



import com.lmj.stone.demo.persistence.dobj.CityDO;

import java.util.List;

/**
 * 城市业务逻辑接口类
 *
 * Created by bysocket on 07/02/2017.
 */
public interface CityService {

    /**
     * 获取城市信息列表
     *
     * @return
     */
    List<CityDO> findAllCity();

    /**
     * 根据城市 ID,查询城市信息
     *
     * @param id
     * @return
     */
    CityDO findCityById(Long id);

    /**
     * 新增城市信息
     *
     * @param CityDO
     * @return
     */
    Long saveCity(CityDO CityDO);

    /**
     * 更新城市信息
     *
     * @param CityDO
     * @return
     */
    Long updateCity(CityDO CityDO);

    /**
     * 根据城市 ID,删除城市信息
     *
     * @param id
     * @return
     */
    Long deleteCity(Long id);


    /**
     * 根据城市名称，查询城市信息
     * @param cityName
     */
    CityDO findCityByName(String cityName);



}
