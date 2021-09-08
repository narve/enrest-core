using System;
using System.Collections.Generic;
using System.Linq;
using System.Threading.Tasks;
using Dapper;
using DatabaseSchemaReader.DataSchema;
using DV8.Html.Elements;
using DV8.Html.Utils;
using HttpServer.DbUtil;
using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;
using Microsoft.Extensions.Logging;

// ReSharper disable UnusedMember.Global

namespace HttpServer.Controllers
{
    // [Authorize]
    [ApiController]
    [Route("/")]
    public class IndexController
    {
        private readonly ILogger<IndexController> _logger;
        private readonly IDbInspector _dbInspector;
        private readonly IDbConnectionProvider _dbConnectionProvider;

        public IndexController(ILogger<IndexController> logger, IDbInspector dbInspector, IDbConnectionProvider dbConnectionProvider)
        {
            _logger = logger;
            _dbInspector = dbInspector;
            _dbConnectionProvider = dbConnectionProvider;
        }

        [HttpGet("favicon.ico")]
        [AllowAnonymous]
        public ActionResult GetFavIcon()
        {
            const string svg = @"
<svg
  xmlns=""http://www.w3.org/2000/svg""
  viewBox=""0 0 16 16"">

  <text x=""0"" y=""14"">🦄</text>
</svg>
";
            return new ContentResult { Content = svg };
        }

        [HttpGet("{table}/{id}")]
        public async Task<object> GetById(string table, string id)
        {
            var t = _dbInspector.GetSchema().FindTableByName(table);
            var idColName = t.PrimaryKey.Columns.Single();
            var idCol = t.FindColumn(idColName);
            var idExp = idCol.DataType.IsString ? idColName : $"cast ({idCol.Name} as varchar)";
            var sql = $"select * from {table} where {idExp} = @id";
            var d = await _dbConnectionProvider.Get().QuerySingleAsync(sql, new { id });
            return d;
        }

        [HttpGet("{table}")]
        public async Task<object> GetForType(string table)
        {
            var sql = $"select * from {table}";
            var rs = await _dbConnectionProvider.Get().QueryAsync(sql);
            var rs2 = rs
                .Cast<IDictionary<string, object>>()
                .Select(objects => Prettify(table, objects))
                .ToList();
            return rs2;
        }

        public IDictionary<string, object> Prettify(string table, IDictionary<string, object> dict)
        {
            return dict
                .Select(kvp => KeyValuePair.Create(ColumnToTitle(kvp.Key), DbValueToElement(table, kvp)))
                .ToDictionary(kvp => kvp.Key, kvp => kvp.Value);
        }

        public object DbValueToElement(string table, KeyValuePair<string, object> keyValuePair)
        {
            if (_dbInspector.IsFk(table, keyValuePair.Key))
            {
                var trg = _dbInspector.GetFkTarget(table, keyValuePair.Key);
                return new A
                {
                    Text = trg + "#" + keyValuePair.Value,
                    Href = "/" + trg + "/" + keyValuePair.Value,
                };
            }

            // var t = keyValuePair.Value?.GetType();
            // if (t != null && t != typeof(string) && t != typeof(long) && t != typeof(int))
            // {
            //     return keyValuePair.Value?.ToString();
            // }

            return keyValuePair.Value;
        }

        private string ColumnToTitle(string argKey)
        {
            return argKey.UppercaseFirst();
        }

        private A Link(DatabaseTable t) =>
            new()
            {
                Href = t.Name,
                Text = $"{t.Name.UppercaseFirst()} {t.Description}",
            };

        [HttpGet]
        public object Get() =>
            _dbInspector.GetSchema().Tables
                .Where(t => !"information_schema".Equals(t.SchemaOwner, StringComparison.OrdinalIgnoreCase))
                .Where(t => !"auth".Equals(t.SchemaOwner, StringComparison.OrdinalIgnoreCase))
                .Where(t => "public".Equals(t.SchemaOwner, StringComparison.OrdinalIgnoreCase))
                .Select(Link);

        // [HttpGet]
        // public async Task<object> GetOld()
        // {
        //     // var conn = GetConnection();
        //
        //     var fkSql = @"-- noinspection SqlResolveForFile
        //     
        //     SELECT 
        //         m.name
        //         , p.*
        //     FROM
        //         sqlite_master m
        //         JOIN pragma_foreign_key_list(m.name) p ON m.name != p.""table""
        //                 WHERE m.type = 'table'
        //                 ORDER BY m.name
        //                     ;";
        //
        //     var infoSql = @"-- noinspection SqlResolveForFile
        //     
        //     SELECT 
        //       *
        //     FROM 
        //       sqlite_master AS m
        //     JOIN 
        //       pragma_table_info(m.name) AS p
        //     ORDER BY 
        //       m.name, 
        //       p.cid";
        //
        //     var sqls = new Dictionary<string, string>
        //     {
        //         { "Foreign keys", fkSql },
        //         { "Info", infoSql },
        //     };
        //
        //     var res = new Dictionary<string, object>();
        //
        //
        //     foreach (var sql in sqls)
        //     {
        //         // IEnumerable<dynamic> rs = await conn.QueryAsync(sql.Value);
        //         // var rs2 = rs.Cast<IDictionary<string, object>>(); 
        //         // var rs3 = rs2
        //         //     .Select(x => x.Keys.Distinct().Select(k => KeyValuePair.Create(k, x[k])).ToDictionary(x2 => x2.Key, x2 => x2.Key == "sql" ? "sql!" : x2.Value))
        //         //     .ToList();
        //         // res.Add(sql.Key, rs2);
        //     }
        //
        //
        //     // res.Add("Meta", GetSchema());
        //
        //     // return new object[] { tables, res};
        //     return res;
        // }
    }
}