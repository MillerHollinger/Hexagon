import java.util.*;
public class HexBlock
{
   public String type; // what it is
   public String personalName; // a personal name given to this by the owner to identify it
   public int level; // power
   public String owner; // who owns it
   public ArrayList<String> access; // who can use it
   public ArrayList<String> text; // text it displays, each item is a new line.
   public int fee; // how many <#> it costs to use, money sent to owner's bank
   public boolean userHere; // if a player is standing here
   
   public HexBlock()
   {
      randomGenerate();
   } //randomly generate this
   public HexBlock(String t, int l) //specific choice
   {
      type = t;
      personalName = type+" LV "+l;
      level = l;
      owner = "";
      access = new ArrayList<String>();
      text = new ArrayList<String>();
      fee = 0;
      userHere = false;
   } 
   
   public void randomGenerate()
   {
      type = "NOTHING";
      personalName = "";
      level = 1;
      owner = "World - Unowned";
      access = new ArrayList<String>();
      access.add(owner);
      text = new ArrayList<String>();
      fee = 0;
      if (new Random().nextInt(2400) == 0) // 1/2400 chance of being a bank
      {
         type = "BANK";
         level = new Random().nextInt(6)+1;
         owner = "World - Unowned";
         text.add("---TREASURE!--------------------------------------------");
         text.add("You've found yourself a random treasure bank! Good job!");
         text.add("Use a hack on this bank! Tons of treasure waits inside..");
         text.add("--------------------------------------------------------");
      }
      if (new Random().nextInt(4) == 0) // 1/4 chance of being a bit
      {
         type = "BIT";
         level = 1;
         owner = "World - Unowned";
         while (new Random().nextInt(3) == 0 && level < 6) // 1/6 chance to level up
            level++;
      }
   }
  
   public String getDisplay()
   {
      if (!userHere)
      {
         switch (type)
         {
            case "NOTHING":
               return ".";
            case "BIT":
               switch (level)
               {
                  case 1:
                     return "A";
                  case 2:
                     return "B";
                  case 3:
                     return "C";
                  case 4:
                     return "D";
                  case 5:
                     return "E";
                  case 6:
                     return "F";
                  case 7:
                     return "X";
               }
               break;
            case "TELEPORTER":
               return "%";
            case "FIREWALL":
               return "#";
            case "BANK":
               return "$";
            case "ASSEMBLER":
               return "+";
            case "COMPILER":
               return "=";
            case "TRANSFER":
               return "&";
            case "GATE":
               return "~";
            case "DISPLAY":
               return "!";
            case "CORRUPTION":
               return "?";
         }
      
         return ".";
      }
      else
         return "@";
   }
   /* Nothing/Bit/Teleporter...Display
   Block types:
         1. . Nothing
               Can be walked on or have a Locagon placed on it.
         2. A-F Bit
               Used to make locagons or programs.
                  Locagons are structures that have an owner, a level, and a function.
                  They also have a use fee and access rights.
                     The use fee is sent to the user's bank.
                     The access rights are a list of IDs which can use it.
               Programs are used to destroy stuff, edit data, or steal things.
                  HEX/HACK/OWNED : Displays programs the user has.
                  HEX/HACK/TARGETS : Gives a list of potential program targets, i.e. adjacent or diagonal to the player.
                  HEX/HACK/[ListNum]/[HackName]/[Level] : Uses the program [HackName] level [Level] on item in HEX/HACK/TARGETS number [ListNum], consuming the program.
         3. Locagon.
               %Telporter : Moves players.
                  HEX/E : Display list of other teleporters, their locations, and the cost to warp.
                  HEX/E/WARP/[ListNum] : Pays the price to teleport, puts the money in the owner's bank, and warps the player to teleporter number [ListNum].
               #Firewall : Impassible. No interactions.
               $Bank : One per player. Stores money earned by Teleporters, Assemblers, Compilers, and Gates.
                  HEX/E : Display stored money and owner.
                  HEX/E/TAKE : Empties out the <#> to the owner.
               +Assembler : Makes locagons of its level.
                  HEX/E : Display what can be made and price in bits.
                  HEX/E/MAKE/[ListNum] : Pays the price to assemble number [ListNum] and adds the item to the player's inventory.
               =Compiler : Makes assault programs
                  HEX/E : Display list of what can be compiled.
                  HEX/E/COMP/[ListNum] : Pays the price to compile number [ListNum] and adds the program to the player's inventory.
               &Transfer : Exchanges bits for <#> and the other way around
                  HEX/E : Displays the exchange rate
                  HEX/E/Buy/[Level]/[Count] : Buys [Count] level [Level] bits in exchange for <#>
                  HEX/E/Sell/[Level]/[Count] : Sells [Count] level [Level] bits for <#>
               ~Gate : Only allows those on its access list through. Acts like a firewall for anyone else. A fee is paid per entry.
               !Display : Displays text, up to 20*level characters.
                  HEX/E : Displays text*/
}