<%@ page contentType="text/html;charset=UTF-8"%>
<%@ page
	import = "java.util.List"
	import = "java.net.URLEncoder"
	import = "youtubeGet.Channel_Info_Bean" %>
<%
List <Channel_Info_Bean> list = (List<Channel_Info_Bean>)request.getAttribute("Channels_list");
%>
<!DOCTYPE html">
<html>
<head>
<meta charset="UTF-8">
<title>チャンネル一覧-登録者の推移をグラフ表示します</title>
</head>

<body>

<%for (Channel_Info_Bean channel: list){
	String channel_name_encode = URLEncoder.encode(channel.getChannel_Name(),"UTF-8"); %>

<p><a href="./Chart_display?id=<%=channel.getID()%>&channel=<%=URLEncoder.encode(channel_name_encode, "UTF-8")%>">
<%=channel.getChannel_Name()%>
</a></p>

<%
}
%>

</body>
</html>