package com.pj;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import cn.dev33.satoken.SaManager;

/**
 * Sa-Token 测试  
 * @author kong
 *
 */
@SpringBootApplication
public class SaTokenApplication {

	public static void main(String[] args) {
		SpringApplication.run(SaTokenApplication.class, args); 
		System.out.println("\n启动成功：Sa-Token配置如下：" + SaManager.getConfig());
	}

}
