using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using Dapper;
using DV8.Html.Elements;
using DV8.Html.Serialization;
using DV8.Html.Utils;
using HttpServer.DbUtil;
using Microsoft.AspNetCore.Http;
using Microsoft.AspNetCore.Mvc.Formatters;
using Microsoft.Extensions.DependencyInjection;
using Microsoft.Net.Http.Headers;

namespace HttpServer.Middleware
{
    public class HtmlOutputFormatter : TextOutputFormatter
    {
        public static readonly MediaTypeHeaderValue[] HtmlMediaTypes = new[]
        {
            MediaTypeHeaderValue.Parse("text/html"),
            MediaTypeHeaderValue.Parse("application/html"),
        };

        private IDbConnectionProvider _connectionProvider;

        public HtmlOutputFormatter()
        {
            HtmlMediaTypes.ForEach(t => SupportedMediaTypes.Add(t));
            SupportedEncodings.Add(Encoding.UTF8);
            SupportedEncodings.Add(Encoding.Unicode);
        }

        public override async Task WriteResponseBodyAsync(OutputFormatterWriteContext context, Encoding selectedEncoding)
        {
            var ser = new HtmlSerializerRegistry();
            var sp = context.HttpContext.RequestServices;
            this._connectionProvider = sp.GetRequiredService<IDbConnectionProvider>();
            var creator = new ItemSerializer(sp.GetRequiredService<IDbInspector>(), sp.GetRequiredService<ILinkManager>());
            ser.Add(o => o is IDictionary<string, object>, o => creator.Serialize((IDictionary<string, object>)o, 3, ser));
            HtmlSerializerRegistry.AddDefaults(ser);
            var htmlElements = ser.Serialize(context.Object, 2, ser);

            var res = MakeCompleteDocument(context, htmlElements);
            await context.HttpContext.Response.WriteAsync(res.ToHtml());
        }

        private IHtmlElement MakeCompleteDocument(OutputFormatterWriteContext context, IEnumerable<IHtmlElement> body) =>
            new Html
            {
                Subs = new IHtmlElement[]
                {
                    new Head
                    {
                        Subs = new IHtmlElement[]
                        {
                            new Link
                            {
                                Rel = "stylesheet",
                                Href = "/web/api.css",
                            }
                        }
                    },
                    new Body
                    {
                        Subs = BodyHeader(context).Concat(body.ToArray()).Concat(BodyFooter(context)).ToArray()
                    }
                }
            };

        private IHtmlElement[] BodyHeader(OutputFormatterWriteContext ctx)
        {
            return new IHtmlElement[]
            {
                new Div
                {
                    Subs = NavBar(ctx),
                },
                new H1(ctx.HttpContext.Request.Path),
            };
        }

        private IHtmlElement[] NavBar(OutputFormatterWriteContext ctx)
        {
            var p = ctx.HttpContext.Request.Path.Value;
            var l = new List<A> { new A("/", "home") };
            for (var index = 1; index < p.Length; index++)
            {
                if (p[index] == '/')
                {
                    l.Add(new A(p.Substring(0, index)));
                }
            }

            return l.Cast<IHtmlElement>().ToArray();
        }

        private IHtmlElement[] BodyFooter(OutputFormatterWriteContext ctx)
        {
            return new IHtmlElement[]
            {
                new Div()
                {
                    Subs = CookieInformation(ctx)
                        .Concat(ServerInformation().GetAwaiter().GetResult())
                        .ToArray()
                }
            };
        }

        private async Task<IHtmlElement[]> ServerInformation()
        {
            var strings = new[]
            {
                "SELECT concat( SESSION_USER, ', ', CURRENT_USER)",
                "select auth.uid()",
            };
            var conn = await _connectionProvider.Get();
            var elements = strings.Select(s => conn.QuerySingle<object>(s))
                .Select(r => new Li(r.ToString()))
                .ToArray();
            return elements;
            // var x = await conn.QuerySingleAsync("select set_config('request.jwt.claim.sub', 'f65eaad3-2041-4f01-a5db-133c57ebdb05', false)");
            // return new Span("Authid=" + x).ToArray();


        }

        private IHtmlElement[] CookieInformation(OutputFormatterWriteContext ctx)
        {
            return ctx.HttpContext.Request.Cookies
                .Select(c => new Span($"{c.Key}={c.Value}"))
                .Cast<IHtmlElement>().ToArray();
        }
    }
}