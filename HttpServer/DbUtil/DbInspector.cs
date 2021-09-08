using System.Data.Common;
using DatabaseSchemaReader.DataSchema;

namespace HttpServer.DbUtil
{
    public class DbInspector : IDbInspector
    {
        private readonly IDbConnectionProvider _connectionProvider;
        private DatabaseSchema _schema;

        public DbInspector(IDbConnectionProvider connectionProvider) =>
            _connectionProvider = connectionProvider;

        public DatabaseSchema GetSchema()
        {
            _schema ??= LoadSchema();
            return _schema;
        }

        public DatabaseSchema LoadSchema()
        {
            var conn = _connectionProvider.Get();
            using var dbReader = new DatabaseSchemaReader.DatabaseReader((DbConnection)conn);
            return dbReader.ReadAll();
        }
    }
}