package com.ctsi.hook.weixin;

public class DataIndexStruct {

    //文件名长度
    public int nameLen;
    //文件名
    public String name;
    //文件在数据段中偏移值
    public int dataOffset;
    //文件的数据长度
    public int dataLen;

    public int getLen() {
        return 4 + nameLen + 4 + 4;
    }

    @Override
    public String toString() {
        return "nameLen=" + nameLen + "    name=" + name + "    dataOffset=" + dataOffset + "    dataLen=" + dataLen;
    }
}
