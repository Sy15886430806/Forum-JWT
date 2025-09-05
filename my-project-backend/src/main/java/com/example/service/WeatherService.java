package com.example.service;

import com.example.entity.vo.response.WeatherVO;
import org.springframework.stereotype.Service;

@Service
public interface WeatherService {
    WeatherVO fetchWeather(double longitude, double latitude);
}
