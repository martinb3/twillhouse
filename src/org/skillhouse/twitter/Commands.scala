package org.skillhouse.twitter;

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

import java.lang.Character
import java.net.URL
import java.util.Properties
import twitter4j.TwitterFactory


import twitter4j.Twitter

import scala.util.matching.Regex;
import org.skillhouse.twitter.TwillhouseCommands.commands;

object TwillhouseCommands {
	// this is a helper object to track all of our commands
	val commands = 
		NullCommand :: 
		HelpCommand :: 
		QuitCommand ::
		TweetCommand ::
		ArriveCommand ::
		DepartCommand :: 
		OauthCommand::
		BadCommand :: 
		Nil
		
		// these commands _WILL_ match in the order listed, so keep BadCommand last
}

abstract class TwillhouseCommand(val strpat:String) {
	// used so the pattern for this command isn't recompiled over and over
	val regex = strpat.r
	val pattern = regex.pattern;
	
	// used to look at the input command and matches later
	var matcher = pattern.matcher("");
	var line = ""
	
	protected def exec() = println("Command is not yet implemented")
	
	def linehelp() = {}; // by default, no line help in listing
	def fullhelp() = println("No detailed help for this command")
	
	def unapply(line:String) = { apply(line); matcher.matches }
	
	def apply(argline:String) = { line = argline.trim.toLowerCase; matcher = pattern.matcher(line) }
	
	def try_exec() = { try { exec() } catch { case t:Throwable => {println("Encountered an error with your command at:");println(t);} } }
}

object NullCommand extends TwillhouseCommand("^$") { override def exec = {} }

object BadCommand extends TwillhouseCommand(".*") {
	override def exec = println("Bad command or syntax error. Try 'help'.\n")
}

object QuitCommand extends TwillhouseCommand("3|quit|exit|q|e") {
	override def exec = { println("Goodbye."); System.exit(0) }
	override def linehelp = println(" [q|quit|e|exit] --\n\t exit the application\n")
}

object HelpCommand extends TwillhouseCommand("h|help") {
	override def linehelp = println(" [h|help] --\n\t display detailed help messages\n")
	override def exec = {
		println("Possible commands:\n")

		// print each command
		for(cmd <- commands) cmd linehelp
		
		println("Explanation of parameters:\n")
		println(" If you specify a valid twitter username, it will be mentioned in the tweet")
		println(" Camera/NoCamera specifies the posting of picture from the webcam")
		println(" Anything generated longer than 140 characters may be truncated or shortened")
		println("\nLine editing is supported. Connectivity is required.\n")
	}
};

object TweetCommand extends TwillhouseCommand("(t|tweet)\\s+(\\S+)\\s+(camera|ca|nocamera|nc)\\s+(.+)") with TwitterUpload {
	override def linehelp = println(" [t|tweet] [username] [[camera|ca]|[nocamera|nc]] [message...]  --\n\t tweet an arbitrary message to a user\n")
	
	override def exec = {
		val user = matcher.group(2); val msg = matcher.group(4)
		val status = (if(validUsername(user)) { "@"+user } else { user }) + " " + msg;
		val camera = matcher.group(3);
		
		sendUpload(status, camera.equals("camera")||camera.equals("ca"));
	}
	
} 
object ArriveCommand extends TwillhouseCommand("(ar|arrive)\\s+(\\S+)\\s+(camera|ca|nocamera|nc)") with TwitterUpload {
	override def linehelp = println(" [ar|arrive] [username] [[camera|ca]|[nocamera|nc]] --\n\t tweet your arrival\n");
	
	override def exec() = {
		val user = matcher.group(2); val msg = "just got here"
		val status = (if(validUsername(user)) { "@"+user } else { user }) + " " + msg;
		val camera = matcher.group(3);
		
		sendUpload(status, camera.equals("camera")||camera.equals("ca"));
	}
}
object DepartCommand extends TwillhouseCommand("(dp|depart)\\s+(\\S+)\\s+(camera|ca|nocamera|nc)") with TwitterUpload {
	override def linehelp = println(" [dp|depart] [username] [[camera|ca]|[nocamera|nc]] --\n\t tweet your departure\n");
	
	override def exec() = {
		val user = matcher.group(2); val msg = "just left"
		val status = (if(validUsername(user)) { "@"+user } else { user }) + " " + msg;
		val camera = matcher.group(3);
		
		sendUpload(status, camera.equals("camera")||camera.equals("ca"));
	}
}

object OauthCommand extends TwillhouseCommand("o|oa|oauth") with TwitterUpload {
	import Twillhouse.{reader,prop,propFile};
	
	override def linehelp = println(" [o|oauth] --\n\t reset Twitter's oauth parameters so this software can tweet as 'at_skillhouse' and post to TwitPic\n");
	
	override def exec() = {
        for(thisProp <- List("oauth.consumerKey", "oauth.consumerSecret", "twitpic.secret") if prop.get(thisProp) != null)
        	reader.readLine("Please define a value for " + prop + ": ")
        
        twitter.setOAuthConsumer(prop.getProperty("oauth.consumerKey"), prop.getProperty("oauth.consumerSecret"))
            
        val requestToken = twitter.getOAuthRequestToken();
        System.out.println("Request token: "+ requestToken.getToken());
        System.out.println("Request token secret: "+ requestToken.getTokenSecret());
        var accessToken : AccessToken = null;

        while (null == accessToken) {
            println("Open the following URL and grant access to your account:");
            println(requestToken.getAuthorizationURL());
            
            
            var pin = reader.readLine("Enter the PIN(if available) and hit enter after you granted access.[PIN]:")
            
            if(pin.length() > 0){
                accessToken = twitter.getOAuthAccessToken(requestToken, pin);
            }else{
                accessToken = twitter.getOAuthAccessToken(requestToken);
            }
            
        }
        println("Access token: " + accessToken.getToken());
        println("Access token secret: " + accessToken.getTokenSecret());

        prop.setProperty("oauth.accessToken", accessToken.getToken());
        prop.setProperty("oauth.accessTokenSecret", accessToken.getTokenSecret());

        val os = new FileOutputStream(propFile);
        prop.store(os, "twitter4j.properties");
        IOUtils.closeQuietly(os);
        
        println("Successfully stored access token to " + propFile.getAbsolutePath()+ ".");
	}
}

