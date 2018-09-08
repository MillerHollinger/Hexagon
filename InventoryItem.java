public class InventoryItem
{
   public String name;
   public int level;
   public boolean isLocagon;
   public int count;
   
   public InventoryItem(String n, int l, boolean i)
   {
      name = n;
      level = l;
      isLocagon = i;
      count = 1;
   }
   
   public String toString()
   {
      return name + " LV "+level;
   }
}