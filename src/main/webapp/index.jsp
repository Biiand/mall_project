<%@ page language="java" contentType="text/html; charset=utf-8"
         pageEncoding="utf-8"%>
<html>
<body>
<h2><strong>Tomcat 01</strong></h2>

<h3>SpringMVC文件上传</h3>
<form name="fileForm1" method="post" action="/manage/product/upload.do" enctype="multipart/form-data">
    <input type="file" name="upload_file"/>
    <input type="submit" value="文件上传">
</form>
<br>
<h3>SpringMVC富文本上传</h3>
<form name="fileForm2" method="post" action="/manage/product/rich_text_img_upload.do" enctype="multipart/form-data">
    <input type="file" name="upload_file"/>
    <input type="submit" value="富文本图片上传">
</form>
</body>
</html>
