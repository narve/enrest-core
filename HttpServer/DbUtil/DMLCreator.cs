// using System;
// using System.Collections.Generic;
// using System.Linq;
// using HttpServer.DbUtil;
// using Nodes.API.Queries;
// using Nodes.API.Support;
// using static System.String;
//
// // ReSharper disable ParameterTypeCanBeEnumerable.Global
//
// namespace Nodes.API.Database
// {
//     // TODO: Refactor this using database dialects properly
//     public class DMLCreator
//     {
//         private readonly bool _sqlLite;
//
//         // public DMLCreator(IDbConnection connection) => _sqlLite = connection is SqliteConnection;
//         public DMLCreator(bool isSqlLite) => _sqlLite = isSqlLite;
//
//         public string InsertSql(string tableName, ColMap[] colSpecs, bool includeOutput = true)
//         {
//             var colNames = Join("\r\n   , ", colSpecs.Select(x => x.Column));
//             var valRefs = Join("\r\n   , ", colSpecs.Select(x => x.SqlReference));
//             var output = includeOutput ? "OUTPUT INSERTED.*" : "";
//             return $"INSERT INTO {tableName} ( {colNames} ) {output} VALUES ( {valRefs} )";
//         }
//
//         public string UpdateSql(string tableName, string id, ColMap[] colSpecs)
//         {
//             var updates = colSpecs.Select(x => $"{x.Column} = {x.SqlReference}");
//             return $"UPDATE {tableName} SET {updates.JoinToString()} WHERE id = @Id";
//         }
//
//         public string SearchSql(string tableName, List<KeyValuePair<string, IFilter>> filterSpecs,
//             SearchOptions options)
//         {
//             options ??= new SearchOptions();
//
//             var temporalFilter = options.Historical == default
//                 ? ""
//                 : options.Historical.ToSqlTerm();
//
//             var where = (filterSpecs?.Any() ?? false)
//                 ? " WHERE " + filterSpecs.Select(kvp => kvp.Value.ToSqlTerm(kvp.Key, _sqlLite)).JoinToString(" AND ")
//                 : "";
//
//             // orderBy always includes `Id` to ensure predictable ordering of results.
//             var orderByWithId = options.OrderBy.Contains(nameof(DTOBase.Id), StringComparer.OrdinalIgnoreCase)
//                 ? options.OrderBy
//                 : options.OrderBy.Concat(new[] { nameof(DTOBase.Id) });
//             var orderBy = orderByWithId.JoinToString();
//
//             if (_sqlLite)
//             {
//                 var limit = $"LIMIT {options.Take}";
//                 if (options.Skip > 0) limit += $" OFFSET {options.Skip}";
//                 return $"SELECT * FROM {tableName}{where} ORDER BY {orderBy} {limit}";
//             }
//             else if (options.Skip > 0)
//             {
//                 var limit = $" OFFSET {options.Skip} ROWS FETCH NEXT {options.Take} ROWS ONLY";
//                 return $"SELECT * FROM {tableName}{where} ORDER BY {orderBy} {limit}";
//             }
//             else
//             {
//                 return $"SELECT TOP {options.Take} * FROM {tableName} {temporalFilter} {where} ORDER BY {orderBy}";
//             }
//         }
//
//         public string CountSql(string tableName, List<KeyValuePair<string, IFilter>> filterSpecs)
//         {
//             var where = (filterSpecs?.Any() ?? false)
//                 ? " WHERE " + filterSpecs.Select(kvp => kvp.Value.ToSqlTerm(kvp.Key, _sqlLite)).JoinToString(" AND ")
//                 : "";
//             return $"SELECT COUNT(Id) FROM {tableName}{where}";
//         }
//     }
// }