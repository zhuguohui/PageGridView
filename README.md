# PageGridView
支持纵向，横向，水平分页的gridview
#效果

![效果](https://github.com/zhuguohui/PageGridView/blob/master/gif/1.gif?raw=true)

#特点

1.支持纵向，横向，水平分页三种布局方式。
2.支持点击事件。
3.支持分割线设置，支持自定义分页指示器。
4.使用简单方便

#使用

###1.在布局文件中定义

![这里写图片描述](http://img.blog.csdn.net/20160821113709429)

**支持的属性如下**

![这里写图片描述](http://img.blog.csdn.net/20160821113832696)

###2.设置Adapter

```java
  //设置adapter
  pageGridView.setAdapter(adapter1);
  //设置点击监听器
  pageGridView.setOnItemClickListener(adapter1);
  //设置分页指示器
   pageGridView2.setPageIndicator(pageIndicator);
```
**注意**
**如果使用分页显示，由于会对数据进行重排序，所以点击事件的position只用和数据集合结合使用。**

**下面是各个借口的定义**

**使用分页功能必须使用此Adapter**

![这里写图片描述](http://img.blog.csdn.net/20160821114631869)

**点击监听器**

![这里写图片描述](http://img.blog.csdn.net/20160821114748526)

**分页指示器**
![这里写图片描述](http://img.blog.csdn.net/20160821114825886)
