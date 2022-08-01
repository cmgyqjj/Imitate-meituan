package com.hmdp;

import java.util.ArrayList;

/**
 * @author:{QJJ}
 * @date:{2022}
 * @description:
 **/
public class HeapOOM {
    static class OOMObject {
    }
    public static void main(String[] args) {
        ArrayList<Object> arrayList = new ArrayList<>();
        while(1==1){
            arrayList.add(new OOMObject());
        }
    }
}

