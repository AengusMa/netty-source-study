package com.mwl;

import io.netty.buffer.PooledByteBufAllocator;
import org.junit.Test;

/**
 * @author mawenlong
 * @date 2019/01/15
 */
public class TestIn {

    @Test
    public void test() throws InterruptedException {
        System.out.println(fun(1024));
    }

    public int fun(int num) {
        int normalizedCapacity = num;
        normalizedCapacity--;
        normalizedCapacity |= normalizedCapacity >>> 1;
        normalizedCapacity |= normalizedCapacity >>> 2;
        normalizedCapacity |= normalizedCapacity >>> 4;
        normalizedCapacity |= normalizedCapacity >>> 8;
        normalizedCapacity |= normalizedCapacity >>> 16;
        normalizedCapacity++;
        if (normalizedCapacity < 0) {
            normalizedCapacity >>>= 1;
        }
        return normalizedCapacity;
    }

    @Test
    public void testAllocate() {
        int page = 1024 * 8;
        PooledByteBufAllocator allocator = PooledByteBufAllocator.DEFAULT;
        allocator.directBuffer(2 * page);
    }
}