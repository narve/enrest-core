using System.Collections.Generic;
using System.Linq;

namespace HttpServer
{
    public interface ILinkManager
    {
        public const string GetCreateForm = "forms/create/{table}";
        public const string GetEditForm = "forms/edit/{table}/{id}";
        public const string PostNewItem = "{table}";
        public const string PostExistingItem = "{table}/{id}";
        public const string GetItemById = "{table}/{id}";
        public const string GetItemsOfType = "{table}";

        string LinkToItem(string trg, object id) => "/" + trg + "/" + id;

        string LinkToQuery(string tName, IEnumerable<KeyValuePair<string, object>> filters = null)
        {
            var b = "/" + tName;
            if (filters != null)
            {
                b += "?";
                b += filters.Select(kvp => kvp.Key + "=" + kvp.Value).JoinToString();
            }

            return b;
        }

        string LinkToEditForm(string table, string id) => $"/forms/edit/{table}/{id}";
        string LinkToCreateForm(string table) => $"/forms/create/{table}";
    }

    public class LinkManager : ILinkManager
    {
    }
}