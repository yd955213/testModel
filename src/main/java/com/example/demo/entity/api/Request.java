package com.example.demo.entity.api;

import com.google.gson.annotations.SerializedName;
import org.springframework.stereotype.Repository;

@lombok.NoArgsConstructor
@lombok.Data
public class Request<T> {

    @SerializedName("DeviceUniqueCode")
    public String DeviceUniqueCode;
    @SerializedName("TimeStamp")
    public String TimeStamp;
    @SerializedName("Data")
    public T Data;
}