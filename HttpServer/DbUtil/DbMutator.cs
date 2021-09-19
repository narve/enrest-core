using System.Collections.Generic;
using System.Linq;
using System.Text.RegularExpressions;
using System.Threading.Tasks;
using Dapper;
using DatabaseSchemaReader.DataSchema;
using Microsoft.Extensions.Primitives;

namespace HttpServer.DbUtil
{
    public class DbMutator
    {
        private readonly IDbInspector _dbInspector;
        private readonly IDbConnectionProvider _connectionProvider;

        public DbMutator(IDbInspector dbInspector, IDbConnectionProvider connectionProvider)
        {
            _dbInspector = dbInspector;
            _connectionProvider = connectionProvider;
        }

        public async Task<IDictionary<string, object>> InsertRow(string table, IEnumerable<KeyValuePair<string, StringValues>> formValues)
        {
            var conn = _connectionProvider.Get();
            var tableInfo = _dbInspector.GetSchema().FindTableByName(table);

            var parameters = new SortedDictionary<string, object>();
            foreach (var kvp in formValues)
            {
                var columnInfo = tableInfo.FindColumn(kvp.Key);
                parameters.Add(kvp.Key, Coerce(columnInfo, kvp.Value.SingleOrDefault()));
            }

            var colNames = parameters.Keys.Where(x => Regex.IsMatch(x, "^[\\w]+$")).ToList();

            var sql = new[]
            {
                $"INSERT INTO {table}",
                $"({colNames.JoinToString()})",
                $"VALUES ({colNames.Select(x => "@" + x).JoinToString()}) " +
                $"RETURNING {table}.*"
            }.JoinToString(" \r\n");
            var ins = await conn.QuerySingleAsync(sql, parameters, null);
            return ins;
        }

        public object Coerce(DatabaseColumn columnInfo, string value)
        {
            if (value == null) return null;
            if (columnInfo.DataType.IsInt) return int.Parse(value);
            return value;
        }
    }
}