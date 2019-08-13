package net.reeuwijk.tools;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Hashtable;

import net.reeuwijk.tools.util.EmailInterface;

public class ImapDedup {

	public static void main(String[] args) throws FileNotFoundException, IOException {
		EmailInterface ei = new EmailInterface(args[0], args[1], "mail.reeuwijk.net");
		Hashtable<String, ArrayList<String>> msgIds = ei.dedupOnMsgID();
	}

}