package practica1;

import java.io.*;
import java.sql.Connection;

import org.jsoup.Jsoup;
import org.jsoup.helper.HttpConnection.Response;

import java.awt.LinearGradientPaint;

public class mainPractica1 {
	
	public static void main(String[] args) throws IOException {

		File f = new File("top_sites_themoz.txt" );
		BufferedReader entrada;


		WebDownloader wd = new WebDownloader("downloads2",6,3);
		wd.process("top_sites_themoz.txt");

	}

}
