package com.kankankeke.reggie.dto;

import com.kankankeke.reggie.entity.Setmeal;
import com.kankankeke.reggie.entity.SetmealDish;
import lombok.Data;
import java.util.List;

@Data
public class SetmealDto extends Setmeal {

    private List<SetmealDish> setmealDishes;

    private String categoryName;
}
