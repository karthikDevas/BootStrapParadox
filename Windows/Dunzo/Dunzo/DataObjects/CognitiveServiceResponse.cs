using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace Dunzo
{
    public class Word
    {
        public List<int> boundingBox { get; set; }
        public string text { get; set; }
        public string confidence { get; set; }
    }

    public class ReceiptLine
    {
        public List<int> boundingBox { get; set; }
        public string text { get; set; }
        public List<Word> words { get; set; }
    }

    public class RecognitionResult
    {
        public List<ReceiptLine> lines { get; set; }
    }

    public class CognitiveServiceResponse
    {
        public string status { get; set; }
        public RecognitionResult recognitionResult { get; set; }
    }
   
        

}
