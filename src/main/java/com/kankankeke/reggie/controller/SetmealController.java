package com.kankankeke.reggie.controller;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.kankankeke.reggie.common.Result;
import com.kankankeke.reggie.dto.SetmealDto;
import com.kankankeke.reggie.entity.Category;
import com.kankankeke.reggie.entity.Setmeal;
import com.kankankeke.reggie.service.CategoryService;
import com.kankankeke.reggie.service.SetmealDishService;
import com.kankankeke.reggie.service.SetmealService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 套餐管理 前端控制器
 */
@RestController
@RequestMapping("/setmeal")
@Slf4j
public class SetmealController {

    @Autowired
    private SetmealService setmealService;

    @Autowired
    private SetmealDishService setmealDishService;

    @Autowired
    private CategoryService categoryService;

    /**
     * 新增套餐
     * @param setmealDto 用套餐Dto对象接收参数
     * @return
     */
    @PostMapping()
    @CacheEvict(value = "setmealCache",allEntries = true)//删除该分类缓存下的所有缓存数据
    public Result<String> save(@RequestBody SetmealDto setmealDto) {
        log.info(setmealDto.toString());

        //因为是两张表关联查询，所以MP直接查是不可以的，自己写一个，把两个信息关联起来存储
        setmealService.saveWithDish(setmealDto);
        return Result.success("新增套餐成功");
    }

    /**
     * 套餐模块的分页查询，因为是多表查询，所以直接MP的分页是不行的
     * 所以这里自己写的Mapper文件，一个SQL+标签动态SQL解决的
     * @param page 查第几页
     * @param pageSize 每页条数
     * @param name 模糊查询
     * @return
     */
    @GetMapping("page")
    public Result<Page> page(int page, int pageSize, String name) {
        //分页构造器对象
        Page<Setmeal> pageInfo = new Page<>(page,pageSize);
        Page<SetmealDto> dtoPage = new Page<>();

        LambdaQueryWrapper<Setmeal> queryWrapper = new LambdaQueryWrapper<>();
        //添加查询条件，根据name进行like模糊查询
        queryWrapper.like(name != null,Setmeal::getName,name);
        queryWrapper.orderByDesc(Setmeal::getUpdateTime);
        setmealService.page(pageInfo,queryWrapper);

        //对象拷贝
        BeanUtils.copyProperties(pageInfo,dtoPage,"records");//将数据从pageInfo拷贝到dtoPage
        List<Setmeal> records = pageInfo.getRecords();

        List<SetmealDto> list = records.stream().map((item) -> {
            SetmealDto setmealDto = new SetmealDto();
            //对象拷贝
            BeanUtils.copyProperties(item,setmealDto);
            //分类id
            Long categoryId = item.getCategoryId();
            //根据分类id查询分类对象
            Category category = categoryService.getById(categoryId);
            if(category != null){
                //分类名称
                String categoryName = category.getName();
                setmealDto.setCategoryName(categoryName);
            }
            return setmealDto;
        }).collect(Collectors.toList());

        dtoPage.setRecords(list);
        return Result.success(dtoPage);
    }


    /**
     * 拿到套餐信息，回填前端页面，为后续套餐更新做准备，调用Service层写
     * @param id ResultFul风格传入参数，接收套餐id对象，用@PathVariable来接收同名参数
     * @return 返回套餐对象
     */
    @GetMapping("/{id}")
    public Result<SetmealDto> getSetmal(@PathVariable("id") Long id){
        log.info("获取套餐Id"+id);
        SetmealDto setmealDto=setmealService.getSetmealData(id);
        return Result.success(setmealDto);
    }

    /**
     * 删除套餐操作
     * 删除的时候，套餐下的关联关系也需要删除掉，要同时处理两张表
     * @param ids 接收多个id，id可以单个也可以多个，批量删或者单个删都可，毕竟走的都是遍历删除
     * @return
     */
    @DeleteMapping
    @CacheEvict(value = "setmealCache",allEntries = true)//删除该分类缓存下的所有缓存数据
    public Result<String> delete(@RequestParam List<Long> ids){
        log.info("ids:{}", ids);
        setmealService.removeWithDish(ids);
        return Result.success("套餐删除成功");
    }


    /**
     * 单一停售和批量停售都是此操作
     * @param ids
     * @return
     */
    @PostMapping("/status/0")
    public Result<String> stopSale(@RequestParam List<Long> ids){

        for (int i = 0; i < ids.size(); i++) {
            Setmeal setmeal = setmealService.getById(ids.get(i));
            setmeal.setStatus(0);
            LambdaQueryWrapper<Setmeal> lambdaQueryWrapper = new LambdaQueryWrapper<>();
            lambdaQueryWrapper.eq(Setmeal::getId, ids.get(i));
            setmealService.update(setmeal, lambdaQueryWrapper);
        }
        return Result.success("套餐停售成功");
    }

    /**
     * 单一启售和批量启动售都是此操作
     * @param ids
     * @return
     */
    @PostMapping("/status/1")
    public Result<String> startSale(@RequestParam List<Long> ids){

        for (int i = 0; i < ids.size(); i++) {
            Setmeal setmeal = setmealService.getById(ids.get(i));
            setmeal.setStatus(1);
            LambdaQueryWrapper<Setmeal> lambdaQueryWrapper = new LambdaQueryWrapper<>();
            lambdaQueryWrapper.eq(Setmeal::getId, ids.get(i));
            setmealService.update(setmeal, lambdaQueryWrapper);
        }
        return Result.success("套餐启售成功");
    }

    /**
     * 修改套餐
     * @param setmealDto
     * @return
     */
    @PutMapping
    public Result<String> update(@RequestBody SetmealDto setmealDto) {
        log.info(setmealDto.toString());
//        setmealDto.updateDishWithFlavor(setmealDto);
        setmealService.updateById(setmealDto);
        return Result.success("修改套餐成功");
    }

    /**
     * 根据条件查询套餐数据
     * 消费者前台页面显示套餐相关的内容
     * https://.../list?categoryId=14122321312status=1
     * 这里不能用RequestBody注解接收参数，是因为传来的参数不是完整的对象并且不是Json，只是对象的一部分
     * 用k-v形式进行传输，所以不能用RequestBody接收
     * @param setmeal
     * @return
     */
    @GetMapping("/list")  // 在消费者端 展示套餐信息
    @Cacheable(value = "setmealCache",key = "#setmeal.categoryId + '_' + #setmeal.status")
    public Result<List<Setmeal>> list(Setmeal setmeal){
        LambdaQueryWrapper<Setmeal> queryWrapper = new LambdaQueryWrapper<>();
        //种类不为空才查
        queryWrapper.eq(setmeal.getCategoryId() != null,Setmeal::getCategoryId,setmeal.getCategoryId());
        //在售状态才查
        queryWrapper.eq(setmeal.getStatus() != null,Setmeal::getStatus,setmeal.getStatus());
        //根据Setmeal中的更新时间来降序排序
        queryWrapper.orderByDesc(Setmeal::getUpdateTime);

        List<Setmeal> list = setmealService.list(queryWrapper);

        return Result.success(list);
    }
}
