package stone.lmj.demo.controller;


import stone.lmj.demo.persistence.dobj.CityDO;
import stone.lmj.demo.service.CityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;


import java.util.List;

/**
 * Created by bysocket on 07/02/2017.
 */
@RestController
public class CityRestController {

    @Autowired
    private CityService cityService;

    @RequestMapping(value = "/api/city/{id}", method = RequestMethod.GET)
    public CityDO findOneCity(@PathVariable("id") Long id) {
        return cityService.findCityById(id);
    }

    @RequestMapping(value = "/api/city", method = RequestMethod.GET)
    public List<CityDO> findAllCity() {
        return cityService.findAllCity();
    }

    @RequestMapping(value = "/api/city", method = RequestMethod.POST)
    public void createCity(@RequestBody CityDO CityDO) {
        cityService.saveCity(CityDO);
    }

    @RequestMapping(value = "/api/city", method = RequestMethod.PUT)
    public void modifyCity(@RequestBody CityDO CityDO) {
        cityService.updateCity(CityDO);
    }

    @RequestMapping(value = "/api/city/{id}", method = RequestMethod.DELETE)
    public void modifyCity(@PathVariable("id") Long id) {
        cityService.deleteCity(id);
    }

    @RequestMapping(value = "/api/city/search", method = RequestMethod.GET)
    public CityDO findOneCity(@RequestParam(value = "cityName", required = true) String cityName) {
        return cityService.findCityByName(cityName);
    }

}
