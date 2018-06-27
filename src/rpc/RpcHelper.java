package rpc;

import java.io.*;

import javax.servlet.http.HttpServletResponse;

import org.json.*;

public class RpcHelper {
	  // Writes a JSONObject to http response.
		public static void writeJsonObject(HttpServletResponse response, JSONObject obj) throws IOException {
			PrintWriter out = response.getWriter();
			try {
				response.setContentType("application/json");
				response.addHeader("Access-Control-Allow-Origin", "*");
				out.println(obj);

			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} finally {
				out.close();
			}
			
		}

		// Writes a JSONArray to http response.
		public static void writeJsonArray(HttpServletResponse response, JSONArray array) throws IOException {
			PrintWriter out = response.getWriter();
			try {
				response.setContentType("application/json");
				response.addHeader("Access-Control-Allow-Origin", "*");
				out.println(array);
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				out.close();
			}

		}

}
