package kr.co.killers.ktx;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Properties;

import javax.mail.Message;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class KTX {
	//txtGoHour 시작하는 시간
	//txtGoAbrdDt 시작날짜
	//내려 가는 편
	static String fullUrl2 = "http://www.letskorail.com/ebizprd/EbizPrdTicketPr21111_i1.do?&txtGoAbrdDt=20170127&txtGoYoil=%EA%B8%88&txtGoStartCode=0001&txtGoStart=%EC%84%9C%EC%9A%B8&txtGoEndCode=0059&txtGoEnd=%EB%A7%88%EC%82%B0&selGoTrain=00&selGoRoom=&selGoRoom1=&txtGoHour=000000&txtGoTrnNo=&useSeatFlg=&useServiceFlg=&selGoSeat=&selGoService=&txtGoPage=1&txtPnrNo=&hidRsvChgNo=&hidStlFlg=&radJobId=1&SeandYo=&hidRsvTpCd=03&txtGoHour_first=&selGoSeat1=015&selGoSeat2=&txtPsgCnt1=1&txtPsgCnt2=0&txtMenuId=11&txtPsgFlg_1=1&txtPsgFlg_2=0&txtPsgFlg_3=0&txtPsgFlg_4=0&txtPsgFlg_5=0&chkCpn=N&txtSeatAttCd_4=015&txtSeatAttCd_3=00&txtSeatAttCd_2=000&txtGoStartCode2=&txtGoEndCode2=&hidDiscount=&hidEasyTalk=";
	
	//돌아 오는 편
	static String fullUrl3 = "http://www.letskorail.com/ebizprd/EbizPrdTicketPr21111_i1.do?&txtGoAbrdDt=20170130&txtGoYoil=%ED%99%94&txtGoStartCode=0059&txtGoStart=%EB%A7%88%EC%82%B0&txtGoEndCode=0001&txtGoEnd=%EC%84%9C%EC%9A%B8&selGoTrain=00&selGoRoom=&selGoRoom1=&txtGoHour=000000&txtGoTrnNo=&useSeatFlg=&useServiceFlg=&selGoSeat=&selGoService=&txtGoPage=1&txtPnrNo=&hidRsvChgNo=&hidStlFlg=&radJobId=1&SeandYo=&hidRsvTpCd=03&txtGoHour_first=&selGoSeat1=015&selGoSeat2=&txtPsgCnt1=1&txtPsgCnt2=0&txtMenuId=11&txtPsgFlg_1=1&txtPsgFlg_2=0&txtPsgFlg_3=0&txtPsgFlg_4=0&txtPsgFlg_5=0&chkCpn=N&txtSeatAttCd_4=015&txtSeatAttCd_3=000&txtSeatAttCd_2=000&txtGoStartCode2=&txtGoEndCode2=&hidDiscount=&hidEasyTalk=";

	public static void main(String[] args) throws Exception {
		
		String text = "N";
		String ktxflag1 = "N";
		String ktxflag2 = "N";
		
		while ("N".equals(text)) {
			try {
				
				if("N".equals(ktxflag1)){
					ktxflag1 = ktxshow(fullUrl2);
				}
				if("N".equals(ktxflag2)){
					ktxflag2 = ktxshow(fullUrl3);
				}
				if("Y".equals(ktxflag1)&&"Y".equals(ktxflag2)){
					text ="Y";
				}
				
				// 1분간격
				if("N".equals(text)){
					Thread.sleep(60000);
				}
				System.out.println(System.currentTimeMillis()+" 실행중 ");
			} catch (Exception e) {
				e.printStackTrace();
				System.out.println(System.currentTimeMillis());
			} 
		}

	}

	public static String ktxshow(String url) throws Exception {
		HttpParams httpParameters = new BasicHttpParams();
		HttpConnectionParams.setConnectionTimeout(httpParameters, 10000);
		HttpConnectionParams.setSoTimeout(httpParameters, 150000);

		HttpClient httpClient = new DefaultHttpClient(httpParameters);

		HttpGet postRequest = new HttpGet(url);

		HttpResponse response = httpClient.execute(postRequest);

		String html = readResult(response.getEntity().getContent());

		Document doc = Jsoup.parse(html);
		Elements trs = doc.select("#tableResult>tbody>tr");

		StringBuffer sb = new StringBuffer();
		//td:eq(4) 특실 
		//td:eq(5) 일반석
/*		
		for(int i=0;i<6;i++){
			Element tr = trs.get(i);
			String alt = tr.select("td:eq(5)").select("img").attr("alt");
			if (!"좌석매진".equals(alt)) {
				sb.append(tr.text());
			}
		}
		*/
		// 전체검색
		
		for (Element tr : trs) {
			String alt = tr.select("td:eq(5)").select("img").attr("alt");
			if (!"좌석매진".equals(alt)) {
				sb.append(tr.text());
			}
		}
		
		if(sb.toString().length()>0){
			sb.append(url);
			String mailtext = "ktx 마산행 열차";
			if(fullUrl3.equals(url)){
				 mailtext = "ktx 서울행 열차";
			}
			sendMail(sb.toString(),mailtext);
			return "Y";
		}
		
		return "N";
	}

	public static String readResult(InputStream is) throws Exception {
		StringBuffer httpResponse = new StringBuffer();
		BufferedReader httpBufferedReader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
		String inputLine = null;
		while ((inputLine = httpBufferedReader.readLine()) != null) {
			httpResponse.append(inputLine);
		}
		String response = httpResponse.toString();
		// HTTP 메세지 수신
		return response;
	}

	public static void sendMail(String body,String subject) throws Exception {
		 // 메일 관련 정보
        String host = "smtp.gmail.com";
        //구글 계정
        String username = "####";
        //구글 패스워드
        String password = "####";
         
        //수신 메일 
        String recipient = "####";
         
        //properties 설정
        Properties props = new Properties();
        props.put("mail.smtps.auth", "true");
        // 메일 세션
        Session session = Session.getDefaultInstance(props);
        MimeMessage msg = new MimeMessage(session);
 
        // 메일 관련
        msg.setSubject(subject);
        msg.setText(body);
        msg.setFrom(new InternetAddress(username));
        msg.addRecipient(Message.RecipientType.TO, new InternetAddress(recipient));
 
        // 발송 처리
        Transport transport = session.getTransport("smtps");
        transport.connect(host, username, password);
        transport.sendMessage(msg, msg.getAllRecipients());
        transport.close();     
	}

}
