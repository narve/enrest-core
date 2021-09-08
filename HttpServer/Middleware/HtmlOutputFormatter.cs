using System;
using System.Collections.Generic;
using System.Text;
using System.Threading.Tasks;
using DV8.Html.Elements;
using DV8.Html.Serialization;
using Microsoft.AspNetCore.Http;
using Microsoft.AspNetCore.Mvc.Formatters;
using Microsoft.Net.Http.Headers;

namespace HttpServer.Middleware
{
    public class HtmlOutputFormatter : TextOutputFormatter
    {
        public HtmlOutputFormatter()
        {
            SupportedMediaTypes.Add(MediaTypeHeaderValue.Parse("text/html"));
            SupportedMediaTypes.Add(MediaTypeHeaderValue.Parse("application/html"));
            SupportedEncodings.Add(Encoding.UTF8);
            SupportedEncodings.Add(Encoding.Unicode);
        }

        public override async Task WriteResponseBodyAsync(OutputFormatterWriteContext context, Encoding selectedEncoding)
        {
            var ser = new HtmlSerializerRegistry();
            ser.Add(o => o is IDictionary<string, object>, o =>
            {
                try
                {
                    return new GenDictSerializer().Serialize(o, 3, ser);
                }
                catch (Exception e)
                {
                    return new Span("ERROR").ToArray();
                }
            });
            HtmlSerializerRegistry.AddDefaults(ser);
            var htmlElements = ser.Serialize(context.Object, 2, ser);
            foreach (var element in htmlElements)
            {
                await context.HttpContext.Response.WriteAsync(element.ToHtml());
            }
        }
    }
}