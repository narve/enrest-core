using System.Collections.Generic;
using System.Data.Common;
using System.Linq;
using System.Threading.Tasks;
using Dapper;
using DatabaseSchemaReader.DataSchema;
using DV8.Html.Serialization;
using Microsoft.AspNetCore.Mvc;
using Microsoft.Data.Sqlite;
using Microsoft.Extensions.Logging;

namespace HttpServer.Controllers
{
    // [Authorize]
    [ApiController]
    [Route("/")]
    public class IndexController
    {
        private readonly ILogger<IndexController> _logger;

        public IndexController(ILogger<IndexController> logger) => _logger = logger;

        [HttpGet("{table}")]
        public async Task<object> GetForType(string table)
        {
            var sql = $"select * from {table}";
            IEnumerable<dynamic> rs = await GetConnection().QueryAsync(sql);

            var rs2 = rs.Cast<IDictionary<string, object>>().ToList();


            return rs2;
        }

        [HttpGet]
        public async Task<object> Get()
        {
            var conn = GetConnection();

            var fkSql = @"-- noinspection SqlResolveForFile
            
            SELECT 
                m.name
                , p.*
            FROM
                sqlite_master m
                JOIN pragma_foreign_key_list(m.name) p ON m.name != p.""table""
                        WHERE m.type = 'table'
                        ORDER BY m.name
                            ;";

            var infoSql = @"-- noinspection SqlResolveForFile
            
            SELECT 
              *
            FROM 
              sqlite_master AS m
            JOIN 
              pragma_table_info(m.name) AS p
            ORDER BY 
              m.name, 
              p.cid";

            var sqls = new Dictionary<string, string>
            {
                { "Foreign keys", fkSql },
                { "Info", infoSql },
            };

            var res = new Dictionary<string, object>();


            foreach (var sql in sqls)
            {
                IEnumerable<dynamic> rs = await conn.QueryAsync(sql.Value);
                var rs2 = rs.Cast<IDictionary<string, object>>(); 
                var rs3 = rs2
                    .Select(x => x.Keys.Distinct().Select(k => KeyValuePair.Create(k, x[k])).ToDictionary(x2 => x2.Key, x2 => x2.Key == "sql" ? "sql!" : x2.Value))
                    .ToList();
                res.Add(sql.Key, rs2);
            }


            // res.Add("Meta", GetSchema());

            // return new object[] { tables, res};
            return res;
        }

        private DatabaseSchema GetSchema()
        {
            DbConnection conn = GetConnection();
            var dbReader = new DatabaseSchemaReader.DatabaseReader(conn);
            //Then load the schema (this will take a little time on moderate to large database structures)
            DatabaseSchema schema = dbReader.ReadAll();

            //The structure is identical for all providers (and the full framework).
            foreach (var table in schema.Tables)
            {
                //do something with your model
            }

            return schema;
        }

        private static DbConnection GetConnection() =>
            new SqliteConnection("Data Source=c:\\Development\\sample.db");
    }
}