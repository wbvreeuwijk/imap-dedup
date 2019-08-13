package net.reeuwijk.tools.util;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Properties;
import java.util.StringTokenizer;

import javax.mail.Flags.Flag;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Store;

public class EmailInterface {

	private String user = "";
	private String password = "";
	private String host = "";
	private Session emailSession = null;
	String[] folderNames = null;

	public EmailInterface(String user, String password, String host) throws FileNotFoundException, IOException {
		super();
		this.user = user;
		this.password = password;
		this.host = host;
		Properties props = new Properties();
		props.load(new FileInputStream("imap-dedup.properties"));
		emailSession = Session.getDefaultInstance(props);
		folderNames = ((String) props.get("folders")).split(",");
	}

	public Hashtable<String, ArrayList<String>> dedupOnMsgID() {
		Hashtable<String, ArrayList<String>> msgIds = new Hashtable<String, ArrayList<String>>();
		try {
			Store store = emailSession.getStore();
			store.connect(host, user, password);
			Hashtable<String, Folder> folders = new Hashtable<String,Folder>();
			for (String name : folderNames) {
				Folder emailFolder = store.getFolder(name);
				emailFolder.open(Folder.READ_WRITE);
				folders.put(name,emailFolder);
			}
			for (String folderName : folderNames) {
				Folder emailFolder = folders.get(folderName);
				Message[] messages = emailFolder.getMessages();
				System.out.println("Folder "+folderName+" contains " + messages.length + " message(s)");
				for (int i = 0; i < messages.length; i++) {
					Message msg = messages[i];
					String[] msgIdList = msg.getHeader("Message-ID");
					if (msgIdList != null) {
						for (String id : msgIdList) {
							ArrayList<String> msgs = msgIds.get(id);
							if (msgs == null) {
								msgs = new ArrayList<String>();
							}
							msgs.add(folderName + ":" + msg.getMessageNumber());
							msgIds.put(id, msgs);
							if (msgs.size() > 1) {
								System.out.println(id + ": " + msgs.size());
							}
						}
					}
				}
			}

			Enumeration<String> keys = msgIds.keys();
			while (keys.hasMoreElements()) {
				String key = keys.nextElement();
				ArrayList<String> msgs = msgIds.get(key);
				if (msgs.size() > 1) {
					for (int i = 1; i < msgs.size(); i++) {
						String[] keyItems = msgs.get(i).split(":");
						Folder emailFolder = folders.get(keyItems[0]);
						Message msg = emailFolder.getMessage(Integer.parseInt(keyItems[1]));
						String[] msgIdList = msg.getHeader("Message-ID");
						if (msgIdList[0].equals(key)) {
							msg.setFlag(Flag.DELETED, true);
						} else {
							System.err.println("Wrong Message-ID " + key + " != " + msgIdList[0]);
						}
					}
				}
			}
			for (String folder : folderNames) {
				Folder emailFolder = folders.get(folder);
				emailFolder.close(true);
			}
			store.close();
		} catch (MessagingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return msgIds;
	}
}
