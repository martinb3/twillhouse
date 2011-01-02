package org.skillhouse.twitter;

import org.apache.commons.io.IOUtils
import twitter4j.Twitter

import twitter4j.TwitterException
import twitter4j.TwitterFactory
import twitter4j.http.AccessToken
import twitter4j.http.RequestToken

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

import Console.{println,flush}
import jline.ConsoleReader

// all possible commands are exported from here
import org.skillhouse.twitter.TwillhouseCommands.commands;

object Twillhouse {

	// some commands may want to take other input, like the oauth one,
	// so they need access to this reader, as a public object here...
	val reader = new ConsoleReader; def Reader = reader
	
	val prop = new Properties();
	val propFile = new File("twitter4j.properties");
	
	def main(args : Array[String]) : Unit = {
		
        val is = new FileInputStream(propFile)
        prop.load(is); IOUtils.closeQuietly(is)

		var line = ""
        println("\nWelcome to Twillhouse -- the Skillhouse Twitter signin utility\n")
        
		while ({line = reader.readLine("Please enter a command: "); line != null}) {
            flush();
            val execute = for (cmd <- commands if cmd.unapply(line)) yield cmd
            if (execute.size > 0) { val mycmd = execute.head; mycmd.try_exec }
            println() // newline before next prompt
        }
	}
}
