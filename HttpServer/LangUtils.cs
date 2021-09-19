using System.Collections.Generic;
using System.Linq;
using static System.String;

namespace HttpServer
{
    public static class LangUtils
    {
        public static string JoinToString<T>(this IEnumerable<T> items, string separator = ", ", string ifEmpty = "")
        {
            var list = items.ToList();
            return list.Any() ? Join(separator, list.Select(e => e?.ToString()).ToArray()) : ifEmpty;
        }
    }
}