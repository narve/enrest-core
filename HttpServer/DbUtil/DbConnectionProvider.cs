using System;
using System.Data;
using Microsoft.Data.Sqlite;
using Microsoft.Extensions.Configuration;
using Npgsql;

namespace HttpServer.DbUtil
{
    public class DbConnectionProvider : IDbConnectionProvider
    {
        private readonly IConfiguration _configuration;
        private IDbConnection _connection;

        public DbConnectionProvider(IConfiguration configuration)
        {
            _configuration = configuration;
        }

        void IDisposable.Dispose() => _connection?.Dispose();

        public IDbConnection Get()
        {
            _connection ??= CreateConnection();
            return _connection;
        }

        private IDbConnection CreateConnection() =>
            PgConnection();

        private IDbConnection PgConnection() =>
            new NpgsqlConnection(_configuration.GetConnectionString("SupabaseEdna"));

        private IDbConnection SqliteConnection() =>
            new SqliteConnection(_configuration.GetConnectionString("SqliteSample"));
    }
}