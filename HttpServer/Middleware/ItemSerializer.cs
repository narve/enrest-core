using System.Collections.Generic;
using System.Linq;
using DV8.Html.Elements;
using DV8.Html.Support;
using DV8.Html.Utils;
using HttpServer.DbUtil;

namespace HttpServer.Middleware
{
    public class ItemSerializer : IHtmlSerializer
    {
        private readonly IDbInspector _dbInspector;
        private readonly ILinkManager _linkManager;

        public ItemSerializer(IDbInspector dbInspector, ILinkManager linkManager)
        {
            _dbInspector = dbInspector;
            _linkManager = linkManager;
        }

        public bool CanSerialize(object o)
        {
            return o is IDictionary<string, object>;
        }

        public IEnumerable<IHtmlElement> Serialize(object x, int lvl, IHtmlSerializer fac)
        {
            var d = (IDictionary<string, object>)x;
            var itemtype = HtmlSupport.Itemtype(x);
            var subs = d.Keys.ToRawList().Cast<string>()
                .Select(name => KeyValuePair.Create(name, d[name]))
//                    .Where(a => a.Val != null)
                .SelectMany(a => new IHtmlElement[]
                {
                    new Dt(ColumnToTitle(a.Key)),
                    new Dd
                    {
                        // Itemprop = a.Key,
                        // Subs = fac.Serialize(a.Val, lvl - 1, fac).ToArray()
                        Subs = DbValueToElement("item", d, a).ToArray()
                    }
                })
                .ToArray();
            return new Dl
            {
                Subs = subs,
                Itemscope = true,
                Itemtype = itemtype,
            }.ToArray();
        }

        private HtmlElement WithItemProp(HtmlElement e, string propName)
        {
            e.Itemprop = propName;
            return e;
        }

        private HtmlElement DbValueToElement(string table, IDictionary<string, object> obj, KeyValuePair<string, object> kvp)
        {
            var col = _dbInspector.GetColumn(table, kvp.Key);
            if (col == null)
            {
                // throw new ArgumentException(nameof(col), kvp.Key);
                // return new Span(kvp.Value?.ToString());
            }

            if (_dbInspector.IsFk(table, kvp.Key))
            {
                var trg = _dbInspector.GetFkTarget(table, kvp.Key);
                return new A
                {
                    // Text = trg + "#" + kvp.Value,
                    rel = trg,
                    Href = _linkManager.LinkToItem(trg, kvp.Value),
                    Subs = new Span(kvp.Value?.ToString())
                    {
                        Itemscope = false,
                        Itemprop = kvp.Key
                        // Itemtype = kvp.Key,
                    }.ToArray()
                };
            }

            if (_dbInspector.GetPkColumn(table).Name == kvp.Key)
            {
                return new A
                {
                    // Text = kvp.Value.ToString(),
                    rel = "self",
                    Href = _linkManager.LinkToItem(table, kvp.Value),
                    Subs = WithItemProp(new Span(kvp.Value?.ToString()), kvp.Key).ToArray(),
                    Itemscope = false,
                };
            }

            if (_dbInspector.IsLob(table, kvp.Key))
            {
                return new A
                {
                    Text = "Download " + kvp.Key,
                    Href = _linkManager.LinkToLob(table, _dbInspector.GetId(table, obj), kvp.Key),
                    Itemscope = true, Itemprop = kvp.Key,
                };
            }

            if (kvp.Value is HtmlElement e)
                return WithItemProp(e, kvp.Key);

            if (kvp.Value is HtmlElement[] ea)
                return new Ul(ea.Select(x => new Li(WithItemProp(x, kvp.Key))));

            return WithItemProp(new Span(kvp.Value?.ToString()), kvp.Key);
        }

        private string ColumnToTitle(string colName) =>
            colName.UppercaseFirst();
    }
}