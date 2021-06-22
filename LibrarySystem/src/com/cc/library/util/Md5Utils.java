package com.cc.library.util;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import java.util.Base64;
public class Md5Utils {

	/**
	 * md5加密
	 * @param message 输入的数据
	 * @return 加密后的数据
	 */
	public static String md5(String message){
		try{
			MessageDigest md = MessageDigest.getInstance("md5");
			byte md5[] = md.digest(message.getBytes());
			return  Base64.getEncoder().encodeToString(md5);
		}catch(NoSuchAlgorithmException e){
			throw new RuntimeException(e);
		}
	}
	
}
