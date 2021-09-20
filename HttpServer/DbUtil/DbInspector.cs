using System;
using System.Collections.Generic;
using System.Data.Common;
using System.Linq;
using Dapper;
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
            var schema = dbReader.ReadAll();

            var colSql =
                "SELECT table_name, column_name, is_identity, identity_generation, is_generated, is_updatable " +
                "from information_schema.columns where table_schema = 'public'";
            IEnumerable<dynamic> cols = conn.Query(colSql).ToList();
            var tabs = cols.Select(x => x.table_name).ToHashSet();

            foreach (var table in schema.Tables)
            {
                // var tabs = new[] { "item", "location" };
                if (!tabs.Contains(table.Name))
                    continue;

                foreach (var column in table.Columns)
                {
                    var colInfo = cols.SingleOrDefault(c => c.table_name == table.Name && c.column_name == column.Name);
                    if (colInfo == null)
                    {
                        var colsForTable = cols.Where(c => c.table_name == table.Name).ToList();
                        throw new Exception("wtf");
                    }
                    var isIdentityString = colInfo.is_identity;
                    var isIdentity = "YES".Equals(isIdentityString);
                    if (isIdentity)
                    {
                        if (column.IdentityDefinition == null)
                        {
                            column.IdentityDefinition = new DatabaseColumnIdentity();
                        }
                    }
                }
            }

            return schema;
        }

    }
}