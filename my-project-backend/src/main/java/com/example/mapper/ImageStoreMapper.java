package com.example.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.entity.dto.StoreImage;
import org.apache.ibatis.annotations.Mapper;

import java.awt.*;

@Mapper
public interface ImageStoreMapper extends BaseMapper<StoreImage> {

}
