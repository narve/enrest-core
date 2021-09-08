using System;
using System.Linq;
using DatabaseSchemaReader.DataSchema;
using static System.String;
using static System.StringComparison;

namespace HttpServer.DbUtil
{
    public interface IDbInspector
    {
        DatabaseSchema GetSchema();

        bool IsFk(string table, string key) => 
            GetFk(table, key) != null; 
        
        DatabaseConstraint GetFk(string table, string key) =>
            GetSchema().Tables
                .SingleOrDefault(x => x.Name.Equals(table, OrdinalIgnoreCase))
                .ForeignKeys
                .SingleOrDefault(fk => fk.Columns.Single().Equals(key, OrdinalIgnoreCase));

        string GetFkTarget(string table, string key) => 
            GetFk(table, key).RefersToTable;
    }
}