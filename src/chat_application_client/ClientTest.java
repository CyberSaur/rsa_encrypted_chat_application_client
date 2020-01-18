/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package chat_application_client;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
/**
 *
 * @author Suneth
 */
public class ClientTest{
    
    public static void main(String[] args){
        try{
            Client charlie = new Client("127.0.0.1");
            charlie.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            charlie.startRunning();
        }catch(Exception ex){
            JOptionPane.showMessageDialog(null,"Error occured","Error",JOptionPane.ERROR_MESSAGE);
            System.out.print("Error occured: " + ex.getMessage());
        }
    }    
}
