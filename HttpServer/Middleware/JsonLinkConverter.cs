using System;
using System.Collections.Generic;
using System.Text.Json;
using System.Text.Json.Serialization;
using DV8.Html.Elements;

namespace HttpServer.Middleware
{
    public class JsonLinksConverter : JsonConverter<Ul>
    {
        public override Ul? Read(ref Utf8JsonReader reader, Type typeToConvert, JsonSerializerOptions options)
        {
            throw new NotImplementedException();
        }

        public override void Write(Utf8JsonWriter writer, Ul value, JsonSerializerOptions options)
        {
            // base.Write(writer, value, options);
            throw new NotImplementedException();
        }
    }

    public class JsonLinkConverter : JsonConverter<A>
    {
        public override A? Read(ref Utf8JsonReader reader, Type typeToConvert, JsonSerializerOptions options)
        {
            throw new NotImplementedException();
        }

        public override void Write(Utf8JsonWriter writer, A value, JsonSerializerOptions options)
        {
            // throw new NotImplementedException();
            // writer.WritePropertyName(nameof(A.Href));
            writer.WriteString(nameof(A.Href), value.Href);
            writer.WriteString(nameof(A.rel), value.rel);
            writer.WriteString(nameof(A.Text), value.Text);
            writer.Flush();
            // writer.WritePropertyName(nameof(A.Href));
        }
    }
}