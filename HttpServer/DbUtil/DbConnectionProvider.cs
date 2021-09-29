using System;
using System.Collections.Generic;
using System.Data;
using System.Threading.Tasks;
using Dapper;
using Microsoft.AspNetCore.Http;
using Microsoft.Data.Sqlite;
using Microsoft.Extensions.Configuration;
using Npgsql;

namespace HttpServer.DbUtil
{
    public class DbConnectionProvider : IDbConnectionProvider
    {
        private readonly IConfiguration _configuration;
        private readonly IHttpContextAccessor _accessor;
        private IDbConnection _connection;

        public DbConnectionProvider(IConfiguration configuration, IHttpContextAccessor accessor)
        {
            _configuration = configuration;
            _accessor = accessor;
        }

        void IDisposable.Dispose() => _connection?.Dispose();

        public async Task<IDbConnection> Get()
        {
            _connection ??= await CreateConnection();
            return _connection;
        }

        private Task<IDbConnection> CreateConnection() =>
            PgConnection();

        private async Task<IDbConnection> PgConnection()
        {
            var conn = new NpgsqlConnection(_configuration.GetConnectionString("SupabaseEdna"));
            await conn.OpenAsync();
            var role = _accessor?.HttpContext?.Request.Cookies?["username"];
            if (role != null)
            {
                var uid = role.Split("::")[1];
                await conn.ExecuteAsync($"select set_config('request.jwt.claim.sub', '{uid}', false)");
                await conn.ExecuteAsync("SET ROLE authenticated");
                var rsPost = await conn.QuerySingleAsync("SELECT concat( SESSION_USER, ', ', CURRENT_USER)");
            }
            else
            {
                await conn.ExecuteAsync("SET ROLE anon");
            }

            return conn;
        }

        private IDbConnection SqliteConnection() =>
            new SqliteConnection(_configuration.GetConnectionString("SqliteSample"));
    }
}