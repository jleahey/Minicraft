package org.newdawn.easyogg;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;

import com.jcraft.jogg.Packet;
import com.jcraft.jogg.Page;
import com.jcraft.jogg.StreamState;
import com.jcraft.jogg.SyncState;
import com.jcraft.jorbis.Block;
import com.jcraft.jorbis.Comment;
import com.jcraft.jorbis.DspState;
import com.jcraft.jorbis.Info;

/**
 * Simple Clip like player for OGG's. Code is mostly taken from the example provided with 
 * JOrbis.
 * 
 * @author kevin
 */
public class OggClip {
	private final int BUFSIZE = 4096 * 2;
	private int convsize = BUFSIZE * 2;
	private byte[] convbuffer = new byte[convsize];
	private int RETRY = 3;
	private int retry = RETRY;
	private String playlistfile = "playlist";
	private boolean icestats = false;
	private SyncState oy;
	private StreamState os;
	private Page og;
	private Packet op;
	private Info vi;
	private Comment vc;
	private DspState vd;
	private Block vb;
	private SourceDataLine outputLine;
	private int frameSizeInBytes;
	private int bufferLengthInBytes;
	private int rate;
	private int channels;
	private BufferedInputStream bitStream=null;
	private byte[] buffer=null;
	private int bytes=0;
	private int format;
	private Thread player=null;
	
	/**
	 * Create a new clip based on a reference into the class path
	 * 
	 * @param ref The reference into the class path which the ogg can be read from
	 * @throws IOException Indicated a failure to find the resource
	 */
	public OggClip(String ref) throws IOException {
		try {
			init(Thread.currentThread().getContextClassLoader().getResourceAsStream(ref));
		} catch (IOException e) {
			throw new IOException("Couldn't find: "+ref);
		}
	}

	/**
	 * Create a new clip based on a reference into the class path
	 * 
	 * @param in The stream from which the ogg can be read from
	 * @throws IOException Indicated a failure to read from the stream
	 */
	public OggClip(InputStream in) throws IOException {
		init(in);
	}
	
	/**
	 * Initialise the ogg clip
	 * 
	 * @param in The stream we're going to read from
	 * @throws IOException Indicates a failure to read from the stream
	 */
	private void init(InputStream in) throws IOException {
		if (in == null) {
			throw new IOException("Couldn't find input source");
		}
		bitStream = new BufferedInputStream(in);
	}
	
	/**
	 * Play the clip once
	 */
	public void play() {
		player = new Thread() {
			public void run() {
				bitStream.mark(Integer.MAX_VALUE);
				try {
					playStream(Thread.currentThread());
				} catch (InternalException e) {
					e.printStackTrace();
				}
				
				try {
					bitStream.reset();
				} catch (IOException e) {
				}
			};
		};
		player.setDaemon(true);
		player.start();
	}

	/**
	 * Loop the clip - maybe for background music
	 */
	public void loop() {
		player = new Thread() {
			public void run() {
				while (player == Thread.currentThread()) {
					bitStream.mark(Integer.MAX_VALUE);
					try {
						playStream(Thread.currentThread());
					} catch (InternalException e) {
						e.printStackTrace();
						player = null;
					} 

					try {
						bitStream.reset();
					} catch (IOException e) {
					}
				}
			};
		};
		player.setDaemon(true);
		player.start();
	}
	
	/**
	 * Stop the clip playing
	 */
	public void stop() {
		player = null;
	}
	
	/**
	 * Close the stream being played from
	 */
	public void close() {
		try {
			if (bitStream != null)
				bitStream.close();
		} catch (IOException e) {
		}
	}
	
	/*
	 * Taken from the JOrbis Player
	 */
	private void initJavaSound(int channels, int rate) throws InternalException {
		try {
			AudioFormat audioFormat = new AudioFormat((float) rate, 16,
					channels, true, // PCM_Signed
					false // littleEndian
			);
			DataLine.Info info = new DataLine.Info(SourceDataLine.class,
					audioFormat, AudioSystem.NOT_SPECIFIED);
			if (!AudioSystem.isLineSupported(info)) {
				throw new Exception("Line " + info + " not supported.");
			}

			try {
				outputLine = (SourceDataLine) AudioSystem.getLine(info);
				// outputLine.addLineListener(this);
				outputLine.open(audioFormat);
			} catch (LineUnavailableException ex) {
				throw new Exception("Unable to open the sourceDataLine: " + ex);
			} catch (IllegalArgumentException ex) {
				throw new Exception("Illegal Argument: " + ex);
			}

			frameSizeInBytes = audioFormat.getFrameSize();
			int bufferLengthInFrames = outputLine.getBufferSize()
					/ frameSizeInBytes / 2;
			bufferLengthInBytes = bufferLengthInFrames * frameSizeInBytes;

			this.rate = rate;
			this.channels = channels;
		} catch (Exception ee) {
			System.out.println(ee);
		}
	}

	/*
	 * Taken from the JOrbis Player
	 */
	private SourceDataLine getOutputLine(int channels, int rate) throws InternalException {
		if (outputLine == null || this.rate != rate
				|| this.channels != channels) {
			if (outputLine != null) {
				outputLine.drain();
				outputLine.stop();
				outputLine.close();
			}
			initJavaSound(channels, rate);
			outputLine.start();
		}
		return outputLine;
	}

	/*
	 * Taken from the JOrbis Player
	 */
	private void initJOrbis(){
	    oy=new SyncState();
	    os=new StreamState();
	    og=new Page();
	    op=new Packet();
	  
	    vi=new Info();
	    vc=new Comment();
	    vd=new DspState();
	    vb=new Block(vd);
	  
	    buffer=null;
	    bytes=0;

	    oy.init();
	}

	/*
	 * Taken from the JOrbis Player
	 */
	private void playStream(Thread me) throws InternalException {
		int last_channels = -1;
		int last_rate = -1;

		boolean chained = false;

		initJOrbis();

		retry = RETRY;

		loop: while (true) {
			int eos = 0;

			int index = oy.buffer(BUFSIZE);
			buffer = oy.data;
			try {
				bytes = bitStream.read(buffer, index, BUFSIZE);
			} catch (Exception e) {
				throw new InternalException(e);
			}
			oy.wrote(bytes);

			if (chained) { 
				chained = false;  
			} else { 
				if (oy.pageout(og) != 1) {
					if (bytes < BUFSIZE)
						break;
					throw new InternalException("Input does not appear to be an Ogg bitstream.");
				}
			} 
			os.init(og.serialno());
			os.reset();

			vi.init();
			vc.init();

			if (os.pagein(og) < 0) {
				// error; stream version mismatch perhaps
				throw new InternalException("Error reading first page of Ogg bitstream data.");
			}

			retry = RETRY;

			if (os.packetout(op) != 1) {
				// no page? must not be vorbis
				throw new InternalException("Error reading initial header packet.");
			}

			if (vi.synthesis_headerin(vc, op) < 0) {
				// error case; not a vorbis header
				throw new InternalException("This Ogg bitstream does not contain Vorbis audio data.");
			}

			int i = 0;

			while (i < 2) {
				while (i < 2) {
					int result = oy.pageout(og);
					if (result == 0)
						break; // Need more data
					if (result == 1) {
						os.pagein(og);
						while (i < 2) {
							result = os.packetout(op);
							if (result == 0)
								break;
							if (result == -1) {
								throw new InternalException("Corrupt secondary header.  Exiting.");
							}
							vi.synthesis_headerin(vc, op);
							i++;
						}
					}
				}

				index = oy.buffer(BUFSIZE);
				buffer = oy.data;
				try {
					bytes = bitStream.read(buffer, index, BUFSIZE);
				} catch (Exception e) {
					throw new InternalException(e);
				}
				if (bytes == 0 && i < 2) {
					throw new InternalException("End of file before finding all Vorbis headers!");
				}
				oy.wrote(bytes);
			}

			convsize = BUFSIZE / vi.channels;

			vd.synthesis_init(vi);
			vb.init(vd);

			double[][][] _pcm = new double[1][][];
			float[][][] _pcmf = new float[1][][];
			int[] _index = new int[vi.channels];

			getOutputLine(vi.channels, vi.rate);

			while (eos == 0) {
				while (eos == 0) {
					if (player != me) {
						return;
					}

					int result = oy.pageout(og);
					if (result == 0)
						break; // need more data
					if (result == -1) { // missing or corrupt data at this page
						// position
						// System.err.println("Corrupt or missing data in
						// bitstream;
						// continuing...");
					} else {
						os.pagein(og);

						if (og.granulepos() == 0) { //
							chained = true; //
							eos = 1; // 
							break; //
						} //

						while (true) {
							result = os.packetout(op);
							if (result == 0)
								break; // need more data
							if (result == -1) { // missing or corrupt data at
								// this page position
								// no reason to complain; already complained
								// above

								// System.err.println("no reason to complain;
								// already complained above");
							} else {
								// we have a packet. Decode it
								int samples;
								if (vb.synthesis(op) == 0) { // test for
									// success!
									vd.synthesis_blockin(vb);
								}
								while ((samples = vd.synthesis_pcmout(_pcmf,
										_index)) > 0) {
									double[][] pcm = _pcm[0];
									float[][] pcmf = _pcmf[0];
									boolean clipflag = false;
									int bout = (samples < convsize ? samples
											: convsize);

									// convert doubles to 16 bit signed ints
									// (host order) and
									// interleave
									for (i = 0; i < vi.channels; i++) {
										int ptr = i * 2;
										// int ptr=i;
										int mono = _index[i];
										for (int j = 0; j < bout; j++) {
											int val = (int) (pcmf[i][mono + j] * 32767.);
											if (val > 32767) {
												val = 32767;
												clipflag = true;
											}
											if (val < -32768) {
												val = -32768;
												clipflag = true;
											}
											if (val < 0)
												val = val | 0x8000;
											convbuffer[ptr] = (byte) (val);
											convbuffer[ptr + 1] = (byte) (val >>> 8);
											ptr += 2 * (vi.channels);
										}
									}
									outputLine.write(convbuffer, 0, 2
											* vi.channels * bout);
									vd.synthesis_read(bout);
								}
							}
						}
						if (og.eos() != 0)
							eos = 1;
					}
				}

				if (eos == 0) {
					index = oy.buffer(BUFSIZE);
					buffer = oy.data;
					try {
						bytes = bitStream.read(buffer, index, BUFSIZE);
					} catch (Exception e) {
						throw new InternalException(e);
					}
					if (bytes == -1) {
						break;
					}
					oy.wrote(bytes);
					if (bytes == 0)
						eos = 1;
				}
			}

			os.clear();
			vb.clear();
			vd.clear();
			vi.clear();
		}

		oy.clear();
	}
	
	private class InternalException extends Exception {
		public InternalException(Exception e) {
			super(e);
		}
		
		public InternalException(String msg) {
			super(msg);
		}
	}
}
