package org.fisco.bcos.asset.client;

import java.util.*;
import java.io.*;

public class CLI{

    private Map<String, String> map;
    private Scanner scanner;
    private boolean status;
    private String current;
    private String path;

    public CLI(){
        path = "info.txt";
        status = true;
        scanner = new Scanner(System.in);
        map = new HashMap<String, String>();
        readFile();
    }

    public boolean getStatus(){
        return this.status;
    }

    public String getCurrent(){
        return this.current;
    }

    public void setCurrentNull(){
        this.current = null;
    }

    public Map<String, String> getMap(){
        return this.map;
    }

    public void readFile(){
        try{
            FileReader fd = new FileReader(path);
            BufferedReader br = new BufferedReader(fd);
            String s1 = null;
            while((s1 = br.readLine()) != null) {
                String[] temp = s1.split("  ");
                map.put(temp[0],temp[1]);
            }
           br.close();
           fd.close();
        } catch (IOException e) {
            System.out.println("Error:" + e.getMessage());
        }
    }

	public void writeToFile()
	{
		try{
            File file = new File(path);
            FileWriter fw = new FileWriter(file,false);
            for (String key : map.keySet()) {
                String temp = key+"  "+map.get(key);
                fw.write(temp+"\n");
            }

            fw.flush();
            fw.close();    

        } catch (IOException e) {
            System.out.println("Error:" + e.getMessage());
        }
	}

    public boolean login()
    {
        int choice;
        String account, password, again;
        Console console = System.console();
        System.out.println("------Welecome to the FISCO-BCOS Trade Platform------\n");
<<<<<<< HEAD
        System.out.println("Plz enter:\n1:SIGN IN\t2:SIGN UP\t0:quit()");
=======
        System.out.println("Plz enter:\n0:SIGN IN\t1:SIGN UP\t2:quit()");
>>>>>>> b0e2296dddd942d0d94f65d583e0637ddc75f4d0
        if (scanner.hasNextInt()){
            choice = scanner.nextInt();
            switch(choice){

<<<<<<< HEAD
                case 1:
=======
                case 0:
>>>>>>> b0e2296dddd942d0d94f65d583e0637ddc75f4d0
                    account = (String)scanner.nextLine();
                    System.out.print("------SIGN IN------\nID: ");
                    account = (String)scanner.nextLine();
                    System.out.print("Password:");
                    password = new String(console.readPassword());
                    if (map.get(account)!=null && map.get(account).compareTo(password) == 0) {
                        current = account;
                        System.out.print("Sign in successfully! Wait for key...");
                        again = (String)scanner.nextLine();
                        return true;
                    } else {
                        System.out.print("No account or wrong password! Wait for key...");
                        again = (String)scanner.nextLine();
                        return false;
                    } 

<<<<<<< HEAD
                case 2:
                    account = (String)scanner.nextLine();
                    System.out.print("------REGISTER------\nID: ");
=======
                case 1:
                    account = (String)scanner.nextLine();
                    System.out.print("------REGISTER------\n ID: ");
>>>>>>> b0e2296dddd942d0d94f65d583e0637ddc75f4d0
                    account = (String)scanner.nextLine();
                    System.out.print("Password:");
                    password = new String(console.readPassword());
                    System.out.print("Input Again:");
                    again = new String(console.readPassword());
                    if (password.compareTo(again)==0 && map.get(account)==null) {
                        map.put(account, password);
                        writeToFile();
                        readFile();
                        System.out.print("Register success! Wait for key...");
                        again = (String)scanner.nextLine();
                        return false;
                    } else {
                        System.out.print("Register failed! Wait for key...");
                        again = (String)scanner.nextLine();
                        return false;
                    }

<<<<<<< HEAD

=======
                case 2:
                    this.status = false;
                    return false;
>>>>>>> b0e2296dddd942d0d94f65d583e0637ddc75f4d0

                default:
                    System.out.print("Invalid input! Wait for key...");
                    again = (String)scanner.nextLine();
                    return false;
            }
        }
        else {
            System.out.print("Invalid input! Wait for key...");
            return false;
        }
    }

    public void clearFile(){
        for (int i = 0; i < 20; ++i) System.out.print("\n");
    }

    public void msg(){
        System.out.print("Dear "+current+", what do u want to do next?\n");
        System.out.println("1: Check my credit.\n2: Trade with others.\n3: Financing/Loan from Bank.\n4: IOU split.\n5: Transfer/Loan Repayment.\n6: Query transaction.\n0: Exit\n\n");

    }

}
