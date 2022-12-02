package com.kankankeke.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.kankankeke.reggie.common.BaseContext;
import com.kankankeke.reggie.common.Result;
import com.kankankeke.reggie.entity.ShoppingCart;
import com.kankankeke.reggie.service.ShoppingCartService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/shoppingCart")
public class ShoppingCartController {

    @Autowired
    private ShoppingCartService shoppingCartService;

    /**
     * 减少购物车中菜品或者套餐的数量
     * @param shoppingCart
     * @return
     */
    @PostMapping("/sub")
    public Result<ShoppingCart> sub(@RequestBody ShoppingCart shoppingCart){
        log.info("购物车数据:{}",shoppingCart);
        //减少，number大于1就set-1（更新），否则remove
        //设置用户Id，也就是指定当前购物车是哪个用户的
        Long curremtId = BaseContext.getCurrentId();
        shoppingCart.setUserId(curremtId);

        //查询当前菜品或者套餐是否在购物车中
        Long dishId = shoppingCart.getDishId();

        LambdaQueryWrapper<ShoppingCart> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ShoppingCart::getUserId, curremtId);
        //动态拼接一下添加的查询条件
        if (dishId != null) {
            //减少的是菜品
            queryWrapper.eq(ShoppingCart::getDishId, dishId);
        } else {
            //减少的是套餐
            queryWrapper.eq(ShoppingCart::getSetmealId, shoppingCart.getSetmealId());
        }

        /*查询当前菜品或者套餐是否在购物车中
          SQL:select * from shopping_cart where user_Id=? and dish_Id=?/setmealId=?
          如果可以查出来，说明购物车已经加入了相关菜品
         */
        ShoppingCart cartServiceOne = shoppingCartService.getOne(queryWrapper);
        Integer number = cartServiceOne.getNumber();
        if (number > 1) {
            //如果数量大于1，就在原来数量基础上减一
            cartServiceOne.setNumber(number - 1);
            //SQL:update shopping_cart set number =（更新后数量）
            shoppingCartService.updateById(cartServiceOne);
        } else {
            //数量减1后为0，删除购物车中的该条数据
            shoppingCartService.removeById(cartServiceOne);
            //因为这个分支的cartServiceOne是null，所以要覆盖一下
            cartServiceOne = shoppingCart;
        }

        return Result.success(cartServiceOne);
    }
    /**
     * 往购物车内部添加
     * @param shoppingCart
     * @return
     */
    @PostMapping("/add")
    public Result<ShoppingCart> add(@RequestBody ShoppingCart shoppingCart) {
        log.info("购物车数据:{}",shoppingCart);

        //解析一下接受的对象不难发现没有用户ID，所以我们得设置用户Id，也就是指定当前购物车是哪个用户的
        Long curremtId = BaseContext.getCurrentId();
        shoppingCart.setUserId(curremtId);

        //查询当前菜品或者套餐是否在购物车中
        Long dishId = shoppingCart.getDishId();

        LambdaQueryWrapper<ShoppingCart> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ShoppingCart::getUserId, curremtId);

        //动态拼接一下添加的查询条件
        if (dishId != null) {
            //添加到购物车的是菜品
            queryWrapper.eq(ShoppingCart::getDishId, dishId);
        } else {
            //添加到购物车的是套餐
            queryWrapper.eq(ShoppingCart::getSetmealId, shoppingCart.getSetmealId());
        }
        /*查询当前菜品或者套餐是否在购物车中
          SQL:select * from shopping_cart where user_Id=? and dish_Id=?/setmealId=?
          如果可以查出来，说明购物车已经加入了相关菜品
         */
        ShoppingCart cartServiceOne = shoppingCartService.getOne(queryWrapper);

        if (cartServiceOne != null) {
            //如果已经存在，就在原来数量基础上加一
            Integer number = cartServiceOne.getNumber();
            cartServiceOne.setNumber(number + 1);
            //SQL:update shopping_cart set number =（更新后数量）
            shoppingCartService.updateById(cartServiceOne);
        } else {
            ////如果不存在，则添加到购物车，数量默认是1
            shoppingCart.setNumber(1);
            //SQL:insert into shopping_cart (ShoppingCart解析出来的字段) values (ShoppingCart解析出来的数据)
            shoppingCart.setCreateTime(LocalDateTime.now());
            shoppingCartService.save(shoppingCart);
            //因为这个分支的cartServiceOne是null，所以要覆盖一下
            cartServiceOne = shoppingCart;
        }

        return Result.success(cartServiceOne);
    }

    /**
     * 查看购物车
     * @return
     */
    @GetMapping("/list")
    public Result<List<ShoppingCart>> list(){
        log.info("查看购物车...");

        LambdaQueryWrapper<ShoppingCart> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ShoppingCart::getUserId,BaseContext.getCurrentId());
        queryWrapper.orderByAsc(ShoppingCart::getCreateTime);

        List<ShoppingCart> list = shoppingCartService.list(queryWrapper);

        return Result.success(list);
    }

    @DeleteMapping("/clean")
    public Result<String> clean(){
        //SQL:delete from shopping_cart where user_id = ?
        LambdaQueryWrapper<ShoppingCart> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ShoppingCart::getUserId,BaseContext.getCurrentId());

        shoppingCartService.remove(queryWrapper);

        return Result.success("清空购物车成功");
    }



}
