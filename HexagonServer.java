import java.util.*;
import java.net.*;
import java.io.*;

// Made by Miller Hollinger
// Use HexNet to connect to the server

/*
To Do:
-Corruption update
   -CPT_Endless hack - creates corruption
   -PUR_Counter hack - deletes corruption
   -STABLIZER locagon - stops spread of corruption
-Bug testing
*/

public class HexagonServer
{
   public static ArrayList<HEXid> users = new ArrayList<HEXid>();
   public static ArrayList<Socket> sockets = new ArrayList<Socket>();
   public static HexBlock[][] world = new HexBlock[200][200];
   
   public static void main(String[] args) throws Exception
   {
      ServerSocket server = new ServerSocket(6000);
      for (int i = 0; i < world.length; i++)
      {
         for (int j = 0; j < world[0].length; j++)
         {
            world[i][j] = new HexBlock();
            print(world[i][j].getDisplay());
         }
         println("");
      }
      
      //Random rn = new Random();
      //int corX = rn.nextInt(200);//Where the CORRUPT BIT is
      //int corY = rn.nextInt(200);
      //world[corX][corY].type = "BIT";
      //world[corX][corY].level = 7;
      
      new KickThread().start();
      try
      {
         while(true)
         {
            new ServerThread(server.accept()).start();
         }
      }
      catch(Exception e)
      {
         println("[!] Unexpected Error : "+e);
      }
   }
   
   private static class KickThread extends Thread
   {
      ArrayList<String> userStrs;
      public KickThread()
      {
         userStrs = new ArrayList<String>();
      }
      public void run()
      {
         Scanner userInput = new Scanner(System.in);
         while(true)
         {
            println("[>] ADMIN THREAD ONLINE");
            println("[>] K/[HEXid]");
            println("[>]   Kicks user [HEXid]");
            println("[>] U/");
            println("[>]   Prints out all online users");
            println("[>] P/");
            println("[>]   Prints out the world.");
            String cmd = userInput.nextLine();
            ArrayList<String> names = new ArrayList<String>();
            for (int i = 0; i < users.size(); i++)
               names.add(users.get(i).id);
            if (cmd.substring(0,1).equals("K"))
            {
               String toKick = cmd.substring(2,cmd.length());
               for (int i = 0; i < names.size(); i++)
               {
                  if (names.get(i).equals(toKick))
                  {
                     println("[>] User "+toKick+" found.");
                     println("[>] Enter reason to kick, or X to cancel.");
                     String why = userInput.nextLine();
                     if (!why.equals("X"))
                     {
                        boolean success = false;
                        for (int j = 0; j < users.size(); j++)
                           if (users.get(j).id.equals(toKick))
                           {
                              success = true;
                              users.get(j).kick(why);
                              j += users.size();
                           }
                        if (success)
                           println("[>] Success. "+toKick+" removed from game.");
                        else
                           println("[!] FAILED - USER NOT FOUND");
                     }
                     else
                        println("[>] Cancelled. User "+toKick+" not kicked.");
                  }
               }
            }
            else if (cmd.substring(0,1).equals("U"))
            {
               println("[>] Printing online users.");
               for (int i = 0; i < names.size(); i++)
                  println("[>] "+names.get(i));
            }
            else if (cmd.substring(0,1).equals("P"))
            {
               for (int i = 0; i < world.length; i++)
               {
                  for (int j = 0; j < world[0].length; j++)
                  {
                     world[i][j] = new HexBlock();
                     print(world[i][j].getDisplay());
                  }
                  println("");
               }
            }
            else
               println("[!] INVALID COMMAND");
            println(">>>---<<<>>>---<<<>>>---<<<>>>---<<<>>>---<<<>>>---<<<");
         }
      }
   }
   
   private static class ServerThread extends Thread
   {
      private Socket socket;
   
      private PrintWriter printer;
   
      private BufferedReader reader;
   
      private String in;
   
      private HEXid user;
      
      //Constructor
      public ServerThread(Socket s)
      {
         try
         {
            socket = s;
            sockets.add(s);
            user = new HEXid();
            user.inventory.add(new InventoryItem("ASSEMBLER",1,true));
            for (int i = 0; i < 6; i++)
            {
               user.bits.add(new InventoryItem("BIT",i+1,false));
               user.bits.get(i).count = 0;
            }
            user.level = 1;
            users.add(user);
            printer = new PrintWriter(socket.getOutputStream(), true);
            reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            println("[+]Socket ["+socket.toString()+"] SUCCESS: CREATION//"+user.id);
         }
         catch (Exception e)
         {
            println("[!]ServerThread ["+s.toString()+"] FAIL: CREATION");
         }
      }
   
      //The run() function for each thread
      public void run()
      {
         println("[+]Socket ["+socket.toString()+"] SUCCESS: RUN");
         printer.println(">>HEXAGON ONLINE<<");
         printer.println("v1.1.1 : 9/8/18");
         printer.println("(C) Hexagon Studios");
         printer.println("READ THE TUTORIAL! TYPE TUT/1 FOR TUTORIAL! READ IT! READ IT!");
         printer.println("Run CMD/ and HEX/ for command lists.");
         printer.println("!>DO NOT CLICK THE OUTPUT BOX OR AUTOSCROLL WILL BREAK<!");
         printer.println("");
         /*printer.println("<?> C0RRUP710N UPD4TE <?>");
         printer.println("A new EVIL BIT can be found - the X BIT!");
         printer.println("Go to INF/ to learn more about this evil, evil bit! What could it do...?");
         printer.println("");*/
         printer.println("--Changes--");
         printer.println(">Bits are easier to find - from 1/6 chance to 1/4!");
         printer.println("  -To balance this, you can only hold 60 bits total.");
         printer.println("  -Suggestion : use a TRANSFER to turn your bits to <#>.");
         printer.println(">You can build on top of your own locagons, destroying the older one.");
         printer.println(">World quadrupled in size to allow for more building space.");
         printer.println("  -Went from 100x100(10k blocks) to 200x200(40k blocks)");
         printer.println(">Adding CORRPUTION soon.");
         boolean userDetected = false;
         try
         {
            while(true)
            {
               if (user.id.substring(0,6).equals("KICKED"))
               {
                  kickPlayer(user.id.substring(7,user.id.length()));
               }
               if (user.id.substring(0,6).equals("DELETE"))
               {
                  world[user.row][user.column].userHere = false;
                  printer.println("    #-----#    ");
                  printer.println("    | .-. |    ");
                  printer.println("    | | | |    ");
                  printer.println("    | |-| |    ");
                  printer.println("    |\\   /|    ");
                  printer.println("    | \\ / |    ");
                  printer.println("    |  \\  |    ");
                  printer.println("    | / \\ |    ");
                  printer.println("    |/   \\|    ");
                  printer.println("    #-----#    ");
                  printer.println("  <GAME>       ");
                  printer.println("       <OVER>  ");
                  printer.println("You have been defeated!");
                  printer.println("The hacker "+user.id.substring(7,user.id.length())+" has deleted you with VIR_Delete.");
                  printer.println("You have been given a new HEXid and your game has been restarted.");
                  user = new HEXid();
                  printer.println("Get your revenge! Make a team, track them down, do whatever it takes to stop them!");
                  printer.println("Good luck, "+user.id+"!");
               }
               
               printer.println(">>>>-><-><-><-><-><-><-><-><-><-><-><-><-><-><-><-><-><-><-><-><-><-><-><-><-><-><-><-><-><-><-<<<<");
               if (user.charge >= user.level*10 && user.level < 6)
               {
                  printer.println("@>>-<<-->>>>--------<<<<-->>-<<@");
                  printer.println("| LEVEL UP!                    |");
                  printer.println("| -You've levelled up! Great!  |");
                  printer.println("| >--> LV  "+user.level+"  ->>>- LV  "+(user.level+1)+"  >-->|");
                  printer.println("| +Better Locagons Unlocked!   |");
                  printer.println("| +Better Hacks Unlocked!      |");
                  printer.println("| +Higher Hack Resistance!     |");
                  printer.println("@>>-<<-->>>>--------<<<<-->>-<<@");
                  user.charge -= user.level*10;
                  user.level++;
               }
               in = reader.readLine();
               try
               {
                  String base = in;
                  String arguments = in;
                  if (in.length() >= 4)
                  {
                     base = in.substring(0,4);
                     arguments = in.substring(4,in.length());
                  }
                  switch (base)
                  {
                     case "CMD/":
                        CMD(arguments);
                        break;
                     case "CHT/":
                        CHT(arguments);
                        break;
                     case "XFR/":
                        XFR(arguments);
                        break;
                     case "TUT/":
                        TUT(arguments);
                        break;
                     case "ACT/":
                        ACT(arguments);
                        break;
                     case "HEX/":
                        HEX(arguments);
                        break;
                     case "RUL/":
                        RUL(arguments);
                        break;
                     /*case "INF/":
                        INF(arguments);
                        break;*/
                     case "W":
                        if (isMoveable(world[user.row-1][user.column]))
                        {
                           world[user.row][user.column].userHere = false;
                           if (user.invisLeft == 0)
                              world[user.row-1][user.column].userHere = true;
                           else
                              user.invisLeft--;
                           user.row -= 1;
                           checkForBits();
                           visualizeWorld();
                        }
                        else
                           printer.println("[!] UNABLE TO MOVE - SOMETHING BLOCKING MOVEMENT");
                        break;
                     case "A":
                        if (isMoveable(world[user.row][user.column-1]))
                        {
                           world[user.row][user.column].userHere = false;
                           if (user.invisLeft == 0)
                              world[user.row][user.column-1].userHere = true;
                           else
                              user.invisLeft--;
                           user.column -= 1;
                           checkForBits();
                           visualizeWorld();
                        }
                        else
                           printer.println("[!] UNABLE TO MOVE - SOMETHING BLOCKING MOVEMENT");
                        break;
                     case "S":
                        if (isMoveable(world[user.row+1][user.column]))
                        {
                           world[user.row][user.column].userHere = false;
                           if (user.invisLeft == 0)
                              world[user.row+1][user.column].userHere = true;
                           else
                              user.invisLeft--;
                           user.row += 1;
                           checkForBits();
                           visualizeWorld();
                        }
                        else
                           printer.println("[!] UNABLE TO MOVE - SOMETHING BLOCKING MOVEMENT");
                        break;
                     case "D":
                        if (isMoveable(world[user.row][user.column+1]))
                        {
                           world[user.row][user.column].userHere = false;
                           if (user.invisLeft == 0)
                              world[user.row][user.column+1].userHere = true;
                           else
                              user.invisLeft--;
                           user.column += 1;
                           checkForBits();
                           visualizeWorld();
                        }
                        else
                           printer.println("[!] UNABLE TO MOVE - SOMETHING BLOCKING MOVEMENT");
                        break;
                  }
               }
               catch (Exception e)
               {
                  printer.println("[!] BAD SYNTAX - USE CMD/ FOR COMMANDS");
               }
            }
         }
         catch (Exception e)
         {
            printer.println("[!] THE HEXAGON HAS CRASHED - PLEASE RECONNECT");
            println("[!] USER QUIT: "+user.id);
            world[user.row][user.column].userHere = false;
            for (int i = 0; i < world.length; i++)
            {
               for (int j = 0; j < world[0].length; j++)
               {
                  if (world[i][j].owner.equals(user.id))
                  {
                     world[i][j].owner = "ABANDONED";
                     world[i][j].fee = 0;
                  }
               }
            }
            users.remove(user);
         }
      }
      
      //Command list.
      public void INF(String arguments)
      {
         if (arguments.length() == 0)
         {
            printer.println(">>>HEXAGON INFO PAGE<<<");
            printer.println("P1-P3: The Corruption");
            printer.println("...More coming as updates released...");
         }
         else
         {
            switch (Integer.parseInt(arguments))
            {
               case 1: 
                  printer.println(">>PAGE 1                                               <<");
                  printer.println("The Corruption is the name given to the evil ? character.");
                  printer.println("It is never seen in a good Hexagon. If no user is evil,  ");
                  printer.println("The Corruption will never be seen, and the world will be ");
                  printer.println("at peace. But should a user with a deathwish and a hope  ");
                  printer.println("to undo the entire Hexagon come across the X bit, then,  ");
                  printer.println("The Corruption can appear. When a user uses one X bit in ");
                  printer.println("a LV 7 COMPILER -- there is only one X BIT and one LV 7  ");
                  printer.println("COMPILER -- they can create the most destructive hack    ");
                  printer.println("ever. The one that will destroy the Hexagon.             ");
                  printer.println("                                             NEXT PAGE...");
                  break;
               case 2: 
                  printer.println(">>PAGE 2                                               <<");
                  printer.println("CPT_Endless is the cursed hack capable of undoing the    ");
                  printer.println("very fabric of the Hexagon. The user who runs it destroys");
                  printer.println("themself and, in the process, creates CORRUPTION.        ");
                  printer.println("CORRUPTION, displayed as a ?, spreads every time a user  ");
                  printer.println("runs any command. It also slows down the running of every");
                  printer.println("command the more of them there are. They kill on touch,  ");
                  printer.println("go through any locagon of LV 5 or less, and will consume ");
                  printer.println("all the Hexagon. Once run, there is little hope left for ");
                  printer.println("those who wish to inhabit the lands. However...          ");
                  printer.println("                                             NEXT PAGE...");
                  break;
               case 3: 
                  printer.println(">>PAGE 3                                               <<");
                  printer.println("There is one hope: The Firewall.");
                  printer.println("The Firewall is the hero of the Hexagon. When CPT_Endless");
                  printer.println("is run, one person becomes The Firewall. They become LV 7");
                  printer.println("and gain an infinitely reusable hack : PUR_Counter, and  ");
                  printer.println("50 LV 5 STABILIZER Locagons. PUR_Counter deletes any     ");
                  printer.println("CORRUPTION left onscreen, and STABILIZERs stop any       ");
                  printer.println("CORRUPTION from spreading nearby. Only they can protect  ");
                  printer.println("the Hexagon from the CORRUPTION! Perhaps the task is too ");
                  printer.println("hard. Is it better to let the world die? Good luck!      ");
                  break;
            }
         }
      }
      
      //Command list.
      public void CMD(String arguments)
      {
         printer.println("[>] CMD/");
         printer.println("[>]    Displays this menu");
         printer.println("[>] RUL/");
         printer.println("[>]    Displays rules !!!MUST READ!!!");
         printer.println("[>] CHT/[Message]");
         printer.println("[>]    Sends [Message] to chat");
         printer.println("[>] XFR/[Count]/[HEXid]");
         printer.println("[>]    Sends [Count]<#> to [HEXid]");
         printer.println("[>] TUT/[p]");
         printer.println("[>]    Prints out page [p] of the tutorial, 1-4");
         printer.println("[>] ACT/");
         printer.println("[>]    Prints out your account data");
         /*printer.println("[>] INF/[p]");
         printer.println("[>]    Story-related information, page [p]");*/
         printer.println("[>] HEX/");
         printer.println("[>]    Prints out the HEX main menu");
      }  
      
      //Rules.
      public void RUL(String arguments)
      {
         printer.println("[>] ---RULES-------------------------------------------------------------");
         printer.println("[>] In the Hexagon, you CAN, and SHOULD:");
         printer.println("[>] Steal, Embezzle, Launder Money, Rig Elections, Conquer Domiciles, ");
         printer.println("[>] Infiltrate Government Controlled Areas, Assassinate the Leader, ");
         printer.println("[>] Break Into Banks, Pickpocket <#>, Imprison Someone, Imprison EVERYONE, ");
         printer.println("[>] Delete a City, and, of course, HACK EVERYTHING!");
         printer.println("[>]");
         printer.println("[>] Hate speech isn't permitted. Don't be a jerk.");
         printer.println("[>]");
         printer.println("[>] The only way to get kicked is to be really, really offensive in chat.");
         printer.println("[>]");
         printer.println("[>] Really! You can do practically anything else. Pickpocket your pals,");
         printer.println("[>] sneak around a city, steal hundreds of <#>. Go ahead!");
         printer.println("[>]");
         printer.println("[>] One more time: THE ONLY KICKABLE THING YOU CAN DO IS HATE SPEECH.");
         printer.println("[>] We ENCOURAGE stealing, wrecking bases, and hacking ingame.");
      } 
      
      //Chat program.
      public void CHT(String arguments)
      {
         if (arguments.length() > 0)
         {
            printer.println("[>] SENDING.");
            println("[CHAT] " + user.id + " >> " + arguments);
            for (Socket s : sockets)
            {
               try
               {
                  new PrintWriter(s.getOutputStream(), true).println("[CHAT] " + user.id + " >> " + arguments);
               }
               catch (Exception e)
               {
                  printer.println("[!] FAILED TO SEND TO A USER");
               }
            }
         }
         else
         {
            printer.println("[!] BAD SYNTAX - USE CMD/ FOR COMMANDS");
         }
         
      }
      
      //Hex transfer program.
      public void XFR(String arguments)
      {
         try
         {
            int slashLoc = arguments.indexOf("/");
            if (slashLoc != -1)
            {
               int toSend = Integer.parseInt(arguments.substring(0,slashLoc));
               String targetID = arguments.substring(slashLoc+1, arguments.length());
               if (toSend <= user.hexes && toSend > 0)
                  for (HEXid current : users)
                  {
                     if (current.id.equals(targetID))
                     {
                        user.hexes = user.hexes - toSend;
                        current.hexes = current.hexes + toSend;
                        printer.println("[>] SUCCESS! SENT "+toSend+"<#> TO "+current.id);
                        println("[>] "+user.id+" SENT "+toSend+"<#> TO "+current.id);
                        return;
                     }
                  }
               printer.println("[!] INVALID ARGUMENTS - SEND POSITIVE HEXES TO A REAL USER");
            }
            else
               printer.println("[!] BAD SYNTAX - USE CMD/ FOR COMMANDS");
         }
         catch (Exception e)
         {
            printer.println("[!] BAD SYNTAX - USE CMD/ FOR COMMANDS");
         }
      }
      
      //Tutorial program.
      public void TUT(String arguments)
      {
         try
         {
            int page = Integer.parseInt(arguments);
            switch (page)
            {
               case 1:
                  printer.println("[>] ---TUTORIAL PAGE 1---------------------------");
                  printer.println("[>] Welcome to the Hexagon! The Hexagon is a kind");
                  printer.println("[>] of private internet. Here, the goal is to    ");
                  printer.println("[>] earn -- or steal -- all the <#>(called hexes)");
                  printer.println("[>] you can get your hands on. <#> is money here,");
                  printer.println("[>] so naturally everyone wants to steal it from ");
                  printer.println("[>] you. You should also try and steal it from   ");
                  printer.println("[>] everyone. How, though? This tutorial will get");
                  printer.println("[>] you from n00b from PRO in a couple pages. Now");
                  printer.println("[>] you should run TUT/2 to view page 2, where we");
                  printer.println("[>] will talk about commands.                    ");
                  break;
               case 2:
                  printer.println("[>] ---TUTORIAL PAGE 2---------------------------");
                  printer.println("[>] Commands! Everything in the Hexagon is       ");
                  printer.println("[>] controlled by commands. You've already run a ");
                  printer.println("[>] few to get here -- TUT/1 and TUT/2. Each     ");
                  printer.println("[>] command is pretty simple. First, you write a ");
                  printer.println("[>] base, like TUT/, and then put in data to help");
                  printer.println("[>] the command run, like the page numbers. Now, ");
                  printer.println("[>] run the command CMD/. CMD/ does not have any ");
                  printer.println("[>] data to type in, just type CMD/ and nothing  ");
                  printer.println("[>] else. It will give you a list of commands you");
                  printer.println("[>] can run. Go to the next page.                ");
                  break;
               case 3:
                  printer.println("[>] ---TUTORIAL PAGE 3---------------------------");
                  printer.println("[>] The CMD/ list has some commands that look    ");
                  printer.println("[>] like ABC/[Thing1]/[Thing2]. To understand    ");
                  printer.println("[>] what [Thing1] and [Thing2] mean, read the    ");
                  printer.println("[>] text under the command. It will tell you what");
                  printer.println("[>] to replace [Thing1] and [Thing2] with when   ");
                  printer.println("[>] you enter in the command. It might be a word,");
                  printer.println("[>] a number, or something else. For example,    ");
                  printer.println("[>] look at XFR/[Count]/[HEXid]. If you wanted to");
                  printer.println("[>] send 13<#> to A67B2F, you would type in      ");
                  printer.println("[>] XFR/13/A67B2F. Just 1 more page!             ");
                  break;
               case 4:
                  printer.println("[>] ---TUTORIAL PAGE 4---------------------------");
                  printer.println("[>] Every command can be found somewhere in a    ");
                  printer.println("[>] menu. CMD/ has the general commands. Before  ");
                  printer.println("[>] you continue, run CHT/[Message] to say hi or ");
                  printer.println("[>] something. Once you're ready for the real    ");
                  printer.println("[>] game, and you've read the rules, type HEX/.  ");
                  printer.println("[>] It will give you the HEX/ command list. It's ");
                  printer.println("[>] pretty big! There is a lot you can do in the ");
                  printer.println("[>] Hexagon -- building, hacking, stealing, etc. ");
                  printer.println("[>] When you're ready, read the HEX/ tutorial too");
                  printer.println("[>] so you know what's going on. Tutorial over!  ");
                  break;
               default:
                  printer.println("[!] PAGE "+page+" DOES NOT EXIST");
                  break;
            }
         }
         catch (Exception e)
         {
            printer.println("[!] BAD SYNTAX - USE CMD/ FOR COMMANDS");
         }
      }
      
      //Account data.
      public void ACT(String arguments)
      {
         printer.println("[>] --HexID "+user.id); 
         printer.println("[>] Level "+user.level); 
         printer.println("[>] Charge "+user.charge+"/"+user.level*10);
         printer.println("[>] Hexes "+user.hexes+"<#>");
         printer.println("[>] Location X"+user.column+",Y"+user.row);
      } 
      
      //The game.
      public void HEX(String arguments)
      {
         try
         {
            if (arguments.length() == 0)
            {
               printer.println("[>] /-------------\\");
               printer.println("[>] <HEXAGON WORLD>");
               printer.println("[>] \\-------------/");
               printer.println("[>] Welcome to Hexagon World!");
               printer.println("[>] HEX/");
               printer.println("[>]    Displays this menu");
               printer.println("[>] HEX/C");
               printer.println("[>]    Displays a list of what each character means");
               printer.println("[>] HEX/T/[p]");
               printer.println("[>]    Displays page [p] of the tutorial");
               printer.println("[>] HEX/[Direction]");
               printer.println("[>]    Moves you around the world and runs HEX/V afterwards");
               printer.println("[>]    Use W,A,S,D to move Up,Left,Down,Right respectively");
               printer.println("[>] [Direction]");
               printer.println("[>]    Shorthand for HEX/[Direction], does the same thing but");
               printer.println("[>]    is easier and faster to type");
               printer.println("[>] HEX/V");
               printer.println("[>]    Shows you the world around you");
               printer.println("[>] HEX/I");
               printer.println("[>]    Tells you what you can interact with where you are");
               printer.println("[>] HEX/B/[Direction]/[Level]/[Type]");
               printer.println("[>]    If you have one in your inventory, places a level [Level] ");
               printer.println("[>]    locagon type [Type] on the ground next to you in direction [Direction]");
               printer.println("[>] HEX/H");
               printer.println("[>]    Tells you what hacks you have and how to use each");
               printer.println("[>] HEX/L");
               printer.println("[>]    Displays your entire inventory");
            }
            else
            {
               switch (arguments.substring(0,1))
               {
                  case "T": //DONE!
                     findHexTutorial(Integer.parseInt(arguments.substring(2,arguments.length())));
                     break;
                     
                  case "C": //DONE!
                     printCharSet();
                     break;
                        
                  case "V": //DONE!
                     visualizeWorld();
                     break;
                        
                  case "I": //DONE!
                     getLocationCommands();
                     break;
                        
                  case "B": //DONE!
                     attemptBuild(arguments);
                     break;
                        
                  case "H":
                     if (arguments.equals("H"))
                        getUserHacks();
                     else
                     {
                        if (arguments.substring(2,5).equals("EXP"))
                           getHackTut(arguments.substring(6, arguments.length()));
                        else if (arguments.substring(2,5).equals("RUN"))
                        {
                           int hack = Integer.parseInt(arguments.substring(6, arguments.length()));
                           if (user.hacks.size() > hack)
                           {
                              doHack(user.hacks.get(hack).name, user.hacks.get(hack).level);
                              user.hacks.remove(hack);
                           }
                        }
                        else 
                           printer.println("[!] BAD SYNTAX - USE CMD/ FOR COMMANDS");
                     }
                     break;
                        
                  case "L": //DONE!
                     printer.println("[>] >>LOCAGONS<<");
                     for (int i = 0; i < user.inventory.size(); i++)
                        printer.println("[>] "+(i+1)+" : "+user.inventory.get(i).toString());
                     printer.println("[>] >>BITS<<");
                     for (int i = 0; i < user.bits.size(); i++)
                        printer.println("[>] BIT LV "+(i+1)+" : "+user.bits.get(i).count);
                     break;
                        
                  case "W": //DONE!
                     if (isMoveable(world[user.row-1][user.column]))
                     {
                        world[user.row][user.column].userHere = false;
                        if (user.invisLeft == 0)
                           world[user.row-1][user.column].userHere = true;
                        else
                           user.invisLeft--;
                        user.row -= 1;
                        checkForBits();
                        visualizeWorld();
                     }
                     else
                        printer.println("[!] UNABLE TO MOVE - SOMETHING BLOCKING MOVEMENT");
                     break;
                  case "A": //DONE!
                     if (isMoveable(world[user.row][user.column-1]))
                     {
                        world[user.row][user.column].userHere = false;
                        if (user.invisLeft == 0)
                           world[user.row][user.column-1].userHere = true;
                        else
                           user.invisLeft--;
                        user.column -= 1;
                        checkForBits();
                        visualizeWorld();
                     }
                     else
                        printer.println("[!] UNABLE TO MOVE - SOMETHING BLOCKING MOVEMENT");
                     break;
                  case "S": //DONE!
                     if (isMoveable(world[user.row+1][user.column]))
                     {
                        world[user.row][user.column].userHere = false;
                        if (user.invisLeft == 0)
                           world[user.row+1][user.column].userHere = true;
                        else
                           user.invisLeft--;
                        user.row += 1;
                        checkForBits();
                        visualizeWorld();
                     }
                     else
                        printer.println("[!] UNABLE TO MOVE - SOMETHING BLOCKING MOVEMENT");
                     break;
                  case "D": //DONE!
                     if (isMoveable(world[user.row][user.column+1]))
                     {
                        world[user.row][user.column].userHere = false;
                        if (user.invisLeft == 0)
                           world[user.row][user.column+1].userHere = true;
                        else
                           user.invisLeft--;
                        user.column += 1;
                        checkForBits();
                        visualizeWorld();
                     }
                     else
                        printer.println("[!] UNABLE TO MOVE - SOMETHING BLOCKING MOVEMENT");
                     break;
                        
                  case "E": //DONE!
                     localAction(arguments.substring(2, arguments.length()));
                     break;
               }
            }
         }
         catch (Exception e)
         {
            printer.println("[!] BAD SYNTAX - USE CMD/ FOR COMMANDS");
         }
      } 
      
      //Destroys the given block. If it's less than or equal the given hack level and a locoagon, it destroys it and rewards the user.
      public void destroy(int level, int x, int y)
      {
         if (!world[x][y].type.equals("NOTHING"))
            if (world[x][y].level <= level)
            {
               if (!world[x][y].type.equals("BANK"))
               {
                  boolean before = world[x][y].userHere;
                  printer.println("[H] Destroyed X"+x+" Y"+y+"!");
                  printer.println("[H] Salvaged 2 LV "+world[x][y].level+" BIT!");
                  giveBit(world[x][y].level);
                  giveBit(world[x][y].level);
                  world[x][y] = new HexBlock("NOTHING", 1);
                  world[x][y].userHere = before;
               }
               else
                  printer.println("[!] BANKS CANNOT BE DESTROYED");
            }
            else
               printer.println("[!] FAILED TO DESTROY X"+x+" Y"+y+" - SECURITY TOO HIGH");
      }
      
      //Attempts to gain access to the given block.
      public void hackAccess(int level, int x, int y)
      {
         if (!world[x][y].type.equals("NOTHING"))
            if (world[x][y].level <= level)
            {
               printer.println("[H] Accessed X"+x+" Y"+y+"!");
               world[x][y].access.add(user.id);
            }
            else
               printer.println("[!] FAILED TO ACCESS X"+x+" Y"+y+" - SECURITY TOO HIGH");
      }
      
      //Attempts to override the given block.
      public void hackOwnership(int level, int x, int y)
      {
         if (!world[x][y].type.equals("NOTHING"))
            if (world[x][y].level <= level)
            {
               if (!world[x][y].type.equals("BANK"))
               {
                  printer.println("[H] Overrode X"+x+" Y"+y+"!");
                  world[x][y].owner = user.id;
               }
               else
               {
                  if (!user.ownsBank)
                  {
                     printer.println("[H] Overrode X"+x+" Y"+y+"!");
                     world[x][y].owner = user.id;
                     user.ownsBank = true;
                  }
                  else
                     printer.println("[!] FAILED TO CLAIM X"+x+" Y"+y+" - YOU ALREADY HAVE A BANK");
               }
            }
            else
               printer.println("[!] FAILED TO CLAIM X"+x+" Y"+y+" - SECURITY TOO HIGH");
      }
      
      //Runs a hack.
      public void doHack(String name, int level)
      {
         printer.println("[H] Running Hack: "+name+" LV "+level);
         printer.println("[H] Run Reward : 3 LV "+level+" BITS");
         printer.println("[H] Run Reward : "+level+" CHARGE");
         for (int i = 0; i < 3; i++)
            giveBit(level);
         user.charge+=level;
         switch(name)
         {
            case "PWR_Crash":
               destroy(level, user.row, user.column);
               break;
            case "PWR_Wave": 
               for (int i = user.row-1; i <= user.row+1; i++)
                  for (int j = user.column-1; j <= user.column+1; j++)
                     destroy(level, i, j);
               break;
            case "PWR_Clear": 
               for (int i = user.row-3; i <= user.row+3; i++)
                  for (int j = user.column-3; j <= user.column+3; j++)
                     destroy(level, i, j);
               break;
                           
            case "OVR_Access": 
               hackAccess(level, user.row, user.column);
               break;
            case "OVR_Capture": 
               hackOwnership(level, user.row, user.column);
               break;
            case "OVR_Conquer": 
               for (int i = user.row-3; i <= user.row+3; i++)
                  for (int j = user.column-3; j <= user.column+3; j++)
                     hackOwnership(level, i, j);
               break;
                           
            case "PIK_Steal": 
               if (world[user.row][user.column].type.equals("BANK"))
               {
                  if (world[user.row][user.column].level <= level)
                  {
                     boolean success = false;
                     for (int i = 0; i < users.size(); i++)
                     {
                        if (users.get(i).id.equals(world[user.row][user.column].owner))
                        {
                           user.hexes += users.get(i).inBank;
                           printer.println("[H] Stole "+users.get(i).inBank+"<#> from "+users.get(i).id+"'s BANK!");
                           users.get(i).inBank = 0;
                           success = true;
                        }
                     }
                     if (!success)
                        printer.println("[!] HACK FAILED - USER DOES NOT EXIST");
                  }
                  else
                     printer.println("[!] HACK FAILED - SECURITY TOO HIGH");
               }
               else
                  printer.println("[!] HACK FAILED - NOT ON A BANK");
               break;
            case "PIK_Pick": 
               for (int i = user.row-1; i <= user.row+1; i++)
                  for (int j = user.column-1; j <= user.column+1; j++)
                     if (world[i][j].type.equals("GATE"))
                        if (world[i][j].level <= level)
                           world[i][j].owner = user.id;
                        else
                           printer.println("[!] FAILED TO CLAIM X"+i+" Y"+j+" - SECURITY TOO HIGH"); 
               break;
            case "PIK_Slip": 
               user.invisLeft += 20;
               world[user.row][user.column].userHere = false;
               break;
                           
            case "VIR_Take": 
               for (int i = user.row-1; i <= user.row+1; i++)
                  for (int j = user.column-1; j <= user.column+1; j++)
                     if (world[i][j].userHere)
                        for (int k = 0; k < users.size(); k++)
                           if (users.get(k).row == i && users.get(k).column == j)
                           {
                              if (users.get(k).level <= level)
                              {
                                 if (i != user.row || j != user.column)
                                 {
                                    int stolen = users.get(k).hexes/5;
                                    users.get(k).hexes -= stolen;
                                    user.hexes += stolen;
                                    printer.println("[H] Stole "+stolen+"<#> from "+users.get(k).id+"!");
                                 }
                              }
                              else
                                 printer.println("[!] FAILED TO STEAL FROM USER "+users.get(k).id+" - LEVEL TOO HIGH");
                           }
               break;
            case "VIR_Rob": 
               for (int i = user.row-1; i <= user.row+1; i++)
                  for (int j = user.column-1; j <= user.column+1; j++)
                     if (world[i][j].userHere)
                        for (int k = 0; k < users.size(); k++)
                           if (users.get(k).row == i && users.get(k).column == j)
                           {
                              if (users.get(k).level <= level)
                              {
                                 if (i != user.row || j != user.column)
                                 {
                                    int stolen = users.get(k).hexes*4/5;
                                    users.get(k).hexes -= stolen;
                                    user.hexes += stolen;
                                    printer.println("[H] Stole "+stolen+"<#> from "+users.get(k).id+"!");
                                 }
                              }
                              else
                                 printer.println("[!] FAILED TO STEAL FROM USER "+users.get(k).id+" - LEVEL TOO HIGH");
                           }
               break;
            case "VIR_Delete": 
               for (int i = user.row-1; i <= user.row+1; i++)
                  for (int j = user.column-1; j <= user.column+1; j++)
                     if (world[i][j].userHere)
                        for (int k = 0; k < users.size(); k++)
                           if (users.get(k).row == i && users.get(k).column == j)
                           {
                              if (users.get(k).level <= level)
                              {
                                 if (i != user.row || j != user.column)
                                 {
                                    int stolen = users.get(k).hexes;
                                    users.get(k).hexes -= stolen;
                                    user.hexes += stolen;
                                    printer.println("[H] Stole "+stolen+"<#> from "+users.get(k).id+"!");
                                    printer.println("[H] Deleted "+users.get(k).id+"! You monster!");
                                    users.get(k).id = "DELETE/"+user.id;
                                 }
                              }
                              else
                                 printer.println("[!] FAILED TO DEFEAT USER "+users.get(k).id+" - LEVEL TOO HIGH");
                           }
               break;
               
            case "CPT_Endless":
               printer.println("[H] How could you?! Save us, Firewall! Save us all!");
               user.kick("CORRUPTING THE WORLD - DISREGARD BAN WARNINGS - GOOD JOB!");
               break;
         }
         printer.println("[H] Run Complete");
         visualizeWorld();
      }
      
      //Returns true if the player is allowed to walk on this block.
      public boolean isMoveable(HexBlock block)
      {
         if (block.type.equals("FIREWALL")) //If a firewall blocks the user
            return false;
         if (block.userHere) //If a user is already standing here
            return false;
         if (block.type.equals("GATE") && !block.access.contains(user.id) && !block.owner.equals(user.id)) //If this a gate that the user can't access
            return false;
         return true;
      }
      
      //Tries to place a locagon.
      public void attemptBuild(String arguments)
      {
         try
         {
            String direction = arguments.substring(2,3);
            String toPlace = arguments.substring(6,arguments.length());
            int level = Integer.parseInt(arguments.substring(4,5));
            if (checkInvFor(toPlace, level))
            {
               if (!(toPlace.equals("BANK") && user.ownsBank))
               {
                  switch (direction)
                  {
                     case "W":
                        if (!world[user.row-1][user.column].userHere && 
                              (world[user.row-1][user.column].type.equals("NOTHING") || world[user.row-1][user.column].owner.equals(user.id)))
                        {
                           world[user.row-1][user.column] = new HexBlock(toPlace, level);
                           world[user.row-1][user.column].owner = user.id;
                           if (toPlace.equals("BANK"))
                              user.ownsBank = true;
                           printer.println("[>] GAINED "+level+" CHARGE!");
                           user.charge += level;
                           printer.println("[>] PLACED : "+removeInventory(toPlace, level));
                           visualizeWorld();
                        }
                        else
                           printer.println("[!] UNABLE TO BUILD - SOMETHING BLOCKING PLACEMENT");
                        break;
                     case "A":
                        if (!world[user.row][user.column-1].userHere && 
                              (world[user.row][user.column-1].type.equals("NOTHING") || world[user.row][user.column-1].owner.equals(user.id)))
                        {
                           world[user.row][user.column-1] = new HexBlock(toPlace, level);
                           world[user.row][user.column-1].owner = user.id;
                           if (toPlace.equals("BANK"))
                              user.ownsBank = true;
                           printer.println("[>] GAINED "+level+" CHARGE!");
                           user.charge += level;
                           printer.println("[>] PLACED : "+removeInventory(toPlace, level));
                           visualizeWorld();
                        }
                        else
                           printer.println("[!] UNABLE TO BUILD - SOMETHING BLOCKING PLACEMENT");
                        break;
                     case "S":
                        if (!world[user.row+1][user.column].userHere && 
                              (world[user.row+1][user.column].type.equals("NOTHING") || world[user.row+1][user.column].owner.equals(user.id)))
                        {
                           world[user.row+1][user.column] = new HexBlock(toPlace, level);
                           world[user.row+1][user.column].owner = user.id;
                           if (toPlace.equals("BANK"))
                              user.ownsBank = true;
                           printer.println("[>] GAINED "+level+" CHARGE!");
                           user.charge += level;
                           printer.println("[>] PLACED : "+removeInventory(toPlace, level));
                           visualizeWorld();
                        }
                        else
                           printer.println("[!] UNABLE TO BUILD - SOMETHING BLOCKING PLACEMENT");
                        break;
                     case "D":
                        if (!world[user.row][user.column+1].userHere && 
                              (world[user.row][user.column+1].type.equals("NOTHING") || world[user.row][user.column+1].owner.equals(user.id)))
                        {
                           world[user.row][user.column+1] = new HexBlock(toPlace, level);
                           world[user.row][user.column+1].owner = user.id;
                           if (toPlace.equals("BANK"))
                              user.ownsBank = true;
                           printer.println("[>] GAINED "+level+" CHARGE!");
                           user.charge += level;
                           printer.println("[>] PLACED : "+removeInventory(toPlace, level));
                           visualizeWorld();
                        }
                        else
                           printer.println("[!] UNABLE TO BUILD - SOMETHING BLOCKING PLACEMENT");
                        break;
                     default:
                        printer.println("[!] INVALID DIRECTION - USE [W A S D] FOR [UP LEFT DOWN RIGHT]");
                        break;
                  }
               }
               else
               {
                  printer.println("[!] YOU MAY ONLY BUILD ONE BANK - DESTROY OTHER");
               }
            }
            else
            {
               printer.println("[!] LACK LOCAGON - ASSEMBLE LOCAGON");
            }
         }
         catch (Exception e)
         {
            printer.println("[!] BAD SYNTAX - USE CMD/ FOR COMMANDS");
            println(e);
         }
      }
      
      //Removes a locagon from the user's inventory and return it.
      public InventoryItem removeInventory(String name, int lvl)
      {
         for (int i = 0; i < user.inventory.size(); i++)
         {
            if (user.inventory.get(i).name.equals(name) && user.inventory.get(i).level == lvl)
            {
               return user.inventory.remove(i);
            }
         }
         return new InventoryItem("GLITCH_ITEM_IMPOSSIBLE",-1,false);
      }
      
      //Returns true if the inventory has an item of the name.
      public boolean checkInvFor(String name)
      {
         boolean has = false;
         for (int i = 0; i < user.inventory.size(); i++)
            if (user.inventory.get(i).name.equals(name))
               return true;
         return false;
      }
      
      //Returns true if the inventory has an item of the name and level.
      public boolean checkInvFor(String name, int lvl)
      {
         boolean has = false;
         for (int i = 0; i < user.inventory.size(); i++)
            if (user.inventory.get(i).name.equals(name) && user.inventory.get(i).level == lvl)
               return true;
         return false;
      }
      
      //Checks if the ground has a bit on it and gives it to the user if there is one.
      public void checkForBits()
      {
         if (world[user.row][user.column].type.equals("BIT"))
         {
            if (world[user.row][user.column].level == 7)
            {
               printer.println("[!] ?L?EGAL AC?ION - COR?UPT B?T COL?ECT?D - Y?U AR? DO?MED");
               user.hasCorruptBit = true;
               HexBlock empty = new HexBlock("NOTHING", 1);
               empty.userHere = true;
               world[user.row][user.column] = empty;
            }
            else
            {
               int totalBits = 0;
               for (int i = 0; i < user.bits.size(); i++)
                  totalBits += user.bits.get(i).count;
               if (totalBits <= 60)
               {
                  giveBit(world[user.row][user.column].level);
                  HexBlock empty = new HexBlock("NOTHING", 1);
                  empty.userHere = true;
                  world[user.row][user.column] = empty;
               }
               else
                  printer.println("[!] UNABLE TO PICK UP BIT - YOU HAVE 60 BITS ALREADY");
            }
         }
      }
      
      //Adds a bit of a given level.
      public void giveBit(int level)
      {
         user.bits.get(level-1).count+=1;
         printer.println("[INV] ACQUIRED : BIT LV"+level);
      }
      
      //A dictionary of hacks.
      public void getHackTut(String str)
      {
         switch(str)
         {
            case "PWR_Crash":
               printer.println("[>] PWR_Crash : LV 1+");
               printer.println("[>]    Destroys whatever you are standing on.");
               printer.println("[>]    A basic program. Useful for removing things in your own base.");
               printer.println("[>] PWR is a line of destructive programs used to obliterate systems.");
               printer.println("[>] With enough PWR, entire system cities can be leveled to dust.");
               break;
            case "PWR_Wave": 
               printer.println("[>] PWR_Wave : LV 4+");
               printer.println("[>]    Destroys everything close around you and what you are standing on.");
               printer.println("[>]    Good at breaching firewalls. Use with a PIK_ program to rob a bank.");
               printer.println("[>] PWR is a line of destructive programs used to obliterate systems.");
               printer.println("[>] With enough PWR, entire system cities can be leveled to dust.");
               break;
            case "PWR_Clear": 
               printer.println("[>] PWR_Clear : LV 6");
               printer.println("[>]    Annihilates every locagon you can see with HEX/V.");
               printer.println("[>]    A truly heinous hack. Make cities vanish before your eyes.");
               printer.println("[>] PWR is a line of destructive programs used to obliterate systems.");
               printer.println("[>] With enough PWR, entire system cities can be leveled to dust.");
               break;
                           
            case "OVR_Access": 
               printer.println("[>] OVR_Access : LV 1+");
               printer.println("[>]    Hacks you onto the locagon you're standing's access list.");
               printer.println("[>]    Great for getting into ASSEMBLERs or COMPILERs that you don't own.");
               printer.println("[>] The company OVR makes programs that go around security procedures to grant you access.");
               printer.println("[>] Why bother making anything when you can just crack into other's stuff?");
               break;
            case "OVR_Capture": 
               printer.println("[>] OVR_Capture : LV 4+");
               printer.println("[>]    Gives you ownership of the locagon you're standing on.");
               printer.println("[>]    PIK_Pick open a gate and then take control of it. Your system now!");
               printer.println("[>] The company OVR makes programs that go around security procedures to grant you access.");
               printer.println("[>] Why bother making anything when you can just crack into other's stuff?");
               break;
            case "OVR_Conquer": 
               printer.println("[>] OVR_Conquer : LV 6");
               printer.println("[>]    Gives you ownership of everything you can see with HEX/V.");
               printer.println("[>]    Take control of everything! NOTE: Doesn't work on banks. Too much encryption...");
               printer.println("[>] The company OVR makes programs that go around security procedures to grant you access.");
               printer.println("[>] Why bother making anything when you can just crack into other's stuff?");
               break;
                           
            case "PIK_Steal": 
               printer.println("[>] PIK_Steal : LV 1+");
               printer.println("[>]    Empties out the bank you're standing on, regardless of owner or access.");
               printer.println("[>]    A necessary tool for any good heist. Remember, hacks only work on locagons their level or lower.");
               printer.println("[>] Be a bit more subtle with PIK programs. For the professional looter.");
               printer.println("[>] The only way to really get a fortune is through stealing, and PIK is just the tool.");
               break;
            case "PIK_Pick": 
               printer.println("[>] PIK_Pick : LV 4+");
               printer.println("[>]    Grants you ownership of every nearby gate.");
               printer.println("[>]    The best way to sneakily bypass gates. Much harder to detect than a hole in the wall.");
               printer.println("[>] Be a bit more subtle with PIK programs. For the professional looter.");
               printer.println("[>] The only way to really get a fortune is through stealing, and PIK is just the tool.");
               break;
            case "PIK_Slip": 
               printer.println("[>] PIK_Slip : LV 6");
               printer.println("[>]    Become invisible for the next 20 moves. You cannot be targeted by VIR_ programs.");
               printer.println("[>]    Vanish from sight completely with PIK_Slip. Wait for an opening, and strike from the shadows!");
               printer.println("[>] Be a bit more subtle with PIK programs. For the professional looter.");
               printer.println("[>] The only way to really get a fortune is through stealing, and PIK is just the tool.");
               break;
                           
            case "VIR_Take": 
               printer.println("[>] VIR_Take : LV 1+");
               printer.println("[>]    Pickpocket 20% of any nearby player's <#>. Crowded areas turn into bank vaults!");
               printer.println("[>]    Remember! VIR_Take and other programs only work on things their level or less.");
               printer.println("[>] VIR programs are for actual attacking of other users. You have to be brave to use these.");
               printer.println("[>] By far the most dangerous programs, VIR_Take programs put your enemies on edge.");
               break;
            case "VIR_Rob": 
               printer.println("[>] VIR_Rob : LV 4+");
               printer.println("[>]    Pickpocket 80% of any nearby player's <#>.");
               printer.println("[>]    Almost completely clear someone's pockets of precious <#>! 4x as effective as VIR_Take!");
               printer.println("[>] VIR programs are for actual attacking of other users. You have to be brave to use these.");
               printer.println("[>] By far the most dangerous programs, VIR_Take programs put your enemies on edge."); 
               break;
            case "VIR_Delete": 
               printer.println("[>] VIR_Delete : LV 6");
               printer.println("[>]    TERMINATE EVERY NEARBY PLAYER AND STEAL ALL THEIR <#>. Not for the faint of heart. ");
               printer.println("[>]    Literally destroy any player who's too close and take every <#> they have.");
               printer.println("[>] VIR programs are for actual attacking of other users. You have to be brave to use these.");
               printer.println("[>] By far the most dangerous programs, VIR_Take programs put your enemies on edge.");
               break;
               
            case "CPT_Endless": 
               printer.println("[>] CPT_Endless : LV ?");
               printer.println("[>] RUN IT. RU? IT. ??N IT. RU???T. ????IT. R?????. R??????????????????????????????????????????????????");
               printer.println("[>] CLEAR THE WORLD. ????R THE WORLD. CLEAR?????WORLD. CLEAR T??????LD. ???????????????????????????????");
               printer.println("[>] UNLEASH THE CORRUPTION. UNLEA?????E CORRUTION. UNLE??????????RRUTION. UNL??????????????????????????");
               printer.println("[>] SHOW YOUR POWER TO THEM ALL. S??????????OWER TO THEM ALL. SHO???????????????? ALL. ????????????????");
               break;
               
            default:
               printer.println("[!] HACK DOES NOT EXIST - ENTER VALID HACK");
               break;
         }
      }
      
      //Tells the user what hacks they have.
      public void getUserHacks()
      {
         printer.println("[>] {HACKS AVAILABLE}");
         for (int i = 0; i < user.hacks.size(); i++)
            printer.println("[>] "+i+" : "+user.hacks.get(i).toString());
         printer.println("[>] HEX/H/EXP/[Name]");
         printer.println("[>]    Explains what the hack [Name] does");
         printer.println("[>] HEX/H/RUN/[Index0]");
         printer.println("[>]    Runs hack number [Index0] in your hack inventory");
      }
      
      //Tells the user about the locagon they are currently on.
      public void printLocagonInfo()
      {
         printer.println("[>] >>>"+world[user.row][user.column].type+"<<<");
         printer.println("[>]   Name : "+world[user.row][user.column].personalName);
         printer.println("[>]   Owner : "+world[user.row][user.column].owner);
         printer.println("[>]   Level : "+world[user.row][user.column].level);
         printer.println("[>]   Users : "+world[user.row][user.column].access.toString());
         printer.println("[>]   Fee : "+world[user.row][user.column].fee+"<#>");
         printer.println("[>] -DESCRIPTION TEXT-");
         if (world[user.row][user.column].text.size() > 0)
            for (int i = 0; i < world[user.row][user.column].text.size(); i++)
               printer.println("[>] "+world[user.row][user.column].text.get(i));
         else
            printer.println("[!] NO CUSTOM TEXT FOUND.");
      }
      
      //Pays the fee for where the user is standing.
      public void buyFee()
      {
         printer.println("[>] Paying "+world[user.row][user.column].fee+"<#> for access to "+world[user.row][user.column].owner+"'s "+world[user.row][user.column].type);
         if (user.hexes >= world[user.row][user.column].fee)
         {
            if (!world[user.row][user.column].access.contains(user.id))
            {
               user.hexes -= world[user.row][user.column].fee;
               for (int i = 0; i < users.size(); i++)
                  if (users.get(i).id.equals(world[user.row][user.column].owner))
                     users.get(i).inBank += world[user.row][user.column].fee;
               world[user.row][user.column].access.add(user.id);
               printer.println("[>] PAID! "+user.id+" (YOU) can use "+world[user.row][user.column].owner+"'s "+world[user.row][user.column].type);
            }
            else
            {
               printer.println("[!] YOU ALREADY HAVE ACCESS TO THIS LOCAGON");
               printer.println("[>] Transaction failed.");
            }
         }
         else
         {
            printer.println("[!] YOU LACK <#> - GET MORE OR HACK LOCAGON");
            printer.println("[>] Transaction failed.");
         }
      }
      
      //Prints out compiler or assembler lists.
      public void listPrint()
      {
         if (world[user.row][user.column].type.equals("ASSEMBLER"))
         {
            printer.println("[>] <ASSEMBLER MENU> LEVEL "+world[user.row][user.column].level);
            printer.println("[>] COST TO ASSEMBLE : 2 LV "+world[user.row][user.column].level+" BITS");
            printer.println("[>] 1 : TELEPORTER LV "+world[user.row][user.column].level);
            printer.println("[>] 2 : FIREWALL LV "+world[user.row][user.column].level);
            printer.println("[>] 3 : BANK LV "+world[user.row][user.column].level);
            if (world[user.row][user.column].level < 6)
               printer.println("[>] 4 : ASSEMBLER LV "+(world[user.row][user.column].level+1));
            else
               printer.println("[>] 4 : ASSEMBLER LV "+world[user.row][user.column].level);
            printer.println("[>] 5 : COMPILER LV "+world[user.row][user.column].level);
            printer.println("[>] 6 : TRANSFER LV "+world[user.row][user.column].level);
            printer.println("[>] 7 : GATE LV "+world[user.row][user.column].level);
            printer.println("[>] 8 : DISPLAY LV "+world[user.row][user.column].level);
         }
         else
         {
            printer.println("[>] <COMPILER MENU> LEVEL "+world[user.row][user.column].level);
            printer.println("[>] COST TO COMPILE : 4 LV "+world[user.row][user.column].level+" BITS");
            printer.println("[>] LOW-POWER HACKS +");
            printer.println("[>] 1 : PWR_Crash LV"+world[user.row][user.column].level);
            printer.println("[>] 2 : OVR_Access LV"+world[user.row][user.column].level);
            printer.println("[>] 3 : PIK_Steal LV"+world[user.row][user.column].level);
            printer.println("[>] 4 : VIR_Take LV"+world[user.row][user.column].level);
            if (world[user.row][user.column].level >= 4)
            {
               printer.println("[>] MID-POWER HACKS =|=");
               printer.println("[>] 1 : PWR_Wave LV"+world[user.row][user.column].level);
               printer.println("[>] 2 : OVR_Capture LV"+world[user.row][user.column].level);
               printer.println("[>] 3 : PIK_Pick LV"+world[user.row][user.column].level);
               printer.println("[>] 4 : VIR_Rob LV"+world[user.row][user.column].level);
            }
            if (world[user.row][user.column].level >= 6)
            {
               printer.println("[>] EXTREME POWER HACKS #/!\\#");
               printer.println("[>] 1 : PWR_Clear LV"+world[user.row][user.column].level);
               printer.println("[>] 2 : OVR_Conquer LV"+world[user.row][user.column].level);
               printer.println("[>] 3 : PIK_Slip LV"+world[user.row][user.column].level);
               printer.println("[>] 4 : VIR_Delete LV"+world[user.row][user.column].level);
            }
            if (world[user.row][user.column].level >= 7)
            {
               printer.println("[>] CORRUPTION ]<[?]>[");
               printer.println("[>] 1 : CPT_ENDLESS LV 7");
            }
         }
      }
      
      //Returns if the user can pay the bits for a locagon on the space they are on, and charges them if they can.
      public boolean checkUserBits(int count)
      {
         int usableBits = 0;
         if (user.bits.get(world[user.row][user.column].level-1).count >= count)
         {
            user.bits.get(world[user.row][user.column].level-1).count -= count;
            printer.println("[>] Enough bits found!");
            return true;
         }
         else
         {
            printer.println("[!] INSUFFICIENT BITS - FIND MORE");
            return false;
         }
      }
      //Returns if the user has a certain amount of bits of a given level.
      public boolean hasBits(int count, int level)
      {
         if (user.bits.get(level-1).count >= count)
            return true;
         return false;
      }
      
      //Removes a bit of a given level. Assumes that the bit exists.
      public void removeBit(int level)
      {
         for (int i = 0; i < user.bits.size(); i++)
         {
            if (user.bits.get(i).level == level)
            {
               printer.println("[INV] LOST : "+user.bits.get(i));
               user.bits.remove(i);
               break;
            }
         }
      }
      
      //When the user runs an E/ command, looks here. >NOT DONE<
      public void localAction(String arguments)
      {
         if (arguments.equals("PAY") && !world[user.row][user.column].type.equals("NOTHING")) //If they want to buy access to what they're standing on.
            buyFee();
         else if (arguments.equals("INFO")) //If they want info on the locagon they're on.
            printLocagonInfo();
         else if (world[user.row][user.column].access.contains(user.id) 
            || world[user.row][user.column].owner.equals(user.id))
         {
            if (arguments.equals("LIST") && 
            (world[user.row][user.column].type.equals("ASSEMBLER") || world[user.row][user.column].type.equals("COMPILER"))) //If they want to see what they can make.
               listPrint();
            
            else if (arguments.contains("MAKE") && world[user.row][user.column].type.equals("ASSEMBLER")) //If they're using MAKE on an ASSEMBLER.
            {
               if (user.level >= world[user.row][user.column].level)
               {
                  int toMake = Integer.parseInt(arguments.substring(5,arguments.length()));
                  if (toMake <= 8 && toMake > 0)
                     if (checkUserBits(2)) //If they have paid for the locagon.
                     {
                        String locAdd = "";
                        switch (toMake)
                        {
                           case 1:
                              locAdd = "TELEPORTER";
                              break;
                           case 2:
                              locAdd = "FIREWALL";
                              break;
                           case 3:
                              locAdd = "BANK";
                              break;
                           case 4:
                              locAdd = "ASSEMBLER";
                              break;
                           case 5:
                              locAdd = "COMPILER";
                              break;
                           case 6:
                              locAdd = "TRANSFER";
                              break;
                           case 7:
                              locAdd = "GATE";
                              break;
                           case 8:
                              locAdd = "DISPLAY";
                              break;
                        }
                        if (locAdd.equals("ASSEMBLER") && world[user.row][user.column].level < 6)
                        {
                           user.inventory.add(new InventoryItem(locAdd, world[user.row][user.column].level+1, true));
                        }
                        else
                           user.inventory.add(new InventoryItem(locAdd, world[user.row][user.column].level, true));
                        printer.println("[INV] ACQUIRED : "+user.inventory.get(user.inventory.size()-1));
                     }
                     else{}
                  else
                  {
                     printer.println("[!] INVALID NUMBER - ENTER A VALID VALUE"); 
                  }
               }
               else
               {
                  printer.println("[!] LEVEL TOO LOW - CANNOT ASSEMBLE");
               }
            }
            
            else if (arguments.contains("COMP") && world[user.row][user.column].type.equals("COMPILER")) //If they're using COMP on a COMPILER.
            {
               if (user.level >= world[user.row][user.column].level || world[user.row][user.column].level == 7)
               {
                  int toComp = Integer.parseInt(arguments.substring(5,6));
                  int pwr = Integer.parseInt(arguments.substring(7,8));
                  if (toComp <= 4 && toComp > 0)
                  {
                     if (world[user.row][user.column].level == 7)
                     {
                        printer.println("[>] ------SAVED MESSAGE FROM HEXAGON DEV MILLER------------------------");
                        printer.println("[>] Great job! You got the X bit and now you're on the verge of");
                        printer.println("[>] destroying the Hexagon using CPT_Endless. Before you go on,");
                        printer.println("[>] I want to make sure you know what you're doing.");
                        printer.println("[>] When you make this program, a global message will be sent");
                        printer.println("[>] telling everyone in the game that you've made it. A second");
                        printer.println("[>] message will be sent when you run it (and you die).");
                        printer.println("[>] But -- who says you have to run this hack? Now, yes, it is");
                        printer.println("[>] technically my job to prevent you from running this hack.");
                        printer.println("[>] This is a good idea though! Hold EVERYONE for ransom. Demand");
                        printer.println("[>] a tribute of maybe 200<#> every 10 minutes and you'll own the majority");
                        printer.println("[>] of the world's wealth in half an hour. It's the having, not the");
                        printer.println("[>] use of the program that makes it so powerful. But if you do");
                        printer.println("[>] want to destroy the world, go ahead. SHOW. THEM. ALL.");
                        user.hacks.add(new InventoryItem("CPT_Endless", 7, false));
                        user.hasCorruptBit = false;
                        printer.println("[HACK] ACQUIRED : CPT_Endless - THE WORLD BOWS BEFORE YOU");
                     }
                     if ((world[user.row][user.column].level >= 1 && pwr <= 1) || (world[user.row][user.column].level >= 4 && pwr <= 2) || 
                     (world[user.row][user.column].level >= 6 && pwr <= 3))
                        if (checkUserBits(4)) //If they have paid for the hack.
                        {
                           String hackAdd = getHackFor(toComp, world[user.row][user.column].level, pwr);
                           user.hacks.add(new InventoryItem(hackAdd, world[user.row][user.column].level, false));
                           printer.println("[HACK] ACQUIRED : "+user.hacks.get(user.hacks.size()-1));
                        }
                        else
                        {
                           printer.println("[!] INSUFFICIENT BITS - FIND MORE");
                        }
                     else
                     {
                        printer.println("[!] LV/POWER MISMATCH - HIGHER LEVEL COMPILER NEEDED");
                     }
                  }
                  else
                  {
                     printer.println("[!] INVALID NUMBER - ENTER A VALID VALUE"); 
                  }
               }
               else
               {
                  printer.println("[!] LEVEL TOO LOW - CANNOT COMPILE");
               }
            }
            
            else if (arguments.contains("TP") && world[user.row][user.column].type.equals("TELEPORTER")) //TELEPORTING: NOT DONE
            {
               int teleportersFound = 0;
               if (arguments.equals("TP"))
               {
                  printer.println("[>] AVAILABLE TELEPORTERS");
                  for (int i = 0; i < world.length; i++)
                  {
                     for (int j = 0; j < world[0].length; j++)
                     {
                        if (world[i][j].type.equals("TELEPORTER") && world[i][j].owner.equals(world[user.row][user.column].owner))
                        {
                           teleportersFound++;
                           printer.println("[>] "+teleportersFound+" : "+world[i][j].personalName+", X"+i+", Y"+j);
                        }
                     }
                  }
                  if (teleportersFound > 0)
                  {
                     printer.println("[>] HEX/E/TP/[ListNum]");
                     printer.println("[>]    Teleports you to teleporter number [ListNum]");
                  }
                  else
                     printer.println("[!] NO ENDPOINTS - THERE ARE NO TELEPORTERS CONNECTED");
               }
               else
               {
                  int toWarp = Integer.parseInt(arguments.substring(3, arguments.length()));
                  for (int i = 0; i < world.length; i++)
                  {
                     for (int j = 0; j < world.length; j++)
                     {
                        if (world[i][j].type.equals("TELEPORTER") && world[i][j].owner.equals(world[user.row][user.column].owner))
                        {
                           teleportersFound++;
                           if (teleportersFound == toWarp)
                           {
                              if (!world[i][j].userHere)
                              {
                                 printer.println("[>] ~Warping!~");
                                 world[user.row][user.column].userHere = false;
                                 user.row = i;
                                 user.column = j;
                                 visualizeWorld();
                                 printer.println("[>] Welcome to "+world[user.row][user.column].personalName+".");
                                 printer.println("[>] Step off the teleporter to reappear.");
                              }
                              else
                                 printer.println("[!] TELEPORTER BLOCKED - TARGET TELEPORTER OCCUPIED");
                           }
                        }
                     }
                  }
                  if (teleportersFound < toWarp || toWarp < 0)
                  {
                     printer.println("[!] NO TELEPORTER - THAT TELEPORTER NUMBER DOES NOT EXIST");
                  }
               }
            }
            
            else if (arguments.equals("GETALL") && world[user.row][user.column].type.equals("BANK")) //Bank cashout program
            {
               boolean success = false;
               for (int i = 0; i < users.size(); i++)
               {
                  if (users.get(i).id.equals(world[user.row][user.column].owner))
                  {
                     printer.println("[>] "+users.get(i).inBank+" <#> CASHED OUT TO "+world[user.row][user.column].owner);
                     users.get(i).hexes += users.get(i).inBank;
                     users.get(i).inBank = 0;
                     success = true;
                     i += users.size();
                  }
               }
               if (!success)
                  printer.println("[!] BANK OWNER DOES NOT EXIST");
            }
            
            else if (arguments.contains("EDIT") && !world[user.row][user.column].type.equals("NOTHING")) //HEX/E/EDIT function, used to edit one's own locagons
            {
               if (world[user.row][user.column].owner.equals(user.id))
               {
                  if (arguments.equals("EDIT"))
                     printEditCmds();
                  else
                  {
                     String editCut = arguments.substring(5, arguments.length());
                     String baseCmd = "";
                     String extra = "";
                     if (editCut.indexOf("/") > 0)
                     {
                        baseCmd = editCut.substring(0, editCut.indexOf("/"));
                        extra = editCut.substring(editCut.indexOf("/")+1, editCut.length());
                     }
                     else
                        baseCmd = editCut;
                     switch (baseCmd)
                     {
                        case "ACCESS":
                           if (extra.length() == 6)
                           {
                              world[user.row][user.column].access.add(extra);
                              printer.println("[>] Added user : "+extra);
                           }
                           else
                              printer.println("[!] INVALID ID");
                           break;
                           
                        case "ACCESSCLR":
                           world[user.row][user.column].access = new ArrayList<String>();
                           printer.println("[>] Access cleared");
                           break;
                           
                        case "FEE":
                           if (Integer.parseInt(extra) >= 0)
                           {
                              world[user.row][user.column].fee = Integer.parseInt(extra);
                              printer.println("[>] Fee is now : "+Integer.parseInt(extra)+"<#>");
                           }
                           else
                              printer.println("[!] FAILED - FEE MUST BE AT LEAST 0");
                           break;
                           
                        case "TEXT":
                           if ((world[user.row][user.column].text.size() < world[user.row][user.column].level)
                              || (world[user.row][user.column].type.equals("DISPLAY") && world[user.row][user.column].text.size() < world[user.row][user.column].level*3))
                           {
                              world[user.row][user.column].text.add(extra);
                              printer.println("[>] Wrote to text : "+extra);
                           }
                           else
                              printer.println("[!] CANNOT WRITE MORE TEXT - FULL");
                           break;
                           
                        case "TEXTCLR":
                           world[user.row][user.column].text = new ArrayList<String>();
                           printer.println("[>] Text cleared.");
                           break;
                           
                        case "NAME":
                           world[user.row][user.column].personalName = extra;
                           printer.println("[>] Name set to : "+world[user.row][user.column].personalName);
                           break;
                           
                        case "DEL":
                           if (world[user.row][user.column].type.equals("BANK"))
                              user.ownsBank = false;
                           world[user.row][user.column] = new HexBlock("NOTHING",1);
                           printer.println("[>] The locagaon has been deleted.");
                           
                           break;
                           
                        default:
                           printer.println("[!] UNEDITABLE - USE HEX/E/EDIT TO SEE EDITABLES");
                           break;
                     }
                  }
               }
               else
                  printer.println("[!] OWNER ONLY ["+world[user.row][user.column].owner+"] - YOU ARE NOT OWNER");
            }
            
            else if (arguments.contains("EXCH") && world[user.row][user.column].type.equals("TRANSFER")) //Used to buy/sell hexes
            { 
               String cutExch = arguments.substring(5, arguments.length());
               String selling = cutExch.substring(0,1);
               int level = Integer.parseInt(cutExch.substring(2,3));
               int count = Integer.parseInt(cutExch.substring(4, cutExch.length()));
               int gained = 0;
               if (count > 0 && level >= 1 && level <= 6)
               {
                  if (selling.equals("B"))
                  { 
                     if (hasBits(count, level) && count > 0)
                     {
                        gained = count*(int)(Math.pow(2,level));
                        printer.println("[>] SOLD : "+count+" LV "+level+" BITS");
                        printer.println("[>] GAINED : "+gained+"<#>");
                        user.bits.get(level-1).count -= count;
                        user.hexes += gained;
                     }
                     else
                        printer.println("[!] FAILED - YOU DO NOT HAVE "+count+" LV "+level+" BITS");
                  }
                  else if (selling.equals("H"))
                  {  
                     if (user.hexes >= count)
                     {
                        if (count % ((int)Math.pow(2,level)) == 0)
                        {
                           gained = count/(int)Math.pow(2,level);
                           user.bits.get(level-1).count += gained;
                           user.hexes -= count;
                           printer.println("[>] SOLD : "+count+"<#>");
                           printer.println("[>] GAINED : "+gained+" LV "+level+" BITS");
                        }
                        else
                        {
                           printer.println("[!] THE <#> YOU ARE TRYING TO SELL IS INVALID");
                           printer.println("[!] FOR LEVEL "+level+" BITS, SELL A MULTIPLE OF "+Math.pow(2,level)+"<#>");
                        }
                     }
                     else
                        printer.println("[!] FAILED - YOU DO NOT HAVE "+count+"<#>");
                  }
                  else
                     printer.println("[!] INVALID SELL TYPE ["+selling+"] - B: BIT, H: <#>(HEXES)");
               }
               else
                  printer.println("[!] NUMBER INVALID - ENTER POSITIVE NUMBERS AND LV 1-6");
            }
            else
               printer.println("[!] BAD COMBINATION - COMMAND OR LOCAGON WRONG");
         }
         else
            printer.println("[!] ACCESS DENIED - GAIN ACCESS TO THIS LOCAGON");
      }
      
      //Prints out the HEX/E/EDIT commands.
      public void printEditCmds()
      {
         printer.println("[>] <EDIT COMMANDS>");
         printer.println("[>] HEX/E/EDIT/ACCESS/[HEXid]");
         printer.println("[>]    Adds [HEXid] to the access list");
         printer.println("[>] HEX/E/EDIT/ACCESSCLR");
         printer.println("[>]    Clears the access list");
         printer.println("[>] HEX/E/EDIT/FEE/[Cost]");
         printer.println("[>]    Sets the <#> fee to [Cost]");
         printer.println("[>] HEX/E/EDIT/TEXT/[NewLine]");
         printer.println("[>]    Adds a new line of text to this locagon, up to this locagon's level lines");
         printer.println("[>]    NOTE : Displays can have their level times 4 lines");
         printer.println("[>] HEX/E/EDIT/TEXTCLR");
         printer.println("[>]    Clears the text display");
         printer.println("[>] HEX/E/EDIT/NAME/[NewName]");
         printer.println("[>]    Names the locagon [NewName]");
         printer.println("[>] HEX/E/EDIT/DEL");
         printer.println("[>]    Permanently deletes this locagon. No bits or locagons are returned.");
      }
      
      //Returns the hack made based on the school of hack as a number and the level
      public String getHackFor(int sch, int lvl, int pwr)
      {
         String back = "GLITCH_IMPOSSIBLE";
         switch (sch)
         {
            case 1:
               switch (pwr)
               {
                  case 1:
                     back = "PWR_Crash";
                     break;
                  case 2:
                     back = "PWR_Wave";
                     break;
                  case 3:
                     back = "PWR_Clear";
                     break;
               }
               break;
            
            case 2:
               switch (pwr)
               {
                  case 1:
                     back = "OVR_Access";
                     break;
                  case 2:
                     back = "OVR_Capture";
                     break;
                  case 3:
                     back = "OVR_Conquer";
                     break;
               }
               break;
            
            case 3:
               switch (pwr)
               {
                  case 1:
                     back = "PIK_Steal";
                     break;
                  case 2:
                     back = "PIK_Pick";
                     break;
                  case 3:
                     back = "PIK_Slip";
                     break;
               }
               break;
            
            case 4:
               switch (pwr)
               {
                  case 1:
                     back = "VIR_Take";
                     break;
                  case 2:
                     back = "VIR_Rob";
                     break;
                  case 3:
                     back = "VIR_Delete";
                     break;
               }
               break;
            case 5:
               back = "CPT_Endless";
               break;
         }
         
         return back;
      } 
      
      //Prints out a 7x7 area centered on the user so they know where they are.
      public void visualizeWorld()
      {
         printer.println("[>] <WORLD>");
         for (int i = user.row - 5; i <= user.row + 5; i++)
         {
            printer.print("[>] ");
            for (int j = user.column - 5; j <= user.column + 5; j++)
               try
               {
                  printer.print(world[i][j].getDisplay());
               }
               catch (Exception e){}
            printer.println();
         }
         printer.println();
      }
      
      //Tells the user what they can do where they are.
      public void getLocationCommands()
      {
         HexBlock on = world[user.row][user.column];
         switch (on.type)
         {
            case "TELEPORTER":
               printer.println("[>] TELEPORTER %");
               printer.println("[>] HEX/E/INFO");
               printer.println("[>]    Tells you about this TELEPORTER");
               printer.println("[>] HEX/E/PAY");
               printer.println("[>]    Buys access to this TELEPORTER");
               printer.println("[>] HEX/E/TP");
               printer.println("[>]    Tells you where you can teleport");
               printer.println("[>] HEX/E/EDIT");
               printer.println("[>]    Allows the owner to edit this TELEPORTER");
               break;
            case "BANK":
               printer.println("[>] BANK $");
               printer.println("[>] HEX/E/INFO");
               printer.println("[>]    Tells you about this BANK");
               printer.println("[>] HEX/E/GETALL");
               printer.println("[>]    Sends all the <#> in here to the owner's account");
               printer.println("[>] HEX/E/EDIT");
               printer.println("[>]    Allows the owner to edit this BANK");
               break;
            case "ASSEMBLER":
               printer.println("[>] ASSEMBLER +");
               printer.println("[>] HEX/E/INFO");
               printer.println("[>]    Tells you about this ASSEMBLER");
               printer.println("[>] HEX/E/PAY");
               printer.println("[>]    Buys access to this ASSEMBLER");
               printer.println("[>] HEX/E/LIST");
               printer.println("[>]    Tells you what you can make");
               printer.println("[>] HEX/E/MAKE/[ListNum]");
               printer.println("[>]    Makes list item number [ListNum] and uses the bits");
               printer.println("[>] HEX/E/EDIT");
               printer.println("[>]    Allows the owner to edit this ASSEMBLER");
               break;
            case "COMPILER":
               printer.println("[>] COMPILER =");
               printer.println("[>] HEX/E/INFO");
               printer.println("[>]    Tells you about this COMPILER");
               printer.println("[>] HEX/E/PAY");
               printer.println("[>]    Buys access to this COMPILER");
               printer.println("[>] HEX/E/LIST");
               printer.println("[>]    Tells you what you can make");
               printer.println("[>] HEX/E/COMP/[ListNum]/[Power]");
               printer.println("[>]    Compiles list item number [ListNum] and uses the bits");
               printer.println("[>]    Use [Power] to specify from which set it comes from :");
               printer.println("[>]    1 : LOW, 2 : MID, 3 : EXTREME");
               printer.println("[>] HEX/E/EDIT");
               printer.println("[>]    Allows the owner to edit this COMPILER");
               break;
            case "TRANSFER":
               printer.println("[>] TRANSFER &");
               printer.println("[>] HEX/E/INFO");
               printer.println("[>]    Tells you about this TRANSFER");
               printer.println("[>] HEX/E/PAY");
               printer.println("[>]    Buys access to this TRANSFER");
               printer.println("[>] HEX/E/EXCH/B/[Level]/[Count]");
               printer.println("[>]    Turns in [Count] level [Level] bits for <#>");
               printer.println("[>] HEX/E/EXCH/H/[Level]/[Count]");
               printer.println("[>]    Turns in [Count] <#> for level [Level] bits");
               printer.println("[>] HEX/E/EDIT");
               printer.println("[>]    Allows the owner to edit this TRANSFER");
               break;
            case "GATE":
               printer.println("[>] GATE ~");
               printer.println("[>] HEX/E/INFO");
               printer.println("[>]    Tells you about this GATE");
               printer.println("[>] HEX/E/EDIT");
               printer.println("[>]    Allows the owner to edit this GATE");
               break;
            case "DISPLAY":
               printer.println("[>] DISPLAY !");
               printer.println("[>] HEX/E/INFO");
               printer.println("[>]    Tells you about this DISPLAY");
               printer.println("[>] HEX/E/EDIT");
               printer.println("[>]    Allows the owner to edit this DISPLAY");
               break;
            default:
               printer.println("[!] NO LOCAGON - STAND ON A LOCAGON");
               break;
         }
      }
      
      //The Hexagon tutorial.
      public void findHexTutorial(int page)
      {
         switch (page)
         {
            case 1:
               printer.println("[>] <<<<TUTORIAL PAGE 1>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
               printer.println("[>] Now are you in Hexagon proper -- the actual world  ");
               printer.println("[>] free for you to explore, build in, and plunder for ");
               printer.println("[>] <#>. First, for the basics. You can build things in");
               printer.println("[>] the Hexagon called locagons (loh-cah-gonz). They do");
               printer.println("[>] everything you might need. There are also hacks    ");
               printer.println("[>] that you can use for all sorts of sneaky stuff,    ");
               printer.println("[>] like robbing banks, breaking in walls, and stealing");
               printer.println("[>] access to locagons. Go to the next page!           ");
               printer.println("[>] NEXT PAGE: PAGE 2");
               break;
            case 2:
               printer.println("[>] <<<<TUTORIAL PAGE 2>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
               printer.println("[>] Use the HEX/V command to visualize the area around ");
               printer.println("[>] you. Everything is made of text here. What does it ");
               printer.println("[>] mean?... Type HEX/C to see what each character     ");
               printer.println("[>] means. You might notice that players are @ symbols,");
               printer.println("[>] but you can't see yourself using HEX/V. That's     ");
               printer.println("[>] intentional! You're invisible when you join so     ");
               printer.println("[>] players can't hack you. To turn visible, move in a ");
               printer.println("[>] direction. Go to the next page to learn how!       ");
               printer.println("[>] NEXT PAGE: PAGE 3");
               break;
            case 3:
               printer.println("[>] <<<<TUTORIAL PAGE 3>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
               printer.println("[>] Use HEX/[Direction] to move around. Here's an image");
               printer.println("[>] to show you what W, A, S and D do. As the image    ");
               printer.println("[>] #---# shows, W moves you UP, A is LEFT, S is DOWN, ");
               printer.println("[>] | W | and D is RIGHT. Type HEX/W,A,S or D to move  ");
               printer.println("[>] |A+D| that way by one block. After you move, you'll");
               printer.println("[>] | S | be shown your new location. Try moving around");
               printer.println("[>] #---# a bit, if you can. If you started the game   ");
               printer.println("[>] in someone else's base, maybe you should rejoin... ");
               printer.println("[>] NEXT PAGE: PAGE 4");
               break;
            case 4:
               printer.println("[>] <<<<TUTORIAL PAGE 4>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
               printer.println("[>] Now, bits! Bits are the letters from A to F you can");
               printer.println("[>] see scattered around. Just move on top of one and  ");
               printer.println("[>] you'll pick it up! Bits can be turned into <#> or  ");
               printer.println("[>] used to make locagons or hacks. For example, check ");
               printer.println("[>] your inventory with HEX/L. You've been given a free");
               printer.println("[>] assembler! Place it on the ground using the HEX/B  ");
               printer.println("[>] command. It has a lot of [brackets], so be careful ");
               printer.println("[>] when you type it in.                               ");
               printer.println("[>] NEXT PAGE: PAGE 5");
               break;
            case 5:
               printer.println("[>] <<<<TUTORIAL PAGE 5>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
               printer.println("[>] Once you've placed your ASSEMBLER, use HEX/V to see");
               printer.println("[>] it on the ground. It's a + sign! That's your very  ");
               printer.println("[>] own ASSEMBLER which you can use to make other      ");
               printer.println("[>] locagons. Walk on top of it and use HEX/I to see   ");
               printer.println("[>] what commands you can use to interface with it. For");
               printer.println("[>] now, we're interested in HEX/E/LIST, which will    ");
               printer.println("[>] tell you what you can make. Go collect some bits   ");
               printer.println("[>] and go back to it!");
               printer.println("[>] NEXT PAGE: PAGE 6");
               break;
            case 6:
               printer.println("[>] <<<<TUTORIAL PAGE 6>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
               printer.println("[>] Once you're back on your ASSEMBLER, give HEX/E/LIST");
               printer.println("[>] a run again. There're a lot of locagons! Let's go  ");
               printer.println("[>] through each.                                      ");
               printer.println("[>]                                                    ");
               printer.println("[>] ASSEMBLER                                          ");
               printer.println("[>] Used to make locagons of its level, or another     ");
               printer.println("[>] ASSEMBLER of one level higher. Very important!     ");
               printer.println("[>]                                                    ");
               printer.println("[>] NEXT PAGE: PAGE 7");
               break;
            case 7:
               printer.println("[>] <<<<TUTORIAL PAGE 7>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
               printer.println("[>] COMPILER                                           ");
               printer.println("[>] Used to make hacks, which you can use to steal,    ");
               printer.println("[>] control other people's locagons, and more!         ");
               printer.println("[>]                                                    ");
               printer.println("[>] BANK                                               ");
               printer.println("[>] If you set a fee to access one of your locagons,   ");
               printer.println("[>] the fee <#> goes here for you to collect!          ");
               printer.println("[>]                                                    ");
               printer.println("[>] NEXT PAGE: PAGE 8");
               break;
            case 8:
               printer.println("[>] <<<<TUTORIAL PAGE 8>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
               printer.println("[>] FIREWALL                                           ");
               printer.println("[>] Impassible by anyone. Protect your bank and        ");
               printer.println("[>] yourself with FIREWALLs!                           ");
               printer.println("[>]                                                    ");
               printer.println("[>] TELEPORTER                                         ");
               printer.println("[>] You can teleport between every teleporter you own  ");
               printer.println("[>] for free! Charge <#> to others for access.         ");
               printer.println("[>]                                                    ");
               printer.println("[>] NEXT PAGE: PAGE 9");
               break;
            case 9:
               printer.println("[>] <<<<TUTORIAL PAGE 9>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
               printer.println("[>] TRANSFER                                           ");
               printer.println("[>] Used to turn <#> into bits and bits into <#>. Be   ");
               printer.println("[>] sure to make one so you can start making <#>!      ");
               printer.println("[>]                                                    ");
               printer.println("[>] GATE                                               ");
               printer.println("[>] Like a FIREWALL, but some people can pass through  ");
               printer.println("[>] it if they have access. A good front door!         ");
               printer.println("[>]                                                    ");
               printer.println("[>] NEXT PAGE: PAGE 10");
               break;
            case 10:
               printer.println("[>] <<<<TUTORIAL PAGE 10>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
               printer.println("[>] DISPLAY                                            ");
               printer.println("[>] No function aside to store a lot of text. Can have ");
               printer.println("[>] 4x more text than other locagons.");
               printer.println("[>]                                                    ");
               printer.println("[>] USER                                               ");
               printer.println("[>] Good or evil? Trusted or traitor? This is another  ");
               printer.println("[>] user, just like you, who might want your <#>!      ");
               printer.println("[>]                                                    ");
               printer.println("[>] NEXT PAGE: PAGE 11");
               break;
            case 11:
               printer.println("[>] <<<<TUTORIAL PAGE 11>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
               printer.println("[>] That's every locagon! Now, you should collect bits.");
               printer.println("[>] Find a whole lot of them! Keep in mind that you can");
               printer.println("[>] only use level 1 bits (A) on level 1 ASSEMBLERs.   ");
               printer.println("[>] If you have a bit you can't use, sell it at a      ");
               printer.println("[>] TRANSFER for <#> and buy other bits. Once you have ");
               printer.println("[>] a nice base, compile some hacks and hit the road!  ");
               printer.println("[>] What's theirs is yours! Read the last page to learn");
               printer.println("[>] about levels. Then, you'll be ready!               ");
               printer.println("[>] NEXT PAGE: PAGE 12");
               break;
            case 12:
               printer.println("[>] <<<<TUTORIAL PAGE 12>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
               printer.println("[>] Everything has a level. You start at level 1 and,  ");
               printer.println("[>] to level up, you need to make locagons and compile ");
               printer.println("[>] hacks. Every time you make a locagon or hack, you  ");
               printer.println("[>] gain charge. Check how much charge you need to gain");
               printer.println("[>] a level with ACT/. You can only use assemblers and ");
               printer.println("[>] compilers of your level or less, so make lots of   ");
               printer.println("[>] things to make stronger stuff. Higher level things ");
               printer.println("[>] are more resistant to hacks and better overall.    ");
               printer.println("[>] NEXT PAGE: PAGE 13");
               break;
            case 13:
               printer.println("[>] <<<<TUTORIAL PAGE 13>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
               printer.println("[>] Every hack is only effective against things of its ");
               printer.println("[>] level or less. A PWR_Wave LV 5 won't be able to do ");
               printer.println("[>] anything to a LV 6 FIREWALL. The same goes for     ");
               printer.println("[>] players -- a VIR_Rob won't be able to take any <#> ");
               printer.println("[>] from a player of a higher level. Now, go and       ");
               printer.println("[>] conquer the Hexagon! You can decide how you want to");
               printer.println("[>] play! Build a city! Make a gang! Mess around with  ");
               printer.println("[>] other people! For ideas as to what to do, read on. ");
               printer.println("[>] NEXT PAGE: PAGE 14");
               break;
            case 14:
               printer.println("[>] <<<<IDEAS>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
               printer.println("[>] Don't know what to do? We have some ideas for you. ");
               printer.println("[>] IDEA 1: MAKE A SMALL BASE!                         ");
               printer.println("[>]    Go to your ASSEMBLER and run HEX/E/LIST to see  ");
               printer.println("[>] the things that you can make. Get some bits and put");
               printer.println("[>] together a bunch of locagons. FIREWALLs, a GATE,   ");
               printer.println("[>] maybe a TRANSFER, COMPILER, another ASSEMBLER of a ");
               printer.println("[>] higher level, and other stuff. Then, use the build ");
               printer.println("[>] command to make your own base of operations.       ");
               printer.println("[>] NEXT IDEA: PAGE 15");
               break;
            case 15:
               printer.println("[>] <<<<IDEAS>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
               printer.println("[>] IDEA 2: BECOME A FEARED HACKER!                    ");
               printer.println("[>]    Do you think finding bits sucks? Then this is   ");
               printer.println("[>] perfect for you! Compile some hacks and hit the    ");
               printer.println("[>] road! If you see a BANK, break into that sucker!   ");
               printer.println("[>] Use a PIK_Steal of high enough level and rob it of ");
               printer.println("[>] whatever it contains. If you see a player, use a   ");
               printer.println("[>] VIR_ hack and take their <#> out of their pockets! ");
               printer.println("[>] Steal entire bases! It's all yours now!            ");
               printer.println("[>] NEXT IDEA: PAGE 16");
               break;
            case 16:
               printer.println("[>] <<<<IDEAS>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
               printer.println("[>] IDEA 3: MAKE A GREAT WALL!                         ");
               printer.println("[>]    Build a giant wall that fences in a huge area,  ");
               printer.println("[>] making an inside and an outside. Set up several    ");
               printer.println("[>] teleporters on either side and charge a fee for    ");
               printer.println("[>] access to the teleporter. Build a bank, protect it,");
               printer.println("[>] and wait for people to pay for the teleporter.     ");
               printer.println("[>] Or, instead, claim the land inside and make people ");
               printer.println("[>] pay to buy the land!");
               printer.println("[>] NEXT IDEA: PAGE 17");
               break;
            case 17:
               printer.println("[>] <<<<IDEAS>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
               printer.println("[>] IDEA 4: ESTABLISH ORDER!                           ");
               printer.println("[>]    Get some friends for this one. Establish a well-");
               printer.println("[>] protected city, with outer walls, a storage area,  ");
               printer.println("[>] safehouses, and locagons. Offer anyone who moves in");
               printer.println("[>] protection as long as they pay a rent. Then, use   ");
               printer.println("[>] VIR_Delete to exterminate lawbreakers and raiders! ");
               printer.println("[>] How big can you make your city? Raise an army of   ");
               printer.println("[>] citizens and patrol the land!                      ");
               printer.println("[>] NEXT IDEA: PAGE 18");
               break;
            case 18:
               printer.println("[>] <<<<IDEAS>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
               printer.println("[>] IDEA 5: COLLECT RANSOM!                            ");
               printer.println("[>]    Find someone who isn't paying attention and make");
               printer.println("[>] a wall around their house quickly and put a single ");
               printer.println("[>] GATE. Then, when they realise they can't escape,   ");
               printer.println("[>] make them pay their own ransom! Then, keep the wall");
               printer.println("[>] there and make them pay every time they leave or   ");
               printer.println("[>] enter by adding a fee to some TELEPORTERs and then ");
               printer.println("[>] clear the access list often. It's brilliant!       ");
               printer.println("[>] NEXT IDEA: PAGE 19");
               break;
            case 19:
               printer.println("[>] <<<<IDEAS>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
               printer.println("[>] IDEA 6: MAKE A TELEPORTER NETWORK!                 ");
               printer.println("[>]    Create TELEPORTERs in important places, like a  ");
               printer.println("[>] city, and then charge people for the TELEPORTER's  ");
               printer.println("[>] use. Reset the access list every 30 minutes to make");
               printer.println("[>] them 'subscription-based' and rake in the <#> as   ");
               printer.println("[>] users teleport around to get places faster. For an ");
               printer.println("[>] evil twist, make a teleporter lead to a sealed box ");
               printer.println("[>] and trap people in there! Muahaha!                 ");
               printer.println("[>] LAST IDEA: PAGE 20");
               break;
            case 20:
               printer.println("[>] <<<<IDEAS>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
               printer.println("[>] IDEA 7: BE A INFAMOUS HACKER!                      ");
               printer.println("[>]    First, get to level 6 and make tons and tons of ");
               printer.println("[>] hacks. Then, think of an identifying catchphrase or");
               printer.println("[>] symbol. Use EXTREME POWER hacks to level cities and");
               printer.println("[>] complete massive heists from BANKs! Then, leave a  ");
               printer.println("[>] display with your catchphrase on it so people know ");
               printer.println("[>] when 'Mr.Doom' or whatever your cheesy hacker name ");
               printer.println("[>] is has hacked their base and stolen their <#>!     ");
               break;
            default:
               printer.println("[!] Invalid page number.");
               break;
         }
      }
      
      //What each symbol means.
      public void printCharSet()
      {
         printer.println("[>] <CHARSET>>>>>");
         printer.println("[>] EMPTY       .");
         printer.println("[>] BIT(A-F)    A");
         printer.println("[>] ASSEMBLER   +");
         printer.println("[>] COMPILER    =");
         printer.println("[>] BANK        $");
         printer.println("[>] FIREWALL    #");
         printer.println("[>] TELEPORTER  %");
         printer.println("[>] TRANSFER    &");
         printer.println("[>] GATE        ~");
         printer.println("[>] DISPLAY     !");
         printer.println("[>] USER        @");
      }
      
      //Ends this player's game, i.e. kicks them
      public void kickPlayer(String reason) throws InterruptedException
      {
         printer.println("!--------------------!");
         printer.println("|YOU HAVE BEEN KICKED|");
         printer.println("!--------------------!");
         printer.println("REASON:");
         printer.println(reason);
         printer.println("CEASE THIS ACTIVITY IMMEDIATELY");
         printer.println("YOU WILL BE BANNED IF YOU CONTINUE");
         Thread.sleep(1000000000);
      }
      //Bans this IP forever
      public void banPlayer(String reason) throws InterruptedException
      {
         printer.println("#--------------------#");
         printer.println("|YOU HAVE BEEN BANNED|");
         printer.println("#--------------------#");
         printer.println("REASON:");
         printer.println(reason);
         printer.println("THIS PC IS NOW BANNED FROM THE HEXAGON");
         printer.println("YOU MAY GET ANOTHER PC OR WAIT FOR THE NEXT SERVER");
         Thread.sleep(1000000000);
         //Add user's IP to banlist
      }
   }
   
   
   public static void print(Object obj)
   {
      System.out.print(obj.toString());
   }
   public static void println(Object obj)
   {
      System.out.println(obj.toString());
   }
}