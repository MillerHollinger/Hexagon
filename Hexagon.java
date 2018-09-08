/*
Welcome to the User Version of Hexagon! 
(C) Hexagon Studios / Miller Hollinger

It looks like you're looking around the code.
That's good! Have a look around. It's pretty complex.
Try not to edit anything, or if you do, make a copy of the file.
If you mess up your game by poking around that's too bad.

I've written some simple comments to explain what each part of the code does.
*/

//All the stuff to import.
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.Font;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;

import java.net.Socket;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.text.DefaultCaret;


public class Hexagon {

   //The objects that read and send data to the server.
   BufferedReader in;
   PrintWriter out;
   
   //The UI that appears on the screen.
   JFrame frame = new JFrame("Hexagon");
   JTextField textField = new JTextField(40);
   JTextArea messageArea = new JTextArea(8, 40);
DefaultCaret caret = (DefaultCaret)messageArea.getCaret();
   public Hexagon() {
     //Configuration of the UI. Sets up the font and everything so it looks all cool.
      Font textFont = new Font("Monospaced", Font.PLAIN, 18);
      textField.setFont(textFont);
      textField.setEditable(false);
      messageArea.setEditable(false);
      messageArea.setFont(textFont);
      frame.getContentPane().add(textField, "South");
      frame.getContentPane().add(new JScrollPane(messageArea), "Center");
      frame.pack();
   
      
      
   
      //An action listener. Does something when text is sent to the user.
      textField.addActionListener(
         new ActionListener() {
            public void actionPerformed(ActionEvent e) {
               out.println(textField.getText());
               textField.setText("");
            }
         });
   }
   
   //The prompt for the user to type in the UI address.
   private String getServerAddress() {
      return JOptionPane.showInputDialog(
         frame,
         "Enter Server IP",
         "Hexagon Server IP",
         JOptionPane.QUESTION_MESSAGE);
   }
   
   //The code that actually does the game.
   private void run() throws IOException {
      //Connects the user to the server.
      String serverAddress = getServerAddress();
      Socket socket = new Socket(serverAddress, 6000);
      in = new BufferedReader(new InputStreamReader(
         socket.getInputStream()));
      out = new PrintWriter(socket.getOutputStream(), true);
      
      //Once connected, let the user run commands in the line.
      textField.setEditable(true);
   
      //This while(true) keeps the game running at all times, until the user closes the game.
      while(true) {
         //Reads the user's command and, if it isn't null, sends it to the server for processing.
         String line = in.readLine();
         caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
         if (line != null)
            messageArea.append(line + "\n");
      }
   }

   public static void main(String[] args) throws Exception {
      //Starts up the game. Creates a new player and runs the above code.
      Hexagon client = new Hexagon();
      client.frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
      client.frame.setVisible(true);
      client.run();
   }
}