package org.newdawn.easyogg;

import java.io.FileInputStream;
import java.io.IOException;

public class TestOgg {

	public static void main(String[] argv) {
		try {
			//OggClip ogg = new OggClip(new FileInputStream("startup2.ogg"));
			OggClip ogg = new OggClip("startup2.ogg");
			System.out.println("Start Loop");
			ogg.loop();
			
			boolean gameLooping = true;
			int num = 0;
			while (gameLooping) {
				num++;
				System.out.println("Game Logic: "+num);
				try { Thread.sleep(1000); } catch (Exception e) {};
				
				if (num == 20) {
					System.out.println("Stop");
					ogg.stop();
				}
				if (num == 30) {
					System.out.println("Reloop");
					ogg.loop();
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
