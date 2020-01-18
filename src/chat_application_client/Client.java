/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package chat_application_client;

import java.io.*;
import java.net.*;
import java.awt.*;
import java.awt.event.*;
import java.security.KeyPair;
import java.security.PublicKey;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.*;
/**
 *
 * @author Suneth
 */
public class Client extends JFrame{
    
    private JTextField userText;
    private JTextArea chatWindow;
    private ObjectOutputStream output;
    private ObjectOutputStream keyOutput;
    private ObjectOutputStream signOutput;
    private ObjectInputStream input;
    private ObjectInputStream keyInput;
    private ObjectInputStream signInput;
    private String message = "";
    private String serverIP;
    private Socket connection;
    RSAencryption rsa = new RSAencryption();
		
    //constructor
    public Client(String host){
        super("WhatsChat Instant Messenger");
        try{
            serverIP = host;
            userText = new JTextField();
            userText.setEditable(false);
            userText.addActionListener(
            new ActionListener(){
                public void actionPerformed(ActionEvent event){
                    try{
                            sendMessage(event.getActionCommand());
                            userText.setText("");
                       }catch(Exception ex){
                            JOptionPane.showMessageDialog(null,"Error occured","Error",JOptionPane.ERROR_MESSAGE);
                            System.out.print("Error occured: " + ex.getMessage());
                       }
                }
            });
            add(userText, BorderLayout.NORTH);
            chatWindow = new JTextArea();
            add(new JScrollPane(chatWindow), BorderLayout.CENTER);
            setSize(300,150);
            setVisible(true);
        }catch(Exception ex){
            JOptionPane.showMessageDialog(null,"Error occured","Error",JOptionPane.ERROR_MESSAGE);
            System.out.print("Error occured: " + ex.getMessage());
        }
    }

    //connect to server
    public void startRunning(){
        try{
            try{
                    connectToServer();
                    setupStreams();
                    setupKeyStreams();
                    setupSignStreams();
                    whileChatting();
               }catch(Exception ex){
                    showMessage("\n Client terminated the connection ");
               }finally{
                    closeCrap();
               }
        }catch(Exception ex){
            JOptionPane.showMessageDialog(null,"Error occured","Error",JOptionPane.ERROR_MESSAGE);
            System.out.print("Error occured: " + ex.getMessage());
        }
    }
    
    //connect to server
    private void connectToServer(){
        try{
            showMessage("Attempting connection... \n");
            connection = new Socket(InetAddress.getByName(serverIP), 6789);
            showMessage("Connected to: " + connection.getInetAddress().getHostName());
        }catch(Exception ex){
            JOptionPane.showMessageDialog(null,"Error occured","Error",JOptionPane.ERROR_MESSAGE);
            System.out.print("Error occured: " + ex.getMessage());
        }
    }
    
    //set up stream to send and receive messages
    private void setupStreams(){
        try{
            output = new ObjectOutputStream(connection.getOutputStream());
            output.flush();
            input = new ObjectInputStream(connection.getInputStream());
            showMessage("\n The streams are now setup! \n");
        }catch(Exception ex){
            JOptionPane.showMessageDialog(null,"Error occured","Error",JOptionPane.ERROR_MESSAGE);
            System.out.print("Error occured: " + ex.getMessage());
        }
    }
    
    //set up stream to send and receive keys
    private void setupKeyStreams(){
        try{
            keyOutput = new ObjectOutputStream(connection.getOutputStream());
            keyOutput.flush();
            keyInput = new ObjectInputStream(connection.getInputStream());
        }catch(Exception ex){
            JOptionPane.showMessageDialog(null,"Error occured","Error",JOptionPane.ERROR_MESSAGE);
            System.out.print("Error occured: " + ex.getMessage());
        }
    }
    
    //receive the public key
    private PublicKey receiveKey() throws Exception{
        PublicKey publicKey = (PublicKey)keyInput.readObject();
        return publicKey;
    }
    
    //set up stream to send and receive signatures
    private void setupSignStreams(){
        try{
            signOutput = new ObjectOutputStream(connection.getOutputStream());
            signOutput.flush();
            signInput = new ObjectInputStream(connection.getInputStream());
        }catch(Exception ex){
            JOptionPane.showMessageDialog(null,"Error occured","Error",JOptionPane.ERROR_MESSAGE);
            System.out.print("Error occured: " + ex.getMessage());
        }
    }
    
    //receive the signature
    private String receiveSign() throws Exception{
        String signature = (String)signInput.readObject();
        return signature;
    }
    
    //while chatting with server
    private void whileChatting(){
        try{
            ableToType(true);
            do{
                try{
                        String cipherText = (String)input.readObject();
                        System.out.println(cipherText);
                        PublicKey publicKey = receiveKey();
                        // Now decrypt it
                        String decipheredMessage = rsa.decrypt(cipherText, publicKey);
                        System.out.println(decipheredMessage);
                        showMessage("\n" + decipheredMessage);
                        String signature = receiveSign(); 
                        // Let's check the signature
                        boolean isCorrect = rsa.verify("foobar", signature, publicKey);
                        System.out.println("Signature correct: " + isCorrect); 
                   }catch(ClassNotFoundException ex){
                        JOptionPane.showMessageDialog(null,"Unable to send the message","Error",JOptionPane.ERROR_MESSAGE);
                        System.out.print("Error occured: " + ex.getMessage());
                   }
                }while(!message.equals("SERVER - END"));
        }catch(Exception ex){
            JOptionPane.showMessageDialog(null,"Unable to send the message","Error",JOptionPane.ERROR_MESSAGE);
            System.out.print("Error occured: " + ex.getMessage());
        }
    }
    
    //close the streams and sockets
    private void closeCrap(){
        try{
            showMessage("\n Closing background tasks down... ");
            ableToType(false);
            try{
                    output.close();
                    keyOutput.close();
                    signOutput.close();
                    input.close();
                    keyInput.close();
                    signInput.close();
                    connection.close();
               }catch(IOException ex){
                    JOptionPane.showMessageDialog(null,"Error occured","Error",JOptionPane.ERROR_MESSAGE);
                    System.out.print("Error occured: " + ex.getMessage());
               }
        }catch(Exception ex){
            JOptionPane.showMessageDialog(null,"Error occured","Error",JOptionPane.ERROR_MESSAGE);
            System.out.print("Error occured: " + ex.getMessage());
        }
    }
    
    //input validation
    public boolean inputValidation(String m) throws Exception{
        String pattern= "^[a-zA-Z0-9\\t\\n ,./<>?;:\"'`!@#$%^&*()\\[\\]{}_+=|\\\\-]+$";
        return m.matches(pattern);
    }
    
    //send messages to server
    private void sendMessage(String message){
        try{
            if(inputValidation(message) == true)
            {
                // First generate a public/private key pair
                KeyPair pair = rsa.generateKeyPair();
                // KeyPair pair = getKeyPairFromKeyStore();
                // Encrypt the message
                System.out.println("CLIENT - " + message);
                String cipherText = rsa.encrypt("CLIENT - " + message, pair.getPrivate());
                System.out.println(cipherText);            
                output.writeObject(cipherText);
                output.flush();
                sendKey(pair.getPublic());
                showMessage("\nCLIENT - " + message);
                // Let's sign our message
                String signature = rsa.sign("foobar", pair.getPrivate());
                sendSign(signature);
            }
            else
            {
                JOptionPane.showMessageDialog(null,"Unable to send the message","Error",JOptionPane.ERROR_MESSAGE);
            }
        }catch(Exception ex){
            JOptionPane.showMessageDialog(null,"Unable to send the message","Error",JOptionPane.ERROR_MESSAGE);
            System.out.print("Error occured: " + ex.getMessage());
        }
    }
    
    //send the public key
    private void sendKey(PublicKey publicKey){
        try{    
            keyOutput.writeObject(publicKey);
            keyOutput.flush();
        }catch(IOException ex){
            JOptionPane.showMessageDialog(null,"Unable to send the message","Error",JOptionPane.ERROR_MESSAGE);
            System.out.print("Error occured: " + ex.getMessage());
        }
    }
            
    //send the signature
    private void sendSign(String signature){
        try{    
            signOutput.writeObject(signature);
            signOutput.flush();
        }catch(Exception ex){
            JOptionPane.showMessageDialog(null,"Unable to send the message","Error",JOptionPane.ERROR_MESSAGE);
            System.out.print("Error occured: " + ex.getMessage());
        }
    }
    
    //change/update chat window
    private void showMessage(final String m){
        try{
            SwingUtilities.invokeLater(
            new Runnable(){
                public void run(){
                    chatWindow.append(m);
                }
            });
        }catch(Exception ex){
            JOptionPane.showMessageDialog(null,"Unable to send the message","Error",JOptionPane.ERROR_MESSAGE);
            System.out.print("Error occured: " + ex.getMessage());
        }
    }
    
    //gives user permission to type message into the text box
    private void ableToType(final boolean tof){
        try{
            SwingUtilities.invokeLater(
            new Runnable(){
                public void run(){
                    userText.setEditable(tof);
                }
            });
        }catch(Exception ex){
            JOptionPane.showMessageDialog(null,"Error occured","Error",JOptionPane.ERROR_MESSAGE);
            System.out.print("Error occured: " + ex.getMessage());
        }
    }
}
