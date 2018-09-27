# SerialPortLib <!-- md文件学习 -->
使用串口的.so文件，实现串口的一些基本操作

# 仅供自己学习
---
## 如何使用  
* gradle  
  * 在项目的build.gradle中  
  ```allprojects {
		repositories {
			...
			maven { url 'https://www.jitpack.io' }
		}
	}
  ```  
  * 在module的build.gradle  
  ```
  dependencies {
	        implementation 'com.github.fung-zf:SerialPortLib:V1.0.0'
	}
  ```  
 * maven  
  a. 
  ```
  <repositories>
		<repository>
		    <id>jitpack.io</id>
		    <url>https://www.jitpack.io</url>
		</repository>
	</repositories>
  ```  
   b. 添加依赖  
  ```
   <dependency>
	    <groupId>com.github.fung-zf</groupId>
	    <artifactId>SerialPortLib</artifactId>
	    <version>V1.0.0</version>
	</dependency>
  ```  
  #### 注解  
  
