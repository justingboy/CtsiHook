package com.ctsi.hook.weixin;

import java.util.ArrayList;
import java.util.List;

public class WxapkgStruct {
	
	public HeaderStruct header = new HeaderStruct();
	public List<DataIndexStruct> indexStructList = new ArrayList<DataIndexStruct>();
	
	private void parseHeaderStruct(byte[] data){
		try{
			header.magic1 = data[0];
			header.unknow = Utils.byte2int(Utils.reverseBytes(Utils.copyByte(data, 1, 4)));
			header.offsetLen = Utils.byte2int(Utils.reverseBytes(Utils.copyByte(data, 5, 4)));
			header.bodyDataLen = Utils.byte2int(Utils.reverseBytes(Utils.copyByte(data, 9, 4)));
			header.magic2 = data[13];
			header.fileCount = Utils.byte2int(Utils.reverseBytes(Utils.copyByte(data, 14, 4)));
		}catch(Exception e){
			System.out.println("parse header err:"+e.toString());
			Utils.printStatce(e);
			System.exit(-1);
		}
	}
	
	private DataIndexStruct parseIndexStruct(byte[] data, int offset){
		DataIndexStruct indexStruct = new DataIndexStruct();
		try{
			indexStruct.nameLen = Utils.byte2int(Utils.reverseBytes(Utils.copyByte(data, 0+offset, 4)));
			indexStruct.name = new String(Utils.copyByte(data, 4+offset, indexStruct.nameLen), "utf-8");
			indexStruct.dataOffset = Utils.byte2int(Utils.reverseBytes(Utils.copyByte(data, 4+indexStruct.nameLen+offset, 4)));
			indexStruct.dataLen = Utils.byte2int(Utils.reverseBytes(Utils.copyByte(data, 8+indexStruct.nameLen+offset, 4)));
		}catch(Exception e){
			System.out.println("parse data index err:"+e.toString());
			Utils.printStatce(e);
			System.exit(-1);
		}
		return indexStruct;
	}
	
	private void parseIndexStructList(byte[] data){
		try{
			DataIndexStruct indexStruct = parseIndexStruct(data, 0);
			int offset = 0;
			for(int i=1; i<header.fileCount; i++){
				offset += indexStruct.getLen();
				indexStructList.add(indexStruct);
				indexStruct = parseIndexStruct(data, offset);
			}
		}catch(Exception e){
			System.out.println("parse data index list err:"+e.toString());
			Utils.printStatce(e);
			System.exit(-1);
		}
	}
	
	public boolean parse(byte[] data){
		parseHeaderStruct(Utils.copyByte(data, 0, header.getLen()));
		if(header.isValid()){
			parseIndexStructList(Utils.copyByte(data, header.getLen(), data.length-header.getLen()));
			return true;
		}else{
			return false;
		}
	}
	
	@Override
	public String toString(){
		StringBuilder sb = new StringBuilder();
		for(int i=0;i<indexStructList.size();i++){
			sb.append("第"+(i+1)+"File: "+indexStructList.get(i)+"\n");
		}
		return "header info:\n"+header.toString() + "\n\ndataindexlist info:\n"+sb.toString();
	}

}
