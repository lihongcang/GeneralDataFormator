# GeneralDataFormator
JAVA实现的模块化、可扩展的数据清洗与格式化框架。

### 项目介绍
项目包括三种角色的积木块：生产者（生产数据）、消费者（处理数据）、工作者（交付数据）。相同角色的积木块，接口是相同的，但具体的运行方式可以是不同的。比如，生产者可以从数据库取到数据，也可以从文本文档取到数据；工作者可以把数据交付给数据库，也可以交付给文本文档；消费者更是如此，处理逻辑不同，消费者也是不同的。项目提供了一些类型的消费者，你可以直接使用，同时项目支持你自定义自己的处理逻辑。

你使用Main类来决定积木块的数量与连接顺序，以达到你的目的。可以是一个从数据库取数据的生产者，接着五个去除数据中脏数据的消费者，接着一个海量文本去重的消费者，接着十个交付数据的工作者。具体如果堆积，取决于你的任务以及机器。

> **重要**：项目中提取网页正文的算法，是基于[Html2Article](https://github.com/stanzhai/Html2Article)，用java重新实现的。

> **重要**：项目中的海量文本去重的算法，是基于SimHash的实现，并做了较多改进，如使用两种hash算法同时约束，结合截断全文并判断长度等，使得在海量数据上误判的概率大大减小。

> **重要**：项目中的分词算法，采用的是优秀的中文分词工具[ansj](https://github.com/NLPchina/ansj_seg)

### 积木块程序细节
Main类：
以生产者为例，通过传入不同的配置文件，来创建不同的生产者
```java
//线程调度
for(int i=0;i<1;i++){
    Producer producer = new Producer(blockingQueueIn,"/config_producer.properties");
    Thread queryThread = new Thread(producer);
    queryThread.setName("producer-"+i+" start");
    queryThread.start();
    System.out.println("producer-"+i+" start");
}
```
Producer类：
通过Main类传入的配置文件路径，来决定调用getFromDB()还是getFromText
```java
public void run() {
    switch (type){
        case "database":
            getFromDB();
            break;
        case "text":
            getFromText();
    }
}
```
Cousumer类：
通过Main类传入的配置文件路径，来决定调用的处理函数和传给处理函数的配置文件路径，如果不传，则按默认参数来执行
```java
case 1:
    System.out.println("test");
    if(if_custom_config){
        CleanData cleandata = new CleanData(function_config_file_path);
        processed_content = cleandata.cleanData(content);
        break;
    }else{
        processed_content = CleanData.cleanData(content);
        break;
    }
```
Worker类：
通过Main类传入的配置文件路径，来决定调用putToDB()还是putToText
```java
case "database":
    putToDB();
    break;
case "text":
    putToText();
```
### 消费者目前提供的处理函数
1. CleanData 数据清洗
2. GetHtmlContent 提取网页正文
3. SplitArticle 文章分段
4. TokenArticle 文章分词
5. RemoveRepeat 海量文本去重（待完善）
6. YourConsumer 自定义
### 配置文件字段含义
1. config_producer.properties
```python
#type=database
# jdbc.driver=com.microsoft.sqlserver.jdbc.SQLServerDriver
# jdbc.url=jdbc:sqlserver://:1433;DatabaseName=
# jdbc.user=
# jdbc.pass=
# jdbc.statement=
# 取哪些字段
#fields_string=

# 数据源类型
type=text
# 数据路径
file_path=C:\\Users\\Leon\\Documents\\GeneralDataFormator\\src\\main\\resources\\test.txt
# n行作为一条数据
n2one=2000
```
2. config_consumer.properties
```python
# 从生产者取哪些字段
fields=content
# 处理哪一个字段
fieldPrimary=content
# 处理后是否写入新字段
override_fieldPrimary=true
# 如果是新字段名是什么
#new_field_name=content_result

# 选择的功能编号
# function_num=1
# 是否向处理函数传入配置文件
# if_custom_config=true
# 配置文件路径
# function_config_file_path=\\config_CleanData.properties
function_num=4
if_custom_config=false
function_config_file_path=\\config_SplitArticle.properties
```
3. config_worker.properties
```python
#type=database
# jdbc.driver=com.microsoft.sqlserver.jdbc.SQLServerDriver
# jdbc.url=jdbc:sqlserver://:1433;DatabaseName=DB_Fulltext_page
# jdbc.user=
# jdbc.pass=
# jdbc.statement=
# fields_string=content:docid
type=text
field=content
split_string=\n
file_path=C:\\Users\\Leon\\Documents\\GeneralDataFormator\\src\\main\\resources\\test_out.txt
```
4. config_CleanData.properties
```python
# 去除空格
remove_whitespace=false
# 去除非中英文数字字符
remove_not_ch_en_num=true
# 去除叠词
repeat2one=6
# 去重复空格
remove_repeat_if_whitespace=false
# 去html标签
remove_html_tag=true
# 长度过滤
length_limit=5
```
5. config_GetHtmlContent.properties
```python
# 追加模式
appendMode=true
# 搜索深度
depth=6
# 长度阈值
limitCount=180
# 判断头部
headEmptyLines=2
# 判断结尾
endLimitCharCount=20
```
6. config_SplitArticle.properties
```python
# n行一条记录
split_num=1
# 重写分隔符
if_override_split_char=false
# 自定义分隔符
#split_chars=;?!~。？！；\n
```
7. config_TokenArticle.properties
```python
# 是否自定义停用词过滤器
if_custom_stop=false
# 停用词性
#stop_natures=u:r
# 停用词文件路径
#stop_dic_file_path=
```
