package com.example.demo.entity.api;

import com.example.demo.utils.MyLocalTimeUtil;
import com.google.gson.annotations.SerializedName;

@lombok.NoArgsConstructor
@lombok.Data
public class Request<T> {

    @SerializedName("DeviceUniqueCode")
    public String DeviceUniqueCode;
    @SerializedName("TimeStamp")
    public String TimeStamp = MyLocalTimeUtil.getLocalDataTime();
    @SerializedName("Data")
    public T Data;
}