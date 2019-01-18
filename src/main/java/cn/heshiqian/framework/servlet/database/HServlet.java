package cn.heshiqian.framework.servlet.database;

public final class HServlet {

    public static final String SYS_CONSTANT_KEY="__JSON_String__ONLY_USES_BY_FrameWork";

    public static final int FILER_TYPE_OFF=-1;
    public static final int FILER_TYPE_AUTO=0;
    public static final int FILER_TYPE_CUSTOM=1;

    public static final String VO_FORMAT_ERROR = "此接口返回的VO设置了模板模式，但是你没有设置任何对应的文件名，请使用setTemplateFile方法设置！";
    public static final String FILE_NOT_FIND_ERROR_1 = "文件不存在! 文件名：";
    public static final String FILE_NOT_FIND_ERROR_2 = " 详细请查看后台Log";
    public static final String FILE_NOT_FIND_ERROR_3 = "文件不存在：";
    public static final String FILE_NOT_FIND_ERROR_4 = " 若你使用VO对象返回，请在设置模板文件时，不要添加任何'/'来指定相对路径，系统会自动查找是否有对应名称的文件！";
    public static final String FILE_NOT_FIND_ERROR_5 = "查找路径：";

    public static final String VO_RETURN_ERROR="此方法返回为VO对象，需在方法上加注解MapToFile以支持VO对象返回！";

    public static final String JSON_FORMAT_ERROR="JSON生成异常！<br>";

    public static final String STATIC_LOG_1="静态文件访问：";
    public static final String STATIC_LOG_2="收到的头：";

    public static final String NULL_RETURN_WARING="警告！方法没有返回值。此警告不影响流程执行，只是系统返回！<br>如果希望不再出现此提示，请在返回方法上注解@NullReturn";

    public static final String CLASS_INFO_1="没有此接口的相关类";
    public static final String CLASS_INFO_2="没有初始化的类";
    public static final String CLASS_INFO_3="即将初始化类：";
    public static final String CLASS_LONG_TIME_TO_USE="类因为长时间无使用，已被移出类池！";
    public static final String CLASS_POOL_ERROR="在类池中不存在传入的类！";

    public static final String HANDLE_CENTER_NO_INTERFACE="没有此接口!";
    public static final String HANDLE_EXCEPTION_PART_1="<span style=\"font-size:16px;color:#6c1003;font-weight:bold\">";
    public static final String HANDLE_EXCEPTION_PART_2="</span><br>";
    public static final String HANDLE_EXCEPTION_PART_3="<span style=\"font-size:12px;color:#333\">类：";
    public static final String HANDLE_EXCEPTION_PART_4="中，方法：";
    public static final String HANDLE_EXCEPTION_PART_5="，第：";
    public static final String HANDLE_EXCEPTION_PART_6="行</span><br>";

    public static final String HANDLE_DISPATCHER_INFO_1="方法中参数：";
    public static final String HANDLE_DISPATCHER_INFO_2="没有实际注解，框架没有处理，默认传入null占位！请自行判断！";
    public static final String HANDLE_DISPATCHER_INFO_3="函数：" ;
    public static final String HANDLE_DISPATCHER_INFO_4="()有返回值，但是你注解了@NullReturn，请去除，此注解将忽略所有返回值！";
    public static final String HANDLE_DISPATCHER_INFO_5="@MapToFile与@ResponseBody在返回中，只能选择其中一种！请修改：";
    public static final String HANDLE_DISPATCHER_INFO_5_1="中";
    public static final String HANDLE_DISPATCHER_INFO_5_2="方法！";
    public static final String HANDLE_DISPATCHER_INFO_6="请求url中不含有key为：";
    public static final String HANDLE_DISPATCHER_INFO_6_1="的值！";

    public static final String HANDLE_REQUEST_METHOD_ERROR_GET="请求方式错误！此方法请求方式应为GET";
    public static final String HANDLE_REQUEST_METHOD_ERROR_POST="请求方式错误！此方法请求方式应为POST";

    public static final String SQH_ERROR_1="传入数据流为空！如果没有传入任何'{}'、'[]'的空JSON结构，可能导致框架解析JSON异常！如果需要空执行，请不要将方法使用返回值并且不要带上@ResponesBody注解！";
    public static final String SQH_ERROR_UNCODE="未开发部分，请等待更新！";
    public static final String SQH_ERROR_2="检查到JSON开始与结束没有使用'{','}','[',']'这些括弧包括，请检查你传入的JSON串！";
    public static final String SQH_ERROR_3="读入数据流时发生错误！";

    public static final String SFH_INFO_1="静态文件代理Handle已生成！地址：";
    public static final String SFH_INFO_2="自定配置解析完毕！";
    public static final String SFH_ERROR_1="配置信息为空！请检查'filterList'键值！";
    public static final String SFH_ERROR_2="自定义配置解析失败！使用默认配置：'自动'";

    public static final String SFH_INFO_3="文件解析为'路径匹配'，路径：";
    public static final String SFH_INFO_3_1=" 配置：";
    public static final String SFH_INFO_4="文件解析为'后缀名匹配'，文件名：";
    public static final String SFH_INFO_5="路径(文件)：";
    public static final String SFH_INFO_6="在配置文件中没有解析配置！默认返回404错误！";
    public static final String SFH_INFO_7="访问的文件不存在：";
    public static final String SFH_INFO_7_1="，后缀为：";

    public static final String HS_START_INFO_1="框架开始启动";
    public static final String HS_START_INFO_2="上下文已存储！-->";
    public static final String HS_START_INFO_3="本地地址：";
    public static final String HS_START_INFO_4="Handler已生成！内存地址：";
    public static final String HS_START_INFO_5="Handler已被生成过！这里重新执行了初始化";
    public static final String HS_START_INFO_6="主处理Servlet正在执行销毁";




}
