package com.example.positionsvr;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Formatter;

public class Person {
    String id;
    String name;
    Short age;
    String addr;
    String telephone;
    String city;
    String area;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Short getAge() {
        return age;
    }

    public void setAge(Short age) {
        this.age = age;
    }

    public void setAddress(String addr) {
        this.addr = addr;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public void setArea(String area) {
        this.area = area;
    }

    public void setTelephone(String telephone) {
        this.telephone = telephone;
    }

    @Override
    public String toString() {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date date = new Date(System.currentTimeMillis());
        String currentTime = simpleDateFormat.format(date);
        return telephone + "#" + city+area + "#" + currentTime;

    }
}
