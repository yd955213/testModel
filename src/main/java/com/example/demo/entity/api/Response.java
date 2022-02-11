package com.example.demo.entity.api;

import com.example.demo.utils.MyLocalTimeUtil;
import com.google.gson.annotations.SerializedName;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author: yd
 * @date: 2022-02-10
 * @version: 1.0
 * @description:
 * @modifiedBy:
 */
@NoArgsConstructor
@Data
public class Response<T> {
    @SerializedName("Code")
    public String Code = "0";
    @SerializedName("Msg")
    public String Msg = "OK";
    @SerializedName("TimeStamp")
    public String TimeStamp = MyLocalTimeUtil.getLocalDataTime() ;
    @SerializedName("Data")
    public T Data;

}
