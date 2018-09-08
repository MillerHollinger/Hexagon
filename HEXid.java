import java.util.*;

public class HEXid
{
   public String id; //Name.
   public int hexes; //Currency.
   public int level; //What they can do.
   public int charge; //Like XP for level ups.
   
   public int row;   //Used for Hexagon navigation.
   public int column;
   
   public int invisLeft; //How many invisible turns are left
   
   
   public ArrayList<InventoryItem> inventory; //List of things they have in InventoryItem form.
   public ArrayList<InventoryItem> hacks;
   public ArrayList<InventoryItem> bits;
   
   //First if the user owns a bank, then how much money they have stored there.
   public boolean ownsBank;
   public int inBank;
   
   //Corruption update booleans.
   boolean hasCorruptBit;
   boolean isFirewall;
   
   public HEXid() //New user constructor.
   {
      id = createId(); 
      hexes = 0; 
      charge = 0;
      level = 1;
      row = new Random().nextInt(100);
      column =  new Random().nextInt(100);
      inventory = new ArrayList<InventoryItem>();
      hacks = new ArrayList<InventoryItem>();
      bits = new ArrayList<InventoryItem>();
      invisLeft = 0;
      ownsBank = false;
      inBank = 0;
      hasCorruptBit = false;
      isFirewall = false;
   }
   public HEXid(String i, int h, int l) //Full constructor.
   {
      id = i; 
      charge = 0;
      hexes = h; 
      level = l;
      row = new Random().nextInt(100);
      column =  new Random().nextInt(100);
      inventory = new ArrayList<InventoryItem>();
      ownsBank = false;
      hasCorruptBit = false;
   }
   
   public void kick(String why)
   {
      id = "KICKED/"+why;
   }

   public String createId()
   {
      String back = "";
      Random rn = new Random();
      int rand;
      
      while (back.length() < 6)
      {
         rand = rn.nextInt(16);
         if (rand <= 9)
            back += rand;
         else
            switch (rand)
            {
               case 10:
                  back += "A";
                  break;
                  
               case 11:
                  back += "B";
                  break;
                  
               case 12:
                  back += "C";
                  break;
                  
               case 13:
                  back += "D";
                  break;
                  
               case 14:
                  back += "E";
                  break;
                  
               case 15:
                  back += "F";
                  break;
            }
      }
      return back;
   }
}