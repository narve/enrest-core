using System.Collections.Generic;
using System.Text;
using System.Threading.Tasks;
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
        public static readonly MediaTypeHeaderValue[] HtmlMediaTypes = new []
        {
            MediaTypeHeaderValue.Parse("text/html"),
            MediaTypeHeaderValue.Parse("application/html"),
        };
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
            var creator = new ItemSerializer(sp.GetRequiredService<IDbInspector>(), sp.GetRequiredService<ILinkManager>());
            ser.Add(o => o is IDictionary<string, object>, o => creator.Serialize((IDictionary<string, object>)o, 3, ser));
            HtmlSerializerRegistry.AddDefaults(ser);
            var htmlElements = ser.Serialize(context.Object, 2, ser);
            foreach (var element in htmlElements)
            {
                await context.HttpContext.Response.WriteAsync(element.ToHtml());
            }
        }
    }
}