// using System;
// using System.Collections.Generic;
// using System.ComponentModel.DataAnnotations;
// using System.ComponentModel.DataAnnotations.Schema;
// using System.Data;
// using System.Linq;
// using System.Reflection;
//
// // ReSharper disable StringLiteralTypo
//
// namespace HttpServer.DbUtil
// {
//     public class DDLCreator
//     {
//         public string GetTableName(Type dtoType) => "_" + dtoType.Name;
//
//         public ColMap[] GenerateColMapForUpdate(Type dtoType) =>
//             GenerateColMapForInsert(dtoType);
//
//         public ColMap[] GenerateColMapForInsert(Type dtoType) =>
//             dtoType.GetProperties()
//                 .Where(IsDbColumn)
//                 .Where(IsNotHistoryColumn)
//                 .Select(pi => MapToColMap(dtoType, pi))
//                 .ToArray();
//
//         private bool IsNotHistoryColumn(PropertyInfo arg)
//         {
//             return true;
//         }
//
//         public ColMap[] GenerateColMapForInsertIntoHistory(Type dtoType) =>
//             dtoType.GetProperties()
//                 .Where(IsDbColumn)
//                 .Select(pi => MapToColMap(dtoType, pi))
//                 .ToArray();
//
//         public ColMap[] GenerateFullColMap(Type dtoType) =>
//             dtoType.GetProperties()
//                 .Where(IsDbColumn)
//                 .Select(pi => MapToColMap(dtoType, pi))
//                 .ToArray();
//
//         public bool IsDbColumn(PropertyInfo arg) =>
//             !arg.GetCustomAttributes<NotMappedAttribute>().Any();
//
//         public ColMap MapToColMap(Type dtoType, PropertyInfo pi) =>
//             IsTimeStampColumn()
//                 ? ColMap.CurrentTimeStamp(pi.Name)
//                 : new ColMap(pi.Name, pi.Name, ColumnDefinition(dtoType, pi));
//
//         public string ColumnDefinition(Type dtoType, PropertyInfo pi)
//         {
//             var type = pi.PropertyType;
//             if (Nullable.GetUnderlyingType(type) != null)
//             {
//                 type = Nullable.GetUnderlyingType(type);
//             }
//
//             var isPk = IsPk(pi);
//             var isFk = pi.GetCustomAttributes<LinkToOneAttribute>().Any();
//             var isTimestamp = pi.PropertyType == typeof(DateTimeOffset) 
//                               || pi.PropertyType == typeof(DateTimeOffset?)
//                               || pi.PropertyType == typeof(DateTime?) // Technically not correct, but SysStartTime and friends must be DateTime. 
//                               ;
//             var isString = pi.PropertyType == typeof(string);
//             var isCode = pi.PropertyType.IsSubclassOf(typeof(Enumeration));
//             var isDecimal = type == typeof(decimal) || type == typeof(decimal?);
//             var isNumeric = IsNumeric(type);
//             var isBoolean = type == typeof(bool);
//             // var isClob = false; // email body? 
//             var isId = isPk || isFk;
//
//             var isHistoryColumn = DTOTypes.IsHistoryColumn(pi);
//
//             string colType;
//
//             if (isId)
//             {
//                 colType = "VARCHAR (36)";
//             }
//             else if (isHistoryColumn && _useTemporalTables)
//             {
//                 // These are the two columns that are used for temporal tables
//                 // todo: sqlite? 
//                 colType = "DATETIME2";
//             }
//             else if (isTimestamp)
//             {
//                 colType = "DATETIMEOFFSET";
//             }
//             else if (isCode)
//             {
//                 colType = "VARCHAR (36)"; // Status, SubscriptionType etc
//             }
//             else if (isDecimal)
//             {
//                 colType = "REAL";
//             }
//             else if (isNumeric)
//             {
//                 colType = "NUMERIC (18,3)";
//             }
//             else if (isBoolean)
//             {
//                 colType = "BIT";
//             }
//             else if (isString)
//             {
//                 var length = pi.GetCustomAttributes<MaxLengthAttribute>().FirstOrDefault()?.Length ?? 256;
//                 if (length > 4000)
//                 {
//                     // TODO: Should be VARCHAR(MAX) FOR SQL SERVER
//                     colType = "NTEXT";
//                 }
//                 else
//                 {
//                     colType = $"NVARCHAR ({length})";
//                 }
//             }
//             else if (type == typeof(List<Permission>))
//             {
//                 colType = $"NVARCHAR ({512})";
//             }
//             else if (type == typeof(List<SubscriptionType>))
//             {
//                 colType = $"NVARCHAR ({50})";
//             }
//             else if (type == typeof(List<string>))
//             {
//                 var length = pi.GetCustomAttributes<MaxLengthAttribute>().FirstOrDefault()?.Length ?? 512;
//                 colType = $"NVARCHAR ({length})";
//             }
//             else if (type == typeof(List<Coordinate>))
//             {
//                 // TODO: Should be VARCHAR(MAX) FOR SQL SERVER
//                 colType = "NTEXT";
//             }
//             else if (type == typeof(Coordinate))
//             {
//                 colType = "VARCHAR (36)";
//             }
//             else if (type == typeof(ICustomProperties).GetProperty(nameof(ICustomProperties.CustomProperties))?.PropertyType)
//             {
//                 colType = $"NVARCHAR ({ICustomProperties.MaxStringLength})";
//             }
//             else
//             {
//                 throw new NotSupportedException(
//                     $"unable to locate type for {pi.PropertyType} {dtoType.Name}.{pi.Name}");
//             }
//
//
//             // var type = isTimestamp ? "DATETIMEOFFSET" : "VARCHAR (64)";
//             var nullability = (IsNullable(pi) || isPk) ? "" : "NOT NULL";
//             var pk = isPk ? "PRIMARY KEY" : "";
//             var fk = "";
//             if (isFk)
//             {
//                 var to = pi.GetCustomAttributes<LinkToOneAttribute>().Single();
//                 fk = $"REFERENCES {GetTableName(to.ToType)} (Id)";
//             }
//
//             var history = "";
//             if (isHistoryColumn && _useTemporalTables)
//             {
//                 var startOrEnd = pi.Name == nameof(IHasHistory.SysStartTime) ? "START" : "END";
//                 history = $"GENERATED ALWAYS AS ROW {startOrEnd} ";
//             }
//
//             return new[] { colType, history, nullability, pk, fk }.Where(x => !string.IsNullOrWhiteSpace(x)).JoinToString(" ");
//         }
//
//         public bool IsNumeric(Type t) =>
//             t == typeof(decimal) || t == typeof(int) || t == typeof(float);
//
//
//         private static bool IsPk(PropertyInfo pi) => pi.Name == nameof(DTOBase.Id);
//
//         private static bool IsNullable(PropertyInfo pi) =>
//             pi.GetCustomAttributes<OptionalAttribute>().Any();
//
//         public string CreateSql(Type dtoType)
//         {
//             var tableName = GetTableName(dtoType);
//             var colSpecs = GenerateFullColMap(dtoType);
//             var constraints = ExtractConstraints(dtoType);
//
//             var maxName = colSpecs.Select(x => x.Column.Length).Max();
//             var colDefs = colSpecs.Select(x => x.Column.PadRight(maxName) + " " + x.Definition).ToList();
//
//             var with = "";
//             if (DTOTypes.HasHistory(dtoType) && _useTemporalTables)
//             {
//                 colDefs.Add($"PERIOD FOR SYSTEM_TIME ({nameof(IHasHistory.SysStartTime)}, {nameof(IHasHistory.SysEndTime)})");
//                 const string schemaName = "dbo"; // Schema name is required... is it always dbo?  
//                 with = $"WITH (SYSTEM_VERSIONING = ON (HISTORY_TABLE = {schemaName}.{GetHistoryTableName(dtoType)}));";
//             }
//
//
//             var allDefs = string.Join(", \r\n  ", colDefs.Concat(constraints).ToArray());
//
//             var ddl = $"CREATE TABLE {tableName} (\r\n  {allDefs}\r\n) {with}";
//             return ddl;
//         }
//
//         public string GetHistoryTableName(Type dtoType) => 
//             GetTableName(dtoType) + "History";
//
//         public string[] ExtractConstraints(Type dtoType)
//         {
//             var constraintNames = dtoType.GetProperties()
//                 .SelectMany(x => x.GetCustomAttributes<UniqueIndexAttribute>())
//                 .Select(attr => attr.Name)
//                 .Distinct()
//                 .ToList();
//             return constraintNames.Select(n => ExtractConstraint(dtoType, n)).ToArray();
//         }
//
//         private string ExtractConstraint(Type dtoType, string name)
//         {
//             var cols = dtoType.GetProperties()
//                 .Where(p => p.GetCustomAttributes<UniqueIndexAttribute>().Any(a => a.Name == name))
//                 .Select(p => p.Name)
//                 .Distinct()
//                 .ToList();
//             return $"CONSTRAINT {name} UNIQUE ({cols.JoinToString()})";
//         }
//     }
// }