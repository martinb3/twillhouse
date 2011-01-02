package org.skillhouse.twitter

import java.net.URL
import twitter4j.Twitter
import org.apache.commons.io.output.CountingOutputStream;
import java.io.BufferedOutputStream;
import org.apache.commons.io.IOUtils;
import twitter4j.Twitter
import twitter4j.TwitterException
import twitter4j.TwitterFactory
import twitter4j.http.AccessToken
import twitter4j.http.RequestToken;

import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.http.AccessToken;
import twitter4j.http.RequestToken;
import twitter4j.media.MediaProvider
import twitter4j.media.ImageUploaderFactory
import twitter4j.conf.ConfigurationBuilder

import java.io.File
import java.io.ByteArrayOutputStream
import java.io.ByteArrayInputStream
import java.io.BufferedInputStream
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.IOException;
import java.io.PrintWriter

trait TwitterUpload {
	import Twillhouse.{reader,prop,propFile};
	val twitter : Twitter= new TwitterFactory().getInstance();
	val conf = new ConfigurationBuilder().setMediaProviderAPIKey(prop.getProperty("twitpic.secret", "unknown")).build();

	def validUsername(u:String) = {
		try { 
			val matches = twitter.lookupUsers(Array[String](u))
			matches.size() == 1
		} catch {
			case _ => false
		}
	}
	
	def sendUpload(message:String, camera:Boolean) = {
		// access token is already available, or consumer key/secret is not set.
		if(!twitter.getAuthorization().isEnabled()){
			throw new IllegalStateException("OAuth consumer key/secret is not set.");
		};
		
		var status = message;
		if(camera) {
			val cameraUrl = prop.getProperty("camera.url","unknown");
			val urlStream = new URL(cameraUrl).openStream;
			val imageInputStream = new BufferedInputStream(urlStream);
			
			print("Downloading image from camera...");
			val imageDataStream = new ByteArrayOutputStream;
			IOUtils.copy(imageInputStream, imageDataStream);
			println("done!");
			IOUtils.closeQuietly(urlStream); IOUtils.closeQuietly(imageInputStream);
			
			val imageByteStream = new BufferedInputStream(new ByteArrayInputStream(imageDataStream.toByteArray));
			var upload = new ImageUploaderFactory(conf).getInstance(MediaProvider.TWITPIC);

			print("Uploading image to TwitPic...");
			var url = upload.upload("a-photo.jpg", imageByteStream, status);
			println("done! Image located at " + url);
			IOUtils.closeQuietly(imageByteStream);
		
			status += (" " + url);
		}
		
		print("Sending status update to Twitter...")
		var response = twitter.updateStatus(status);
		println("done!")
		println("at_skillhouse status is [" + response.getText() + "].");
		
	}
}