import com.example.demo.entity.commonInterface.RedisKeys;
import com.example.demo.utils.redis.RedisUtil;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.util.ObjectUtils;

import java.io.File;
import java.util.*;

/**
 * @author: yd
 * @date: 2022-02-15
 * @version: 1.0
 * @description:
 * @modifiedBy:
 */

@SpringBootTest
public class MyTest {
    @Autowired
    RedisUtil redisUtil;
    @Test
    void test(){
        int length = 2000000;
        long startTime = 0L;
        long endTime = 0L;

        startTime = System.currentTimeMillis();
        for (int i =0; i <length; i++){
            redisUtil.setGet(RedisKeys.personInfoUniqueCodeSet);
        }
        endTime = System.currentTimeMillis();



        startTime = System.currentTimeMillis();
        for (int i =0; i <length; i++){
            redisUtil.getSetSize(RedisKeys.personInfoUniqueCodeSet);
        }
        endTime = System.currentTimeMillis();
        System.out.println("getSetSize 耗时：" + (endTime - startTime));

        startTime = System.currentTimeMillis();
        for (int i =0; i <length; i++){
            redisUtil.setGet(RedisKeys.personInfoUniqueCodePrefix +"10");
        }
        endTime = System.currentTimeMillis();
        System.out.println("setGet 耗时：" + (endTime - startTime));
    }
}
