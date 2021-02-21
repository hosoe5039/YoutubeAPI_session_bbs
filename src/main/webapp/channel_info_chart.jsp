<%@ page language="java" contentType="text/html; charset=UTF-8"
pageEncoding="UTF-8"%>
<%@ page import = "java.util.*"
import = "youtubeGet.Channel_Info_Bean"%>

<%
	String channel_name =  "'"+ (String)request.getAttribute("channel_name") + "'";
	List <Channel_Info_Bean> list = (List<Channel_Info_Bean>)request.getAttribute("Channel_info_list");
	Channel_Info_Bean channel_info = list.get(0);

	String day = "";
	String subscriber_count ="";
	String period="";


	int max = channel_info.getSubscriber_count();//最大値
	int min = channel_info.getSubscriber_count();//最小値
	int step_data = 0;

	for (int i = 0; i < list.size(); i++){
		Channel_Info_Bean channel = list.get(i);
		day = day + "'"+ channel.getDay() + "'";
		period = channel.getDay();
		int get_subscriber_count = channel.getSubscriber_count();
		subscriber_count = subscriber_count +  String.valueOf(get_subscriber_count) + ",";

		if(max < get_subscriber_count){
			max = get_subscriber_count;
		}else if(min > get_subscriber_count){
			min = get_subscriber_count;
		}
	}

		if(max == min){
			max = max + 10;
			min = min - 10;
			step_data = 10;
		}else if (max != min){
			if(max - min >= 10){
				step_data = max - min;
			}else{
				step_data = (max - min) /10;
			}
		}


		day = day.replace("''","','");
		String period_day = "'" + day.substring(1,11) +"～"+ period + "'";
		subscriber_count = subscriber_count + "#";
		subscriber_count = subscriber_count.replace(",#","");
		String chart_max = String.valueOf(max);
		String chart_min = String.valueOf(min);
		String step_size = String.valueOf(step_data);
		System.out.println(chart_max);
		System.out.println(chart_min);
%>
<!DOCTYPE html>
<html lang="ja">

<head>
  <meta charset="utf-8">
　<title>グラフ</title>
</head>
<body>
  <h1>チャンネル登録者推移</h1>
  <canvas id="myLineChart"></canvas>
    <script src="https://cdnjs.cloudflare.com/ajax/libs/Chart.js/2.7.2/Chart.bundle.js"></script>

  <script>
  var ctx = document.getElementById("myLineChart");
  var myLineChart = new Chart(ctx, {
    type: 'line',
    data: {
      labels: [<%=day%>],
      datasets: [
        {
          label: <%=channel_name%>,
          data: [<%=subscriber_count%>],
          borderColor: "rgba(255,0,0,1)",
          backgroundColor: "rgba(0,0,0,0)"
        },
      ],
    },
    options: {
      title: {
        display: true,
        text: <%=period_day%>,
      },
      scales: {
        yAxes: [{
          ticks: {
            suggestedMax: <%=chart_max%>,
            suggestedMin: <%=chart_min%>,
            stepSize: <%=step_size%>,
            callback: function(value, index, values){
              return  value +  '人'
            }
          }
        }]
      },
    }
  });
  </script>
</body>

</html>