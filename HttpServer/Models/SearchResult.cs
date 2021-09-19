using DV8.Html.Elements;

namespace HttpServer.Models
{
    public class SearchResult
    {
        public object[] Items { get; set; }

        public A[] Links { get; set; }
    }
}