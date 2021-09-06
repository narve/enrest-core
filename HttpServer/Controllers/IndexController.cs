using System;
using System.Collections.Generic;
using System.Data;
using System.Data.SqlClient;
using System.Linq;
using System.Threading.Tasks;
using Dapper;
using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;
using Microsoft.Data.Sqlite;
using Microsoft.Extensions.Logging;
using Microsoft.Identity.Web.Resource;

namespace HttpServer.Controllers
{
    // [Authorize]
    [ApiController]
    [Route("/")]
    public class IndexController
    {
        private readonly ILogger<IndexController> _logger;

        public IndexController(ILogger<IndexController> logger) => _logger = logger;

        [HttpGet]
        public async Task<IEnumerable<object>> Get()
        {
            // IDbConnection conn = new SqliteConnection("Data Source=:memory:");
            IDbConnection conn = new SqliteConnection("Data Source=c:\\Development\\sample.db");

            // var res1 = await conn.ExecuteAsync("create table User (id varchar(32) primary key, handle varchar(64) not null unique)");
            // var res2 = await conn.ExecuteAsync("insert into User (id, handle) values ('id1', 'handle1')");

            var infoSql = @"SELECT 
  *
FROM 
  sqlite_master AS m
JOIN 
  pragma_table_info(m.name) AS p
ORDER BY 
  m.name, 
  p.cid";
            

            var rs1 = await conn.QueryAsync("select * from sqlite_schema");
            var rs2 = await conn.QueryAsync("select * from User");
            var rs3 = await conn.QueryAsync(infoSql);




            return new[] { new { Message = "Hei", Schema = rs1, Users = rs2, Columns = rs3 } };
        }
    }
}