## 【H】[框架类]H-Servlet 简单的Web框架

--------------

更多详情查看：
https://blog.csdn.net/hsq062_2/article/details/83538901


H代号作品，简单的Servlet框架，基于Tomcat服务器

### 使用方法：

#### https://blog.csdn.net/hsq062_2/article/details/83024385 第一章
#### https://blog.csdn.net/hsq062_2/article/details/83033914 第二章
#### https://blog.csdn.net/hsq062_2/article/details/83508679 第三章

### 开发日志：

##### 2018年8月10日
修改了对于静态文件的发送机制，根据浏览器请求Accept值来回复，但是这里面还有问题，对于浏览器发送的*/* 的请求没有办法。应该还是要判断请求文件的后缀名来决定回复的类型，要不然就是重新设计一个对于静态文件的处理，让它不会被Servlet接受，就不会出现静态文件进入Servlet了。

##### 2018年9月7日
弃用之前的CFLog，改为最新版，之前版本不可用！！！

##### 2018年10月30日10:40:02
结束一期
