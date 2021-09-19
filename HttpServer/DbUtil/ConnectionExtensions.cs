// using System;
// using System.Data;
// using System.Threading.Tasks;
// using Dapper;
// using Microsoft.Data.Sqlite;
// using Nodes.API.Database;
//
// namespace HttpServer.DbUtil
// {
//     public static class ConnectionExtensions
//     {
//
//        public static async Task<T> GetById<T>(this IDbConnection connection, string id, IDbTransaction transaction = null)
//             => (T)await GetById(connection, id, typeof(T), transaction);
//
//         public static async Task<object> GetById(this IDbConnection connection, string id, Type dtoType, IDbTransaction transaction = null)
//         {
//             var tableName = DDLCreator.CreateFor(connection).GetTableName(dtoType);
//             var sql = $"SELECT * FROM {tableName} WHERE Id = @Id";
//             var obj = await connection.QuerySingleOrDefaultAsync(dtoType, sql, new { Id = id }, transaction: transaction);
//             return obj;
//         }
//
//         public static void OpenWithRetry(this IDbConnection connection)
//         {
//             if (connection.State != ConnectionState.Open)
//             {
//                 Retry.Do(connection.Open, TimeSpan.FromSeconds(1));
//             }
//         }
//
//         public static async Task<T> Insert<T>(this IDbConnection connection, T item, IDbTransaction transaction = null) where T : DTOBase
//         {
//             if (item.Id == null)
//                 throw new ArgumentException("Missing ID for insert");
//
//             var ddl = DDLCreator.CreateFor(connection);
//             var dml = new DMLCreator(connection is SqliteConnection);
//
//             var insertColMap = ddl.GenerateColMapForInsert(typeof(T));
//             if (connection is SqlConnection)
//             {
//                 var sql = dml.InsertSql(ddl.GetTableName(typeof(T)), insertColMap);
//
//                 // TODO: These lines are only for debugging (and perhaps re-running) the generated SQL
//                 // including variables. NOT SAFE due to risk of Sql injection attacks.
//                 if (DTOTypes.HasHistory(typeof(T)))
//                 {
//                     // ReSharper disable once UnusedVariable
//                     var unsafeSql = CreateUnsafeSql(sql, item, ddl.GenerateColMapForInsert(typeof(T)));
//                 }
//
//
//                 return await connection.QuerySingleAsync<T>(sql, item, transaction);
//             }
//             else
//             {
//                 var sql = dml.InsertSql(ddl.GetTableName(typeof(T)), insertColMap, false);
//                 var rows = await connection.ExecuteAsync(sql, item, transaction);
//                 if (rows != 1)
//                 {
//                     throw new ArgumentException("wtf");
//                 }
//
//                 return await GetById<T>(connection, item.Id);
//             }
//         }
//
//         private static string CreateUnsafeSql(string sql, object obj, ColMap[] generateColMap)
//         {
//             foreach (var col in generateColMap)
//             {
//                 var prop = obj.GetType().GetProperty(col.Property);
//                 var val = prop?.GetValue(obj);
//                 var s = val?.ToString() ?? "NULL";
//                 s = s.Replace("'", "''");
//                 sql = sql.Replace($"@{col.Property}", $"'{s}'");
//             }
//
//             return sql;
//         }
//
//
//         public static async Task<T> Update<T>(this IDbConnection connection, T item, IDbTransaction transaction = null) where T : DTOBase =>
//             (T)await Update(connection, item.Id, item, item.GetType(), transaction);
//
//         public static async Task<object> Update(this IDbConnection connection, string id, object item, Type dtoType,
//             IDbTransaction transaction = null)
//         {
//             var ddl = DDLCreator.CreateFor(connection);
//             return await Update(connection, id, item, dtoType, ddl.GenerateColMapForUpdate(dtoType), transaction);
//         }
//
//         public static async Task<object> Update(this IDbConnection connection, string id, object item, Type dtoType, ColMap[] columnsToUpdate,
//             IDbTransaction transaction = null)
//         {
//             var ddl = DDLCreator.CreateFor(connection);
//             var tableName = ddl.GetTableName(dtoType);
//             var sql = new DMLCreator(connection is SqliteConnection).UpdateSql(tableName, id, columnsToUpdate);
//             var count = await connection.ExecuteAsync(sql, item, transaction);
//             if (count != 1) throw new Exception($"Update returned {count} affected rows, expected 1. Table='{tableName}', id='{id}'");
//             return item;
//         }
//
//         public static bool AreTemporalTablesSupported(this IDbConnection c) =>
//             c is SqlConnection;
//     }
// }