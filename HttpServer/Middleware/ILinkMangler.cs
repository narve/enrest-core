using System;
using System.Collections.Generic;
using System.Threading.Tasks;
using Microsoft.AspNetCore.Http;
using Microsoft.Extensions.Configuration;
using Microsoft.Extensions.Logging;

namespace HttpServer.Middleware
{
    public interface ILinkMangler
    {
        public string MapOutgoingUrl(string url);
        public string MapIncomingUrl(string path);
    }

    public class NoMangler : ILinkMangler
    {
        public string MapOutgoingUrl(string url) => url;

        public string MapIncomingUrl(string path) => path;
    }

    public class GuidMangler : ILinkMangler
    {
        private readonly Dictionary<string, string> _outgoing = new();
        private readonly Dictionary<string, string> _incoming = new();

        public string MapOutgoingUrl(string url)
        {
            if (_outgoing.TryGetValue(url, out var newUrl))
                return newUrl;

            var guid = Guid.NewGuid().ToString();
            _outgoing[url] = guid;
            _incoming[guid] = url;
            return guid;
        }

        public string MapIncomingUrl(string url)
        {
            var path = url[1..];
            if (_incoming.TryGetValue(path, out var newUrl))
                return newUrl;

            if (IsWhiteListed(path))
            {
                return path;
            }

            throw new ArgumentException("Not found/ mapped: " + path);
        }

        private bool IsWhiteListed(string url) =>
            string.IsNullOrEmpty(url) || url.Equals("/") || url.StartsWith("/web");
    }

    public class LinkManglerMiddleware
    {
        private readonly RequestDelegate _next;

        public LinkManglerMiddleware(RequestDelegate next) => _next = next;

        public async Task InvokeAsync(HttpContext context, IServiceProvider provider, ILogger<ExceptionMiddleware> logger,
            IConfiguration config, ILinkMangler mangler)
        {
            context.Request.Path = mangler.MapIncomingUrl(context.Request.Path);
            await _next(context);
        }
    }
}