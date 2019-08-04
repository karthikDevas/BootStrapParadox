using Newtonsoft.Json;
using Newtonsoft.Json.Linq;
using System;
using System.Collections.Generic;
using System.Diagnostics;
using System.IO;
using System.Linq;
using System.Net.Http;
using System.Net.Http.Headers;
using System.Runtime.InteropServices.WindowsRuntime;
using System.Text.RegularExpressions;
using System.Threading.Tasks;
using Windows.Foundation;
using Windows.Foundation.Collections;
using Windows.Storage;
using Windows.Storage.Pickers;
using Windows.Storage.Streams;
using Windows.UI.Xaml;
using Windows.UI.Xaml.Controls;
using Windows.UI.Xaml.Controls.Primitives;
using Windows.UI.Xaml.Data;
using Windows.UI.Xaml.Input;
using Windows.UI.Xaml.Media;
using Windows.UI.Xaml.Media.Imaging;
using Windows.UI.Xaml.Navigation;


// The Blank Page item template is documented at https://go.microsoft.com/fwlink/?LinkId=402352&clcid=0x409

namespace Dunzo
{
    /// <summary>
    /// An empty page that can be used on its own or navigated to within a Frame.
    /// </summary>
    public sealed partial class MainPage : Page
    {
        const string subscriptionKey = "<Your Subscription Key Here>";

        Store Store = new Store();

        const string uriBase =
            "https://westcentralus.api.cognitive.microsoft.com/vision/v2.0/recognizeText";
        List<ReceiptLine> ReceiptLines = null;
        List<YText> linesinreceipt = null;

        public MainPage()
        {
            this.InitializeComponent();
        }
        async Task MakeAnalysisRequest(byte[] image)
        {
            ProgressRing.IsActive = true;
            try
            {
                HttpClient client = new HttpClient();

                client.DefaultRequestHeaders.Add(
                    "Ocp-Apim-Subscription-Key", subscriptionKey);

                string uri = uriBase + "?mode=Printed";

                HttpResponseMessage response;

                linesinreceipt = new List<YText>();
                byte[] byteData = image;


                using (ByteArrayContent content = new ByteArrayContent(byteData))
                {

                    content.Headers.ContentType =
                        new MediaTypeHeaderValue("application/octet-stream");

                    response = await client.PostAsync(uri, content);


                }
                IEnumerable<string> operationLocation = response.Headers.Single(g => g.Key == "Operation-Location").Value;
                await Task.Delay(2000);
                var Value = await client.GetAsync(operationLocation.ToList()[0]);

                string contentString = await Value.Content.ReadAsStringAsync();

                CognitiveServiceResponse res = JsonConvert.DeserializeObject<CognitiveServiceResponse>(contentString);

                foreach (var line in res.recognitionResult.lines)
                {
                    var liney = line.boundingBox[1];
                    if (linesinreceipt.Count(g => g.y == liney || (liney <= g.y + 7)) > 0)
                    {
                        var receiptline = linesinreceipt.Last(g => g.y == liney || (liney <= g.y + 7));
                        receiptline.text = receiptline.text + " " + line.text;
                    }
                    else
                        linesinreceipt.Add(new YText() { y = line.boundingBox[1], text = line.text });

                }
                Store = new Store();
                Store.Items = new List<Item>();

                var headerindex = linesinreceipt.IndexOf(linesinreceipt.First(g => g.text.ToLower().Contains("amount") || g.text.ToLower().Contains("amt") || g.text.ToLower().Contains("total")));
                List<string> numbers = Regex.Split(linesinreceipt[++headerindex].text, @"[^0-9\.\,]+").ToList();
                numbers.RemoveAll(g => string.IsNullOrEmpty(g.Trim()));
                var itemn = Regex.Replace(linesinreceipt[headerindex].text, @"[0-9\.\,]+", "");
                int count = numbers.Count;
                int j = numbers.Count;
                Store.Items.Add(new Item() { name = itemn, qty = System.Convert.ToDouble(numbers[j - 3]), rate = System.Convert.ToDouble(numbers[j - 2]), amount = System.Convert.ToDouble(numbers[j - 1]) });

                while (true)
                {
                    try
                    {
                        List<string> numbers1 = Regex.Split(linesinreceipt[++headerindex].text, @"[^0-9\.\,]+").ToList();
                        numbers1.RemoveAll(g => string.IsNullOrEmpty(g.Trim()));
                        int k = numbers1.Count;
                        if (numbers1.Count < count-1  )
                            break;
                        var itemn1 = Regex.Replace(linesinreceipt[headerindex].text, @"[0-9\.\,]+", "");

                        Store.Items.Add(new Item() { name = itemn1, qty = System.Convert.ToDouble(numbers1[k - 3]), rate = System.Convert.ToDouble(numbers1[k - 2]), amount = System.Convert.ToDouble(numbers1[k - 1]) });
                    }
                    catch { }
                }
                ResultListView.ItemsSource = Store.Items.CloneJson();
                ReceiptLines = res.recognitionResult.lines;
                TextListView.ItemsSource = linesinreceipt.CloneJson();
                try
                {
                    Store.tin_no = getStoreTINNumber();
                    Store.phone = getStorePhoneNumber();
                }
                catch { }
                Debug.WriteLine("\nResponse:\n\n{0}\n",
                    JToken.Parse(contentString).ToString());
                OrderPostRequest orderPost = new OrderPostRequest();
                orderPost.id = "101";

                orderPost.store = JsonConvert.SerializeObject(Store);
                var jsonstring = JsonConvert.SerializeObject(orderPost);
                HttpClient client1 = new HttpClient();
               await client1.PostAsync("https://10.105.24.27:8443/api/orders/", new StringContent(jsonstring));

            }
            catch (Exception e)
            {
                Console.WriteLine("\n" + e.Message);
            }
            ProgressRing.IsActive = false;

        }

        public async Task<StorageFile> PickFileAsync()
        {
            FileOpenPicker openPicker = new FileOpenPicker();
            openPicker.FileTypeFilter.Add(".jpg");
            openPicker.FileTypeFilter.Add(".png");
            openPicker.FileTypeFilter.Add(".jpeg");
            return await openPicker.PickSingleFileAsync();
        }

        private async void Button_Click(object sender, RoutedEventArgs e)
        {
            byte[] fileBytes = null;
            var file = await PickFileAsync();
            BitmapImage bitmapImage = new BitmapImage();
            await bitmapImage.SetSourceAsync(await file.OpenAsync(FileAccessMode.Read));
            image.Source = bitmapImage;
            using (var stream = await file.OpenReadAsync())
            {
                fileBytes = new byte[stream.Size];
                using (var reader = new DataReader(stream))
                {
                    await reader.LoadAsync((uint)stream.Size);
                    reader.ReadBytes(fileBytes);
                }
            }
            await MakeAnalysisRequest(fileBytes);
        }
        private string getStorePhoneNumber()
        {
            var phonenumberline = ReceiptLines.Single(g => g.text.ToLower().Contains("ph no") || g.text.ToLower().Contains("phone") || g.text.ToLower().Contains("tel no") || g.text.ToLower().Contains("ph")).text;
            return new String(phonenumberline.Where(Char.IsDigit).ToArray());
        }
        private string getStoreTINNumber()
        {
            var phonenumberline = ReceiptLines.Single(g => g.text.ToLower().Contains("tin no") || g.text.ToLower().Contains("tin")).text;
            return new String(phonenumberline.Where(Char.IsDigit).ToArray());
        }


    }

    public class YText
    {
        public double y { get; set; }
        public string text { get; set; }
    }
    public class OrderPostRequest
    {
        public string id { get; set; }
        public string store { get; set; }
    }
    public static class CommonData
    {
        public static T CloneJson<T>(this T source)
        {
            if (Object.ReferenceEquals(source, null))
            {
                return default(T);
            }

            var deserializeSettings = new JsonSerializerSettings { ObjectCreationHandling = ObjectCreationHandling.Replace };

            return JsonConvert.DeserializeObject<T>(JsonConvert.SerializeObject(source), deserializeSettings);
        }
    }
}
