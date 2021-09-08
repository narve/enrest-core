using System;
using System.Data;
using Microsoft.Data.Sqlite;
using Npgsql;

namespace HttpServer.DbUtil
{
    public class DbConnectionProvider : IDbConnectionProvider
    {
        private IDbConnection _connection;

        void IDisposable.Dispose() => _connection?.Dispose();

        public IDbConnection Get()
        {
            _connection ??= CreateConnection();
            return _connection;
        }

        private IDbConnection CreateConnection() =>
            PgConnection();

        private IDbConnection PgConnection()
        {
            // var s = "postgres://postgres:[YOUR-PASSWORD]@db.xupzhicrqmyvtgztrmjb.supabase.co:6543/postgres";
            // var s = "postgres://postgres:ur4MKwGTXtW9Eat@db.xupzhicrqmyvtgztrmjb.supabase.co:6543/postgres";
            var s = "Host=db.xupzhicrqmyvtgztrmjb.supabase.co;Username=postgres;Password=ur4MKwGTXtW9Eat;Database=postgres";

            return new NpgsqlConnection(s);
        }

        private IDbConnection SqliteConnection() =>
            new SqliteConnection("Data Source=c:\\Development\\sample.db");
    }
}