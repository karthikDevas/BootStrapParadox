using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace Dunzo
{
    public class Item
    {
        public string name { get; set; }
        public double qty { get; set; }
        public double rate { get; set; }
        public double amount { get; set; }
    }
    public class Store
    {
        public string name { get; set; }
        public string address { get; set; }
        public string phone {get;set;}
        public string tin_no { get; set; }
        public List<Item> Items { get; set; }
    }
}
