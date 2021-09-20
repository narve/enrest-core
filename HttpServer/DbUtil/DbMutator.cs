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

        public async Task<IDictionary<string, object>> UpdateRow(string table, string id, IEnumerable<KeyValuePair<string, object>> formValues)
        {
            var conn = _connectionProvider.Get();
            var tableInfo = _dbInspector.GetSchema().FindTableByName(table);

            var parameters = new SortedDictionary<string, object>();
            foreach (var kvp in formValues)
            {
                var columnInfo = tableInfo.FindColumn(kvp.Key);
                // parameters.Add(kvp.Key, Coerce(columnInfo, kvp.Value.SingleOrDefault()));
                parameters.Add(kvp.Key, Coerce(columnInfo, kvp.Value));
            }

            var colNames = parameters.Keys.Where(x => Regex.IsMatch(x, "^[\\w]+$")).ToList();
            var pk = _dbInspector.GetPkColumn(table);
            parameters.Add(pk.Name, Coerce(pk, id));


            var sql = new[]
            {
                $"UPDATE {table}",
                // $"({colNames.JoinToString()})",
                $"SET {colNames.Select(x => $"{x} = @" + x).JoinToString()} " +
                $"WHERE id = @id " +
                $"RETURNING {table}.*"
            }.JoinToString(" \r\n");
            var obj = await conn.QuerySingleAsync(sql, parameters, null);
            return obj;
        }

        public object Coerce(DatabaseColumn columnInfo, object value)
        {
            if (value == null) return null;
            if (value is string nullString && string.IsNullOrEmpty(nullString)) return null;
            if (columnInfo.DataType.IsInt && value is string intString) return int.Parse(intString);
            return value;
        }

        public async Task<IDictionary<string, object>> GetById(string table, string id)
        {
            var t = _dbInspector.GetSchema().FindTableByName(table);
            var idColName = t.PrimaryKey.Columns.Single();
            var idCol = t.FindColumn(idColName);
            var idExp = idCol.DataType.IsString ? idColName : $"cast ({idCol.Name} as varchar)";
            var sql = $"select * from {table} where {idExp} = @id";
            var dyn = await _connectionProvider.Get().QuerySingleAsync(sql, new { id });
            var dict = (IDictionary<string, object>)dyn;
            return dict;
        }
    }
}