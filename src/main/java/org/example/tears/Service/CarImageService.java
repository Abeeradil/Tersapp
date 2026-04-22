package org.example.tears.Service;

import org.example.tears.Model.Car;
import org.springframework.stereotype.Service;

@Service
public class CarImageService {

    public String getMainCarImage(Car car) {
        String brand = car.getBrand().getName().toLowerCase();
        String model = car.getModel().getName().toLowerCase();

        return "/carimage/" + brand + "_" + model +".png";
    }

}
