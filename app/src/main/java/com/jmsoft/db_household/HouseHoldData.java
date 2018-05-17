package com.jmsoft.db_household;

public class HouseHoldData
{
    public int number;
    public String date;
    public String content;
    public String money;
    public String category;

    HouseHoldData(int number,String date,String content,String money,String category)
    {
        this.number = number;
        this.date = date;
        this.content = content;
        this.money = money;
        this.category = category;
    }
}
