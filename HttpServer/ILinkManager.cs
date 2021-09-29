using System.Collections.Generic;
using System.Linq;
using HttpServer.Middleware;

namespace HttpServer
{
    public interface ILinkManager
    {
        public const string GetCreateForm = "/forms/create/{table}";
        public const string GetEditForm = "/forms/edit/{table}/{id}";
        public const string GetDeleteForm = "/forms/delete/{table}/{id}";
        public const string GetLoginForm = "/forms/login";
        public const string GetLogoutForm = "/forms/logout";
        public const string LoginAction = "/actions/login";
        public const string LogoutAction = "/actions/logout";
        public const string DeleteAction = "/actions/delete/{table}/{id}";
        public const string PostNewItem = "{table}";
        public const string PostExistingItem = "{table}/{id}";
        public const string GetItemById = "{table}/{id}";
        public const string GetItemsOfType = "{table}";
        public const string DownloadLob = "/blob/{table}/{id}/{field}";

        string Return(string outgoingUrl);

        string LinkToItem(string trg, object id) => Return("/" + trg + "/" + id);
        string LinkToLob(string table, string id, string field) => Return(Replace(DownloadLob, new { table, id, field }));


        string LinkToQuery(string tName, IEnumerable<KeyValuePair<string, object>> filters = null)
        {
            var b = "/" + tName;
            if (filters != null)
            {
                b += "?";
                b += filters.Select(kvp => kvp.Key + "=" + kvp.Value).JoinToString();
            }

            return Return(b);
        }

        string LinkToEditForm(string table, string id) =>
            Return($"/forms/edit/{table}/{id}");

        string LinkToDeleteForm(string table, string id) =>
            Return($"/forms/delete/{table}/{id}");

        string LinkToCreateForm(string table) =>
            Return($"/forms/create/{table}");


        string Replace(string template, object fields)
        {
            var props = fields.GetType().GetProperties();
            foreach (var prop in props)
            {
                template = template.Replace($"{{{prop.Name}}}", prop.GetValue(fields)?.ToString());
            }

            return Return(template);
        }

        string LinkToCreateAction(string table) => Return("/" + table);
        string LinkToEditAction(string table, string id) => Return("/" + table + "/" + id);
        string LinkToDeleteAction(string table, string id) => Return(Replace(DeleteAction, new { table, id }));
    }

    public class LinkManager : ILinkManager
    {
        private readonly ILinkMangler _linkMangler;

        public LinkManager(ILinkMangler linkMangler) =>
            _linkMangler = linkMangler;

        public string Return(string outgoingUrl) =>
            _linkMangler.MapOutgoingUrl(outgoingUrl);
    }
}