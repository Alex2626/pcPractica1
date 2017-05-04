package practica1;

import java.io.*;
import java.sql.Connection;

import org.jsoup.Jsoup;
import org.jsoup.helper.HttpConnection.Response;

import java.awt.LinearGradientPaint;

public class mainPractica1 {
	/*
	public static void writeHtml(String url, String html, String outPutFolder) throws IOException {

		// modo JISUS
		String[] fileCut = url.split("\\.");
		String nameFile = fileCut[1];

		File folder = new File(outPutFolder);
		folder.mkdir();

		File ofile = new File(outPutFolder+"\\"+nameFile + ".html");
		BufferedWriter bw;

		bw = new BufferedWriter(new FileWriter(ofile));
		bw.write(html);
		bw.close();
	}

	public static void writeError(String url ) throws IOException {
		//ESTO PARA ESCRIBIR LOS ERRORES
		File errorFile = new File("error_log.txt");
		BufferedWriter berror;
		berror = new BufferedWriter(new FileWriter(errorFile));
		berror.write(url);
		berror.close();
	}

*/
	
	public static void main(String[] args) throws IOException {

		File f = new File("top_sites_themozLarga.txt" );
		BufferedReader entrada;

		WebDownloader wd = new WebDownloader("downloads2",6,3);
		wd.process("top_sites_themozLarga.txt");
	}

}
