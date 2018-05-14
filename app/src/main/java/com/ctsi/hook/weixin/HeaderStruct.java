package com.ctsi.hook.weixin;

public class HeaderStruct {

	public byte magic1;
	public int unknow;
	public int offsetLen;
	public int bodyDataLen;
	public byte magic2;
	//注意这个字段按照WX代码中是属于数据段的，但是这里为类操作方便就把它放到头部信息中了
	public int fileCount;

	public int getLen(){
		return 1 + 4 + 4 + 4 + 1 + 4;
	}

	public boolean isValid(){
		return (-66 == magic1) && (-19 == magic2);
	}

	@Override
	public String toString(){
		return "magic1="+magic1+"\tunknow="+unknow+"\toffsetLen="+offsetLen+"\tbodyDataLen="+bodyDataLen+"\tmagic2="+magic2+"\tfileCount="+fileCount;
	}

}
