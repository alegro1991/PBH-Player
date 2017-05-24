import java.awt.BorderLayout;

import java.awt.GridLayout;
import java.awt.KeyEventDispatcher;
import java.awt.KeyboardFocusManager;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.util.Map;

import javazoom.jl.decoder.JavaLayerException;
import javazoom.jl.player.advanced.PlaybackListener;
import javazoom.spi.mpeg.sampled.file.MpegAudioFileReader;
import javazoom.jl.player.AudioDevice;
import javazoom.jl.player.FactoryRegistry;
import javazoom.jl.player.advanced.AdvancedPlayer;
import javazoom.jl.player.advanced.PlaybackEvent;

import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.UnsupportedAudioFileException;
import javax.swing.*;
import javax.swing.UIManager.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;

public class PBHPlayer extends JFrame{
	
	static JLabel nameOfFile;
	static InputStream in;
	static AudioDevice ad;
	public PBHPlayer(){
		super("PBH Player");
		try {
		    for (LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
		        if ("Nimbus".equals(info.getName())) {
		            UIManager.setLookAndFeel(info.getClassName());
		            break;
		        }
		    }
		} catch (Exception e) {
			e.printStackTrace();
		}
		setResizable(false);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setSize(400, 100);
		setLocation(500, 200);
		setLayout(new GridLayout(3, 1));
		this.setFocusable(true);
		this.requestFocus();
		Slider jump = new Slider();
		Options options = new Options(32, 20);
		OptionPane op = new OptionPane(options, this);
		JFileChooser jfc = new JFileChooser();
		PanelDisplay pd = new PanelDisplay();
		Buttons buttons = new Buttons(op, jump, jfc);
		SliderPanel sliderPanel = new SliderPanel(buttons.getPlayback(), jump, jfc);
		this.add(pd);
		this.add(buttons);
		this.add(sliderPanel);
		FileFilter fnef = new FileNameExtensionFilter("MP3 files", "mp3");
		jfc.setFileFilter(fnef);
		KeyboardFocusManager.getCurrentKeyboardFocusManager()
			.addKeyEventDispatcher(new KeyEventDispatcher(){
				@Override
				public boolean dispatchKeyEvent(KeyEvent e){
					if(e.getID() == KeyEvent.KEY_PRESSED){
						if(e.getKeyCode() == options.getCode()){
							if(Playing.isRunning == true){
								jump.isHeld = true;
								jump.sliderPlaceHolder = ((((jump.getValue()*26)/1000) - options.getWithdrawal())* 1000)/26;
								pause();
							}
							else{
								if(jump.sliderPlaceHolder < 0){
									jump.sliderPlaceHolder = 0;
								}
								Playing.durationPoint = jump.sliderPlaceHolder;
								Playing.pausePoint = jump.sliderPlaceHolder;
								play(buttons.getPlayback(), jump, jfc);
							}
						}
					}
					return false;
				}
			});
		}
	
	public static void main(String[] args) throws UnsupportedAudioFileException, IOException, JavaLayerException {
		// TODO Auto-generated method stub
		PBHPlayer window = new PBHPlayer();
		window.setVisible(true);
		window.pack();
		Playing.isRunning = false;
	}
	
	private class PanelDisplay extends JPanel{
		PanelDisplay(){
			setLayout(new BorderLayout());
			nameOfFile = new JLabel("empty");
			nameOfFile.setHorizontalAlignment(SwingConstants.CENTER);
			add(nameOfFile);
		}
	}

	
	private class Options{
		
		private int code;
		private int valueOfWithdrawal;
		
		Options(int code, int valueOfWithdrawal){
			this.code = code;
			this.valueOfWithdrawal = valueOfWithdrawal;
		}
		
		void setCode(int code){
			this.code = code;
		}
		
		int getCode(){
			return code;
		}
		
		void setWithdrawal(int valueOfWithdrawal){
			this.valueOfWithdrawal = valueOfWithdrawal;
		}
		
		int getWithdrawal(){
			return valueOfWithdrawal;
		}
	}
	
	
	private static class Slider extends JSlider{
		boolean isHeld;
		int sliderPointer;
		int sliderPlaceHolder;
		Slider(){
			isHeld = false;
		}
	}
	
	private class Buttons extends JPanel implements ActionListener{
		JButton play, pause, stop, choose, time;
		JLabel playback, length;
		OptionPane op;
		Slider jump;
		JFileChooser jfc;
		Buttons(OptionPane op, Slider jump, JFileChooser jfc){
			this.jump = jump;
			this.op = op;
			this.jfc = jfc;
			play = new JButton("Play");
			pause = new JButton("Pause");
			stop = new JButton("Stop");
			choose = new JButton("File");
			time = new JButton("Options");
			playback = new JLabel("00:00:00");
			length = new JLabel("00:00:00");
			JComponent[] elements = {play, pause, stop, choose, time, playback, length};
			for(int i = 0; i < elements.length; i++){
				add(elements[i]);
				elements[i].setFocusable(false);
			}
			play.addActionListener(this);
			stop.addActionListener(this);
			pause.addActionListener(this);
			time.addActionListener(this);
			choose.addActionListener(this);
		}
		
		public JLabel getPlayback(){
			return playback;
		}
		
		public void actionPerformed(ActionEvent e){
			if(e.getSource() == play){
				play(playback, jump, jfc);
			}
			else if(e.getSource() == stop){
				stop(playback, jump, jfc);
			}
			else if(e.getSource() == choose){
				choose(length, jump, jfc);
			}
			else if(e.getSource() == pause){
				pause();
			}
			else if(e.getSource() == time){
				op.setWithdrawal();
			}
		}
	}
	

	
	private class SliderPanel extends JPanel{
		SliderPanel(JLabel playback, Slider jump, JFileChooser jfc){
			setLayout(new BorderLayout());
			SliderHandler sh = new SliderHandler(playback, jump, jfc);
			SliderGrip sg = new SliderGrip(jump);
			jump.addChangeListener(sg);
			jump.addMouseListener(sh);
			add(jump);
			jump.setEnabled(false);
		}
	}
	
	private static class OptionPane{
		JSpinner js;
		JComboBox<String> getList;
		Options options;
		PBHPlayer pbh;
		OptionPane(Options options, PBHPlayer pbh){
			this.pbh = pbh;
			this.options = options;
			SpinnerNumberModel snm = new SpinnerNumberModel(20, 0, Integer.MAX_VALUE, 1);
			js = new JSpinner(snm);
			String[] list = {"Space", "Enter", "Backspace", "Shift", "Control", "Alt", "Escape"};
			getList = new JComboBox<String>(list);
			ListHandler ls = new ListHandler(this, options);
			getList.addActionListener(ls);
		}

		private void setWithdrawal(){
			JLabel timeLabel = new JLabel("Set the jump back and the key");
			JPanel timePanel = new JPanel();
			timePanel.setLayout(new GridLayout(3, 1));
			timePanel.add(timeLabel);
			timePanel.add(js);
			timePanel.add(getList);
			JOptionPane.showMessageDialog(pbh, timePanel);
			try {
				js.commitEdit();
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			options.setWithdrawal((Integer) js.getValue());
		}
	}
	
	private static class Playing{
		static AdvancedPlayer player;
		static int wholeDuration;
		static int durationPoint;
		static Long duration;
		static int pausePoint;
		static boolean isRunning;
	}
	
	
	private static class ListHandler implements ActionListener{
		OptionPane op;
		Options options;
		ListHandler(OptionPane op, Options options){
			this.op = op;
			this.options = options;
		}
		
		@Override
		public void actionPerformed(ActionEvent arg0) {
			// TODO Auto-generated method stub
			String whatKey = (String)op.getList.getSelectedItem();
			switch (whatKey){
			case "Space":
				options.setCode(32);
				break;
			case "Enter":
				options.setCode(10);
				break;
			case "Backspace":
				options.setCode(8);
				break;
			case "Shift":
				options.setCode(16);
				break;
			case "Control":
				options.setCode(17);
				break;
			case "Alt":
				options.setCode(18);
				break;
			case "Escape":
				options.setCode(27);
				break;
			}
		}
		
	}


	
	private static class SliderGrip implements ChangeListener{

		Slider jump;
		SliderGrip(Slider jump){
			this.jump = jump;
		}
		@Override
		public void stateChanged(ChangeEvent arg0) {
			// TODO Auto-generated method stub
			if(jump.getValueIsAdjusting()){
				jump.isHeld = true;
				jump.sliderPlaceHolder = jump.getValue();
			}
		}
	}
	
	private static class SliderHandler implements MouseListener{
		JLabel playback;
		Slider jump;
		JFileChooser jfc;
		SliderHandler(JLabel playback, Slider jump, JFileChooser jfc){
			this.playback = playback;
			this.jump = jump;
			this.jfc = jfc;
		}

		@Override
		public void mouseClicked(MouseEvent arg0) {
			// TODO Auto-generated method stub

		}

		@Override
		public void mouseEntered(MouseEvent arg0) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void mouseExited(MouseEvent arg0) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void mousePressed(MouseEvent arg0) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void mouseReleased(MouseEvent arg0) {
			// TODO Auto-generated method stub
			pause();
			Playing.durationPoint = jump.sliderPlaceHolder;
			Playing.pausePoint = jump.sliderPlaceHolder;
			play(playback, jump, jfc);
		}
	}

	
	private static class PlayersHandler extends PlaybackListener{
		Slider jump;
		JFileChooser jfc;
		PlayersHandler(Slider jump, JFileChooser jfc){
			this.jump = jump;
			this.jfc = jfc;
		}
		public void playbackFinished(PlaybackEvent e){
			Playing.isRunning = false;			
			Playing.durationPoint += e.getFrame()/26;
			select(jump, jfc);
		}
		public void playbackStarted(PlaybackEvent e){
		}
	}
	
	private static class runThread implements Runnable{
		public void run(){
			try {
				Playing.player.play(Playing.durationPoint, Integer.MAX_VALUE);
			} catch (JavaLayerException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			catch (NullPointerException e2){
				e2.printStackTrace();
			}
		}
	}
	
	private static class sliderRunner implements Runnable{
		JLabel playback;
		Slider jump;
		sliderRunner(JLabel playback, Slider jump){
			this.playback = playback;
			this.jump = jump;
		}
		int currentLength = 0;
		public void run(){
			while(Playing.isRunning){
				long millis = System.currentTimeMillis();
				jump.sliderPointer = ((ad.getPosition())/26) + Playing.pausePoint;
				if(jump.isHeld == true){
					break;
				}
				jump.setValue(jump.sliderPointer);
				currentLength = (jump.sliderPointer * 26)/1000;
				System.out.println(ad.getPosition()/26);
				playback.setText(setFormat(currentLength));
				try {
					Thread.sleep(1000 - millis % 1000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}		
			Playing.pausePoint = jump.sliderPointer;
		}
	}
	
	static void stop(JLabel playback, Slider jump, JFileChooser jfc){
		if(Playing.player != null){
			Playing.player.close();
			Playing.durationPoint = 0;
			jump.sliderPointer = 0;
			jump.sliderPlaceHolder = 0;
			Playing.pausePoint = 0;
			jump.setValue(0);
			playback.setText("00:00:00");
			select(jump, jfc);
			Playing.isRunning = false;
		}
	}
	
	static void play(JLabel playback, Slider jump, JFileChooser jfc){
		if(in != null){
			if(Playing.isRunning == false){
				try {
					FactoryRegistry fr = FactoryRegistry.systemRegistry();
					ad = fr.createAudioDevice();
					sliderRunner sr = new sliderRunner(playback, jump);
					Thread sliderThread = new Thread(sr);
					Playing.player = new AdvancedPlayer(in, ad);
				Playing.isRunning = true;
				PlayersHandler ph = new PlayersHandler(jump, jfc);
				Playing.player.setPlayBackListener(ph);
				runThread rt = new runThread();
				Thread playThread = new Thread(rt);
				jump.isHeld = false; 
				sliderThread.start();
				playThread.start();
				} catch (JavaLayerException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}
		}
	}
	
	static void pause(){
		if(in != null){
			if(Playing.isRunning == true){
				Playing.player.stop();
			}
		}
	}
	
	static void choose(JLabel length, Slider jump, JFileChooser jfc){
		int isChoosen = jfc.showOpenDialog(null);
		if(isChoosen == JFileChooser.APPROVE_OPTION){
			select(jump, jfc);
			jump.setMinimum(0);
			jump.setMaximum(Playing.wholeDuration);
			jump.setMajorTickSpacing(3000);
			jump.setPaintTicks(true);
			length.setText(setFormat(Playing.duration.intValue()));
		}
	}
	
	static void select(Slider jump, JFileChooser jfc){
		try {
			File file = jfc.getSelectedFile();
			in = new BufferedInputStream(new FileInputStream(file));
			nameOfFile.setText(file.getName());
			jump.setEnabled(true);	
			AudioFileFormat baseFileFormat = new MpegAudioFileReader().getAudioFileFormat(file);
			Map properties = baseFileFormat.properties();
			Playing.duration = (Long) properties.get("duration"); 
			Playing.duration /= 1000;
			Playing.wholeDuration = (Playing.duration.intValue())/26;
			Playing.duration /=1000;
		} catch (FileNotFoundException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (UnsupportedAudioFileException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		};
	}
	
	private static String setFormat(int totalTime){
		int hours = totalTime / 3600;
		int minutes = (totalTime % 3600) / 60;
		int seconds = totalTime % 60;
		String timeString = String.format("%02d:%02d:%02d", hours, minutes, seconds);
		return timeString;
	}
}