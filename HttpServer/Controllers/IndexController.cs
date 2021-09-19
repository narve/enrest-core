using System;
using System.Collections.Generic;
using System.Collections.Immutable;
using System.Data;
using System.Linq;
using System.Threading.Tasks;
using Dapper;
using DatabaseSchemaReader.DataSchema;
using DV8.Html.Elements;
using DV8.Html.Utils;
using HttpServer.DbUtil;
using HttpServer.Models;
using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Http;
using Microsoft.AspNetCore.Mvc;
using Microsoft.Extensions.Logging;
using Microsoft.Extensions.Primitives;

// ReSharper disable PossibleNullReferenceException

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
        private readonly FormCreator _formsCreator;
        private readonly IHttpContextAccessor _httpContextAccessor;
        private readonly DbMutator _dbMutator;

        public IndexController(ILogger<IndexController> logger, IDbInspector dbInspector, IDbConnectionProvider dbConnectionProvider,
            FormCreator formsCreator, IHttpContextAccessor httpContextAccessor, DbMutator dbMutator)
        {
            _logger = logger;
            _dbInspector = dbInspector;
            _dbConnectionProvider = dbConnectionProvider;
            _formsCreator = formsCreator;
            _httpContextAccessor = httpContextAccessor;
            _dbMutator = dbMutator;
        }

        [HttpPost("{table}")]
        public async Task<RedirectResult> CreateObject(string table)
        {
            var collection = _httpContextAccessor.HttpContext.Request.Form;
            var kvpa = collection.Select(kvp => KeyValuePair.Create(kvp.Key, kvp.Value)).ToList();
            _logger.LogInformation("Should insert into {table} values {values}", table, kvpa.JoinToString());
            var inserted = await _dbMutator.InsertRow(table, kvpa);
            _logger.LogInformation("Insert into {table}: {values}", table, inserted.DictToString());
            return new RedirectResult("/" + table + "/" + inserted["id"], false, false);
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
            d._links = GetLinksForItem(table, d);
            return d;
        }

        private A[] GetLinksForItem(string table, IDictionary<string, object> o)
        {
            // var dict = (IDictionary<string, object>)o; 
            return _dbInspector.GetSchema().Tables
                .SelectMany(t => t.ForeignKeys)
                .Where(fk => fk.RefersToTable == table)
                .Select(fk => GetFkLink(fk, o))
                .ToArray();
        }

        private A GetFkLink(DatabaseConstraint fk, IDictionary<string, object> o)
        {
            var filter = fk.Columns.Select(c => $"{c}={o["id"]}").JoinToString(",");
            return new A("/" + fk.TableName + "?" + filter);
        }

        [HttpGet("{table}")]
        public async Task<object> GetResultForTable(string table)
        {
            IQueryCollection query = _httpContextAccessor.HttpContext.Request.Query;
            // Dictionary<string,string?> dict = query.ToDictionary(kvp => kvp.Key, kvp => kvp.Value.SingleOrDefault());
            
            var parameters = new SortedDictionary<string, object>();
            var where = ""; 
            // if (query.Any())
            // {
            //     where += " WHERE ";
            // }
            // foreach (var kvp in query)
            // {
            //     where += kvp.Key + "=@" + kvp.Key;
            //     parameters.Add(kvp.Key, kvp.Value);
            // }
            
            
            
            
            var sql = $"select * from {table}{where}";
            var rs = await _dbConnectionProvider.Get().QueryAsync(sql, parameters);
            object[] rs2 = rs
                .Cast<IDictionary<string, object>>()
                .Select(objects => Prettify(table, objects))
                .ToArray();
            return new SearchResult { Items = rs2, Links = LinksForTable(table) };
        }

        [HttpGet("forms/create/{table}")]
        public async Task<IHtmlElement> GetCreateForm(string table)
        {
            return await _formsCreator.GetCreateForm(table);
        }


        private A[] LinksForTable(string table) =>
            new[]
            {
                new A($"/forms/create/{table}", $"Create-form for {table}", "create form")
            };

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