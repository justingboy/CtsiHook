package com.ctsi.hook.weixin;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;

public class Main {
	
	public static void main(String[] args){
		if(args.length <= 0){
			System.out.println("请输入wxapkg文件...");
			return;
		}

		File wxFile = new File(args[0]);
		if(!wxFile.exists()){
			System.out.println(wxFile.getAbsolutePath()+" 文件不存在...");
			return;
		}

		String dir = args[0].substring(0, args[0].indexOf("."));

		byte[] wxpkgByteAry = null;
		FileInputStream fis = null;
		ByteArrayOutputStream bos = null;
		try{
			fis = new FileInputStream(wxFile);
			bos = new ByteArrayOutputStream();
			int len = -1;
			byte[] buffer = new byte[1024];
			while((len=fis.read(buffer)) != -1){
				bos.write(buffer, 0, len);
			}
			wxpkgByteAry = bos.toByteArray();
			bos.flush();
		}catch(Exception e){
		}finally{
			if(fis != null){
				try {
					fis.close();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			if(bos != null){
				try {
					bos.close();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}

		WxapkgStruct struct = new WxapkgStruct();
		boolean isSucc = struct.parse(wxpkgByteAry);
		System.out.println("header info:"+struct.header);
		System.out.println("data info:"+struct.indexStructList);
		if(!isSucc){
			System.out.println("wxapkg文件格式错误...");
			return;
		}

		for(DataIndexStruct indexStruct : struct.indexStructList){
			String path = Utils.isSaveFile(dir+indexStruct.name,
					Utils.copyByte(wxpkgByteAry, indexStruct.dataOffset, indexStruct.dataLen));
			if(path == null){
				System.out.println(indexStruct.name+" 文件解压失败!");
			}else{
				System.out.println(indexStruct.name+" 文件解压成功: "+path);
			}
		}

	}
}
