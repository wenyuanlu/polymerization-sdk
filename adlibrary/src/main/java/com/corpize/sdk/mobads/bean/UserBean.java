package com.corpize.sdk.mobads.bean;

/**
 * author: yh
 * date: 2019-08-20 16:35
 * description: 用户信息
 */
public class UserBean {

    private String name;            //用户在APP所填写的姓名或昵称
    private String yob;             //用户出生年月日 1990-02-18
    private String gender;          //用户性别M - Male\ F - Female\ O - Other\ U - Unknow
    private String phone;           //用户当前手机号码
    private int    marriage;        //婚姻状况 1、未婚 2、已婚
    private String hobby;           //用户在APP、上的喜好标签，英文逗号分隔,例如：女装,美食,包包
    private String edu;             //用户学历

    public UserBean (String name, String yob, String gender, String phone, int marriage, String hobby, String edu) {
        this.name = name;
        this.yob = yob;
        this.gender = gender;
        this.phone = phone;
        this.marriage = marriage;
        this.hobby = hobby;
        this.edu = edu;
    }

    public String getName () {
        return name;
    }

    public void setName (String name) {
        this.name = name;
    }

    public String getYob () {
        return yob;
    }

    public void setYob (String yob) {
        this.yob = yob;
    }

    public String getGender () {
        return gender;
    }

    public void setGender (String gender) {
        this.gender = gender;
    }

    public String getPhone () {
        return phone;
    }

    public void setPhone (String phone) {
        this.phone = phone;
    }

    public int getMarriage () {
        return marriage;
    }

    public void setMarriage (int marriage) {
        this.marriage = marriage;
    }

    public String getHobby () {
        return hobby;
    }

    public void setHobby (String hobby) {
        this.hobby = hobby;
    }

    public String getEdu () {
        return edu;
    }

    public void setEdu (String edu) {
        this.edu = edu;
    }
}
