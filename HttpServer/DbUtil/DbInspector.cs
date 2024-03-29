﻿using System;
using System.Collections.Generic;
using System.Data.Common;
using System.Linq;
using System.Threading.Tasks;
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
            _schema ??= LoadSchema().GetAwaiter().GetResult();
            return _schema;
        }

        public async Task ReloadSchema() => _schema = await LoadSchema();

        public async Task<DatabaseSchema> LoadSchema()
        {
            var conn = await _connectionProvider.Get();
            using var dbReader = new DatabaseSchemaReader.DatabaseReader((DbConnection)conn);
            var schema = dbReader.ReadAll();

            var colSql =
                "SELECT table_name, column_name, is_identity, identity_generation, is_generated, is_updatable " +
                "from information_schema.columns where table_schema in ( 'public', 'auth')";
            IEnumerable<dynamic> cols = conn.Query(colSql).ToList();
            var tabs = cols.Select(x => x.table_name).ToHashSet();

            tabs.Add("users");

            foreach (var table in schema.Tables)
            {
                if (table.Name.Equals("users"))
                {
                    // continue;
                }
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